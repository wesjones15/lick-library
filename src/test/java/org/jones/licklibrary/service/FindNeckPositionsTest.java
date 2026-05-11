package org.jones.licklibrary.service;

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
        List<TabNote> positions = builder.findNeckPositions(Note.E);
        assertFalse(positions.isEmpty());
        for (TabNote t : positions) {
            assertEquals(Note.E, t.toNote());
        }
    }

    @Test
    void findNeckPositions_correctCount() {
        // E appears on 2 open strings (0 and 5) → 3 occurrences each (frets 0, 12, 24)
        // + 4 remaining strings → 2 occurrences each = 14 total
        assertEquals(14, builder.findNeckPositions(Note.E).size());
    }

    @Test
    void findNeckPositions_includesOpenStrings() {
        List<TabNote> positions = builder.findNeckPositions(Note.E);
        assertTrue(positions.stream().anyMatch(t -> t.stringIndex() == 0 && t.fret() == 0));
        assertTrue(positions.stream().anyMatch(t -> t.stringIndex() == 5 && t.fret() == 0));
    }
}
