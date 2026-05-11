package org.jones.licklibrary.constants;

public interface Instrument {
    Note[] tuning();
    String[] labels();
    int[] displayOrder();
    String name();

    default int stringCount()                           { return tuning().length; }
    default Note getNoteAt(int stringIndex, int fret)   { return tuning()[stringIndex].shift(fret); }
    default int minFret(int stringIndex)                { return 0; }
}
