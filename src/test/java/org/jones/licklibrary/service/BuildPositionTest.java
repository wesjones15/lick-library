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
    void buildPositions_rootIsFirstNote() {
        TabNote root = new TabNote(0, 0, 0, null);
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,  null, 0),
            new IntervalNote(Interval.FOUR, null, 1)
        );
        List<Note> absoluteNotes = List.of(Note.E, Note.A);

        List<Position> positions = new ArrayList<>();
        lickService.buildPositions(root, intervals, absoluteNotes, 4, positions);

        assertFalse(positions.isEmpty());
        assertEquals(new TabNote(0, 0, 0, null), positions.get(0).notes().get(0));
    }

    @Test
    void buildPositions_techniqueConstrainsNextNoteToSameString() {
        TabNote root = new TabNote(0, 0, 0, null);
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, "h", 0),
            new IntervalNote(Interval.TWO, null, 1)
        );
        // E → F# (2 semitones). F# on string 0 = fret 2.
        List<Note> absoluteNotes = List.of(Note.E, Note.F_SHARP);

        List<Position> positions = new ArrayList<>();
        lickService.buildPositions(root, intervals, absoluteNotes, 4, positions);

        assertFalse(positions.isEmpty());
        positions.forEach(p -> {
            assertEquals(0, p.notes().get(1).stringIndex());
            assertEquals(2, p.notes().get(1).fret());
        });
    }

    @Test
    void buildPositions_columnIndexTakenFromIntervals() {
        TabNote root = new TabNote(0, 0, 0, null);
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,  null, 2),
            new IntervalNote(Interval.FOUR, null, 5)
        );
        List<Note> absoluteNotes = List.of(Note.E, Note.A);

        List<Position> positions = new ArrayList<>();
        lickService.buildPositions(root, intervals, absoluteNotes, 4, positions);

        assertFalse(positions.isEmpty());
        assertEquals(2, positions.get(0).notes().get(0).columnIndex());
        assertEquals(5, positions.get(0).notes().get(1).columnIndex());
    }

    @Test
    void buildPositions_returnsAllValidPaths() {
        // Root: G string (2) fret 5 = C. Next note = D.
        // With all-strings search, D is reachable on multiple strings within the 4-fret span.
        TabNote root = new TabNote(2, 5, 0, null);
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, null, 0),
            new IntervalNote(Interval.TWO, null, 1)
        );
        List<Note> absoluteNotes = List.of(Note.C, Note.D);

        List<Position> positions = new ArrayList<>();
        lickService.buildPositions(root, intervals, absoluteNotes, 4, positions);

        assertTrue(positions.size() > 1, "expected multiple paths, got " + positions.size());
        // Closest candidate (string 1 fret 5, distance 1.0) should be among results
        boolean hasClosest = positions.stream()
            .anyMatch(p -> p.notes().get(1).stringIndex() == 1 && p.notes().get(1).fret() == 5);
        assertTrue(hasClosest, "expected closest candidate (string 1 fret 5) in results");
    }
}
