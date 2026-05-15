package org.jones.licklibrary.domain.chord;

import org.jones.licklibrary.domain.chord.dto.UploadChordRequest;
import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.InstrumentRegistry;
import org.jones.licklibrary.domain.shared.Note;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chord")
public class ChordController {

    private final ChordService chordService;

    public ChordController(ChordService chordService) {
        this.chordService = chordService;
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, List<String>>> getAllChordVoicings(
        @RequestParam String root,
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

        return ResponseEntity.ok(chordService.getAllVoicings(rootNote, inst, instrument));
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

    @PostMapping
    public ResponseEntity<?> uploadChord(@RequestBody UploadChordRequest req) {
        try {
            UUID id = chordService.uploadChord(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
