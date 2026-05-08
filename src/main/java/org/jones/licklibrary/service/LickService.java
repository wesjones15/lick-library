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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LickService {

    static final int MAX_FRET = 15;

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
        List<IntervalNote> intervals = LickUtils.toIntervals(notes);
        String hash = LickUtils.hashIntervals(intervals);

        Optional<Lick> existing = lickRepository.findByIntervalHash(hash);
        if (existing.isPresent()) {
            return toSummaryResponse(existing.get());
        }

        Mode mode = request.mode() != null ? request.mode() : LickUtils.detectMode(intervals);

        Lick lick = new Lick();
        lick.setIntervalHash(hash);
        lick.setIntervals(intervals);
        lick.setRawTab(request.rawTab());
        lick.setMode(mode);
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
        // MVP: always recompute; position cache skipped
        return findPositions(lick.getIntervals(), key);
    }

    List<Position> findPositions(List<IntervalNote> intervals, Note key) {
        List<Note> absoluteNotes = LickUtils.toAbsoluteNotes(intervals, key);
        List<TabNote> rootCandidates = findNeckPositions(absoluteNotes.get(0));

        List<Position> results = new ArrayList<>();
        for (TabNote root : rootCandidates) {
            results.addAll(buildPositions(root, intervals, absoluteNotes));
        }

        Set<String> seen = new HashSet<>();
        results = results.stream().filter(p -> seen.add(p.toTabString())).collect(Collectors.toCollection(ArrayList::new));

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
        int minString = (technique != null && !technique.isEmpty()) ? s : 0;
        int maxString = (technique != null && !technique.isEmpty()) ? s : 5;

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

    List<Position> buildPositions(TabNote root, List<IntervalNote> intervals, List<Note> absoluteNotes) {
        List<Position> results = new ArrayList<>();
        if (root.fret() > MAX_FRET) return results;
        List<TabNote> path = new ArrayList<>();
        path.add(new TabNote(root.stringIndex(), root.fret(), intervals.get(0).columnIndex(), intervals.get(0).technique()));
        dfsPositions(path, intervals, absoluteNotes, 1, results);
        return results;
    }

    private void dfsPositions(List<TabNote> path, List<IntervalNote> intervals,
            List<Note> absoluteNotes, int idx, List<Position> results) {
        if (idx == absoluteNotes.size()) {
            results.add(new Position(new ArrayList<>(path)));
            return;
        }
        TabNote prev = path.get(path.size() - 1);
        String technique = intervals.get(idx - 1).technique();
        for (TabNote candidate : findCandidates(prev, absoluteNotes.get(idx), technique)) {
            TabNote node = new TabNote(candidate.stringIndex(), candidate.fret(),
                intervals.get(idx).columnIndex(), intervals.get(idx).technique());
            path.add(node);
            int minFret = path.stream().mapToInt(TabNote::fret).min().orElse(0);
            int maxFret = path.stream().mapToInt(TabNote::fret).max().orElse(0);
            if (node.fret() <= MAX_FRET && maxFret - minFret <= 4) {
                dfsPositions(path, intervals, absoluteNotes, idx + 1, results);
            }
            path.remove(path.size() - 1);
        }
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
