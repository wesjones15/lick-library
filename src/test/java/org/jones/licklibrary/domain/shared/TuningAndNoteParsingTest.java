package org.jones.licklibrary.domain.shared;

import org.jones.licklibrary.domain.shared.instrument.Bass;
import org.jones.licklibrary.domain.shared.instrument.CustomInstrument;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.jones.licklibrary.domain.shared.Note.*;

class TuningAndNoteParsingTest {

    @Test
    void shiftHandlesNegativeAndLargeOffsets() {
        assertEquals(B, C.shift(-1));
        assertEquals(C, C.shift(-12));
        assertEquals(C_SHARP, C.shift(25));
    }

    @ParameterizedTest
    @CsvSource({
        "C_SHARP, C_SHARP", "B_FLAT, B_FLAT", "c#, C_SHARP", "Db, C_SHARP", "B♭, B_FLAT",
        "F♯, F_SHARP", "Cb, B", "Fb, E", "E#, F", "B#, C", "' A ', A"
    })
    void parsesEveryStoredAndTypedSpelling(String input, Note expected) {
        assertEquals(expected, NoteParser.parse(input));
    }

    @Test
    void ebTuningSoundsAHalfStepBelowItsShapes() {
        assertEquals(-1, Guitar.EB.soundingOffset());
        assertEquals(0, Guitar.STANDARD.soundingOffset());
        assertEquals(0, Guitar.DROP_D.soundingOffset());
        assertEquals(0, Bass.STANDARD.soundingOffset());
    }

    @Test
    void evenlyShiftedCustomTuningsPlayStandardShapes() {
        assertEquals(-2, new CustomInstrument(new Note[]{D, G, C, F, A, D}).soundingOffset());
        assertEquals(-1, new CustomInstrument(new Note[]{D_SHARP, G_SHARP, C_SHARP, F_SHARP, B_FLAT, D_SHARP}).soundingOffset());
        assertEquals(1, new CustomInstrument(new Note[]{F, B_FLAT, D_SHARP, G_SHARP, C, F}).soundingOffset());
        assertEquals(0, new CustomInstrument(new Note[]{D, A, D, G, B, E}).soundingOffset());
        assertEquals(0, new CustomInstrument(new Note[]{G, C, E, A}).soundingOffset());
    }

    @Test
    void ebLabelledTabIsDetectedAsTheEbPreset() {
        Note[] tuning = {D_SHARP, G_SHARP, C_SHARP, F_SHARP, B_FLAT, D_SHARP};
        assertSame(Guitar.EB, InstrumentRegistry.fromTuning(tuning));
    }
}
