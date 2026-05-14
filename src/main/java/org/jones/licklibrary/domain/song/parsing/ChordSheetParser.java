package org.jones.licklibrary.domain.song.parsing;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ChordSheetParser {

    private static final Pattern METADATA_LINE  = Pattern.compile("^\\(.*\\)$");
    private static final Pattern SECTION_LABEL  = Pattern.compile("^\\[.*\\]$");
    private static final Pattern CHORD_TOKEN    = Pattern.compile(
            "^(NC|N\\.C\\.|[A-G][#b]?[a-zA-Z0-9]*(/[A-G][#b]?[a-zA-Z0-9]*)?)$"
    );

    private static final double CONTENT_WIDTH   = 1100.0;
    private static final double CHAR_WIDTH_RATIO = 0.6;
    private static final double MAX_FONT_SIZE   = 12.0;
    private static final double MIN_FONT_SIZE   = 6.0;
    private static final double DEFAULT_FONT_SIZE = 8.0;

    public record ParseResult(List<ChordLyric> chordLines, int numColumns) {}

    public ParseResult parse(String rawText) {
        String[] lines = rawText.split("\n");
        List<String> stripped = stripMetadata(lines);
        List<ChordLyric> pairs = pairLines(stripped);
        int numColumns = pairs.size() <= 30 ? 2 : 3;
        double columnWidth = CONTENT_WIDTH / numColumns;
        List<ChordLyric> sized = applyFontSizes(pairs, columnWidth);
        return new ParseResult(sized, numColumns);
    }

    private List<String> stripMetadata(String[] lines) {
        List<String> result = new ArrayList<>(List.of(lines));
        if (!result.isEmpty() && METADATA_LINE.matcher(result.get(0).trim()).matches()) {
            result.remove(0);
        }
        return result;
    }

    private boolean isChordLine(String line) {
        if (line.isBlank()) return false;
        for (String token : line.trim().split("\\s+")) {
            String cleaned = token.replaceAll("^\\((.+)\\)$", "$1").replaceAll("\\(.*?\\)", "");
            if (cleaned.isEmpty()) continue;
            if (!CHORD_TOKEN.matcher(cleaned).matches()) return false;
        }
        return true;
    }

    private List<ChordLyric> pairLines(List<String> lines) {
        List<ChordLyric> pairs = new ArrayList<>();
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);

            if (line.isBlank()) {
                pairs.add(new ChordLyric("", "", DEFAULT_FONT_SIZE));
                i++;
                continue;
            }

            if (SECTION_LABEL.matcher(line.trim()).matches()) {
                pairs.add(equalized(" ".repeat(line.length()), line, DEFAULT_FONT_SIZE));
                i++;
                continue;
            }

            if (isChordLine(line)) {
                i++;
                boolean nextIsLyric = i < lines.size()
                        && !lines.get(i).isBlank()
                        && !isChordLine(lines.get(i))
                        && !SECTION_LABEL.matcher(lines.get(i).trim()).matches();

                String lyric = nextIsLyric ? lines.get(i++) : "";
                pairs.add(equalized(line, lyric, DEFAULT_FONT_SIZE));
                continue;
            }

            pairs.add(equalized(" ".repeat(line.length()), line, DEFAULT_FONT_SIZE));
            i++;
        }
        return pairs;
    }

    private ChordLyric equalized(String chords, String lyrics, double fontSize) {
        int len = Math.max(chords.length(), lyrics.length());
        return new ChordLyric(padRight(chords, len), padRight(lyrics, len), fontSize);
    }

    private String padRight(String s, int len) {
        if (s.length() >= len) return s;
        return s + " ".repeat(len - s.length());
    }

    private List<ChordLyric> applyFontSizes(List<ChordLyric> pairs, double columnWidth) {
        double globalFontSize = MAX_FONT_SIZE;
        for (ChordLyric pair : pairs) {
            if (pair.chords().isBlank() && pair.lyrics().isBlank()) continue;
            int maxLen = Math.max(pair.chords().length(), pair.lyrics().length());
            if (maxLen == 0) continue;
            double size = columnWidth / (maxLen * CHAR_WIDTH_RATIO);
            globalFontSize = Math.min(globalFontSize, size);
        }
        globalFontSize = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, globalFontSize));

        List<ChordLyric> result = new ArrayList<>();
        double charLimit = columnWidth / (MIN_FONT_SIZE * CHAR_WIDTH_RATIO);
        for (ChordLyric pair : pairs) {
            if (pair.chords().isBlank() && pair.lyrics().isBlank()) {
                result.add(new ChordLyric("", "", DEFAULT_FONT_SIZE));
                continue;
            }
            int maxLen = Math.max(pair.chords().length(), pair.lyrics().length());
            if (maxLen > charLimit) {
                result.addAll(breakLine(pair, (int) charLimit, MIN_FONT_SIZE));
            } else {
                result.add(new ChordLyric(pair.chords(), pair.lyrics(), globalFontSize));
            }
        }
        return result;
    }

    private List<ChordLyric> breakLine(ChordLyric pair, int charLimit, double fontSize) {
        String lyrics = pair.lyrics();
        String chords = pair.chords();

        int breakAt = Math.min(charLimit, lyrics.length());
        while (breakAt > 0 && breakAt < lyrics.length() && lyrics.charAt(breakAt - 1) != ' ') {
            breakAt--;
        }
        if (breakAt == 0) breakAt = Math.min(charLimit, lyrics.length());

        String lyrics1 = lyrics.substring(0, breakAt);
        String lyrics2 = lyrics.length() > breakAt ? lyrics.substring(breakAt) : "";
        String chords1 = chords.length() >= breakAt ? chords.substring(0, breakAt) : padRight(chords, breakAt);
        String chords2 = chords.length() > breakAt ? chords.substring(breakAt) : "";

        int strip = 0;
        while (strip < lyrics2.length() && lyrics2.charAt(strip) == ' ') strip++;
        lyrics2 = lyrics2.substring(strip);
        chords2 = chords2.length() > strip ? chords2.substring(strip) : "";

        List<ChordLyric> out = new ArrayList<>();
        if (!lyrics1.isBlank() || !chords1.isBlank()) {
            out.add(equalized(chords1, lyrics1, fontSize));
        }
        if (!lyrics2.isBlank() || !chords2.isBlank()) {
            out.add(equalized(chords2, lyrics2, fontSize));
        }
        return out;
    }
}
