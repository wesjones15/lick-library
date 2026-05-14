The current state of the app is a react vite frontend and a java spring boot backend. you input a guitar tab string, and it is converted into a collection of note objects and then gets processed into a list of output tab strings in the chosen key and mode. the unique licks get stored in a h2 db.

The purpose of this file is to hold ideas in memory so we can easily refer to them in claude code ui.

key: [ ]  = open, [x] = complete, [~] = deferred
---

<details>
<summary>[x] 1. Position algorithm selector</summary>

- Strategy pattern with abstract PositionBuilder; greedy default, DFS optional
- `?algo=` param on GET /api/lick/{id}; frontend toggle on detail page
- Implemented with an abstract class/method because for each algo, the input and output types will be the same
- findPositions, buildPositions, dfsPositions, and findCandidates extracted from LickService into PositionBuilder strategy classes

</details>

<details>
<summary>[x] 2. Multi-instrument support</summary>

- Instrument interface with tuning (Note[]), labels, displayOrder, stringCount, getNoteAt, minFret
- Implemented: Guitar (STANDARD, DROP_D, OPEN_G, OPEN_D, DADGAD), Bass, Ukulele, Mandolin, Banjo
- Banjo 5th string starts at fret 5 (minFret TODO)
- Frontend instrument selector on detail page; all position logic calls through Instrument interface

</details>

<details>
<summary>[x] 3. Loser-bracket chord-aware algorithm</summary>

- Two-pass greedy: pass 1 places one note per unique columnIndex (melodic line); pass 2 places chord partners on a different string near the parallel note
- Directly addresses the known limitation where buildPosition broke for shared columnIndex (simultaneous) notes
- Slots in as algorithm 3 under the PositionBuilder abstraction

</details>

<details>
<summary>[x] 4. Song chord sheet with transposition</summary>

- ChordLyric objects: string chords, string lyrics, double fontSize
- Song DB: title, artist, key, tempo, capo, ordered list of ChordLyric
- One-pager: 2–3 columns, font auto-sized to fill screen without line breaks, pairs share font size
- Upload flow: txt intake + metadata → backend processing → stored as ChordLyric list
- Transposition updates chord lines in the displayed song object

</details>

<details open>
<summary>[ ] 5. Multi-measure phrase segmentation</summary>

- Long phrases split at `|` boundaries before running position builders
- parseTab currently strips `|` — would need to emit measure-break markers during parsing
- Segment interval list, run findPositions per segment; would improve quality on multi-bar phrases

</details>

<details>
<summary>[x] 6. Tab grid auto-expand at boundary</summary>

- Detect cursor sitting on a closing `|`, insert `-` before each line's closing `|` on all 6 lines simultaneously
- Recalculate cursor position after expansion
- Paste is safe — onChange fires for paste and bypasses handleKeyDown entirely

</details>

<details>
<summary>[x] 7. Two-digit fret parsing</summary>

- Lookahead in parseTab: when a digit is found at position j, check if j+1 is also a digit, combine and skip j+1
- Handles frets 0–99

</details>

<details open>
<summary>[ ] 8. Scale / CAGED neck learning tool</summary>

- Full CAGED/diatonic scales for each mode as a learning tool; additional vertical on the site
- Define interval sequence for each mode, build scale positions per key
- Depends on #19 (neck visualization) for the visual neck display component
- Question: how to present modes — as shifted major or from own root? Open design discussion
- scrap above, maybe caged scales just get added as a third vertical for the site, as a learning tool for the user.
  - what is the digital equivalent of the paper scale cards i learned from?
  - we can use the little maths i figured out about scales in this somehow
  - like how diatonic ionian G shape is composed of pentatonic G C and D overlayed in the same section

</details>

<details open>
<summary>[ ] 9. iPad PWA (home screen shortcut)</summary>

- Pure frontend config: manifest.json, apple-touch-icon, viewport meta, display: standalone
- Vite PWA plugin (vite-plugin-pwa) handles most boilerplate
- No backend touches needed
- Target: 2025 iPad Air

</details>

<details open>
<summary>[ ] 10. Auth & security hardening</summary>

- JWT auth, Spring Security, CORS policy, rate limiting, user-scoped repositories
- H2 → Postgres migration required
- Worth doing last if at all; not prioritized over functional features

</details>

<details open>
<summary>[ ] 11. Parallel harmony generator</summary>

- Static phrase transformation in interval-space
- Input: List\<IntervalNote\>, Mode, harmonicInterval (e.g. THIRD, SIXTH)
- Map each note to its diatonic equivalent N scale degrees up within the mode's interval sequence
- Snap-to-scale flag for chromatic/passing tones
- Lives in LickUtils as a stateless pure function alongside toIntervals and detectMode

</details>

<details open>
<summary>[ ] 12. Upload instrument selection</summary>

- Allow user to change input instrument for tab upload
- parseTab + toIntervals currently always use Guitar.STANDARD
- Would potentially remove need for TabNote if parseTab is retooled for any tuning/instrument

</details>

<details open>
<summary>[ ] 13. Containerize and deploy</summary>

- Containerize frontend and backend
- Deploy to a hosting service

</details>

<details>
<summary>[x] 14. GitHub setup</summary>

- Set up GitHub upstream; uploaded frontend and backend to personal GitHub

</details>

<details open>
<summary>[ ] 15. Phrase predictor</summary>

- Deterministic rule engine; no AI/probabilistic components
- Input: key, Mode, chord progression, optional seed phrase
- Rules: chord tones on strong beats → stepwise voice leading → tension/resolution → passing tones on weak beats
- Output: List\<IntervalNote\> fed through existing position pipeline
- Guitar.STANDARD may attach idiomatic jump hint table (see #20) as weighted candidates

</details>

<details>
<summary>[x] 16. One-pager song chord sheet display</summary>

- Monospaced font (Roboto/Courier New); 2–3 columns depending on song size
- ChordLyric pairs: chord row + lyric row share font size; auto-shrink to prevent line breaks
- Song list page showing artist + song name; upload button → upload song page
- Processing happens in backend on upload; chord lines stored separate from lyrics for transposition
- Font size computed at upload time against fixed iPad Air horizontal viewport reference

</details>

<details open>
<summary>[ ] 17. Octave tracking</summary>

- Derive absolute pitch from fret + string using open string offsets + semitones per fret
- No string gauge attribute needed — gauge affects tone not pitch
- Each Instrument provides open string absolute pitches alongside existing tuning()
- Add octave as a derived field on TabNote or wrapper, computed at render time

</details>

<details open>
<summary>[ ] 18. Microphone pitch detection</summary>

- YIN algorithm or pitchfinder.js for single-note pitch detection
- Limit to Guitar.STANDARD + clean tone input for accuracy
- On note detected: resolve to nearest Note enum, display all neck positions via findNeckPositions
- Separate Fretboard Explorer mode; tab-following (timing sync) is a later phase
- Known constraint: distortion and low notes degrade accuracy

</details>

<details open>
<summary>[ ] 19. Animated neck visualization</summary>

- Playhead-driven neck display; notes flash on active fret/string at tempo
- Playhead indexes into columnIndex sequence at rate derived from BPM; pairs with metronome
- Instrument-aware: render string count and layout from Instrument interface
- Scale backdrop (#8) overlays dim dots for valid mode positions in current fret window

</details>

<details open>
<summary>[ ] 20. CAGED idiomatic jump hints</summary>

- Guitar.STANDARD-specific static structure: list of (intervalDelta, stringsTraversed) pairs
- Consumed by predictor (#15) as weighted hint table for interval selection
- Does not affect position finding geometry — only influences which interval to target
- Other instruments may define their own tables on their own classes; no shared interface contract

</details>

<details open>
<summary>[ ] 21. Scale overlay + mic input page</summary>

- Separate navbar page combining neck display and mic input
- Select key and mode; overlay scale positions as dim dots on the neck
- Mic input identifies pitch live and highlights corresponding note on neck
- Depends on #18 (mic pitch detection) and #19 (neck visualization)

</details>

<details>
<summary>[x] 22. Navbar metronome widget</summary>

- Lives in navbar as a collapsible widget accessible from any page
- Web Audio API AudioWorklet for drift-free timing (not setInterval)
- Visual pulse tied to the audio clock

</details>

<details>
<summary>[x] 23. Persistent navbar</summary>

- Navbar on top of site for desktop displays
- Site name + nav links (Licks, Songs); designed to accommodate new buttons as features grow

</details>

<details>
<summary>[x] 24. Song transposition</summary>

- Transpose up/down via semitone counter; updates chord lines in displayed song
- Accounts for sharps/flats added/removed; minimum 1-space gap between chords maintained
- Slash chords (G/B): both roots transposed independently, rejoined with "/"
- Length delta compensation: if sharp is added, insert space in lyrics string at same index

</details>

<details open>
<summary>[ ] 25. Chord parser gaps (font recompute + boundary)</summary>

- Long line auto-break: chord/lyric pair too long → split into two halves; enforce same length via trailing spaces
- Default behavior: shrink font, but enforce minimum readable size and max line length; break at last word before limit
- After splitting, strip leading spaces from both strings in second ChordLyric symmetrically
- Err on shorter lines during breaking without splitting a chord or a word

</details>

<details>
<summary>[x] 26. Flatten song detail header</summary>

- Title card flatter and spread horizontally; song content area taller

</details>

<details>
<summary>[x] 27. Song upload on own page</summary>

- Upload song form on its own page; upload button remains on songs list page

</details>

<details open>
<summary>[ ] 28. Chord sheet parser gaps (font + boundary)</summary>

- Gap 1 — font size not recomputed after line breaking: after breaking over-long pairs, recompute globalFontSize across full resulting list; halves are shorter so minimum will be larger → more readable
- Fix: two-pass in ChordSheetParser.applyFontSizes — first break all over-long pairs, recompute globalFontSize, then apply to every non-spacer pair
- Gap 2 — chord boundary not checked during break: if split index falls mid-token in chord string (e.g. "G/B"), chord gets silently truncated
- Fix: in ChordSheetParser.breakLine(), after finding lyrics word boundary, walk back while chords.charAt(breakAt - 1) != ' '; take the more conservative (shorter) of lyrics and chord boundaries

</details>

<details>
<summary>[x] 29. Capo-aware transpose widget</summary>

- Capo group on left, Transpose group on right of song header
- Capo adjustable via widget; transpose shows semitone delta from original key
- Key of B in capo 4 shows leading chord as G
- Reset button below transpose row (hidden at semitones=0)

</details>

<details>
<summary>[x] 30. Re-parse button</summary>

- Button on song detail page triggers rerun of chord sheet parsing logic for existing songs
- Purpose: update songs after song parser logic is updated

</details>

<details open>
<summary>[ ] 31. Delete confirmation dialog</summary>

- Warning/confirmation box before song deletion

</details>

<details open>
<summary>[ ] 32. Song recycle bin</summary>

- Deleted songs go to a recycle DB where they can be recovered

</details>

<details open>
<summary>[ ] 33. Users, auth, playlists</summary>

- Account creation with admin-gated 2FA
- Users can upload songs, view songs, make and view playlists
- Users can only delete their own songs; admin can delete anything

</details>

<details>
<summary>[x] 34. Semitone wrap to zero at octave</summary>

- Transpose counter wraps back to 0 when cycling through a full octave (±12)

</details>

<details>
<summary>[x] 35. No layout shift on transpose</summary>

- Chord sheet fades to 50% opacity during transpose fetch; no element added/removed from DOM
- "Transposing…" placeholder element removed entirely

</details>

<details>
<summary>[x] 36. BPM click starts metronome</summary>

- Clicking BPM displayed under song name starts the metronome at that tempo
- MetronomeContext shared between SongDetailPage and Metronome component

</details>

<details open>
<summary>[ ] 37. Metronome time signature options</summary>

- Options for single tone (no accent) and 3/4 time

</details>

<details>
<summary>[x] 38. Bold chords + hover chord diagram</summary>

- Chord names bold in sheet (NC excluded)
- Hover popover shows chord as ASCII tab using existing position pipeline
- Chord qualities defined as static interval sets in ChordService (~14 qualities); no DB storage
- Voicings derived on-the-fly via LoserBracketPositionBuilder; module-level cache prevents re-fetching
- Multiple voicings navigable with ‹ N/M › pagination in popover; unknown chords show `???`

</details>

<details open>
<summary>[ ] 39. Capo reset button</summary>

- Reset button for capo (parallel to transpose reset)

</details>

<details open>
<summary>[ ] 40. Global playlists</summary>

- Playlist tab in navbar; list shows playlist name, song count, creator
- Clicking song from playlist shows "back to playlist" button above song name (hyperlinked)
- Songs in playlist show Next → / ← Back for sequential navigation
- Per-song key/capo override stored in playlist; overrides song default

</details>

<details open>
<summary>[ ] 41. Slash chord display fix (G/B hover diagram)</summary>

- Update chord hover to handle slash chords — strip bass note, look up root chord quality

</details>

<details open>
<summary>[ ] 42. Show all chords button on song page</summary>

- Button on song title bar; on click shows list of all chords in the song

</details>

<details open>
<summary>[ ] 43. Chord voicing improvements (real voicings, visual display)</summary>

- Update chord building logic to use real and common voicings
- Allow user to provide voicings on upload
- Visual grid display as alternative to ASCII tab in popover
- Consider whether backend chord logic should use TabNote instead of IntervalNote

</details>

<details open>
<summary>[ ] 44. Update frontend and backend READMEs</summary>

- Update README on frontend and backend to reflect new feature set

</details>

<details open>
<summary>[ ] 45. song metadata update form</summary>

- button to page with form to submit an update request to song details. allow user to submit revision to song attributes. perhaps future allow user to update individual ChordLyric blocks. updates go to a queue where admin can review and approve. upon approval, db entries update. perhaps line gets reparsed
</details>

<details open>
<summary>[ ] 46. add ability to include tab snippets in chord sheets</summary>

- add GuitarTabLine as object in chordsheet, since some chordsheets include riffs. these can have chord labels above, or not. but will be like 6 lines and we already know how to detect.
</details>