package org.jones.licklibrary.service;

import org.jones.licklibrary.repository.LickRepository;
import org.jones.licklibrary.repository.PositionCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LickServiceTest {

    @Mock private LickRepository lickRepository;
    @Mock private PositionCacheRepository positionCacheRepository;

    private LickService lickService;

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository);
    }

    // --- parseTab ---

    @Test
    void parseTab_Sample() {
        String inputTab = "";

    }

    @Test
    void parseTab_handlesTwoDigitFrets() {
        // TODO
    }

    @Test
    void parseTab_recordsTechniqueCharacters() {
        // TODO
    }

    @Test
    void parseTab_simultaneousNotesTakesFirst() {
        // TODO
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

    // --- uploadLick ---

    @Test
    void uploadLick_storesNewLick() {
        // TODO
    }

    @Test
    void uploadLick_doesNotDuplicateExistingLick() {
        // TODO
    }

    // --- getLicks / resolvePositions ---

    @Test
    void getLicks_returnsCachedPositionsOnHit() {
        // TODO
    }

    @Test
    void getLicks_computesAndCachesPositionsOnMiss() {
        // TODO
    }

    // --- findPositions ---

    @Test
    void findPositions_returnsValidPositions() {
        // TODO
    }

    @Test
    void findPositions_filtersPositionsExceedingFourFretSpan() {
        // TODO
    }

    @Test
    void findPositions_ranksPositionsBySpanAscending() {
        // TODO
    }

    @Test
    void findPositions_returnsEmptyListWhenNoValidPositionExists() {
        // TODO
    }
}
