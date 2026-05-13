package org.jones.licklibrary.service;

import org.jones.licklibrary.model.ChordLyric;
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
        List<ChordLyric> lines = result.chordLines();
        ChordLyric firstContent = lines.stream()
                .filter(cl -> !cl.chords().isBlank())
                .findFirst()
                .orElseThrow();
        assertThat(firstContent.chords()).contains("D");
        assertThat(firstContent.chords()).contains("Em");
    }

    @Test
    void sectionLabelsAreAllSpaceChordRows() {
        result.chordLines().stream()
                .filter(cl -> cl.lyrics().trim().startsWith("[") && cl.lyrics().trim().endsWith("]"))
                .forEach(cl -> assertThat(cl.chords().trim()).isEmpty());
    }

    @Test
    void slashChordsArePreservedIntact() {
        boolean foundSlash = result.chordLines().stream()
                .anyMatch(cl -> cl.chords().contains("D/C#") || cl.chords().contains("G/F#"));
        assertThat(foundSlash).isTrue();
    }

    @Test
    void ncIsPreservedAndNotTransposed() {
        boolean foundNC = result.chordLines().stream()
                .anyMatch(cl -> cl.chords().contains("NC"));
        assertThat(foundNC).isTrue();
        // NC should remain exactly "NC", not mutated to contain a note
        result.chordLines().stream()
                .filter(cl -> cl.chords().contains("NC"))
                .forEach(cl -> assertThat(cl.chords()).doesNotContain("NCm").doesNotContain("NC#"));
    }

    @Test
    void blankLinesProduceSpacerPairs() {
        boolean hasBlankPair = result.chordLines().stream()
                .anyMatch(cl -> cl.chords().isBlank() && cl.lyrics().isBlank());
        assertThat(hasBlankPair).isTrue();
    }

    @Test
    void allPairsHaveEqualLengthStrings() {
        result.chordLines().forEach(cl ->
                assertThat(cl.chords().length())
                        .as("chords and lyrics must be same length: chords='%s' lyrics='%s'",
                                cl.chords(), cl.lyrics())
                        .isEqualTo(cl.lyrics().length())
        );
    }

    @Test
    void allFontSizesAreWithinBounds() {
        result.chordLines().forEach(cl ->
                assertThat(cl.fontSize())
                        .as("fontSize out of bounds for pair: chords='%s'", cl.chords())
                        .isBetween(6.0, 12.0)
        );
    }

    @Test
    void numColumnsIsReasonable() {
        assertThat(result.numColumns()).isBetween(2, 3);
    }
}
