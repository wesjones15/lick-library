# Lick Library — Backend

A Spring Boot service for storing and exploring guitar licks and song chord sheets. Upload a tab once; retrieve playable positions in any key on any instrument. Upload a chord sheet; transpose it to any key on the fly.

---

## Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.0 |
| Database | H2 (embedded, file-persisted at `./data/licklibrary`) |
| ORM | JPA / Hibernate |
| Tests | JUnit 5 + Mockito |

---

## Running

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080/api`.  
H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/licklibrary`).

---

## API

### Upload a lick

```
POST /api/lick
Content-Type: application/json

{
  "rawTab": "E|--5-7-|\nA|------|\n...",
  "mode":     "DORIAN",    // optional — auto-detected if omitted
  "inputKey": "A"          // optional — defaults to first note in tab
}
```

Returns a summary response. Deduplicates by interval hash — uploading the same musical shape twice returns the existing record.

---

### List all licks

```
GET /api/lick
```

Returns all licks as summary objects (no positions).

---

### Get a lick with positions

```
GET /api/lick/{id}?key=A&algo=greedy&instrument=GUITAR
```

| Param | Default | Description |
|---|---|---|
| `key` | *(required)* | Root note to render in. Accepts `A`, `C_SHARP`, `B_FLAT`, etc. |
| `algo` | `greedy` | Position-finding algorithm: `greedy`, `dfs`, or `chord` |
| `instrument` | `GUITAR` | Named instrument preset (see table below) |
| `tuning` | — | Custom tuning, overrides `instrument`. Space-separated note names: `E A D G B E` |

Returns the full lick response including `mode` and a list of rendered position tabs.

---

### Delete a lick

```
DELETE /api/lick/{id}
```

Returns 204.

---

### Upload a song

```
POST /api/song
Content-Type: application/json

{
  "title":         "Blackbird",
  "artist":        "The Beatles",
  "originalKey":   "G",
  "capo":          2,         // optional
  "tempo":         92,        // optional
  "rawChordSheet": "..."      // plain-text chord sheet
}
```

The raw chord sheet is parsed into a list of `ChordLyric` pairs (chord row + lyric row). Font size is auto-computed based on line length and column count (2 or 3 columns).

---

### List all songs

```
GET /api/song
```

Returns all songs as summary objects (`id`, `title`, `artist`, `originalKey`).

---

### Get a song

```
GET /api/song/{id}?semitones=0
```

| Param | Default | Description |
|---|---|---|
| `semitones` | `0` | Transpose the chord sheet by this many semitones at response time |

Returns the full song including `chordLines` (list of ChordLyric), `numColumns`, `capo`, `tempo`, and `originalKey`.

---

### Re-parse a song

```
POST /api/song/{id}/reparse
```

Re-runs the chord sheet parser on the stored `rawChordSheet`. Useful after parser logic updates. Returns the updated song detail.

---

### Delete a song

```
DELETE /api/song/{id}
```

Returns 204.

---

### Get chord voicings

```
GET /api/chord?root=A&quality=m7&instrument=GUITAR
```

| Param | Default | Description |
|---|---|---|
| `root` | *(required)* | Root note: `A`, `C_SHARP`, `B_FLAT`, etc. |
| `quality` | *(required)* | Chord quality (see table below) |
| `instrument` | `GUITAR` | Named instrument preset |

Returns `List<String>` — ASCII tab voicings. For `instrument=GUITAR`, returns up to 5 real CAGED shapes transposed to the requested root. For other instruments, returns an empty list until shapes are seeded for them. Unknown quality → 400.

| `quality=` | Chord |
|---|---|
| *(empty string)* | Major |
| `m` | Minor |
| `7` | Dominant 7th |
| `maj7` | Major 7th |
| `m7` | Minor 7th |
| `sus2` | Suspended 2nd |
| `sus4` | Suspended 4th |
| `dim` | Diminished |
| `aug` | Augmented |
| `add9` | Add 9 |
| `6` | Major 6th |
| `m6` | Minor 6th |
| `dim7` | Diminished 7th |
| `m7b5` | Half-diminished |

---

## Instruments

| `instrument=` value | Tuning (low → high) | Strings |
|---|---|---|
| `GUITAR` | E A D G B E | 6 |
| `DROP_D` | D A D G B E | 6 |
| `OPEN_G` | D G D G B D | 6 |
| `OPEN_D` | D A D F# A D | 6 |
| `DADGAD` | D A D G A D | 6 |
| `BASS` | E A D G | 4 |
| `UKULELE` | G C E A | 4 |
| `MANDOLIN` | G D A E | 4 |
| `BANJO` | D G B D G | 5 |

For any other tuning, pass `tuning=E A D G B E` instead of `instrument=`. Accepts natural notes (`E`, `A`), sharps (`C#`, `F#`), and flats (`Bb`).

---

## Position-finding algorithms

### `greedy`
Single-pass nearest-neighbour. For each root candidate on the neck, picks the closest valid next note (by Euclidean fret+string distance) at every step. Fast; produces one path per root. Good default.

### `dfs`
Depth-first search from each root candidate. Explores the N closest candidates per note (N scales down for longer licks to bound the search), then deduplicates by string-pattern + fret-bin so only genuinely distinct fingerings survive. Results are interleaved by starting string for variety.

### `chord`
Loser-bracket two-pass algorithm. First pass places the melodic line greedily. Second pass fits simultaneously-played notes (same column in the tab) near their companion notes on adjacent strings. The only algorithm that correctly handles chords; partial positions are returned when a chord partner can't fit the span. Also used internally by `ChordService` to generate voicings.

All algorithms filter out positions with any note above fret 15 or with a span exceeding the lick's original fret range (minimum 4 frets).

---

## Upload pipeline

```
rawTab
  │
  ▼  parseTab
List<TabNote>          — stringIndex, fret, columnIndex, technique
  │
  ▼  LickUtils.toIntervals(notes, rootKey, instrument)
List<IntervalNote>     — interval (ONE…SEVEN + flats), technique, columnIndex
  │
  ▼  LickUtils.hashIntervals
SHA-256 of interval names (technique-agnostic dedup key)
  │
  ├─ duplicate? → return existing Lick
  │
  ▼  LickUtils.detectMode (or user-supplied override)
Mode                   — IONIAN / DORIAN / … / LOCRIAN
  │
  ▼  persist Lick
```

**Deduplication** is by interval hash only. Two tabs with the same note intervals but different articulation (hammer-ons vs. slides) store as one lick. Two tabs with the same notes but different `inputKey` values store separately (different interval relationships).

**Mode detection** uses flat-interval elimination: each flat interval present in the lick rules out incompatible modes. Ties break toward more common modes (Ionian > Aeolian > …).

---

## Lookup pipeline

```
GET /api/lick/{id}?key=A&algo=greedy&instrument=BASS
  │
  ▼  fetch Lick by ID
  │
  ▼  LickUtils.toAbsoluteNotes(intervals, key)
List<Note>             — absolute pitch sequence for the requested key
  │
  ▼  PositionBuilder.build(intervals, key, spanLimit, instrument)
List<Position>         — each Position is a List<TabNote> on the target instrument
  │
  ▼  Position.toTabString(instrument)
String                 — column-aligned ASCII tab using instrument's string labels
```

---

## Chord sheet pipeline

```
rawChordSheet (plain text)
  │
  ▼  ChordSheetParser.parse
List<ChordLyric>       — chord row + lyric row pairs, section headers, spacers
  │                       font size auto-computed per pair; long lines broken at word boundaries
  ▼  persist Song
```

```
GET /api/song/{id}?semitones=N
  │
  ▼  fetch Song
  │
  ▼  ChordTransposer.transpose(chordLines, semitones)
List<ChordLyric>       — all chord tokens shifted N semitones; slash chords handled per-root
  │
  ▼  SongDetailResponse
```

---

## Tab format

Positions are rendered as standard ASCII tab. String labels reflect the actual tuning:

```
e|------5-7-|    ← highest string, lowercase
B|---------7-|
G|--6-7------|
D|-----------|
A|-----------|
E|-----------|    ← lowest string
```

For alternate tunings, labels change accordingly — Drop D's low string shows `D|`, Open G shows `d|B|G|D|G|D|`, etc.

Technique characters appear between notes in the same slot:

| Char | Meaning |
|---|---|
| `h` | Hammer-on |
| `p` | Pull-off |
| `/` | Slide up |
| `\` | Slide down |

---

## Project structure

Domain-vertical (DDD) layout. Each domain imports only from `domain/shared/` and `domain/position/` — no cross-domain imports.

```
src/main/java/org/jones/licklibrary/
├── core/
│   ├── config/CorsConfig.java           allows GET POST DELETE from localhost:5173
│   └── exception/ResourceNotFoundException.java  @ResponseStatus(NOT_FOUND)
│
└── domain/
    ├── shared/                          shared kernel — imported by all domains
    │   ├── Note.java                    enum — 12-tone chromatic scale
    │   ├── Interval.java                enum — scale degrees with display names
    │   ├── Mode.java                    enum — IONIAN … LOCRIAN
    │   ├── TabNote.java                 record — raw parsed note
    │   ├── IntervalNote.java            record — interval + technique + columnIndex
    │   ├── Position.java                record — List<TabNote> + toTabString(Instrument)
    │   ├── Instrument.java              interface
    │   ├── InstrumentRegistry.java      name → Instrument lookup
    │   ├── NoteParser.java              "C#" / "Bb" → Note enum
    │   └── instrument/
    │       ├── Guitar.java              STANDARD, DROP_D, OPEN_G, OPEN_D, DADGAD
    │       ├── Bass.java
    │       ├── Ukulele.java
    │       ├── Mandolin.java
    │       ├── Banjo.java
    │       └── CustomInstrument.java    built from tuning string at request time
    │
    ├── position/                        position-finding infrastructure
    │   ├── PositionCache.java           JPA entity (reserved for caching)
    │   ├── PositionCacheRepository.java
    │   ├── LickUtils.java               toIntervals, toAbsoluteNotes, hashIntervals, detectMode
    │   └── builder/
    │       ├── PositionBuilder.java     abstract base: findNeckPositions, findCandidates
    │       ├── GreedyPositionBuilder.java
    │       ├── DfsPositionBuilder.java
    │       └── LoserBracketPositionBuilder.java
    │
    ├── lick/
    │   ├── Lick.java                    JPA entity
    │   ├── LickController.java          REST endpoints: POST/GET/DELETE /api/lick
    │   ├── LickRepository.java
    │   ├── LickService.java             upload + lookup orchestration, tab parsing
    │   ├── IntervalNoteListConverter.java  JPA converter for List<IntervalNote>
    │   └── dto/
    │       ├── LickResponse.java
    │       ├── PositionResponse.java
    │       └── UploadLickRequest.java
    │
    ├── song/
    │   ├── Song.java                    JPA entity
    │   ├── SongController.java          REST endpoints: POST/GET/DELETE /api/song
    │   ├── SongRepository.java
    │   ├── SongService.java
    │   ├── dto/
    │   │   ├── SongDetailResponse.java
    │   │   ├── SongSummaryResponse.java
    │   │   └── UploadSongRequest.java
    │   └── parsing/
    │       ├── ChordLyric.java          record — chords, lyrics, fontSize
    │       ├── ChordLyricListConverter.java  JPA converter (JSON via Jackson)
    │       ├── ChordSheetParser.java    raw text → List<ChordLyric>; font sizing, line breaking
    │       └── ChordTransposer.java     transposes ChordLyric list by N semitones
    │
    └── chord/
        ├── ChordQuality.java            JPA entity — chord suffix
        ├── ChordShape.java              JPA entity — CAGED template frets
        ├── ChordQualityRepository.java
        ├── ChordShapeRepository.java
        ├── ChordService.java            chord quality maps; voicings via shape transposition
        ├── ChordShapeSeed.java          seeds 70 CAGED shapes on first boot
        └── ChordController.java         GET /api/chord
```

---

## Data model

### `Lick` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `interval_hash` | VARCHAR(64) | SHA-256, UNIQUE — dedup key |
| `intervals` | TEXT | `"1:0:,b3:1:h,4:2:,5:3:"` — displayName:columnIndex:technique |
| `raw_tab` | TEXT | Original uploaded tab |
| `mode` | VARCHAR(16) | Mode enum name |
| `tab_span` | INTEGER | max fret − min fret of original tab; used as span limit |
| `endpoint_degree` | VARCHAR(16) | Reserved for solo chaining |
| `created_at` | TIMESTAMP | |

### `Song` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `title` | VARCHAR | |
| `artist` | VARCHAR | |
| `original_key` | VARCHAR | |
| `capo` | INTEGER | |
| `tempo` | INTEGER | |
| `chord_lines` | TEXT | JSON list of ChordLyric objects |
| `num_columns` | INTEGER | 2 or 3, computed at parse time |
| `raw_chord_sheet` | TEXT | Original upload; used by reparse |
| `created_at` | TIMESTAMP | |

`ChordLyric` record: `chords` (String), `lyrics` (String), `fontSize` (double). Section headers and spacers are stored as ChordLyric rows with empty fields.

### `position_cache` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `interval_hash` | VARCHAR(64) | |
| `note_key` | VARCHAR(8) | |
| `positions_json` | TEXT | |

UNIQUE on `(interval_hash, note_key)`. Table exists but is not actively used — positions are recomputed on every request.

### `chord_quality` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `suffix` | VARCHAR | UNIQUE — `""`, `"m"`, `"7"`, `"maj7"`, … |

14 rows seeded on first startup. `suffix` matches the `quality=` request param accepted by `GET /api/chord`.

### `chord_shape` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `chord_quality_id` | UUID | FK → `chord_quality` |
| `shape_name` | VARCHAR | `CAGED_E`, `CAGED_A`, `CAGED_G`, `CAGED_C`, `CAGED_D` |
| `template_frets` | TEXT | JSON array — `"x"` muted, `-1` stays open, `0+` fretted (offset added on transpose) |
| `root_string` | INTEGER | Index into instrument tuning (0 = lowest string) |
| `instrument` | VARCHAR | `"GUITAR"` — matches `InstrumentRegistry` key |
| `source` | VARCHAR | `"system"` for seed rows; `"song:{id}"` for future custom voicings |
| `label` | VARCHAR | Nullable — for future admin UI |

70 rows seeded on first startup (5 CAGED shapes × 14 qualities). `GET /api/chord?instrument=GUITAR` transposes the matching template to the requested root and formats it as an ASCII tab string. Other instruments return an empty list until shapes are seeded for them.

---

## Running tests

```bash
mvn test
```

Tests use an in-memory H2 database (`jdbc:h2:mem:testdb`, `create-drop`) so they are fully isolated.

---

## Known limitations

- **Banjo 5th-string drone** — the fifth string starts at fret 5, not fret 0. `minFret()` override not yet implemented; positions involving string 5 may be incorrect.
- **Position cache not wired** — the `position_cache` table exists but `resolvePositions` does not read from it; positions are recomputed on every `GET /api/lick/{id}` call.
- **Pagination** — `GET /api/lick` and `GET /api/song` return all records with no paging.
- **Simultaneous notes in DFS** — `DfsPositionBuilder` iterates notes sequentially; chords are only handled correctly by `LoserBracketPositionBuilder` (`algo=chord`).
- **Upload instrument** — tab parsing always uses `Guitar.STANDARD`; the upload endpoint does not accept an instrument parameter.
