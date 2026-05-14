package org.jones.licklibrary.domain.position.builder;

import org.jones.licklibrary.domain.position.LickUtils;
import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.Position;
import org.jones.licklibrary.domain.shared.TabNote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DfsPositionBuilder extends PositionBuilder {

    @Override
    public List<Position> build(List<IntervalNote> intervals, Note key, int spanLimit, Instrument instrument) {
        List<Note> absoluteNotes = LickUtils.toAbsoluteNotes(intervals, key);
        List<TabNote> rootCandidates = findNeckPositions(absoluteNotes.get(0), instrument);

        List<Position> results = new ArrayList<>();
        for (TabNote root : rootCandidates) {
            if (results.size() >= MAX_POSITIONS) break;
            buildPositions(root, intervals, absoluteNotes, spanLimit, results, instrument);
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

    public void buildPositions(TabNote root, List<IntervalNote> intervals, List<Note> absoluteNotes,
            int spanLimit, List<Position> results, Instrument instrument) {
        if (root.fret() > MAX_FRET) return;
        List<TabNote> path = new ArrayList<>();
        path.add(new TabNote(root.stringIndex(), root.fret(),
            intervals.get(0).columnIndex(), intervals.get(0).technique()));
        dfsPositions(path, intervals, absoluteNotes, 1, results, spanLimit, instrument);
    }

    private void dfsPositions(List<TabNote> path, List<IntervalNote> intervals,
            List<Note> absoluteNotes, int idx, List<Position> results, int spanLimit, Instrument instrument) {
        if (results.size() >= MAX_POSITIONS) return;
        if (idx == absoluteNotes.size()) {
            results.add(new Position(new ArrayList<>(path)));
            return;
        }
        TabNote prev = path.get(path.size() - 1);
        String technique = intervals.get(idx - 1).technique();
        List<TabNote> candidates = findCandidates(prev, absoluteNotes.get(idx), technique, instrument);
        int candidateCap = Math.max(4, 20 / absoluteNotes.size());
        int limit = Math.min(candidates.size(), candidateCap);
        for (TabNote candidate : candidates.subList(0, limit)) {
            TabNote node = new TabNote(candidate.stringIndex(), candidate.fret(),
                intervals.get(idx).columnIndex(), intervals.get(idx).technique());
            path.add(node);
            int minFret = path.stream().mapToInt(TabNote::fret).min().orElse(0);
            int maxFret = path.stream().mapToInt(TabNote::fret).max().orElse(0);
            if (node.fret() <= MAX_FRET && maxFret - minFret <= spanLimit) {
                dfsPositions(path, intervals, absoluteNotes, idx + 1, results, spanLimit, instrument);
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
