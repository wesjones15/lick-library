package org.jones.licklibrary.constants;

import static org.jones.licklibrary.constants.Note.*;

public class Bass implements Instrument {

    public static final Bass STANDARD = new Bass(new Note[]{E, A, D, G});

    private final Note[] tuning;

    private Bass(Note[] tuning) { this.tuning = tuning; }

    @Override public Note[]   tuning()       { return tuning; }
    @Override public String   name()         { return "Bass"; }
    @Override public String[] labels()       { return new String[]{"G", "D", "A", "E"}; }
    @Override public int[]    displayOrder() { return new int[]{3, 2, 1, 0}; }
}
