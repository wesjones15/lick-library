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

---

## Session 4

### Decisions Made
- `IntervalNote` gains `columnIndex` as a third record component — a normalized sequential integer (0, 1, 2…) derived from raw tab column positions
- Simultaneous notes (multiple TabNotes at the same raw column) receive the same normalized columnIndex and are ALL preserved in the interval list (no first-per-column filtering)
- `IntervalNoteListConverter` now has two distinct formats:
  - **DB storage**: `displayName:columnIndex:technique` comma-separated (e.g. `1:0:,b3:1:h,4:2:,5:3:`) — roundtrippable, includes columnIndex
  - **Display**: `toDisplayString()` static method — interval names + technique as trailing token, no columnIndex (e.g. `1 b3 h 4 5`)
- `Lick.toString()` delegates to `IntervalNoteListConverter.toDisplayString(intervals)`
- `tabParserService`, `IntervalService`, `PositionFinderService` never got their own files — all logic lives in `LickService` until it warrants splitting

### Implemented
- `IntervalNote.java` — added `columnIndex` as third record component; `toString()` unchanged (display only)
- `IntervalNoteListConverter.java` — new storage format `displayName:columnIndex:technique`; new `toDisplayString()` static method
- `Lick.java` — `toString()` updated to call `IntervalNoteListConverter.toDisplayString(intervals)`
- `LickService.toIntervals()` — now assigns normalized columnIndex and preserves all simultaneous notes
- `IntervalNoteTest.java` — updated to 3-arg constructor
- `IntervalNoteListConverterTest.java` — updated constructors + storage format assertions; added `toDisplayString` tests and simultaneous-notes serialize test

---

## Session 5

### Decisions Made
- `Position.toString()` renamed to `toTabString()` and fully rewritten using a column-slot model
- Column-slot layout rules: 1 leading hyphen, 1 trailing hyphen, 1-char separator between slots (technique char or `-`), slot width = max fret digit count across all strings at that column
- `buildPosition` now stamps output `TabNote` column indices from `IntervalNote.columnIndex()` rather than loop index — simultaneous notes correctly share a slot in `toTabString()`
- `findPositions` sort changed from span-ascending to max-fret-ascending (positions sorted lowest on neck first)
- `MAX_FRET = 15` constant added to `LickService` — positions with any note above this fret are filtered out; easy to change

### Implemented
- `Position.java` — replaced `toString()` with `toTabString()` using column-slot rendering
- `PositionTest.java` — 5 tests covering: single note, two notes same string, technique pair, two-digit fret slot widening, gap created by note on another string
- `LickService.buildPosition()` — column index now sourced from `intervals.get(i).columnIndex()`
- `LickService.findPositions()` — sort changed to max-fret ascending; added `MAX_FRET` filter
- `LickServiceTest` — 4 `buildPosition` tests (root placement, technique string constraint, columnIndex propagation, greedy nearest-neighbour); 1 `findPositions` integration-style test with stdout output for manual verification

---

## Session 6

### Decisions Made
- `rawTab` added back to `Lick` entity and all responses — needed to show the original tab in the UI list and detail views
- `mode` field added to `Lick` — auto-detected from intervals using flat-interval elimination; user can override on upload
- `IntervalNote` serialization format finalized: `displayName:columnIndex:technique` comma-separated; technique is empty string (not absent) when null — ensures roundtrip consistency
- `LickResponse` now has two shapes: summary (id, rawTab, intervalDisplayString, no positions) and detail (+ mode, List<PositionResponse>)
- `PositionResponse(String tabString)` record wraps rendered tab strings — backend renders via `Position.toTabString()` so frontend never reimplements that logic
- Position cache exists in DB schema but is **skipped in MVP** — positions are always recomputed on demand; relevant tests annotated `@Disabled`
- `PositionCache.key` column renamed to `note_key` to avoid H2 reserved word conflict
- Backslash (`\`) added as a valid technique character alongside `h`, `p`, `/` — represents a slide down; regex updated from `[hp/]` to `[hp/\\]`
- Dedup is by `intervalHash` (SHA-256 of interval names, technique-agnostic) — same musical shape from different starting positions deduplicates correctly
- `detectMode` cannot produce DORIAN in the current elimination logic (b3+b7 tiebreaks to AEOLIAN since AEOLIAN ranks higher in the candidate list) — noted, tests reflect actual behavior, not fixed in MVP

### Implemented — Backend
- `LickService.java` — full upload and lookup pipelines: `parseTab`, `toIntervals`, `detectMode`, `hashIntervals`, `findPositions`, `buildPosition`, `findCandidates`, `resolvePositions`, `toLickResponse`; all prior `TODO` stubs replaced
- `LickController.java` — POST /api/lick, GET /api/lick, GET /api/lick/{id}?key=, DELETE /api/lick/{id}; error handling: 400 on bad key/blank tab, 404 via `LickNotFoundException`
- `LickNotFoundException.java` — `@ResponseStatus(NOT_FOUND)` exception used by both GET and DELETE
- `UploadLickRequest.java` — `record(String rawTab, String mode)` request body
- `PositionResponse.java` — `record(String tabString)` API wrapper
- `CorsConfig.java` — allows GET, POST, DELETE from `http://localhost:5173`
- `Mode.java` — `IONIAN … LOCRIAN` enum; `detectMode` logic in `LickUtils`
- `LickUtils.java` — stateless helpers extracted: `toIntervals`, `toAbsoluteNotes`, `proximityScore`, `toNoteString`, `hashIntervals`, `detectMode`
- `Lick.java` — added `rawTab`, `mode`, `endpointDegree` fields; removed `sourceNotes` (not needed)
- `LickResponse.java` — updated to carry both summary and detail shapes via nullable `mode` and `positions`
- `PositionCacheRepository.java` — `findByIntervalHashAndKey(String, String)` method

### Implemented — Backend Tests
- `LickControllerIntegrationTest.java` — 7 tests: upload, list, get-by-id, dedup, mode detection, 404 on unknown id, 400 on bad key; uses `@SpringBootTest` + `@AutoConfigureMockMvc` + in-memory H2
- `ParseTabTest.java` — technique characters (h, p, /), backslash technique, simultaneous notes, prefix stripping, ordering; two-digit fret test `@Disabled`
- `LickUtilsTest.java` — `toIntervals` (first note ONE, math, octave wrap), `hashIntervals` (determinism, technique-agnostic), `toAbsoluteNotes`, `detectMode`
- `FindCandidatesTest.java` — adjacent strings (no technique), same-string-only constraint (with technique), proximity sort
- `FindPositionsTest.java` — valid positions generated, 4-fret span filter, MAX_FRET filter, empty result
- `LickServiceTest.java` (cache tests) — `@Disabled`; document intended cache hit/miss behavior for future implementation
- `test/resources/application.properties` — in-memory H2 (`jdbc:h2:mem:testdb`), `create-drop` DDL for test isolation

### Implemented — Frontend (new repo: `lick_library_ui`)
- React 18 + TypeScript + Vite + Tailwind CSS v4 (`@tailwindcss/vite` plugin, `@import "tailwindcss"` in CSS)
- React Router v6; two routes: `/` (LibraryPage) and `/lick/:id` (DetailPage)
- `src/api/client.ts` — typed fetch wrappers: `getAllLicks`, `uploadLick`, `getLick`, `deleteLick`
- `LibraryPage.tsx` — upload form + lick list; upload and delete both trigger list refresh
- `DetailPage.tsx` — key selector (12 notes) + position tab blocks; re-fetches on key change
- `LickCard.tsx` — interval display string, mode chip, raw tab in monospace, red × delete button (stopPropagation to prevent navigation)
- `LickList.tsx` — maps over `LickSummary[]`, threads `onDelete` callback to each card
- `UploadForm.tsx` — textarea for raw tab, optional mode dropdown (auto-detect default), submit
- `KeySelector.tsx` — controlled dropdown for C through B
- `PositionTab.tsx` — `<pre>` block rendering a single `tabString` from backend
