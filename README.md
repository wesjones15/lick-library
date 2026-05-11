# Lick Library — Backend

A Spring Boot service for storing and exploring guitar licks. Upload a tab once; retrieve playable positions in any key on any instrument.

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
Loser-bracket two-pass algorithm. First pass places the melodic line greedily. Second pass fits simultaneously-played notes (same column in the tab) near their companion notes on adjacent strings. The only algorithm that correctly handles chords; partial positions are returned when a chord partner can't fit the span.

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

```
src/main/java/org/jones/licklibrary/
├── controller/
│   ├── LickController.java          REST endpoints
│   └── LickNotFoundException.java
├── service/
│   ├── LickService.java             Upload + lookup orchestration, tab parsing
│   ├── LickUtils.java               toIntervals, toAbsoluteNotes, hashIntervals, detectMode
│   ├── PositionBuilder.java         Abstract base: findNeckPositions, findCandidates
│   ├── GreedyPositionBuilder.java
│   ├── DfsPositionBuilder.java
│   └── LoserBracketPositionBuilder.java
├── model/
│   ├── Lick.java                    JPA entity
│   ├── TabNote.java                 record — raw parsed note
│   ├── IntervalNote.java            record — interval + technique + columnIndex
│   ├── IntervalNoteListConverter.java  JPA converter for List<IntervalNote>
│   ├── Position.java                record — List<TabNote> + toTabString()
│   ├── LickResponse.java            API response (summary and detail shapes)
│   ├── UploadLickRequest.java
│   ├── PositionResponse.java
│   ├── PositionCache.java           JPA entity (reserved for caching)
│   └── Mode.java                    enum
├── constants/
│   ├── Note.java                    enum — 12-tone chromatic scale
│   ├── Interval.java                enum — scale degrees with display names
│   ├── Instrument.java              interface
│   ├── Guitar.java                  STANDARD, DROP_D, OPEN_G, OPEN_D, DADGAD
│   ├── Bass.java
│   ├── Ukulele.java
│   ├── Mandolin.java
│   ├── Banjo.java
│   ├── CustomInstrument.java        built from tuning string at request time
│   ├── InstrumentRegistry.java      name → Instrument lookup
│   └── NoteParser.java              "C#" / "Bb" → Note enum
├── repository/
│   ├── LickRepository.java
│   └── PositionCacheRepository.java
└── config/
    └── CorsConfig.java              allows localhost:5173
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

### `position_cache` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `interval_hash` | VARCHAR(64) | |
| `note_key` | VARCHAR(8) | |
| `positions_json` | TEXT | |

UNIQUE on `(interval_hash, note_key)`. Table exists but is not actively used — positions are recomputed on every request.

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
- **Pagination** — `GET /api/lick` returns all licks with no paging.
- **Simultaneous notes in DFS** — `DfsPositionBuilder` iterates notes sequentially; chords are only handled correctly by `LoserBracketPositionBuilder` (`algo=chord`).
