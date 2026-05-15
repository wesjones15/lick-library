package org.jones.licklibrary.domain.chord;

import org.jones.licklibrary.domain.chord.dto.UploadChordRequest;
import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.Interval;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        Map.entry("m7b5",  List.of(Interval.ONE, Interval.FLAT_THREE, Interval.FLAT_FIVE, Interval.FLAT_SEVEN)),
        Map.entry("/4",   List.of()),
        Map.entry("m/3",  List.of()),
        Map.entry("7/4",  List.of()),
        Map.entry("/7",   List.of())
    );

    private final ChordShapeRepository shapeRepo;
    private final ChordQualityRepository qualityRepo;

    public ChordService(ChordShapeRepository shapeRepo, ChordQualityRepository qualityRepo) {
        this.shapeRepo = shapeRepo;
        this.qualityRepo = qualityRepo;
    }

    public boolean knowsQuality(String quality) {
        return CHORD_QUALITIES.containsKey(quality);
    }

    public Map<String, List<String>> getAllVoicings(Note root, Instrument instrument, String instrumentName) {
        List<String> suffixes = shapeRepo.findDistinctQualitiesByInstrument(instrumentName.toUpperCase());
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String suffix : suffixes) {
            result.put(suffix, getVoicings(root, suffix, instrument, instrumentName));
        }
        return result;
    }

    public List<String> getVoicings(Note root, String quality, Instrument instrument, String instrumentName) {
        List<ChordShape> shapes = shapeRepo.findByChordQuality_SuffixAndInstrument(quality, instrumentName.toUpperCase());
        return shapes.stream()
            .map(s -> transposeShape(s, root))
            .sorted(Comparator.comparingInt(ChordService::minFret))
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

    private static int minFret(int[] frets) {
        return Arrays.stream(frets)
            .filter(f -> f != -2)
            .map(f -> f == -1 ? 0 : f)
            .min()
            .orElse(0);
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

    public UUID uploadChord(UploadChordRequest req) {
        Note root;
        try {
            root = Note.valueOf(req.root().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown root note: " + req.root());
        }

        List<String> rawFrets = req.frets();
        if (rawFrets == null || rawFrets.size() != 6) {
            throw new IllegalArgumentException("Exactly 6 fret values required");
        }
        int[] frets = new int[6];
        for (int i = 0; i < 6; i++) {
            String f = rawFrets.get(i).trim();
            if (f.equalsIgnoreCase("x")) {
                frets[i] = -2;
            } else {
                try {
                    int v = Integer.parseInt(f);
                    if (v < 0) throw new IllegalArgumentException("Fret value must be >= 0");
                    frets[i] = v;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid fret value: " + f);
                }
            }
        }

        Note[] tuning = Guitar.STANDARD.tuning();
        int rootString = -1;
        for (int i = 0; i < tuning.length; i++) {
            if (frets[i] == -2) continue;
            if (tuning[i].shift(frets[i]) == root) {
                rootString = i;
                break;
            }
        }
        if (rootString == -1) {
            throw new IllegalArgumentException("No string produces the root note " + req.root() + " with the given frets");
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < frets.length; i++) {
            if (i > 0) json.append(',');
            if (frets[i] == -2) json.append('"').append('x').append('"');
            else json.append(frets[i]);
        }
        json.append(']');

        String suffix = req.quality() != null ? req.quality() : "";
        ChordQuality quality = qualityRepo.findBySuffix(suffix)
            .orElseGet(() -> qualityRepo.save(new ChordQuality(suffix)));

        String instrumentName = (req.instrument() != null && !req.instrument().isBlank())
            ? req.instrument().toUpperCase()
            : "GUITAR";

        ChordShape shape = new ChordShape();
        shape.setChordQuality(quality);
        shape.setTemplateFrets(json.toString());
        shape.setRootString(rootString);
        shape.setSource("user");
        shape.setInstrument(instrumentName);
        shape.setShapeName(req.shapeName());
        return shapeRepo.save(shape).getId();
    }
}
