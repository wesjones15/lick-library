package org.jones.licklibrary.domain.lick;

import org.jones.licklibrary.core.exception.ResourceNotFoundException;
import org.jones.licklibrary.domain.lick.dto.LickResponse;
import org.jones.licklibrary.domain.lick.dto.PositionResponse;
import org.jones.licklibrary.domain.lick.dto.UploadLickRequest;
import org.jones.licklibrary.domain.position.LickUtils;
import org.jones.licklibrary.domain.position.PositionCacheRepository;
import org.jones.licklibrary.domain.position.builder.DfsPositionBuilder;
import org.jones.licklibrary.domain.position.builder.GreedyPositionBuilder;
import org.jones.licklibrary.domain.position.builder.LoserBracketPositionBuilder;
import org.jones.licklibrary.domain.position.builder.PositionBuilder;
import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Mode;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.Position;
import org.jones.licklibrary.domain.shared.TabNote;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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

    public LickResponse uploadLick(UploadLickRequest request, Instrument inst, String instrumentName) {
        if (request.rawTab() == null || request.rawTab().isBlank()) {
            throw new IllegalArgumentException("rawTab must not be blank");
        }
        List<TabNote> notes = parseTab(request.rawTab());
        Note rootKey = request.inputKey() != null ? request.inputKey()
                : inst.getNoteAt(notes.get(0).stringIndex(), notes.get(0).fret());
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, rootKey, inst);
        String hash = LickUtils.hashIntervals(intervals);

        Optional<Lick> existing = lickRepository.findByIntervalHashAndInstrumentAndAutoImportedFalse(hash, instrumentName);
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
        lick.setInstrument(instrumentName);
        lick = lickRepository.save(lick);
        return toSummaryResponse(lick);
    }

    List<TabNote> parseTab(String rawTab) {
        String[] strings = rawTab.split("\n");
        List<TabNote> out = new ArrayList<>();
        for (int i = 0; i < strings.length; i++) {
            String line = strings[i];
            for (int j = 2; j < line.length(); j++) {
                if (Character.isDigit(line.charAt(j))) {
                    int fretNum;
                    int techniqueIdx;
                    if (j + 1 < line.length() && Character.isDigit(line.charAt(j + 1))) {
                        fretNum = (line.charAt(j) - '0') * 10 + (line.charAt(j + 1) - '0');
                        techniqueIdx = j + 2;
                        j++;
                    } else {
                        fretNum = line.charAt(j) - '0';
                        techniqueIdx = j + 1;
                    }
                    String technique = "";
                    if (techniqueIdx < line.length()) {
                        char next = line.charAt(techniqueIdx);
                        if (next == 'h' || next == 'p' || next == '/' || next == '\\') {
                            technique = String.valueOf(next);
                        }
                    }
                    out.add(new TabNote(i, fretNum, j - 2, technique));
                }
            }
        }
        out.sort(Comparator.comparing(TabNote::columnIndex));
        return out;
    }

    public UUID uploadSongLick(String rawTab) {
        List<TabNote> notes = parseTab(rawTab);
        if (notes.isEmpty()) return null;
        Note rootKey = Guitar.STANDARD.getNoteAt(notes.get(0).stringIndex(), notes.get(0).fret());
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, rootKey, Guitar.STANDARD);
        Mode mode = LickUtils.detectMode(intervals);
        int tabSpan = notes.stream().mapToInt(TabNote::fret).max().orElse(0)
                    - notes.stream().mapToInt(TabNote::fret).min().orElse(0);
        Lick lick = new Lick();
        lick.setIntervalHash(LickUtils.hashIntervals(intervals));
        lick.setIntervals(intervals);
        lick.setRawTab(rawTab);
        lick.setMode(mode);
        lick.setTabSpan(tabSpan);
        lick.setAutoImported(true);
        lick.setInstrument("GUITAR");
        return lickRepository.save(lick).getId();
    }

    public void deleteAutoImportedLicks(List<UUID> ids) {
        ids.forEach(lickRepository::deleteById);
    }

    // --- Lookup pipeline ---

    public List<LickResponse> getAllLicks(boolean includeSongLicks,
            String instrument, String mode,
            Integer minLength, Integer maxLength, String intervals) {
        List<Lick> all = includeSongLicks
            ? lickRepository.findAll()
            : lickRepository.findAllByAutoImportedFalse();
        List<String> intervalTokens = intervals != null ? parseIntervalTokens(intervals) : List.of();
        return all.stream()
            .filter(l -> instrument == null || instrument.equalsIgnoreCase(
                l.getInstrument() != null ? l.getInstrument() : "GUITAR"))
            .filter(l -> mode == null || (l.getMode() != null
                && l.getMode().name().equalsIgnoreCase(mode)))
            .filter(l -> minLength == null || (l.getTabSpan() != null && l.getTabSpan() >= minLength))
            .filter(l -> maxLength == null || (l.getTabSpan() != null && l.getTabSpan() <= maxLength))
            .filter(l -> intervalTokens.isEmpty() || hasContiguousSubsequence(l.getIntervals(), intervalTokens))
            .map(this::toSummaryResponse)
            .toList();
    }

    private List<String> parseIntervalTokens(String csv) {
        return Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    private boolean hasContiguousSubsequence(List<IntervalNote> intervals, List<String> tokens) {
        if (tokens.isEmpty()) return true;
        for (int i = 0; i <= intervals.size() - tokens.size(); i++) {
            boolean match = true;
            for (int j = 0; j < tokens.size(); j++) {
                if (!intervals.get(i + j).interval().displayName().equals(tokens.get(j))) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }

    public LickResponse getLick(UUID id, Note key, String algo, Instrument instrument) {
        Lick lick = lickRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lick not found: " + id));
        List<Position> positions = resolvePositions(lick, key, algo, instrument);
        return toLickResponse(lick, positions, instrument);
    }

    public boolean deleteLick(UUID id) {
        Lick lick = lickRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lick not found: " + id));
        if (lick.isAutoImported()) return false;
        lickRepository.deleteById(id);
        return true;
    }

    List<Position> resolvePositions(Lick lick, Note key, String algo, Instrument instrument) {
        int spanLimit = Math.max(4, lick.getTabSpan() != null ? lick.getTabSpan() : 4);
        PositionBuilder builder = switch (algo == null ? "" : algo.toLowerCase()) {
            case "dfs"   -> dfsBuilder;
            case "chord" -> loserBracketBuilder;
            default      -> greedyBuilder;
        };
        return builder.build(lick.getIntervals(), key, spanLimit, instrument);
    }

    LickResponse toLickResponse(Lick lick, List<Position> positions, Instrument instrument) {
        List<PositionResponse> positionResponses = positions.stream()
            .map(p -> new PositionResponse(p.toTabString(instrument)))
            .toList();
        return new LickResponse(
            lick.getId(),
            lick.getRawTab(),
            IntervalNoteListConverter.toDisplayString(lick.getIntervals()),
            lick.getMode(),
            positionResponses,
            lick.isAutoImported(),
            lick.getInstrument()
        );
    }

    LickResponse toSummaryResponse(Lick lick) {
        return new LickResponse(
            lick.getId(),
            lick.getRawTab(),
            IntervalNoteListConverter.toDisplayString(lick.getIntervals()),
            lick.getMode(),
            null,
            lick.isAutoImported(),
            lick.getInstrument()
        );
    }
}
