package org.jones.licklibrary.domain.position;

import org.jones.licklibrary.domain.shared.Interval;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Mode;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.TabNote;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LickUtilsTest {

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
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(4, 7, 0, null);
        assertEquals(Math.sqrt(8), LickUtils.proximityScore(from, to), 1e-9);
    }

    @Test
    void toIntervals_usesProvidedRootKeyInsteadOfFirstNote() {
        List<TabNote> notes = List.of(
            new TabNote(4, 0, 0, null),
            new TabNote(4, 2, 1, null)
        );
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, Note.C, Guitar.STANDARD);
        assertEquals(Interval.SIX,   intervals.get(0).interval());
        assertEquals(Interval.SEVEN, intervals.get(1).interval());
    }

    @Test
    void toIntervals_firstNoteIsONE_whenRootMatchesFirstNote() {
        List<TabNote> notes = List.of(new TabNote(1, 2, 0, null));
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, Note.C_SHARP, Guitar.STANDARD);
        assertEquals(Interval.ONE, intervals.get(0).interval());
    }

    @Test
    void toIntervals_computesCorrectIntervalsFromNotes() {
        List<TabNote> notes = List.of(
            new TabNote(1, 0, 0, null),
            new TabNote(1, 2, 1, null),
            new TabNote(1, 4, 2, null)
        );
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, Note.B, Guitar.STANDARD);
        assertEquals(3, intervals.size());
        assertEquals(Interval.ONE,   intervals.get(0).interval());
        assertEquals(Interval.TWO,   intervals.get(1).interval());
        assertEquals(Interval.THREE, intervals.get(2).interval());
    }

    @Test
    void toIntervals_wrapsAroundOctave() {
        List<TabNote> notes = List.of(
            new TabNote(1, 0, 0, null),
            new TabNote(4, 0, 1, null)
        );
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, Note.B, Guitar.STANDARD);
        assertEquals(Interval.FLAT_SEVEN, intervals.get(1).interval());
    }

    @Test
    void toIntervals_simultaneousNotesShareNormalizedColumnIndex() {
        List<TabNote> notes = List.of(
            new TabNote(0, 0, 2, null),
            new TabNote(1, 0, 2, null)
        );
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, Note.E, Guitar.STANDARD);
        assertEquals(2, intervals.size());
        assertEquals(intervals.get(0).columnIndex(), intervals.get(1).columnIndex());
    }

    @Test
    void toAbsoluteNotes_convertsIntervalsToNotesForKey() {
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
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,        null, 0),
            new IntervalNote(Interval.FLAT_SEVEN, null, 1)
        );
        assertEquals(Mode.AEOLIAN, LickUtils.detectMode(intervals));
    }

    @Test
    void detectMode_phrygianWhenFlatTwo() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,      null, 0),
            new IntervalNote(Interval.FLAT_TWO, null, 1)
        );
        assertEquals(Mode.PHRYGIAN, LickUtils.detectMode(intervals));
    }

    @Test
    void detectMode_locrianWhenFlatTwoAndFlatFive() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,       null, 0),
            new IntervalNote(Interval.FLAT_TWO,  null, 1),
            new IntervalNote(Interval.FLAT_FIVE, null, 2)
        );
        assertEquals(Mode.LOCRIAN, LickUtils.detectMode(intervals));
    }
}
