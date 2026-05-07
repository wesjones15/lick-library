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
