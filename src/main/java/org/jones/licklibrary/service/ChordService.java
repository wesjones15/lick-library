package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Guitar;
import org.jones.licklibrary.constants.Instrument;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.model.ChordShape;
import org.jones.licklibrary.repository.ChordShapeRepository;
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

    private final ChordShapeRepository shapeRepo;

    public ChordService(ChordShapeRepository shapeRepo) {
        this.shapeRepo = shapeRepo;
    }

    public boolean knowsQuality(String quality) {
        return CHORD_QUALITIES.containsKey(quality);
    }

    public List<String> getVoicings(Note root, String quality, Instrument instrument, String instrumentName) {
        List<ChordShape> shapes = shapeRepo.findByChordQuality_SuffixAndInstrument(quality, instrumentName.toUpperCase());
        return shapes.stream()
            .map(s -> transposeShape(s, root))
            .map(frets -> formatShape(frets, instrument))
            .collect(Collectors.toList());
    }

    static int[] transposeShape(ChordShape shape, Note root) {
        int[] template = parseTemplateFrets(shape.getTemplateFrets());
        Note openNote = Guitar.STANDARD.tuning()[shape.getRootString()];
        int targetFret = (root.ordinal() - openNote.ordinal() + 12) % 12;
        int offset = targetFret - template[shape.getRootString()];
        if (offset < 0) offset += 12;

        int[] result = new int[template.length];
        for (int i = 0; i < template.length; i++) {
            if (template[i] == -2 || template[i] == -1) {
                result[i] = template[i];
            } else {
                result[i] = template[i] + offset;
            }
        }
        return result;
    }

    static String formatShape(int[] frets, Instrument instrument) {
        int maxFret = 0;
        for (int f : frets) {
            if (f >= 0) maxFret = Math.max(maxFret, f);
        }
        int w = maxFret >= 10 ? 2 : 1;

        String[] labels = instrument.labels();
        int[] displayOrder = instrument.displayOrder();
        int stringCount = instrument.stringCount();

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < stringCount; i++) {
            if (result.length() > 0) result.append('\n');
            int stringIdx = displayOrder[i];
            int fret = frets[stringIdx];

            StringBuilder row = new StringBuilder("-");
            if (fret == -2) {
                row.append("x");
                for (int p = 1; p < w; p++) row.append('-');
            } else {
                String fretStr = fret == -1 ? "0" : String.valueOf(fret);
                row.append(fretStr);
                for (int p = fretStr.length(); p < w; p++) row.append('-');
            }
            row.append('-');
            result.append(labels[i]).append('|').append(row).append('|');
        }
        return result.toString();
    }

    static int[] parseTemplateFrets(String json) {
        String content = json.trim().replaceAll("[\\[\\]]", "");
        String[] parts = content.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim().replace("\"", "");
            result[i] = part.equals("x") ? -2 : Integer.parseInt(part);
        }
        return result;
    }
}
