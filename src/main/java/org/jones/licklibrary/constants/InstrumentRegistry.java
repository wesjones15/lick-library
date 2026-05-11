package org.jones.licklibrary.constants;

public class InstrumentRegistry {

    public static Instrument fromName(String name) {
        return switch (name == null ? "" : name.toUpperCase()) {
            case "GUITAR", "STANDARD_GUITAR", "" -> Guitar.STANDARD;
            case "DROP_D"   -> Guitar.DROP_D;
            case "OPEN_G"   -> Guitar.OPEN_G;
            case "OPEN_D"   -> Guitar.OPEN_D;
            case "DADGAD"   -> Guitar.DADGAD;
            case "BASS"     -> Bass.STANDARD;
            case "UKULELE"  -> Ukulele.STANDARD;
            case "MANDOLIN" -> Mandolin.STANDARD;
            case "BANJO"    -> Banjo.STANDARD;
            default -> throw new IllegalArgumentException("Unknown instrument: " + name);
        };
    }
}
