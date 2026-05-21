- The purpose of this file is to hold ideas in memory so we can easily refer to them in claude code ui.
- key: [ ]  = open, [x] = complete, [~] = deferred
---

songs that should have variable font size throughout seem to have a uniform font size applied

- noodle loadsong keeps reloading to plain noodle whenever i swipe away and swipe back
  - Im not triggering reload, this is happening passively, 

<details open>
<summary>Pending</summary>

<details>
<summary>[ ] 137. (Songs) backend parser logic or frontend font?</summary>

- column view font size issue:
  - song parser seems to be forcing a uniform font
    - seems to be a smaller font than necessary
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
  - create crossInstrumentPositionBuilder class
    - takes in 2 instruments/tunings: input and output
    - takes tab from tuningA, converts to instrument-agnostic intermediate object collection (TabNote?)
      - takes intermediate object collection and tuningB
        - builds positions for tuning B
        - need to allow jumps to farther notes than standard positionBuilder allows for
          - this is to increase number of positions produced
          - also to increase possible positions generated at low frets
    - for licks that are converted from standard to another instrument, this builder will be preferred
    - it should reduce chords (dedupe intervals in a column)
    - it will be less strict wrt reach during traversal, since caged doesn't apply to other instruments/tunings
- case
  - if
    - SongDetail page is open
    - Exp Tab button is active
    - instrument other than standard guitar is selected
  - then
    - for a detected lick
      - if no positions exist or no positions can be generated
        - show an empty tab with the correct strings for selected instrument
        - show a little error message below it in small red text,
          - no valid positions found for lick with <instrument>
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
<summary>[ ] 148. (Housekeeping) ui/ux enhancements</summary>

- in miniview
  - hide add to playlist behind more options toggle
  - add a logo icon to left of song/artist name
    - clicking logo takes you home
- add Noodle tab to Home
- iphone ui needs another pass across board for toolbars, options,dropdowns,etc
- get rid of Live tab

  - 

</details>

<details>
<summary>[ ] 153. (Noodle) </summary>

- [ ] KaraokeDisplay Wishlist Items
  - make karaoke display scrollable.
  - clicking a line should set song to start from there on play (after 4 beat leadin)
</details>


<details>
<summary>[ ] 150. (Songs) use noodle karaoke feature elsewhere</summary>

- add another play button to song detail page. this will do what noodle page does, but in scroll view
</details>

<details>
<summary>[ ] 151. (Development) Reduce Repetition</summary>

- can we dive in the frontend and see whether we've introduced any redundancy or repetition in the music consts
  - consolidate and reduce frontend methods,etc where possible
  - update readme
- check backend for repetition
  - consolidate and reduce backend methods, classes where possible
  - update readme
- since live and theory and licks all got moved around
  - perhaps rename files and directories to reflect new functionality. 
  - 
</details>

<details>
<summary>[ ] 156. (Position) CrossInstrumentPositionBuilder testing</summary>

- I want crossInstrument_realWorldTab_guitarIntervalsAndUkulelePositions to compare 
  - the notes in the Guitar.STANDARD rawtab input lick
  - the notes in the Ukulele.STANDARD output lick
  - I added sout to the tests and got the raw tabs. 
- current input
  e|----------------------------------|------------|
  B|----------------------------------|------------|
  G|----------------------------------|-----2------|
  D|-------------------2---4---2------|------------|
  A|--0----2---3---4------------------|------------|
  E|----------------------------------|------------|
- current output
  A|-2-------0-2-0---|
  E|---------------0-|
  C|---1-2-3---------|
  g|-----------------|
- notice that the test currently passes - it shouldn't pass with the output the method is producing
  - if you look at what it is comparing, first note of each: A vs G
  - these 2 tabs don't produce identical intervals wrt key of G
  - redesign this test so that it only asserts what i asked.
    - assert that the literal notes between the 2 tabs are the same
      - this test should FAIL
- both licks are returning this string for LickUtils.toIntervals()
  - 3 b5 5 b6 2 3 2 6
  - this is incorrect.
  - the song is in G Ionian
  - the Guitar.STANDARD lick should evaluate to 
    - 2 3 4 b5 6 7 6 2
  - the ukulele lick (incorrect position) evaluates to
    - 3 b5 5 b6 2 3 2 6
  - write a test for toIntervals.
    - assert input guitar lick produces "2 3 4 b5 6 7 6 2" intervals
      System.out.println(rawTab+"\n");
      System.out.println(intervals.stream()
      .map(IntervalNote::toString)
      .collect(Collectors.joining(" ")));
      System.out.println("\n\n");
      System.out.println(ukulelePositions.get(0).toTabString(Ukulele.STANDARD)+"\n");
      System.out.println(ukuleleIntervals.stream()
      .map(IntervalNote::toString)
      .collect(Collectors.joining(" ")));
- toIntervals uses TabNotes. 
  - TabNote pojo was designed when the app was only for Guitar.STANDARD.
  - I want toIntervals to be mode aware and key aware. 
  - 
</details>
Case: song is key G. song contains tab with guitar.STANDARD. tab begins with first note A. when converted to ukulele, output tab is first note G. why is the ukulele tab not
  showing A as first note? this is not related to uploadSongLick. this is referring to the ukulele lick that is generated when i change the intrument on song detail page while exp
  tab feature is active
the original tab in Guitar.STANDARD, was already known to be parsed correctly into accurate intervals relative to song key, in toIntervals. the issue lies in
converting it to ukulele. The song is in G. The guitar tab starts with the note A. the ukulele tab that is produced, starts on the note G. it should start on the note A.

<details>
<summary>[ ] 157. (Position) TabNote, LickUtils.toIntervals</summary>

- rewrite the basic position methods to be instrument agnostic.
- we need to pass the instrument into getNoteAt as an arg. 
  - that way we can retrieve the tuning, which gives stringIndex the context it needs.
- or should it take a string root note instead? 
  - then the tuning/instrument is external to the getNoteAt call.
</details>


</details>



<details Deferred>
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
<summary>[ ] 157. </summary>

- 
</details>