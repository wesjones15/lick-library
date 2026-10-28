package org.jones.licklibrary.domain.song.parsing;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.UnaryOperator;

@Component
public class ChordTransposer {

    public List<ChordSheetLine> transpose(List<ChordSheetLine> chordLines, int semitones) {
        int normalized = Math.floorMod(semitones, 12);
        if (normalized == 0) return chordLines;
        return chordLines.stream()
                .map(line -> line instanceof GuitarTabLine tab
                        ? transposeTabHeader(tab, normalized)
                        : (ChordSheetLine) transposePair((ChordLyric) line, normalized))
                .toList();
    }

    /** Tab headers mix chords with labels ("Intro riff  G  C"), so only real chord symbols move. */
    private GuitarTabLine transposeTabHeader(GuitarTabLine tab, int semitones) {
        if (tab.header() == null || tab.header().isBlank()) return tab;
        String header = realign(new StringBuilder(tab.header()), t -> ChordSymbol.transpose(t, semitones, true));
        return new GuitarTabLine(header, tab.tabLines(), tab.fontSize());
    }

    private ChordLyric transposePair(ChordLyric pair, int semitones) {
        if (pair.chords().isBlank()) return pair;

        StringBuilder chords = new StringBuilder(realign(new StringBuilder(pair.chords()),
                t -> ChordSymbol.transpose(t, semitones, false)));
        StringBuilder lyrics = new StringBuilder(pair.lyrics());

        int maxLen = Math.max(chords.length(), lyrics.length());
        while (chords.length() < maxLen) chords.append(' ');
        while (lyrics.length() < maxLen) lyrics.append(' ');

        return new ChordLyric(chords.toString(), lyrics.toString(), pair.fontSize());
    }

    /**
     * Rewrites each space-separated token, keeping later tokens in their columns: a longer token
     * borrows spaces from the gap after it (always leaving one), a shorter one pads that gap.
     */
    private String realign(StringBuilder line, UnaryOperator<String> rewrite) {
        int i = 0;
        while (i < line.length()) {
            if (line.charAt(i) == ' ') { i++; continue; }

            int start = i;
            while (i < line.length() && line.charAt(i) != ' ') i++;
            String token = line.substring(start, i);
            String newToken = rewrite.apply(token);
            if (newToken.equals(token)) continue;

            line.replace(start, i, newToken);
            int afterToken = start + newToken.length();
            int delta = newToken.length() - token.length();

            if (delta > 0) {
                int gapEnd = afterToken;
                while (gapEnd < line.length() && line.charAt(gapEnd) == ' ') gapEnd++;
                int gap = gapEnd - afterToken;
                int spare = gapEnd == line.length() ? gap : Math.max(0, gap - 1);
                line.delete(afterToken, afterToken + Math.min(delta, spare));
            } else if (delta < 0) {
                line.insert(afterToken, " ".repeat(-delta));
            }
            i = afterToken;
        }
        return line.toString();
    }
}
