package org.jones.licklibrary.domain.shared;

public interface Instrument {
    Note[] tuning();
    String[] labels();
    int[] displayOrder();
    String name();

    default int stringCount()                           { return tuning().length; }
    default Note getNoteAt(int stringIndex, int fret)   { return tuning()[stringIndex].shift(fret); }
    default int minFret(int stringIndex)                { return 0; }

    /**
     * Semitones this instrument sounds relative to the standard-tuning shapes that chord charts
     * and tabs for it are written in. Eb tuning plays E shapes a half step down, so it is -1.
     */
    default int soundingOffset()                        { return 0; }
}
