package org.jones.licklibrary.domain.song;

import org.jones.licklibrary.domain.song.parsing.ChordLyric;
import org.jones.licklibrary.domain.song.parsing.ChordSheetLine;
import org.jones.licklibrary.domain.song.parsing.ChordSheetParser;
import org.jones.licklibrary.domain.song.parsing.GuitarTabLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChordSheetParserTest {

    private ChordSheetParser parser;
    private ChordSheetParser.ParseResult result;

    @BeforeEach
    void setUp() throws IOException {
        parser = new ChordSheetParser();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sample_songsheet.txt")) {
            assertThat(is).as("sample_songsheet.txt must be on the test classpath").isNotNull();
            String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            result = parser.parse(raw);
        }
    }

    @Test
    void parsesIntoNonEmptyList() {
        assertThat(result.chordLines()).isNotEmpty();
    }

    @Test
    void firstContentPairContainsDAndEm() {
        ChordLyric firstContent = result.chordLines().stream()
                .filter(line -> line instanceof ChordLyric cl && !cl.chords().isBlank())
                .map(line -> (ChordLyric) line)
                .findFirst()
                .orElseThrow();
        assertThat(firstContent.chords()).contains("D");
        assertThat(firstContent.chords()).contains("Em");
    }

    @Test
    void sectionLabelsAreAllSpaceChordRows() {
        result.chordLines().stream()
                .filter(line -> line instanceof ChordLyric cl
                        && cl.lyrics().trim().startsWith("[")
                        && cl.lyrics().trim().endsWith("]"))
                .map(line -> (ChordLyric) line)
                .forEach(cl -> assertThat(cl.chords().trim()).isEmpty());
    }

    @Test
    void slashChordsArePreservedIntact() {
        boolean foundSlash = result.chordLines().stream()
                .filter(line -> line instanceof ChordLyric)
                .map(line -> (ChordLyric) line)
                .anyMatch(cl -> cl.chords().contains("D/C#") || cl.chords().contains("G/F#"));
        assertThat(foundSlash).isTrue();
    }

    @Test
    void ncIsPreservedAndNotTransposed() {
        boolean foundNC = result.chordLines().stream()
                .filter(line -> line instanceof ChordLyric)
                .map(line -> (ChordLyric) line)
                .anyMatch(cl -> cl.chords().contains("NC"));
        assertThat(foundNC).isTrue();
        result.chordLines().stream()
                .filter(line -> line instanceof ChordLyric cl && cl.chords().contains("NC"))
                .map(line -> (ChordLyric) line)
                .forEach(cl -> assertThat(cl.chords()).doesNotContain("NCm").doesNotContain("NC#"));
    }

    @Test
    void blankLinesProduceSpacerPairs() {
        boolean hasBlankPair = result.chordLines().stream()
                .filter(line -> line instanceof ChordLyric)
                .map(line -> (ChordLyric) line)
                .anyMatch(cl -> cl.chords().isBlank() && cl.lyrics().isBlank());
        assertThat(hasBlankPair).isTrue();
    }

    @Test
    void allChordLyricPairsHaveEqualLengthStrings() {
        result.chordLines().stream()
                .filter(line -> line instanceof ChordLyric)
                .map(line -> (ChordLyric) line)
                .forEach(cl ->
                        assertThat(cl.chords().length())
                                .as("chords and lyrics must be same length: chords='%s' lyrics='%s'",
                                        cl.chords(), cl.lyrics())
                                .isEqualTo(cl.lyrics().length())
                );
    }

    @Test
    void allFontSizesAreWithinBounds() {
        result.chordLines().forEach(line -> {
            double fontSize = line instanceof GuitarTabLine gt ? gt.fontSize()
                    : ((ChordLyric) line).fontSize();
            assertThat(fontSize)
                    .as("fontSize out of bounds for line: %s", line)
                    .isBetween(8.0, 20.0);
        });
    }

    @Test
    void numColumnsIsReasonable() {
        assertThat(result.numColumns()).isBetween(1, 4);
    }

    @Test
    void sampleSongFitsVerticallyWithinContentHeight() {
        assertThat(result.numColumns()).isBetween(1, 4);

        double fontSize = result.chordLines().stream()
                .filter(line -> line instanceof ChordLyric cl
                        && (!cl.chords().isBlank() || !cl.lyrics().isBlank()))
                .map(line -> (ChordLyric) line)
                .mapToDouble(ChordLyric::fontSize)
                .findFirst()
                .orElseThrow();
        assertThat(fontSize).isBetween(8.0, 20.0);

        int pairsPerCol = (int) Math.ceil((double) result.chordLines().size() / result.numColumns());
        double estimatedHeight = pairsPerCol * 2.5 * fontSize;
        assertThat(estimatedHeight).isLessThanOrEqualTo(660.0);
    }

    @Test
    void shortSongUsesAtMostTwoColumns() {
        String raw = "G  Em  C  D\nFirst verse lyrics here\n\nG  Em  C  D\nSecond verse lyrics\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        assertThat(r.numColumns()).isLessThanOrEqualTo(2);
    }

    @Test
    void longSongUsesMultipleColumns() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("G  Em  C  D\nVerse lyrics line ").append(i).append("\n");
        }
        ChordSheetParser.ParseResult r = parser.parse(sb.toString());
        assertThat(r.numColumns()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void pipeSeparatorsRecognizedAsChordLine() {
        String raw = "| G Am | F C |\nSome lyrics here\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        ChordLyric first = (ChordLyric) r.chordLines().get(0);
        assertThat(first.chords()).contains("G");
        assertThat(first.lyrics()).contains("Some lyrics");
    }

    @Test
    void hyphenSeparatorsRecognizedAsChordLine() {
        String raw = "C - G/B - Am - G\nSome lyrics here\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        ChordLyric first = (ChordLyric) r.chordLines().get(0);
        assertThat(first.chords()).contains("C");
        assertThat(first.chords()).contains("G/B");
        assertThat(first.lyrics()).contains("Some lyrics");
    }

    @Test
    void trailingAsterisksRecognizedAsChordLine() {
        String raw = "G Gm* D\nSome lyrics\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        ChordLyric first = (ChordLyric) r.chordLines().get(0);
        assertThat(first.chords()).contains("Gm*");
        assertThat(first.lyrics()).contains("Some lyrics");
    }

    @Test
    void pureAnnotationLineIsLyricLine() {
        String raw = "G Em\nfirst line\n(Verse 2)\nSome lyrics here\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        boolean annotationIsInLyrics = r.chordLines().stream()
                .filter(line -> line instanceof ChordLyric)
                .map(line -> (ChordLyric) line)
                .anyMatch(cl -> cl.lyrics().contains("Verse 2"));
        assertThat(annotationIsInLyrics).isTrue();
    }

    @Test
    void parentheticalAnnotationDoesNotBlockChordLineDetection() {
        String raw = "G (or Bm) Em D\nSome lyrics here\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        ChordLyric first = (ChordLyric) r.chordLines().get(0);
        assertThat(first.chords()).contains("G");
        assertThat(first.chords()).contains("(or Bm)");
        assertThat(first.lyrics()).contains("Some lyrics");
    }

    @Test
    void parenthesizedChordsIdentifiedAsChordLine() {
        String raw = "D   (G)   Em\nThese are the lyrics\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        assertThat(r.chordLines()).isNotEmpty();
        ChordLyric first = (ChordLyric) r.chordLines().get(0);
        assertThat(first.chords()).contains("(G)");
        assertThat(first.lyrics()).contains("These are the lyrics");
    }

    @Test
    void parentheticalQualifierChordsIdentifiedAsChordLine() {
        String raw = " G(add9)   D\nSome lyrics here\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        ChordLyric first = (ChordLyric) r.chordLines().get(0);
        assertThat(first.chords()).contains("G(add9)");
        assertThat(first.lyrics()).contains("Some lyrics");
    }

    @Test
    void sus4AfterNumberChordsIdentifiedAsChordLine() {
        String raw = "D7sus4  G\nSome lyrics here\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        ChordLyric first = (ChordLyric) r.chordLines().get(0);
        assertThat(first.chords()).contains("D7sus4");
        assertThat(first.lyrics()).contains("Some lyrics");
    }

    @Test
    void addNoteNameChordsIdentifiedAsChordLine() {
        String raw = "CaddG  D\nSome lyrics here\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        ChordLyric first = (ChordLyric) r.chordLines().get(0);
        assertThat(first.chords()).contains("CaddG");
        assertThat(first.lyrics()).contains("Some lyrics");
    }

    @Test
    void tabBlockWithHeaderIsDetectedAsGuitarTabLine() {
        String raw = "C#m  F#  D   E\n"
                + "e|---4---2---2---0----|\n"
                + "B|---5---2---3---0----|\n"
                + "G|---6---3---2---1----|\n"
                + "D|---6---4---0---2----|\n"
                + "A|---4---4-------2----|\n"
                + "E|-------2-------0----|\n"
                + "\n"
                + "G  Em\nLyrics here\n";
        ChordSheetParser.ParseResult r = parser.parse(raw);
        assertThat(r.chordLines())
                .anyMatch(line -> line instanceof GuitarTabLine gt
                        && gt.header().contains("C#m")
                        && gt.tabLines().size() == 6);
    }
}
