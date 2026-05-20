package org.jones.licklibrary.domain.position;

import org.jones.licklibrary.domain.position.builder.CrossInstrumentPositionBuilder;
import org.jones.licklibrary.domain.position.builder.PositionBuilder;
import org.jones.licklibrary.domain.shared.Interval;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.Position;
import org.jones.licklibrary.domain.shared.instrument.Mandolin;
import org.jones.licklibrary.domain.shared.instrument.Ukulele;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrossInstrumentPositionBuilderTest {

    private CrossInstrumentPositionBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new CrossInstrumentPositionBuilder();
    }

    @Test
    void ukulele_pentatonicMinor_producesLowPositions() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,        null, 0),
            new IntervalNote(Interval.FLAT_THREE, null, 1),
            new IntervalNote(Interval.FOUR,       null, 2),
            new IntervalNote(Interval.FIVE,       null, 3),
            new IntervalNote(Interval.FLAT_SEVEN, null, 4)
        );

        List<Position> positions = builder.build(intervals, Note.E, 4, Ukulele.STANDARD);

        assertFalse(positions.isEmpty(), "Should find positions on ukulele");
        Position first = positions.get(0);
        int maxFret = first.notes().stream().mapToInt(n -> n.fret()).max().orElse(99);
        assertTrue(maxFret <= 7, "First position should be in low register (max fret ≤ 7), got " + maxFret);
    }

    @Test
    void mandolin_majorScale_producesPositions() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,   null, 0),
            new IntervalNote(Interval.TWO,   null, 1),
            new IntervalNote(Interval.THREE, null, 2),
            new IntervalNote(Interval.FOUR,  null, 3),
            new IntervalNote(Interval.FIVE,  null, 4)
        );

        List<Position> positions = builder.build(intervals, Note.A, 4, Mandolin.STANDARD);

        assertFalse(positions.isEmpty(), "Should find positions on mandolin");
    }

    @Test
    void noPositionExceedsMaxFret() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE,        null, 0),
            new IntervalNote(Interval.FLAT_THREE, null, 1),
            new IntervalNote(Interval.FIVE,       null, 2)
        );

        List<Position> positions = builder.build(intervals, Note.C, 4, Ukulele.STANDARD);

        positions.forEach(p -> p.notes().forEach(note ->
            assertTrue(note.fret() <= PositionBuilder.MAX_FRET,
                "Fret " + note.fret() + " exceeds MAX_FRET")));
    }
}
