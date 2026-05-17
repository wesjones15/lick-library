The current state of the app is a react vite frontend and a java spring boot backend. you input a guitar tab string, and it is converted into a collection of note objects and then gets processed into a list of output tab strings in the chosen key and mode. the unique licks get stored in a h2 db.

The purpose of this file is to hold ideas in memory so we can easily refer to them in claude code ui.

key: [ ]  = open, [x] = complete, [~] = deferred
---


<details open>
<summary>Pending</summary>

<details>
<summary>Theory Live Unification Epic</summary>

<details>
<summary>[ ] 95. (Theory) theory tab content</summary>

- perhaps a compare modes tool, to show root on ionian is x on dorian etc.
-  theory tab shouldn't have mic input
</details>

<details>
<summary>[~] 97. (Live) CAGED-based next-note suggestions for Live tab (from 90)</summary>

- Replace Euclidean distance filter with CAGED positional logic
- Define 5 CAGED boxes as fret-range windows relative to the root (~4 frets wide, ~1-fret overlap)
- When note is selected, identify its CAGED box; candidates come from that box and adjacent boxes only
- Naturally constrains next-note highlights to playable hand positions
- **File:** `src/features/live/LivePage.tsx`
</details>

<details>
<summary>[ ] 98. (Theory) mode relationship display for Theory tab (from 90)</summary>

- Show how each mode relates to its parent Ionian key, e.g. "A Aeolian = relative minor of C Major (6th degree)"
- User picks root + mode; page shows parent key and degree position
- Mapping: IONIAN=1, DORIAN=2, PHRYGIAN=3, LYDIAN=4, MIXOLYDIAN=5, AEOLIAN=6, LOCRIAN=7
- **File:** `src/features/theory/TheoryPage.tsx`
</details>

<details>
<summary>[ ] 99. (Theory) CAGED position zones on neck for Theory tab (from 8)</summary>

- Overlay 5 CAGED boxes as labeled shaded regions on a static GuitarNeck
- Semi-transparent rect per box spanning its fret range × all 6 strings, labeled C/A/G/E/D
- User picks root key; boxes shift to correct fret positions
- **Files:** `src/features/theory/TheoryPage.tsx`, reuse `GuitarNeck`
</details>

<details>
<summary>[ ] 100. (Theory) pentatonic-within-diatonic overlay for Theory tab (from 8)</summary>

- Any diatonic CAGED box = three overlapping pentatonic shapes (e.g. G Ionian = G + C + D pentatonic)
- Color-code or outline the three pentatonic subsets within the 7-note scale overlay
- Implement after CAGED zones (idea 100) are working
- **File:** `src/features/theory/TheoryPage.tsx`
</details>

<details>
<summary>[ ] 109. (Live) live page guitar notes display</summary>

- active note should pulse more than next note
- next note should pulse its own color instead of dark red.
- its hard to visually discern current note from next note at a glance, the pale yellow is tough to see
- clicking on one of the notes in the toolbar should highlight all notes of that degree!
- include next closest own note in next note candidates.
  - what if the nodes from the next own note glow and pulse half as bright as the first degree next nodes?
</details>

<details>
<summary>[ ] 110. (Theory) theory page interactive guitar ui</summary>

- guitar neck re used in a sub page under theory.
- interactive diagram that shows theory stuff when you click a note.
- omg. click notes on the neck and it builds a lick! tab output.
  - single note at a time i guess for now
- click a note, all instances of that note glow
  - show common intervals. note relations
  - now we can get into the weeds about theory
</details>

<details>
<summary>[ ] 118. (Theory,Live) Unify theory and live pages</summary>

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
        - 
</details>

</details>


<details>
<summary>Songs Enhancement Epic</summary>

<details>
<summary>[ ] 103. (Songs) Multi-Mode Key enhancement</summary>

- currently all song keys are assumed to be Major or Minor. 
- should we add support for the other modes or is that super uncommon?
-
</details>

<details>
<summary>[ ] 105. (Songs,Licks) song transpose enhancement </summary>

- now that tablines are recognized in song parser and they are in tab format that is consumable by the licks parser
  - transposing song key should transpose the tabs as well using lick transpose logic.
</details>

<details>
<summary>[ ] 106. (Songs) iphone portrait scrolling view cutoff issue</summary>

- iphone portrait scrolling view in song display. 
- smallview has same behavior as normal view for this issue
  - song body is too zoomed in. song line gets cut off (iphone 17 pro)
  - there is no whitespace, but the body is zoomed to fill screen with the text that is shown
  - we already tried a horizontal scrolling approach but what i want is for the text to be smaller to fit the screen 
</details>

<details>
<summary>[ ] 111. (Songs,Chords) chord parser addition</summary>

- handle D7sus4 and similar
- handle CaddG and similar (case by case?)
- does this need to be combined with the deferred idea 79 (slash chords are overengineered)?
</details>

</details>


<details>
<summary>Reverting Playlist Song Key Changes And Implementing Simple Offset Approach</summary>

<details>
<summary>[ ] 117. (Playlist,Songs) songs page link</summary>

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
<summary>Deferred</summary>

<details>
<summary>Licks Enhancements</summary>

<details>
<summary>[~] 5. (Licks) Multi-measure phrase segmentation</summary>

- Long phrases split at `|` boundaries before running position builders
- parseTab currently strips `|` — would need to emit measure-break markers during parsing
- Segment interval list, run findPositions per segment; would improve quality on multi-bar phrases
</details>

<details>
<summary>[~] 11. (Licks) Parallel harmony generator</summary>

- Static phrase transformation in interval-space
- Input: List\<IntervalNote\>, Mode, harmonicInterval (e.g. THIRD, SIXTH)
- Map each note to its diatonic equivalent N scale degrees up within the mode's interval sequence
- Snap-to-scale flag for chromatic/passing tones
- Lives in LickUtils as a stateless pure function alongside toIntervals and detectMode

</details>

<details>
<summary>[~] 15. (Licks) Phrase predictor</summary>

- Deterministic rule engine; no AI/probabilistic components
- Input: key, Mode, chord progression, optional seed phrase
- Rules: chord tones on strong beats → stepwise voice leading → tension/resolution → passing tones on weak beats
- Output: List\<IntervalNote\> fed through existing position pipeline
- Guitar.STANDARD may attach idiomatic jump hint table (see #20) as weighted candidates

</details>

<details>
<summary>[~] 17. (Licks) Octave tracking</summary>

- Derive absolute pitch from fret + string using open string offsets + semitones per fret
- No string gauge attribute needed — gauge affects tone not pitch
- Each Instrument provides open string absolute pitches alongside existing tuning()
- Add octave as a derived field on TabNote or wrapper, computed at render time

</details>

<details>
<summary>[~] 12. (Licks) Upload instrument selection</summary>

- Allow user to change input instrument for tab upload
- parseTab + toIntervals currently always use Guitar.STANDARD
- Would potentially remove need for TabNote if parseTab is retooled for any tuning/instrument
- need to update lick library to support
  - manage lick flow, similar to manage song, to replace the direct delete lick ui
  - add sort and filter to lick library display.
    - filter by instrument, default show all
    - not sure for sort

</details>

<details>
<summary>[~] 101. (Licks) animated lick playback on neck for Lick detail (from 19)</summary>

- Add GuitarNeck below the tab on LickDetailPage; feed it computed positions for selected key
- Playhead advances through columnIndex sequence at BPM from MetronomeContext
- Active column's dots light up; play/pause button; loops
- Depends on position data from `GET /api/lick/{id}` and existing MetronomeContext
- **File:** `src/features/lick/LickDetailPage.tsx`, reuse `GuitarNeck`
</details>

</details>


<details>
<summary>Obsolete/Absorbed Into Theory/Live Monster</summary>

<details>
<summary>[~] 20. (Live?) CAGED idiomatic jump hints</summary>

- Guitar.STANDARD-specific static structure: list of (intervalDelta, stringsTraversed) pairs
- Consumed by predictor (#15) as weighted hint table for interval selection
- Does not affect position finding geometry — only influences which interval to target
- Other instruments may define their own tables on their own classes; no shared interface contract

</details>

<details>
<summary>[~] 56. (Chords) chord page enhancement - progressions</summary>

- chord page could also have option to show user chord progressions for certain keys. based off of interval, circle of fifths kinda stuff, mode, etc
</details>

</details>


<details>
<summary>Misc Forgotten Features</summary>

<details>
<summary>[~] 32. (Songs) Song recycle bin</summary>

- Deleted songs go to a recycle DB where they can be recovered

</details>

<details>
<summary>[~] 49. (Chords, Songs) create cache db of chord voicings to avoid need for rerunning chord calculations</summary>

- this feature is heavy if no users, but if we scale, it will be beneficial to reduce overhead
</details>

<details>
<summary>[~] 61. (Songs) super edge case: songs with bpm changes</summary>

- update bpm display for song to list multiple clickable bpms
  - the way to actually add mutliple bpms will be only exposed in song metadata update card (idea 45)
- consider bpm in autoscroll?
- make bpm refs in song itself clickable and they update metronome?
</details>

<details>
<summary>[~] 37. (Metronome) Metronome time signature options</summary>

- Options for single tone (no accent) and 3/4 time

</details>

</details>


<details>
<summary>Slash Chords Revision</summary>

<details>
<summary>[~] 79. (Chords) are slash chords overengineered?</summary>

- is there any point to having the middle layer of interpretting slash chords as /11 or /4 instead of /B or whatever matches in the key?
- i feel like we can just save them in the db directly as /B or whatever is there no?
- if i'm wrong, i want to discuss, don't just implement
- we should only implement if the end result cuts down on code volume significantly
</details>

</details>


<details>
<summary>Security,Microphone Access, Deployment</summary>

<details>
<summary>[~] 10. (Development) Auth & security hardening</summary>

- JWT auth, Spring Security, CORS policy, rate limiting, user-scoped repositories
- H2 → Postgres migration required
- Worth doing last if at all; not prioritized over functional features

</details>

<details>
<summary>[~] 13. (Development) Containerize and deploy</summary>

- Containerize frontend and backend
- Deploy to a hosting service

</details>

<details>
<summary>[~] 33. (Development) Users, auth, playlists</summary>

- Account creation with admin-gated 2FA
- Users can upload songs, view songs, make and view playlists
- Users can only delete their own songs; admin can delete anything

</details>

<details>
<summary>[~] 91. (Live) make app HTTPS</summary>

- mic doesn't work without https. can we make this app secure
</details>

<details>
<summary>[~] 108. (Live) mic input guitar note accuracy</summary>

- the live mic input is not accurate currently at placing the note on the neck
</details>

<details>
<summary>[~] 93. (Live) dynamic fret display idea</summary>

- in live tab, when the being played is up the neck, the diagram should shift to center currently played note in the diagram, revealing frets further up the neck, and perhaps hiding the first few frets,
  - at least 12 frets should show onscreen
- maybe frets shouldn't show uniform, but vary in size up the neck like real guitars
</details>

</details>


</details>

<details>
<summary>[ ] 119. </summary>

- 
</details>