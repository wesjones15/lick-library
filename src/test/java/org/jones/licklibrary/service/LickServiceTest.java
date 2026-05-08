package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Lick;
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

    // --- findNeckPositions ---

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

    // --- proximityScore ---

    @Test
    void proximityScore_samePosition() {
        TabNote n = new TabNote(2, 5, 0, null);
        assertEquals(0, lickService.proximityScore(n, n));
    }

    @Test
    void proximityScore_sameString() {
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(2, 8, 0, null);
        assertEquals(3, lickService.proximityScore(from, to));
    }

    @Test
    void proximityScore_sameFretAdjacentString() {
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(3, 5, 0, null);
        assertEquals(1, lickService.proximityScore(from, to));
    }

    @Test
    void proximityScore_bothDiffer() {
        TabNote from = new TabNote(2, 5, 0, null);
        TabNote to   = new TabNote(4, 7, 0, null);
        assertEquals(4, lickService.proximityScore(from, to));
    }

    // --- buildPosition ---

    @Test
    void buildPosition_rootIsFirstNote() {
        TabNote root = new TabNote(0, 0, 0, null);
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,  null, 0),
            new IntervalNote(Interval.FOUR, null, 1)
        );
        List<Note> absoluteNotes = List.of(Note.E, Note.A);

        var position = lickService.buildPosition(root, intervals, absoluteNotes);

        assertNotNull(position);
        assertEquals(new TabNote(0, 0, 0, null), position.notes().get(0));
    }

    @Test
    void buildPosition_techniqueConstrainsNextNoteToSameString() {
        TabNote root = new TabNote(0, 0, 0, null);
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, "h", 0),
            new IntervalNote(Interval.TWO, null, 1)
        );
        // E → F# (2 semitones). F# on string 0 = fret 2.
        List<Note> absoluteNotes = List.of(Note.E, Note.F_SHARP);

        var position = lickService.buildPosition(root, intervals, absoluteNotes);

        assertNotNull(position);
        assertEquals(0, position.notes().get(1).stringIndex()); // forced to same string
        assertEquals(2, position.notes().get(1).fret());
    }

    @Test
    void buildPosition_columnIndexTakenFromIntervals() {
        TabNote root = new TabNote(0, 0, 0, null);
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,  null, 2),
            new IntervalNote(Interval.FOUR, null, 5)
        );
        List<Note> absoluteNotes = List.of(Note.E, Note.A);

        var position = lickService.buildPosition(root, intervals, absoluteNotes);

        assertNotNull(position);
        assertEquals(2, position.notes().get(0).columnIndex());
        assertEquals(5, position.notes().get(1).columnIndex());
    }

    @Test
    void buildPosition_greedyPicksClosestCandidate() {
        // Root: G string fret 5 = C. Next = D (TWO above C).
        // Candidates on strings 1-3: (1,5) score 1, (3,7) score 3, (2,0) score 5.
        TabNote root = new TabNote(2, 5, 0, null);
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, null, 0),
            new IntervalNote(Interval.TWO, null, 1)
        );
        List<Note> absoluteNotes = List.of(Note.C, Note.D);

        var position = lickService.buildPosition(root, intervals, absoluteNotes);

        assertNotNull(position);
        assertEquals(1, position.notes().get(1).stringIndex());
        assertEquals(5, position.notes().get(1).fret());
    }

    // --- findPositions ---

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

        assertFalse(positions.isEmpty());
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
    void findPositions_ranksPositionsBySpanAscending() {
        // TODO
    }

    @Test
    void findPositions_returnsEmptyListWhenNoValidPositionExists() {
        // TODO
    }
}
