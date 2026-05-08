package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
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
class BuildPositionTest {

    @Mock private LickRepository lickRepository;
    @Mock private PositionCacheRepository positionCacheRepository;

    private LickService lickService;

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository);
    }

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
        assertEquals(0, position.notes().get(1).stringIndex());
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
}
