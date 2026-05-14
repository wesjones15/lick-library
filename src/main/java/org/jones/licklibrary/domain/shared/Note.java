package org.jones.licklibrary.domain.shared;

public enum Note {
    C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, B_FLAT, B;

    public Note shift(int semitones) {
        return values()[(ordinal() + semitones) % 12];
    }
}
