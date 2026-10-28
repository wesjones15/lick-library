package org.jones.licklibrary.domain.song.parsing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ChordSymbolTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "C", "Am", "F#m7", "Bb", "Ebmaj7", "Cmaj7#11", "E7#9", "A7b9", "C6/9", "Cm7b5", "Cø7", "C°7",
        "Caug", "C+", "CM7", "Cmin7", "C-7", "Dsus4", "D7sus4", "Csus", "Cadd9", "Cmadd9", "CaddG",
        "G/B", "D/F#", "Bb/D", "Am/G", "C5", "C13", "Cdim7", "B♭", "F♯m", "Gb5", "Calt", "C7no3"
    })
    void recognisesRealChords(String token) {
        assertThat(ChordSymbol.isChord(token)).as(token).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bridge", "And", "Baby", "Go", "Do", "Ebb", "Cab", "H", "am", "x2", "Chorus", "Add", "Em,"})
    void rejectsWords(String token) {
        assertThat(ChordSymbol.isChord(token)).as(token).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"N.C.", "NC", "n.c.", "/", "///", "x2", "4x", "-", "..."})
    void neutralTokens(String token) {
        assertThat(ChordSymbol.isNeutral(token)).as(token).isTrue();
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
        "C;      2;  D",
        "B;      1;  C",
        "Bb;     2;  C",
        "Cb;     1;  C",
        "E#;     0;  F",
        "Fb;     1;  F",
        "B♭m;    2;  Cm",
        "F♯7;    1;  G7",
        "G/B;    2;  A/C#",
        "D/F#;   3;  F/A",
        "(G);    2;  (A)",
        "(G;     2;  (A",
        "Bm);    2;  C#m)",
        "|G|;    2;  |A|",
        "Gm*;    2;  Am*",
        "Am7(b5);2;  Bm7(b5)",
        "C6/9;   2;  D6/9",
        "Cmaj7#11;1; C#maj7#11",
        "Gb5;    1;  G5",
        "N.C.;   2;  N.C.",
        "x2;     2;  x2",
        "(or;    2;  (or",
        "C;     -1;  B",
        "C;     13;  C#"
    })
    void transposesTokens(String token, int semitones, String expected) {
        assertThat(ChordSymbol.transpose(token, semitones, false)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
        "Bridge; Bridge",
        "Intro:; Intro:",
        "And;    And",
        "G;      A",
        "(Am7);  (Bm7)",
        "Dsus4;  Esus4"
    })
    void strictModeOnlyMovesRealChords(String token, String expected) {
        assertThat(ChordSymbol.transpose(token, 2, true)).isEqualTo(expected);
    }
}
