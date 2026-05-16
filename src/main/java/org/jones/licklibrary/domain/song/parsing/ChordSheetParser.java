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
            "^(NC|N\\.C\\.|[A-G][#b]?(maj|min|m|dim|aug|sus|add)?[0-9]*(b[0-9]+)?(/[A-G][#b]?)?)$"
    );

    private static final double CONTENT_WIDTH    = 1100.0;
    private static final double CONTENT_HEIGHT   = 660.0;  // iPad Air landscape minus navbar + header
    private static final double CHAR_WIDTH_RATIO = 0.6;
    private static final double LINE_HEIGHT_FACTOR = 2.5;  // 2 lines × leading-tight (1.25)
    private static final double MAX_FONT_SIZE    = 20.0;
    private static final double MIN_FONT_SIZE    = 8.0;
    private static final double DEFAULT_FONT_SIZE = 8.0;
    private static final int    MAX_COLUMNS      = 4;

    private record ColumnPlan(int numColumns, double effectiveFont) {}

    public record ParseResult(List<ChordLyric> chordLines, int numColumns) {}

    public ParseResult parse(String rawText) {
        String[] lines = rawText.split("\n");
        List<String> stripped = stripMetadata(lines);
        List<ChordLyric> pairs = pairLines(stripped);
        ColumnPlan plan = selectColumnPlan(pairs);
        double columnWidth = CONTENT_WIDTH / plan.numColumns();
        List<ChordLyric> sized = applyFontSizes(pairs, columnWidth, plan.effectiveFont());
        return new ParseResult(sized, plan.numColumns());
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
        String normalized = line
            .replaceAll("\\(.*?\\)", " ")   // remove (...) annotation groups
            .replaceAll("[|*%]", " ")        // |, * and % → space
            .replaceAll("\\s+-\\s+", " ")   // " - " chord separators → space
            .trim();
        if (normalized.isBlank()) return false;
        for (String token : normalized.split("\\s+")) {
            if (token.isEmpty()) continue;
            if (!CHORD_TOKEN.matcher(token).matches()) return false;
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

    private ColumnPlan selectColumnPlan(List<ChordLyric> pairs) {
        for (int n = 2; n <= MAX_COLUMNS; n++) {
            double colWidth = CONTENT_WIDTH / n;
            double charLimit = colWidth / (MIN_FONT_SIZE * CHAR_WIDTH_RATIO);
            List<ChordLyric> broken = breakOversized(pairs, charLimit);
            double widthFont = computeGlobalFontSize(broken, colWidth);
            int pairsPerCol = (int) Math.ceil((double) broken.size() / n);
            double heightFont = CONTENT_HEIGHT / (pairsPerCol * LINE_HEIGHT_FACTOR);
            double effectiveFont = Math.min(widthFont, heightFont);
            if (effectiveFont >= MIN_FONT_SIZE) {
                return new ColumnPlan(n, effectiveFont);
            }
        }
        return new ColumnPlan(MAX_COLUMNS, MIN_FONT_SIZE);
    }

    private List<ChordLyric> applyFontSizes(List<ChordLyric> pairs, double columnWidth, double globalFontSize) {
        double charLimit = columnWidth / (MIN_FONT_SIZE * CHAR_WIDTH_RATIO);
        List<ChordLyric> broken = breakOversized(pairs, charLimit);
        List<ChordLyric> result = new ArrayList<>();
        for (ChordLyric pair : broken) {
            if (pair.chords().isBlank() && pair.lyrics().isBlank()) {
                result.add(new ChordLyric("", "", DEFAULT_FONT_SIZE));
            } else {
                result.add(new ChordLyric(pair.chords(), pair.lyrics(), globalFontSize));
            }
        }
        return result;
    }

    private List<ChordLyric> breakOversized(List<ChordLyric> pairs, double charLimit) {
        List<ChordLyric> result = new ArrayList<>();
        for (ChordLyric pair : pairs) {
            if (pair.chords().isBlank() && pair.lyrics().isBlank()) {
                result.add(new ChordLyric("", "", DEFAULT_FONT_SIZE));
                continue;
            }
            int maxLen = Math.max(pair.chords().length(), pair.lyrics().length());
            if (maxLen > charLimit) {
                result.addAll(breakLine(pair, (int) charLimit, MIN_FONT_SIZE));
            } else {
                result.add(pair);
            }
        }
        return result;
    }

    private double computeGlobalFontSize(List<ChordLyric> broken, double columnWidth) {
        double size = MAX_FONT_SIZE;
        for (ChordLyric pair : broken) {
            if (pair.chords().isBlank() && pair.lyrics().isBlank()) continue;
            int maxLen = Math.max(pair.chords().length(), pair.lyrics().length());
            if (maxLen == 0) continue;
            size = Math.min(size, columnWidth / (maxLen * CHAR_WIDTH_RATIO));
        }
        return Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, size));
    }

    private List<ChordLyric> breakLine(ChordLyric pair, int charLimit, double fontSize) {
        String lyrics = pair.lyrics();
        String chords = pair.chords();

        // Find word break in lyrics
        int lyricsBreak = Math.min(charLimit, lyrics.length());
        while (lyricsBreak > 0 && lyricsBreak < lyrics.length() && lyrics.charAt(lyricsBreak - 1) != ' ') {
            lyricsBreak--;
        }
        if (lyricsBreak == 0) lyricsBreak = Math.min(charLimit, lyrics.length());

        // Find word break in chords independently to avoid truncating mid-token
        int chordsBreak = Math.min(charLimit, chords.length());
        while (chordsBreak > 0 && chordsBreak < chords.length() && chords.charAt(chordsBreak - 1) != ' ') {
            chordsBreak--;
        }
        if (chordsBreak == 0) chordsBreak = Math.min(charLimit, chords.length());

        // Use the more conservative break to ensure neither string is cut mid-token
        int breakAt = Math.min(lyricsBreak, chordsBreak);

        String lyrics1 = lyrics.substring(0, Math.min(breakAt, lyrics.length()));
        String lyrics2 = lyrics.length() > breakAt ? lyrics.substring(breakAt) : "";
        String chords1 = chords.length() >= breakAt ? chords.substring(0, breakAt) : padRight(chords, breakAt);
        String chords2 = chords.length() > breakAt ? chords.substring(breakAt) : "";

        // Strip leading spaces from each second half independently
        lyrics2 = lyrics2.stripLeading();
        chords2 = chords2.stripLeading();

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
