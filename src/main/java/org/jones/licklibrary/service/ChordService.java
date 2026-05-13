package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Instrument;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.Position;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChordService {

    private static final Map<String, List<Interval>> CHORD_QUALITIES = Map.ofEntries(
        Map.entry("",      List.of(Interval.ONE, Interval.THREE, Interval.FIVE)),
        Map.entry("m",     List.of(Interval.ONE, Interval.FLAT_THREE, Interval.FIVE)),
        Map.entry("7",     List.of(Interval.ONE, Interval.THREE, Interval.FIVE, Interval.FLAT_SEVEN)),
        Map.entry("maj7",  List.of(Interval.ONE, Interval.THREE, Interval.FIVE, Interval.SEVEN)),
        Map.entry("m7",    List.of(Interval.ONE, Interval.FLAT_THREE, Interval.FIVE, Interval.FLAT_SEVEN)),
        Map.entry("sus2",  List.of(Interval.ONE, Interval.TWO, Interval.FIVE)),
        Map.entry("sus4",  List.of(Interval.ONE, Interval.FOUR, Interval.FIVE)),
        Map.entry("dim",   List.of(Interval.ONE, Interval.FLAT_THREE, Interval.FLAT_FIVE)),
        Map.entry("aug",   List.of(Interval.ONE, Interval.THREE, Interval.FLAT_SIX)),
        Map.entry("add9",  List.of(Interval.ONE, Interval.THREE, Interval.FIVE, Interval.TWO)),
        Map.entry("6",     List.of(Interval.ONE, Interval.THREE, Interval.FIVE, Interval.SIX)),
        Map.entry("m6",    List.of(Interval.ONE, Interval.FLAT_THREE, Interval.FIVE, Interval.SIX)),
        Map.entry("dim7",  List.of(Interval.ONE, Interval.FLAT_THREE, Interval.FLAT_FIVE, Interval.SIX)),
        Map.entry("m7b5",  List.of(Interval.ONE, Interval.FLAT_THREE, Interval.FLAT_FIVE, Interval.FLAT_SEVEN))
    );

    private final LoserBracketPositionBuilder builder = new LoserBracketPositionBuilder();

    public boolean knowsQuality(String quality) {
        return CHORD_QUALITIES.containsKey(quality);
    }

    public List<String> getVoicings(Note root, String quality, Instrument instrument) {
        List<Interval> intervals = CHORD_QUALITIES.get(quality);
        if (intervals == null) throw new IllegalArgumentException("Unknown chord quality: " + quality);

        List<IntervalNote> notes = intervals.stream()
            .map(i -> new IntervalNote(i, "", 0))
            .collect(Collectors.toList());

        List<Position> positions = builder.build(notes, root, 4, instrument);
        return positions.stream()
            .map(p -> p.toTabString(instrument))
            .collect(Collectors.toList());
    }
}
