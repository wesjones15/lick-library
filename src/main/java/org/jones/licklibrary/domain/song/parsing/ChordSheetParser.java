package org.jones.licklibrary.domain.song.parsing;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ChordSheetParser {

    private static final Pattern METADATA_LINE  = Pattern.compile("^\\(.*\\)$");
    private static final Pattern SECTION_LABEL  = Pattern.compile("^\\[.*\\]$");
    private static final Pattern CHORD_TOKEN    = Pattern.compile(
            "^(NC|N\\.C\\.|[A-G][#b]?(maj|min|m|dim|aug|sus|add)?[0-9]*(b[0-9]+)?(/[A-G][#b]?)?)$"
    );
    // Tab line: optional string letter, pipe, then content with at least one dash
    private static final Pattern TAB_LINE = Pattern.compile(
            "^[eEbBgGdDaA]?\\|.*-.*$"
    );

    private static final double CONTENT_WIDTH    = 1100.0;
    private static final double CONTENT_HEIGHT   = 660.0;
    private static final double CHAR_WIDTH_RATIO = 0.6;
    private static final double LINE_HEIGHT_FACTOR = 2.5;
    private static final double MAX_FONT_SIZE    = 20.0;
    private static final double MIN_FONT_SIZE    = 8.0;
    private static final double DEFAULT_FONT_SIZE = 8.0;
    private static final int    MAX_COLUMNS      = 4;
    private static final int    TAB_BLOCK_LINES  = 6;
    private static final int    TAB_BLOCK_PAIR_COST = 4; // height cost per tab block

    private record ColumnPlan(int numColumns, double effectiveFont) {}

    public record ParseResult(List<ChordSheetLine> chordLines, int numColumns) {}

    public ParseResult parse(String rawText) {
        String[] lines = rawText.split("\n");
        List<String> stripped = stripMetadata(lines);
        List<ChordSheetLine> pairs = pairLines(stripped);
        ColumnPlan plan = selectColumnPlan(pairs);
        double columnWidth = CONTENT_WIDTH / plan.numColumns();
        List<ChordSheetLine> sized = applyFontSizes(pairs, columnWidth, plan.effectiveFont());
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
            .replaceAll("\\(.*?\\)", " ")
            .replaceAll("[|*%]", " ")
            .replaceAll("\\s+-\\s+", " ")
            .trim();
        if (normalized.isBlank()) return false;
        for (String token : normalized.split("\\s+")) {
            if (token.isEmpty()) continue;
            if (!CHORD_TOKEN.matcher(token).matches()) return false;
        }
        return true;
    }

    private boolean isTabLine(String line) {
        return TAB_LINE.matcher(line.stripTrailing()).matches();
    }

    private boolean hasTabBlock(List<String> lines, int start, int offset) {
        if (start + offset + TAB_BLOCK_LINES > lines.size()) return false;
        for (int j = 0; j < TAB_BLOCK_LINES; j++) {
            if (!isTabLine(lines.get(start + offset + j))) return false;
        }
        return true;
    }

    private List<ChordSheetLine> pairLines(List<String> lines) {
        List<ChordSheetLine> pairs = new ArrayList<>();
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);

            // Tab block without header (current line is first tab line)
            if (hasTabBlock(lines, i, 0)) {
                List<String> tabLines = lines.subList(i, i + TAB_BLOCK_LINES).stream()
                        .map(String::stripTrailing).collect(Collectors.toList());
                pairs.add(new GuitarTabLine("", tabLines, DEFAULT_FONT_SIZE));
                i += TAB_BLOCK_LINES;
                continue;
            }

            // Tab block with header (current line is header, next 6 are tab lines)
            if (hasTabBlock(lines, i, 1)) {
                List<String> tabLines = lines.subList(i + 1, i + 1 + TAB_BLOCK_LINES).stream()
                        .map(String::stripTrailing).collect(Collectors.toList());
                pairs.add(new GuitarTabLine(line, tabLines, DEFAULT_FONT_SIZE));
                i += 1 + TAB_BLOCK_LINES;
                continue;
            }

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

    private ColumnPlan selectColumnPlan(List<ChordSheetLine> pairs) {
        for (int n = 2; n <= MAX_COLUMNS; n++) {
            double colWidth = CONTENT_WIDTH / n;
            double charLimit = colWidth / (MIN_FONT_SIZE * CHAR_WIDTH_RATIO);
            List<ChordSheetLine> broken = breakOversized(pairs, charLimit);
            double widthFont = computeGlobalFontSize(broken, colWidth);
            int effectivePairs = 0;
            for (ChordSheetLine line : broken) {
                effectivePairs += (line instanceof GuitarTabLine) ? TAB_BLOCK_PAIR_COST : 1;
            }
            int pairsPerCol = (int) Math.ceil((double) effectivePairs / n);
            double heightFont = CONTENT_HEIGHT / (pairsPerCol * LINE_HEIGHT_FACTOR);
            double effectiveFont = Math.min(widthFont, heightFont);
            if (effectiveFont >= MIN_FONT_SIZE) {
                return new ColumnPlan(n, effectiveFont);
            }
        }
        return new ColumnPlan(MAX_COLUMNS, MIN_FONT_SIZE);
    }

    private List<ChordSheetLine> applyFontSizes(List<ChordSheetLine> pairs, double columnWidth, double globalFontSize) {
        double charLimit = columnWidth / (MIN_FONT_SIZE * CHAR_WIDTH_RATIO);
        List<ChordSheetLine> broken = breakOversized(pairs, charLimit);
        List<ChordSheetLine> result = new ArrayList<>();
        for (ChordSheetLine line : broken) {
            if (line instanceof GuitarTabLine tab) {
                result.add(new GuitarTabLine(tab.header(), tab.tabLines(), globalFontSize));
            } else {
                ChordLyric pair = (ChordLyric) line;
                if (pair.chords().isBlank() && pair.lyrics().isBlank()) {
                    result.add(new ChordLyric("", "", DEFAULT_FONT_SIZE));
                } else {
                    result.add(new ChordLyric(pair.chords(), pair.lyrics(), globalFontSize));
                }
            }
        }
        return result;
    }

    private List<ChordSheetLine> breakOversized(List<ChordSheetLine> pairs, double charLimit) {
        List<ChordSheetLine> result = new ArrayList<>();
        for (ChordSheetLine line : pairs) {
            if (line instanceof GuitarTabLine) {
                result.add(line);
                continue;
            }
            ChordLyric pair = (ChordLyric) line;
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

    private double computeGlobalFontSize(List<ChordSheetLine> broken, double columnWidth) {
        double size = MAX_FONT_SIZE;
        for (ChordSheetLine line : broken) {
            if (line instanceof GuitarTabLine) continue;
            ChordLyric pair = (ChordLyric) line;
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

        int lyricsBreak = Math.min(charLimit, lyrics.length());
        while (lyricsBreak > 0 && lyricsBreak < lyrics.length() && lyrics.charAt(lyricsBreak - 1) != ' ') {
            lyricsBreak--;
        }
        if (lyricsBreak == 0) lyricsBreak = Math.min(charLimit, lyrics.length());

        int chordsBreak = Math.min(charLimit, chords.length());
        while (chordsBreak > 0 && chordsBreak < chords.length() && chords.charAt(chordsBreak - 1) != ' ') {
            chordsBreak--;
        }
        if (chordsBreak == 0) chordsBreak = Math.min(charLimit, chords.length());

        int breakAt = Math.min(lyricsBreak, chordsBreak);

        String lyrics1 = lyrics.substring(0, Math.min(breakAt, lyrics.length()));
        String lyrics2 = lyrics.length() > breakAt ? lyrics.substring(breakAt) : "";
        String chords1 = chords.length() >= breakAt ? chords.substring(0, breakAt) : padRight(chords, breakAt);
        String chords2 = chords.length() > breakAt ? chords.substring(breakAt) : "";

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
