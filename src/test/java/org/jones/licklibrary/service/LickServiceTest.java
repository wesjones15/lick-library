package org.jones.licklibrary.service;

import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Lick;
import org.jones.licklibrary.model.TabNote;
import org.jones.licklibrary.repository.LickRepository;
import org.jones.licklibrary.repository.PositionCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
        String inputTab =
                "E|------------------------------------|\n" +
                "B|------1-----------------------------|\n" +
                "G|--1/2-----2p0---0-------------------|\n" +
                "D|--------------3---3-0---------------|\n" +
                "A|------------------------0--3--5-----|\n" +
                "E|------------------------------------|";
        List<TabNote> notes = lickService.parseTab(inputTab);

    }

    @Test
    void parseTab_SimpleSample() {
        String inputTab =
                "E|----------------|\n" +
                "B|------1---------|\n" +
                "G|--1/2-----2p0---|\n" +
                "D|--------------3-|\n" +
                "A|----------------|\n" +
                "E|----------------|";
        List<TabNote> notes = lickService.parseTab(inputTab);

        System.out.println(notes.toString());
        System.out.println(lickService.toNoteString(notes));

    }

    @Test
    void test_toIntervals_simple_sample() {
        String inputTab =
                "E|----------------|\n" +
                "B|------1---------|\n" +
                "G|--1/2-----2p0---|\n" +
                "D|--------------3-|\n" +
                "A|----------------|\n" +
                "E|----------------|";
        List<TabNote> notes = lickService.parseTab(inputTab);
        List<IntervalNote> intervals = lickService.toIntervals(notes);
        System.out.println(intervals.toString());
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
