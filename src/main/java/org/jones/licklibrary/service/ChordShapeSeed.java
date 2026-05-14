package org.jones.licklibrary.service;

import org.jones.licklibrary.model.ChordQuality;
import org.jones.licklibrary.model.ChordShape;
import org.jones.licklibrary.repository.ChordQualityRepository;
import org.jones.licklibrary.repository.ChordShapeRepository;
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
        if (qualityRepo.count() > 0) return;

        String[] suffixes = {"", "m", "7", "maj7", "m7", "sus2", "sus4", "dim", "aug", "add9", "6", "m6", "dim7", "m7b5"};
        Map<String, ChordQuality> qualityMap = new LinkedHashMap<>();
        for (String suffix : suffixes) {
            qualityMap.put(suffix, qualityRepo.save(new ChordQuality(suffix)));
        }

        // Each row: {qualitySuffix, shapeName, rootString, templateFrets JSON}
        // Instrument is always "GUITAR" for all seed rows.
        // templateFrets encoding: "x" = muted, -1 = stay-open on transpose, 0+ = fretted (offset added on transpose)
        // All open strings in these CAGED shapes use 0 (they move with transposition to stay as chord tones).
        Object[][] rows = {
            // CAGED_E — rootString=0 (low E string)
            // E major open: E A D G B e strings
            {"",     "CAGED_E", 0, f(0,2,2,1,0,0)},  // E:  E B E G# B E
            {"m",    "CAGED_E", 0, f(0,2,2,0,0,0)},  // Em: E B E G  B E
            {"7",    "CAGED_E", 0, f(0,2,0,1,0,0)},  // E7: E B D G# B E
            {"maj7", "CAGED_E", 0, f(0,2,1,1,0,0)},  // Emaj7: E B D# G# B E
            {"m7",   "CAGED_E", 0, f(0,2,0,0,0,0)},  // Em7: E B D G  B E
            {"sus2", "CAGED_E", 0, f(0,2,2,"x",0,2)},// Esus2: E B E x B F#
            {"sus4", "CAGED_E", 0, f(0,2,2,2,0,0)},  // Esus4: E B E A  B E
            {"dim",  "CAGED_E", 0, f(0,1,2,0,"x",0)},// Edim:  E Bb E G  x E
            {"aug",  "CAGED_E", 0, f(0,3,2,1,1,0)},  // Eaug:  E C  E G# C E
            {"add9", "CAGED_E", 0, f(0,2,2,1,0,2)},  // Eadd9: E B E G# B F#
            {"6",    "CAGED_E", 0, f(0,2,2,1,2,0)},  // E6:    E B E G# C# E
            {"m6",   "CAGED_E", 0, f(0,2,2,0,2,0)},  // Em6:   E B E G  C# E
            {"dim7", "CAGED_E", 0, f(0,1,2,0,2,0)},  // Edim7: E Bb E G  Db E
            {"m7b5", "CAGED_E", 0, f(0,1,0,0,"x",0)},// Em7b5: E Bb D G  x  E

            // CAGED_A — rootString=1 (A string)
            // A major open: x A D G B e
            {"",     "CAGED_A", 1, f("x",0,2,2,2,0)},  // A:    x A E A C# E
            {"m",    "CAGED_A", 1, f("x",0,2,2,1,0)},  // Am:   x A E A C  E
            {"7",    "CAGED_A", 1, f("x",0,2,0,2,0)},  // A7:   x A E G  C# E
            {"maj7", "CAGED_A", 1, f("x",0,2,1,2,0)},  // Amaj7:x A E G# C# E
            {"m7",   "CAGED_A", 1, f("x",0,2,0,1,0)},  // Am7:  x A E G  C  E
            {"sus2", "CAGED_A", 1, f("x",0,2,2,0,0)},  // Asus2:x A E A  B  E
            {"sus4", "CAGED_A", 1, f("x",0,2,2,3,0)},  // Asus4:x A E A  D  E
            {"dim",  "CAGED_A", 1, f("x",0,1,2,1,"x")},// Adim: x A Eb A C  x
            {"aug",  "CAGED_A", 1, f("x",0,3,2,2,1)},  // Aaug: x A F  A C# F
            {"add9", "CAGED_A", 1, f("x",0,2,4,2,0)},  // Aadd9:x A E  B C# E
            {"6",    "CAGED_A", 1, f("x",0,2,2,2,2)},  // A6:   x A E A C# F#
            {"m6",   "CAGED_A", 1, f("x",0,2,2,1,2)},  // Am6:  x A E A C  F#
            {"dim7", "CAGED_A", 1, f("x",0,1,2,1,2)},  // Adim7:x A Eb A C  Gb
            {"m7b5", "CAGED_A", 1, f("x",0,1,2,1,3)},  // Am7b5:x A Eb A C  G

            // CAGED_G — rootString=0 (low E string)
            // G major open: G major across all 6 strings
            {"",     "CAGED_G", 0, f(3,2,0,0,0,3)},  // G:    G B D G  B G
            {"m",    "CAGED_G", 0, f(3,1,0,0,3,3)},  // Gm:   G Bb D G D G
            {"7",    "CAGED_G", 0, f(3,2,0,0,0,1)},  // G7:   G B D G  B F
            {"maj7", "CAGED_G", 0, f(3,2,0,0,0,2)},  // Gmaj7:G B D G  B F#
            {"m7",   "CAGED_G", 0, f(3,1,0,0,3,1)},  // Gm7:  G Bb D G D F
            {"sus2", "CAGED_G", 0, f(3,0,0,0,3,3)},  // Gsus2:G A D G  D G
            {"sus4", "CAGED_G", 0, f(3,3,0,0,1,3)},  // Gsus4:G C D G  C G
            {"dim",  "CAGED_G", 0, f(3,1,"x",0,2,3)},// Gdim: G Bb x G Db G
            {"aug",  "CAGED_G", 0, f(3,2,1,0,4,3)},  // Gaug: G B Eb G D# G
            {"add9", "CAGED_G", 0, f(3,2,0,2,0,3)},  // Gadd9:G B D A  B G
            {"6",    "CAGED_G", 0, f(3,2,0,0,0,0)},  // G6:   G B D G  B E
            {"m6",   "CAGED_G", 0, f(3,1,0,0,"x",0)},// Gm6:  G Bb D G x  E
            {"dim7", "CAGED_G", 0, f(3,1,"x",0,2,0)},// Gdim7:G Bb x G Db E(Fb)
            {"m7b5", "CAGED_G", 0, f(3,1,"x",0,2,1)},// Gm7b5:G Bb x G Db F

            // CAGED_C — rootString=1 (A string)
            // C major open: x C E G C E
            {"",     "CAGED_C", 1, f("x",3,2,0,1,0)},  // C:    x C E G C  E
            {"m",    "CAGED_C", 1, f("x",3,1,0,1,3)},  // Cm:   x C Eb G C  G
            {"7",    "CAGED_C", 1, f("x",3,2,3,1,0)},  // C7:   x C E Bb C  E
            {"maj7", "CAGED_C", 1, f("x",3,2,0,0,0)},  // Cmaj7:x C E G  B  E
            {"m7",   "CAGED_C", 1, f("x",3,1,3,1,3)},  // Cm7:  x C Eb Bb C G
            {"sus2", "CAGED_C", 1, f("x",3,0,0,1,3)},  // Csus2:x C D G  C  G
            {"sus4", "CAGED_C", 1, f("x",3,3,0,1,1)},  // Csus4:x C F G  C  F
            {"dim",  "CAGED_C", 1, f("x",3,1,"x","x",2)},// Cdim:x C Eb x x  Gb
            {"aug",  "CAGED_C", 1, f("x",3,2,1,1,0)},  // Caug: x C E G# C  E
            {"add9", "CAGED_C", 1, f("x",3,2,0,3,0)},  // Cadd9:x C E G  D  E
            {"6",    "CAGED_C", 1, f("x",3,2,2,1,0)},  // C6:   x C E A  C  E
            {"m6",   "CAGED_C", 1, f("x",3,1,2,1,3)},  // Cm6:  x C Eb A  C  G
            {"dim7", "CAGED_C", 1, f("x",3,1,2,"x",2)},// Cdim7:x C Eb A  x  Gb
            {"m7b5", "CAGED_C", 1, f("x",3,1,3,1,2)},  // Cm7b5:x C Eb Bb C  Gb

            // CAGED_D — rootString=2 (D string)
            // D major open: x x D A D F#
            {"",     "CAGED_D", 2, f("x","x",0,2,3,2)},// D:    x x D A D  F#
            {"m",    "CAGED_D", 2, f("x","x",0,2,3,1)},// Dm:   x x D A D  F
            {"7",    "CAGED_D", 2, f("x","x",0,2,1,2)},// D7:   x x D A C  F#
            {"maj7", "CAGED_D", 2, f("x","x",0,2,2,2)},// Dmaj7:x x D A C# F#
            {"m7",   "CAGED_D", 2, f("x","x",0,2,1,1)},// Dm7:  x x D A C  F
            {"sus2", "CAGED_D", 2, f("x","x",0,2,3,0)},// Dsus2:x x D A D  E
            {"sus4", "CAGED_D", 2, f("x","x",0,2,3,3)},// Dsus4:x x D A D  G
            {"dim",  "CAGED_D", 2, f("x","x",0,1,3,1)},// Ddim: x x D Ab D  F
            {"aug",  "CAGED_D", 2, f("x","x",0,3,3,2)},// Daug: x x D Bb D  F#
            {"add9", "CAGED_D", 2, f("x","x",0,2,3,0)},// Dadd9:x x D A D  E (same as sus2; no M3 in open D-shape)
            {"6",    "CAGED_D", 2, f("x","x",0,2,0,2)},// D6:   x x D A B  F#
            {"m6",   "CAGED_D", 2, f("x","x",0,2,0,1)},// Dm6:  x x D A B  F
            {"dim7", "CAGED_D", 2, f("x","x",0,1,0,1)},// Ddim7:x x D Ab B  F
            {"m7b5", "CAGED_D", 2, f("x","x",0,1,1,1)},// Dm7b5:x x D Ab C  F
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
