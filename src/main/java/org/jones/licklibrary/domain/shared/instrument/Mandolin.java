package org.jones.licklibrary.domain.shared.instrument;

import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.Note;

import static org.jones.licklibrary.domain.shared.Note.*;

public class Mandolin implements Instrument {

    public static final Mandolin STANDARD = new Mandolin(new Note[]{G, D, A, E});

    private final Note[] tuning;

    private Mandolin(Note[] tuning) { this.tuning = tuning; }

    @Override public Note[]   tuning()       { return tuning; }
    @Override public String   name()         { return "Mandolin"; }
    @Override public String[] labels()       { return new String[]{"e", "A", "D", "G"}; }
    @Override public int[]    displayOrder() { return new int[]{3, 2, 1, 0}; }
}
