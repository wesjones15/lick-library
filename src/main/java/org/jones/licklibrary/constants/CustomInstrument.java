package org.jones.licklibrary.constants;

import java.util.Map;

import static org.jones.licklibrary.constants.Note.*;

public class CustomInstrument implements Instrument {

    private static final Map<Note, String> DISPLAY = Map.ofEntries(
        Map.entry(C,       "C"),
        Map.entry(C_SHARP, "C#"),
        Map.entry(D,       "D"),
        Map.entry(D_SHARP, "D#"),
        Map.entry(E,       "E"),
        Map.entry(F,       "F"),
        Map.entry(F_SHARP, "F#"),
        Map.entry(G,       "G"),
        Map.entry(G_SHARP, "G#"),
        Map.entry(A,       "A"),
        Map.entry(B_FLAT,  "Bb"),
        Map.entry(B,       "B")
    );

    private final Note[] tuning;
    private final String[] labels;
    private final int[] displayOrder;

    public CustomInstrument(Note[] tuning) {
        if (tuning == null || tuning.length == 0)
            throw new IllegalArgumentException("Tuning must have at least one string");
        this.tuning = tuning;
        int n = tuning.length;
        labels = new String[n];
        displayOrder = new int[n];
        for (int i = 0; i < n; i++) {
            labels[i]       = DISPLAY.get(tuning[n - 1 - i]);
            displayOrder[i] = n - 1 - i;
        }
    }

    @Override public Note[]   tuning()       { return tuning; }
    @Override public String   name()         { return "Custom"; }
    @Override public String[] labels()       { return labels; }
    @Override public int[]    displayOrder() { return displayOrder; }
}
