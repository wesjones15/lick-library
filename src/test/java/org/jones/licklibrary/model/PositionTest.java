package org.jones.licklibrary.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionTest {

    // Slots: [0,1,2], all width 1 → total row width = 1+1+1+1+1+1+1 = 7
    // e: -0----- B: ---2--- G: -----2-
    @Test
    void toTabString_simpleDescendingAcrossStrings() {
        Position p = new Position(List.of(
            new TabNote(5, 0, 0, null),
            new TabNote(4, 2, 1, null),
            new TabNote(3, 2, 2, null)
        ));
        String expected =
            "e|-0-----|\n" +
            "B|---2---|\n" +
            "G|-----2-|\n" +
            "D|-------|\n" +
            "A|-------|\n" +
            "E|-------|";
        assertEquals(expected, p.toTabString());
    }

    // Slots: [0,2,4], all width 1 → row width 7
    // G: -5/7--- (technique replaces separator between slot 0 and slot 2)
    // D: -----5-
    @Test
    void toTabString_techniqueReplacesHyphenBetweenNotes() {
        Position p = new Position(List.of(
            new TabNote(3, 5, 0, "/"),
            new TabNote(3, 7, 2, null),
            new TabNote(2, 5, 4, null)
        ));
        String expected =
            "e|-------|\n" +
            "B|-------|\n" +
            "G|-5/7---|\n" +
            "D|-----5-|\n" +
            "A|-------|\n" +
            "E|-------|";
        assertEquals(expected, p.toTabString());
    }

    // Single note → slots [0], width 1, row width = 1+1+1 = 3
    @Test
    void toTabString_singleNote() {
        Position p = new Position(List.of(
            new TabNote(3, 5, 0, null)
        ));
        String expected =
            "e|---|\n" +
            "B|---|\n" +
            "G|-5-|\n" +
            "D|---|\n" +
            "A|---|\n" +
            "E|---|";
        assertEquals(expected, p.toTabString());
    }

    // Two notes same string, adjacent cols → exactly 1 hyphen between them
    // Slots: [0,1], both width 1, row width = 1+1+1+1+1 = 5
    @Test
    void toTabString_twoNotesSameStringNoTechnique() {
        Position p = new Position(List.of(
            new TabNote(3, 5, 0, null),
            new TabNote(3, 7, 1, null)
        ));
        String expected =
            "e|-----|\n" +
            "B|-----|\n" +
            "G|-5-7-|\n" +
            "D|-----|\n" +
            "A|-----|\n" +
            "E|-----|";
        assertEquals(expected, p.toTabString());
    }

    // Two-digit fret: slot 0 width = 2, slot 1 width = 1
    // Row width = 1+2+1+1+1 = 6
    // G: -12---  D: ----5-
    @Test
    void toTabString_twoDigitFretWidensSlot() {
        Position p = new Position(List.of(
            new TabNote(3, 12, 0, null),
            new TabNote(2, 5, 1, null)
        ));
        String expected =
            "e|------|\n" +
            "B|------|\n" +
            "G|-12---|\n" +
            "D|----5-|\n" +
            "A|------|\n" +
            "E|------|";
        assertEquals(expected, p.toTabString());
    }

    @Test
    void toTabString_withGap() {
        Position p = new Position(List.of(
                new TabNote(3, 2, 0, null),
                new TabNote(2, 5, 1, null),
                new TabNote(3, 7, 2, null)
        ));
        String expected =
                "e|-------|\n" +
                "B|-------|\n" +
                "G|-2---7-|\n" +
                "D|---5---|\n" +
                "A|-------|\n" +
                "E|-------|";
        assertEquals(expected, p.toTabString());
    }

    @Test
    void toTabString_withDupeNote() {
        Position p = new Position(List.of(
                new TabNote(3, 2, 0, null),
                new TabNote(2, 5, 0, null),
                new TabNote(3, 7, 1, null)
        ));
        String expected =
                "e|-----|\n" +
                "B|-----|\n" +
                "G|-2-7-|\n" +
                "D|-5---|\n" +
                "A|-----|\n" +
                "E|-----|";
        assertEquals(expected, p.toTabString());
    }
}
