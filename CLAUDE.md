# Lick Library — Backend

Spring Boot backend for a guitar lick library. Upload a tab to store its interval shape. Lookup by key to get all licks rendered in that key as positions on the neck.

---

## Stack

- Java, Spring Boot
- H2 (embedded, file-persisted)
- Standard tuning only (MVP)
- Major scale only (MVP)

---

## Project Structure

```
src/main/java/org/jones/licklibrary/
├── controller/
│   └── LickController.java         # REST endpoints
├── service/
│   ├── TabParserService.java        # raw tab → ordered note sequence
│   ├── IntervalService.java         # notes → IntervalNote sequence
│   ├── PositionFinderService.java   # interval sequence + key → alternate neck positions
│   └── LickService.java             # orchestrates pipelines, handles DB lookup
├── model/
│   ├── TabNote.java                 # record: stringIndex, fret, columnIndex, technique
│   ├── IntervalNote.java            # record: interval, technique — primary lick representation
│   ├── Lick.java                    # DB entity
│   └── Position.java                # a single playable position on the neck
├── repository/
│   └── LickRepository.java          # JPA repo, lookup by interval hash
└── constants/
    ├── Note.java                    # enum: C C_SHARP D D_SHARP E F F_SHARP G G_SHARP A A_SHARP B
    ├── Interval.java                # enum: ONE FLAT_TWO TWO FLAT_THREE THREE FOUR FLAT_FIVE FIVE FLAT_SIX SIX FLAT_SEVEN SEVEN
    └── Guitar.java                  # open string notes in standard tuning
```

```
src/main/resources/
├── application.properties
└── data.sql                         # optional seed data
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
shift(int semitones) → (ordinal + semitones) % 12
```

Conversion from Note to Interval: `(note.ordinal() - firstNote.ordinal() + 12) % 12`

**Guitar.java** — open strings low to high: `E, A, D, G, B, E`. `getNoteAt(stringIndex, fret)` = `STANDARD_TUNING[stringIndex].shift(fret)`.

---

## Models

**TabNote** — raw parsed position from ASCII tab. Short-lived: exists only during tab parsing.
```
record TabNote(int stringIndex, int fret, int columnIndex, String technique)
toNote() → Guitar.getNoteAt(stringIndex, fret)
```

**IntervalNote** — primary lick representation. Pairs an interval with the technique used to exit toward the next note (`h`, `p`, `/`, etc.). Technique is null for most notes.
```
record IntervalNote(Interval interval, String technique)
```

Serialization format for DB storage: comma-separated, technique appended with `:` only when present.
```
ONE,FLAT_THREE:h,FOUR,FIVE
```

---

## Upload Pipeline

```
POST /api/lick { tab }
    │
    ▼
parseTab (LickService)
    parse each of 6 string lines → Map<columnIndex, TabNote>
    merge all 6 into TreeMap<columnIndex, List<TabNote>>
    for simultaneous notes: take first (MVP)
    output: ordered List<TabNote>
    │
    ▼
toIntervals (LickService)
    resolve each TabNote → Note via Guitar.getNoteAt()
    first note = ONE, all others derived via (note - firstNote + 12) % 12
    pair each Interval with technique from TabNote
    output: List<IntervalNote>
    │
    ▼
LickService
    serialize intervals → String
    hash interval sequence (technique-agnostic hash — hash only Interval names)
    check DB by hash → if exists, return existing record
    if new → store and return
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
        if not → call PositionFinderService → cache → return
    │
    ▼
PositionFinderService
    convert Interval sequence → absolute Notes for given key
    for each note: find all valid string/fret locations on neck
    generate combinations (one location per note in sequence)
    filter: max 4-fret span, no impossible string jumps
    rank by span (tighter = better)
    output: List<Position>
```

---

## Tab Parser Detail

Each string line is processed independently:

1. Strip string label prefix (`E|`, `B|`, `A|` etc.)
2. Walk character by character, recording column index of each fret number
3. Group consecutive digits (handles frets 10, 11, 12 etc.)
4. Record technique character if present (`h`, `p`, `/`, `\`, `b`) — the character *following* the fret number, indicating how to transition to the next note
5. Output: `Map<Integer, TabNote>` for that string

Merge all 6 string maps into `TreeMap<Integer, List<TabNote>>` — TreeMap gives temporal order for free.

---

## DB Schema (H2)

```sql
CREATE TABLE lick (
    id              UUID PRIMARY KEY,
    interval_hash   VARCHAR(64) UNIQUE NOT NULL,  -- SHA-256 of interval names only (technique-agnostic)
    intervals       VARCHAR(255) NOT NULL,          -- e.g. "ONE,FLAT_THREE:h,FOUR,FIVE"
    mode_tag        VARCHAR(32),                    -- optional, user-supplied
    endpoint_degree VARCHAR(16),                    -- e.g. "FIVE", for solo chaining later
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE position_cache (
    id              UUID PRIMARY KEY,
    interval_hash   VARCHAR(64) NOT NULL,
    key             VARCHAR(8) NOT NULL,            -- e.g. "A"
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

LickResponse contains the IntervalNote sequence and positions for the requested key. Filter params (mode, endpoint degree etc.) should be stubbed from day one even if unused in MVP.

---

## Future (not in MVP)

- **Pentatonic + minor scales** — position finder needs to know which scale degrees are valid
- **Modal filtering** — query licks by mode tag, filter by harmonic context
- **Solo builder** — chain licks by endpoint degree and mode compatibility
- **Lick similarity search** — find licks with same or related interval shape
- **Community library** — multi-user, shared DB (already key-agnostic so straightforward)
- **Non-standard tunings** — Guitar.java is isolated for this reason
- **Simultaneous notes** — currently skipped, would need chord-aware interval logic
