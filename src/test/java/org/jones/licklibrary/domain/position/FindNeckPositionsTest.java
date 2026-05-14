package org.jones.licklibrary.domain.position;

import org.jones.licklibrary.domain.position.builder.GreedyPositionBuilder;
import org.jones.licklibrary.domain.position.builder.PositionBuilder;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.TabNote;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
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
        assertEquals(9, builder.findNeckPositions(Note.E, Guitar.STANDARD).size());
    }

    @Test
    void findNeckPositions_includesOpenStrings() {
        List<TabNote> positions = builder.findNeckPositions(Note.E, Guitar.STANDARD);
        assertTrue(positions.stream().anyMatch(t -> t.stringIndex() == 0 && t.fret() == 0));
        assertTrue(positions.stream().anyMatch(t -> t.stringIndex() == 5 && t.fret() == 0));
    }
}
