package org.jones.licklibrary.domain.song.parsing;

import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.NoteParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Recognises and transposes single chord-symbol tokens as they appear on a chord sheet. */
final class ChordSymbol {

    private static final String NOTE = "[A-G][#b♯♭]?";
    private static final String QUALITY = "(?:maj|Maj|ma|M|min|mi|m|dim|°|aug|\\+|ø|-)?";
    // Possessive quantifiers keep long digit runs from backtracking
    private static final String EXTENSION =
            "(?:\\d++|add" + NOTE + "|(?:add|sus|maj|Maj|M|no)\\d*+|[#b♯♭+-]\\d++|alt|/9)";

    private static final Pattern CHORD = Pattern.compile(
            "^" + NOTE + QUALITY + EXTENSION + "*+" + "(?:/" + NOTE + ")?$");

    /** Tokens that can sit on a chord line without making it lyrics: N.C., rhythm slashes, repeat counts. */
    private static final Pattern NEUTRAL = Pattern.compile(
            "^(?:N\\.?C\\.?|/+|x\\d+|\\d+x|-+|\\.{2,})$", Pattern.CASE_INSENSITIVE);

    /** Wrapper characters peeled off before reading the root, then put back unchanged. */
    private static final Pattern TOKEN = Pattern.compile(
            "^([(\\[{|]*)(" + NOTE + ")(.*?)(?:/(" + NOTE + "))?([)\\]}|*.,]*)$");

    private static final Pattern PARENTHETICAL = Pattern.compile("\\(.*?\\)");

    private ChordSymbol() {}

    static boolean isChord(String token) {
        return CHORD.matcher(token).matches();
    }

    static boolean isNeutral(String token) {
        return NEUTRAL.matcher(token).matches();
    }

    /**
     * Transposes one whitespace-free token. With {@code strict}, the token is left alone unless it
     * reads as a real chord, which keeps words like "Bridge" in a tab header from becoming "Cridge".
     */
    static String transpose(String token, int semitones, boolean strict) {
        Matcher m = TOKEN.matcher(token);
        if (!m.matches()) return token;
        String prefix = m.group(1), root = m.group(2), quality = m.group(3), bass = m.group(4), suffix = m.group(5);
        if (strict) {
            String core = root + PARENTHETICAL.matcher(quality).replaceAll("") + (bass != null ? "/" + bass : "");
            if (!isChord(core)) return token;
        }
        try {
            String out = prefix + shift(root, semitones) + quality;
            if (bass != null) out += "/" + shift(bass, semitones);
            return out + suffix;
        } catch (IllegalArgumentException e) {
            return token;
        }
    }

    private static String shift(String note, int semitones) {
        return display(NoteParser.parse(note).shift(semitones));
    }

    static String display(Note note) {
        return switch (note) {
            case C -> "C";
            case C_SHARP -> "C#";
            case D -> "D";
            case D_SHARP -> "D#";
            case E -> "E";
            case F -> "F";
            case F_SHARP -> "F#";
            case G -> "G";
            case G_SHARP -> "G#";
            case A -> "A";
            case B_FLAT -> "Bb";
            case B -> "B";
        };
    }
}
