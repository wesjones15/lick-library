package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Instrument;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Position;
import org.jones.licklibrary.model.TabNote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class LoserBracketPositionBuilder extends PositionBuilder {

    @Override
    List<Position> build(List<IntervalNote> intervals, Note key, int spanLimit, Instrument instrument) {
        List<Note> absoluteNotes = LickUtils.toAbsoluteNotes(intervals, key);
        List<TabNote> rootCandidates = findNeckPositions(absoluteNotes.get(0), instrument);
        List<Position> results = new ArrayList<>();
        for (TabNote root : rootCandidates) {
            if (root.fret() > MAX_FRET) continue;
            buildPath(root, intervals, absoluteNotes, spanLimit, instrument).ifPresent(results::add);
            if (results.size() >= MAX_POSITIONS) break;
        }
        results.sort(Comparator.comparingInt(p ->
            p.notes().stream().mapToInt(TabNote::fret).max().orElse(0)));
        return results;
    }

    private Optional<Position> buildPath(TabNote root, List<IntervalNote> intervals,
            List<Note> absoluteNotes, int spanLimit, Instrument instrument) {

        List<TabNote> path = new ArrayList<>();
        Map<Integer, TabNote> placedByColumn = new HashMap<>();
        List<Integer> loserBracket = new ArrayList<>();
        int lastMelodicIdx = 0;

        TabNote rootNode = new TabNote(root.stringIndex(), root.fret(),
            intervals.get(0).columnIndex(), intervals.get(0).technique());
        path.add(rootNode);
        placedByColumn.put(intervals.get(0).columnIndex(), rootNode);

        for (int i = 1; i < absoluteNotes.size(); i++) {
            int colIdx = intervals.get(i).columnIndex();
            if (!placedByColumn.containsKey(colIdx)) {
                String technique = intervals.get(lastMelodicIdx).technique();
                List<TabNote> candidates = findCandidates(
                    path.get(path.size() - 1), absoluteNotes.get(i), technique, instrument);
                if (candidates.isEmpty()) return Optional.empty();
                TabNote node = new TabNote(candidates.get(0).stringIndex(), candidates.get(0).fret(),
                    colIdx, intervals.get(i).technique());
                int curMin = path.stream().mapToInt(TabNote::fret).min().orElse(node.fret());
                int curMax = path.stream().mapToInt(TabNote::fret).max().orElse(node.fret());
                if (node.fret() > MAX_FRET
                        || Math.max(curMax, node.fret()) - Math.min(curMin, node.fret()) > spanLimit)
                    return Optional.empty();
                path.add(node);
                placedByColumn.put(colIdx, node);
                lastMelodicIdx = i;
            } else {
                loserBracket.add(i);
            }
        }

        for (int i : loserBracket) {
            int colIdx = intervals.get(i).columnIndex();
            TabNote parallel = placedByColumn.get(colIdx);
            List<TabNote> candidates = findCandidates(parallel, absoluteNotes.get(i), null, instrument);
            for (TabNote candidate : candidates) {
                if (candidate.stringIndex() == parallel.stringIndex()) continue;
                if (candidate.fret() > MAX_FRET) continue;
                int curMin = path.stream().mapToInt(TabNote::fret).min().orElse(candidate.fret());
                int curMax = path.stream().mapToInt(TabNote::fret).max().orElse(candidate.fret());
                if (Math.max(curMax, candidate.fret()) - Math.min(curMin, candidate.fret()) > spanLimit) continue;
                path.add(new TabNote(candidate.stringIndex(), candidate.fret(),
                    colIdx, intervals.get(i).technique()));
                break;
            }
        }

        return Optional.of(new Position(path));
    }
}
