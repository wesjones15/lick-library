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
import java.util.Optional;

public class GreedyPositionBuilder extends PositionBuilder {

    @Override
    public List<Position> build(List<IntervalNote> intervals, Note key, int spanLimit, Instrument instrument) {
        List<Note> absoluteNotes = LickUtils.toAbsoluteNotes(intervals, key);
        List<TabNote> rootCandidates = findNeckPositions(absoluteNotes.get(0), instrument);
        List<Position> results = new ArrayList<>();
        for (TabNote root : rootCandidates) {
            if (root.fret() > MAX_FRET) continue;
            buildGreedyPath(root, intervals, absoluteNotes, spanLimit, instrument).ifPresent(results::add);
            if (results.size() >= MAX_POSITIONS) break;
        }
        results.sort(Comparator.comparingInt(p ->
            p.notes().stream().mapToInt(TabNote::fret).max().orElse(0)));
        return results;
    }

    private Optional<Position> buildGreedyPath(TabNote root, List<IntervalNote> intervals,
            List<Note> absoluteNotes, int spanLimit, Instrument instrument) {
        List<TabNote> path = new ArrayList<>();
        path.add(new TabNote(root.stringIndex(), root.fret(),
            intervals.get(0).columnIndex(), intervals.get(0).technique()));
        for (int i = 1; i < absoluteNotes.size(); i++) {
            String technique = intervals.get(i - 1).technique();
            List<TabNote> candidates = findCandidates(path.get(path.size() - 1), absoluteNotes.get(i), technique, instrument);
            if (candidates.isEmpty()) return Optional.empty();
            TabNote node = new TabNote(candidates.get(0).stringIndex(), candidates.get(0).fret(),
                intervals.get(i).columnIndex(), intervals.get(i).technique());
            path.add(node);
            int minFret = path.stream().mapToInt(TabNote::fret).min().orElse(0);
            int maxFret = path.stream().mapToInt(TabNote::fret).max().orElse(0);
            if (node.fret() > MAX_FRET || maxFret - minFret > spanLimit) return Optional.empty();
        }
        return Optional.of(new Position(path));
    }
}
