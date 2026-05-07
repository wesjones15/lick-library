package org.jones.licklibrary.constants;

public enum Interval {
    ONE, FLAT_TWO, TWO, FLAT_THREE, THREE, FOUR, FLAT_FIVE, FIVE, FLAT_SIX, SIX, FLAT_SEVEN, SEVEN;

    private static final String[] DISPLAY_NAMES = {
        "1", "b2", "2", "b3", "3", "4", "b5", "5", "b6", "6", "b7", "7"
    };

    public Interval shift(int semitones) {
        return values()[(ordinal() + semitones) % 12];
    }

    public String displayName() {
        return DISPLAY_NAMES[ordinal()];
    }

    public static Interval fromDisplayName(String name) {
        for (Interval i : values()) {
            if (i.displayName().equals(name)) return i;
        }
        throw new IllegalArgumentException("Unknown interval display name: " + name);
    }
}
