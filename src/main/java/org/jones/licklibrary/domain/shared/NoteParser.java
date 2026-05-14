package org.jones.licklibrary.domain.shared;

import java.util.HashMap;
import java.util.Map;

import static org.jones.licklibrary.domain.shared.Note.*;

public class NoteParser {

    private static final Map<String, Note> MAP = new HashMap<>();

    static {
        MAP.put("C",  C);   MAP.put("C#", C_SHARP);  MAP.put("DB", C_SHARP);
        MAP.put("D",  D);   MAP.put("D#", D_SHARP);   MAP.put("EB", D_SHARP);
        MAP.put("E",  E);
        MAP.put("F",  F);   MAP.put("F#", F_SHARP);   MAP.put("GB", F_SHARP);
        MAP.put("G",  G);   MAP.put("G#", G_SHARP);   MAP.put("AB", G_SHARP);
        MAP.put("A",  A);   MAP.put("A#", B_FLAT);    MAP.put("BB", B_FLAT);
        MAP.put("B",  B);
    }

    private NoteParser() {}

    public static Note parse(String s) {
        String key = s.trim().toUpperCase()
                .replace("♭", "B")
                .replace("♯", "#");
        Note n = MAP.get(key);
        if (n == null) throw new IllegalArgumentException("Unknown note: " + s);
        return n;
    }
}
