package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Guitar;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.TabNote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FindNeckPositionsTest {

    private PositionBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new GreedyPositionBuilder();
    }

    @Test
    void findNeckPositions_allResultsResolveToRequestedNote() {
        List<TabNote> positions = builder.findNeckPositions(Note.E, Guitar.STANDARD);
        assertFalse(positions.isEmpty());
        for (TabNote t : positions) {
            assertEquals(Note.E, Guitar.STANDARD.getNoteAt(t.stringIndex(), t.fret()));
        }
    }

    @Test
    void findNeckPositions_correctCount() {
        // MAX_FRET=15; E within frets 0-15 per string:
        // str0(E): 0,12 → 2 | str1(A): 7 → 1 | str2(D): 2,14 → 2
        // str3(G): 9 → 1 | str4(B): 5 → 1 | str5(E): 0,12 → 2 → total 9
        assertEquals(9, builder.findNeckPositions(Note.E, Guitar.STANDARD).size());
    }

    @Test
    void findNeckPositions_includesOpenStrings() {
        List<TabNote> positions = builder.findNeckPositions(Note.E, Guitar.STANDARD);
        assertTrue(positions.stream().anyMatch(t -> t.stringIndex() == 0 && t.fret() == 0));
        assertTrue(positions.stream().anyMatch(t -> t.stringIndex() == 5 && t.fret() == 0));
    }
}
