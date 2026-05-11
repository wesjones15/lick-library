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
    @Override public int[]    displayOrder() { return new int[]{5, 4, 3, 2, 1, 0}; }

    @Override
    public String[] labels() {
        int n = tuning.length;
        String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            result[i] = noteToLabel(tuning[n - 1 - i], i == 0);
        }
        return result;
    }

    private static String noteToLabel(Note note, boolean lowercase) {
        String name = switch (note) {
            case C       -> "C";  case C_SHARP -> "C#"; case D       -> "D";
            case D_SHARP -> "D#"; case E       -> "E";  case F       -> "F";
            case F_SHARP -> "F#"; case G       -> "G";  case G_SHARP -> "G#";
            case A       -> "A";  case B_FLAT  -> "Bb"; case B       -> "B";
        };
        return lowercase ? name.toLowerCase() : name;
    }
}
