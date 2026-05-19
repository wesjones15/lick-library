The current state of the app is a react vite frontend and a java spring boot backend. you input a guitar tab string, and it is converted into a collection of note objects and then gets processed into a list of output tab strings in the chosen key and mode. the unique licks get stored in a h2 db.

The purpose of this file is to hold ideas in memory so we can easily refer to them in claude code ui.

key: [ ]  = open, [x] = complete, [~] = deferred
---

songs that should have variable font size throughout seem to have a uniform font size applied

<details open>
<summary>Pending</summary>

<details>
<summary>[ ] 103. (Songs) Multi-Mode Key enhancement</summary>

- currently all song keys are assumed to be Major or Minor. 
- should we add support for the other modes or is that super uncommon?
- in song toolbar, instrument selector has Major/Minor right next to it
  - update so instead of major/minor, it says the sound key of the song, like in small mode.
    - sound key should be suffixed with m decorator for minor.
-
</details>

<details>
<summary>[ ] 106. (Songs) iphone portrait scrolling view cutoff issue</summary>

- iphone portrait scrolling view in song display. 
- smallview has same behavior as normal view for this issue
  - song body is too zoomed in. song line gets cut off (iphone 17 pro)
  - there is no whitespace, but the body is zoomed to fill screen with the text that is shown
  - we already tried a horizontal scrolling approach but what i want is for the text to be smaller to fit the screen 
- maybe we just make the font smaller in iphone view (and ipad portrait scrolling view?)
- case
  - expected view for scrolling view on song detail page
    - G                    C      G                      C
    - There's flies in the kitchen, I can hear all their buzzin'
  - ipad vertical view, song detail page, scrolling view
    - G                    C      G                      C
    - There's flies in the kitchen, I can hear all their buz
  - iphone vertical view, song detail page, scrolling view
    - G                    C      G
    - There's flies in the kitchen,
  - desktop landscape view, song detail page, scrolling view
    - G                    C      G            
    - There's flies in the kitchen, I can hear 
</details>

<details>
<summary>[ ] 111. (Songs,Chords) chord parser addition</summary>

- handle D7sus4 and similar
- handle CaddG and similar (case by case?)
- does this need to be combined with the deferred idea 79 (slash chords are overengineered)?
</details>

<details>
<summary>[ ] 137. (Songs) backend parser logic or frontend font?</summary>

- song parser seems to be forcing a uniform font
- i see a song is getting lots of whitespace added between sections instead of making the font bigger
- frontend
  - when transposing, if a chord becoming sharp eats the space between it and the next chord (or next character, since not everything in a chordline is chords),
    - add a space between them to prevent chord interpretation errors
  - when exp tab mode is on.
    - preserve technique on the tab
      - slash, etc should be preserved in processed tab, in same relative position to the original
</details>


<details>
<summary>[ ] 126. (Theory) Modes comparison</summary>

- there is learning potential with a modes comparison tool
- create a card like pentatonic and chords cards
  - put Modes button as toggle
  - have a dropdown select for other mode.
  - that will create an interval toolbar 
    - with colors shifted, so the red note in the 'other mode' is where the 1 of the main mode lies on the other mode scale
  - also have key select in the card
- the idea currently isn't fleshed out
- the theory live scope is expanding, but is also shaping into 1 big tool instead of a combo of interactables
  - i ultimately want mode comparison to go into the main interactive tool, but
</details>


<details>
<summary>[ ] 142. (Licks) Copy Lick Icon Button</summary>

- add copy lick button to 
  - lick visualizer, to left of edit lick button
    - should copy the condensed lick, not the spread version
- Lick Library list
  - lick card update
  - add icon/button for open lick in visualizer
  - add icon for copy lick to clipboard
  - add icon or just rest of card takes user to lick detail page.
    - lick detail page 
      - still shows other positions, allows transposition
        - add copy to clipboard icon button next to "original tab"
        - add copy to clipboard icon button to position card, in row below tab, left aligned
      - move positionbuilder algo to right align on row
      - instrument and key selectors should be side-by-side
        - mode selector? maybe not
      - put a border around tab intervals and mode card
      - add visualize button
        - acts as manage mode.
        - if you click on a position while visualize is active, it takes you to lick visualizer with selected position loaded.
</details>

<details>
<summary>[ ] 143. (Licks,Songs) exp tab feature bug fixes</summary>

- when on song detail, and exp tab is active
  - instrument = ukulele
    - at zero transpose offset, lick starts at fret12 instead of fret0 (maybe allow further reach when converting tabs between instruments)
    - at nonzero transpose offset, the standard guitar transposition gets displayed at certain offsets 
      - i might have requested this in the spec
  - instrument = custom or nonstandard guitar, 
    - i don't see tabs getting produced 
  - perhaps we create a crossInstrumentPositionBuilder
    - for licks that are converted from standard to another instrument, this builder will be preferred
    - it can reduce chords (dedupe intervals in a column)
    - it will be less strict wrt reach during traversal, since caged doesn't apply to other instruments/tunings
</details>

<details>
<summary>[ ] 145. (Licks) Renaming Lick Things</summary>

- rename
  - website Lick Library -> WesLicks
  - Licks -> Lick Library (displayed like Chord Gallery has a line break)
    - Both Lick Library and Chord Gallery get left aligned in the navbar.
      - when these are viewed from hamburger icon, remove line break
</details>

<details>
<summary>[ ] 146. (Songs,Theory) Noodle Tool</summary>

- in song detail page, add noodle icon button
  - this will open guitarneck with key/mode set to current song
  - sync with metronome, 
  - input field fills with the chords of the song, 
    - user can update this to put custom chords in 
  - currentChord will update as the metronome ticks, 
    - neck will highlight with notes that meet rules:
      - note is in scale for current mode
      - note is one of the intervals in the current chord
    - hook (defer) this feature can go in live tab
- flow:
  - start in song detail page.
    - user clicks noodle button
    - user is directed to live tab
      - (Play Along Mode?)
    - live tab receives the song object (chordlines,lyriclines)
    - instead of displaying chordsheet,
      - shows single line of song at a time 
        - next line is smaller below in gray font
        - prev line is smaller above in gray font
        - chord lyric pairs are displayed together
        - tablines can be ignored initially
          - future will make the tablines get played on the guitar neck, instead of skipping them
      - chord lyrics block shown is synced to metronome,
        - after each measure, shows next lyric block.
    - assume each bar has 4 chords
      - stub method for logic to handle more than 4 chords in a measure
      - stub method for logic to handle less than 4 chords in a measure
    - intervals that make up each chord get highlighted in diagram so user can see which notes to focus during each phrase
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

<details>
<summary>[~] 135. (Licks,Live) lickbuilder future work</summary>

- i want this feature to use the dynamic neck overlay that is currently deferred
  - this would enable users to make tabs at higher frets than 12
- if microphone feature in Live tab ever gets sufficiently accurate, we will merge lickbuilder and live features
</details>

<details>
<summary>[~] 133. (Licks) lick visualizer enhancement</summary>

- if playing in column mode, and slide,hammer, bend, pulloff is encountered
  - use animated transition between notes
- in all mode, label each note by column number
-
</details>


</details>

<details>
<summary>[ ] 147. </summary>

- 
</details>
