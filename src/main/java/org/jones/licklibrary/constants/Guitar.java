package org.jones.licklibrary.constants;

import static org.jones.licklibrary.constants.Note.*;

public class Guitar implements Instrument {

    public static final Guitar STANDARD = new Guitar("Standard", new Note[]{E, A, D, G, B, E});
    public static final Guitar DROP_D   = new Guitar("Drop D",   new Note[]{D, A, D, G, B, E});
    public static final Guitar OPEN_G   = new Guitar("Open G",   new Note[]{D, G, D, G, B, D});
    public static final Guitar OPEN_D   = new Guitar("Open D",   new Note[]{D, A, D, F_SHARP, A, D});
    public static final Guitar DADGAD   = new Guitar("DADGAD",   new Note[]{D, A, D, G, A, D});

    private final String name;
    private final Note[] tuning;

    private Guitar(String name, Note[] tuning) {
        this.name = name;
        this.tuning = tuning;
    }

    @Override public Note[]   tuning()       { return tuning; }
    @Override public String   name()         { return name; }
    @Override public String[] labels()       { return new String[]{"e", "B", "G", "D", "A", "E"}; }
    @Override public int[]    displayOrder() { return new int[]{5, 4, 3, 2, 1, 0}; }
}
