package org.jones.licklibrary.domain.position;

import org.jones.licklibrary.domain.position.builder.GreedyPositionBuilder;
import org.jones.licklibrary.domain.position.builder.LoserBracketPositionBuilder;
import org.jones.licklibrary.domain.shared.Interval;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.Position;
import org.jones.licklibrary.domain.shared.TabNote;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
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

    private static List<IntervalNote> minorPentatonicFragment() {
        return List.of(
            new IntervalNote(Interval.ONE,        null, 0),
            new IntervalNote(Interval.FLAT_THREE, null, 1),
            new IntervalNote(Interval.FIVE,       null, 2)
        );
    }

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

        List<Position> loser  = loserBuilder.build(intervals, Note.A, 4, Guitar.STANDARD);
        List<Position> greedy = greedyBuilder.build(intervals, Note.A, 4, Guitar.STANDARD);

        Set<String> loserTabs  = loser.stream().map(Position::toTabString).collect(Collectors.toSet());
        Set<String> greedyTabs = greedy.stream().map(Position::toTabString).collect(Collectors.toSet());

        assertEquals(greedyTabs, loserTabs,
            "loser bracket with no chords should produce the same positions as greedy");
    }

    @Test
    void build_chordPartnersShareColumnIndex() {
        List<Position> positions = loserBuilder.build(chordFragment(), Note.A, 4, Guitar.STANDARD);

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
        List<Position> positions = loserBuilder.build(chordFragment(), Note.A, 4, Guitar.STANDARD);

        assertFalse(positions.isEmpty());
        for (Position p : positions) {
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
        List<Position> positions = loserBuilder.build(chordFragment(), Note.A, 4, Guitar.STANDARD);

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
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,  null, 0),
            new IntervalNote(Interval.FIVE, null, 0)
        );

        List<Position> positions = loserBuilder.build(intervals, Note.A, 0, Guitar.STANDARD);

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
