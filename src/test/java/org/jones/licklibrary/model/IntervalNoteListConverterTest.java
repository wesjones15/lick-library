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
            new IntervalNote(Interval.ONE, null, 0),
            new IntervalNote(Interval.THREE, null, 1),
            new IntervalNote(Interval.FIVE, null, 2)
        );
        assertEquals("1:0:,3:1:,5:2:", converter.convertToDatabaseColumn(intervals));
    }

    @Test
    void serialize_withTechniques() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, null, 0),
            new IntervalNote(Interval.THREE, null, 1),
            new IntervalNote(Interval.TWO, "/", 2),
            new IntervalNote(Interval.THREE, null, 3),
            new IntervalNote(Interval.FOUR, null, 4)
        );
        assertEquals("1:0:,3:1:,2:2:/,3:3:,4:4:", converter.convertToDatabaseColumn(intervals));
    }

    @Test
    void serialize_simultaneousNotes() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, null, 0),
            new IntervalNote(Interval.THREE, null, 0),
            new IntervalNote(Interval.FIVE, null, 1)
        );
        assertEquals("1:0:,3:0:,5:1:", converter.convertToDatabaseColumn(intervals));
    }

    @Test
    void deserialize_noTechniques() {
        List<IntervalNote> result = converter.convertToEntityAttribute("1:0:,3:1:,5:2:");
        assertEquals(3, result.size());
        assertEquals(Interval.ONE, result.get(0).interval());
        assertEquals(0, result.get(0).columnIndex());
        assertNull(result.get(0).technique());
        assertEquals(Interval.THREE, result.get(1).interval());
        assertEquals(1, result.get(1).columnIndex());
        assertEquals(Interval.FIVE, result.get(2).interval());
        assertEquals(2, result.get(2).columnIndex());
    }

    @Test
    void deserialize_withTechniques() {
        List<IntervalNote> result = converter.convertToEntityAttribute("1:0:,3:1:,2:2:/,3:3:,4:4:");
        assertEquals(5, result.size());
        assertEquals(Interval.TWO, result.get(2).interval());
        assertEquals(2, result.get(2).columnIndex());
        assertEquals("/", result.get(2).technique());
        assertNull(result.get(3).technique());
    }

    @Test
    void roundTrip() {
        List<IntervalNote> original = List.of(
            new IntervalNote(Interval.ONE, null, 0),
            new IntervalNote(Interval.FLAT_THREE, "h", 1),
            new IntervalNote(Interval.FOUR, null, 2),
            new IntervalNote(Interval.FIVE, null, 3)
        );
        String serialized = converter.convertToDatabaseColumn(original);
        List<IntervalNote> restored = converter.convertToEntityAttribute(serialized);
        assertEquals(original.size(), restored.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).interval(), restored.get(i).interval());
            assertEquals(original.get(i).technique(), restored.get(i).technique());
            assertEquals(original.get(i).columnIndex(), restored.get(i).columnIndex());
        }
    }

    @Test
    void deserialize_emptyString() {
        assertTrue(converter.convertToEntityAttribute("").isEmpty());
    }

    @Test
    void toDisplayString_noTechniques() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, null, 0),
            new IntervalNote(Interval.THREE, null, 1),
            new IntervalNote(Interval.FIVE, null, 2)
        );
        assertEquals("1 3 5", IntervalNoteListConverter.toDisplayString(intervals));
    }

    @Test
    void toDisplayString_withTechniques() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, null, 0),
            new IntervalNote(Interval.THREE, null, 1),
            new IntervalNote(Interval.TWO, "/", 2),
            new IntervalNote(Interval.THREE, null, 3),
            new IntervalNote(Interval.FOUR, null, 4)
        );
        assertEquals("1 3 2 / 3 4", IntervalNoteListConverter.toDisplayString(intervals));
    }

    @Test
    void toDisplayString_omitsColumnIndex() {
        List<IntervalNote> intervals = List.of(
            new IntervalNote(Interval.ONE, null, 0),
            new IntervalNote(Interval.THREE, null, 0),
            new IntervalNote(Interval.FIVE, null, 1)
        );
        assertEquals("1 3 5", IntervalNoteListConverter.toDisplayString(intervals));
    }
}
