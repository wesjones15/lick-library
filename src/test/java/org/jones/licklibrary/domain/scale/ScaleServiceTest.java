package org.jones.licklibrary.domain.scale;

import org.jones.licklibrary.domain.scale.dto.ScalePosition;
import org.jones.licklibrary.domain.scale.dto.ScaleResponse;
import org.jones.licklibrary.domain.shared.Mode;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.instrument.Bass;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.jones.licklibrary.domain.shared.instrument.Ukulele;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ScaleServiceTest {

    private final ScaleService service = new ScaleService();

    // --- Mode correctness — each mode's characteristic note ---

    @Test
    void getScale_ionian_hasAllSevenDegrees() {
        ScaleResponse res = service.getScale(Note.G, Mode.IONIAN, Guitar.STANDARD);
        Set<Integer> degrees = res.positions().stream()
                .map(ScalePosition::degree)
                .collect(Collectors.toSet());
        assertEquals(Set.of(1, 2, 3, 4, 5, 6, 7), degrees);
    }

    @Test
    void getScale_degreeOneIsAlwaysRoot_allModes() {
        for (Mode mode : Mode.values()) {
            ScaleResponse res = service.getScale(Note.C, mode, Guitar.STANDARD);
            boolean allDegreeOneAreC = res.positions().stream()
                    .filter(p -> p.degree() == 1)
                    .allMatch(p -> "C".equals(p.note()));
            assertTrue(allDegreeOneAreC,
                    "Degree 1 must always equal root C for mode " + mode);
        }
    }

    @Test
    void getScale_aeolian_degreeThreeIsMinorThird() {
        // A Aeolian: degree-3 = C (b3 from A; A→C = 3 semitones)
        ScaleResponse res = service.getScale(Note.A, Mode.AEOLIAN, Guitar.STANDARD);
        boolean hasC = res.positions().stream()
                .anyMatch(p -> p.degree() == 3 && "C".equals(p.note()));
        assertTrue(hasC, "A Aeolian degree-3 should be C (b3)");
    }

    @Test
    void getScale_dorian_degreeSevenIsFlatSeven() {
        // D Dorian: degree-7 = C (b7 from D; D→C = 10 semitones)
        ScaleResponse res = service.getScale(Note.D, Mode.DORIAN, Guitar.STANDARD);
        boolean hasC = res.positions().stream()
                .anyMatch(p -> p.degree() == 7 && "C".equals(p.note()));
        assertTrue(hasC, "D Dorian degree-7 should be C (b7)");
    }

    @Test
    void getScale_phrygian_degreeTwoIsFlatTwo() {
        // E Phrygian: degree-2 = F (b2 from E; E→F = 1 semitone)
        ScaleResponse res = service.getScale(Note.E, Mode.PHRYGIAN, Guitar.STANDARD);
        boolean hasF = res.positions().stream()
                .anyMatch(p -> p.degree() == 2 && "F".equals(p.note()));
        assertTrue(hasF, "E Phrygian degree-2 should be F (b2)");
    }

    @Test
    void getScale_lydian_degreeFourIsAugmentedFourth() {
        // F Lydian: degree-4 = B (#4 from F; F→B = 6 semitones)
        ScaleResponse res = service.getScale(Note.F, Mode.LYDIAN, Guitar.STANDARD);
        boolean hasB = res.positions().stream()
                .anyMatch(p -> p.degree() == 4 && "B".equals(p.note()));
        assertTrue(hasB, "F Lydian degree-4 should be B (#4)");
    }

    @Test
    void getScale_mixolydian_degreeSevenIsFlatSeven() {
        // G Mixolydian: degree-7 = F (b7 from G; G→F = 10 semitones)
        ScaleResponse res = service.getScale(Note.G, Mode.MIXOLYDIAN, Guitar.STANDARD);
        boolean hasF = res.positions().stream()
                .anyMatch(p -> p.degree() == 7 && "F".equals(p.note()));
        assertTrue(hasF, "G Mixolydian degree-7 should be F (b7)");
    }

    @Test
    void getScale_locrian_degreesTwoAndFiveAreFlat() {
        // B Locrian: degree-2 = C (b2; B→C = 1 semitone), degree-5 = F (b5; B→F = 6 semitones)
        ScaleResponse res = service.getScale(Note.B, Mode.LOCRIAN, Guitar.STANDARD);
        boolean hasFlatTwo = res.positions().stream()
                .anyMatch(p -> p.degree() == 2 && "C".equals(p.note()));
        boolean hasFlatFive = res.positions().stream()
                .anyMatch(p -> p.degree() == 5 && "F".equals(p.note()));
        assertTrue(hasFlatTwo, "B Locrian degree-2 should be C (b2)");
        assertTrue(hasFlatFive, "B Locrian degree-5 should be F (b5)");
    }

    // --- Multi-instrument — string count constrains positions ---

    @Test
    void getScale_bass_fewerPositionsThanGuitar() {
        ScaleResponse guitar = service.getScale(Note.G, Mode.IONIAN, Guitar.STANDARD);
        ScaleResponse bass   = service.getScale(Note.G, Mode.IONIAN, Bass.STANDARD);
        assertTrue(bass.positions().size() < guitar.positions().size(),
                "Bass (4 strings) should produce fewer scale positions than Guitar (6 strings)");
    }

    @Test
    void getScale_bass_stringIndicesNeverExceedThree() {
        ScaleResponse res = service.getScale(Note.A, Mode.AEOLIAN, Bass.STANDARD);
        assertFalse(res.positions().isEmpty(), "Bass A Aeolian should have positions");
        res.positions().forEach(p ->
                assertTrue(p.string() <= 3,
                        "Bass string index must be 0–3, got " + p.string()));
    }

    @Test
    void getScale_ukulele_stringIndicesNeverExceedThree() {
        ScaleResponse res = service.getScale(Note.C, Mode.IONIAN, Ukulele.STANDARD);
        assertFalse(res.positions().isEmpty(), "Ukulele C Ionian should have positions");
        res.positions().forEach(p ->
                assertTrue(p.string() <= 3,
                        "Ukulele string index must be 0–3, got " + p.string()));
    }

    @Test
    void getScale_ukulele_hasAllSevenDegrees() {
        ScaleResponse res = service.getScale(Note.C, Mode.IONIAN, Ukulele.STANDARD);
        Set<Integer> degrees = res.positions().stream()
                .map(ScalePosition::degree)
                .collect(Collectors.toSet());
        assertEquals(Set.of(1, 2, 3, 4, 5, 6, 7), degrees,
                "Ukulele C Ionian should have all 7 scale degrees within 12 frets");
    }

    // --- Fret boundary ---

    @Test
    void getScale_fretNeverExceedsMaxFretCount() {
        ScaleResponse res = service.getScale(Note.E, Mode.IONIAN, Guitar.STANDARD);
        res.positions().forEach(p ->
                assertTrue(p.fret() <= 12, "Fret must be ≤ 12, got " + p.fret()));
    }

    @Test
    void getScale_fretIsNonNegative() {
        ScaleResponse res = service.getScale(Note.G, Mode.DORIAN, Guitar.STANDARD);
        res.positions().forEach(p ->
                assertTrue(p.fret() >= 0, "Fret must be ≥ 0, got " + p.fret()));
    }

    // --- Response metadata ---

    @Test
    void getScale_responseRootAndModeMatchInputs() {
        ScaleResponse res = service.getScale(Note.D, Mode.DORIAN, Guitar.STANDARD);
        assertEquals("D", res.root());
        assertEquals("DORIAN", res.mode());
    }

    @Test
    void getScale_allPositionsHaveNonNullNote() {
        ScaleResponse res = service.getScale(Note.C, Mode.IONIAN, Guitar.STANDARD);
        res.positions().forEach(p -> assertNotNull(p.note(), "Position note must not be null"));
    }

    @Test
    void getScale_positionNoteMatchesExpectedNoteForDegree() {
        // G Ionian degree-4 = C (G→C = 5 semitones, natural 4th)
        ScaleResponse res = service.getScale(Note.G, Mode.IONIAN, Guitar.STANDARD);
        boolean hasDeg4C = res.positions().stream()
                .anyMatch(p -> p.degree() == 4 && "C".equals(p.note()));
        assertTrue(hasDeg4C, "G Ionian degree-4 should be C");
    }
}
