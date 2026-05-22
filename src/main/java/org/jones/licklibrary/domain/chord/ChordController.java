package org.jones.licklibrary.domain.chord;

import org.jones.licklibrary.core.security.UserPrincipal;
import org.jones.licklibrary.domain.chord.dto.ChordVoicingResponse;
import org.jones.licklibrary.domain.chord.dto.UploadChordRequest;
import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.InstrumentRegistry;
import org.jones.licklibrary.domain.shared.Note;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chord")
public class ChordController {

    private final ChordService chordService;
    private final ChordShapeSeed chordShapeSeed;

    public ChordController(ChordService chordService, ChordShapeSeed chordShapeSeed) {
        this.chordService = chordService;
        this.chordShapeSeed = chordShapeSeed;
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, List<ChordVoicingResponse>>> getAllChordVoicings(
        @RequestParam String root,
        @RequestParam(defaultValue = "GUITAR") String instrument,
        @AuthenticationPrincipal UserPrincipal principal
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

        return ResponseEntity.ok(chordService.getAllVoicings(rootNote, inst, instrument, principal.userId()));
    }

    @GetMapping
    public ResponseEntity<List<ChordVoicingResponse>> getChordVoicings(
        @RequestParam String root,
        @RequestParam(defaultValue = "") String quality,
        @RequestParam(defaultValue = "GUITAR") String instrument,
        @AuthenticationPrincipal UserPrincipal principal
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

        List<ChordVoicingResponse> voicings = chordService.getVoicings(rootNote, quality, inst, instrument, principal.userId());
        return ResponseEntity.ok(voicings);
    }

    @PostMapping
    public ResponseEntity<?> uploadChord(@RequestBody UploadChordRequest req,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        try {
            UUID id = chordService.uploadChord(req, principal.userId());
            return ResponseEntity.status(HttpStatus.CREATED).body(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoicing(@PathVariable UUID id,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        chordService.deleteVoicing(id, principal.userId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reseed")
    public ResponseEntity<Void> reseed() {
        chordShapeSeed.reseedMissing();
        return ResponseEntity.ok().build();
    }
}
