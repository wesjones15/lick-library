package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.TabNote;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LickUtilsTest {

    // --- proximityScore ---

    @Test
    void proximityScore_samePosition() {
        TabNote n = new TabNote(2, 5, 0, null);
        assertEquals(0, LickUtils.proximityScore(n, n));
    }

    @Test
    void proximityScore_sameString() {
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(2, 8, 0, null);
        assertEquals(3, LickUtils.proximityScore(from, to));
    }

    @Test
    void proximityScore_sameFretAdjacentString() {
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(3, 5, 0, null);
        assertEquals(1, LickUtils.proximityScore(from, to));
    }

    @Test
    void proximityScore_bothDiffer() {
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(4, 7, 0, null);
        assertEquals(4, LickUtils.proximityScore(from, to));
    }

    // --- toIntervals ---

    @Test
    void toIntervals_firstNoteIsAlwaysONE() {
        // TODO
    }

    @Test
    void toIntervals_computesCorrectIntervalsFromNotes() {
        // TODO
    }

    @Test
    void toIntervals_wrapsAroundOctave() {
        // TODO
    }

    @Test
    void toIntervals_simultaneousNotesShareNormalizedColumnIndex() {
        // TODO
    }

    // --- toAbsoluteNotes ---

    @Test
    void toAbsoluteNotes_convertsIntervalsToNotesForKey() {
        // TODO
    }

    // --- hashIntervals ---

    @Test
    void hashIntervals_techniqueAgnostic() {
        // TODO
    }

    @Test
    void hashIntervals_sameIntervalsProduceSameHash() {
        // TODO
    }
}
