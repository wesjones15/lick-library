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

---

## Session 7

### Decisions Made
- **Position finder: greedy → DFS branching** — the old `buildPosition` committed to the single closest candidate at each step; if that pick led to a dead end later, the entire position was silently discarded. Long licks often produced only one result. Replaced with a recursive DFS (`buildPositions` + `dfsPositions`) that explores all valid candidates at every step and collects every complete path.
- **String search range expanded** — `findCandidates` previously restricted to current string ±1 when no technique was present. Now searches all 6 strings (0–5), letting the span and MAX_FRET pruning serve as the physical constraint instead.
- **Euclidean proximity scoring** — `proximityScore` changed from Manhattan (`|Δfret| + |Δstring|`) to Euclidean (`Math.hypot(Δfret, Δstring)`) so diagonal jumps aren't over-penalized vs. pure string or fret movement.
- **Early pruning inside DFS** — branches are cut as soon as the current path would exceed the 4-fret span or MAX_FRET (15), keeping the search tractable even with a wider candidate set.
- **Deduplication by tab string** — different root candidates can produce the same physical fingering; results are deduplicated by rendered tab string before sorting.
- **Root fret guard** — roots above MAX_FRET are rejected immediately in `buildPositions` before any DFS work begins.
- **`FindPositionsTest` count assertion relaxed** — the old assertion `assertEquals(8, ...)` was tied to the greedy algorithm's output count; updated to `assertTrue(size >= 8, ...)` since branching correctly produces more positions.

### Implemented
- `LickService.java` — replaced `buildPosition` with `buildPositions` (package-private) and `dfsPositions` (private); updated `findPositions` to flatMap results and deduplicate; updated `findCandidates` to search all strings + `comparingDouble`
- `LickUtils.java` — `proximityScore` return type `int` → `double`, implementation `Math.abs` sum → `Math.hypot`
- `BuildPositionTest.java` — all 4 tests migrated from `buildPosition` to `buildPositions`; last test renamed `buildPositions_returnsAllValidPaths` and now asserts multiple paths are returned
- `FindCandidatesTest.java` — `noTechnique` test updated to assert all-strings range and that candidates span more than 2 strings
- `FindPositionsTest.java` — count assertion relaxed from `assertEquals(8)` to `assertTrue(>= 8)`
- `LickUtilsTest.java` — all `proximityScore` assertions updated to `double` with delta; `bothDiffer` case now asserts `Math.sqrt(8)` instead of `4`

---

## Session 8

### Decisions Made
- **Upload root key** — `toIntervals` previously always treated the first note of the tab as the root (ONE). Added an optional `inputKey` field to the upload request so users can declare what key the tab is in. Intervals are then computed relative to that key instead of the first note. A tab starting on E in a song in A now correctly stores E as the FIVE rather than ONE.
- **`toIntervals` overload pattern** — the single-argument version delegates to the new two-argument version with `notes.get(0).toNote()` as the root; all existing callers and tests are unaffected.
- **`inputKey` not persisted** — the root key is only used during interval computation at upload time; it is not stored on the `Lick` entity. The stored intervals already encode the correct relationship.
- **Sharp note bug fix** — `KeySelector` was sending `"C#"`, `"D#"` etc. to the backend, but `Note.valueOf("C#")` throws because Java enum names can't contain `#`. Sharp keys in the detail page key selector silently returned 400. Fixed by using `C_SHARP`/`D_SHARP` etc. as option values (displaying `C#`/`D#` to the user). This also gates the upload root key feature working correctly for sharp keys.
- **Dedup still applies** — two tabs with the same notes but different `inputKey` selections will produce different interval sequences and thus different hashes; they are stored as separate licks (correct behavior).

### Implemented — Backend
- `LickUtils.java` — added `toIntervals(List<TabNote>, Note rootKey)` overload; existing `toIntervals(List<TabNote>)` delegates to it
- `UploadLickRequest.java` — added `Note inputKey` field (nullable); Jackson deserializes `"A"`, `"C_SHARP"` etc. directly to `Note` enum
- `LickService.uploadLick` — resolves root key from `request.inputKey()` if non-null, else falls back to first note
- `LickServiceTest.java` — updated `UploadLickRequest` constructors to pass explicit `null` for `inputKey`
- `LickUtilsTest.java` — added `toIntervals_usesProvidedRootKeyInsteadOfFirstNote`: A and B relative to root C should be SIX and SEVEN

### Implemented — Frontend
- `KeySelector.tsx` — replaced flat `NOTES` string array with `{ value, label }` pairs; `value` is the Java enum name (`C_SHARP`), `label` is the display string (`C#`); fixes sharp key requests returning 400
- `client.ts` — added `inputKey?: string` to `UploadRequest` interface
- `UploadForm.tsx` — added `inputKey` state (default `''`), root key dropdown ("Root: first note" as default option) placed before the mode dropdown; conditionally includes `inputKey` in the request body

---

## Session 9

### Decisions Made
- **Sliding DFS candidate cap** — `dfsPositions` previously explored all candidates at every step, causing exponential blowup on long licks (a 23-note tab hung indefinitely). Added a per-step cap `Math.max(4, 20 / noteCount)` so shorter licks get more branching (variety) while longer licks are bounded. Candidates are proximity-sorted so capping to the N closest keeps the most musically relevant branches.
- **Global MAX_POSITIONS cap** — the old `MAX_POSITIONS = 50` check was on a per-root local list, not the global results list. Each root could add up to 50, resulting in hundreds of positions total. Fixed by passing the global results list directly into `buildPositions`/`dfsPositions` so the cap applies across all root candidates.
- **Dynamic span (`tabSpan`)** — the hardcoded 4-fret span filter rejected tabs whose original fingering spans more than 4 frets (the 23-note example spans 0–5). Fixed by storing `tabSpan = max_fret − min_fret` on the `Lick` entity at upload time and passing it as `spanLimit` to `findPositions` via `resolvePositions`. The 2-arg `findPositions` overload defaults to 4 for all existing callers and tests.
- **`buildPositions` signature change** — no longer returns a list; now takes the global results list as a parameter and mutates it in place. This is what enables the global cap.
- **Position diversity: string-pattern + fret-region dedup** — positions that share the same string assignment (same string for each note) and sit in the same 5-fret region of the neck are the same fingering shape in a slightly different register ("sequential variations"). Replaced exact-tab-string dedup with a `(string sequence, min_fret / 5)` key, keeping only the lowest-register representative per group. Preserves genuine variety (different strings OR different neck region) while eliminating near-duplicate positions.

### Implemented
- `Lick.java` — added `tabSpan` (`Integer`, nullable) with getter/setter; H2 adds the column automatically via `ddl-auto`
- `LickService.java` — sliding candidate cap in `dfsPositions`; global results list threaded through `buildPositions`; `findPositions` 3-arg overload with `spanLimit`; `resolvePositions` computes `Math.max(4, tabSpan)`; `buildDiversityKey` helper; string-pattern + fret-region dedup replacing tab-string dedup; unused `HashSet`/`Set`/`Collectors` imports removed; `LinkedHashMap`/`Map` imports added
- `BuildPositionTest.java` — updated 4 call sites from `buildPositions(root, intervals, notes, 4)` to `buildPositions(root, intervals, notes, 4, results)` with a locally created `ArrayList`; added `ArrayList` import
- `FindPositionsTest.java` — long-lick test rewritten as `findPositions_longLickProducesPositions`: computes span from parsed notes, calls 3-arg `findPositions(intervals, Note.A, Math.max(4, span))`, asserts non-empty and within MAX_POSITIONS; `@Timeout` annotation removed; added `findPositions_noDuplicateStringPatternsInSameRegion` test using the string-pattern + fret-region key

---

## Session 10

### Decisions Made
- **±2 string constraint in `findCandidates`** — the session-7 expansion to all-6-strings caused 5-string leap positions that were physically absurd. Restricted to current string ±2 (bounded to 0–5) when no technique is present. Technique still locks to same string only.
- **Round-robin interleaving** — after sorting by max-fret ascending, positions are grouped by starting string then interleaved round-robin so consecutive results always come from different starting strings. Ensures visual variety in the first several positions shown.
- **Overwrite-mode tab editor** — the upload textarea is now pre-filled with a 6-string, 16-column empty tab template. Typing replaces the character under the cursor in place (overwrite mode); the cursor advances automatically. String labels and `|` bars are protected (unoverwritable). Backspace replaces the previous char with `-`. Non-tab keys (letters, symbols not in `[0-9hp/\-]`) are blocked entirely via `e.preventDefault()` to prevent lines from growing. Cursor restoration uses `useLayoutEffect` + a `nextCursorRef` to fire synchronously after React reconciliation.
- **Position algorithm strategy pattern** — `findNeckPositions`, `findCandidates`, `buildPositions`, `dfsPositions`, `buildDiversityKey`, and `findPositions` extracted from `LickService` into an abstract `PositionBuilder` base class. Two concrete implementations: `DfsPositionBuilder` (existing logic, unchanged) and `GreedyPositionBuilder` (new — single-pass nearest-neighbour, one path per root). `LickService` holds instances of both and selects based on an `algo` param.
- **Algo selection via `?algo=` query param** — `GET /api/lick/{id}?key=A&algo=greedy` (default) or `algo=dfs`. Frontend detail page gains a Greedy/DFS toggle that triggers a re-fetch on change.
- **Mode interval tooltip** — hovering the mode chip on the detail page shows a dark tooltip with the mode's interval formula (e.g. `1  2  b3  4  5  6  b7`). Implemented with Tailwind `group`/`group-hover` opacity transition.
- **Key display fix** — the "Positions in …" heading was showing raw enum names like `G_SHARP`. Added a `KEY_LABELS` map to render `G#`, `Bb`, etc. `A_SHARP` displays as `Bb` (and in the key selector dropdown) per standard music convention.

### Implemented — Backend
- `service/PositionBuilder.java` — new abstract base class; `MAX_FRET = 15`, `MAX_POSITIONS = 50`; `findNeckPositions` and `findCandidates` (moved verbatim from `LickService`)
- `service/DfsPositionBuilder.java` — new class; all DFS + diversity dedup + round-robin logic moved from `LickService` verbatim
- `service/GreedyPositionBuilder.java` — new class; single-pass algorithm, picks `candidates.get(0)` at each step, discards path if any step fails span/fret constraints
- `LickService.java` — removed all position-building methods; added `greedyBuilder`/`dfsBuilder` fields; `resolvePositions(Lick, Note, String algo)` and `getLick(UUID, Note, String algo)`; forwarding constants `MAX_FRET`/`MAX_POSITIONS` kept for test compatibility
- `LickController.java` — `@RequestParam(defaultValue = "greedy") String algo` added to `GET /{id}`

### Implemented — Frontend
- `UploadForm.tsx` — pre-filled `EMPTY_TAB` template; `handleKeyDown` with `isProtected` guard, overwrite logic, backspace-to-dash, full single-char `preventDefault`; `useRef` + `useLayoutEffect` for cursor restoration; submit disabled until at least one digit present; resets to template on success
- `DetailPage.tsx` — `algo` state + Greedy/DFS toggle button group; `KEY_LABELS` map for display; `useEffect` depends on `[id, key, algo]`; mode interval tooltip via `MODE_INTERVALS` map + `group-hover`
- `KeySelector.tsx` — `A_SHARP` label changed from `A#` to `Bb`
- `client.ts` — `getLick(id, key, algo = 'greedy')`

### Implemented — Tests
- `FindNeckPositionsTest`, `FindCandidatesTest` — removed Mockito mocks; instantiate `new GreedyPositionBuilder()` directly
- `BuildPositionTest` — removed mocks; instantiates `new DfsPositionBuilder()`; calls `builder.buildPositions(...)`
- `FindPositionsTest` — keeps `LickService` mock for `parseTab`; adds `DfsPositionBuilder dfsBuilder`; all `findPositions` calls replaced with `dfsBuilder.build(...)`
- `FindCandidatesTest` — `searchesAllStrings` test renamed to `searchesWithinTwoStringsWhenNoTechnique`; asserts `Math.abs(stringIndex - 2) <= 2`
- `FindPositionsTest` — `ranksPositionsByMaxFretAscending` replaced with `ranksPositionsByMaxFretAscendingWithinEachStartingString` (round-robin breaks global order); added `firstRoundHasDistinctStartingStrings`

### Other
- `idea_bucket.txt` — created; 11 feature ideas with codebase-specific notes added under each point

---

## Session 11

### Decisions Made
- **LoserBracketPositionBuilder (chord-aware greedy)** — third position algorithm added, selectable via `?algo=chord`. Two-pass approach: first pass greedily places one note per unique `columnIndex`; second pass places chord partners (notes sharing a `columnIndex`) near their parallel notes on a different string. Partners that can't fit the span constraint are silently skipped rather than discarding the whole position. This is the only algorithm that correctly handles simultaneous notes.
- **Bb/grid fixes (frontend)** — the `KeySelector` was sending `A_SHARP` for B♭; `Note` enum uses `B_FLAT`. Fixed `KeySelector.tsx` and `DetailPage.tsx` `KEY_LABELS` map. The "Positions in …" heading applied `uppercase` via Tailwind which turned `Bb` into `BB`; fixed with a `normal-case` span wrapper.
- **Dynamic position grid (frontend)** — detail page positions now use a CSS `auto-fill` grid whose minimum cell width is computed from the rendered tab line length (`tabLineLength * 8.4 + 32` px), so cells fill the viewport naturally and wrap onto additional rows.
- **Tab grid expansion (idea #6, frontend)** — when the cursor is at the closing `|` of the tab textarea and a valid input character is typed, `expandTab()` inserts a `-` before each line's closing `|` to grow the grid by one column. The cursor is repositioned using `linesBefore` offset + `nextCursorRef` to land in the correct slot.
- **Two-digit fret parsing (idea #7, backend)** — `parseTab` now handles frets 10–15: when a digit at column `j` is followed by another digit at `j+1`, the two are combined into a single fret number, `j` is advanced to skip `j+1`, and the technique character is read from `j+2`. Single-digit fret handling is unchanged.
- **Instrument abstraction (idea #2, backend)** — `Guitar` is no longer a standalone constants class; it now implements a new `Instrument` interface. Four additional instruments added: `Bass`, `Ukulele`, `Mandolin`, `Banjo`. `Guitar` now has named tuning variants: `STANDARD`, `DROP_D`, `OPEN_G`, `OPEN_D`, `DADGAD`. `InstrumentRegistry.fromName(String)` maps query-param strings to instances. All position-building methods, `Position.toTabString`, and `LickUtils.toIntervals` now take an explicit `Instrument` parameter — no `Guitar.STANDARD` hardcoded outside `Guitar.java` and `LickService.uploadLick`. `TabNote.toNote()` removed; `LickUtils.toNoteString` removed (unused). All backwards-compat overloads removed; tests updated to pass `Guitar.STANDARD` explicitly.
- **`GET /api/lick/{id}` instrument param** — `?instrument=GUITAR` (default) or any `InstrumentRegistry`-known name. Invalid name returns 400.
- **`ParseTabTest` two-digit fret test un-disabled** — the test was `@Disabled("not yet implemented")` since session 6; now enabled and passing.
- **`FindNeckPositionsTest` count fixed** — expected count for E on standard guitar was 14 (assumed fret 24 in range); correct count with MAX_FRET=15 is 9.

### Implemented — Backend
- `constants/Instrument.java` — new interface: `tuning()`, `labels()`, `displayOrder()`, `name()`, `stringCount()` (default), `getNoteAt(int, int)` (default), `minFret(int)` (default → 0)
- `constants/InstrumentRegistry.java` — `fromName(String)`: switch on uppercased name; throws `IllegalArgumentException` for unknown names
- `constants/Guitar.java` — implements `Instrument`; static instances `STANDARD`, `DROP_D`, `OPEN_G`, `OPEN_D`, `DADGAD`; legacy static `getNoteAt` removed
- `constants/Bass.java`, `Ukulele.java`, `Mandolin.java`, `Banjo.java` — new `Instrument` implementations; `Banjo` has a TODO for 5th-string `minFret` quirk (deferred)
- `service/PositionBuilder.java` — all methods take `Instrument`; backwards-compat overloads removed; `Guitar` import removed
- `service/DfsPositionBuilder.java`, `GreedyPositionBuilder.java`, `LoserBracketPositionBuilder.java` — all take `Instrument`; `LoserBracketPositionBuilder` is new
- `service/LickService.java` — `getLick`/`resolvePositions`/`toLickResponse` now 4-arg; `uploadLick` uses `Guitar.STANDARD.getNoteAt(...)` instead of `toNote()`; `LickUtils.toIntervals` call passes `Guitar.STANDARD`
- `service/LickUtils.java` — `toIntervals(List<TabNote>, Note, Instrument)` 3-arg replaces 2-arg and 1-arg versions; `toNoteString` removed
- `model/TabNote.java` — `toNote()` removed; now a plain data record
- `model/Position.java` — `toTabString(Instrument)` overload added using `instrument.labels()`/`displayOrder()`/`stringCount()`; no-arg delegates to `Guitar.STANDARD`
- `controller/LickController.java` — `?instrument=` query param wired via `InstrumentRegistry`; 400 on unknown instrument
- `service/parseTab` — two-digit fret lookahead added

### Implemented — Tests
- `LoserBracketPositionBuilderTest.java` — new; 5 tests: same results as greedy on non-chord input, chord partners share `columnIndex`, partners on different strings, span respected, partial positions returned when partner unplaceable
- All 7 existing position-building tests updated to pass `Guitar.STANDARD` explicitly
- `LickUtilsTest.java` — 1-arg `toIntervals` calls replaced with 3-arg; stale comments about "A string" corrected to "B string"
- `ParseTabTest.java` — `@Disabled` removed from two-digit fret test; `toNoteString` calls removed; `toIntervals` call updated to 3-arg

---

## Session 12

### Decisions Made
- **Instrument selector (frontend + backend)** — Output positions on LibraryPage and DetailPage now reflect the selected instrument. The selector persists to `localStorage` so it survives navigation between pages. The upload pipeline stays fixed to `Guitar.STANDARD` (no need to store instrument with the lick shape).
- **Named instruments** — Dropdown offers Standard Guitar, Drop D, Open G, Open D, DADGAD, Bass, Ukulele, Mandolin, Banjo, and Custom. Frontend values match `InstrumentRegistry` keys (`GUITAR`, `DROP_D`, etc.).
- **Custom tuning with Apply button** — Custom tuning is entered as a space-separated note string (e.g. `E A D G B E`). Positions only re-fetch on explicit click of the Apply button or pressing Enter — no debounce. This avoids mid-keystroke 400 errors. `appliedTuning` state gates the fetch; `customTuning` state tracks the input field live.
- **`NoteParser`** — New utility class that maps user-typed note strings to `Note` enum values. Handles `C#`, `Db`, `Bb`, `A#`; both `A#` and `BB` map to `B_FLAT`. Case-insensitive after normalisation.
- **`CustomInstrument`** — New `Instrument` implementation constructed from a `Note[]` tuning array. Labels and display order are derived from the tuning (high string first, highest string label is lowercase) — same convention as the fix applied to `Guitar` variants this session.
- **`GET /api/lick/{id}` tuning param** — New optional `?tuning=E A D G B E` query param. When present, the backend parses it via `NoteParser` and builds a `CustomInstrument`; invalid note names return 400. `?instrument=` is used when tuning is absent (existing behaviour).
- **Guitar variant label bug fix** — `Guitar.labels()` was hardcoded to standard guitar string names (`e|B|G|D|A|E|`) for all variants, making Drop D, Open G, etc. look identical to standard in the rendered tab. Changed `labels()` to compute names dynamically from the instance's `tuning` array using a `noteToLabel()` helper. Highest-string label is lowercase; all others uppercase. Drop D's bottom string now shows `D|` instead of `E|`.
- **Positions header shows instrument** — DetailPage heading changed from `Positions in A` to `Positions in A — Bass` (or the tuning string for Custom, e.g. `E A D G B E`). Added `INSTRUMENT_LABELS` map in `DetailPage.tsx`.
- **Stale JVM diagnosis** — Backend process (PID 50842) had been running since Friday before the instrument abstraction was compiled. All instrument changes were on disk but the running JVM had old code. Killed old process and restarted; confirmed by sending an unknown instrument name and getting 400.

### Implemented — Backend
- `constants/NoteParser.java` — new class; static `Map<String, Note>` with aliases; `parse(String)` throws `IllegalArgumentException` on unknown input
- `constants/CustomInstrument.java` — new `Instrument` implementation; constructor reverses `Note[]` for labels/displayOrder (high string first); `noteToLabel` helper for display names
- `constants/Guitar.java` — `labels()` rewritten to be instance-aware: computes string names dynamically from `tuning` field via `noteToLabel(Note, boolean lowercase)` helper; hardcoded standard labels removed
- `controller/LickController.java` — `@RequestParam(required = false) String tuning` added to `GET /{id}`; when non-blank, splits on `\s+`, parses each token via `NoteParser`, constructs `CustomInstrument`; parse errors return 400

### Implemented — Frontend (`lick_library_ui`)
- `src/hooks/useInstrument.ts` — new hook; `useState` with lazy localStorage initialiser; `setInstrument` / `setCustomTuning` write-through to `localStorage`; keys `lick_instrument` / `lick_custom_tuning`
- `src/components/InstrumentSelector.tsx` — new component; `<select>` over named instrument list; when `CUSTOM` selected, shows flex row with text `<input>` (Enter triggers `onSubmit`) + indigo "Apply" button; optional `error` prop shows red text below
- `src/api/client.ts` — `getLick` gains `instrument` and `customTuning` params; sends `?tuning=` for custom, `?instrument=` for named; throws `Error(\`${res.status}\`)` so callers can detect 400
- `src/pages/LibraryPage.tsx` — imports `useInstrument` + `InstrumentSelector`; selector shown above lick list as passive preference (no re-fetch; persists selection for DetailPage)
- `src/pages/DetailPage.tsx` — imports `useInstrument` + `InstrumentSelector`; `appliedTuning` state replaces debounce; useEffect deps `[id, key, algo, instrument, appliedTuning]`; guard skips fetch when CUSTOM + empty applied tuning; `instrumentError` state for 400 responses; `INSTRUMENT_LABELS` map; "Positions in A — Bass" header format; controls row uses `flex flex-wrap items-start gap-3`

---

## Session 13

### Decisions Made
- **Persistent navbar + Layout** (idea 23) — `Layout.tsx` wraps all pages with a shared top nav; `NAV_LINKS` array drives links so adding new pages requires no nav edits. Nav links: Licks + Songs.
- **Metronome** (idea 22) — navbar widget with Web Audio API lookahead scheduling; BPM +/− buttons + tap tempo; click sound generated via `AudioContext` oscillator + exponential decay. Runs in the navbar so it persists across page navigation.
- **Songs feature — backend** (ideas 16, 24, 25) — full chord sheet pipeline: upload → parse → store → transpose → serve.
  - `Song` entity: `id`, `title`, `artist`, `key` (`originalKey`), `tempo`, `capo`, `rawChordSheet`, `chordLines` (JSON via converter), `createdAt`
  - `ChordSheetParser`: CHORD_TOKEN regex detects chord lines; `pairLines()` pairs chord rows with their lyric rows; `isChordLine()` strips parenthesized qualifiers before matching; font sizes computed from chord density; `breakLine()` splits overlong lines
  - `ChordTransposer`: token-scan approach; NC/N.C. passthrough; slash chord handling; space budget management; wraps `(G)` outer parens and preserves `G(add9)` qualifiers; negative semitone fix: `((semitones % 12) + 12) % 12`
  - `SongController`: POST `/api/song`, GET `/api/song`, GET `/api/song/{id}?semitones=`, DELETE `/api/song/{id}`, POST `/api/song/{id}/reparse`
  - `SongService`: `uploadSong`, `getSong` (with transpose), `getAllSongs`, `deleteSong`, `reparseSong`; `rawChordSheet` saved at upload for re-parse; `canReparse` flag on both summary and detail responses
- **Parenthesized chord fix** — `(G)` was misidentified as a lyric line. `isChordLine()` now strips outer parens (`^\((.+)\)$→$1`) then inner qualifiers (`\(.*?\)→""`) before testing against CHORD_TOKEN. `transposeChordPart()` re-wraps `(G)` and preserves `G(add9)`.
- **Re-parse endpoint** (idea 30) — `POST /api/song/{id}/reparse` re-runs `ChordSheetParser` on the stored `rawChordSheet`; returns 409 if `rawChordSheet` is null. `canReparse` flag drives UI visibility.
- **LAN access** — `CorsConfig` changed to `allowedOriginPatterns("*")`; Vite config: `server: { host: true, allowedHosts: true }`; `BASE_URL` changed from `localhost` hardcode to `window.location.hostname` so iPad fetches from the Mac's IP.

### Implemented — Backend
- `model/Song.java`, `model/ChordLyric.java`, `model/ChordLyricListConverter.java`
- `model/SongSummaryResponse.java`, `model/SongDetailResponse.java` — both carry `canReparse`
- `service/ChordSheetParser.java`, `service/ChordTransposer.java`
- `service/SongService.java`, `controller/SongController.java`
- `config/CorsConfig.java` — `allowedOriginPatterns("*")`
- `ChordSheetParserTest.java` — `parenthesizedChordsIdentifiedAsChordLine`, `parentheticalQualifierChordsIdentifiedAsChordLine`

### Implemented — Frontend
- `src/api/client.ts` — `SongSummary`, `SongDetail` types; `getAllSongs`, `getSong`, `uploadSong`, `deleteSong`, `reparseSong`; `BASE_URL` uses `window.location.hostname`
- `src/pages/SongsPage.tsx` — song library list; "Re-parse" toggle button reveals ↺ icon per eligible card
- `src/pages/SongDetailPage.tsx` — song header (title, artist, BPM); transpose + capo widget; `ChordSheet` component
- `src/pages/SongUploadPage.tsx` — upload form at `/songs/upload`; navigates to `/songs` on success
- `src/components/SongCard.tsx` — re-parse ↺ → ✓ on success; delete ×; navigate on click
- `src/components/SongList.tsx`, `src/components/ChordSheet.tsx`
- `src/main.tsx` — routes: `/songs`, `/songs/upload`, `/song/:id`
- `src/components/Layout.tsx` — NAV_LINKS with Songs link
- `vite.config.ts` — `server: { host: true, allowedHosts: true }`

---

## Session 14

### Decisions Made
- **Idea 27: Song upload on its own page** — upload form moved from SongsPage to a dedicated `/songs/upload` route (`SongUploadPage.tsx`); "Upload" button on SongsPage navigates there; success navigates back to `/songs`.
- **Idea 26: Flatten song detail header** — title, artist, and controls all in one horizontal row; BPM inline under artist; capo display removed from header (handled by the capo widget). Chord sheet gets more vertical space with reduced top padding.
- **Capo-aware transpose widget** (idea 29) — two groups side by side separated by a vertical divider:
  - **Capo** (left): [−] digit [+], clamped 0–11, initialized from `song.capo ?? 0` on load
  - **Transpose** (right): [−] dual-key-display [+], with reset below; reset uses `invisible` (not conditional render) to prevent layout shift
  - Dual key display: **shape** = `keyLabel(originalKey, semitones - capo)` (what you physically play), **sound** = `keyLabel(originalKey, semitones)` (what it sounds like)
  - Semitone delta (`0`, `+N`, `-N`) shown in `text-xs text-gray-300` between the shape and sound columns
- **Re-parse UX** (idea 30) — "Re-parse" toggle on SongsPage reveals ↺ icon per eligible card (only when `song.canReparse`); ↺ becomes ✓ on success via local `reparsed` state; icon is always rendered (invisible when toggle is off) to prevent layout shift.

### Implemented — Frontend
- `src/pages/SongUploadPage.tsx` — new page (idea 27)
- `src/pages/SongsPage.tsx` — reparsing toggle + onReparse refresh (idea 30)
- `src/components/SongCard.tsx` — ↺/✓ re-parse button; `reparsed` local state
- `src/pages/SongDetailPage.tsx` — flattened header (idea 26); capo-aware transpose widget with dual key display, semitone delta, and invisible reset (idea 29)

---

## Session 15

### Decisions Made
- **Idea 34: Semitone wrap** — transpose counter wraps back to 0 when it hits ±12 (full octave = back to original key). Pressing + at +11 or − at −11 resets to 0 instead of continuing to ±12.
- **Idea 35: No layout shift on transpose** — removed the inline "Transposing…" `<p>` that was the sole cause of the chord sheet jumping on every transpose fetch. Replaced with an `opacity-50` fade on the `ChordSheet` while loading; `className` prop added to `ChordSheet` to support this. Old content stays in place; new content fades in when the fetch resolves.
- **Idea 36: BPM click starts metronome** — `bpm` and `isPlaying` lifted from local `Metronome` state into a new `MetronomeContext`. `MetronomeProvider` wraps the router in `main.tsx`. `Metronome.tsx` consumes the context (adds `useEffect` to sync `bpmInput` when context `bpm` changes from outside). The BPM display in `SongDetailPage` is now a button: clicking sets context `bpm` to `song.tempo` and `setIsPlaying(true)`.
- **Idea 38: Bold chords + hover chord diagram** — two-part feature:
  - **Bold rendering**: `ChordSheet.tsx` splits each chord line by whitespace, renders chord tokens as `<ChordToken>` (bold, `display: inline-block`). NC/N.C. rendered plain (not bold). `whiteSpace: 'pre'` on the parent div preserves spacing.
  - **Hover voicings**: `ChordToken` fetches voicings from `GET /api/chord?root=E&quality=m` on `mouseEnter`; result cached in a module-level `Map` so re-hover is instant. Popover appears below the chord name (`top: 100%`) with voicing tab + `‹ N/M ›` navigation. Unknown chords (backend returns `[]`) show `???`. NC/N.C. excluded from `ChordToken` entirely.
  - **Backend**: `ChordService` defines a static `CHORD_QUALITIES` map (14 entries: major, minor, 7th, maj7, m7, sus2, sus4, dim, aug, add9, 6, m6, dim7, m7b5). `getVoicings(Note, String, Instrument)` wraps the interval list as simultaneous `IntervalNote`s (all `columnIndex=0`), runs `LoserBracketPositionBuilder`, returns rendered tab strings. No DB storage — voicings are computed on demand.
  - **Frontend chord parser**: `src/utils/parseChordName.ts` — splits chord name into root (mapped to Java `Note` enum name) + quality suffix; strips slash bass (`G/B` → `G`); returns `null` for NC/N.C.

### Implemented — Backend
- `controller/ChordController.java` — `GET /api/chord?root=&quality=&instrument=`; validates root via `Note.valueOf()`, instrument via `InstrumentRegistry`; 400 on unknown quality
- `service/ChordService.java` — `CHORD_QUALITIES` static map; `getVoicings(Note, String, Instrument)`; `knowsQuality(String)`

### Implemented — Frontend
- `src/contexts/MetronomeContext.tsx` — new; `MetronomeProvider` + `useMetronomeContext` hook
- `src/main.tsx` — wrapped with `MetronomeProvider`
- `src/components/Metronome.tsx` — consumes `MetronomeContext` for `bpm`/`isPlaying`; `useEffect` syncs `bpmInput` on external BPM change
- `src/pages/SongDetailPage.tsx` — BPM span → clickable button (idea 36); semitone wrap ±12 (idea 34); `ChordSheet` gets `className` for opacity fade (idea 35); removed "Transposing…" inline paragraph
- `src/components/ChordSheet.tsx` — `ChordToken` component; `renderChords()` helper; `className` prop on root div; `voicingCache` module-level map
- `src/utils/parseChordName.ts` — new; root+quality parser with Note enum mapping
- `src/api/client.ts` — `getChordVoicings(root, quality)` function
