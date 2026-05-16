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

<details>
<summary>[x] 45. song metadata update form</summary>

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
<summary>[x] 69. fix capo/transpose widget again</summary>

- case: capo 2, key A, chords start with G -> capo:2,shape:G,sound:A
  - if capo is moved down by 1 -> capo:1,shape:G,sound:G# (chords unchanged)
  - if capo is moved up by 1 -> capo:3,shape:G,sound:A (chords unchanged)
  - if transpose +1 -> capo:2,shape:G#,sound:Bb (chords transposed +1)
  - if transpose -1 -> capo:2,shape:F#,sound:G# (chords transposed -1)
- currently, shape is getting adjusted by default capo, and it shouldn't
- changing transpose should not reset the capo.
</details>

<details>
<summary>[x] 70. chord upload for ??? is failing for G/F#</summary>

- is this because we have not defined a G with quality /7 ?
- chord upload for existing chords works (tested with adding missing B7 voicing)
- other uncommon qualities are causing errors as well
</details>

<details>
<summary>[x] 31. Delete confirmation dialog</summary>

- Warning/confirmation box before song deletion

</details>

<details>
<summary>[x] 25. Chord parser gaps (font recompute + boundary)</summary>

- Long line auto-break: chord/lyric pair too long → split into two halves; enforce same length via trailing spaces
- Default behavior: shrink font, but enforce minimum readable size and max line length; break at last word before limit
- After splitting, strip leading spaces from both strings in second ChordLyric symmetrically
- Err on shorter lines during breaking without splitting a chord or a word

</details>

<details>
<summary>[x] 28. Chord sheet parser gaps (font + boundary)</summary>

- Gap 1 — font size not recomputed after line breaking: after breaking over-long pairs, recompute globalFontSize across full resulting list; halves are shorter so minimum will be larger → more readable
- Fix: two-pass in ChordSheetParser.applyFontSizes — first break all over-long pairs, recompute globalFontSize, then apply to every non-spacer pair
- Gap 2 — chord boundary not checked during break: if split index falls mid-token in chord string (e.g. "G/B"), chord gets silently truncated
- Fix: in ChordSheetParser.breakLine(), after finding lyrics word boundary, walk back while chords.charAt(breakAt - 1) != ' '; take the more conservative (shorter) of lyrics and chord boundaries

</details>

<details>
<summary>[x] 57. handle |,-,* chars in chordline</summary>

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
<summary>[x] 62. fix display for long chord sheets</summary>

- entry for vampire doesn't fit onscreen properly.
- update parsing to allow for more columns, smaller font
-
</details>

<details>
<summary>[x] 73. song parser needs another pass</summary>

- I am observing a relatively short song get parsed over-eagerly.
  - expectation: fewer, longer columns
  - current: song is shrunk to fit 4 columns, but each column is so short, that it covers only a third of the length of the page
- the point of the parser is to make the biggest font, and the fewest columns, while still fitting the whole song on one page without scrolling
</details>

<details>
<summary>[x] 48. change chord display from ASCII to pretty diagram</summary>

- chords should be shown as an image rather than ascii
- perhaps use js library: svguitar
</details>

<details>
<summary>[x] 65. chord voicing upload ui display</summary>

- add pretty chord display right next to the fret input, showing how voicing will display on page once added
</details>

<details>
<summary>[x] 64. chord gallery enhancement- manage/delete voicings</summary>

- add Manage button/icon to top of Chord Gallery page
- when you click manage, if you click on a chord box, it will show all voicings in a grid, and they will have X on top right of box, and clicking will trigger confirmation prompt. if confirmed, it will delete
- how does this handle deleting voicings that were added by system? should it allow this?
  - some default voicings are weird
- chord gallery should pull all voicings for chord, including custom and / chords if they exist.
</details>

<details>
<summary>[x] 76. reseed shouldn't fill out slash chords since its just guessing them</summary>

- i don't want reseed to fill up with inaccurate chord charts.
</details>


<details>
<summary>[x] 77. chord gallery ->manage->manage voicings enhancement</summary>

- update display so the x is on right side of the voicing diagram
- the delete yes/no dialogue should be on right side of voicing diagram. delete should be above the confirm button
</details>

<details>
<summary>[x] 78. chord gallery - slash chords are displayed wrong</summary>

- currently slash chords are displayed like G/11 when the number after the slash actually represents a note relative to the root. G/11 should display as G/F# etc.
- reseeder shouldn't bother with slash chord generation.
- ensure chord voicing upload flow and reseeder flow account for duplicate chord voicings
- chord gallery should pull existing slash chords and display them correctly
</details>

<details>
<summary>[x] 68. stub out additional details for song page</summary>

- Manage: page to update song metadata (right align) (pencil icon like in songlist, maybe a little bigger)
- Tuning: just display the tuning for the song near where the bpm is displayed (default show standard or EADGBe)
- View button: user can toggle multi-column view or scrolling view (stub for now) (hovering button explains use)
- Show Chords button: displays all chords and voicings for (current key of) song (stub for now)
  - display the hover modals in a row on bottom of page
</details>

<details>
<summary>[x] 42. Add "Show Chords" button on song page</summary>

- Button on song title bar; on click shows list of all chords in the song

</details>

<details>
<summary>[x] 81. </summary>

- the x button in manage voicings in chord gallery by each voicing should be 5 times bigger and centered next to the chart. the delete confirm dialogue should be a single button    
  containing "delete?" instead of what is currently there
</details>

<details>
<summary>[x] 71. enhancement for manage page</summary>

- Manage Chords page in song flow should show the chords relative to the current transposition
- should show the chord chart display, like the one used in show chords.
- retain add voicing button (hover label but use icon + ) (chord name is top center aligned, add icon is top right aligned)
</details>

<details>
<summary>[x] 59. Songs Library enhancement</summary>

- make list sortable and filterable.
  - alphabetical by artist, by song
  - filter by artist
  - sort by key
- pagination
</details>

<details>
<summary>[x] 63. redesign song card in Song Library</summary>

-  a square card, let song name have line break if needed, shrink font if really big
- artist still small and gray underneath. still show song key, add song tempo. pencil icon but bigger
- organize square cards in a grid
</details>

<details>
<summary>[x] 74. parser pass</summary>

- the current parser is great, just a few notes
- allow % as ignored character in chordline
- "    Ahh " is being detected as a chordline erroneously
- perhaps short songs should still be 2 columns, this helps with show chords
</details>

<details>
<summary>[x] 75. Manage Song API bug</summary>

- when i update song metadata, and swap the artist and song name, i get error: Update failed
</details>

<details>
<summary>[x] 83. song library manage</summary>

- change reparse button to Manage
- when manage is selected, the reparse button (now labeled reparse <icon> ) and the manage button become masive in song cards
- hide manage button otherwise
- when manage is on, then you cant misclick to song page
- when manage is off, key and bpm are larger
</details>

<details>
<summary>[x] 82. fix chord hover diagram getting cut off at bottom of song</summary>

- move it up in such cases or something
</details>

<details>
<summary>[x] 55. implement scrolling view in song display</summary>

- add alternate chordsheet view that doubles the font size, and displays as a single scrolling column
- user triggers scrolling with a button.
- perhaps song should get artifact of list of timestamps for chorus verse etc, and scrolling will jump to these points after a timer instead of scrolling slowly the whole time
- maybe a steady scroll option too, incase auto is whacky.
</details>

<details>
<summary>[x] 84. enhance scrolling view for song display</summary>

- for both column and scrolling view, allow a little padding at bottom, so text isnt right up on screen edge
  - there is room to accomplish this by reducing space between song title display and song body
- in scrolling view center the box holding the song (do not center align text)
- restore the button's original design with view: above columns.
  - button should not change size when toggled
</details>

<details>
<summary>[x] 52. add Home page with features clickable in page body</summary>

- clicking Lick Library in navbar should take user to home page, where the different navbar options are displayed in more detail in the page body
</details>

<details>
<summary>[x] 85. ipad web ui</summary>

- when saved as webapp on homescreen on ipad, hide addressbar,
</details>

<details>
<summary>[x] 86. stub out planned feature verticals </summary>

- in navbar and home screen, add links to the following
  - Playlists: empty page, but in a new vertical slice so new directory
  - Theory: empty page in new slice ..
  - Live: empty page to be used for play along with mic input and a guitar neck visual
- just stub the empty pages, add text field describing whats to come, for each page stub
</details>

<details>
<summary>[x] 46. add ability to include tab snippets in chord sheets</summary>

- add GuitarTabLine as object in chordsheet, since some chordsheets include riffs. these can have chord labels above, or not. but will be like 6 lines and we already know how to detect.
</details>

<details>
<summary>[x] 8. Scale / CAGED neck learning tool</summary>

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
<summary>[x] 19. Animated neck visualization</summary>

- Playhead-driven neck display; notes flash on active fret/string at tempo
- Playhead indexes into columnIndex sequence at rate derived from BPM; pairs with metronome
- Instrument-aware: render string count and layout from Instrument interface
- Scale backdrop (#8) overlays dim dots for valid mode positions in current fret window

</details>

<details>
<summary>[x] 21. Scale overlay + mic input page</summary>

- Separate navbar page combining neck display and mic input
- Select key and mode; overlay scale positions as dim dots on the neck
- Mic input identifies pitch live and highlights corresponding note on neck
- Depends on #18 (mic pitch detection) and #19 (neck visualization)

</details>

<details>
<summary>[x] 89. live neck enhancement</summary>

- darken strings
- maybe tan rectangle under strings display to give more guitar appearance.
- when a note is selected, the pale bright yellow outline should be a thin
  - note name should be bigger, slightly bigger when unselected, even bigger when selected
- when live page is loaded, default to C Major perhaps instead of empty.
</details>

<details>
<summary>[x] 90. live enhance</summary>

- need to incorporate next possible note.
- we can test without using mic input
- clicking to select a note in the display should be treated as "note being played"
  - only one note allowed to be selected at a time in this state
- next possible note should use the caged intervals and jumps
- is this where we incorporate the fun caged relational stuff or is that theory?
  - for example G diatonic is composed of 3 pentatonic shapes overlaid: G C D
- if the user only knows the scale as major, but they are playing a different scale, perhaps some way of converting or showing 6=1 or something, some way to understand the scale mode relations
</details>

<details>
<summary>[x] 94. live tab enhance - note picker</summary>

- next note is a little zealous.
  - distance from note at fret/string should account for diagonal distance,
  - fewer notes should light up. no distance greater than 4.000.
- add a key up to the title card row of live tab. list the intervals relevant to the mode and put each number in a bubble matching their color (red is root).
  - when a currentNote is active, that interval should be highlighted same in the key.
  - when next notes are flashing, those intervals should be highlighted same in the key

-
</details>


</details>


<details open>
<summary>Pending</summary>

<details>
<summary>[ ] 12. Upload instrument selection</summary>

- Allow user to change input instrument for tab upload
- parseTab + toIntervals currently always use Guitar.STANDARD
- Would potentially remove need for TabNote if parseTab is retooled for any tuning/instrument

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
<summary>[ ] 67. Theory tab in navbar</summary>

- this will take user to a page that holds all the circle of fifths, caged, scales, live input stuff
</details>

<details>
<summary>[ ] 72. Multi-Mode Key Support</summary>

- currently all song keys are assumed to be Major. need to add support for minor key etc.
- i'm a nerd for relative minor relative major so maybe that gets considered later on in Music Theory section
</details>

<details>
<summary>[ ] 87. chord hover modal needs to make sure its not going off screen in any direction</summary>

-
</details>

<details>
<summary>[ ] 88. theres still a little bit of space at the top of the song title toolbar that scrolls when in scroll mode, even though its supposed to be frozen </summary>

-
</details>

<details>
<summary>[ ] 91. make app HTTPS</summary>

- mic doesn't work without https. can we make this app secure
</details>

<details>
<summary>[ ] 92. iphone/vertical layout update</summary>

- app is geared for landscape ipad
- on portrait mode, we should display song list as list instead of grid
- on portrait mode, i see that the lines get cut off in scrolling song mode, the lines are cut short
  - portrait ipad cuts lines a little
  - portrait iphone cuts lines significantly, leaving right 3rd of screen blank.
- maybe for all layouts, in song display, artist should still be small and grey, but it should be moved above song title
</details>

<details>
<summary>[ ] 93. dynamic fret display idea</summary>

- in live tab, when the being played is up the neck, the diagram should shift to center currently played note in the diagram, revealing frets further up the neck, and perhaps hiding the first few frets, 
  - at least 12 frets should show onscreen
- maybe frets shouldn't show uniform, but vary in size up the neck like real guitars
</details>

<details>
<summary>[ ] 95. theory tab content</summary>

- perhaps a compare modes tool, to show root on ionian is x on dorian etc.
-  theory tab shouldn't have mic input
</details>

<details>
<summary>[ ] 96. live neck visual polish (from 89)</summary>

- Darken string lines: `#6b7280` → `#374151`
- Add warm tan/wood `<rect>` behind string lines as fretboard surface background
- Active note ring: change from filled pale-yellow circle to stroke-only ring (fill="none", stroke, strokeWidth)
- Larger note name labels: unselected 1-char 7→9px, 2-char 5.5→7px; active one step larger again
- **File:** `src/features/live/GuitarNeck.tsx`
</details>

<details>
<summary>[ ] 97. CAGED-based next-note suggestions for Live tab (from 90)</summary>

- Replace Euclidean distance filter with CAGED positional logic
- Define 5 CAGED boxes as fret-range windows relative to the root (~4 frets wide, ~1-fret overlap)
- When note is selected, identify its CAGED box; candidates come from that box and adjacent boxes only
- Naturally constrains next-note highlights to playable hand positions
- **File:** `src/features/live/LivePage.tsx`
</details>

<details>
<summary>[ ] 98. mode relationship display for Theory tab (from 90)</summary>

- Show how each mode relates to its parent Ionian key, e.g. "A Aeolian = relative minor of C Major (6th degree)"
- User picks root + mode; page shows parent key and degree position
- Mapping: IONIAN=1, DORIAN=2, PHRYGIAN=3, LYDIAN=4, MIXOLYDIAN=5, AEOLIAN=6, LOCRIAN=7
- **File:** `src/features/theory/TheoryPage.tsx`
</details>

<details>
<summary>[ ] 99. CAGED position zones on neck for Theory tab (from 8)</summary>

- Overlay 5 CAGED boxes as labeled shaded regions on a static GuitarNeck
- Semi-transparent rect per box spanning its fret range × all 6 strings, labeled C/A/G/E/D
- User picks root key; boxes shift to correct fret positions
- **Files:** `src/features/theory/TheoryPage.tsx`, reuse `GuitarNeck`
</details>

<details>
<summary>[ ] 100. pentatonic-within-diatonic overlay for Theory tab (from 8)</summary>

- Any diatonic CAGED box = three overlapping pentatonic shapes (e.g. G Ionian = G + C + D pentatonic)
- Color-code or outline the three pentatonic subsets within the 7-note scale overlay
- Implement after CAGED zones (idea 100) are working
- **File:** `src/features/theory/TheoryPage.tsx`
</details>

<details>
<summary>[ ] 101. animated lick playback on neck for Lick detail (from 19)</summary>

- Add GuitarNeck below the tab on LickDetailPage; feed it computed positions for selected key
- Playhead advances through columnIndex sequence at BPM from MetronomeContext
- Active column's dots light up; play/pause button; loops
- Depends on position data from `GET /api/lick/{id}` and existing MetronomeContext
- **File:** `src/features/lick/LickDetailPage.tsx`, reuse `GuitarNeck`
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

<details>
<summary>[~] 79. are slash chords overengineered?</summary>

- is there any point to having the middle layer of interpretting slash chords as /11 or /4 instead of /B or whatever matches in the key?
- i feel like we can just save them in the db directly as /B or whatever is there no?
- if i'm wrong, i want to discuss, don't just implement
- we should only implement if the end result cuts down on code volume significantly
</details>

<details>
<summary>[~] 80. little view mode</summary>

- add button that makes song title card a lil smaller and moves all the controls up to the navbar
- add hidden song controls card to navbar that allows this
</details>


</details>


<details>
<summary>[ ] 102. </summary>

- 
</details>