# Lick Library — Backend

A Spring Boot service for storing and exploring guitar licks and song chord sheets. Upload a tab once; retrieve playable positions in any key on any instrument. Upload a chord sheet; transpose it to any key on the fly. All endpoints are protected by JWT authentication via Google OAuth2.

---

## Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.0 |
| Database | PostgreSQL |
| Migrations | Flyway |
| ORM | JPA / Hibernate |
| Tests | JUnit 5 + Mockito |

---

## Authentication

The API uses Google OAuth2 for login and JWTs for session management.

### Login flow

1. Client redirects user to `GET /api/oauth2/authorize?provider=google`
2. Google authenticates and redirects to `GET /api/auth/callback`
3. Server looks up or creates the `User` record, issues a signed JWT, and redirects to the frontend with `?token=<jwt>` in the query string
4. Client stores the token and sends it as `Authorization: Bearer <token>` on all subsequent requests

### Endpoint protection

- All endpoints except `/api/auth/**` require a valid JWT
- `/api/admin/**` additionally requires `role = ADMIN`
- A 401 is returned for missing or invalid tokens

### JWT payload

```json
{ "sub": "<userId>", "role": "ADMIN|USER", "status": "PENDING|APPROVED|REJECTED" }
```

### User lifecycle

New users are created with `status = PENDING`. An admin must approve them via `/api/admin/approve/{userId}` before they can access the app. Until then all non-auth requests return 403.

### Dev profile

When the `dev` Spring profile is active, `POST /api/auth/dev/login?userId=<id>` issues a token for any existing user without OAuth. Disabled in production.

---

## Running

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080/api`.

Requires a local PostgreSQL database named `licklibrary`. Connection is configured per Spring profile (default active profile is `local`):

| Profile | URL | Credentials |
|---|---|---|
| `local` | `jdbc:postgresql://localhost:5432/licklibrary` | username `$USER`, no password |
| `dev` | `jdbc:postgresql://localhost:5432/licklibrary` | username `$USER`, no password |
| `prod` | `$DB_URL` | `$DB_USERNAME` / `$DB_PASSWORD` env vars |

Google OAuth credentials must be provided via `$GOOGLE_CLIENT_ID` and `$GOOGLE_CLIENT_SECRET` env vars (all profiles).

---

## API

> All endpoints below require `Authorization: Bearer <token>` unless stated otherwise.

---

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
| `algo` | `greedy` | Position-finding algorithm: `greedy`, `dfs`, or `chord`. Ignored when `instrument` is not `GUITAR`. |
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

The raw chord sheet is parsed into a list of `ChordLyric` pairs (chord row + lyric row). Font size is auto-computed based on line length and column count.

---

### List all songs

```
GET /api/song
GET /api/song?mine=true
```

Returns all songs as summary objects (`id`, `title`, `artist`, `originalKey`, `tempo`, `canReparse`, `ownedByCurrentUser`). Admins see all songs; regular users see only songs they uploaded. Pass `?mine=true` to filter to your own songs regardless of role.

---

### Get a song

```
GET /api/song/{id}?semitones=0
```

| Param | Default | Description |
|---|---|---|
| `semitones` | `0` | Transpose the chord sheet by this many semitones at response time |

Returns the full song including `chordLines` (list of `ChordSheetLine`), `numColumns`, `capo`, `tempo`, `originalKey`, and `timeSignature`.

---

### Update a song

```
PUT /api/song/{id}
Content-Type: application/json

{
  "title":         "Blackbird",
  "artist":        "The Beatles",
  "originalKey":   "G",
  "tempo":         92
}
```

Owner and admins only. Updates song metadata. Include `rawChordSheet` instead to re-parse the chord chart (metadata fields are ignored when `rawChordSheet` is present).

---

### Submit a song update request (non-owners)

```
POST /api/song/{id}/update-request
Content-Type: application/json

{
  "title": "...",           // metadata fields, OR
  "rawChordSheet": "..."    // chart update — mutually exclusive
}
```

For non-owners: queues the change for admin review instead of applying it directly. Returns `SongUpdateRequestSummary` (status `PENDING`).

---

### Re-parse a song

```
PUT /api/song/{id}/reparse
```

Re-runs the chord sheet parser on the stored `rawChordSheet`. Owner and admins only. Returns the updated song detail.

---

### Delete a song

```
DELETE /api/song/{id}
```

Owner and admins only. Returns 204.

---

### Get a song beatmap

```
GET /api/song/{id}/beatmap
```

Returns `BeatmapResponse { beats: int[] }` — list of beat timestamps in milliseconds. Returns 404 if no beatmap has been saved.

---

### Save a song beatmap

```
PUT /api/song/{id}/beatmap
Content-Type: application/json

{ "beats": [0, 650, 1300, 1950, ...] }
```

Owner and admins only. Stores or replaces the beat timing map for the song. Returns the saved `BeatmapResponse`.

---

### Submit a beatmap update request (non-owners)

```
POST /api/song/{id}/beatmap-request
Content-Type: application/json

{ "beats": [0, 650, 1300, ...] }
```

Queues a beatmap change for admin review. Returns `SongUpdateRequestSummary`.

---

### Get chord voicings for a root + quality

```
GET /api/chord?root=A&quality=m7&instrument=GUITAR
```

| Param | Default | Description |
|---|---|---|
| `root` | *(required)* | Root note: `A`, `C_SHARP`, `B_FLAT`, etc. |
| `quality` | *(required)* | Chord quality (see table below) |
| `instrument` | `GUITAR` | Named instrument preset |

Returns `List<ChordVoicingResponse>` — each with `id`, `frets` (int array), and `source`. For `instrument=GUITAR`, returns real CAGED shapes transposed to the requested root, user-uploaded shapes first. Unknown quality → 400.

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

### Get all chord voicings (chord gallery)

```
GET /api/chord/all?root=A&instrument=GUITAR
```

Returns all voicings for every quality for the given root, grouped as `Map<quality, List<ChordVoicingResponse>>`.

---

### Upload a chord voicing

```
POST /api/chord
Content-Type: application/json

{
  "chordName": "Am7",    // parsed into root + quality
  "frets":     [-1, 0, 2, 0, 1, 0]
}
```

Adds a user-submitted voicing. Duplicate fret arrays for the same chord are rejected with 409.

---

### Delete a chord voicing

```
DELETE /api/chord/{id}
```

Returns 204.

---

### Get scale positions

```
GET /api/scale?root=A&mode=IONIAN&instrument=GUITAR
```

| Param | Default | Description |
|---|---|---|
| `root` | *(required)* | Root note: `A`, `C_SHARP`, `B_FLAT`, etc. |
| `mode` | *(required)* | Scale mode: `IONIAN`, `DORIAN`, `PHRYGIAN`, `LYDIAN`, `MIXOLYDIAN`, `AEOLIAN`, `LOCRIAN` |
| `instrument` | `GUITAR` | Named instrument preset |

Returns `ScaleResponse { root, mode, positions: [{ string, fret, degree, note }] }` — all positions for every note of the scale across the neck up to fret 15.

---

### User profile

```
GET  /api/user/me                          → UserProfileResponse
PATCH /api/user/me/username                → UserProfileResponse
  body: { "username": "jdoe" }
POST /api/user/request-deletion            → 204  (queues deletion for admin review)
DELETE /api/user/me                        → 204  (immediate self-deletion)
```

---

### Admin

All `/api/admin/**` endpoints require `role = ADMIN`.

```
GET  /api/admin/queue                      → List<AdminUserResponse>  pending users
POST /api/admin/approve/{userId}           → AdminUserResponse | 204
    approves ACCOUNT_CREATION; if requestType=ACCOUNT_DELETION, deletes the user and returns 204
POST /api/admin/reject/{userId}            → AdminUserResponse
GET  /api/admin/users                      → List<AdminUserResponse>
DELETE /api/admin/users/{userId}           → 204

GET  /api/admin/song-updates               → List<SongUpdateRequestSummary>  pending changes
GET  /api/admin/song-updates/{id}          → SongUpdateReviewResponse  with before/after diff
POST /api/admin/song-updates/{id}/approve  → 200  applies the change
POST /api/admin/song-updates/{id}/reject   → 200
```

---

### Playlist

```
POST   /api/playlist                          → PlaylistSummaryResponse
  body: { "name": "Jazz Standards" }

GET    /api/playlist                          → List<PlaylistSummaryResponse>
    users see own playlists; admins see all

GET    /api/playlist/{id}                     → PlaylistDetailResponse
PATCH  /api/playlist/{id}                     → PlaylistSummaryResponse  (rename)
  body: { "name": "New Name" }
DELETE /api/playlist/{id}                     → 204
PATCH  /api/playlist/{id}/visibility?isPublic=true → 204

POST   /api/playlist/{id}/entries             → PlaylistDetailResponse
  body: { "songId": "...", "keyOffset": 0, "capoOffset": 0 }

PUT    /api/playlist/{id}/entries/{entryId}   → PlaylistDetailResponse
  body: { "position": 2, "keyOffset": -2, "capoOffset": 1 }

DELETE /api/playlist/{id}/entries/{entryId}           → PlaylistDetailResponse
DELETE /api/playlist/{id}/entries/{entryId}/overrides  → PlaylistDetailResponse  (clear key/capo overrides)

GET    /api/playlist/containing?songId={songId}        → List<{ playlistId, entryId }>
```

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

### Non-standard instruments
When `instrument` is anything other than `GUITAR`, the `CrossInstrumentPositionBuilder` is used automatically regardless of the `algo` parameter. It applies a greedy path-finding strategy adapted for instruments with different string counts and tunings.

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
List<ChordSheetLine>   — ChordLyric (chord row + lyric row) or GuitarTabLine
  │                       section headers, spacers; font size auto-computed per pair
  ▼  persist Song
```

```
GET /api/song/{id}?semitones=N
  │
  ▼  fetch Song
  │
  ▼  ChordTransposer.transpose(chordLines, semitones)
List<ChordSheetLine>   — all chord tokens shifted N semitones; slash chords handled per-root
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
│   ├── auth/                          OAuth2 handlers + dev login
│   │   ├── DevLoginController.java    POST /api/auth/dev/login — dev profile only
│   │   ├── GoogleOAuth2UserService.java
│   │   ├── LickLibraryOAuth2User.java
│   │   ├── OAuth2LoginFailureHandler.java
│   │   └── OAuth2LoginSuccessHandler.java  issues JWT, redirects to frontend
│   ├── config/
│   │   ├── CorsConfig.java            allows GET POST PUT PATCH DELETE from localhost:5173
│   │   ├── FlywayConfig.java          Flyway migration runner + checksum repair on startup
│   │   ├── OAuth2Config.java
│   │   └── SecurityConfig.java        JWT filter + OAuth2 login chain, endpoint protection rules
│   ├── exception/
│   │   └── ResourceNotFoundException.java  @ResponseStatus(NOT_FOUND)
│   └── security/
│       ├── JwtAuthenticationFilter.java
│       ├── JwtTokenProvider.java
│       └── UserPrincipal.java         record: userId, role, status
│
└── domain/
    ├── shared/                        shared kernel — imported by all domains
    │   ├── Note.java                  enum — 12-tone chromatic scale
    │   ├── Interval.java              enum — scale degrees with display names
    │   ├── Mode.java                  enum — IONIAN … LOCRIAN
    │   ├── TabNote.java               record — raw parsed note
    │   ├── IntervalNote.java          record — interval + technique + columnIndex
    │   ├── Position.java              record — List<TabNote> + toTabString(Instrument)
    │   ├── Instrument.java            interface
    │   ├── InstrumentRegistry.java    name → Instrument lookup
    │   ├── NoteParser.java            "C#" / "Bb" → Note enum
    │   └── instrument/
    │       ├── Guitar.java            STANDARD, DROP_D, OPEN_G, OPEN_D, DADGAD
    │       ├── Bass.java
    │       ├── Ukulele.java
    │       ├── Mandolin.java
    │       ├── Banjo.java
    │       └── CustomInstrument.java  built from tuning string at request time
    │
    ├── position/                      position-finding infrastructure
    │   ├── PositionCache.java         JPA entity (reserved for caching)
    │   ├── PositionCacheRepository.java
    │   ├── LickUtils.java             toIntervals, toAbsoluteNotes, hashIntervals, detectMode
    │   └── builder/
    │       ├── PositionBuilder.java               abstract base: findNeckPositions, findCandidates
    │       ├── GreedyPositionBuilder.java          single-pass nearest-neighbour
    │       ├── DfsPositionBuilder.java             depth-first with diversity dedup
    │       ├── LoserBracketPositionBuilder.java    two-pass chord-aware greedy
    │       └── CrossInstrumentPositionBuilder.java  used automatically for non-GUITAR instruments
    │
    ├── lick/
    │   ├── Lick.java                  JPA entity
    │   ├── LickController.java        REST endpoints: POST/GET/DELETE /api/lick
    │   ├── LickRepository.java
    │   ├── LickService.java           upload + lookup orchestration, tab parsing
    │   ├── IntervalNoteListConverter.java  JPA converter for List<IntervalNote>
    │   └── dto/
    │       ├── LickResponse.java
    │       ├── PositionResponse.java
    │       └── UploadLickRequest.java
    │
    ├── song/
    │   ├── Song.java                  JPA entity
    │   ├── SongBeatmap.java           JPA entity — beat timestamps for a song
    │   ├── SongBeatmapRepository.java
    │   ├── SongLick.java              JPA entity — lick ↔ song association
    │   ├── SongLickRepository.java
    │   ├── SongUpdateRequest.java     JPA entity — pending chart/metadata changes
    │   ├── SongUpdateRequestRepository.java
    │   ├── SongController.java        REST: POST/GET/PUT/DELETE /api/song + beatmap + update-request
    │   ├── SongRepository.java
    │   ├── SongService.java
    │   ├── dto/
    │   │   ├── BeatmapResponse.java
    │   │   ├── SongDetailResponse.java
    │   │   ├── SongLickInfo.java
    │   │   ├── SongSummaryResponse.java
    │   │   ├── SongUpdateRequestSummary.java
    │   │   ├── SongUpdateReviewResponse.java
    │   │   ├── UpdateSongRequest.java
    │   │   └── UploadSongRequest.java
    │   └── parsing/
    │       ├── ChordLyric.java           record — chords, lyrics, fontSize
    │       ├── ChordLyricListConverter.java  JPA converter (JSON via Jackson)
    │       ├── ChordSheetLine.java        sealed interface — ChordLyric | GuitarTabLine
    │       ├── ChordSheetParser.java      raw text → List<ChordSheetLine>; handles tab lines
    │       ├── ChordTransposer.java       transposes ChordLyric list by N semitones
    │       └── GuitarTabLine.java        record — raw ASCII guitar tab block in a chord sheet
    │
    ├── chord/
    │   ├── ChordQuality.java          JPA entity — chord suffix
    │   ├── ChordShape.java            JPA entity — CAGED template frets + user voicings
    │   ├── ChordQualityRepository.java
    │   ├── ChordShapeRepository.java
    │   ├── ChordService.java          voicings via shape transposition; upload/delete
    │   ├── ChordShapeSeed.java        seeds CAGED shapes on first boot (system source only)
    │   └── ChordController.java       GET /api/chord, GET /api/chord/all, POST /api/chord, DELETE /api/chord/{id}
    │
    ├── playlist/
    │   ├── Playlist.java              JPA entity
    │   ├── PlaylistEntry.java         JPA entity — song in a playlist with position + key/capo overrides
    │   ├── PlaylistEntryRepository.java
    │   ├── PlaylistRepository.java
    │   ├── PlaylistController.java    REST: /api/playlist/**
    │   ├── PlaylistService.java
    │   └── dto/
    │       ├── AddEntryRequest.java
    │       ├── CreatePlaylistRequest.java
    │       ├── PlaylistContainingEntry.java
    │       ├── PlaylistDetailResponse.java
    │       ├── PlaylistEntryResponse.java
    │       ├── PlaylistSummaryResponse.java
    │       └── UpdateEntryRequest.java
    │
    ├── scale/
    │   ├── ScaleController.java       GET /api/scale
    │   ├── ScaleService.java          maps all scale notes to neck positions via findNeckPositions
    │   └── dto/
    │       ├── ScalePosition.java     record: string, fret, degree, note
    │       └── ScaleResponse.java     record: root, mode, positions
    │
    └── user/
        ├── User.java                  JPA entity — id (Long), googleId, email, username, role, status
        ├── UserController.java        REST: /api/user/**
        ├── AdminController.java       REST: /api/admin/**  (requires ADMIN role)
        ├── UserRepository.java
        ├── UserRole.java              enum: ADMIN, USER
        ├── UserService.java           OAuth lookup/create, token issuance, approval workflow
        ├── UserStatus.java            enum: PENDING, APPROVED, REJECTED
        └── dto/
            ├── AdminUserResponse.java
            ├── UpdateUsernameRequest.java
            └── UserProfileResponse.java
```

---

## Data model

### `lick` entity

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

### `song` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `owner_id` | BIGINT | FK → `users.id` |
| `title` | VARCHAR | |
| `artist` | VARCHAR | |
| `original_key` | VARCHAR | |
| `capo` | INTEGER | |
| `tempo` | INTEGER | |
| `bpm_offset` | DECIMAL | Fine-grained tempo adjustment |
| `time_signature` | VARCHAR | e.g. `"4/4"` |
| `chord_lines` | TEXT | JSON list of ChordSheetLine objects |
| `num_columns` | INTEGER | computed at parse time |
| `raw_chord_sheet` | TEXT | Original upload; used by reparse |
| `created_at` | TIMESTAMP | |

### `song_beatmap` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `song_id` | UUID | |
| `beats` | TEXT | Comma-separated beat timestamps in milliseconds |

### `song_update_request` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `song_id` | UUID | |
| `requester_id` | BIGINT | FK → `users.id` |
| `request_type` | VARCHAR | `SONG_METADATA`, `SONG_CHART`, or `SONG_BEATMAP` |
| `payload` | TEXT | JSON-encoded update payload |
| `status` | VARCHAR | `PENDING`, `APPROVED`, `REJECTED` |
| `created_at` | TIMESTAMP | |

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

14 rows seeded on first startup. `suffix` matches the `quality=` request param.

### `chord_shape` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `chord_quality_id` | UUID | FK → `chord_quality` |
| `shape_name` | VARCHAR | `CAGED_E`, `CAGED_A`, `CAGED_G`, `CAGED_C`, `CAGED_D` |
| `template_frets` | TEXT | JSON int array — `-1` stays open, `0+` fretted (offset on transpose) |
| `root_string` | INTEGER | Index into instrument tuning (0 = lowest string) |
| `instrument` | VARCHAR | `"GUITAR"` |
| `source` | VARCHAR | `"system"` for seed rows; `"user"` for uploaded voicings |
| `label` | VARCHAR | Nullable |

### `users` entity

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | PK, auto-increment |
| `google_id` | VARCHAR | UNIQUE — Google OAuth subject identifier |
| `email` | VARCHAR | UNIQUE |
| `username` | VARCHAR | Nullable; user-settable display name |
| `role` | VARCHAR | `ADMIN` or `USER` |
| `status` | VARCHAR | `PENDING`, `APPROVED`, or `REJECTED` |
| `creation_ts` | TIMESTAMP | |
| `request_type` | VARCHAR | `ACCOUNT_CREATION` or `ACCOUNT_DELETION` (when pending deletion review) |

### `playlist` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `owner_id` | BIGINT | FK → `users.id` |
| `name` | VARCHAR | |
| `is_public` | BOOLEAN | |
| `created_at` | TIMESTAMP | |

### `playlist_entry` entity

| Column | Type | Notes |
|---|---|---|
| `id` | UUID | PK |
| `playlist_id` | UUID | FK → `playlist` |
| `song_id` | UUID | FK → `song` |
| `position` | INTEGER | Sort order within the playlist |
| `key_offset` | INTEGER | Semitone shift applied on top of the song's original key |
| `capo_offset` | INTEGER | Capo adjustment relative to the song's default capo |

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
