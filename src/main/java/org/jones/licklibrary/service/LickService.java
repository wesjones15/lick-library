package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Guitar;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.controller.LickNotFoundException;
import org.jones.licklibrary.model.*;
import org.jones.licklibrary.repository.LickRepository;
import org.jones.licklibrary.repository.PositionCacheRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class LickService {

    static final int MAX_FRET = 15;
    static final int MAX_POSITIONS = 50;

    private final LickRepository lickRepository;
    private final PositionCacheRepository positionCacheRepository;

    public LickService(LickRepository lickRepository,
                       PositionCacheRepository positionCacheRepository) {
        this.lickRepository = lickRepository;
        this.positionCacheRepository = positionCacheRepository;
    }

    // --- Upload pipeline ---

    public LickResponse uploadLick(UploadLickRequest request) {
        if (request.rawTab() == null || request.rawTab().isBlank()) {
            throw new IllegalArgumentException("rawTab must not be blank");
        }
        List<TabNote> notes = parseTab(request.rawTab());
        Note rootKey = request.inputKey() != null ? request.inputKey() : notes.get(0).toNote();
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, rootKey);
        String hash = LickUtils.hashIntervals(intervals);

        Optional<Lick> existing = lickRepository.findByIntervalHash(hash);
        if (existing.isPresent()) {
            return toSummaryResponse(existing.get());
        }

        Mode mode = request.mode() != null ? request.mode() : LickUtils.detectMode(intervals);

        int tabSpan = notes.stream().mapToInt(TabNote::fret).max().orElse(0)
                    - notes.stream().mapToInt(TabNote::fret).min().orElse(0);

        Lick lick = new Lick();
        lick.setIntervalHash(hash);
        lick.setIntervals(intervals);
        lick.setRawTab(request.rawTab());
        lick.setMode(mode);
        lick.setTabSpan(tabSpan);
        lick = lickRepository.save(lick);
        return toSummaryResponse(lick);
    }

    List<TabNote> parseTab(String rawTab) {
        String[] strings = rawTab.split("\n");
        List<TabNote> out = new ArrayList<>();
        // TODO: retool this to look at at least 3 chars
        //  at a time, and only grab numbers that are
        //  surrounded by - or technique. this will enable
        //  us to discern 2 digit fret nums
        for (int i = 0; i < strings.length; i++) {
            String line = strings[i];
            for (int j = 2; j < line.length(); j++) {
                String fret = String.valueOf(line.charAt(j));
                if (fret.matches("[0-9]")) {
                    String technique = "";
                    if (line.length()-1 > j) {
                        String nextChar = String.valueOf(line.charAt(j+1));
                        if (nextChar.matches("[hp/\\\\]")){
                            technique = nextChar;
                        }
                    }
                    TabNote note = new TabNote(i, Integer.valueOf(fret), j-2, technique);
                    out.add(note);
                }
            }
        }
        out.sort(Comparator.comparing(TabNote::columnIndex));
        return out;
    }

    // --- Lookup pipeline ---

    public List<LickResponse> getAllLicks() {
        return lickRepository.findAll().stream()
            .map(this::toSummaryResponse)
            .toList();
    }

    public LickResponse getLick(UUID id, Note key) {
        Lick lick = lickRepository.findById(id)
            .orElseThrow(() -> new LickNotFoundException(id));
        List<Position> positions = resolvePositions(lick, key);
        return toLickResponse(lick, positions);
    }

    public void deleteLick(UUID id) {
        if (!lickRepository.existsById(id)) throw new LickNotFoundException(id);
        lickRepository.deleteById(id);
    }

    List<Position> resolvePositions(Lick lick, Note key) {
        int spanLimit = Math.max(4, lick.getTabSpan() != null ? lick.getTabSpan() : 4);
        return findPositions(lick.getIntervals(), key, spanLimit);
    }

    List<Position> findPositions(List<IntervalNote> intervals, Note key) {
        return findPositions(intervals, key, 4);
    }

    List<Position> findPositions(List<IntervalNote> intervals, Note key, int spanLimit) {
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

        return results;
    }

    List<TabNote> findNeckPositions(Note note) {
        List<TabNote> out = new ArrayList<>();
        for (int string = 0; string < 6; string++) {
            for (int fret = 0; fret <= 24; fret++) {
                if (Guitar.getNoteAt(string, fret) == note) {
                    out.add(new TabNote(string, fret, 0, null));
                }
            }
        }
        return out;
    }

    List<TabNote> findCandidates(TabNote current, Note next, String technique) {
        int s = current.stringIndex();
        int minString = (technique != null && !technique.isEmpty()) ? s : Math.max(0, s - 2);
        int maxString = (technique != null && !technique.isEmpty()) ? s : Math.min(5, s + 2);

        List<TabNote> candidates = new ArrayList<>();
        for (int string = minString; string <= maxString; string++) {
            for (int fret = 0; fret <= 24; fret++) {
                if (Guitar.getNoteAt(string, fret) == next) {
                    candidates.add(new TabNote(string, fret, 0, null));
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(c -> LickUtils.proximityScore(current, c)));
        return candidates;
    }

    void buildPositions(TabNote root, List<IntervalNote> intervals, List<Note> absoluteNotes, int spanLimit, List<Position> results) {
        if (root.fret() > MAX_FRET) return;
        List<TabNote> path = new ArrayList<>();
        path.add(new TabNote(root.stringIndex(), root.fret(), intervals.get(0).columnIndex(), intervals.get(0).technique()));
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

    LickResponse toLickResponse(Lick lick, List<Position> positions) {
        List<PositionResponse> positionResponses = positions.stream()
            .map(p -> new PositionResponse(p.toTabString()))
            .toList();
        return new LickResponse(
            lick.getId(),
            lick.getRawTab(),
            IntervalNoteListConverter.toDisplayString(lick.getIntervals()),
            lick.getMode(),
            positionResponses
        );
    }

    LickResponse toSummaryResponse(Lick lick) {
        return new LickResponse(
            lick.getId(),
            lick.getRawTab(),
            IntervalNoteListConverter.toDisplayString(lick.getIntervals()),
            lick.getMode(),
            null
        );
    }
}
