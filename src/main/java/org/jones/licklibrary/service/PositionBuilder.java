package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Instrument;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Position;
import org.jones.licklibrary.model.TabNote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

abstract class PositionBuilder {

    static final int MAX_FRET = 15;
    static final int MAX_POSITIONS = 50;

    abstract List<Position> build(List<IntervalNote> intervals, Note key, int spanLimit, Instrument instrument);

    List<TabNote> findNeckPositions(Note note, Instrument instrument) {
        List<TabNote> out = new ArrayList<>();
        for (int string = 0; string < instrument.stringCount(); string++) {
            for (int fret = instrument.minFret(string); fret <= MAX_FRET; fret++) {
                if (instrument.getNoteAt(string, fret) == note) {
                    out.add(new TabNote(string, fret, 0, null));
                }
            }
        }
        return out;
    }

    List<TabNote> findCandidates(TabNote current, Note next, String technique, Instrument instrument) {
        int s = current.stringIndex();
        int minString = (technique != null && !technique.isEmpty()) ? s : Math.max(0, s - 2);
        int maxString = (technique != null && !technique.isEmpty()) ? s : Math.min(instrument.stringCount() - 1, s + 2);

        List<TabNote> candidates = new ArrayList<>();
        for (int string = minString; string <= maxString; string++) {
            for (int fret = instrument.minFret(string); fret <= MAX_FRET; fret++) {
                if (instrument.getNoteAt(string, fret) == next) {
                    candidates.add(new TabNote(string, fret, 0, null));
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(c -> LickUtils.proximityScore(current, c)));
        return candidates;
    }
}
