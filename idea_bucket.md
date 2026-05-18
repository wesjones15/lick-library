The current state of the app is a react vite frontend and a java spring boot backend. you input a guitar tab string, and it is converted into a collection of note objects and then gets processed into a list of output tab strings in the chosen key and mode. the unique licks get stored in a h2 db.

The purpose of this file is to hold ideas in memory so we can easily refer to them in claude code ui.

key: [ ]  = open, [x] = complete, [~] = deferred
---

songs that should have variable font size throughout seem to have a uniform font size applied

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
<summary>Theory Fourth Round Enhancement</summary>

<details>
<summary>[ ] 125. (Theory) Chords Feature In Theory Tab</summary>

- Chords button in Theory tab
  - button lives next to pentatonic toggle, next to where the active pentatonic card ends
    - clicking the chords button opens a card for chords just like the pentatonic card does
  - when Chords mode is active:
    - 1,2,3 degree candidates no longer pulse or brighten
    - multiple notes can be selected 
      - even notes that aren't in the key
        - these notes get black text, display their note value, and pulse gray
      - a new card lists the intervals used, even off scale intervals
        - it lists root and quality chords you are making with those intervals
          - and perhaps the mode the chord fits in
        - if chord doesn't match anything then say so, but still show the intervals selected in the chord card
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
<summary>[ ] 12. (Licks) Multi-Instrument Support Epic</summary>

- [ ] Phase 1: Backend and Legacy Lick Upload
  - Allow user to change input instrument for tab upload
    - only for Legacy "Upload A Lick" Form
  - parseTab + toIntervals currently always use Guitar.STANDARD
  - Would potentially remove need for TabNote if parseTab is retooled for any tuning/instrument
  - backend logic should be tooled to allow dynamic input for instrument/tuning. 
    - after refactor, standard guitar should still behave the same, but flow should now accept, generate, and serve tabs for other instruments
- [ ] Phase 2: Lick Library Search
  - need to update lick library to support
    - manage lick flow, similar to manage song, to replace the direct delete lick ui
    - add sort and filter to lick library display.
      - filter by instrument, default show all
      - filter by mode
      - filter or sort by length of tab
      - search by present intervals 
        - (query: 1 b2 4) 
          - returns any tab containing that sequence
            - ignore slash or technique when finding results
            - if query contains technique, then don't ignore technique in search results.
- [ ] Phase 3: Chord Gallery Voicings DB
  - track chord voicings for other instruments
    - chord voicing db is updated to track instrument/tuning.
    - chord voicing modal will show instrument name/tuning in little gray font left aligned underneath the chord name in the modal
      - modal will retrieve chord voicings for instrument selected
        - add instrument selector to chord gallery
  - Add Chord Voicing for chord flow
    - add Instrument selector 
      - (also allow custom, like in lick search)
      - default value to standard guitar
      - if user changes instrument in chord voicing modal
        - then update the form and the display 
          - the fret entry should update to show the number and tuning of strings for the selected instrument
          - the display modal should be updated
            - modal shown in add voicing flow should display like the hover chord modal in layout
              - the name of the chord should show above diagram
              - the instrument name/tuning should show below chord name
            - the diagram should show correct number of strings for the instrument
- [ ] Phase 4: Song Detail and Tab Modal
  - in song page, where it currently says Standard
    - turn this into a selector button for instrument
      - chord voicings will now depend on selected instrument
        - if chord has no saved voicings, 
          - open Add Voicing modal, following existing flow for chords-with-no-voicings
            - instead of just hardcoding chord name,
              - also hardcode instrument for this circumstance
  - if exp tab feature enabled
    - replace the tabline blocks with a generated position for the selected instrument
      - position logic should be passed the notes of the strings, amount of strings, and run with that
    - the lick position shown at 0 transpose should be
      - as geographically close on the neck as the tabline block rawtab position as possible
  - if exp tab feature disabled
    - show unaltered tabline
- [ ] Phase 5: GuitarNeck Multi-Instrument Support
  - Implement support for custom tunings/instruments in GuitarNeck visual
    - if given 4 strings and GCEA or whatever,
      - GuitarNeck should produce only 4 strings
      - scales generated should be interval based on open string note.
        - i think existing logic should support this
    - Lick Visualizer, Lick Builder, Theory/Live, should get Instrument selector, and update based on instrument tuning
      - chord gallery Chord Theory page should also use this 
</details>

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
      - 
</details>

<details>
<summary>[ ] 141. (Licks) Lick page overhaul</summary>

- currently licks page has upload lick card and buttons to Lick Library, Lick Visualizer, Lick Builder
- Lick Builder has developed into a full on replacement for the original Upload Lick flow
  - Licks page should be 4 buttons (structured like homepage?)
    - Lick Library 
    - Lick Builder
    - Lick Visualizer
    - Lick Builder (Legacy): directs user to original Upload A Lick form
- rename
  - website Lick Library -> WesLicks
  - Licks -> Lick Library (displayed like Chord Gallery has a line break)
  - Both Lick Library and Chord Gallery get left aligned in the navbar.
    - when these are viewed from hamburger icon, remove line break
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
<summary>[ ] 143. </summary>

- 
</details>
