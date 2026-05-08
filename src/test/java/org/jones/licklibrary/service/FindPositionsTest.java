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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        assertTrue(positions.size() >= 8, "expected at least 8 positions, got " + positions.size());
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
    void findPositions_ranksPositionsByMaxFretAscendingWithinEachStartingString() {
        List<Position> positions = lickService.findPositions(minorPentatonicFragment(), Note.A);
        Map<Integer, Integer> lastMaxFret = new HashMap<>();
        for (Position p : positions) {
            int startString = p.notes().get(0).stringIndex();
            int mf = maxFret(p);
            int prev = lastMaxFret.getOrDefault(startString, 0);
            assertTrue(mf >= prev,
                "max-fret not ascending within starting string " + startString + ": " + mf + " < " + prev);
            lastMaxFret.put(startString, mf);
        }
    }

    @Test
    void findPositions_firstRoundHasDistinctStartingStrings() {
        List<Position> positions = lickService.findPositions(minorPentatonicFragment(), Note.A);
        long distinctStarts = positions.stream()
            .map(p -> p.notes().get(0).stringIndex()).distinct().count();
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < Math.min((int) distinctStarts, positions.size()); i++) {
            int s = positions.get(i).notes().get(0).stringIndex();
            assertTrue(seen.add(s), "duplicate starting string at index " + i + ": string " + s);
        }
    }

    @Test
    void findPositions_longLickProducesPositions() {
        String rawTab =
            "e|------------|--------------|--------------|----------|\n" +
            "B|------------|--------------|--------------|----------|\n" +
            "G|------------|--------------|--------------|----------|\n" +
            "D|------------|--------------|--------------|----------|\n" +
            "A|-----0-2/4--|-4\\2-0-2/4\\2--|-2h4-2h4-4\\2--|-2/4\\2-0--|\n" +
            "E|-0-2--------|--------------|--------------|------4/5-|";

        List<TabNote> notes = lickService.parseTab(rawTab);
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, Note.A);
        int span = notes.stream().mapToInt(TabNote::fret).max().orElse(0)
                 - notes.stream().mapToInt(TabNote::fret).min().orElse(0);
        List<Position> positions = lickService.findPositions(intervals, Note.A, Math.max(4, span));

        assertFalse(positions.isEmpty());
        assertTrue(positions.size() <= LickService.MAX_POSITIONS);
        positions.forEach(p -> assertTrue(maxFret(p) - minFret(p) <= Math.max(4, span),
            "position exceeds span limit: " + p.toTabString()));
    }

    @Test
    void findPositions_noDuplicateStringPatternsInSameRegion() {
        List<Position> positions = lickService.findPositions(minorPentatonicFragment(), Note.A);
        Set<List<Integer>> keys = new HashSet<>();
        for (Position p : positions) {
            List<Integer> key = new ArrayList<>();
            p.notes().forEach(n -> key.add(n.stringIndex()));
            key.add(p.notes().stream().mapToInt(TabNote::fret).min().orElse(0) / 5);
            assertTrue(keys.add(key), "duplicate string pattern + region: " + p.toTabString());
        }
    }

}
