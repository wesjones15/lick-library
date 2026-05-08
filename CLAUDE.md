# Lick Library — Backend

Spring Boot backend for a guitar lick library. Upload a tab to store its interval shape. Lookup by key to get all licks rendered in that key as positions on the neck.

---

## Stack

- Java 21, Spring Boot 3.3.0
- H2 (embedded, file-persisted)
- JUnit 5 + Mockito
- Standard tuning only (MVP)

---

## Project Structure

```
src/main/java/org/jones/licklibrary/
├── controller/
│   └── LickController.java              # REST endpoints
├── service/
│   ├── LickService.java                 # pipeline orchestration, DB interaction
│   └── LickUtils.java                   # stateless helpers: toIntervals, toAbsoluteNotes, proximityScore, toNoteString, hashIntervals
├── model/
│   ├── TabNote.java                     # record: stringIndex, fret, columnIndex, technique
│   ├── IntervalNote.java                # record: interval, technique, columnIndex
│   ├── IntervalNoteListConverter.java   # JPA converter + toDisplayString()
│   ├── Lick.java                        # DB entity
│   ├── LickResponse.java                # API response record
│   ├── Mode.java                        # enum: IONIAN DORIAN PHRYGIAN LYDIAN MIXOLYDIAN AEOLIAN LOCRIAN
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

---

## Enums

**Note.java** — used only during parsing to resolve string+fret to an absolute note. Does not travel beyond the tab parsing step.
```
C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B
shift(int semitones) → (ordinal + semitones) % 12
```

**Interval.java** — used everywhere from parsing onward.
```
ONE, FLAT_TWO, TWO, FLAT_THREE, THREE, FOUR, FLAT_FIVE, FIVE, FLAT_SIX, SIX, FLAT_SEVEN, SEVEN
displayName() → "1", "b2", "2", "b3", "3", "4", "b5", "5", "b6", "6", "b7", "7"
fromDisplayName(String) → reverse lookup
shift(int semitones) → (ordinal + semitones) % 12
```

Conversion from Note to Interval: `(note.ordinal() - firstNote.ordinal() + 12) % 12`

**Guitar.java** — open strings low to high: `E, A, D, G, B, E`. `getNoteAt(stringIndex, fret)` = `STANDARD_TUNING[stringIndex].shift(fret)`.

**Mode.java** — auto-detected from intervals present; can be overridden by user on upload.
```
IONIAN, DORIAN, PHRYGIAN, LYDIAN, MIXOLYDIAN, AEOLIAN, LOCRIAN
```
Detection: flat intervals eliminate incompatible modes; default to most common remaining candidate when ambiguous.

| Interval present | Eliminates |
|---|---|
| FLAT_TWO   | Ionian, Dorian, Lydian, Mixolydian, Aeolian |
| FLAT_THREE | Ionian, Lydian, Mixolydian |
| FLAT_FIVE  | Ionian, Dorian, Phrygian, Mixolydian, Aeolian |
| FLAT_SIX   | Ionian, Dorian, Lydian, Mixolydian |
| FLAT_SEVEN | Ionian, Lydian |

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
- `columnIndex` is a normalized sequential integer (0, 1, 2…). Notes played simultaneously share the same columnIndex.

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
- `intervalHash` — SHA-256 of interval names only (technique-agnostic), dedup key
- `intervals` — `List<IntervalNote>` via `IntervalNoteListConverter`; the canonical lick shape
- `rawTab` — original ASCII tab string as uploaded; used for display in list view
- `mode` — `Mode` enum, auto-detected from intervals; overridable on upload
- `endpointDegree` — optional, for future solo chaining

**Position** — `record Position(List<TabNote> notes)`. `toTabString()` renders a 6-string ASCII tab using a column-slot model:
- Slots defined by sorted unique `columnIndex` values across all notes
- Slot width = max fret digit count at that column across all strings
- Separator between slots: technique char (if that string's note has one) or `-`
- Fixed 1-char leading and trailing padding inside each `|`

**LickResponse** — two shapes depending on endpoint:
- List view: `id`, `rawTab`, `intervalDisplayString`
- Single view: above + `mode`, `List<Position>` rendered for the requested key

---

## Upload Pipeline

```
POST /api/lick { rawTab, mode? }
    │
    ▼
parseTab (LickService)
    walk each of 6 string lines character by character
    record fret number and technique (following char) at each column index
    merge all 6 strings, sort by columnIndex
    output: List<TabNote>
    │
    ▼
toIntervals (LickUtils)
    resolve each TabNote → Note via Guitar.getNoteAt()
    first note = ONE; all others: (note.ordinal() - first.ordinal() + 12) % 12 → Interval
    assign normalized columnIndex; preserve ALL notes including simultaneous
    output: List<IntervalNote>
    │
    ▼
LickService
    detect mode from intervals (LickUtils.detectMode) — or use override if provided
    hash interval names → SHA-256 (technique-agnostic)
    check DB by hash → if exists, return existing record
    if new → store Lick (intervals, rawTab, mode) and return
```

Deduplication is by `intervalHash`, not `rawTab` — same musical shape from different starting positions deduplicates correctly.

---

## Lookup Pipeline

```
GET /api/lick
    → return all Lick records: id, rawTab, intervalDisplayString (no positions)

GET /api/lick/{id}?key=A
    │
    ▼
LickService
    fetch Lick by id
    check position cache by (intervalHash, key)
    if cached → return cached positions
    if not → call findPositions → cache → return
    │
    ▼
findPositions (LickService)
    toAbsoluteNotes: convert IntervalNote sequence → absolute Notes for given key
    for each root candidate on the neck: greedily build a position (buildPosition)
        findCandidates: search nearby strings/frets for next note
        technique constraint: if technique present, next note must be same string
        no technique: same string or ±1 adjacent string
        pick closest by proximityScore = |fret delta| + |string delta|
    filter: max 4-fret span
    filter: no note above MAX_FRET (default 15, constant in LickService)
    sort: max-fret ascending (lowest on neck first)
    output: List<Position>
```

---

## Tab Parser Detail

1. Strip string label prefix (first 2 chars: `E|`, `B|`, etc.)
2. Walk character by character; column index = character position - 2
3. Digit at position j → fret number, technique = following char if it matches `[hp/]`
4. Output: `TabNote(stringIndex, fret, columnIndex, technique)` per digit found

All 6 strings merged into one list, sorted by `columnIndex`. Notes sharing a column index are simultaneous.

**Known limitation**: two-digit frets (10+) not yet handled — each digit recorded separately.

---

## DB Schema (H2)

```sql
CREATE TABLE lick (
    id              UUID PRIMARY KEY,
    interval_hash   VARCHAR(64) UNIQUE NOT NULL,  -- SHA-256 of interval names (technique-agnostic)
    intervals       TEXT NOT NULL,                 -- e.g. "1:0:,b3:1:h,4:2:,5:3:"
    raw_tab         TEXT,                          -- original ASCII tab as uploaded
    mode            VARCHAR(16),                   -- Mode enum name, auto-detected or user-supplied
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
POST /api/lick          { rawTab, mode? }     → LickResponse (list shape)
GET  /api/lick                                → List<LickResponse> (list shape: id, rawTab, intervalDisplayString)
GET  /api/lick/{id}?key=A                     → LickResponse (single shape: + mode, List<Position>)
```

---

## Future (not in MVP)

- **Solo builder** — chain licks by endpoint degree and mode compatibility
- **Lick similarity search** — find licks with same or related interval shape
- **Modal filtering** — query licks by mode tag
- **Community library** — multi-user, shared DB (already key-agnostic so straightforward)
- **Non-standard tunings** — Guitar.java is isolated for this reason
- **Two-digit fret parsing** — parseTab needs lookahead to group consecutive digits
- **findPositions dedup for simultaneous notes** — buildPosition iterates absoluteNotes sequentially, which breaks for notes sharing a columnIndex; needs chord-aware handling
- **findCandidates optimization** — currently brute-forces all 6×25 positions; could compute fret mathematically
- **Pagination on GET /api/lick** — add page param once library grows
