package org.jones.licklibrary.domain.chord;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChordShapeSeed implements ApplicationRunner {

    private final ChordQualityRepository qualityRepo;
    private final ChordShapeRepository shapeRepo;

    public ChordShapeSeed(ChordQualityRepository qualityRepo, ChordShapeRepository shapeRepo) {
        this.qualityRepo = qualityRepo;
        this.shapeRepo = shapeRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedCagedShapes();
        seedSlashShapes();
    }

    public void reseedMissing() {
        reseedCagedShapes();
    }

    private void seedCagedShapes() {
        if (qualityRepo.count() > 0) return;

        String[] suffixes = {"", "m", "7", "maj7", "m7", "sus2", "sus4", "dim", "aug", "add9", "6", "m6", "dim7", "m7b5"};
        Map<String, ChordQuality> qualityMap = new LinkedHashMap<>();
        for (String suffix : suffixes) {
            qualityMap.put(suffix, qualityRepo.save(new ChordQuality(suffix)));
        }

        Object[][] rows = {
            // CAGED_E — rootString=0 (low E string)
            {"",     "CAGED_E", 0, f(0,2,2,1,0,0)},
            {"m",    "CAGED_E", 0, f(0,2,2,0,0,0)},
            {"7",    "CAGED_E", 0, f(0,2,0,1,0,0)},
            {"maj7", "CAGED_E", 0, f(0,2,1,1,0,0)},
            {"m7",   "CAGED_E", 0, f(0,2,0,0,0,0)},
            {"sus2", "CAGED_E", 0, f(0,2,2,"x",0,2)},
            {"sus4", "CAGED_E", 0, f(0,2,2,2,0,0)},
            {"dim",  "CAGED_E", 0, f(0,1,2,0,"x",0)},
            {"aug",  "CAGED_E", 0, f(0,3,2,1,1,0)},
            {"add9", "CAGED_E", 0, f(0,2,2,1,0,2)},
            {"6",    "CAGED_E", 0, f(0,2,2,1,2,0)},
            {"m6",   "CAGED_E", 0, f(0,2,2,0,2,0)},
            {"dim7", "CAGED_E", 0, f(0,1,2,0,2,0)},
            {"m7b5", "CAGED_E", 0, f(0,1,0,0,"x",0)},

            // CAGED_A — rootString=1 (A string)
            {"",     "CAGED_A", 1, f("x",0,2,2,2,0)},
            {"m",    "CAGED_A", 1, f("x",0,2,2,1,0)},
            {"7",    "CAGED_A", 1, f("x",0,2,0,2,0)},
            {"maj7", "CAGED_A", 1, f("x",0,2,1,2,0)},
            {"m7",   "CAGED_A", 1, f("x",0,2,0,1,0)},
            {"sus2", "CAGED_A", 1, f("x",0,2,2,0,0)},
            {"sus4", "CAGED_A", 1, f("x",0,2,2,3,0)},
            {"dim",  "CAGED_A", 1, f("x",0,1,2,1,"x")},
            {"aug",  "CAGED_A", 1, f("x",0,3,2,2,1)},
            {"add9", "CAGED_A", 1, f("x",0,2,4,2,0)},
            {"6",    "CAGED_A", 1, f("x",0,2,2,2,2)},
            {"m6",   "CAGED_A", 1, f("x",0,2,2,1,2)},
            {"dim7", "CAGED_A", 1, f("x",0,1,2,1,2)},
            {"m7b5", "CAGED_A", 1, f("x",0,1,2,1,3)},

            // CAGED_G — rootString=0 (low E string)
            {"",     "CAGED_G", 0, f(3,2,0,0,0,3)},
            {"m",    "CAGED_G", 0, f(3,1,0,0,3,3)},
            {"7",    "CAGED_G", 0, f(3,2,0,0,0,1)},
            {"maj7", "CAGED_G", 0, f(3,2,0,0,0,2)},
            {"m7",   "CAGED_G", 0, f(3,1,0,0,3,1)},
            {"sus2", "CAGED_G", 0, f(3,0,0,0,3,3)},
            {"sus4", "CAGED_G", 0, f(3,3,0,0,1,3)},
            {"dim",  "CAGED_G", 0, f(3,1,"x",0,2,3)},
            {"aug",  "CAGED_G", 0, f(3,2,1,0,4,3)},
            {"add9", "CAGED_G", 0, f(3,2,0,2,0,3)},
            {"6",    "CAGED_G", 0, f(3,2,0,0,0,0)},
            {"m6",   "CAGED_G", 0, f(3,1,0,0,"x",0)},
            {"dim7", "CAGED_G", 0, f(3,1,"x",0,2,0)},
            {"m7b5", "CAGED_G", 0, f(3,1,"x",0,2,1)},

            // CAGED_C — rootString=1 (A string)
            {"",     "CAGED_C", 1, f("x",3,2,0,1,0)},
            {"m",    "CAGED_C", 1, f("x",3,1,0,1,3)},
            {"7",    "CAGED_C", 1, f("x",3,2,3,1,0)},
            {"maj7", "CAGED_C", 1, f("x",3,2,0,0,0)},
            {"m7",   "CAGED_C", 1, f("x",3,1,3,1,3)},
            {"sus2", "CAGED_C", 1, f("x",3,0,0,1,3)},
            {"sus4", "CAGED_C", 1, f("x",3,3,0,1,1)},
            {"dim",  "CAGED_C", 1, f("x",3,1,"x","x",2)},
            {"aug",  "CAGED_C", 1, f("x",3,2,1,1,0)},
            {"add9", "CAGED_C", 1, f("x",3,2,0,3,0)},
            {"6",    "CAGED_C", 1, f("x",3,2,2,1,0)},
            {"m6",   "CAGED_C", 1, f("x",3,1,2,1,3)},
            {"dim7", "CAGED_C", 1, f("x",3,1,2,"x",2)},
            {"m7b5", "CAGED_C", 1, f("x",3,1,3,1,2)},

            // CAGED_D — rootString=2 (D string)
            {"",     "CAGED_D", 2, f("x","x",0,2,3,2)},
            {"m",    "CAGED_D", 2, f("x","x",0,2,3,1)},
            {"7",    "CAGED_D", 2, f("x","x",0,2,1,2)},
            {"maj7", "CAGED_D", 2, f("x","x",0,2,2,2)},
            {"m7",   "CAGED_D", 2, f("x","x",0,2,1,1)},
            {"sus2", "CAGED_D", 2, f("x","x",0,2,3,0)},
            {"sus4", "CAGED_D", 2, f("x","x",0,2,3,3)},
            {"dim",  "CAGED_D", 2, f("x","x",0,1,3,1)},
            {"aug",  "CAGED_D", 2, f("x","x",0,3,3,2)},
            {"add9", "CAGED_D", 2, f("x","x",0,2,3,0)},
            {"6",    "CAGED_D", 2, f("x","x",0,2,0,2)},
            {"m6",   "CAGED_D", 2, f("x","x",0,2,0,1)},
            {"dim7", "CAGED_D", 2, f("x","x",0,1,0,1)},
            {"m7b5", "CAGED_D", 2, f("x","x",0,1,1,1)},
        };

        List<ChordShape> shapes = new ArrayList<>();
        for (Object[] row : rows) {
            String suffix = (String) row[0];
            String shapeName = (String) row[1];
            int rootString = (int) row[2];
            String templateFrets = (String) row[3];

            ChordShape shape = new ChordShape();
            shape.setChordQuality(qualityMap.get(suffix));
            shape.setShapeName(shapeName);
            shape.setRootString(rootString);
            shape.setTemplateFrets(templateFrets);
            shape.setInstrument("GUITAR");
            shape.setSource("system");
            shapes.add(shape);
        }
        shapeRepo.saveAll(shapes);
    }

    private void seedSlashShapes() {
        if (shapeRepo.findByChordQuality_SuffixAndInstrument("/4", "GUITAR").size() > 0) return;

        String[] slashSuffixes = {"/4", "m/3", "7/4", "/7"};
        Map<String, ChordQuality> qualityMap = new LinkedHashMap<>();
        for (String suffix : slashSuffixes) {
            qualityMap.put(suffix, qualityRepo.findBySuffix(suffix)
                .orElseGet(() -> qualityRepo.save(new ChordQuality(suffix))));
        }

        Object[][] rows = {
            // "/4" — major first inversion (bass = major 3rd above root)
            // INV1_G: rootString=3 (G string). Open shape = G/B (x,2,0,0,3,3)
            {"/4",  "INV1_G", 3, f("x",2,0,0,3,3)},
            // INV1_A: rootString=1 (A string). Open shape = C/E (x,3,2,0,1,0)
            {"/4",  "INV1_A", 1, f("x",3,2,0,1,0)},

            // "m/3" — minor first inversion (bass = minor 3rd above root)
            // INV1M_E: rootString=5 (e string). Open shape = Em/G (3,2,2,0,0,0)
            {"m/3", "INV1M_E", 5, f(3,2,2,0,0,0)},
            // INV1M_D: rootString=2 (D string). Open shape = Dm/F (1,0,0,2,3,1)
            {"m/3", "INV1M_D", 2, f(1,0,0,2,3,1)},

            // "7/4" — dominant 7th first inversion (bass = major 3rd above root)
            // Open shape = G7/B (x,2,0,1,0,1)
            {"7/4", "INV7_G", 3, f("x",2,0,1,0,1)},

            // "/7" — major second inversion (bass = perfect 5th above root = 7 semitones)
            // INV2_G: rootString=0 (E string). Open shape = G/D (x,x,0,0,3,2)
            {"/7",  "INV2_D", 3, f("x","x",0,0,0,2)},
        };

        List<ChordShape> shapes = new ArrayList<>();
        for (Object[] row : rows) {
            String suffix = (String) row[0];
            String shapeName = (String) row[1];
            int rootString = (int) row[2];
            String templateFrets = (String) row[3];

            ChordShape shape = new ChordShape();
            shape.setChordQuality(qualityMap.get(suffix));
            shape.setShapeName(shapeName);
            shape.setRootString(rootString);
            shape.setTemplateFrets(templateFrets);
            shape.setInstrument("GUITAR");
            shape.setSource("system");
            shapes.add(shape);
        }
        shapeRepo.saveAll(shapes);
    }

    private void reseedCagedShapes() {
        String[] suffixes = {"", "m", "7", "maj7", "m7", "sus2", "sus4", "dim", "aug", "add9", "6", "m6", "dim7", "m7b5"};
        Map<String, ChordQuality> qualityMap = new LinkedHashMap<>();
        for (String suffix : suffixes) {
            qualityMap.put(suffix, qualityRepo.findBySuffix(suffix)
                .orElseGet(() -> qualityRepo.save(new ChordQuality(suffix))));
        }

        Object[][] rows = {
            {"",     "CAGED_E", 0, f(0,2,2,1,0,0)},
            {"m",    "CAGED_E", 0, f(0,2,2,0,0,0)},
            {"7",    "CAGED_E", 0, f(0,2,0,1,0,0)},
            {"maj7", "CAGED_E", 0, f(0,2,1,1,0,0)},
            {"m7",   "CAGED_E", 0, f(0,2,0,0,0,0)},
            {"sus2", "CAGED_E", 0, f(0,2,2,"x",0,2)},
            {"sus4", "CAGED_E", 0, f(0,2,2,2,0,0)},
            {"dim",  "CAGED_E", 0, f(0,1,2,0,"x",0)},
            {"aug",  "CAGED_E", 0, f(0,3,2,1,1,0)},
            {"add9", "CAGED_E", 0, f(0,2,2,1,0,2)},
            {"6",    "CAGED_E", 0, f(0,2,2,1,2,0)},
            {"m6",   "CAGED_E", 0, f(0,2,2,0,2,0)},
            {"dim7", "CAGED_E", 0, f(0,1,2,0,2,0)},
            {"m7b5", "CAGED_E", 0, f(0,1,0,0,"x",0)},
            {"",     "CAGED_A", 1, f("x",0,2,2,2,0)},
            {"m",    "CAGED_A", 1, f("x",0,2,2,1,0)},
            {"7",    "CAGED_A", 1, f("x",0,2,0,2,0)},
            {"maj7", "CAGED_A", 1, f("x",0,2,1,2,0)},
            {"m7",   "CAGED_A", 1, f("x",0,2,0,1,0)},
            {"sus2", "CAGED_A", 1, f("x",0,2,2,0,0)},
            {"sus4", "CAGED_A", 1, f("x",0,2,2,3,0)},
            {"dim",  "CAGED_A", 1, f("x",0,1,2,1,"x")},
            {"aug",  "CAGED_A", 1, f("x",0,3,2,2,1)},
            {"add9", "CAGED_A", 1, f("x",0,2,4,2,0)},
            {"6",    "CAGED_A", 1, f("x",0,2,2,2,2)},
            {"m6",   "CAGED_A", 1, f("x",0,2,2,1,2)},
            {"dim7", "CAGED_A", 1, f("x",0,1,2,1,2)},
            {"m7b5", "CAGED_A", 1, f("x",0,1,2,1,3)},
            {"",     "CAGED_G", 0, f(3,2,0,0,0,3)},
            {"m",    "CAGED_G", 0, f(3,1,0,0,3,3)},
            {"7",    "CAGED_G", 0, f(3,2,0,0,0,1)},
            {"maj7", "CAGED_G", 0, f(3,2,0,0,0,2)},
            {"m7",   "CAGED_G", 0, f(3,1,0,0,3,1)},
            {"sus2", "CAGED_G", 0, f(3,0,0,0,3,3)},
            {"sus4", "CAGED_G", 0, f(3,3,0,0,1,3)},
            {"dim",  "CAGED_G", 0, f(3,1,"x",0,2,3)},
            {"aug",  "CAGED_G", 0, f(3,2,1,0,4,3)},
            {"add9", "CAGED_G", 0, f(3,2,0,2,0,3)},
            {"6",    "CAGED_G", 0, f(3,2,0,0,0,0)},
            {"m6",   "CAGED_G", 0, f(3,1,0,0,"x",0)},
            {"dim7", "CAGED_G", 0, f(3,1,"x",0,2,0)},
            {"m7b5", "CAGED_G", 0, f(3,1,"x",0,2,1)},
            {"",     "CAGED_C", 1, f("x",3,2,0,1,0)},
            {"m",    "CAGED_C", 1, f("x",3,1,0,1,3)},
            {"7",    "CAGED_C", 1, f("x",3,2,3,1,0)},
            {"maj7", "CAGED_C", 1, f("x",3,2,0,0,0)},
            {"m7",   "CAGED_C", 1, f("x",3,1,3,1,3)},
            {"sus2", "CAGED_C", 1, f("x",3,0,0,1,3)},
            {"sus4", "CAGED_C", 1, f("x",3,3,0,1,1)},
            {"dim",  "CAGED_C", 1, f("x",3,1,"x","x",2)},
            {"aug",  "CAGED_C", 1, f("x",3,2,1,1,0)},
            {"add9", "CAGED_C", 1, f("x",3,2,0,3,0)},
            {"6",    "CAGED_C", 1, f("x",3,2,2,1,0)},
            {"m6",   "CAGED_C", 1, f("x",3,1,2,1,3)},
            {"dim7", "CAGED_C", 1, f("x",3,1,2,"x",2)},
            {"m7b5", "CAGED_C", 1, f("x",3,1,3,1,2)},
            {"",     "CAGED_D", 2, f("x","x",0,2,3,2)},
            {"m",    "CAGED_D", 2, f("x","x",0,2,3,1)},
            {"7",    "CAGED_D", 2, f("x","x",0,2,1,2)},
            {"maj7", "CAGED_D", 2, f("x","x",0,2,2,2)},
            {"m7",   "CAGED_D", 2, f("x","x",0,2,1,1)},
            {"sus2", "CAGED_D", 2, f("x","x",0,2,3,0)},
            {"sus4", "CAGED_D", 2, f("x","x",0,2,3,3)},
            {"dim",  "CAGED_D", 2, f("x","x",0,1,3,1)},
            {"aug",  "CAGED_D", 2, f("x","x",0,3,3,2)},
            {"add9", "CAGED_D", 2, f("x","x",0,2,3,0)},
            {"6",    "CAGED_D", 2, f("x","x",0,2,0,2)},
            {"m6",   "CAGED_D", 2, f("x","x",0,2,0,1)},
            {"dim7", "CAGED_D", 2, f("x","x",0,1,0,1)},
            {"m7b5", "CAGED_D", 2, f("x","x",0,1,1,1)},
        };

        for (Object[] row : rows) {
            String suffix = (String) row[0];
            String shapeName = (String) row[1];
            int rootString = (int) row[2];
            String templateFrets = (String) row[3];
            if (shapeRepo.existsByShapeNameAndChordQuality_SuffixAndInstrument(shapeName, suffix, "GUITAR")) continue;
            ChordShape shape = new ChordShape();
            shape.setChordQuality(qualityMap.get(suffix));
            shape.setShapeName(shapeName);
            shape.setRootString(rootString);
            shape.setTemplateFrets(templateFrets);
            shape.setInstrument("GUITAR");
            shape.setSource("system");
            shapeRepo.save(shape);
        }
    }

    private void reseedSlashShapes() {
        String[] slashSuffixes = {"/4", "m/3", "7/4", "/7"};
        Map<String, ChordQuality> qualityMap = new LinkedHashMap<>();
        for (String suffix : slashSuffixes) {
            qualityMap.put(suffix, qualityRepo.findBySuffix(suffix)
                .orElseGet(() -> qualityRepo.save(new ChordQuality(suffix))));
        }

        Object[][] rows = {
            {"/4",  "INV1_G", 3, f("x",2,0,0,3,3)},
            {"/4",  "INV1_A", 1, f("x",3,2,0,1,0)},
            {"m/3", "INV1M_E", 5, f(3,2,2,0,0,0)},
            {"m/3", "INV1M_D", 2, f(1,0,0,2,3,1)},
            {"7/4", "INV7_G", 3, f("x",2,0,1,0,1)},
            {"/7",  "INV2_D", 3, f("x","x",0,0,0,2)},
        };

        for (Object[] row : rows) {
            String suffix = (String) row[0];
            String shapeName = (String) row[1];
            int rootString = (int) row[2];
            String templateFrets = (String) row[3];
            if (shapeRepo.existsByShapeNameAndInstrument(shapeName, "GUITAR")) continue;
            ChordShape shape = new ChordShape();
            shape.setChordQuality(qualityMap.get(suffix));
            shape.setShapeName(shapeName);
            shape.setRootString(rootString);
            shape.setTemplateFrets(templateFrets);
            shape.setInstrument("GUITAR");
            shape.setSource("system");
            shapeRepo.save(shape);
        }
    }

    private static String f(Object... values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(",");
            Object v = values[i];
            if (v instanceof String) sb.append("\"").append(v).append("\"");
            else sb.append(v);
        }
        sb.append("]");
        return sb.toString();
    }
}
