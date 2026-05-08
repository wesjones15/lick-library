package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.TabNote;
import org.jones.licklibrary.repository.LickRepository;
import org.jones.licklibrary.repository.PositionCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FindNeckPositionsTest {

    @Mock private LickRepository lickRepository;
    @Mock private PositionCacheRepository positionCacheRepository;

    private LickService lickService;

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository);
    }

    @Test
    void findNeckPositions_allResultsResolveToRequestedNote() {
        List<TabNote> positions = lickService.findNeckPositions(Note.E);
        assertFalse(positions.isEmpty());
        for (TabNote t : positions) {
            assertEquals(Note.E, t.toNote());
        }
    }

    @Test
    void findNeckPositions_correctCount() {
        // E appears on 2 open strings (0 and 5) → 3 occurrences each (frets 0, 12, 24)
        // + 4 remaining strings → 2 occurrences each = 14 total
        assertEquals(14, lickService.findNeckPositions(Note.E).size());
    }

    @Test
    void findNeckPositions_includesOpenStrings() {
        List<TabNote> positions = lickService.findNeckPositions(Note.E);
        assertTrue(positions.stream().anyMatch(t -> t.stringIndex() == 0 && t.fret() == 0));
        assertTrue(positions.stream().anyMatch(t -> t.stringIndex() == 5 && t.fret() == 0));
    }
}
