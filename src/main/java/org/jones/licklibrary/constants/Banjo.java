package org.jones.licklibrary.constants;

import static org.jones.licklibrary.constants.Note.*;

public class Banjo implements Instrument {

    // 5-string open G: strings 0-3 = D G B D (low to high), string 4 = short drone G
    public static final Banjo STANDARD = new Banjo(new Note[]{D, G, B, D, G});

    private final Note[] tuning;

    private Banjo(Note[] tuning) { this.tuning = tuning; }

    @Override public Note[]   tuning()       { return tuning; }
    @Override public String   name()         { return "Banjo"; }
    @Override public String[] labels()       { return new String[]{"g", "D", "B", "G", "D"}; }
    @Override public int[]    displayOrder() { return new int[]{4, 3, 2, 1, 0}; }

    // TODO: string index 4 (the 5th/drone string) physically starts at fret 5.
    //  minFret(4) should return 5 and getNoteAt(4, fret) should shift G by (fret - 5).
    //  Tab rendering also needs to leave frets 0-4 blank for that string.
    //  Deferred until the full 5th-string quirk is addressed.
}
