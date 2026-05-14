package org.jones.licklibrary.domain.position.builder;

import org.jones.licklibrary.domain.position.LickUtils;
import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.Position;
import org.jones.licklibrary.domain.shared.TabNote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class PositionBuilder {

    public static final int MAX_FRET = 15;
    public static final int MAX_POSITIONS = 50;

    public abstract List<Position> build(List<IntervalNote> intervals, Note key, int spanLimit, Instrument instrument);

    public List<TabNote> findNeckPositions(Note note, Instrument instrument) {
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

    public List<TabNote> findCandidates(TabNote current, Note next, String technique, Instrument instrument) {
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
