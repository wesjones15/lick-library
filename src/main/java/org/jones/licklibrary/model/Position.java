package org.jones.licklibrary.model;

import java.util.Arrays;
import java.util.List;

public record Position(List<TabNote> notes) {

    private static final String[] LABELS    = {"e", "B", "G", "D", "A", "E"};
    private static final int[]    DISPLAY   = {5, 4, 3, 2, 1, 0}; // high to low

    @Override
    public String toString() {
        if (notes == null || notes.isEmpty()) return "";

        int width = notes.stream()
            .mapToInt(n -> n.columnIndex()
                + String.valueOf(n.fret()).length()
                + (n.technique() != null && !n.technique().isEmpty() ? 1 : 0))
            .max().orElse(0) + 2;

        char[][] rows = new char[6][width];
        for (char[] row : rows) Arrays.fill(row, '-');

        for (TabNote note : notes) {
            String fretStr = String.valueOf(note.fret());
            int col = note.columnIndex();
            for (int i = 0; i < fretStr.length() && col + i < width; i++) {
                rows[note.stringIndex()][col + i] = fretStr.charAt(i);
            }
            if (note.technique() != null && !note.technique().isEmpty()) {
                int techCol = col + fretStr.length();
                if (techCol < width) rows[note.stringIndex()][techCol] = note.technique().charAt(0);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(LABELS[i]).append("|").append(new String(rows[DISPLAY[i]])).append("|");
        }
        return sb.toString();
    }
}
