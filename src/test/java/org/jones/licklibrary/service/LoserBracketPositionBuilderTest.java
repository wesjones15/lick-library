package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Position;
import org.jones.licklibrary.model.TabNote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class LoserBracketPositionBuilderTest {

    private LoserBracketPositionBuilder loserBuilder;
    private GreedyPositionBuilder greedyBuilder;

    @BeforeEach
    void setUp() {
        loserBuilder = new LoserBracketPositionBuilder();
        greedyBuilder = new GreedyPositionBuilder();
    }

    // A, C, E — all distinct column indices (no chords)
    private static List<IntervalNote> minorPentatonicFragment() {
        return List.of(
            new IntervalNote(Interval.ONE,        null, 0),
            new IntervalNote(Interval.FLAT_THREE, null, 1),
            new IntervalNote(Interval.FIVE,       null, 2)
        );
    }

    // ONE and FIVE at the same column (simultaneous), then FOUR at the next column
    private static List<IntervalNote> chordFragment() {
        return List.of(
            new IntervalNote(Interval.ONE,  null, 0),
            new IntervalNote(Interval.FIVE, null, 0),
            new IntervalNote(Interval.FOUR, null, 1)
        );
    }

    @Test
    void build_noChords_sameResultsAsGreedy() {
        List<IntervalNote> intervals = minorPentatonicFragment();

        List<Position> loser  = loserBuilder.build(intervals, Note.A, 4);
        List<Position> greedy = greedyBuilder.build(intervals, Note.A, 4);

        Set<String> loserTabs  = loser.stream().map(Position::toTabString).collect(Collectors.toSet());
        Set<String> greedyTabs = greedy.stream().map(Position::toTabString).collect(Collectors.toSet());

        assertEquals(greedyTabs, loserTabs,
            "loser bracket with no chords should produce the same positions as greedy");
    }

    @Test
    void build_chordPartnersShareColumnIndex() {
        List<Position> positions = loserBuilder.build(chordFragment(), Note.A, 4);

        assertFalse(positions.isEmpty());
        for (Position p : positions) {
            long notesAtCol0 = p.notes().stream()
                .filter(n -> n.columnIndex() == 0)
                .count();
            assertTrue(notesAtCol0 >= 2,
                "expected at least 2 notes at columnIndex=0 (chord), found " + notesAtCol0
                + " in:\n" + p.toTabString());
        }
    }

    @Test
    void build_chordPartnersOnDifferentStrings() {
        List<Position> positions = loserBuilder.build(chordFragment(), Note.A, 4);

        assertFalse(positions.isEmpty());
        for (Position p : positions) {
            // Group by columnIndex; within each group, all stringIndices must be distinct
            p.notes().stream()
                .collect(Collectors.groupingBy(TabNote::columnIndex))
                .forEach((col, notes) -> {
                    long distinctStrings = notes.stream().mapToInt(TabNote::stringIndex).distinct().count();
                    assertEquals(notes.size(), distinctStrings,
                        "two notes at columnIndex=" + col + " share a string in:\n" + p.toTabString());
                });
        }
    }

    @Test
    void build_spanConstraintRespected() {
        List<Position> positions = loserBuilder.build(chordFragment(), Note.A, 4);

        assertFalse(positions.isEmpty());
        for (Position p : positions) {
            int minFret = p.notes().stream().mapToInt(TabNote::fret).min().orElse(0);
            int maxFret = p.notes().stream().mapToInt(TabNote::fret).max().orElse(0);
            assertTrue(maxFret - minFret <= 4,
                "position exceeds 4-fret span: " + p.toTabString());
        }
    }

    @Test
    void build_partialPositionReturnedWhenPartnerUnplaceable() {
        // spanLimit=0 forces all notes in a position to share the same fret.
        // For roots where the chord partner (FIVE) also happens to sit at that fret
        // (e.g. both open strings), the partner is placed and positions have 2 notes.
        // For roots where no same-fret partner exists, the partner is silently skipped
        // and the position still contains the root (partial chord).
        // In all cases, positions must be returned and the span constraint must be respected.
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,  null, 0),
            new IntervalNote(Interval.FIVE, null, 0)
        );

        List<Position> positions = loserBuilder.build(intervals, Note.A, 0);

        assertFalse(positions.isEmpty(),
            "expected positions even with tight span constraint");
        for (Position p : positions) {
            int min = p.notes().stream().mapToInt(TabNote::fret).min().orElse(0);
            int max = p.notes().stream().mapToInt(TabNote::fret).max().orElse(0);
            assertTrue(max - min <= 0,
                "span violated at spanLimit=0: " + p.toTabString());
            assertTrue(p.notes().size() >= 1,
                "position must contain at least the root note");
        }
    }
}
