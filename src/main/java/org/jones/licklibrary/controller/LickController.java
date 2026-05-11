package org.jones.licklibrary.controller;

import org.jones.licklibrary.constants.CustomInstrument;
import org.jones.licklibrary.constants.Instrument;
import org.jones.licklibrary.constants.InstrumentRegistry;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.constants.NoteParser;
import org.jones.licklibrary.model.LickResponse;
import org.jones.licklibrary.model.UploadLickRequest;
import org.jones.licklibrary.service.LickService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lick")
public class LickController {

    private final LickService lickService;

    public LickController(LickService lickService) {
        this.lickService = lickService;
    }

    @PostMapping
    public ResponseEntity<LickResponse> uploadLick(@RequestBody UploadLickRequest request) {
        try {
            return ResponseEntity.ok(lickService.uploadLick(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<LickResponse> getAllLicks() {
        return lickService.getAllLicks();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLick(@PathVariable UUID id) {
        lickService.deleteLick(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LickResponse> getLick(
            @PathVariable UUID id,
            @RequestParam String key,
            @RequestParam(defaultValue = "greedy") String algo,
            @RequestParam(defaultValue = "GUITAR") String instrument,
            @RequestParam(required = false) String tuning) {
        Note noteKey;
        try {
            noteKey = Note.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        Instrument inst;
        if (tuning != null && !tuning.isBlank()) {
            try {
                String[] tokens = tuning.trim().split("\\s+");
                Note[] notes = new Note[tokens.length];
                for (int i = 0; i < tokens.length; i++) notes[i] = NoteParser.parse(tokens[i]);
                inst = new CustomInstrument(notes);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            try {
                inst = InstrumentRegistry.fromName(instrument);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(lickService.getLick(id, noteKey, algo, inst));
    }
}
