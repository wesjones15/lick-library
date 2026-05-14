package org.jones.licklibrary.domain.position;

import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.Interval;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Mode;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.TabNote;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LickUtils {

    private LickUtils() {}

    public static List<IntervalNote> toIntervals(List<TabNote> notes, Note rootKey, Instrument instrument) {
        List<IntervalNote> out = new ArrayList<>();
        int normalizedCol = 0;
        int lastRawCol = Integer.MIN_VALUE;
        for (TabNote tabNote : notes) {
            if (tabNote.columnIndex() != lastRawCol && lastRawCol != Integer.MIN_VALUE) {
                normalizedCol++;
            }
            lastRawCol = tabNote.columnIndex();
            Note note = instrument.getNoteAt(tabNote.stringIndex(), tabNote.fret());
            Interval interval = Interval.values()[(note.ordinal() - rootKey.ordinal() + 12) % 12];
            out.add(new IntervalNote(interval, tabNote.technique(), normalizedCol));
        }
        return out;
    }

    public static List<Note> toAbsoluteNotes(List<IntervalNote> intervals, Note key) {
        List<Note> out = new ArrayList<>();
        for (IntervalNote in : intervals) {
            out.add(key.shift(in.interval().ordinal()));
        }
        return out;
    }

    public static double proximityScore(TabNote from, TabNote to) {
        return Math.hypot(from.fret() - to.fret(), from.stringIndex() - to.stringIndex());
    }

    public static String hashIntervals(List<IntervalNote> intervals) {
        String input = intervals.stream()
            .map(n -> n.interval().displayName())
            .collect(Collectors.joining(","));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Mode detectMode(List<IntervalNote> intervals) {
        Set<Interval> present = intervals.stream()
            .map(IntervalNote::interval)
            .collect(Collectors.toSet());

        List<Mode> candidates = new ArrayList<>(List.of(Mode.values()));

        if (present.contains(Interval.FLAT_TWO))
            candidates.removeAll(List.of(Mode.IONIAN, Mode.DORIAN, Mode.LYDIAN, Mode.MIXOLYDIAN, Mode.AEOLIAN));
        if (present.contains(Interval.FLAT_THREE))
            candidates.removeAll(List.of(Mode.IONIAN, Mode.LYDIAN, Mode.MIXOLYDIAN));
        if (present.contains(Interval.FLAT_FIVE))
            candidates.removeAll(List.of(Mode.IONIAN, Mode.DORIAN, Mode.PHRYGIAN, Mode.MIXOLYDIAN, Mode.AEOLIAN));
        if (present.contains(Interval.FLAT_SIX))
            candidates.removeAll(List.of(Mode.IONIAN, Mode.DORIAN, Mode.LYDIAN, Mode.MIXOLYDIAN));
        if (present.contains(Interval.FLAT_SEVEN))
            candidates.removeAll(List.of(Mode.IONIAN, Mode.LYDIAN));

        if (candidates.isEmpty()) return Mode.IONIAN;
        if (candidates.size() == 1) return candidates.get(0);

        for (Mode m : List.of(Mode.IONIAN, Mode.AEOLIAN, Mode.DORIAN, Mode.MIXOLYDIAN, Mode.PHRYGIAN, Mode.LYDIAN, Mode.LOCRIAN)) {
            if (candidates.contains(m)) return m;
        }
        return candidates.get(0);
    }
}
