package org.jones.licklibrary.domain.song;

import org.jones.licklibrary.domain.song.parsing.ChordLyric;
import org.jones.licklibrary.domain.song.parsing.ChordLyricListConverter;
import org.jones.licklibrary.domain.song.parsing.ChordSheetLine;
import org.jones.licklibrary.domain.song.parsing.ChordSheetParser;
import org.jones.licklibrary.domain.song.parsing.GuitarTabLine;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChordLyricListConverterTest {

    private final ChordSheetParser parser = new ChordSheetParser();
    private final ChordLyricListConverter converter = new ChordLyricListConverter();

    @Test
    void tabBlockRoundTripsCorrectly() throws IOException {
        String raw = loadResource("tablineparse_sample_file.txt");
        ChordSheetParser.ParseResult result = parser.parse(raw);

        assertThat(result.chordLines())
                .anyMatch(line -> line instanceof GuitarTabLine);

        String json = converter.convertToDatabaseColumn(result.chordLines());
        List<ChordSheetLine> reloaded = converter.convertToEntityAttribute(json);

        assertThat(reloaded)
                .anyMatch(line -> line instanceof GuitarTabLine gt && gt.tabLines().size() == 6);

        assertThat(reloaded)
                .filteredOn(line -> line instanceof ChordLyric)
                .allSatisfy(line -> {
                    ChordLyric cl = (ChordLyric) line;
                    assertThat(cl.chords()).isNotNull();
                    assertThat(cl.lyrics()).isNotNull();
                });
    }

    @Test
    void chordLyricOnlySheetRoundTrips() {
        String raw = "G  Em  C  D\nSome lyrics here\n\nA  F#m\nMore lyrics\n";
        ChordSheetParser.ParseResult result = parser.parse(raw);

        String json = converter.convertToDatabaseColumn(result.chordLines());
        List<ChordSheetLine> reloaded = converter.convertToEntityAttribute(json);

        assertThat(reloaded).hasSameSizeAs(result.chordLines());
        assertThat(reloaded)
                .noneMatch(line -> line instanceof GuitarTabLine);
        assertThat(reloaded)
                .filteredOn(line -> line instanceof ChordLyric)
                .allSatisfy(line -> {
                    ChordLyric cl = (ChordLyric) line;
                    assertThat(cl.chords()).isNotNull();
                    assertThat(cl.lyrics()).isNotNull();
                });
    }

    private String loadResource(String name) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            assertThat(is).as("%s must be on the test classpath", name).isNotNull();
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
