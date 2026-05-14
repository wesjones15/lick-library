package org.jones.licklibrary.domain.song.parsing;

import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.NoteParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChordTransposer {

    public List<ChordLyric> transpose(List<ChordLyric> chordLines, int semitones) {
        if (semitones == 0) return chordLines;
        int normalized = ((semitones % 12) + 12) % 12;
        return chordLines.stream()
                .map(pair -> transposePair(pair, normalized))
                .toList();
    }

    private ChordLyric transposePair(ChordLyric pair, int semitones) {
        if (pair.chords().isBlank()) return pair;

        StringBuilder chords = new StringBuilder(pair.chords());
        StringBuilder lyrics = new StringBuilder(pair.lyrics());

        int i = 0;
        while (i < chords.length()) {
            if (chords.charAt(i) == ' ') { i++; continue; }

            int start = i;
            while (i < chords.length() && chords.charAt(i) != ' ') i++;
            String token = chords.substring(start, i);

            if (token.equals("NC") || token.equals("N.C.")) continue;

            String newToken = transposeToken(token, semitones);
            chords.replace(start, start + token.length(), newToken);

            int delta = newToken.length() - token.length();
            int afterToken = start + newToken.length();
            i = afterToken;

            if (delta > 0) {
                if (afterToken < chords.length() && chords.charAt(afterToken) == ' ') {
                    chords.deleteCharAt(afterToken);
                } else {
                    int insertAt = Math.min(afterToken, lyrics.length());
                    lyrics.insert(insertAt, ' ');
                }
            } else if (delta < 0) {
                chords.insert(afterToken, ' ');
                i++;
            }
        }

        int maxLen = Math.max(chords.length(), lyrics.length());
        while (chords.length() < maxLen) chords.append(' ');
        while (lyrics.length() < maxLen) lyrics.append(' ');

        return new ChordLyric(chords.toString(), lyrics.toString(), pair.fontSize());
    }

    private String transposeToken(String token, int semitones) {
        if (!token.contains("/")) return transposeChordPart(token, semitones);
        String[] parts = token.split("/", 2);
        return transposeChordPart(parts[0], semitones) + "/" + transposeChordPart(parts[1], semitones);
    }

    private String transposeChordPart(String part, int semitones) {
        if (part.isEmpty()) return part;

        if (part.startsWith("(") && part.endsWith(")") && part.length() > 2) {
            return "(" + transposeChordPart(part.substring(1, part.length() - 1), semitones) + ")";
        }

        String qualifier = "";
        String chord = part;
        int qualStart = part.indexOf('(');
        if (qualStart > 0) {
            qualifier = part.substring(qualStart);
            chord = part.substring(0, qualStart);
        }

        String root, suffix;
        if (chord.length() > 1 && (chord.charAt(1) == '#' || chord.charAt(1) == 'b')) {
            root = chord.substring(0, 2);
            suffix = chord.substring(2);
        } else {
            root = chord.substring(0, 1);
            suffix = chord.substring(1);
        }
        try {
            Note shifted = NoteParser.parse(root).shift(semitones);
            return noteDisplay(shifted) + suffix + qualifier;
        } catch (IllegalArgumentException e) {
            return part;
        }
    }

    private String noteDisplay(Note note) {
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
