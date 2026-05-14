package org.jones.licklibrary.domain.shared.instrument;

import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.Note;

import static org.jones.licklibrary.domain.shared.Note.*;

public class Bass implements Instrument {

    public static final Bass STANDARD = new Bass(new Note[]{E, A, D, G});

    private final Note[] tuning;

    private Bass(Note[] tuning) { this.tuning = tuning; }

    @Override public Note[]   tuning()       { return tuning; }
    @Override public String   name()         { return "Bass"; }
    @Override public String[] labels()       { return new String[]{"G", "D", "A", "E"}; }
    @Override public int[]    displayOrder() { return new int[]{3, 2, 1, 0}; }
}
