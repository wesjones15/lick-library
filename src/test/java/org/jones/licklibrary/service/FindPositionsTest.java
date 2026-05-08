package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Position;
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
class FindPositionsTest {

    @Mock private LickRepository lickRepository;
    @Mock private PositionCacheRepository positionCacheRepository;

    private LickService lickService;

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository);
    }

    @Test
    void findPositions_threeNoteMinorPentatonicFragmentInA() {
        // ONE=A, FLAT_THREE=C, FIVE=E
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,        null, 0),
            new IntervalNote(Interval.FLAT_THREE, null, 1),
            new IntervalNote(Interval.FIVE,       null, 2)
        );

        List<Position> positions = lickService.findPositions(intervals, Note.A);

        System.out.println("findPositions — A minor pentatonic fragment (A C E): " + positions.size() + " positions");
        for (Position p : positions) {
            System.out.println(p.toTabString());
            System.out.println();
        }

        assertEquals(8, positions.size());
    }

    @Test
    void findPositions_returnsValidPositions() {
        // TODO
    }

    @Test
    void findPositions_filtersPositionsExceedingFourFretSpan() {
        // TODO
    }

    @Test
    void findPositions_filtersPositionsAboveMaxFret() {
        // TODO
    }

    @Test
    void findPositions_ranksPositionsByMaxFretAscending() {
        // TODO
    }

    @Test
    void findPositions_returnsEmptyListWhenNoValidPositionExists() {
        // TODO
    }
}
