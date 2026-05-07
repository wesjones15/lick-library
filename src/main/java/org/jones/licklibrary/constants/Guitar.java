package org.jones.licklibrary.constants;

public class Guitar {

    // Standard tuning, low to high: E A D G B E
    public static final Note[] STANDARD_TUNING = {
        Note.E, Note.A, Note.D, Note.G, Note.B, Note.E
    };

    public static Note getNoteAt(int stringIndex, int fret) {
        return STANDARD_TUNING[stringIndex].shift(fret);
    }
}
