package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Mode;
import org.jones.licklibrary.model.TabNote;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LickUtilsTest {

    // --- proximityScore ---

    @Test
    void proximityScore_samePosition() {
        TabNote n = new TabNote(2, 5, 0, null);
        assertEquals(0.0, LickUtils.proximityScore(n, n), 1e-9);
    }

    @Test
    void proximityScore_sameString() {
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(2, 8, 0, null);
        assertEquals(3.0, LickUtils.proximityScore(from, to), 1e-9);
    }

    @Test
    void proximityScore_sameFretAdjacentString() {
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(3, 5, 0, null);
        assertEquals(1.0, LickUtils.proximityScore(from, to), 1e-9);
    }

    @Test
    void proximityScore_bothDiffer() {
        // fret delta 2, string delta 2 → hypot(2,2) = sqrt(8)
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(4, 7, 0, null);
        assertEquals(Math.sqrt(8), LickUtils.proximityScore(from, to), 1e-9);
    }

    // --- toIntervals ---

    @Test
    void toIntervals_usesProvidedRootKeyInsteadOfFirstNote() {
        // A string open = A (ordinal 9), fret 2 = B (ordinal 11). Root key = C (ordinal 0).
        // A relative to C: (9 - 0 + 12) % 12 = 9 = SIX
        // B relative to C: (11 - 0 + 12) % 12 = 11 = SEVEN
        List<TabNote> notes = List.of(
            new TabNote(1, 0, 0, null), // A
            new TabNote(1, 2, 1, null)  // B
        );
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, Note.C);
        assertEquals(Interval.SIX,   intervals.get(0).interval());
        assertEquals(Interval.SEVEN, intervals.get(1).interval());
    }

    @Test
    void toIntervals_firstNoteIsAlwaysONE() {
        List<TabNote> notes = List.of(new TabNote(4, 2, 0, null)); // A string fret 2 = B
        List<IntervalNote> intervals = LickUtils.toIntervals(notes);
        assertEquals(Interval.ONE, intervals.get(0).interval());
    }

    @Test
    void toIntervals_computesCorrectIntervalsFromNotes() {
        // A string: open=A, fret2=B, fret4=C# → ONE, TWO, THREE
        List<TabNote> notes = List.of(
            new TabNote(4, 0, 0, null),
            new TabNote(4, 2, 1, null),
            new TabNote(4, 4, 2, null)
        );
        List<IntervalNote> intervals = LickUtils.toIntervals(notes);
        assertEquals(3, intervals.size());
        assertEquals(Interval.ONE,   intervals.get(0).interval());
        assertEquals(Interval.TWO,   intervals.get(1).interval());
        assertEquals(Interval.THREE, intervals.get(2).interval());
    }

    @Test
    void toIntervals_wrapsAroundOctave() {
        // string 4 (B) fret 0 = B (ordinal 11), string 1 (A) fret 0 = A (ordinal 9)
        // (9 - 11 + 12) % 12 = 10 = FLAT_SEVEN
        List<TabNote> notes = List.of(
            new TabNote(4, 0, 0, null), // B string → B
            new TabNote(1, 0, 1, null)  // A string → A
        );
        List<IntervalNote> intervals = LickUtils.toIntervals(notes);
        assertEquals(Interval.FLAT_SEVEN, intervals.get(1).interval());
    }

    @Test
    void toIntervals_simultaneousNotesShareNormalizedColumnIndex() {
        List<TabNote> notes = List.of(
            new TabNote(0, 0, 2, null), // same raw columnIndex
            new TabNote(1, 0, 2, null)
        );
        List<IntervalNote> intervals = LickUtils.toIntervals(notes);
        assertEquals(2, intervals.size());
        assertEquals(intervals.get(0).columnIndex(), intervals.get(1).columnIndex());
    }

    // --- toAbsoluteNotes ---

    @Test
    void toAbsoluteNotes_convertsIntervalsToNotesForKey() {
        // ONE, TWO, THREE in key A → A, B, C#
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,   null, 0),
            new IntervalNote(Interval.TWO,   null, 1),
            new IntervalNote(Interval.THREE, null, 2)
        );
        List<Note> notes = LickUtils.toAbsoluteNotes(intervals, Note.A);
        assertEquals(3, notes.size());
        assertEquals(Note.A,       notes.get(0));
        assertEquals(Note.B,       notes.get(1));
        assertEquals(Note.C_SHARP, notes.get(2));
    }

    // --- hashIntervals ---

    @Test
    void hashIntervals_techniqueAgnostic() {
        List<IntervalNote> withTechnique    = List.of(new IntervalNote(Interval.ONE, "h", 0),
                                                      new IntervalNote(Interval.TWO, "/", 1));
        List<IntervalNote> withoutTechnique = List.of(new IntervalNote(Interval.ONE, null, 0),
                                                      new IntervalNote(Interval.TWO, null, 1));
        assertEquals(LickUtils.hashIntervals(withTechnique), LickUtils.hashIntervals(withoutTechnique));
    }

    @Test
    void hashIntervals_sameIntervalsProduceSameHash() {
        List<IntervalNote> a = List.of(new IntervalNote(Interval.ONE,  null, 0),
                                       new IntervalNote(Interval.FIVE, null, 1));
        List<IntervalNote> b = List.of(new IntervalNote(Interval.ONE,  null, 0),
                                       new IntervalNote(Interval.FIVE, null, 1));
        assertEquals(LickUtils.hashIntervals(a), LickUtils.hashIntervals(b));
    }

    // --- detectMode ---

    @Test
    void detectMode_ionianWhenNoFlats() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,   null, 0),
            new IntervalNote(Interval.TWO,   null, 1),
            new IntervalNote(Interval.THREE, null, 2)
        );
        assertEquals(Mode.IONIAN, LickUtils.detectMode(intervals));
    }

    @Test
    void detectMode_aeolianWhenFlatSeven() {
        // b7 eliminates IONIAN and LYDIAN; tiebreak among remaining picks AEOLIAN
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,        null, 0),
            new IntervalNote(Interval.FLAT_SEVEN, null, 1)
        );
        assertEquals(Mode.AEOLIAN, LickUtils.detectMode(intervals));
    }

    @Test
    void detectMode_phrygianWhenFlatTwo() {
        // b2 eliminates IONIAN, DORIAN, LYDIAN, MIXOLYDIAN, AEOLIAN → PHRYGIAN wins tiebreak over LOCRIAN
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,      null, 0),
            new IntervalNote(Interval.FLAT_TWO, null, 1)
        );
        assertEquals(Mode.PHRYGIAN, LickUtils.detectMode(intervals));
    }

    @Test
    void detectMode_locrianWhenFlatTwoAndFlatFive() {
        // b2 → {PHRYGIAN, LOCRIAN}; b5 further eliminates PHRYGIAN → LOCRIAN uniquely
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,      null, 0),
            new IntervalNote(Interval.FLAT_TWO, null, 1),
            new IntervalNote(Interval.FLAT_FIVE, null, 2)
        );
        assertEquals(Mode.LOCRIAN, LickUtils.detectMode(intervals));
    }
}
