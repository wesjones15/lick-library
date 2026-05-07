package org.jones.licklibrary.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntervalTest {

    // --- shift ---

    @Test
    void shift_byZeroReturnsSame() {
        assertEquals(Interval.ONE, Interval.ONE.shift(0));
        assertEquals(Interval.FIVE, Interval.FIVE.shift(0));
    }

    @Test
    void shift_oneToFive() {
        // 1 + 7 semitones = 5
        assertEquals(Interval.FIVE, Interval.ONE.shift(7));
    }

    @Test
    void shift_oneToFlatSeven() {
        // 1 + 10 semitones = b7
        assertEquals(Interval.FLAT_SEVEN, Interval.ONE.shift(10));
    }

    @Test
    void shift_wrapsAroundOctave() {
        // SEVEN (ordinal 11) + 1 = ONE (ordinal 0)
        assertEquals(Interval.ONE, Interval.SEVEN.shift(1));
    }

    @Test
    void shift_fullOctaveReturnsSame() {
        assertEquals(Interval.THREE, Interval.THREE.shift(12));
    }

    // --- displayName ---

    @Test
    void displayName_naturals() {
        assertEquals("1",  Interval.ONE.displayName());
        assertEquals("2",  Interval.TWO.displayName());
        assertEquals("3",  Interval.THREE.displayName());
        assertEquals("4",  Interval.FOUR.displayName());
        assertEquals("5",  Interval.FIVE.displayName());
        assertEquals("6",  Interval.SIX.displayName());
        assertEquals("7",  Interval.SEVEN.displayName());
    }

    @Test
    void displayName_flats() {
        assertEquals("b2", Interval.FLAT_TWO.displayName());
        assertEquals("b3", Interval.FLAT_THREE.displayName());
        assertEquals("b5", Interval.FLAT_FIVE.displayName());
        assertEquals("b6", Interval.FLAT_SIX.displayName());
        assertEquals("b7", Interval.FLAT_SEVEN.displayName());
    }

    // --- fromDisplayName ---

    @Test
    void fromDisplayName_roundTrips() {
        for (Interval i : Interval.values()) {
            assertEquals(i, Interval.fromDisplayName(i.displayName()));
        }
    }

    @Test
    void fromDisplayName_unknownThrows() {
        assertThrows(IllegalArgumentException.class, () -> Interval.fromDisplayName("x"));
    }
}
