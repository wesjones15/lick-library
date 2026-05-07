package org.jones.licklibrary.constants;

public enum Interval {
    ONE, FLAT_TWO, TWO, FLAT_THREE, THREE, FOUR, FLAT_FIVE, FIVE, FLAT_SIX, SIX, FLAT_SEVEN, SEVEN;

    public Interval shift(int semitones) {
        return values()[(ordinal() + semitones) % 12];
    }
}
