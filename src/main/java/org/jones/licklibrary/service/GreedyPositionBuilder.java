package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Position;
import org.jones.licklibrary.model.TabNote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class GreedyPositionBuilder extends PositionBuilder {

    @Override
    List<Position> build(List<IntervalNote> intervals, Note key, int spanLimit) {
        List<Note> absoluteNotes = LickUtils.toAbsoluteNotes(intervals, key);
        List<TabNote> rootCandidates = findNeckPositions(absoluteNotes.get(0));
        List<Position> results = new ArrayList<>();
        for (TabNote root : rootCandidates) {
            if (root.fret() > MAX_FRET) continue;
            buildGreedyPath(root, intervals, absoluteNotes, spanLimit).ifPresent(results::add);
            if (results.size() >= MAX_POSITIONS) break;
        }
        results.sort(Comparator.comparingInt(p ->
            p.notes().stream().mapToInt(TabNote::fret).max().orElse(0)));
        return results;
    }

    private Optional<Position> buildGreedyPath(TabNote root, List<IntervalNote> intervals,
            List<Note> absoluteNotes, int spanLimit) {
        List<TabNote> path = new ArrayList<>();
        path.add(new TabNote(root.stringIndex(), root.fret(),
            intervals.get(0).columnIndex(), intervals.get(0).technique()));
        for (int i = 1; i < absoluteNotes.size(); i++) {
            String technique = intervals.get(i - 1).technique();
            List<TabNote> candidates = findCandidates(path.get(path.size() - 1), absoluteNotes.get(i), technique);
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
