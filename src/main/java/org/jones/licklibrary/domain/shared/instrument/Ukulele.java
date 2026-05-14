package org.jones.licklibrary.domain.shared.instrument;

import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.Note;

import static org.jones.licklibrary.domain.shared.Note.*;

public class Ukulele implements Instrument {

    public static final Ukulele STANDARD = new Ukulele(new Note[]{G, C, E, A});

    private final Note[] tuning;

    private Ukulele(Note[] tuning) { this.tuning = tuning; }

    @Override public Note[]   tuning()       { return tuning; }
    @Override public String   name()         { return "Ukulele"; }
    @Override public String[] labels()       { return new String[]{"A", "E", "C", "g"}; }
    @Override public int[]    displayOrder() { return new int[]{3, 2, 1, 0}; }
}
