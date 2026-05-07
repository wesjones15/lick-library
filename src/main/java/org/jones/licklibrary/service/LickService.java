package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.*;
import org.jones.licklibrary.repository.LickRepository;
import org.jones.licklibrary.repository.PositionCacheRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LickService {

    private final LickRepository lickRepository;
    private final PositionCacheRepository positionCacheRepository;

    public LickService(LickRepository lickRepository,
                       PositionCacheRepository positionCacheRepository) {
        this.lickRepository = lickRepository;
        this.positionCacheRepository = positionCacheRepository;
    }

    // --- Upload pipeline ---

    public void uploadLick(String tab) {
        List<TabNote> notes = parseTab(tab);
        List<IntervalNote> intervals = toIntervals(notes);
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Parses a raw multi-line tab string into a time-ordered sequence of TabNotes.
     * Each of the 6 string lines is processed independently, then merged via column index.
     */
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

    String toNoteString(List<TabNote> tabnotes) {
        StringBuilder sb = new StringBuilder();
        for (TabNote tabnote : tabnotes) {
            Note note = tabnote.toNote();
            sb.append(note.toString());
            sb.append(" " + tabnote.technique());
        }
        return sb.toString();
    }

    /**
     * Converts an ordered TabNote sequence to IntervalNotes relative to the first note.
     * For simultaneous notes (same columnIndex), takes the first. First note is always ONE.
     */
    List<IntervalNote> toIntervals(List<TabNote> notes) {
        List<TabNote> melody = new ArrayList<>();
        int lastCol = Integer.MIN_VALUE;
        for (TabNote tabNote : notes) {
            if (tabNote.columnIndex() != lastCol) {
                melody.add(tabNote);
                lastCol = tabNote.columnIndex();
            }
        }
        Note first = melody.get(0).toNote();
        List<IntervalNote> out = new ArrayList<>();
        for (TabNote tabNote : melody) {
            Note note = tabNote.toNote();
            Interval interval = Interval.values()[(note.ordinal() - first.ordinal() + 12) % 12];
            out.add(new IntervalNote(interval, tabNote.technique()));
        }
        return out;
    }

    /**
     * SHA-256 hashes the interval sequence for use as a stable DB key.
     */
    String hashIntervals(List<IntervalNote> intervals) {
        throw new UnsupportedOperationException("TODO");
    }

    // --- Lookup pipeline ---

    public Page<LickResponse> getLicks(String key, String mode, int page) {
        throw new UnsupportedOperationException("TODO");
    }

    List<Position> resolvePositions(Lick lick, Note key) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Converts intervals to absolute notes for the given key, finds all valid
     * string/fret locations, generates combinations, filters by 4-fret span,
     * and ranks by span ascending.
     */
    List<Position> findPositions(List<Interval> intervals, Note key) {
        throw new UnsupportedOperationException("TODO");
    }

    LickResponse toLickResponse(Lick lick, List<Position> positions) {
        throw new UnsupportedOperationException("TODO");
    }
}
