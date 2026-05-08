package org.jones.licklibrary.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Position(List<TabNote> notes) {

    private static final String[] LABELS       = {"e", "B", "G", "D", "A", "E"};
    private static final int[]    DISPLAY_ORDER = {5, 4, 3, 2, 1, 0};

    public String toTabString() {
        if (notes == null || notes.isEmpty()) return "";

        List<Integer> slots = notes.stream()
            .map(TabNote::columnIndex)
            .distinct()
            .sorted()
            .toList();

        Map<Integer, Integer> slotWidths = new HashMap<>();
        for (int slot : slots) {
            int w = notes.stream()
                .filter(n -> n.columnIndex() == slot)
                .mapToInt(n -> String.valueOf(n.fret()).length())
                .max().orElse(1);
            slotWidths.put(slot, w);
        }

        Map<Integer, Map<Integer, TabNote>> byString = new HashMap<>();
        for (TabNote note : notes) {
            byString.computeIfAbsent(note.stringIndex(), k -> new HashMap<>())
                    .put(note.columnIndex(), note);
        }

        String[] rows = new String[6];
        for (int s = 0; s < 6; s++) {
            Map<Integer, TabNote> stringNotes = byString.getOrDefault(s, Map.of());
            StringBuilder sb = new StringBuilder();
            sb.append('-');
            for (int i = 0; i < slots.size(); i++) {
                int slot = slots.get(i);
                int w = slotWidths.get(slot);
                TabNote note = stringNotes.get(slot);
                if (note != null) {
                    String fretStr = String.valueOf(note.fret());
                    sb.append(fretStr);
                    for (int p = fretStr.length(); p < w; p++) sb.append('-');
                } else {
                    for (int p = 0; p < w; p++) sb.append('-');
                }
                if (i < slots.size() - 1) {
                    if (note != null && note.technique() != null && !note.technique().isEmpty()) {
                        sb.append(note.technique().charAt(0));
                    } else {
                        sb.append('-');
                    }
                }
            }
            sb.append('-');
            rows[s] = sb.toString();
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (result.length() > 0) result.append('\n');
            result.append(LABELS[i]).append('|').append(rows[DISPLAY_ORDER[i]]).append('|');
        }
        return result.toString();
    }
}
