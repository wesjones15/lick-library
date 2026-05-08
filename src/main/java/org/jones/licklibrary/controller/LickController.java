package org.jones.licklibrary.controller;

import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.LickResponse;
import org.jones.licklibrary.model.UploadLickRequest;
import org.jones.licklibrary.service.LickService;
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

    @GetMapping("/{id}")
    public ResponseEntity<LickResponse> getLick(
            @PathVariable UUID id,
            @RequestParam String key) {
        Note noteKey;
        try {
            noteKey = Note.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(lickService.getLick(id, noteKey));
    }
}
