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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
                        if (nextChar.matches("[hp/]")){
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

    List<Position> resolvePositions(Lick lick, Note key) {
        // MVP: always recompute; position cache skipped
        return findPositions(lick.getIntervals(), key);
    }

    List<Position> findPositions(List<IntervalNote> intervals, Note key) {
        List<Note> absoluteNotes = LickUtils.toAbsoluteNotes(intervals, key);
        List<TabNote> rootCandidates = findNeckPositions(absoluteNotes.get(0));

        List<Position> results = new ArrayList<>();
        for (TabNote root : rootCandidates) {
            Position p = buildPosition(root, intervals, absoluteNotes);
            if (p != null) results.add(p);
        }

        results.removeIf(p -> {
            List<Integer> frets = p.notes().stream().map(TabNote::fret).toList();
            return frets.stream().mapToInt(Integer::intValue).max().orElse(0)
                 - frets.stream().mapToInt(Integer::intValue).min().orElse(0) > 4;
        });
        results.removeIf(p ->
            p.notes().stream().mapToInt(TabNote::fret).max().orElse(0) > MAX_FRET
        );

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
        int minString = (technique != null && !technique.isEmpty()) ? s : Math.max(0, s - 1);
        int maxString = (technique != null && !technique.isEmpty()) ? s : Math.min(5, s + 1);

        List<TabNote> candidates = new ArrayList<>();
        for (int string = minString; string <= maxString; string++) {
            for (int fret = 0; fret <= 24; fret++) {
                if (Guitar.getNoteAt(string, fret) == next) {
                    candidates.add(new TabNote(string, fret, 0, null));
                }
            }
        }
        candidates.sort(Comparator.comparingInt(c -> LickUtils.proximityScore(current, c)));
        return candidates;
    }

    Position buildPosition(TabNote root, List<IntervalNote> intervals, List<Note> absoluteNotes) {
        List<TabNote> sequence = new ArrayList<>();
        sequence.add(new TabNote(root.stringIndex(), root.fret(), intervals.get(0).columnIndex(), intervals.get(0).technique()));

        for (int i = 1; i < absoluteNotes.size(); i++) {
            String technique = intervals.get(i - 1).technique();
            List<TabNote> candidates = findCandidates(sequence.get(sequence.size() - 1), absoluteNotes.get(i), technique);
            if (candidates.isEmpty()) return null;
            TabNote best = candidates.get(0);
            sequence.add(new TabNote(best.stringIndex(), best.fret(), intervals.get(i).columnIndex(), intervals.get(i).technique()));
        }

        return new Position(sequence);
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
