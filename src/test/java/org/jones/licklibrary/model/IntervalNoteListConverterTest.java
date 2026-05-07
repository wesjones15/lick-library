package org.jones.licklibrary.model;

import org.jones.licklibrary.constants.Interval;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntervalNoteListConverterTest {

    private final IntervalNoteListConverter converter = new IntervalNoteListConverter();

    @Test
    void serialize_noTechniques() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, null),
            new IntervalNote(Interval.THREE, null),
            new IntervalNote(Interval.FIVE, null)
        );
        assertEquals("1 3 5", converter.convertToDatabaseColumn(intervals));
    }

    @Test
    void serialize_withTechniques() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, null),
            new IntervalNote(Interval.THREE, null),
            new IntervalNote(Interval.TWO, "/"),
            new IntervalNote(Interval.THREE, null),
            new IntervalNote(Interval.FOUR, null)
        );
        assertEquals("1 3 2 / 3 4", converter.convertToDatabaseColumn(intervals));
    }

    @Test
    void deserialize_noTechniques() {
        List<IntervalNote> result = converter.convertToEntityAttribute("1 3 5");
        assertEquals(3, result.size());
        assertEquals(Interval.ONE, result.get(0).interval());
        assertNull(result.get(0).technique());
        assertEquals(Interval.THREE, result.get(1).interval());
        assertEquals(Interval.FIVE, result.get(2).interval());
    }

    @Test
    void deserialize_withTechniques() {
        List<IntervalNote> result = converter.convertToEntityAttribute("1 3 2 / 3 4");
        assertEquals(5, result.size());
        assertEquals(Interval.TWO, result.get(2).interval());
        assertEquals("/", result.get(2).technique());
        assertNull(result.get(3).technique());
    }

    @Test
    void roundTrip() {
        List<IntervalNote> original = List.of(
            new IntervalNote(Interval.ONE, null),
            new IntervalNote(Interval.FLAT_THREE, "h"),
            new IntervalNote(Interval.FOUR, null),
            new IntervalNote(Interval.FIVE, null)
        );
        String serialized = converter.convertToDatabaseColumn(original);
        List<IntervalNote> restored = converter.convertToEntityAttribute(serialized);
        assertEquals(original.size(), restored.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).interval(), restored.get(i).interval());
            assertEquals(original.get(i).technique(), restored.get(i).technique());
        }
    }

    @Test
    void deserialize_emptyString() {
        assertTrue(converter.convertToEntityAttribute("").isEmpty());
    }
}
