# Lick Library — Backend

Spring Boot backend for a guitar lick library. Upload a tab to store its interval shape. Look up by key and instrument to get all licks rendered as playable positions on the neck.

---

## Stack

- Java 21, Spring Boot 3.3.0
- H2 (embedded, file-persisted at `./data/licklibrary`)
- JUnit 5 + Mockito
- Multi-instrument support via `Instrument` interface

---

## Project Structure

Domain-vertical layout (DDD). Each domain (`lick/`, `song/`, `chord/`) imports only from `domain/shared/` and `domain/position/` — no cross-domain imports.

```
src/main/java/org/jones/licklibrary/
├── core/
│   ├── config/
│   │   └── CorsConfig.java              # allows GET POST DELETE from localhost:5173
│   └── exception/
│       └── ResourceNotFoundException.java  # @ResponseStatus(NOT_FOUND), replaces domain-specific exceptions
│
├── domain/
│   ├── shared/                          # shared kernel — imported by all domains
│   │   ├── Note.java                    # enum: C C_SHARP D D_SHARP E F F_SHARP G G_SHARP A B_FLAT B
│   │   ├── Interval.java                # enum with displayName() and fromDisplayName()
│   │   ├── Mode.java                    # enum: IONIAN DORIAN PHRYGIAN LYDIAN MIXOLYDIAN AEOLIAN LOCRIAN
│   │   ├── TabNote.java                 # record: stringIndex, fret, columnIndex, technique
│   │   ├── IntervalNote.java            # record: interval, technique, columnIndex
│   │   ├── Position.java                # record: List<TabNote> + toTabString(Instrument)
│   │   ├── Instrument.java              # interface: tuning, labels, displayOrder, stringCount, getNoteAt, minFret
│   │   ├── InstrumentRegistry.java      # name string → Instrument instance
│   │   ├── NoteParser.java              # "C#" / "Bb" / "A#" → Note enum
│   │   └── instrument/
│   │       ├── Guitar.java              # STANDARD DROP_D OPEN_G OPEN_D DADGAD
│   │       ├── Bass.java                # STANDARD (E A D G, 4 strings)
│   │       ├── Ukulele.java             # STANDARD (G C E A, 4 strings)
│   │       ├── Mandolin.java            # STANDARD (G D A E, 4 strings)
│   │       ├── Banjo.java               # STANDARD (D G B D G, 5 strings; 5th-string minFret TODO)
│   │       └── CustomInstrument.java    # built from Note[] at request time
│   │
│   ├── position/                        # position-finding infrastructure
│   │   ├── PositionCache.java           # DB entity (reserved for future caching)
│   │   ├── PositionCacheRepository.java
│   │   ├── LickUtils.java               # stateless helpers: toIntervals, toAbsoluteNotes, proximityScore, hashIntervals, detectMode
│   │   └── builder/
│   │       ├── PositionBuilder.java     # abstract base: findNeckPositions, findCandidates, MAX_FRET, MAX_POSITIONS
│   │       ├── GreedyPositionBuilder.java   # single-pass nearest-neighbour
│   │       ├── DfsPositionBuilder.java      # depth-first search with diversity dedup + round-robin
│   │       └── LoserBracketPositionBuilder.java  # two-pass: melody first, chord partners second
│   │
│   ├── lick/
│   │   ├── Lick.java                    # DB entity
│   │   ├── LickController.java          # REST endpoints: POST/GET/DELETE /api/lick
│   │   ├── LickRepository.java          # JPA repo, lookup by interval hash
│   │   ├── LickService.java             # pipeline orchestration, DB interaction, tab parsing
│   │   ├── IntervalNoteListConverter.java  # JPA converter + toDisplayString()
│   │   └── dto/
│   │       ├── LickResponse.java        # API response record
│   │       ├── PositionResponse.java    # record: tabString
│   │       └── UploadLickRequest.java   # record: rawTab, mode?, inputKey?
│   │
│   ├── song/
│   │   ├── Song.java                    # DB entity
│   │   ├── SongController.java          # REST endpoints: POST/GET/DELETE /api/song
│   │   ├── SongRepository.java
│   │   ├── SongService.java
│   │   ├── dto/
│   │   │   ├── SongDetailResponse.java
│   │   │   ├── SongSummaryResponse.java
│   │   │   └── UploadSongRequest.java
│   │   └── parsing/
│   │       ├── ChordLyric.java          # record: chords, lyrics, fontSize
│   │       ├── ChordLyricListConverter.java  # JPA JSON converter
│   │       ├── ChordSheetParser.java    # parses raw chord sheet into ChordLyric pairs
│   │       └── ChordTransposer.java     # transposes chord tokens by semitone count
│   │
│   └── chord/
│       ├── ChordQuality.java            # DB entity: chord suffix
│       ├── ChordShape.java              # DB entity: CAGED template frets
│       ├── ChordQualityRepository.java
│       ├── ChordShapeRepository.java
│       ├── ChordService.java            # transpose shapes + format tab output
│       ├── ChordShapeSeed.java          # seeds CAGED shapes on first boot
│       └── ChordController.java         # GET /api/chord?root=A&quality=m7&instrument=GUITAR
```

---

## Enums & Constants

**Note.java** — 12-tone chromatic scale. Used during tab parsing and position finding. `B_FLAT` is the flat-9/flat-7 value (not `A_SHARP`).
```
C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, B_FLAT, B
shift(int semitones) → values()[(ordinal + semitones) % 12]
```

**Interval.java** — scale degrees; the primary lick representation from parsing onward.
```
ONE, FLAT_TWO, TWO, FLAT_THREE, THREE, FOUR, FLAT_FIVE, FIVE, FLAT_SIX, SIX, FLAT_SEVEN, SEVEN
displayName() → "1", "b2", "2", "b3", "3", "4", "b5", "5", "b6", "6", "b7", "7"
fromDisplayName(String) → reverse lookup
shift(int semitones) → values()[(ordinal + semitones) % 12]
```

Conversion from Note to Interval: `(note.ordinal() - rootKey.ordinal() + 12) % 12`

**Instrument interface** — pluggable string instrument. All position-building and tab-rendering code calls through this interface; no class outside `Guitar.java`/`LickService.uploadLick` hardcodes a specific instrument.
```java
Note[]   tuning()                          // low string first
String[] labels()                          // display labels, high string first
int[]    displayOrder()                    // maps display row → tuning array index
String   name()
default int  stringCount()                 // tuning().length
default Note getNoteAt(int string, int fret) // tuning()[string].shift(fret)
default int  minFret(int string)           // 0 for all except banjo 5th string (TODO)
```

**Guitar.java** — five presets, all implementing `Instrument`. `labels()` is dynamic — computed from the instance's `tuning` array so each variant shows its actual open-string notes (e.g. Drop D's low string shows `D|` not `E|`).
```
STANDARD  E A D G B E
DROP_D    D A D G B E
OPEN_G    D G D G B D
OPEN_D    D A D F# A D
DADGAD    D A D G A D
```

**InstrumentRegistry.fromName(String)** — maps `?instrument=` query param values to instances. Throws `IllegalArgumentException` for unknown names (controller returns 400).
```
"GUITAR" → Guitar.STANDARD     "DROP_D" → Guitar.DROP_D
"OPEN_G" → Guitar.OPEN_G       "OPEN_D" → Guitar.OPEN_D
"DADGAD" → Guitar.DADGAD       "BASS"   → Bass.STANDARD
"UKULELE" → Ukulele.STANDARD   "MANDOLIN" → Mandolin.STANDARD
"BANJO"  → Banjo.STANDARD
```

**NoteParser.parse(String)** — maps user-typed note names to `Note` enum. Case-insensitive; accepts `C#`, `Db`, `Bb`, `A#`, and Unicode `♭`/`♯`. Both `A#` and `Bb` map to `B_FLAT`.

**CustomInstrument** — built at request time from a `Note[]` parsed by `NoteParser`. Labels and display order are derived from the tuning array (high string first, highest string label lowercase). Used when `?tuning=E A D G B E` is present on the request.

**Mode.java** — auto-detected from intervals; user-overridable on upload.
```
IONIAN, DORIAN, PHRYGIAN, LYDIAN, MIXOLYDIAN, AEOLIAN, LOCRIAN
```
Detection: flat intervals eliminate incompatible modes; tiebreak by commonality (Ionian > Aeolian > Dorian > Mixolydian > Phrygian > Lydian > Locrian). Default IONIAN if all candidates eliminated.

| Interval present | Eliminates |
|---|---|
| FLAT_TWO   | Ionian, Dorian, Lydian, Mixolydian, Aeolian |
| FLAT_THREE | Ionian, Lydian, Mixolydian |
| FLAT_FIVE  | Ionian, Dorian, Phrygian, Mixolydian, Aeolian |
| FLAT_SIX   | Ionian, Dorian, Lydian, Mixolydian |
| FLAT_SEVEN | Ionian, Lydian |

---

## Models

**TabNote** — raw parsed note from ASCII tab. Exists only during parsing and position finding; never stored in DB.
```
record TabNote(int stringIndex, Integer fret, int columnIndex, String technique)
```
- `stringIndex`: 0 = lowest string
- `fret`: fret number (supports 0–99; two-digit frets parsed correctly)
- `columnIndex`: character position in tab string (raw, before normalisation)
- `technique`: `"h"`, `"p"`, `"/"`, `"\"`, or `""` (empty, not null, when absent)

**IntervalNote** — primary lick representation. Stored in DB via `IntervalNoteListConverter`.
```
record IntervalNote(Interval interval, String technique, int columnIndex)
toString() → displayName [+ " " + technique]  — display only, no columnIndex
```
- `technique` describes how you *exit* toward the next note (character *following* the fret in the tab). Always empty on the last note.
- `columnIndex` is a *normalised* sequential integer (0, 1, 2…). Notes played simultaneously share the same `columnIndex`.

**IntervalNoteListConverter** — JPA `AttributeConverter<List<IntervalNote>, String>`.
- **DB storage format**: `displayName:columnIndex:technique` comma-separated. Technique is empty string (not absent) when not present.
  ```
  1:0:,b3:1:h,4:2:,5:3:
  ```
- **Display format** (`static toDisplayString()`): interval names with technique as trailing token. `columnIndex` not shown.
  ```
  1 b3 h 4 5
  ```

**Lick** — DB entity.
- `intervalHash` — SHA-256 of interval `displayName()` values only (technique-agnostic), dedup key
- `intervals` — `List<IntervalNote>` via `IntervalNoteListConverter`
- `rawTab` — original ASCII tab as uploaded; displayed in list and detail views unchanged
- `mode` — `Mode` enum, auto-detected or user-supplied
- `tabSpan` — `max_fret − min_fret` of the original tab; stored at upload time and used as the span limit for position finding (minimum 4)
- `endpointDegree` — reserved for future solo chaining

**Position** — `record Position(List<TabNote> notes)`. `toTabString(Instrument)` renders an N-string ASCII tab:
- String count, labels, and display order come from the `Instrument` parameter
- Columns defined by sorted unique `columnIndex` values
- Slot width = max fret digit count at that column across all strings
- Separator between slots: technique char (if present) or `-`
- Fixed 1-char leading and trailing padding inside each `|`
- `toTabString()` (no-arg) delegates to `toTabString(Guitar.STANDARD)`

**LickResponse** — two shapes depending on endpoint:
- List view: `id`, `rawTab`, `intervalDisplayString`, `mode`, `positions=null`
- Detail view: above + `positions` as `List<PositionResponse>`

---

## Upload Pipeline

```
POST /api/lick { rawTab, mode?, inputKey? }
    │
    ▼
parseTab (LickService)
    split on \n → 6 string lines
    skip first 2 chars per line (string label + pipe)
    walk char by char:
      digit → fret (two-digit lookahead: if j and j+1 are both digits, combine them)
      technique = next char if it matches [hp/\]
    merge all lines, sort by columnIndex
    output: List<TabNote>
    │
    ▼
toIntervals (LickUtils)
    resolve each TabNote → Note via instrument.getNoteAt(stringIndex, fret)
    upload always uses Guitar.STANDARD
    rootKey = request.inputKey() if provided, else first note's absolute pitch
    first note relative to rootKey = ONE; others: (note.ordinal() - rootKey.ordinal() + 12) % 12 → Interval
    assign normalised columnIndex (increments when raw columnIndex changes)
    output: List<IntervalNote>  — all notes preserved including simultaneous
    │
    ▼
LickService
    hash interval displayNames → SHA-256 (technique-agnostic dedup key)
    check DB by hash → if exists, return existing Lick
    detect mode (LickUtils.detectMode) or use request.mode() override
    compute tabSpan = max_fret − min_fret across all TabNotes
    persist Lick; return summary LickResponse
```

Deduplication is by `intervalHash` — same musical shape with different articulation, or uploaded from a different register, deduplicates correctly. Different `inputKey` values for the same notes produce different interval relationships and store as separate licks.

---

## Lookup Pipeline

```
GET /api/lick/{id}?key=A&algo=greedy&instrument=GUITAR
    │
    ▼
LickController
    resolve key: Note.valueOf(key.toUpperCase())
    resolve instrument:
      if ?tuning= present → NoteParser.parse each token → new CustomInstrument(notes)
      else → InstrumentRegistry.fromName(instrument)
    invalid key or instrument name → 400
    │
    ▼
LickService.getLick(id, key, algo, instrument)
    fetch Lick by id → 404 if not found
    resolvePositions(lick, key, algo, instrument)
    │
    ▼
resolvePositions
    spanLimit = Math.max(4, lick.getTabSpan())
    select builder: "dfs" → DfsPositionBuilder, "chord" → LoserBracketPositionBuilder, default → GreedyPositionBuilder
    builder.build(lick.getIntervals(), key, spanLimit, instrument)
    │
    ▼
LickService.toLickResponse(lick, positions, instrument)
    for each Position: p.toTabString(instrument)
    return full LickResponse with rendered tabs
```

Positions are **recomputed on every request** — the `position_cache` table exists in the DB schema but is not currently used.

---

## Position Builders

All three builders extend `PositionBuilder` and receive the same inputs: `(List<IntervalNote> intervals, Note key, int spanLimit, Instrument instrument)`.

**Shared base methods** (`PositionBuilder`):
- `findNeckPositions(Note, Instrument)` — finds all fret/string locations for a note within `MAX_FRET` (15)
- `findCandidates(TabNote current, Note next, String technique, Instrument)` — finds candidates for the next note within ±2 strings of `current` (same string only when technique is present), sorted by Euclidean proximity `Math.hypot(Δfret, Δstring)`

**GreedyPositionBuilder** — one path per root candidate. At each step picks the single closest candidate; discards the whole path if any step fails span or fret constraints. Fast; produces ≤1 position per root.

**DfsPositionBuilder** — explores up to `Math.max(4, 20/noteCount)` candidates at each note (per-step cap scales down for longer licks). Deduplicates by `(string-index sequence, minFret / 5)` — keeps only the lowest-register representative of each shape. Results sorted by max-fret ascending then interleaved round-robin by starting string for variety.

**LoserBracketPositionBuilder** — two-pass chord-aware greedy.
- Pass 1: place one note per unique `columnIndex` greedily (melodic line).
- Pass 2: for each note whose `columnIndex` was already placed (chord partner), find the best candidate on a *different* string near its parallel note; skip if span would be exceeded (partial positions returned rather than discarding).
- Only algorithm that correctly handles simultaneous notes (shared `columnIndex`).

All builders: filter out any position with a note above `MAX_FRET = 15`, enforce `spanLimit`, cap total results at `MAX_POSITIONS = 50`.

---

## Tab Parser Detail

1. Split raw tab on `\n` — expect 6 lines (one per string, low E = line 0)
2. Skip first 2 characters per line (string label + `|`)
3. Walk character by character:
   - Digit at position `j`:
     - If `j+1` is also a digit → combine into two-digit fret, advance `j` past `j+1`
     - Else → single-digit fret
   - Technique = char at `j+1` (or `j+2` for two-digit fret) if it matches `[hp/\]`; else `""`
4. Emit `TabNote(stringIndex=lineIndex, fret, columnIndex=j-2, technique)`

All 6 strings merged and sorted by `columnIndex`. Notes sharing a column index are simultaneous.

---

## DB Schema (H2)

```sql
CREATE TABLE lick (
    id              UUID PRIMARY KEY,
    interval_hash   VARCHAR(64) UNIQUE NOT NULL,
    intervals       TEXT NOT NULL,           -- "1:0:,b3:1:h,4:2:,5:3:"
    raw_tab         TEXT,
    mode            VARCHAR(16),
    tab_span        INTEGER,                 -- max_fret - min_fret of original tab
    endpoint_degree VARCHAR(16),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE position_cache (
    id              UUID PRIMARY KEY,
    interval_hash   VARCHAR(64) NOT NULL,
    note_key        VARCHAR(8)  NOT NULL,    -- named note_key to avoid H2 reserved word
    positions_json  TEXT NOT NULL,
    UNIQUE (interval_hash, note_key)
);
```

---

## API

```
POST /api/lick
    body: { rawTab, mode?, inputKey? }
    → LickResponse (list shape)

GET  /api/lick
    → List<LickResponse> (list shape — no positions)

GET  /api/lick/{id}
    ?key=A              (required) root note: A, C_SHARP, B_FLAT, etc.
    ?algo=greedy        (default)  greedy | dfs | chord
    ?instrument=GUITAR  (default)  see InstrumentRegistry for valid names
    ?tuning=E A D G B E (optional) overrides instrument; space-separated notes via NoteParser
    → LickResponse (detail shape — includes mode + List<PositionResponse>)

DELETE /api/lick/{id}
    → 204 No Content
```

---

## Future

- **Position cache** — table and repository exist; `resolvePositions` doesn't use them yet
- **Banjo 5th-string `minFret`** — string 4 starts at fret 5, not fret 0; `minFret()` override not yet implemented
- **Pagination** — `GET /api/lick` returns all licks with no paging
- **Simultaneous notes in DFS** — `DfsPositionBuilder` iterates notes sequentially; chords only handled correctly by `LoserBracketPositionBuilder`
- **Upload instrument** — `parseTab` + `toIntervals` always use `Guitar.STANDARD` on upload; could accept an instrument param
- **Solo builder** — chain licks by `endpointDegree` and mode compatibility
- **Lick similarity search** — find licks sharing interval shape or mode
- **CAGED scale shapes** — generate full diatonic/pentatonic scale positions per mode; reuses `findNeckPositions` + `findCandidates`
- **Multi-bar phrase segmentation** — break long phrases at `|` boundaries to improve position quality on multi-bar licks
