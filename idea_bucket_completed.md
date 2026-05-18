The current state of the app is a react vite frontend and a java spring boot backend. you input a guitar tab string, and it is converted into a collection of note objects and then gets processed into a list of output tab strings in the chosen key and mode. the unique licks get stored in a h2 db.

The purpose of this file is to hold ideas in memory so we can easily refer to them in claude code ui.

key: [ ]  = open, [x] = complete, [~] = deferred
---

<details>
<summary>Completed</summary>

<details>
<summary>Licks Feature</summary>
<details>
<summary>[x] 1. (Licks) Position algorithm selector</summary>

- Strategy pattern with abstract PositionBuilder; greedy default, DFS optional
- `?algo=` param on GET /api/lick/{id}; frontend toggle on detail page
- Implemented with an abstract class/method because for each algo, the input and output types will be the same
- findPositions, buildPositions, dfsPositions, and findCandidates extracted from LickService into PositionBuilder strategy classes

</details>

<details>
<summary>[x] 2. (Licks) Multi-instrument support</summary>

- Instrument interface with tuning (Note[]), labels, displayOrder, stringCount, getNoteAt, minFret
- Implemented: Guitar (STANDARD, DROP_D, OPEN_G, OPEN_D, DADGAD), Bass, Ukulele, Mandolin, Banjo
- Banjo 5th string starts at fret 5 (minFret TODO)
- Frontend instrument selector on detail page; all position logic calls through Instrument interface

</details>

<details>
<summary>[x] 3. (Licks) Loser-bracket chord-aware algorithm</summary>

- Two-pass greedy: pass 1 places one note per unique columnIndex (melodic line); pass 2 places chord partners on a different string near the parallel note
- Directly addresses the known limitation where buildPosition broke for shared columnIndex (simultaneous) notes
- Slots in as algorithm 3 under the PositionBuilder abstraction

</details>

<details>
<summary>[x] 6. (Licks) Tab grid auto-expand at boundary</summary>

- Detect cursor sitting on a closing `|`, insert `-` before each line's closing `|` on all 6 lines simultaneously
- Recalculate cursor position after expansion
- Paste is safe — onChange fires for paste and bypasses handleKeyDown entirely

</details>

<details>
<summary>[x] 7. (Licks) Two-digit fret parsing</summary>

- Lookahead in parseTab: when a digit is found at position j, check if j+1 is also a digit, combine and skip j+1
- Handles frets 0–99

</details>

<details>
<summary>[x] 101. (Licks) animated lick playback on neck for Lick detail (from 19)</summary>

- Add GuitarNeck below the tab on LickDetailPage; feed it computed positions for selected key
- Playhead advances through columnIndex sequence at BPM from MetronomeContext
- Active column's dots light up; play/pause button; loops
- Depends on position data from `GET /api/lick/{id}` and existing MetronomeContext
- **File:** `src/features/lick/LickDetailPage.tsx`, reuse `GuitarNeck`
</details>


</details>


<details>
<summary>Songs Feature</summary>
<details>
<summary>[x] 4. (Songs) Song chord sheet with transposition</summary>

- ChordLyric objects: string chords, string lyrics, double fontSize
- Song DB: title, artist, key, tempo, capo, ordered list of ChordLyric
- One-pager: 2–3 columns, font auto-sized to fill screen without line breaks, pairs share font size
- Upload flow: txt intake + metadata → backend processing → stored as ChordLyric list
- Transposition updates chord lines in the displayed song object

</details>

<details>
<summary>[x] 16. (Songs) One-pager song chord sheet display</summary>

- Monospaced font (Roboto/Courier New); 2–3 columns depending on song size
- ChordLyric pairs: chord row + lyric row share font size; auto-shrink to prevent line breaks
- Song list page showing artist + song name; upload button → upload song page
- Processing happens in backend on upload; chord lines stored separate from lyrics for transposition
- Font size computed at upload time against fixed iPad Air horizontal viewport reference

</details>


<details>
<summary>[x] 24. (Songs) Song transposition</summary>

- Transpose up/down via semitone counter; updates chord lines in displayed song
- Accounts for sharps/flats added/removed; minimum 1-space gap between chords maintained
- Slash chords (G/B): both roots transposed independently, rejoined with "/"
- Length delta compensation: if sharp is added, insert space in lyrics string at same index

</details>

<details>
<summary>[x] 26. (Songs) Flatten song detail header</summary>

- Title card flatter and spread horizontally; song content area taller

</details>

<details>
<summary>[x] 27. (Songs) Song upload on own page</summary>

- Upload song form on its own page; upload button remains on songs list page

</details>

<details>
<summary>[x] 29. (Songs) Capo-aware transpose widget</summary>

- Capo group on left, Transpose group on right of song header
- Capo adjustable via widget; transpose shows semitone delta from original key
- Key of B in capo 4 shows leading chord as G
- Reset button below transpose row (hidden at semitones=0)

</details>

<details>
<summary>[x] 30. (Songs) Re-parse button</summary>

- Button on song detail page triggers rerun of chord sheet parsing logic for existing songs
- Purpose: update songs after song parser logic is updated

</details>

<details>
<summary>[x] 34. (Songs) Semitone wrap to zero at octave</summary>

- Transpose counter wraps back to 0 when cycling through a full octave (±12)

</details>

<details>
<summary>[x] 35. (Songs) No layout shift on transpose</summary>

- Chord sheet fades to 50% opacity during transpose fetch; no element added/removed from DOM
- "Transposing…" placeholder element removed entirely

</details>

<details>
<summary>[x] 36. (Songs) BPM click starts metronome</summary>

- Clicking BPM displayed under song name starts the metronome at that tempo
- MetronomeContext shared between SongDetailPage and Metronome component

</details>

<details>
<summary>[x] 38. (Songs) Bold chords + hover chord diagram</summary>

- Chord names bold in sheet (NC excluded)
- Hover popover shows chord as ASCII tab using existing position pipeline
- Chord qualities defined as static interval sets in ChordService (~14 qualities); no DB storage
- Voicings derived on-the-fly via LoserBracketPositionBuilder; module-level cache prevents re-fetching
- Multiple voicings navigable with ‹ N/M › pagination in popover; unknown chords show `???`

</details>

<details>
<summary>[x] 41. (Songs) Slash chord display fix (G/B hover diagram)</summary>

- add slash chords to chord db. treat them separate in frontend when transposing, for simplicity. but treat them as one chord when clicking to view chord
- ridealong fix: parentheses shouldnt be bold

</details>

<details>
<summary>[x] 60. (Songs) bug with capo and transpose tool</summary>

- changing capo number seems to update the wrong note value, the one labeled shape is updating, but it should be changing sound
</details>

<details>
<summary>[x] 39. (Songs) Capo reset button</summary>

- Reset button for capo (parallel to transpose reset)

</details>

<details>
<summary>[x] 45. (Songs) song metadata update form</summary>

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
<summary>[x] 69. (Songs) fix capo/transpose widget again</summary>

- case: capo 2, key A, chords start with G -> capo:2,shape:G,sound:A
    - if capo is moved down by 1 -> capo:1,shape:G,sound:G# (chords unchanged)
    - if capo is moved up by 1 -> capo:3,shape:G,sound:A (chords unchanged)
    - if transpose +1 -> capo:2,shape:G#,sound:Bb (chords transposed +1)
    - if transpose -1 -> capo:2,shape:F#,sound:G# (chords transposed -1)
- currently, shape is getting adjusted by default capo, and it shouldn't
- changing transpose should not reset the capo.
</details>

<details>
<summary>[x] 31. (Songs) Delete confirmation dialog</summary>

- Warning/confirmation box before song deletion

</details>

<details>
<summary>[x] 25. (Songs) Chord parser gaps (font recompute + boundary)</summary>

- Long line auto-break: chord/lyric pair too long → split into two halves; enforce same length via trailing spaces
- Default behavior: shrink font, but enforce minimum readable size and max line length; break at last word before limit
- After splitting, strip leading spaces from both strings in second ChordLyric symmetrically
- Err on shorter lines during breaking without splitting a chord or a word

</details>

<details>
<summary>[x] 28. (Songs) Chord sheet parser gaps (font + boundary)</summary>

- Gap 1 — font size not recomputed after line breaking: after breaking over-long pairs, recompute globalFontSize across full resulting list; halves are shorter so minimum will be larger → more readable
- Fix: two-pass in ChordSheetParser.applyFontSizes — first break all over-long pairs, recompute globalFontSize, then apply to every non-spacer pair
- Gap 2 — chord boundary not checked during break: if split index falls mid-token in chord string (e.g. "G/B"), chord gets silently truncated
- Fix: in ChordSheetParser.breakLine(), after finding lyrics word boundary, walk back while chords.charAt(breakAt - 1) != ' '; take the more conservative (shorter) of lyrics and chord boundaries

</details>

<details>
<summary>[x] 57. (Songs) handle |,-,* chars in chordline</summary>

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
<summary>[x] 62. (Songs) fix display for long chord sheets</summary>

- entry for vampire doesn't fit onscreen properly.
- update parsing to allow for more columns, smaller font
-
</details>

<details>
<summary>[x] 73. (Songs) song parser needs another pass</summary>

- I am observing a relatively short song get parsed over-eagerly.
    - expectation: fewer, longer columns
    - current: song is shrunk to fit 4 columns, but each column is so short, that it covers only a third of the length of the page
- the point of the parser is to make the biggest font, and the fewest columns, while still fitting the whole song on one page without scrolling
</details>

<details>
<summary>[x] 68. (Songs) stub out additional details for song page</summary>

- Manage: page to update song metadata (right align) (pencil icon like in songlist, maybe a little bigger)
- Tuning: just display the tuning for the song near where the bpm is displayed (default show standard or EADGBe)
- View button: user can toggle multi-column view or scrolling view (stub for now) (hovering button explains use)
- Show Chords button: displays all chords and voicings for (current key of) song (stub for now)
    - display the hover modals in a row on bottom of page
</details>

<details>
<summary>[x] 42. (Songs) Add "Show Chords" button on song page</summary>

- Button on song title bar; on click shows list of all chords in the song

</details>

<details>
<summary>[x] 71. (Songs,Chords) enhancement for manage page</summary>

- Manage Chords page in song flow should show the chords relative to the current transposition
- should show the chord chart display, like the one used in show chords.
- retain add voicing button (hover label but use icon + ) (chord name is top center aligned, add icon is top right aligned)
</details>

<details>
<summary>[x] 59. (Songs) Songs Library enhancement</summary>

- make list sortable and filterable.
    - alphabetical by artist, by song
    - filter by artist
    - sort by key
- pagination
</details>

<details>
<summary>[x] 63. (Songs) redesign song card in Song Library</summary>

-  a square card, let song name have line break if needed, shrink font if really big
- artist still small and gray underneath. still show song key, add song tempo. pencil icon but bigger
- organize square cards in a grid
</details>

<details>
<summary>[x] 74. (Songs) parser pass</summary>

- the current parser is great, just a few notes
- allow % as ignored character in chordline
- "    Ahh " is being detected as a chordline erroneously
- perhaps short songs should still be 2 columns, this helps with show chords
</details>

<details>
<summary>[x] 75. (Songs) Manage Song API bug</summary>

- when i update song metadata, and swap the artist and song name, i get error: Update failed
</details>

<details>
<summary>[x] 83. (Songs) song library manage</summary>

- change reparse button to Manage
- when manage is selected, the reparse button (now labeled reparse <icon> ) and the manage button become masive in song cards
- hide manage button otherwise
- when manage is on, then you cant misclick to song page
- when manage is off, key and bpm are larger
</details>

<details>
<summary>[x] 82. (Songs) fix chord hover diagram getting cut off at bottom of song</summary>

- move it up in such cases or something
</details>

<details>
<summary>[x] 55. (Songs) implement scrolling view in song display</summary>

- add alternate chordsheet view that doubles the font size, and displays as a single scrolling column
- user triggers scrolling with a button.
- perhaps song should get artifact of list of timestamps for chorus verse etc, and scrolling will jump to these points after a timer instead of scrolling slowly the whole time
- maybe a steady scroll option too, incase auto is whacky.
</details>

<details>
<summary>[x] 84. (Songs) enhance scrolling view for song display</summary>

- for both column and scrolling view, allow a little padding at bottom, so text isnt right up on screen edge
    - there is room to accomplish this by reducing space between song title display and song body
- in scrolling view center the box holding the song (do not center align text)
- restore the button's original design with view: above columns.
    - button should not change size when toggled
</details>

<details>
<summary>[x] 46. (Songs) add ability to include tab snippets in chord sheets</summary>

- add GuitarTabLine as object in chordsheet, since some chordsheets include riffs. these can have chord labels above, or not. but will be like 6 lines and we already know how to detect.
</details>

<details>
<summary>[x] 87. (Songs) chord modal display issue </summary>

- chord hover modal needs to make sure its not going off screen in any direction
</details>

<details>
<summary>[x] 88. (Songs) song page scroll tweak</summary>

- theres still a little bit of space at the top of the song title toolbar that scrolls when in scroll mode, even though its supposed to be frozen
</details>

<details>
<summary>[x] 72. (Songs) Multi-Mode Key Support</summary>

- currently all song keys are assumed to be Major. need to add support for minor key etc.
- i'm a nerd for relative minor relative major so maybe that gets considered later on in Music Theory section
</details>

<details>
<summary>[x] 92. (Songs) iphone/vertical layout update</summary>

- app is geared for landscape ipad
- on portrait mode, we should display song list as list instead of grid
- on portrait mode, i see that the lines get cut off in scrolling song mode, the lines are cut short
    - portrait ipad cuts lines a little
    - portrait iphone cuts lines significantly, leaving right 3rd of screen blank.
- maybe for all layouts, in song display, artist should still be small and grey, but it should be moved above song title
- need to fix the iphone/small screen ui. portrait and landscape
- on small screens the song display page too crowded, maybe hide options behind a hamburger if the screen is small enough to cause the song name to line break
- on iphone, the items on the navbar are missing. perhaps a hamburger?
</details>

<details>
<summary>[x] 80. (Songs) little view mode</summary>

- add button that makes song title card a lil smaller and moves all the controls up to the navbar
- add hidden song controls card to navbar that allows this
- add little ^ like icon to left of metronome display that shows on song detail page, has (collapse navbar) hover text and hides the controls and the navbar, leaving just a little navbar up top, showing song name, artist name, bpm, current key and capo, chevron, and metronome widget
- clicking chevron will restore normal display, returning the licklibrary navbar and the song title card
</details>

<details>
<summary>[x] 104. (Songs) small view next pass</summary>

- toggling small view shouldn't interrupt metronome
- show chords icon should live next to small view toggle when small view is active
- instead of iphone view always being hamburger,
    - if default named buttons don't fit, first replace with icon buttons (like iphone landscape),
    - if icons don't fit, use single hamburger button (iphone portrait)
- iphone portrait view scrolling mode is still cutting off right quarter of text on screen for song body
</details>

<details>
<summary>[x] 112. (Songs) pagination on song library</summary>

- song grid display: paginate at 16 instead of 18.
- song list display: also at 16
- add "Show All" button next to pagination that shows all without paginating.
    - when show all is active, the toggle is at bottom of list/grid where pagination was
</details>

<details>
<summary>[x] 105. (Songs,Licks) song transpose enhancement </summary>

- now that tablines are recognized in song parser and they are in tab format that is consumable by the licks parser
    - transposing song key should transpose the tabs as well using lick transpose logic.
- on song upload, or reparse, treat detected tabline blocks in songs as licks, add to lick library, but maybe with a flag to differentiate them from user licks.
    - loading song should load tab blocks as lick cards
        - transposing song should transpose the tab
        - arrows to cycle Positions for transposed
        - if lick is deleted from lick library song licks db,
            - then display tabline as usual in tab if song is not transposed
            - if song is transposed, just show error where tab was, but take up the same amount of space
    - prevent deletion by user.
        - song licks don't get delete buttons in the manage view, but user licks do
    - don't show these licks in the user submitted lick library
        - but add a toggle in the library view to show them in the list
- getting licks from a song should happen on parse and reparse. if a song has tabs/licks but has not been reparsed yet, then it should act like it doesn't have tabs detected.
  toggling experimental mode button on should trigger reparse if song has tablines but no licks yet. if song has no tablines, then turning experimental mode on should not fail or
  produce any licks. if song has licks and tablines, toggling experimental mode will replace regular tablines with the experimental ones
</details>

<details>
<summary>[x] 136. (Songs) Allow update capo in manage song details</summary>

- Allow update capo in manage song details
- make sure the key update field in the manage song details is updating the sound key value

</details>


</details>


<details>
<summary>Home Page and Nav Bar</summary>

<details>
<summary>[x] 14. (Development) GitHub setup</summary>

- Set up GitHub upstream; uploaded frontend and backend to personal GitHub

</details>

<details>
<summary>[x] 22. (Metronome) Navbar metronome widget</summary>

- Lives in navbar as a collapsible widget accessible from any page
- Web Audio API AudioWorklet for drift-free timing (not setInterval)
- Visual pulse tied to the audio clock

</details>

<details>
<summary>[x] 23. (Development) Persistent navbar</summary>

- Navbar on top of site for desktop displays
- Site name + nav links (Licks, Songs); designed to accommodate new buttons as features grow

</details>

<details>
<summary>[x] 44. (Development) Update frontend and backend READMEs</summary>

- Update README on frontend and backend to reflect new feature set

</details>

<details>
<summary>[x] 53. (Development) refactor backend using Domain-Driven Design philosophy</summary>

- this will segregate the different verticals in the app, reducing tokens and context
- instead of searching the full repo, first just use the readme.md and claude.md
</details>

<details>
<summary>[x] 54. (Development) refactor frontend using Feature-Sliced Design philosophy</summary>

- this will segregate the different verticals in the app, reducing tokens and context
</details>

<details>
<summary>[x] 52. (Development) add Home page with features clickable in page body</summary>

- clicking Lick Library in navbar should take user to home page, where the different navbar options are displayed in more detail in the page body
</details>

<details>
<summary>[x] 85. (Development) ipad web ui</summary>

- when saved as webapp on homescreen on ipad, hide addressbar,
</details>

<details>
<summary>[x] 86. (Development) stub out planned feature verticals </summary>

- in navbar and home screen, add links to the following
    - Playlists: empty page, but in a new vertical slice so new directory
    - Theory: empty page in new slice ..
    - Live: empty page to be used for play along with mic input and a guitar neck visual
- just stub the empty pages, add text field describing whats to come, for each page stub
</details>

<details>
<summary>[x] 9. (Development) iPad PWA (home screen shortcut)</summary>

- Pure frontend config: manifest.json, apple-touch-icon, viewport meta, display: standalone
- Vite PWA plugin (vite-plugin-pwa) handles most boilerplate
- No backend touches needed
- Target: 2025 iPad Air

</details>

</details>


<details>
<summary>Chords Gallery Feature</summary>

<details>
<summary>[x] 43. (Chords) Chord voicing improvements (real voicings, visual display)</summary>

- ChordQuality and ChordShape JPA entities; 70 seed rows (5 CAGED shapes × 14 qualities)
- ChordShapeSeed ApplicationRunner seeds on first startup (idempotent)
- transposeShape: offsets fretted values so root lands on correct fret; muted ("x") and stay-open (-1) values unchanged
- formatShape: renders int[] to ASCII tab matching Position.toTabString() output format
- GET /api/chord?instrument=GUITAR returns real CAGED fingerings; other instruments return empty list
- Visual grid display deferred; user-submitted voicings deferred to idea 47

</details>

<details>
<summary>[x] 47. (Chords) allow user to upload missing voicings for chords</summary>

- if chord displays as ??? in song page, then clicking on ??? will open the modal for adding a new chord.
- use existing add chord voicing modal. hardcode the chord being updated as chord name, and disable text entry in that field.
- submit button will add voicing to db and the song will pull the new voicing.
- user submitted voicings get prioritized in chord voicing list, and are displayed first on hover.
</details>

<details>
<summary>[x] 50. (Chords) update chord voicings modal on front end</summary>

- sort chord voicings by lowest fret, so open and common voicings are first in list
- the arrows should loop, so back on first voicing will take you to last voicing etc
</details>

<details>
<summary>[x] 51. (Chords) add "chords\ngallery" tab to navbar</summary>

- chord page lets user select key and shows all voicings of all chords of all quality for that root note
- display chord and voicing modals in a grid (similar to the hover modal in song, which lets you cycle voicings)
</details>

<details>
<summary>[x] 58. (Chords) upload chords page</summary>

- add page with form for user to upload a new chord voicing.
- should seamlessly support new voicing for existing chord, and new chord altogether
- you get here by clicking add chord in Chord Gallery page
- this form will also be used as a modal elsewhere so prep it for that
</details>

<details>
<summary>[x] 66. (Chords) chord voicing upload ui input</summary>

- on upload chord, frontend only, can we make the chordname field up just be a text field? then we can parse the root and quality from that, and allow nonsense to give a little error message. i think this will eliminate need for the shapename text field below.
- also give an error for voicing already exists. a future feature will use this modal, so we need to program in ability for text field to be off limits if flagged
</details>


<details>
<summary>[x] 70. (Chords) chord upload for ??? is failing for G/F#</summary>

- is this because we have not defined a G with quality /7 ?
- chord upload for existing chords works (tested with adding missing B7 voicing)
- other uncommon qualities are causing errors as well
</details>

<details>
<summary>[x] 48. (Chords) change chord display from ASCII to pretty diagram</summary>

- chords should be shown as an image rather than ascii
- perhaps use js library: svguitar
</details>

<details>
<summary>[x] 65. (Chords) chord voicing upload ui display</summary>

- add pretty chord display right next to the fret input, showing how voicing will display on page once added
</details>

<details>
<summary>[x] 64. (Chords) chord gallery enhancement- manage/delete voicings</summary>

- add Manage button/icon to top of Chord Gallery page
- when you click manage, if you click on a chord box, it will show all voicings in a grid, and they will have X on top right of box, and clicking will trigger confirmation prompt. if confirmed, it will delete
- how does this handle deleting voicings that were added by system? should it allow this?
    - some default voicings are weird
- chord gallery should pull all voicings for chord, including custom and / chords if they exist.
</details>

<details>
<summary>[x] 76. (Chords) reseed shouldn't fill out slash chords since its just guessing them</summary>

- i don't want reseed to fill up with inaccurate chord charts.
</details>

<details>
<summary>[x] 77. (Chords) chord gallery ->manage->manage voicings enhancement</summary>

- update display so the x is on right side of the voicing diagram
- the delete yes/no dialogue should be on right side of voicing diagram. delete should be above the confirm button
</details>

<details>
<summary>[x] 78. (Chords) chord gallery - slash chords are displayed wrong</summary>

- currently slash chords are displayed like G/11 when the number after the slash actually represents a note relative to the root. G/11 should display as G/F# etc.
- reseeder shouldn't bother with slash chord generation.
- ensure chord voicing upload flow and reseeder flow account for duplicate chord voicings
- chord gallery should pull existing slash chords and display them correctly
</details>

<details>
<summary>[x] 81. (Chords) manage voicing display enhancement</summary>

- the x button in manage voicings in chord gallery by each voicing should be 5 times bigger and centered next to the chart. the delete confirm dialogue should be a single button    
  containing "delete?" instead of what is currently there
</details>

</details>


<details>
<summary>Live Feature</summary>

<details>
<summary>[x] 8. (Live) Scale / CAGED neck learning tool</summary>

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
<summary>[x] 19. (Live) Animated neck visualization</summary>

- Playhead-driven neck display; notes flash on active fret/string at tempo
- Playhead indexes into columnIndex sequence at rate derived from BPM; pairs with metronome
- Instrument-aware: render string count and layout from Instrument interface
- Scale backdrop (#8) overlays dim dots for valid mode positions in current fret window

</details>

<details>
<summary>[x] 21. (Live) Scale overlay + mic input page</summary>

- Separate navbar page combining neck display and mic input
- Select key and mode; overlay scale positions as dim dots on the neck
- Mic input identifies pitch live and highlights corresponding note on neck
- Depends on #18 (mic pitch detection) and #19 (neck visualization)

</details>

<details>
<summary>[x] 89. (Live) live neck enhancement</summary>

- darken strings
- maybe tan rectangle under strings display to give more guitar appearance.
- when a note is selected, the pale bright yellow outline should be a thin
    - note name should be bigger, slightly bigger when unselected, even bigger when selected
- when live page is loaded, default to C Major perhaps instead of empty.
</details>

<details>
<summary>[x] 90. (Live) live enhance</summary>

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
<summary>[x] 94. (Live) live tab enhance - note picker</summary>

- next note is a little zealous.
    - distance from note at fret/string should account for diagonal distance,
    - fewer notes should light up. no distance greater than 4.000.
- add a key up to the title card row of live tab. list the intervals relevant to the mode and put each number in a bubble matching their color (red is root).
    - when a currentNote is active, that interval should be highlighted same in the key.
    - when next notes are flashing, those intervals should be highlighted same in the key
</details>

<details>
<summary>[x] 18. (Live) Microphone pitch detection</summary>

- YIN algorithm or pitchfinder.js for single-note pitch detection
- Limit to Guitar.STANDARD + clean tone input for accuracy
- On note detected: resolve to nearest Note enum, display all neck positions via findNeckPositions
- Separate Fretboard Explorer mode; tab-following (timing sync) is a later phase
- Known constraint: distortion and low notes degrade accuracy

</details>

<details>
<summary>[x] 96. (Live) live neck visual polish (from 89)</summary>

- intervals in bubbles in the live bar should be have same font color as the notes in the bubbles on the neck
- Darken string lines: `#6b7280` → `#374151`
- Add warm tan/wood `<rect>` behind string lines as fretboard surface background
- Active note ring: it's hard to see with a white background. maybe it pulses too, or maybe it has a very thin dark outline on outer bound of pale yellow border?
- Larger note name labels: unselected 1-char 7→9px, 2-char 5.5→7px; active one step larger again
- **File:** `src/features/live/GuitarNeck.tsx`
</details>

<details>
<summary>[x] 102. (Live) live display should have tighter reach</summary>

- 3.9 max distance. a 4 fret delta is actually a 5 fret reach, which is hard to play
</details>

<details>
<summary>[x] 67. (Theory) Theory tab in navbar</summary>

- this will take user to a page that holds all the circle of fifths, caged, scales, live input stuff
</details>

<details>
<summary>[x] 107. (Live) dialing in live page next note </summary>

- live page next note still feels aggressive, maybe one less string jump allowed
- alternative: only show closest of each note
</details>

<details>
<summary>Theory Live Unification Epic</summary>

<details>
<summary>[x] 95. (Theory) theory tab content</summary>

- perhaps a compare modes tool, to show root on ionian is x on dorian etc.
-  theory tab shouldn't have mic input
</details>

<details>
<summary>[x] 97. (Live) CAGED-based next-note suggestions for Live tab (from 90)</summary>

- Replace Euclidean distance filter with CAGED positional logic
- Define 5 CAGED boxes as fret-range windows relative to the root (~4 frets wide, ~1-fret overlap)
- When note is selected, identify its CAGED box; candidates come from that box and adjacent boxes only
- Naturally constrains next-note highlights to playable hand positions
- **File:** `src/features/live/LivePage.tsx`
</details>

<details>
<summary>[x] 98. (Theory) mode relationship display for Theory tab (from 90)</summary>

- Show how each mode relates to its parent Ionian key, e.g. "A Aeolian = relative minor of C Major (6th degree)"
- User picks root + mode; page shows parent key and degree position
- Mapping: IONIAN=1, DORIAN=2, PHRYGIAN=3, LYDIAN=4, MIXOLYDIAN=5, AEOLIAN=6, LOCRIAN=7
- **File:** `src/features/theory/TheoryPage.tsx`
</details>

<details>
<summary>[x] 99. (Theory) CAGED position zones on neck for Theory tab (from 8)</summary>

- Overlay 5 CAGED boxes as labeled shaded regions on a static GuitarNeck
- Semi-transparent rect per box spanning its fret range × all 6 strings, labeled C/A/G/E/D
- User picks root key; boxes shift to correct fret positions
- **Files:** `src/features/theory/TheoryPage.tsx`, reuse `GuitarNeck`
</details>

<details>
<summary>[x] 100. (Theory) pentatonic-within-diatonic overlay for Theory tab (from 8)</summary>

- Any diatonic CAGED box = three overlapping pentatonic shapes (e.g. G Ionian = G + C + D pentatonic)
- Color-code or outline the three pentatonic subsets within the 7-note scale overlay
- Implement after CAGED zones (idea 100) are working
- **File:** `src/features/theory/TheoryPage.tsx`
</details>

<details>
<summary>[x] 109. (Live) live page guitar notes display</summary>

- active note should pulse more than next note
- next note should pulse its own color instead of dark red.
- its hard to visually discern current note from next note at a glance, the pale yellow is tough to see
- clicking on one of the notes in the toolbar should highlight all notes of that degree!
- include next closest own note in next note candidates.
    - what if the nodes from the next own note glow and pulse half as bright as the first degree next nodes?
- i just noticed its only highlighting nearest 2 notes on each side.
    - lets just consider all intervals when looking for candidates, and grab one of each, then deferring to above second degree node logic
</details>

<details>
<summary>[x] 110. (Theory) theory page interactive guitar ui</summary>

- guitar neck re used in a sub page under theory.
- interactive diagram that shows theory stuff when you click a note.
- omg. click notes on the neck and it builds a lick! tab output.
    - single note at a time i guess for now
- click a note, all instances of that note glow
    - show common intervals. note relations
    - now we can get into the weeds about theory
</details>

<details>
<summary>[x] 118. (Theory,Live) Unify theory and live pages</summary>

- the more i think about it, i think the live thing should just get all the theory stuff
- i don't see it staying distinct, especially if the pitch detector feature isn't worked out yet.
    - i think the caged and theory stuff will be more digestable if they are part of the interactive component.
    - caged scale overlay is already there. add a way to highlight by color, or by caged shape
        - show chords and chord voicings overlaid onto neck depicting relevant scale underneath
        - circle of fifths chord progressions by interval and quality notation
            - show these progressions on the neck.
- something about mode relations
- something about diatonic scales being built of 3 adjacent pentatonic scales
- i think maybe some of this stuff might get crowded, so toggle theory modes by clicking on one of a set of right aligned connected buttons naming the options on live toolbar
    - Option: Lick Visualizer
        - input guitar tab syntax
        - output that sequence visualized on the guitar neck, either at once or column by column of tab
        - potentially show how it relates to its key/mode/scale
    - other Option will be made up of above ideas
        - Option: circle of fifths/progressions
            - voicings for progressions up neck without capo
</details>

</details>

<details>
<summary>Theory Second Pass Enhancement Epic / Pentatonic and Live Rewrite</summary>

<details>
<summary>[x] 120. (Live,Theory) interval toolbar enhancement</summary>

- in live tab, in live mode, when you click on an interval in the toolbar, and it glows,
    - you can select other intervals in the toolbar and they will glow too
- When multiple degrees are selected and glowing, the neck visual shows:
    - All notes of all selected degrees active simultaneously
- When multiple intervals are selected in the toolbar
    - 'clear' button appears, deselects all, and button vanishes
- live tab, live mode. if no intervals are selected in toolbar
    - user can click notes on neck and it will highlight nextNotes, etc like usual
</details>

<details>
<summary>[x] 121. (Live) Next Note Highlight Enhancement</summary>

- when in live tab, and current note is selected, and nextNotes are pulsing, next own note should also pulse bright
    - next own note should behave like the other first degree nextNotes, full bright pulsing.
    - second degree notes off of next own note should be half bright pulsing
- pulsing notes shouldn't have white circle behind
    - remove the white circle totally, the visibility is covered by brightness and pulsing
- The pale yellow active ring should be changed slightly
    - currentNote should pulse its own color, like the other bright notes, but the outer border of the pulse should be the pale yellow ring
</details>

<details>
<summary>[x] 123. (Theory,Live) Pentatonic Demo</summary>

- does the pentatonic mode on live tab allow you to stack pentatonic scales on the chart?
    - adding G + D + C pentatonics could be like: you've built G diatonic
- incorporate pentatonic mode into selecting intervals thing
    - if you are in major mode for example
        - select pentatonic for <key>.
            - this will highlight the notes of that scale on neck
            - it will light up the relevant intervals in the key
        - select intervals in toolbar
            - if a combination matches pentatonic notes, then the relevant pentatonic scale lights up
- The current "Pent. Sets" toggle shows which diatonic notes belong to which of the 3 pentatonic subsets (passively).
    - This seems like a different feature — more active, where you select individual pentatonic scales to stack and discover the diatonic.
        - The feature described in this idea should replace the old pentatonic feature.
- "Select pentatonic for key" — is the pentatonic root always the same as the main root selector, or independently choosable (so you could overlay G pent + C pent + D pent on a G neck)?
    - we are selecting pentatonics of different roots to display a full diatonic
- For the interval-selection → pentatonic recognition flow: does this happen automatically as you select bubbles in the toolbar (live feedback), or is it a separate mode/trigger?
    - this happens live
    - if you select 1 2 3 5 6 in toolbar in G major, the G pentatonic button will glow
- pentatonic widget. starts as button for pentatonic (this lives below the neck visual)
    - on click, button changes color to show pentatonic is active
    - box exposed below
        - above box is a dropdown selection for mode.
            - this adjusts the mode of pentatonics displayed when user clicks pentatonic key in the box below.
            - default value is major OR sync with diatonic scale mode chosen from dropdown in live toolbar.
            - if user updates the mode in live toolbar, it updates mode selected in pentatonic widget.
                - if user has modified mode in pentatonic widget, it no longer syncs with live toolbar
                - reset state on refresh
        - box contains 4w 3l grid of all 12 notes
        - when multiple toolbar intervals are selected, and the selection builds a full pentatonic scale
            - the key of that scale begins to glow in the grid
            - if multiple pentatonics can be built from the interval selections, all applicable keys glow in grid
        - if user clicks on key in box, it will glow all the notes of that pentatonic scale on the neck of the guitar.
            - if no key/mode is selected in the guitar neck live dropdowns,
                - then notes of pentatonic scale should still show on neck when the associated key button is selected from the grid.
            - if those notes aren't a part of the diatonic key/mode selected
                - selecting key in pentatonic grid should still highlight the notes it represents
    - maybe we remove the pentatonic mode selector and just have it permanently sync with the live toolbar mode selector?
        - if we do this, we can extract the mode comparison stuff to a more fleshed out feature.
</details>

<details>
<summary>[x] 128. (Theory) Pentatonic Display Enhancement</summary>

- in live tab, live mode
    - if diatonic scale is selected for some mode/key
        - if pentatonic scale is selected in some mode/key,
            - notes in the pentatonic scale get a ring
                - ring can nest up to 3 times to show up to 3 pentatonics displayed on neck at once
                - color of ring
                    - idea 1: color of ring matches the interval colors relative to pentatonic scale root note
                        - e.g. if diatonic scale is C Major, and first pentatonic scale selected is G Major, then the pentatonic rings will red on G notes etc
                        - if second pentatonic is selected. root note of that scale will be red for second ring
                        - ditto for 3rd.
                        - if a pentatonic button is deselected
                            - if this is first or second pentatonic scale selected, rings shift down to replace removed level.
                            - new first pentatonic will occupy lowest ring, third ring shifts to second ring etc.
                        - ring level chosen is based on how many pentatonics are selected. logic should not care if any particular note is already pentatonic occupied, since ring position is based on pentatonic scale selection order.
    - if user clicks note in interval toolbar, the notes should pulse, but they should not glow pale yellow, as they are not currentNote
</details>

<details>
<summary>[x] 129. (Theory) Live Pentatonic Enhancement</summary>

- the box for pentatonic button and notes grid
    - pentatonic button should be inside the box, to the left of the Mode select.
        - button should not move around when pentatonic is toggled, or when buttons are used
    - synced indicator should be smaller and it should be on the row below mode dropdown, right aligned
        - when desync occurs, the words should vanish, but the space it occupied should remain, so card doesn't resize.
    - the dropdown for mode should be a bit smaller in width. accomplish this by changing "Natural Minor (Aeolian)" to "Aeolian (N. Minor)" also change ionian to be "Ionian (Major)"
    - clear all should be changed to clear, and it should be in row with synced, but left aligned
    - when pentatonic button is inactive, also hide mode and sync, as well as hide the box
- pentatonic nested rings highlighting
    - can each ring be bounded by thin black borders?
- pentatonic detection from interval toolbar
    - update logic to highlight all scales that are not ruled out based on selection
        - use different button colors to distinct partial match from full match, yellow and orange
- when pentatonic key button is selected, and notes outside of scale are glowing, they should display their notes
- get rid of caged button and the caged shape highlighting, move guitar neck up a little with that space being freed up
</details>

<details>
<summary>[x] 127. (Theory) Next Own Note Enhancement</summary>

- next candidate rewrite
    - current implementation:
        - currentNote brightens, pulses, glows
        - nextNote is closest of each interval in the scale, one per interval, including own note; brightens, pulses
        - second degree next notes are shown off of nextOwnNote; pulses, half bright
    - new implementation:
        - currentNote brightens, pulses, glows
        - nextNote is closest of each interval in the scale, one per interval, including own note; brightens to .8, pulses
        - second degree next notes are next closest candidate for each interval relative to currentNote
            - these pulse and are 2/3rds brightness
        - third degree next notes are 3rd closest candidate for each interval relative to currentNote
            - these pulse and are at 1/3rds brightness
        - all degree candidates get dark enough text to discern note
</details>


</details>

<details>
<summary>Theory Third Pass Epic / Visualizer Rewrite</summary>

<details>
<summary>[x] 122. (Theory/Licks) Lick Visualizer Overhaul</summary>

- lick visualizer
    - one column at a time mode should cycle columns every second
    - toggle for mode should just show all notes from tab on neck at once.
    - add button to get lick from lick library.
        - that should open modal that displays list of lick cards
        - click a lick, it will load in the text field in lick visualizer and the guitar neck will show up automatically
            - don't worry about showing generated position licks yet
- "Cycle columns every second"
    - add toggle.
        - default selection is 1 col per second.
        - when toggled, update rate is synced with metronome
    - add pause button for lick playback.
        - add selectable progress bar.
            - progress bar is synced to raw tab.
            - since tab is ingested column by column, dragging the progress bar over a column will display the notes played in that column
            - progress bar can only land on columns where notes are present.
                - can tabs have rests or blank space? if so, need to include a symbol so it isn't skipped like the columns between notes.
        - progress bar card can go below the lick text input field.
            - the currently processed lick will be in that field
                - the progress bar will be aligned under the lick so where you drag it will aligned with the column that gets selected
- When a lick is loaded from the library modal,
    - lick should render in original saved key.
    - expose controls to alter key.
        - this will reveal position selecting flow, similar to lick modal
- when lick is loaded from lick modal from Lick Visualizer
    - automatically update live neck with lick rendering
        - default is render all notes from lick at once on neck
            - toggle will swap mode from all-at-once to playback-mode
                - in playback mode, user can choose between 1 second speed, and metronome-sync
- remove lick text field with buttons. select lick from library, and new lick
    - select lick brings up lick modal.
    - selected lick gets rawtab displayed where lick text field is in current design
    - new lick opens new lick modal, which you type the lick into the field, click submit, and it uploads the lick to the lick library
        - then it automatically calls that lick and puts it in the visualizer
- lick visualizer should not be reinterpreting the tabs inputted.
    - since visualizer takes input as raw tab string, we have absolute positions of every note played on the neck in the lick.
    - use that, not the position finder.
- Perhaps Lick Visualizer should be its own tab "Lick Visualizer" OR perhaps it should be accessible under Licks tab instead.
    - open to discussion
- feature to build lick by clicking on notes on the guitar neck diagram, and it produces a rawTab text that you can save
    - maybe it can determine possible valid keys / modes that describe the lick
        - this should account for blues notes etc and assume blues or jazz over nonstandard mode if it comes down to it.
</details>

<details>
<summary>[x] 130. (Theory,Licks) Lick Visualizer Second Pass</summary>

- make guitar neck always on screen, even when no lick loaded.
- load from library button and New Lick button replace the existing inputs
    - New Lick opens new lick modal
        - uses text field rules from /licks new lick input field
        - Visualize button loads tab in visualizer.
    - if tab being visualized isn't loaded from lick library, then there is a Save Lick button which triggers that process
        - if it is rejected on backend, then produce a red error message below it
        - if lick was loaded from lick library, then Save Lick button is grayed out
    - if lick is modified (Edit Lick button which opens Edit Lick modal (same as New Lick modal))
        - enable save lick button
            - if lick exists, then error message says lick exists
- because lick editing is moved to a modal, now we can display the raw tab ascii in a way that syncs with the progress bar
    - tab ascii should be rendered in a way spreads it out more (possibly insert extra - between notes?)
    - progress bar should be aligned so jump points are lined up with corresponding note column in raw tab
- add a button to build a tab from clicking the neck diagram.
    - one note at a time, for initial impl
        - this way there isn't a need for a next column button
    - output is rawtab ascii. updates live as it is built
        - it is displayed concisely (without the spacing that the progress bar from other screen would add)
        - save lick button
        - you can edit the output tab in the text field before submitting,
            - text field follows same rules as licks page new lick
- lick library modal should show full lick in each card
    - currently it cuts off licks horizontally, hiding last 3 string rows.
    - it enforce card width and cut licks off vertically, so long tabs show all strings in preview, but lines can get cut off with ... to preserve card size
</details> 

<details>
<summary>[x] 131. (Theory,Licks) Lick Visualizer Third Pass</summary>

- when submitting a lick, reformat it so each column with notes has a 1 "-" gap between next column with notes.
- saved licks should use reformatted rawtab
- when tab is displayed in visualizer
    - all-at-once mode
        - show rawtab without any extra spacing added (only have 1 hyphen column between each note column)
    - column mode
        - show rawtab with enough hyphens added between note columns so that spacing aligns with progress bar stages
- if there is a decorator between notes in a loaded tab in the visualizer, add decorator at midpoint between columns of rawtab
</details>

<details>
<summary>[x] 134. (Theory,Live,Licks) Restructure</summary>

- theory tab should now direct to the Live tab Live mode.
    - we will rename this to Theory tab Theory mode
    - once Live tab is freed up,
        - remove microphone stuff from Theory tab Theory mode.
        - add microphone stuff and copy over currentNote logic and guitar neck to new page accessible via Live tab
    - Theory tab Theory mode will just be Theory page
        - pill button is unnecessary since subfeatures are moved around
- move Lick Visualizer under Licks
    - On Licks page, rewrite to change title to Licks
        - below title, the upload lick stuff stays where it is
        - below upload licks is 2 buttons
            - Lick Library - shows lick library, which used to be shown on licks home page
            - Lick Visualizer - directs to Lick Visualizer, which is moved away from live tab over to licks
    - the new/edit lick modal used by lick visualizer should be updated to include optional key and mode
        - make it use the same rules as the upload lick impl on /licks
            - replace the upload a lick card with the content used in the add lick modal
                - don't make it a modal on the licks page tho, it should look similar to the old one after the change is made
- Chords pill page should be moved under Chord Gallery tab.
    - add a button on chord gallery homepage between key select and Upload Voicing, Labeled Chord Theory
        - this directs to the chord pill page from former live tab
</details>

<details>
<summary>[x] 124. (Live,Theory) Separate Interactive page from mic</summary>

- make the mic input lighting up guitar neck its own page and that goes on Live
- everything else from Theory and Live go back to Theory tab
-
</details>
</details>


</details>


<details>
<summary>Playlists Feature</summary>

<details>
<summary>[x] 40. (Playlist) Global playlists</summary>

- Playlist tab in navbar; list shows playlist name, song count, creator
- Clicking song from playlist shows "back to playlist" button above song name (hyperlinked)
- Songs in playlist show Next → / ← Back for sequential navigation
- Per-song key/capo override stored in playlist; overrides song default

</details>

<details>
<summary>[x] 113. (Playlist) add song ui</summary>

- add an add to playlist icon to song display page in toolbar to show in all contexts where song is displayed.
- in manage song view in Songs library page, add icon for Add To Playlist button.
    - clicking button should open playlist modal with filtering dropdown you can type in to select playlist.
- in playlist main page, create button should open Create Playlist modal.
- in playlist main page, add Manage button next to Create button, Manage will expose X button on playlist boxes. playlist name becomes text field prefilled. click manage again to save. x button has confirm before deleting.
- on the page for viewing details of a single playlist.
    - remove x button. add manage icon. manage icon should be like a gear or wrench.
        - manage will expose Add Songs button above songs list.
        - manage will expose x button in each song box. with confirm before deleting.
        - manage will expose button to type in playlist name field and then click button again to save change and toggle back. edit field icon should be pencil
        - manage will expose delete playlist x button with confirm modal.
        - add songs button will open an add songs modal.
            - text field for filter. shows songs below that match artist or songname. plus icon to add
            - display songs as grid. or list (same logic as displaying song list elsewhere)
            - plus button turns to green check when clicked. then turns to red x to remove song from playlist
</details>

<details>
<summary>[x] 114. (Playlist) rename playlist flow</summary>

- click gear on playlist page. it shows edit icon next to playlist name.
    - if you click edit button, turns playlist name into editable text field.
    - there is now a floppy disk icon to the right of the text field.
        - clicking the floppy disk icon saves the name change and changes the playlist name back to a non-editable field and returns the edit icon
- the rename flow on the playlist library page should be updated to match this flow
    - clicking manage should expose an edit button next to playlist name, in addition to the right aligned delete button (which should be red),
        - rest of rename flow should match above
</details>


<details>
<summary>[x] 115. (Playlist) playlist manage flow</summary>

- on playlist details page
    - replace gear with Manage button
    - replace x and check with Delete and Done
- on song display page within playlist
    - ensure the save key/capo button updates the saved voicing for that song within the playlist.
    - the next song button at end of playlist should direct to first song in playlist
- on song display page outside of playlist
    - add to playlist button, when clicked, should add the song to the playlist with whatever the current capo and transposition is set to.
    - when you click on + to add song to playlist in the modal, the green check should turn to a red delete x after 2 seconds
</details>

<details>
<summary>[x] 116. (Playlist) playlist detail manage enhancement</summary>

- the button that says default should instead have 2 rows and say the capo and the sound key
    - clicking this button will open a song voicing modal
        - modal will have song name on top
        - modal will have capo / transpose widget side by side in middle
        - modal will have a save button at the bottom
- songs can only be added to playlist once
- on song display page within playlist
    - "Save key/capo" button
        - rename button to "Save voicing"
        - ensure the button updates the saved capo/transpose for that song within the playlist in the db.
            - when its saved, change the text in the button to "Saved" but don't change size of button
        - maybe an icon instead of words for this button. then clicking it will change it to a green check to show that the voicing was updated for the song in the playlist. 2 seconds later, change to a blue refresh icon.
            - clicking refresh icon will restore default voicing for song in playlist.
    - clicking prev on first song in playlist will take you to last song in playlist
</details>

<details>
<summary>Reverting Playlist Song Key Changes And Implementing Simple Offset Approach</summary>

<details>
<summary>[x] 117. (Playlist,Songs) songs page link</summary>

- the last 2 fixes were wrong and overengineered.
    - when song is added to playlist via song detail page
        - save capo offset (+/- from song's default capo value) and key offset (+/- from song's default key)
    - when viewing songlist in playlist detail
        - if manage is active, then add a button to change voicing offset
        - if manage is not active, song card should look like list view from songs library
            - the key shown in the song card should be offset by the offset value associated with the song entry in the playlist
    - if song is opened via songlist in playlist detail, or via next/prev button in playlist song viewer,
        - apply playlist specific offset for that song in capo widget and transpose widget, so that the song transposes to the expected place.
    - if song is opened from song library page, then playlist offsets are not applied to transposition.
        - song should show in default key and capo in this case
    - rewrite the playlist offset feature with this simpler approach in mind.
    - original message: i think the issue was that the key displayed in the song widget in the playlist detail view is showing the key representing  shape, but when we open the modal, it is treating it as the sound value, and that is causing a weird mismatch even once the song is open. however, i noticed this change persists to the song outside of the scope of the playlist, just viewing the song from the songs vertical. to clarify, the previous change did not fix the issue i had attempted to describe, which is why i am elaborating now
    - i have a song viewed from library that is showing capo 4 D# shape G sound but then the chords are in G because that is the default key of the song. the key is saved as G in the song metadata, so this weird behavior is a result of breaking the transpose widget in some way
-
- when add to playlist modal is open, and playlists are listed
    - if the song is already in a playlist, the add button should be replaced with the x button to remove
    - clicking the playlist in the playlist list should take you to the playlist details page
</details>

</details>


</details>

<details>
<summary>[x] 132. (Licks) Lick Builder Refresh</summary>

- currently lick builder doesn't work
    - when a note is clicked, it should add it to the tab
    - currently notes don't register as clicked
- lick builder should ALSO have the currentNote highlight and next note suggestions from the theory guitarneck
    - lick builder should allow user to set key and mode
        - setting a key and mode  will overlay the diatonic scale on the fretboard
        - reuse existing components here if possible
            - if not, rewrite the diatonic overlay to be applicable to live and lick and theory, since they all use it
- lickbuilder should have start/stop button (next to clear button)
    - clicking start should change button to say stop
    - notes clicked while builder is active will pulse like currentNote
        - if chord detection toggle is on, all notes selected in same chord frame will glow and pulse like currentNote
- allow chord detection in lick builder (include toggle icon button (button turns green when active)(button is next to start stop button))
    - consider all notes selected in 1.5 seconds (resets after every note selection too) to be in the same column
    - one selection per string enforced
    - if selection is same string,
        - stop timer and enter to that note in the next column, and start timer for that column
    - after 1.5 seconds move to next column.
        - don't act until user inputs next note. once user inputs next note, listen for 1.5 sec to build a chord
    - timer starts on first note entry, if no notes entered in 1.5 seconds, then it moves to the next column. timer stops.
        - next note triggers timer, then once chord detection period ends, moves to next column
- add Lick Builder button on Licks page, next to Lick Visualizer button
    - directs to Lick Visualizer with Build pill selected.
-
</details>

<details>
<summary>[x] 138. (Song,Playlist) Capo is messed up again in the song toolbar</summary>

- we previously fixed the behavior of capo and transpose offset in manage song metadata flow.
- there is an issue. currently, in song toolbar, when i change capo value, it acts on shape rather than sound.
    - capo should offset sound key. should not alter shape key
- updating song key within playlist detail manage page
    - the song card displays the sound key for the song which is good
        - but the transpose and capo offsets are not applied to the key that gets displayed
    - the update capo/transpose offsets modal
        - incorrectly loads the original sound key instead of the sound key with offsets applied
        - it incorrectly puts that sound key in the shape spot in the widget
        - perplexingly, it correctly applies capo offset to the displayed sound key in the modal
            - this is strange because currently the capo value in the song detail page applies to shape(incorrectly) rather than the intended sound key.
- cases:
    - song upload flow
        - user provided value for key is considered the sound key
        - user provided value for capo is considered the capo value
            - the capo value cannot be negative
        - shape key is determined by sound key (user input) minus capo value
    - song update flow (manage song metadata)
        - user provided value for key is considered the sound key
        - user provided value for capo is considered the capo value
            - capo value cannot be negative
        - shape key is determined by sound key (user input) minus capo value
    - in song detail page
        - any offset changes are not saved, but only persist as long as song is open.
            - changing capo offset updates the sound value, but not shape value
            - changing transpose offset updates the sound and shape values.
        - add to playlist flow on song detail page
            - when user clicks add to playlist
                - grab currently applied capo offset
                - grab currently applied transpose offset
                - save these offsets in with the song in the playlist
            - when user opens song from playlist detail page, automatically apply the capo offset and the transpose offset to the song and the transpose widget
    - manage page for single playlist
        - song in playlist has capo_offset=0 and transpose_offset = 0
            - in playlist detail view, song card should show the sound key for key, with 0 offset applied
        - song in playlist has nonzero capo_offset applied
            - in playlist detail view, song card should show the result of (sound_key + capo offset) for key
        - song in playlist has nonzero transpose_offset applied
            - in playlist detail view, song card should show the result of (default_sound_key +capo_offset + transpose_offset) for key
        - song in playlist has nonzero capo_offset and nonzero transpose applied
            - in playlist detail view, song card should show the result of (default_sound_key + transpose_offset + capo_offset) for key
        - when user opens voicing modal.
            - capo widget should show (original_capo_value + capo offset).
                - reset button will set capo value back to song default
                - changing capo value via controls will update capo value and update transpose sound key value
            - transpose widget should show
                - sound: (original_song_key + transpose_offset + capo offset)
                - shape: (original_song_key - default_capo + transpose_offset)
                - reset button restores 0 offset for transpose
            - clicking save will update the offsets saved to the song entry in the playlist.
                - the song card in playlistdetail view should show the sound key with offsets applied.
</details>

<details>
<summary>[x] 139. (Development) Move repeat values to constants file</summary>

- noticed we defined the scales and offset math over and over again so we should move it all to one file, and have it referenced from there
</details>


<details>
<summary>[x] 119. (Playlist) playlist song detail view bug</summary>

- playlist song detail page currently conditionally displays a save icon button if user modifies offset from the offset saved to the song entry within the playlist
    - remove this button and behavior
    - instead, add feature to the existing add to playlist button
        - add to playlist modal
            - currently: in playlist list:
                - if song is not in playlist, plus button
                    - on click, add to playlist, button becomes green check for a few seconds, then turns to red x
                - if song is in playlist, x button
                    - on click, turns to plus to signify removal
                    - maybe add a intermediate icon for 1 second to signal  action, before changing to the plus
            - plan:
                - if offset is modified within playlist view, then instead of the save button appearing we use this logic to
                    - if user opens add to playlist modal within playlist song detail, and song entry offset is modified,
                        - in playlist card, instead of showing x, show a update icon
                            - on click, it updates the saved offsets for the song entry in the playlist - in place, song does not change its sequence in the playlist
                            - on click, update icon turns to green check for same amount of time as when adding song, then to a red x
</details>

<details>
<summary>[x] 140. (Playlist,Songs) ui aesthetics</summary>

- in playlist detail page,
    - song card should show capo as well (default capo + capo_offset)
        - should go between key and tempo
        - if no capo, say No Capo
- in manage view on playlist detail page,
    - change the pencil button and the x button (make it red) to be 3 times the size.
    - make the pencil button for renaming playlist 3x bigger.
    - the pencil button by the song card should not have a button around it
    - the pencil button and the x button should be spaced more apart, and not as close to right edge
    - if a playlist is empty, the add songs button should appear outside of manage mode.
- in song library, if show all is clicked,
    - currently the show less button is at bottom.
    - lets move show less back up top where show all is.
- in playlist song detail page,
    - if offsets are modified such that the update icon would appear in the add to playlist modal,
        - change color of add to playlist button to indigo
        - default color should be blue otherwise
</details>


</details>