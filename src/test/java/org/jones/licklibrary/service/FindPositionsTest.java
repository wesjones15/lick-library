package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Position;
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

    // ONE=A, FLAT_THREE=C, FIVE=E
    private static List<IntervalNote> minorPentatonicFragment() {
        return List.of(
            new IntervalNote(Interval.ONE,        null, 0),
            new IntervalNote(Interval.FLAT_THREE, null, 1),
            new IntervalNote(Interval.FIVE,       null, 2)
        );
    }

    private static int maxFret(Position p) {
        return p.notes().stream().mapToInt(TabNote::fret).max().orElse(0);
    }

    private static int minFret(Position p) {
        return p.notes().stream().mapToInt(TabNote::fret).min().orElse(0);
    }

    @Test
    void findPositions_returnsValidPositions() {
        List<Position> positions = lickService.findPositions(minorPentatonicFragment(), Note.A);
        assertFalse(positions.isEmpty());
        positions.forEach(p -> assertEquals(3, p.notes().size()));
    }

    @Test
    void findPositions_filtersPositionsExceedingFourFretSpan() {
        List<Position> positions = lickService.findPositions(minorPentatonicFragment(), Note.A);
        positions.forEach(p -> assertTrue(maxFret(p) - minFret(p) <= 4,
            "position has fret span > 4: " + p.toTabString()));
    }

    @Test
    void findPositions_filtersPositionsAboveMaxFret() {
        List<Position> positions = lickService.findPositions(minorPentatonicFragment(), Note.A);
        positions.forEach(p ->
            p.notes().forEach(n -> assertTrue(n.fret() <= LickService.MAX_FRET,
                "note above MAX_FRET in: " + p.toTabString())));
    }

    @Test
    void findPositions_ranksPositionsByMaxFretAscending() {
        List<Position> positions = lickService.findPositions(minorPentatonicFragment(), Note.A);
        for (int i = 1; i < positions.size(); i++) {
            assertTrue(maxFret(positions.get(i - 1)) <= maxFret(positions.get(i)),
                "positions not sorted at index " + i);
        }
    }
}
