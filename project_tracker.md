# Lick Library — Project Tracker

## Decisions Made

### Stack & Config
- Java 21, Spring Boot 3.3.0, H2 (file-persisted), JUnit 5 + Mockito
- Package: `org.jones.licklibrary`
- Entry point: `LickLibraryApplication.java`
- H2 console enabled at `/h2-console` for development

### Architecture
- Two separate pipelines: **upload** (store interval shape) and **lookup** (render positions in a key)
- Upload takes only `{ tab }` — no root note, no position computation. The store is key-agnostic.
- Lookup (`GET /api/lick?key=A&mode=MAJOR&page=0`) renders positions on demand, cached per `(intervalHash, key)`
- Positions live in a separate `position_cache` table, not on the `lick` row
- Intervals represented as `Interval` enum (not plain strings) from `IntervalService` onward
- `Note` enum used only during parsing to resolve string+fret → absolute note; does not travel beyond `IntervalService`

### Service Method Signatures
**Upload pipeline** (`void uploadLick(String tab)`)
- `parseTab(String rawTab) → List<TabNote>` — TabParserService
- `toIntervals(List<TabNote> notes) → List<Interval>` — IntervalService
- `hashIntervals(List<Interval> intervals) → String` — LickService (private)

**Lookup pipeline** (`Page<LickResponse> getLicks(String key, String mode, int page)`)
- `resolvePositions(Lick lick, Note key) → List<Position>` — LickService (private), checks cache first
- `findPositions(List<Interval> intervals, Note key) → List<Position>` — PositionFinderService
- `toLickResponse(Lick lick, List<Position> positions) → LickResponse` — LickService (private)

---

## What's Been Scaffolded

### Source files (all methods throw `UnsupportedOperationException("TODO")`)
- `constants/` — `Note.java`, `Interval.java`, `Guitar.java` (enums complete, logic ready to fill)
- `model/` — `TabNote.java`, `Position.java`, `Lick.java`, `PositionCache.java`, `LickResponse.java`
- `repository/` — `LickRepository`, `PositionCacheRepository` (JPA interfaces, query methods defined)
- `service/` — `TabParserService`, `IntervalService`, `PositionFinderService`, `LickService`
- `controller/` — `LickController` (POST + GET endpoints wired)
- `resources/application.properties` — H2 file-persisted, DDL auto, H2 console on

### Test files (stubs with descriptive method names, no assertions yet)
- `LickServiceTest` — upload dedup, cache hit/miss on lookup, resolvePositions
- `TabParserServiceTest` — ordering, two-digit frets, techniques, simultaneous notes, prefix stripping
- `IntervalServiceTest` — first note is ONE, interval math, octave wrap
- `PositionFinderServiceTest` — valid positions, 4-fret span filter, ranking, empty result

---

## Still To Do
- Implement all `TODO` methods (user will fill business logic)
- Update CLAUDE.md `Future` section as features are implemented
- Decide on request body shape for `POST /api/lick` (currently raw String — may want a wrapper object)

---

## Session 2

### Decisions Made
- `source_tab` dropped from `Lick` entity and `LickResponse` — `List<IntervalNote>` is the canonical lick representation
- New `IntervalNote` record: `(Interval interval, String technique)` — pairs each interval with the technique used to exit toward the next note (h, p, /, etc.); null on most notes and always null on the last
- Interval hash is technique-agnostic (hashes interval names only) so same shape with different articulation deduplicates correctly
- Serialization format for DB `intervals` column: `ONE,FLAT_THREE:h,FOUR,FIVE` — technique appended with `:` only when present
- Technique character is the *following* character in the tab (describes how you leave a note, not how you arrived)

### Implemented
- `IntervalNote.java` — new record
- `Lick.java` — removed `sourceTab`, added `toString()` rendering e.g. `ONE FLAT_THREE[h] FOUR FIVE`
- `LickResponse.java` — removed `sourceTab`, changed `intervals` to `List<IntervalNote>`
- `LickService.toIntervals()` — implemented: resolves each TabNote → Note → Interval, pairs with technique
- `LickService.serializeIntervals()` / `deserializeIntervals()` — implemented
- `CLAUDE.md` — updated to reflect all of the above

---

## Session 3

### Decisions Made
- `IntervalNote` serialization format changed to numeric degree names: `1 3 2 / 3 4` (space-separated, technique follows its interval as a separate token)
- `Interval` enum is now the source of truth for display names via `displayName()` (ONE→"1", FLAT_THREE→"b3") and `fromDisplayName()` for reverse lookup
- `IntervalNote.toString()` renders itself using `displayName()` — e.g. `"3 h"` or `"1"`
- `IntervalNoteListConverter` is the single owner of serialize/deserialize logic; `LickService.serializeIntervals` / `deserializeIntervals` removed as redundant
- `Lick.intervals` is now `List<IntervalNote>` (not String) — JPA converter handles persistence transparently
- `Position` redesigned from `List<int[]>` to `List<TabNote>` — preserves technique and supports simultaneous notes (multiple TabNotes sharing a columnIndex)
- `Position.toString()` renders a full 6-string ASCII tab from the TabNote list
- `Lick` gains `List<TabNote> sourceNotes` — the original parsed notes (including simultaneous) stored via `TabNoteListConverter`
- `LickService.parseTab` preserves all notes including simultaneous (no filtering); `toIntervals` takes the first note per column for the flat interval sequence

### Implemented
- `Interval.java` — added `displayName()` and `fromDisplayName()`
- `IntervalNote.java` — added `toString()`
- `IntervalNoteListConverter.java` — new JPA converter using enum display names
- `IntervalNoteListConverter` tests, `IntervalNoteTest`, `IntervalTest` (including `shift` tests)
- `LickService.java` — removed `INTERVAL_NAMES`, `serializeIntervals`, `deserializeIntervals`; `toIntervals` now groups by columnIndex
- `Position.java` — redesigned to `List<TabNote>` with ASCII tab renderer
- `TabNoteListConverter.java` — new JPA converter (`stringIndex:fret:columnIndex:technique` format)
- `Lick.java` — `intervals` now `List<IntervalNote>` with `@Convert`; added `sourceNotes` field
