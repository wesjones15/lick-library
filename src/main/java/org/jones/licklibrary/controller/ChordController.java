package org.jones.licklibrary.controller;

import org.jones.licklibrary.constants.Instrument;
import org.jones.licklibrary.constants.InstrumentRegistry;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.service.ChordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chord")
public class ChordController {

    private final ChordService chordService;

    public ChordController(ChordService chordService) {
        this.chordService = chordService;
    }

    @GetMapping
    public ResponseEntity<List<String>> getChordVoicings(
        @RequestParam String root,
        @RequestParam(defaultValue = "") String quality,
        @RequestParam(defaultValue = "GUITAR") String instrument
    ) {
        Note rootNote;
        try {
            rootNote = Note.valueOf(root.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Instrument inst;
        try {
            inst = InstrumentRegistry.fromName(instrument);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        if (!chordService.knowsQuality(quality)) {
            return ResponseEntity.badRequest().build();
        }

        List<String> voicings = chordService.getVoicings(rootNote, quality, inst, instrument);
        return ResponseEntity.ok(voicings);
    }
}
