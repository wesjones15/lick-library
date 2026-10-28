package org.jones.licklibrary.domain.song.parsing;

import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.InstrumentRegistry;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.NoteParser;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ChordSheetParser {

    private static final Pattern METADATA_LINE  = Pattern.compile("^\\(.*\\)$");
    private static final Pattern SECTION_LABEL  = Pattern.compile("^\\[.*\\]$");
    private static final Pattern TAB_LINE = Pattern.compile(
            "^\\s*[A-Ga-g][#b]?\\s?\\|.*-.*$"
    );
    private static final Pattern UNICODE_SPACE = Pattern.compile("[\\u00A0\\u2000-\\u200A\\u202F\\u205F\\u3000]");
    private static final int TAB_STOP = 8;

    private static final double CONTENT_WIDTH    = 1100.0;
    private static final double CONTENT_HEIGHT   = 660.0;
    private static final double CHAR_WIDTH_RATIO = 0.6;
    private static final double LINE_HEIGHT_FACTOR = 2.5;
    private static final double MAX_FONT_SIZE    = 20.0;
    private static final double MIN_FONT_SIZE    = 8.0;
    private static final double DEFAULT_FONT_SIZE = 8.0;
    private static final int    MAX_COLUMNS      = 4;
    private static final int    TAB_BLOCK_MIN    = 3;
    private static final int    TAB_BLOCK_MAX    = 8;  // up to 8-string instruments
    private static final double COLUMN_GAP       = 24.0; // matches the UI's gap between columns
    private static final double ROW_HEIGHT       = LINE_HEIGHT_FACTOR / 2; // one text row, in font sizes
    private static final double FONT_STEP        = 0.5;
    private static final double MAX_WRAPPED_SHARE = 0.1; // wrap outliers only, not ordinary lyric lines

    private record ColumnPlan(int numColumns, double effectiveFont) {}

    public record ParseResult(List<ChordSheetLine> chordLines, int numColumns) {}

    public ParseResult parse(String rawText) {
        String[] lines = normalize(rawText == null ? "" : rawText).split("\n");
        List<String> stripped = stripMetadata(lines);
        List<ChordSheetLine> pairs = pairLines(stripped);
        ColumnPlan plan = selectColumnPlan(pairs);
        double columnWidth = columnWidth(plan.numColumns());
        List<ChordSheetLine> sized = applyFontSizes(pairs, columnWidth, plan.effectiveFont());
        return new ParseResult(sized, plan.numColumns());
    }

    /** Pasted sheets arrive with CRLF, non-breaking spaces and tab characters; all of them break column alignment. */
    private String normalize(String rawText) {
        String text = UNICODE_SPACE.matcher(rawText.replace("\r\n", "\n").replace('\r', '\n')).replaceAll(" ");
        if (text.indexOf('\t') < 0) return text;
        StringBuilder out = new StringBuilder(text.length());
        int col = 0;
        for (char c : text.toCharArray()) {
            if (c == '\t') {
                int pad = TAB_STOP - (col % TAB_STOP);
                out.append(" ".repeat(pad));
                col += pad;
            } else {
                out.append(c);
                col = c == '\n' ? 0 : col + 1;
            }
        }
        return out.toString();
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
        boolean hasChord = false;
        for (String token : normalized.split("\\s+")) {
            if (token.isEmpty()) continue;
            if (ChordSymbol.isChord(token)) hasChord = true;
            else if (!ChordSymbol.isNeutral(token)) return false;
        }
        return hasChord;
    }

    private boolean isTabLine(String line) {
        return TAB_LINE.matcher(line.stripTrailing()).matches();
    }

    private int tabBlockLength(List<String> lines, int start) {
        if (start < 0) return 0;
        int run = 0;
        while (start + run < lines.size() && isTabLine(lines.get(start + run))) run++;
        if (run < TAB_BLOCK_MIN) return 0;
        // Back-to-back systems with no blank line between them: split where the string labels repeat
        for (int period = TAB_BLOCK_MIN; period <= TAB_BLOCK_MAX && 2 * period <= run; period++) {
            boolean repeats = true;
            for (int k = 0; k < period && repeats; k++) {
                repeats = tabLabel(lines.get(start + k)).equals(tabLabel(lines.get(start + period + k)));
            }
            if (repeats) return period;
        }
        return Math.min(run, TAB_BLOCK_MAX);
    }

    private static String tabLabel(String line) {
        int pipe = line.indexOf('|');
        return pipe < 0 ? "" : line.substring(0, pipe).strip();
    }

    public Instrument detectInstrument(List<String> tabLines) {
        try {
            Note[] tuning = new Note[tabLines.size()];
            for (int i = 0; i < tabLines.size(); i++) {
                String line = tabLines.get(i);
                String label = line.substring(0, line.indexOf('|')).strip();
                tuning[tabLines.size() - 1 - i] = NoteParser.parse(label);
            }
            return InstrumentRegistry.fromTuning(tuning);
        } catch (Exception e) {
            return Guitar.STANDARD;
        }
    }

    private List<ChordSheetLine> pairLines(List<String> lines) {
        List<ChordSheetLine> pairs = new ArrayList<>();
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);

            // Tab block without header (current line is first tab line)
            int blockLen = tabBlockLength(lines, i);
            if (blockLen > 0) {
                List<String> tabLines = lines.subList(i, i + blockLen).stream()
                        .map(String::strip).collect(Collectors.toList());
                pairs.add(new GuitarTabLine("", tabLines, DEFAULT_FONT_SIZE));
                i += blockLen;
                continue;
            }

            // Tab block with header (current line is header, next block are tab lines)
            blockLen = tabBlockLength(lines, i + 1);
            if (blockLen > 0) {
                List<String> tabLines = lines.subList(i + 1, i + 1 + blockLen).stream()
                        .map(String::strip).collect(Collectors.toList());
                pairs.add(new GuitarTabLine(line, tabLines, DEFAULT_FONT_SIZE));
                i += 1 + blockLen;
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

    /**
     * Picks the column count that allows the largest text: for each count, the biggest font at which
     * the sheet (long lines wrapped to that font) fits the content height. Plans that would cut off a
     * tab are only used when every plan does.
     */
    private ColumnPlan selectColumnPlan(List<ChordSheetLine> pairs) {
        ColumnPlan best = null;
        boolean bestClipsTabs = true;
        for (int n = 2; n <= MAX_COLUMNS; n++) {
            double colWidth = columnWidth(n);
            boolean clipsTabs = widestTab(pairs) > charLimit(colWidth, MIN_FONT_SIZE);
            for (double font = MAX_FONT_SIZE; font >= MIN_FONT_SIZE; font -= FONT_STEP) {
                if (!fits(pairs, n, colWidth, font, clipsTabs)) continue;
                boolean better = best == null
                        || (bestClipsTabs && !clipsTabs)
                        || (bestClipsTabs == clipsTabs && font > best.effectiveFont());
                if (better) {
                    best = new ColumnPlan(n, font);
                    bestClipsTabs = clipsTabs;
                }
                break;
            }
        }
        return best != null ? best : new ColumnPlan(MAX_COLUMNS, MIN_FONT_SIZE);
    }

    private boolean fits(List<ChordSheetLine> pairs, int numColumns, double colWidth, double font, boolean ignoreTabWidth) {
        int limit = charLimit(colWidth, font);
        if (!ignoreTabWidth && widestTab(pairs) > limit) return false;
        if (font > MIN_FONT_SIZE && wrappedShare(pairs, limit) > MAX_WRAPPED_SHARE) return false;
        double rows = 0;
        double tallest = 0;
        for (ChordSheetLine line : breakOversized(pairs, limit)) {
            double r = rowsFor(line, font);
            rows += r;
            tallest = Math.max(tallest, r);
        }
        // Lines can't split across columns, so a column can run up to one line over the even share
        double rowsPerColumn = rows / numColumns + tallest;
        return rowsPerColumn * ROW_HEIGHT * font <= CONTENT_HEIGHT;
    }

    /** Height of a line in text rows at {@code font}, as the UI lays it out. */
    private static double rowsFor(ChordSheetLine line, double font) {
        if (line instanceof GuitarTabLine tab) {
            boolean hasHeader = tab.header() != null && !tab.header().isBlank();
            return tab.tabLines().size() + (hasHeader ? 1 : 0) + 8.0 / (font * ROW_HEIGHT); // + my-1 margins
        }
        ChordLyric pair = (ChordLyric) line;
        if (pair.chords().isBlank() && pair.lyrics().isBlank()) return DEFAULT_FONT_SIZE / font; // spacer row
        return 2;
    }

    private static double wrappedShare(List<ChordSheetLine> pairs, int limit) {
        long content = 0, wrapped = 0;
        for (ChordSheetLine line : pairs) {
            if (!(line instanceof ChordLyric pair) || (pair.chords().isBlank() && pair.lyrics().isBlank())) continue;
            content++;
            if (Math.max(pair.chords().stripTrailing().length(), pair.lyrics().stripTrailing().length()) > limit) wrapped++;
        }
        return content == 0 ? 0 : (double) wrapped / content;
    }

    private static int widestTab(List<ChordSheetLine> pairs) {
        return pairs.stream()
                .filter(l -> l instanceof GuitarTabLine)
                .flatMap(l -> ((GuitarTabLine) l).tabLines().stream())
                .mapToInt(String::length).max().orElse(0);
    }

    private static double columnWidth(int numColumns) {
        return (CONTENT_WIDTH - COLUMN_GAP * (numColumns - 1)) / numColumns;
    }

    private static int charLimit(double colWidth, double font) {
        return (int) Math.floor(colWidth / (font * CHAR_WIDTH_RATIO));
    }

    private List<ChordSheetLine> applyFontSizes(List<ChordSheetLine> pairs, double columnWidth, double globalFontSize) {
        List<ChordSheetLine> broken = breakOversized(pairs, charLimit(columnWidth, globalFontSize));
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

    /** Splits an over-wide pair into pieces of at most {@code charLimit} columns, keeping chords over their syllables. */
    private List<ChordLyric> breakLine(ChordLyric pair, int charLimit, double fontSize) {
        List<ChordLyric> out = new ArrayList<>();
        String chords = pair.chords();
        String lyrics = pair.lyrics();
        int limit = Math.max(1, charLimit);
        while (Math.max(chords.length(), lyrics.length()) > limit) {
            int breakAt = findBreak(chords, lyrics, limit);
            String chords1 = head(chords, breakAt), lyrics1 = head(lyrics, breakAt);
            String chords2 = tail(chords, breakAt), lyrics2 = tail(lyrics, breakAt);

            // Drop the same indent from both rows so the second half stays aligned
            int indent = Math.min(leadingSpaces(chords2), leadingSpaces(lyrics2));
            chords2 = tail(chords2, indent);
            lyrics2 = tail(lyrics2, indent);

            if (!chords1.isBlank() || !lyrics1.isBlank()) out.add(equalized(chords1, lyrics1, fontSize));
            chords = chords2;
            lyrics = lyrics2;
        }
        if (!chords.isBlank() || !lyrics.isBlank()) out.add(equalized(chords, lyrics, fontSize));
        return out;
    }

    /** Latest column within the limit that splits neither a chord nor a word; failing that, protect the chord. */
    private static int findBreak(String chords, String lyrics, int limit) {
        for (int at = limit; at > 0; at--) {
            if (isCleanBreak(chords, at) && isCleanBreak(lyrics, at)) return at;
        }
        for (int at = limit; at > 0; at--) {
            if (isCleanBreak(chords, at)) return at;
        }
        return limit;
    }

    private static boolean isCleanBreak(String s, int at) {
        return at >= s.length() || s.charAt(at - 1) == ' ' || s.charAt(at) == ' ';
    }

    private static int leadingSpaces(String s) {
        if (s.isBlank()) return Integer.MAX_VALUE;
        int n = 0;
        while (s.charAt(n) == ' ') n++;
        return n;
    }

    private static String head(String s, int end) {
        return s.substring(0, Math.min(end, s.length()));
    }

    private static String tail(String s, int start) {
        return start >= s.length() ? "" : s.substring(start);
    }
}
