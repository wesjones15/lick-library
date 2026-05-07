package org.jones.licklibrary.model;

import org.jones.licklibrary.constants.Interval;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntervalNoteTest {

    @Test
    void toString_noTechnique() {
        assertEquals("3", new IntervalNote(Interval.THREE, null).toString());
    }

    @Test
    void toString_emptyTechniqueOmitted() {
        assertEquals("3", new IntervalNote(Interval.THREE, "").toString());
    }

    @Test
    void toString_withTechnique() {
        assertEquals("3 h", new IntervalNote(Interval.THREE, "h").toString());
        assertEquals("2 /", new IntervalNote(Interval.TWO, "/").toString());
        assertEquals("b3 p", new IntervalNote(Interval.FLAT_THREE, "p").toString());
    }
}
