package org.jones.licklibrary.domain.scale;

import org.jones.licklibrary.domain.scale.dto.ScalePosition;
import org.jones.licklibrary.domain.scale.dto.ScaleResponse;
import org.jones.licklibrary.domain.shared.Mode;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScaleService {

    private static final int FRET_COUNT = 12;

    public ScaleResponse getScale(Note root, Mode mode) {
        int[] offsets = semitones(mode);
        List<ScalePosition> positions = new ArrayList<>();

        for (int degree = 1; degree <= 7; degree++) {
            Note scaleNote = root.shift(offsets[degree - 1]);
            for (int string = 0; string < Guitar.STANDARD.stringCount(); string++) {
                for (int fret = 0; fret <= FRET_COUNT; fret++) {
                    if (Guitar.STANDARD.getNoteAt(string, fret) == scaleNote) {
                        positions.add(new ScalePosition(string, fret, degree, scaleNote.name()));
                    }
                }
            }
        }

        return new ScaleResponse(root.name(), mode.name(), positions);
    }

    private static int[] semitones(Mode mode) {
        return switch (mode) {
            case IONIAN     -> new int[]{0, 2, 4, 5, 7, 9, 11};
            case DORIAN     -> new int[]{0, 2, 3, 5, 7, 9, 10};
            case PHRYGIAN   -> new int[]{0, 1, 3, 5, 7, 8, 10};
            case LYDIAN     -> new int[]{0, 2, 4, 6, 7, 9, 11};
            case MIXOLYDIAN -> new int[]{0, 2, 4, 5, 7, 9, 10};
            case AEOLIAN    -> new int[]{0, 2, 3, 5, 7, 8, 10};
            case LOCRIAN    -> new int[]{0, 1, 3, 5, 6, 8, 10};
        };
    }
}
