package org.jones.licklibrary.domain.lick;

import org.jones.licklibrary.domain.position.LickUtils;
import org.jones.licklibrary.domain.position.PositionCacheRepository;
import org.jones.licklibrary.domain.position.builder.CrossInstrumentPositionBuilder;
import org.jones.licklibrary.domain.position.builder.DfsPositionBuilder;
import org.jones.licklibrary.domain.shared.Interval;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.Position;
import org.jones.licklibrary.domain.shared.TabNote;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.jones.licklibrary.domain.shared.instrument.Ukulele;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FindPositionsTest {

    @Mock private LickRepository lickRepository;
    @Mock private PositionCacheRepository positionCacheRepository;

    private LickService lickService;
    private DfsPositionBuilder dfsBuilder;

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository);
        dfsBuilder = new DfsPositionBuilder();
    }

    @Test
    void findPositions_threeNoteMinorPentatonicFragmentInA() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,        null, 0),
            new IntervalNote(Interval.FLAT_THREE, null, 1),
            new IntervalNote(Interval.FIVE,       null, 2)
        );

        List<Position> positions = dfsBuilder.build(intervals, Note.A, 4, Guitar.STANDARD);

        System.out.println("findPositions — A minor pentatonic fragment (A C E): " + positions.size() + " positions");
        for (Position p : positions) {
            System.out.println(p.toTabString());
            System.out.println();
        }

        assertTrue(positions.size() >= 8, "expected at least 8 positions, got " + positions.size());
    }

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
        List<Position> positions = dfsBuilder.build(minorPentatonicFragment(), Note.A, 4, Guitar.STANDARD);
        assertFalse(positions.isEmpty());
        positions.forEach(p -> assertEquals(3, p.notes().size()));
    }

    @Test
    void findPositions_filtersPositionsExceedingFourFretSpan() {
        List<Position> positions = dfsBuilder.build(minorPentatonicFragment(), Note.A, 4, Guitar.STANDARD);
        positions.forEach(p -> assertTrue(maxFret(p) - minFret(p) <= 4,
            "position has fret span > 4: " + p.toTabString()));
    }

    @Test
    void findPositions_filtersPositionsAboveMaxFret() {
        List<Position> positions = dfsBuilder.build(minorPentatonicFragment(), Note.A, 4, Guitar.STANDARD);
        positions.forEach(p ->
            p.notes().forEach(n -> assertTrue(n.fret() <= LickService.MAX_FRET,
                "note above MAX_FRET in: " + p.toTabString())));
    }

    @Test
    void findPositions_ranksPositionsByMaxFretAscendingWithinEachStartingString() {
        List<Position> positions = dfsBuilder.build(minorPentatonicFragment(), Note.A, 4, Guitar.STANDARD);
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
        List<Position> positions = dfsBuilder.build(minorPentatonicFragment(), Note.A, 4, Guitar.STANDARD);
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
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, Note.A, Guitar.STANDARD);
        int span = notes.stream().mapToInt(TabNote::fret).max().orElse(0)
                 - notes.stream().mapToInt(TabNote::fret).min().orElse(0);
        List<Position> positions = dfsBuilder.build(intervals, Note.A, Math.max(4, span), Guitar.STANDARD);

        assertFalse(positions.isEmpty());
        assertTrue(positions.size() <= LickService.MAX_POSITIONS);
        positions.forEach(p -> assertTrue(maxFret(p) - minFret(p) <= Math.max(4, span),
            "position exceeds span limit: " + p.toTabString()));
    }

    @Test
    void crossInstrument_realWorldTab_guitarIntervalsAndUkulelePositions() {
        String rawTab =
            "e|----------------------------------|------------|\n" +
            "B|----------------------------------|------------|\n" +
            "G|----------------------------------|-----2------|\n" +
            "D|-------------------2---4---2------|------------|\n" +
            "A|--0----2---3---4------------------|------------|\n" +
            "E|----------------------------------|------------|";

        List<TabNote> notes = lickService.parseTab(rawTab);
        assertFalse(notes.isEmpty());

        Note rootKey = Note.G;  // song is in G Ionian; mirrors resolvePositions at runtime
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, rootKey, Guitar.STANDARD);

        assertFalse(intervals.isEmpty());

        List<Position> ukulelePositions = new CrossInstrumentPositionBuilder()
            .build(intervals, rootKey, Math.max(4, notes.stream().mapToInt(TabNote::fret).max().orElse(0)
                - notes.stream().mapToInt(TabNote::fret).min().orElse(0)), Ukulele.STANDARD);


        assertFalse(ukulelePositions.isEmpty(), "CrossInstrumentPositionBuilder should produce at least one ukulele position");
        int firstMaxFret = ukulelePositions.get(0).notes().stream().mapToInt(TabNote::fret).max().orElse(99);
        assertTrue(firstMaxFret <= 10, "first ukulele position should be low-register, got max fret " + firstMaxFret);

        // Assert the ukulele position reproduces the same literal pitch classes as the guitar input
        List<Note> guitarNotes = notes.stream()
            .map(n -> Guitar.STANDARD.getNoteAt(n.stringIndex(), n.fret()))
            .collect(Collectors.toList());

        List<Note> ukuleleNotes = ukulelePositions.get(0).notes().stream()
            .map(n -> Ukulele.STANDARD.getNoteAt(n.stringIndex(), n.fret()))
            .collect(Collectors.toList());

        assertEquals(guitarNotes, ukuleleNotes,
            "ukulele position should reproduce the same literal notes as the guitar input");
    }

    @Test
    void toIntervals_guitarLickKeyG_returnsExpectedIntervals() {
        String rawTab =
            "e|----------------------------------|------------|\n" +
            "B|----------------------------------|------------|\n" +
            "G|----------------------------------|-----2------|\n" +
            "D|-------------------2---4---2------|------------|\n" +
            "A|--0----2---3---4------------------|------------|\n" +
            "E|----------------------------------|------------|";

        List<TabNote> notes = lickService.parseTab(rawTab);
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, Note.G, Guitar.STANDARD);

        String actual = intervals.stream()
            .map(n -> n.interval().displayName())
            .collect(Collectors.joining(" "));

        assertEquals("2 3 4 b5 6 7 6 2", actual,
            "intervals relative to G should be: 2 3 4 b5 6 7 6 2");
    }

    @Test
    void findPositions_noDuplicateStringPatternsInSameRegion() {
        List<Position> positions = dfsBuilder.build(minorPentatonicFragment(), Note.A, 4, Guitar.STANDARD);
        Set<List<Integer>> keys = new HashSet<>();
        for (Position p : positions) {
            List<Integer> key = new ArrayList<>();
            p.notes().forEach(n -> key.add(n.stringIndex()));
            key.add(p.notes().stream().mapToInt(TabNote::fret).min().orElse(0) / 5);
            assertTrue(keys.add(key), "duplicate string pattern + region: " + p.toTabString());
        }
    }
}
