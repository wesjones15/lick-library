package org.jones.licklibrary.domain.position.builder;

import org.jones.licklibrary.domain.position.LickUtils;
import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.Position;
import org.jones.licklibrary.domain.shared.TabNote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CrossInstrumentPositionBuilder extends PositionBuilder {

    @Override
    public List<Position> build(List<IntervalNote> intervals, Note key, int spanLimit, Instrument instrument) {
        List<IntervalNote> deduped = deduplicateChords(intervals);
        List<Note> absoluteNotes = LickUtils.toAbsoluteNotes(deduped, key);
        List<TabNote> rootCandidates = findNeckPositions(absoluteNotes.get(0), instrument);
        List<Position> results = new ArrayList<>();
        for (TabNote root : rootCandidates) {
            if (root.fret() > MAX_FRET) continue;
            buildPath(root, deduped, absoluteNotes, instrument).ifPresent(results::add);
            if (results.size() >= MAX_POSITIONS) break;
        }
        results.sort(Comparator.comparingInt(p ->
            p.notes().stream().mapToInt(TabNote::fret).max().orElse(0)));
        return results;
    }

    private Optional<Position> buildPath(TabNote root, List<IntervalNote> intervals,
            List<Note> absoluteNotes, Instrument instrument) {
        List<TabNote> path = new ArrayList<>();
        path.add(new TabNote(root.stringIndex(), root.fret(),
            intervals.get(0).columnIndex(), intervals.get(0).technique()));
        for (int i = 1; i < absoluteNotes.size(); i++) {
            String technique = intervals.get(i - 1).technique();
            List<TabNote> candidates = findCandidatesAllStrings(
                path.get(path.size() - 1), absoluteNotes.get(i), technique, instrument);
            if (candidates.isEmpty()) return Optional.empty();
            TabNote node = new TabNote(candidates.get(0).stringIndex(), candidates.get(0).fret(),
                intervals.get(i).columnIndex(), intervals.get(i).technique());
            path.add(node);
            if (node.fret() > MAX_FRET) return Optional.empty();
        }
        return Optional.of(new Position(path));
    }

    private List<TabNote> findCandidatesAllStrings(TabNote current, Note next,
            String technique, Instrument instrument) {
        int minStr = (technique != null && !technique.isEmpty()) ? current.stringIndex() : 0;
        int maxStr = (technique != null && !technique.isEmpty()) ? current.stringIndex()
                                                                 : instrument.stringCount() - 1;
        List<TabNote> candidates = new ArrayList<>();
        for (int s = minStr; s <= maxStr; s++) {
            for (int fret = instrument.minFret(s); fret <= MAX_FRET; fret++) {
                if (instrument.getNoteAt(s, fret) == next) {
                    candidates.add(new TabNote(s, fret, 0, null));
                }
            }
        }
        candidates.sort(Comparator.comparingInt(TabNote::fret)
                .thenComparingDouble(c -> LickUtils.proximityScore(current, c)));
        return candidates;
    }

    private List<IntervalNote> deduplicateChords(List<IntervalNote> intervals) {
        Set<String> seen = new LinkedHashSet<>();
        List<IntervalNote> result = new ArrayList<>();
        for (IntervalNote note : intervals) {
            if (seen.add(note.interval().name() + ":" + note.columnIndex())) {
                result.add(note);
            }
        }
        return result;
    }
}
