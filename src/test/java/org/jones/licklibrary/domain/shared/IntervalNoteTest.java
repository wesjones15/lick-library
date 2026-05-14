package org.jones.licklibrary.domain.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntervalNoteTest {

    @Test
    void toString_noTechnique() {
        assertEquals("3", new IntervalNote(Interval.THREE, null, 0).toString());
    }

    @Test
    void toString_emptyTechniqueOmitted() {
        assertEquals("3", new IntervalNote(Interval.THREE, "", 0).toString());
    }

    @Test
    void toString_withTechnique() {
        assertEquals("3 h", new IntervalNote(Interval.THREE, "h", 0).toString());
        assertEquals("2 /", new IntervalNote(Interval.TWO, "/", 0).toString());
        assertEquals("b3 p", new IntervalNote(Interval.FLAT_THREE, "p", 0).toString());
    }
}
