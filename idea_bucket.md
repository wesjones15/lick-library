The current state of the app is a react vite frontend and a java spring boot backend. you input a guitar tab string, and it is converted into a collection of note objects and then gets processed into a list of output tab strings in the chosen key and mode. the unique licks get stored in a h2 db.

The purpose of this file is to hold ideas in memory so we can easily refer to them in claude code ui.

key: [ ]  = open, [x] = complete, [~] = deferred
---
<details>
<summary>Completed</summary>

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

<details>
<summary>[x] 14. GitHub setup</summary>

- Set up GitHub upstream; uploaded frontend and backend to personal GitHub

</details>

<details>
<summary>[x] 16. One-pager song chord sheet display</summary>

- Monospaced font (Roboto/Courier New); 2–3 columns depending on song size
- ChordLyric pairs: chord row + lyric row share font size; auto-shrink to prevent line breaks
- Song list page showing artist + song name; upload button → upload song page
- Processing happens in backend on upload; chord lines stored separate from lyrics for transposition
- Font size computed at upload time against fixed iPad Air horizontal viewport reference

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

<details>
<summary>[x] 26. Flatten song detail header</summary>

- Title card flatter and spread horizontally; song content area taller

</details>

<details>
<summary>[x] 27. Song upload on own page</summary>

- Upload song form on its own page; upload button remains on songs list page

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

<details>
<summary>[x] 38. Bold chords + hover chord diagram</summary>

- Chord names bold in sheet (NC excluded)
- Hover popover shows chord as ASCII tab using existing position pipeline
- Chord qualities defined as static interval sets in ChordService (~14 qualities); no DB storage
- Voicings derived on-the-fly via LoserBracketPositionBuilder; module-level cache prevents re-fetching
- Multiple voicings navigable with ‹ N/M › pagination in popover; unknown chords show `???`

</details>

<details>
<summary>[x] 41. Slash chord display fix (G/B hover diagram)</summary>

- add slash chords to chord db. treat them separate in frontend when transposing, for simplicity. but treat them as one chord when clicking to view chord
- ridealong fix: parentheses shouldnt be bold

</details>

<details>
<summary>[x] 43. Chord voicing improvements (real voicings, visual display)</summary>

- ChordQuality and ChordShape JPA entities; 70 seed rows (5 CAGED shapes × 14 qualities)
- ChordShapeSeed ApplicationRunner seeds on first startup (idempotent)
- transposeShape: offsets fretted values so root lands on correct fret; muted ("x") and stay-open (-1) values unchanged
- formatShape: renders int[] to ASCII tab matching Position.toTabString() output format
- GET /api/chord?instrument=GUITAR returns real CAGED fingerings; other instruments return empty list
- Visual grid display deferred; user-submitted voicings deferred to idea 47

</details>

<details>
<summary>[x] 44. Update frontend and backend READMEs</summary>

- Update README on frontend and backend to reflect new feature set

</details>

<details>
<summary>[x] 47. allow user to upload missing voicings for chords</summary>

- if chord displays as ??? in song page, then clicking on ??? will open the modal for adding a new chord.
- use existing add chord voicing modal. hardcode the chord being updated as chord name, and disable text entry in that field.
- submit button will add voicing to db and the song will pull the new voicing.
- user submitted voicings get prioritized in chord voicing list, and are displayed first on hover.
</details>

<details>
<summary>[x] 50. update chord voicings modal on front end</summary>

- sort chord voicings by lowest fret, so open and common voicings are first in list
- the arrows should loop, so back on first voicing will take you to last voicing etc
</details>

<details>
<summary>[x] 51. add "chords\ngallery" tab to navbar</summary>

- chord page lets user select key and shows all voicings of all chords of all quality for that root note
- display chord and voicing modals in a grid (similar to the hover modal in song, which lets you cycle voicings)
</details>

<details>
<summary>[x] 53. refactor backend using Domain-Driven Design philosophy</summary>

- this will segregate the different verticals in the app, reducing tokens and context
- instead of searching the full repo, first just use the readme.md and claude.md
</details>

<details>
<summary>[x] 54. refactor frontend using Feature-Sliced Design philosophy</summary>

- this will segregate the different verticals in the app, reducing tokens and context
</details>

<details>
<summary>[x] 58. upload chords page</summary>

- add page with form for user to upload a new chord voicing.
- should seamlessly support new voicing for existing chord, and new chord altogether
- you get here by clicking add chord in Chord Gallery page
- this form will also be used as a modal elsewhere so prep it for that
</details>

<details>
<summary>[x] 66. chord voicing upload ui input</summary>

- on upload chord, frontend only, can we make the chordname field up just be a text field? then we can parse the root and quality from that, and allow nonsense to give a little error message. i think this will eliminate need for the shapename text field below.
- also give an error for voicing already exists. a future feature will use this modal, so we need to program in ability for text field to be off limits if flagged
</details>

<details>
<summary>[x] 60. bug with capo and transpose tool</summary>

- changing capo number seems to update the wrong note value, the one labeled shape is updating, but it should be changing sound
</details>

<details>
<summary>[x] 39. Capo reset button</summary>

- Reset button for capo (parallel to transpose reset)

</details>


</details>


<details open>
<summary>Pending</summary>

<details>
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

<details>
<summary>[ ] 12. Upload instrument selection</summary>

- Allow user to change input instrument for tab upload
- parseTab + toIntervals currently always use Guitar.STANDARD
- Would potentially remove need for TabNote if parseTab is retooled for any tuning/instrument

</details>

<details>
<summary>[ ] 19. Animated neck visualization</summary>

- Playhead-driven neck display; notes flash on active fret/string at tempo
- Playhead indexes into columnIndex sequence at rate derived from BPM; pairs with metronome
- Instrument-aware: render string count and layout from Instrument interface
- Scale backdrop (#8) overlays dim dots for valid mode positions in current fret window

</details>

<details>
<summary>[ ] 21. Scale overlay + mic input page</summary>

- Separate navbar page combining neck display and mic input
- Select key and mode; overlay scale positions as dim dots on the neck
- Mic input identifies pitch live and highlights corresponding note on neck
- Depends on #18 (mic pitch detection) and #19 (neck visualization)

</details>

<details>
<summary>[ ] 25. Chord parser gaps (font recompute + boundary)</summary>

- Long line auto-break: chord/lyric pair too long → split into two halves; enforce same length via trailing spaces
- Default behavior: shrink font, but enforce minimum readable size and max line length; break at last word before limit
- After splitting, strip leading spaces from both strings in second ChordLyric symmetrically
- Err on shorter lines during breaking without splitting a chord or a word

</details>

<details>
<summary>[ ] 28. Chord sheet parser gaps (font + boundary)</summary>

- Gap 1 — font size not recomputed after line breaking: after breaking over-long pairs, recompute globalFontSize across full resulting list; halves are shorter so minimum will be larger → more readable
- Fix: two-pass in ChordSheetParser.applyFontSizes — first break all over-long pairs, recompute globalFontSize, then apply to every non-spacer pair
- Gap 2 — chord boundary not checked during break: if split index falls mid-token in chord string (e.g. "G/B"), chord gets silently truncated
- Fix: in ChordSheetParser.breakLine(), after finding lyrics word boundary, walk back while chords.charAt(breakAt - 1) != ' '; take the more conservative (shorter) of lyrics and chord boundaries

</details>

<details>
<summary>[ ] 31. Delete confirmation dialog</summary>

- Warning/confirmation box before song deletion

</details>

<details>
<summary>[ ] 37. Metronome time signature options</summary>

- Options for single tone (no accent) and 3/4 time

</details>

<details>
<summary>[ ] 40. Global playlists</summary>

- Playlist tab in navbar; list shows playlist name, song count, creator
- Clicking song from playlist shows "back to playlist" button above song name (hyperlinked)
- Songs in playlist show Next → / ← Back for sequential navigation
- Per-song key/capo override stored in playlist; overrides song default

</details>

<details>
<summary>[ ] 42. Add "Show Chords" button on song page</summary>

- Button on song title bar; on click shows list of all chords in the song

</details>

<details>
<summary>[ ] 45. song metadata update form</summary>

- button to page with form to submit an update request to song details. allow user to submit revision to song attributes.
  - Label: "Manage" or some icon
    - manage icon is clickable in song card from list, and on song toolbar
  - on Manage song page, allow user to submit changes to tempo, artist, songname, key. 
    - submit button will become clickable if user modifies any above field (these text fields are prepopulated) and submission updates values in db
    - Update Song Chart button exposes big text field to edit the raw guitartab input. 
      - the song attribute fields hide to make room for this field. there is a back button to restore them. 
      - submit button is greyed unless there is change
      - clicking submit updates db for this song. triggers reparse.
    - Manage Chords button 
      - this will hide other fields, expose back button to return. 
      - manage chords will list all detected chords for song, and allow user to select that chord and add a voicing for it, using modal from the ??? feature. these voicings will have source:user
        - since we're using the existing modal, chord updates are submitted per chord, rather than a batch.
    - Delete Song button
      - pop up confirmation dialogue "Are you sure? This action cannot be undone."
      - remove delete button from song list itself
</details>

<details>
<summary>[ ] 46. add ability to include tab snippets in chord sheets</summary>

- add GuitarTabLine as object in chordsheet, since some chordsheets include riffs. these can have chord labels above, or not. but will be like 6 lines and we already know how to detect.
</details>

<details>
<summary>[ ] 48. change chord display from ASCII to pretty diagram</summary>

- chords should be shown as an image rather than ascii
- perhaps use js library: svguitar
</details>

<details>
<summary>[ ] 52. add Home page with features clickable in page body</summary>

- clicking Lick Library in navbar should take user to home page, where the different navbar options are displayed in more detail in the page body
</details>

<details>
<summary>[ ] 55. add alternate chordsheet view that doubles the font size, and displays as a single scrolling column</summary>

- user triggers scrolling with a button.
- perhaps song should get artifact of list of timestamps for chorus verse etc, and scrolling will jump to these points after a timer instead of scrolling slowly the whole time
- maybe a steady scroll option too, incase auto is whacky.
</details>

<details>
<summary>[ ] 57. handle |,-,* chars in chordline</summary>

- | G#m7  G#m | Gm7 | is not parsed as chords in a tab, so we need to fix that.
- (G)              C - G/B - Am - G isn't recognized as a chordline
  - discuss: should i just not allow hyphen in chordline
-      D (or Bm)    E                A       A 
  - should this be allowed?
- E                            E                E              A (coda riff)
  - should this be allowed? how can we handle this without defining chordlines explicitly.
  - chordsheet upload is pretty simple and it should stay that way.
- D F#7 G Gm**
  - allow asterisks in chordlines, but don't parse or bold them
- should words be allowed just in paren in a chordline?
  - sometimes lyrics have parentheses too
  - sometimes chords are in paren
  - any character in a chordline that isnt part of a chord should be non-bold and black font

</details>

<details>
<summary>[ ] 59. Songs Library enhancement</summary>

- make list sortable and filterable.
  - alphabetical by artist, by song
  - filter by artist
  - sort by key
- pagination
</details>

<details>
<summary>[ ] 62. fix display for long chord sheets</summary>

- entry for vampire doesn't fit onscreen properly.
- update parsing to allow for more columns, smaller font
-
</details>

<details>
<summary>[ ] 63. redesign song card in Song Library</summary>

- maybe a square card, let song name have line break if needed, shrink font if really big
- artist still small and gray underneath. still show song key, maybe add song tempo.
- x button remains but should have confirmation dialogue instead of just deleting
- organize square cards in a grid
</details>

<details>
<summary>[ ] 64. chord gallery enhancement- manage/delete voicings</summary>

- there will need to be a way to delete voicings, in chord gallery page.
- how does this handle deleting voicings that were added by system? should it allow this?
-
</details>

<details>
<summary>[ ] 65. chord voicing upload ui display</summary>

- add pretty chord display right next to the fret input, showing how voicing will display on page once added
</details>

<details>
<summary>[ ] 67. Theory tab in navbar</summary>

- this will take user to a page that holds all the circle of fifths, caged, scales, live input stuff 
- initial implementation will be a stub page that has links on home and navbar
</details>

<details>
<summary>[ ] 68. stub out additional details for song page</summary>

- Manage: page to update song metadata
- Tuning: just display the tuning for the song near where the bpm is?
  - let user change tuning to half step down ?
- View: user can toggle multi-column view or scrolling view
- Show Chords: displays all chords and voicings for (current key of) song
</details>


</details>

<details>
<summary>Deferred</summary>

<details>
<summary>[~] 5. Multi-measure phrase segmentation</summary>

- Long phrases split at `|` boundaries before running position builders
- parseTab currently strips `|` — would need to emit measure-break markers during parsing
- Segment interval list, run findPositions per segment; would improve quality on multi-bar phrases

</details>

<details>
<summary>[~] 9. iPad PWA (home screen shortcut)</summary>

- Pure frontend config: manifest.json, apple-touch-icon, viewport meta, display: standalone
- Vite PWA plugin (vite-plugin-pwa) handles most boilerplate
- No backend touches needed
- Target: 2025 iPad Air

</details>

<details>
<summary>[~] 10. Auth & security hardening</summary>

- JWT auth, Spring Security, CORS policy, rate limiting, user-scoped repositories
- H2 → Postgres migration required
- Worth doing last if at all; not prioritized over functional features

</details>

<details>
<summary>[~] 11. Parallel harmony generator</summary>

- Static phrase transformation in interval-space
- Input: List\<IntervalNote\>, Mode, harmonicInterval (e.g. THIRD, SIXTH)
- Map each note to its diatonic equivalent N scale degrees up within the mode's interval sequence
- Snap-to-scale flag for chromatic/passing tones
- Lives in LickUtils as a stateless pure function alongside toIntervals and detectMode

</details>

<details>
<summary>[~] 13. Containerize and deploy</summary>

- Containerize frontend and backend
- Deploy to a hosting service

</details>

<details>
<summary>[~] 15. Phrase predictor</summary>

- Deterministic rule engine; no AI/probabilistic components
- Input: key, Mode, chord progression, optional seed phrase
- Rules: chord tones on strong beats → stepwise voice leading → tension/resolution → passing tones on weak beats
- Output: List\<IntervalNote\> fed through existing position pipeline
- Guitar.STANDARD may attach idiomatic jump hint table (see #20) as weighted candidates

</details>

<details>
<summary>[~] 17. Octave tracking</summary>

- Derive absolute pitch from fret + string using open string offsets + semitones per fret
- No string gauge attribute needed — gauge affects tone not pitch
- Each Instrument provides open string absolute pitches alongside existing tuning()
- Add octave as a derived field on TabNote or wrapper, computed at render time

</details>

<details>
<summary>[~] 18. Microphone pitch detection</summary>

- YIN algorithm or pitchfinder.js for single-note pitch detection
- Limit to Guitar.STANDARD + clean tone input for accuracy
- On note detected: resolve to nearest Note enum, display all neck positions via findNeckPositions
- Separate Fretboard Explorer mode; tab-following (timing sync) is a later phase
- Known constraint: distortion and low notes degrade accuracy

</details>

<details>
<summary>[~] 20. CAGED idiomatic jump hints</summary>

- Guitar.STANDARD-specific static structure: list of (intervalDelta, stringsTraversed) pairs
- Consumed by predictor (#15) as weighted hint table for interval selection
- Does not affect position finding geometry — only influences which interval to target
- Other instruments may define their own tables on their own classes; no shared interface contract

</details>

<details>
<summary>[~] 32. Song recycle bin</summary>

- Deleted songs go to a recycle DB where they can be recovered

</details>

<details>
<summary>[~] 33. Users, auth, playlists</summary>

- Account creation with admin-gated 2FA
- Users can upload songs, view songs, make and view playlists
- Users can only delete their own songs; admin can delete anything

</details>

<details>
<summary>[~] 49. create cache db of chord voicings to avoid need for rerunning chord calculations</summary>

- this feature is heavy if no users, but if we scale, it will be beneficial to reduce overhead
</details>

<details>
<summary>[~] 56. chord page enhancement - progressions</summary>

- chord page could also have option to show user chord progressions for certain keys. based off of interval, circle of fifths kinda stuff, mode, etc
</details>

<details>
<summary>[~] 61. super edge case: songs with bpm changes</summary>

- update bpm display for song to list multiple clickable bpms
  - the way to actually add mutliple bpms will be only exposed in song metadata update card (idea 45)
- consider bpm in autoscroll?
- make bpm refs in song itself clickable and they update metronome?
</details>


</details>


<details>
<summary>[ ] 50. </summary>

- 
</details>