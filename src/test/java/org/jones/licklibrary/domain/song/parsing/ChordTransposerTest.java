package org.jones.licklibrary.domain.song.parsing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChordTransposerTest {

    private final ChordTransposer transposer = new ChordTransposer();

    private ChordLyric pair(String chords, String lyrics) {
        int len = Math.max(chords.length(), lyrics.length());
        return new ChordLyric(String.format("%-" + len + "s", chords), String.format("%-" + len + "s", lyrics), 12);
    }

    private ChordLyric transposeOne(ChordLyric pair, int semitones) {
        return (ChordLyric) transposer.transpose(List.of(pair), semitones).get(0);
    }

    @Test
    void zeroAndFullOctaveReturnInputUnchanged() {
        List<ChordSheetLine> lines = List.of(pair("G   C", "Hello there"));
        assertThat(transposer.transpose(lines, 0)).isSameAs(lines);
        assertThat(transposer.transpose(lines, 12)).isSameAs(lines);
        assertThat(transposer.transpose(lines, -12)).isSameAs(lines);
    }

    @Test
    void negativeSemitonesTransposeDown() {
        ChordLyric out = transposeOne(pair("C   G   Am", "la la la la"), -2);
        assertThat(out.chords().trim()).isEqualTo("Bb  F   Gm");
    }

    @Test
    void longerChordKeepsFollowingChordsInTheirColumns() {
        ChordLyric out = transposeOne(pair("C   F   G", "one two three"), 1);
        assertThat(out.chords()).startsWith("C#  F#  G#");
        assertThat(out.lyrics()).startsWith("one two three");
        assertThat(out.chords().indexOf("F#")).isEqualTo(4);
        assertThat(out.chords().indexOf("G#")).isEqualTo(8);
    }

    @Test
    void longerChordNeverMergesIntoTheNextOne() {
        ChordLyric out = transposeOne(pair("C F G", "a b c"), 1);
        assertThat(out.chords().trim().split("\\s+")).containsExactly("C#", "F#", "G#");
    }

    @Test
    void slashChordGrowingByTwoStillLeavesASeparator() {
        ChordLyric out = transposeOne(pair("C/F  G", "words here"), 1);
        assertThat(out.chords().trim().split("\\s+")).containsExactly("C#/F#", "G#");
    }

    @Test
    void shorterChordPadsSoNextChordStaysPut() {
        ChordLyric out = transposeOne(pair("Bb  Eb  F", "x"), 2);
        assertThat(out.chords().indexOf("F")).isEqualTo(4);
        assertThat(out.chords().indexOf("G")).isEqualTo(8);
    }

    @Test
    void lyricsAreNeverShifted() {
        String lyrics = "Here comes the sun, little darling";
        ChordLyric out = transposeOne(pair("G         Cmaj7     D", lyrics), 1);
        assertThat(out.lyrics().stripTrailing()).isEqualTo(lyrics);
        assertThat(out.chords().length()).isEqualTo(out.lyrics().length());
    }

    @Test
    void parenthesisedGroupsAndBarsAreTransposed() {
        ChordLyric out = transposeOne(pair("(G   C   D)  |Em|", ""), 2);
        assertThat(out.chords().trim().split("\\s+")).containsExactly("(A", "D", "E)", "|F#m|");
    }

    @Test
    void ncAndRepeatMarkersAreLeftAlone() {
        ChordLyric out = transposeOne(pair("N.C.  G  x2", ""), 2);
        assertThat(out.chords().trim().split("\\s+")).containsExactly("N.C.", "A", "x2");
    }

    @Test
    void tabHeaderChordsAreTransposedButLabelsAreNot() {
        GuitarTabLine tab = new GuitarTabLine("Bridge riff  G   C", List.of("e|--0--|"), 10);
        GuitarTabLine out = (GuitarTabLine) transposer.transpose(List.of(tab), 2).get(0);
        assertThat(out.header()).isEqualTo("Bridge riff  A   D");
        assertThat(out.tabLines()).isEqualTo(tab.tabLines());
    }

    @Test
    void blankChordRowsArePassedThrough() {
        ChordLyric lyricOnly = pair("", "just words");
        assertThat(transposeOne(lyricOnly, 3)).isSameAs(lyricOnly);
    }
}
