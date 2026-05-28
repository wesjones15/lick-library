- The purpose of this file is to hold ideas in memory so we can easily refer to them in claude code ui.
- key: [ ]  = open, [x] = complete, [~] = deferred
---

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
<summary>[ ] 145. (Licks) Renaming Lick Things</summary>

- rename
  - website Lick Library -> WesLicks
  - Licks -> Lick Library (displayed like Chord Gallery has a line break)
    - Both Lick Library and Chord Gallery get left aligned in the navbar.
      - when these are viewed from hamburger icon, remove line break
</details>

<details>
<summary>[ ] 148. (UI/UX) Miniview enhancements</summary>

- in miniview
  - hide add to playlist behind more options toggle
  - add a logo icon to left of song/artist name
    - clicking logo takes you home
- add Noodle tab to Home
- iphone ui needs another pass across board for toolbars, options,dropdowns,etc
- get rid of Live tab
</details>

<details>
<summary>[ ] 150. (Songs) Integrate GuitarKaraoke View in SongDetail Page</summary>

- add another play button to song detail page. this will do what noodle page does, but in scroll view
- This will allow users to use Noodle mode's features while in playlist mode
  - maybe just add option for playlist controls to work in Noodle
    - open playlist in noddle mode
    - add bpm offset to things user can modify on playlist song
      - necessary because bpm is used practically for noodle playback
  - that way we can keep Noodle separate from Playlist/Song page
</details>

<details>
<summary>[ ] 161. (Songs/Licks/Position?) Song Lick Off By One Bug</summary>

- some offset is being applied where it shouldnt
- colder weather 
  - key G# G capo 1
  - lick starts G|-2-
  - Uke conversion starts g|-1-
  - off by 1
  - is it due to capo offset being double applied?
</details>

<details>
<summary>[ ] 163. (UI/UX) Post-Release UI/UX Pass</summary>

- [ ] Lick Builder (defer)
- [ ] Lick Visualizer (defer)
- [x] Account Page
  - Account in Navbar (make it an icon button with dropdown including Profile and Sign Out)
  - we previously made clicking Admin/Account reload user page.
    - since we are moving user nav button into dropdown,
    - lets add a refresh button into the account page
      - Users will see the refresh icon near the account status banner
        - clicking will refresh status of user account
      - Admin will see the refresh icon in row with Approval Queue
        - clicking will refresh queue and userlist
  - add manage button/toggle to user account page
    - hide delete button behind manage
    - allow user to change username by clicking pencil icon next to username when manage is active
    - delete account should be greyed out for admin (maybe just for user_id=1 admin)
- [x] Song Card
  - don't show uploader username
</details>

<details>
<summary>[ ] 165. (Noodle) Add Lick Parsing to Noodle Mode</summary>

- currently, Noodle LoadSong mode will ignore and skip over guitar tab snippets
  - but Lick logic already parses and interprets tab blocks detected in song view
- I want Noodle LoadSong to get a toggle that, when enabled,
  - will integrate detected songlicks into LoadSong playback
  - use Lick Visualizer logic to play lick back on GuitarNeck in time with metronome, in the song itself
    - so instead of solos just showing the rhythm chords' intervals,
      - it will playback the solo in realtime with song

- also if possible
  - add toggle icon button
  - when enabled, 
    - play corresponding MIDI note during playback,
      - but only while GuitarNeck is displayed
</details>

<details>
<summary>[ ] 166. (Fork) Make App Use Local Device Storage</summary>

- don't do this idea til more features are implemented and bugs are cleared
  - this will be a fork, not the original
  - use local storage (not browser cache for db
- maybe remove auth and users and make it an offline app that uses ipad storage(not browser cache) to store song data, then distribute it like ds download play used to be used for demo distribution.
</details>


</details>



- what if we use x2 crap to compress long tabs
- detect duplicate section blocks (groups of chordlyrics)
  - remove and reference the original
  - maybe only for columns mode in songdetail idk

- add Eb tuning as an instrument.
- 




<details Deferred>
<summary>Deferred</summary>

<details>
<summary>[~] 151. (Development) Reduce Repetition</summary>

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

<details>
<summary>[~] 153. (Noodle) Make KaraokeDisplay Scrollable</summary>

- KaraokeDisplay Wishlist Items
  - make karaoke display scrollable.
  - clicking a line should set song to start from there on play (after 4 beat leadin)
  - when karaoke display moves to next line,
    - is it possible to smooth animate it?
</details>


</details>

<details>
<summary>[ ] 169. </summary>

- 
</details>