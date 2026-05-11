package org.jones.licklibrary.service;

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
    List<Position> build(List<IntervalNote> intervals, Note key, int spanLimit) {
        List<Note> absoluteNotes = LickUtils.toAbsoluteNotes(intervals, key);
        List<TabNote> rootCandidates = findNeckPositions(absoluteNotes.get(0));
        List<Position> results = new ArrayList<>();
        for (TabNote root : rootCandidates) {
            if (root.fret() > MAX_FRET) continue;
            buildPath(root, intervals, absoluteNotes, spanLimit).ifPresent(results::add);
            if (results.size() >= MAX_POSITIONS) break;
        }
        results.sort(Comparator.comparingInt(p ->
            p.notes().stream().mapToInt(TabNote::fret).max().orElse(0)));
        return results;
    }

    private Optional<Position> buildPath(TabNote root, List<IntervalNote> intervals,
            List<Note> absoluteNotes, int spanLimit) {

        List<TabNote> path = new ArrayList<>();
        Map<Integer, TabNote> placedByColumn = new HashMap<>();
        List<Integer> loserBracket = new ArrayList<>();
        int lastMelodicIdx = 0;

        // Place root
        TabNote rootNode = new TabNote(root.stringIndex(), root.fret(),
            intervals.get(0).columnIndex(), intervals.get(0).technique());
        path.add(rootNode);
        placedByColumn.put(intervals.get(0).columnIndex(), rootNode);

        // First pass: greedy for first-seen column indices
        for (int i = 1; i < absoluteNotes.size(); i++) {
            int colIdx = intervals.get(i).columnIndex();
            if (!placedByColumn.containsKey(colIdx)) {
                String technique = intervals.get(lastMelodicIdx).technique();
                List<TabNote> candidates = findCandidates(
                    path.get(path.size() - 1), absoluteNotes.get(i), technique);
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

        // Second pass: place chord partners near their parallel notes
        for (int i : loserBracket) {
            int colIdx = intervals.get(i).columnIndex();
            TabNote parallel = placedByColumn.get(colIdx);
            List<TabNote> candidates = findCandidates(parallel, absoluteNotes.get(i), null);
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
            // no valid candidate → skip silently, keep partial position
        }

        return Optional.of(new Position(path));
    }
}
