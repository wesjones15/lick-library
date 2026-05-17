The current state of the app is a react vite frontend and a java spring boot backend. you input a guitar tab string, and it is converted into a collection of note objects and then gets processed into a list of output tab strings in the chosen key and mode. the unique licks get stored in a h2 db.

The purpose of this file is to hold ideas in memory so we can easily refer to them in claude code ui.

key: [ ]  = open, [x] = complete, [~] = deferred
---


<details open>
<summary>Pending</summary>

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
</details>

<details>
<summary>[ ] 106. (Songs) iphone portrait scrolling view cutoff issue</summary>

- iphone portrait scrolling view in song display. 
- smallview has same behavior as normal view for this issue
  - song body is too zoomed in. song line gets cut off (iphone 17 pro)
  - there is no whitespace, but the body is zoomed to fill screen with the text that is shown
  - we already tried a horizontal scrolling approach but what i want is for the text to be smaller to fit the screen 
- maybe we just make the font smaller in iphone view (and ipad portrait scrolling view?)
</details>

<details>
<summary>[ ] 111. (Songs,Chords) chord parser addition</summary>

- handle D7sus4 and similar
- handle CaddG and similar (case by case?)
- does this need to be combined with the deferred idea 79 (slash chords are overengineered)?
</details>

</details>

<details>
<summary>[ ] 119. (Playlist) playlist song detail view bug</summary>

- in playlist song detail view, where playlist controls are exposed over song display, the save icon button to "update voicing/save offsets for song in playlist" doesn't work.
</details>

<details>
<summary>Theory Third Round Enhancement</summary>

<details>
<summary>[ ] 124. (Live,Theory) Separate Interactive page from mic</summary>

- make the mic input lighting up guitar neck its own page and that goes on Live
- everything else from Theory and Live go back to Theory tab
- 
</details>

<details>
<summary>[ ] 125. (Theory) Chords Feature In Theory/Live Tab</summary>

- (defer)
  - if you select 3 or 4 or whatever, it will say what chord that forms
    - tie in circle of fifths/progressions connection
  - Where does the chord name display? Inline in the toolbar row, below it, or somewhere on the neck?
  - What if the selected intervals don't form a recognized chord —
    - does it say nothing, or show something like "no chord"?
  - "Tie in circle of fifths/progressions connection" is vague —
    - does that mean label the chord with its diatonic function (e.g. "IV — F major"), or something more visual?
</details>

<details>
<summary>[ ] 126. (Theory) Modes comparison</summary>

- there is learning potential with a modes comparison tool
- the idea currently isn't fleshed out
- the theory live scope is expanding, but is also shaping into 1 big tool instead of a combo of interactables
  - i ultimately want mode comparison to go into the main interactive tool, but
</details>

<details>
<summary>[ ] 122. (Theory/Licks) Lick Visualizer Overhaul</summary>

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
<summary>[ ] 130. </summary>

- 
</details>