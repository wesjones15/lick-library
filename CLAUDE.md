# Lick Library — Backend

Spring Boot backend for a guitar lick library. Upload a tab to store its interval shape. Lookup by key to get all licks rendered in that key as positions on the neck.

---

## Stack

- Java 21, Spring Boot 3.3.0
- H2 (embedded, file-persisted)
- JUnit 5 + Mockito
- Standard tuning only (MVP)
- Major scale only (MVP)

---

## Project Structure

```
src/main/java/org/jones/licklibrary/
├── controller/
│   └── LickController.java              # REST endpoints
├── service/
│   └── LickService.java                 # all pipeline logic (parse, intervals, positions, DB)
├── model/
│   ├── TabNote.java                     # record: stringIndex, fret, columnIndex, technique
│   ├── IntervalNote.java                # record: interval, technique, columnIndex
│   ├── IntervalNoteListConverter.java   # JPA converter + toDisplayString()
│   ├── TabNoteListConverter.java        # JPA converter for List<TabNote>
│   ├── Lick.java                        # DB entity
│   ├── LickResponse.java                # API response record
│   ├── Position.java                    # a single playable position on the neck
│   └── PositionCache.java               # DB entity for position cache
├── repository/
│   ├── LickRepository.java              # JPA repo, lookup by interval hash
│   └── PositionCacheRepository.java
└── constants/
    ├── Note.java                        # enum: C C_SHARP D D_SHARP E F F_SHARP G G_SHARP A A_SHARP B
    ├── Interval.java                    # enum with displayName() and fromDisplayName()
    └── Guitar.java                      # open string notes in standard tuning
```

```
src/main/resources/
├── application.properties
└── data.sql                             # optional seed data
```

---

## Enums

**Note.java** — used only during parsing to resolve string+fret to an absolute note. Does not travel beyond the tab parsing step.
```
C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B
shift(int semitones) → (ordinal + semitones) % 12
```
Flats are aliased to their sharp equivalent on input.

**Interval.java** — used everywhere from parsing onward.
```
ONE, FLAT_TWO, TWO, FLAT_THREE, THREE, FOUR, FLAT_FIVE, FIVE, FLAT_SIX, SIX, FLAT_SEVEN, SEVEN
displayName() → "1", "b2", "2", "b3", "3", "4", "b5", "5", "b6", "6", "b7", "7"
fromDisplayName(String) → reverse lookup
shift(int semitones) → (ordinal + semitones) % 12
```

Conversion from Note to Interval: `(note.ordinal() - firstNote.ordinal() + 12) % 12`

**Guitar.java** — open strings low to high: `E, A, D, G, B, E`. `getNoteAt(stringIndex, fret)` = `STANDARD_TUNING[stringIndex].shift(fret)`.

---

## Models

**TabNote** — raw parsed position from ASCII tab. Short-lived: exists during tab parsing and position finding.
```
record TabNote(int stringIndex, int fret, int columnIndex, String technique)
toNote() → Guitar.getNoteAt(stringIndex, fret)
```

**IntervalNote** — primary lick representation. Pairs an interval with the technique used to exit toward the next note and a normalized column index for ordering/simultaneity.
```
record IntervalNote(Interval interval, String technique, int columnIndex)
toString() → displayName [+ " " + technique]  — display only, no columnIndex
```
- `technique` is the character *following* the fret in the tab (`h`, `p`, `/`, etc.) — describes how you leave a note toward the next. Null on most notes, always null on last.
- `columnIndex` is a normalized sequential integer (0, 1, 2…). Notes played simultaneously (same raw tab column) share the same columnIndex.

**IntervalNoteListConverter** — JPA `AttributeConverter<List<IntervalNote>, String>`.
- **DB storage format**: `displayName:columnIndex:technique` comma-separated. Technique is empty string when absent.
  ```
  1:0:,b3:1:h,4:2:,5:3:
  ```
- **Display format** (static `toDisplayString()`): interval displayNames in sequence with technique as a trailing space-separated token. ColumnIndex not shown.
  ```
  1 b3 h 4 5
  ```

**Lick** — DB entity.
- `intervalHash` — SHA-256 of interval names only (technique-agnostic), used as dedup key
- `intervals` — `List<IntervalNote>` via `IntervalNoteListConverter`; the canonical lick shape
- `sourceNotes` — `List<TabNote>` via `TabNoteListConverter`; original parsed notes including simultaneous
- `modeTag`, `endpointDegree` — optional metadata for future filtering

**Position** — `record Position(List<TabNote> notes)`. Represents one playable position on the neck. `toTabString()` renders a 6-string ASCII tab using a column-slot model:
- Slots defined by sorted unique `columnIndex` values across all notes
- Slot width = max fret digit count at that column across all strings
- Separator between slots: technique char (if the note on that string has one) or `-`
- Fixed 1-char leading and trailing padding inside each `|`

**LickResponse** — `record LickResponse(String intervalHash, List<IntervalNote> intervals, List<Position> positions)`.

---

## Upload Pipeline

```
POST /api/lick { tab }
    │
    ▼
parseTab (LickService)
    walk each of 6 string lines character by character
    record fret number and technique (following char) at each column index
    merge all 6 strings, sort by columnIndex
    output: List<TabNote> (all notes including simultaneous, ordered by column)
    │
    ▼
toIntervals (LickService)
    resolve each TabNote → Note via Guitar.getNoteAt()
    first note = ONE; all others: (note.ordinal() - first.ordinal() + 12) % 12 → Interval
    assign normalized columnIndex: raw column positions (2, 5, 9…) → sequential (0, 1, 2…)
    simultaneous notes (same raw column) receive same normalized columnIndex
    preserve ALL notes — no filtering of simultaneous
    output: List<IntervalNote>
    │
    ▼
LickService
    serialize intervals → String (via IntervalNoteListConverter)
    hash interval sequence (technique-agnostic — Interval names only) → SHA-256
    check DB by hash → if exists, return existing record
    if new → store Lick (intervals + sourceNotes) and return
```

No root note on upload. No position computation on upload. The DB is a key-agnostic store of interval shapes.

---

## Lookup Pipeline

```
GET /api/lick?key=A&mode=MAJOR&page=0
    │
    ▼
LickService
    fetch licks from DB (paginated)
    for each lick:
        check position cache by intervalHash + key
        if cached → return cached positions
        if not → call findPositions → cache → return
    │
    ▼
findPositions (LickService)
    convert IntervalNote sequence → absolute Notes for given key
    for each root candidate on the neck: greedily build a position
        findCandidates: for each next note, search nearby strings/frets
        technique constraint: if technique present, next note must be same string
        no technique: next note may be same string or ±1 adjacent string
        pick closest candidate by proximityScore = |fret delta| + |string delta|
    filter: max 4-fret span
    filter: no note above MAX_FRET (default 15, constant in LickService)
    rank by max-fret ascending (lowest on neck first)
    output: List<Position>
```

---

## Tab Parser Detail

Each string line is processed independently:

1. Strip string label prefix (first 2 chars: `E|`, `B|`, etc.)
2. Walk character by character; column index = character position - 2
3. Digit at position j → fret number, technique = following char if it matches `[hp/]`
4. Output: `TabNote(stringIndex, fret, columnIndex, technique)` per digit found

All 6 strings merged into one list, sorted by `columnIndex`. Notes sharing a column index are simultaneous.

**Known limitation**: two-digit frets (10+) are not yet handled — each digit is recorded separately.

---

## DB Schema (H2)

```sql
CREATE TABLE lick (
    id              UUID PRIMARY KEY,
    interval_hash   VARCHAR(64) UNIQUE NOT NULL,  -- SHA-256 of interval names (technique-agnostic)
    intervals       TEXT NOT NULL,                 -- e.g. "1:0:,b3:1:h,4:2:,5:3:"
    source_notes    TEXT,                          -- original TabNote list including simultaneous
    mode_tag        VARCHAR(32),
    endpoint_degree VARCHAR(16),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE position_cache (
    id              UUID PRIMARY KEY,
    interval_hash   VARCHAR(64) NOT NULL,
    key             VARCHAR(8) NOT NULL,
    positions_json  TEXT NOT NULL,
    UNIQUE (interval_hash, key)
);
```

---

## API

```
POST  /api/lick                          { tab }              → LickResponse
GET   /api/lick?key=A&mode=MAJOR&page=0                       → Page<LickResponse>
```

LickResponse contains the IntervalNote sequence and positions for the requested key. Filter params (mode, endpoint degree) should be stubbed even if unused in MVP.

---

## Future (not in MVP)

- **Pentatonic + minor scales** — position finder needs to know which scale degrees are valid
- **Modal filtering** — query licks by mode tag, filter by harmonic context
- **Solo builder** — chain licks by endpoint degree and mode compatibility
- **Lick similarity search** — find licks with same or related interval shape
- **Community library** — multi-user, shared DB (already key-agnostic so straightforward)
- **Non-standard tunings** — Guitar.java is isolated for this reason
- **Two-digit fret parsing** — parseTab needs lookahead to group consecutive digits
- **findPositions dedup for simultaneous notes** — buildPosition iterates absoluteNotes sequentially, which breaks for notes sharing a columnIndex; needs chord-aware handling
- **findCandidates optimization** — currently brute-forces all 6×25 positions; could compute fret mathematically
