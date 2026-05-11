package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Position;
import org.jones.licklibrary.model.TabNote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class DfsPositionBuilder extends PositionBuilder {

    @Override
    List<Position> build(List<IntervalNote> intervals, Note key, int spanLimit) {
        List<Note> absoluteNotes = LickUtils.toAbsoluteNotes(intervals, key);
        List<TabNote> rootCandidates = findNeckPositions(absoluteNotes.get(0));

        List<Position> results = new ArrayList<>();
        for (TabNote root : rootCandidates) {
            if (results.size() >= MAX_POSITIONS) break;
            buildPositions(root, intervals, absoluteNotes, spanLimit, results);
        }

        Map<List<Integer>, Position> byShape = new LinkedHashMap<>();
        for (Position p : results) {
            List<Integer> shapeKey = buildDiversityKey(p);
            Position existing = byShape.get(shapeKey);
            if (existing == null) {
                byShape.put(shapeKey, p);
            } else {
                int pMin  = p.notes().stream().mapToInt(TabNote::fret).min().orElse(0);
                int exMin = existing.notes().stream().mapToInt(TabNote::fret).min().orElse(0);
                if (pMin < exMin) byShape.put(shapeKey, p);
            }
        }
        results = new ArrayList<>(byShape.values());

        results.sort(Comparator.comparingInt(p ->
            p.notes().stream().mapToInt(TabNote::fret).max().orElse(0)
        ));

        // Round-robin by starting string so consecutive positions differ visually
        Map<Integer, List<Position>> byStartString = new LinkedHashMap<>();
        for (Position p : results) {
            byStartString.computeIfAbsent(p.notes().get(0).stringIndex(), k -> new ArrayList<>()).add(p);
        }
        List<Position> interleaved = new ArrayList<>();
        List<List<Position>> groups = new ArrayList<>(byStartString.values());
        int maxSize = groups.stream().mapToInt(List::size).max().orElse(0);
        for (int i = 0; i < maxSize; i++) {
            for (List<Position> group : groups) {
                if (i < group.size()) interleaved.add(group.get(i));
            }
        }

        return interleaved;
    }

    void buildPositions(TabNote root, List<IntervalNote> intervals, List<Note> absoluteNotes,
            int spanLimit, List<Position> results) {
        if (root.fret() > MAX_FRET) return;
        List<TabNote> path = new ArrayList<>();
        path.add(new TabNote(root.stringIndex(), root.fret(),
            intervals.get(0).columnIndex(), intervals.get(0).technique()));
        dfsPositions(path, intervals, absoluteNotes, 1, results, spanLimit);
    }

    private void dfsPositions(List<TabNote> path, List<IntervalNote> intervals,
            List<Note> absoluteNotes, int idx, List<Position> results, int spanLimit) {
        if (results.size() >= MAX_POSITIONS) return;
        if (idx == absoluteNotes.size()) {
            results.add(new Position(new ArrayList<>(path)));
            return;
        }
        TabNote prev = path.get(path.size() - 1);
        String technique = intervals.get(idx - 1).technique();
        List<TabNote> candidates = findCandidates(prev, absoluteNotes.get(idx), technique);
        int candidateCap = Math.max(4, 20 / absoluteNotes.size());
        int limit = Math.min(candidates.size(), candidateCap);
        for (TabNote candidate : candidates.subList(0, limit)) {
            TabNote node = new TabNote(candidate.stringIndex(), candidate.fret(),
                intervals.get(idx).columnIndex(), intervals.get(idx).technique());
            path.add(node);
            int minFret = path.stream().mapToInt(TabNote::fret).min().orElse(0);
            int maxFret = path.stream().mapToInt(TabNote::fret).max().orElse(0);
            if (node.fret() <= MAX_FRET && maxFret - minFret <= spanLimit) {
                dfsPositions(path, intervals, absoluteNotes, idx + 1, results, spanLimit);
            }
            path.remove(path.size() - 1);
        }
    }

    private static List<Integer> buildDiversityKey(Position p) {
        List<Integer> key = new ArrayList<>();
        for (TabNote n : p.notes()) {
            key.add(n.stringIndex());
        }
        int minFret = p.notes().stream().mapToInt(TabNote::fret).min().orElse(0);
        key.add(minFret / 5);
        return key;
    }
}
