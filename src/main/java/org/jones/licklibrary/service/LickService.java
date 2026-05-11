package org.jones.licklibrary.service;

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

    static final int MAX_FRET = PositionBuilder.MAX_FRET;
    static final int MAX_POSITIONS = PositionBuilder.MAX_POSITIONS;

    private final LickRepository lickRepository;
    private final PositionCacheRepository positionCacheRepository;

    private final PositionBuilder greedyBuilder = new GreedyPositionBuilder();
    private final PositionBuilder dfsBuilder = new DfsPositionBuilder();
    private final PositionBuilder loserBracketBuilder = new LoserBracketPositionBuilder();

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

    public LickResponse getLick(UUID id, Note key, String algo) {
        Lick lick = lickRepository.findById(id)
            .orElseThrow(() -> new LickNotFoundException(id));
        List<Position> positions = resolvePositions(lick, key, algo);
        return toLickResponse(lick, positions);
    }

    public void deleteLick(UUID id) {
        if (!lickRepository.existsById(id)) throw new LickNotFoundException(id);
        lickRepository.deleteById(id);
    }

    List<Position> resolvePositions(Lick lick, Note key, String algo) {
        int spanLimit = Math.max(4, lick.getTabSpan() != null ? lick.getTabSpan() : 4);
        PositionBuilder builder = switch (algo == null ? "" : algo.toLowerCase()) {
            case "dfs"   -> dfsBuilder;
            case "chord" -> loserBracketBuilder;
            default      -> greedyBuilder;
        };
        return builder.build(lick.getIntervals(), key, spanLimit);
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
