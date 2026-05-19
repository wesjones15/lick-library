package org.jones.licklibrary.domain.scale;

import org.jones.licklibrary.domain.scale.dto.ScaleResponse;
import org.jones.licklibrary.domain.shared.Instrument;
import org.jones.licklibrary.domain.shared.InstrumentRegistry;
import org.jones.licklibrary.domain.shared.Mode;
import org.jones.licklibrary.domain.shared.Note;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scale")
public class ScaleController {

    private final ScaleService scaleService;

    public ScaleController(ScaleService scaleService) {
        this.scaleService = scaleService;
    }

    @GetMapping
    public ResponseEntity<ScaleResponse> getScale(
            @RequestParam String root,
            @RequestParam String mode,
            @RequestParam(required = false, defaultValue = "GUITAR") String instrument
    ) {
        Note rootNote;
        try {
            rootNote = Note.valueOf(root.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Mode scaleMode;
        try {
            scaleMode = Mode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Instrument inst;
        try {
            inst = InstrumentRegistry.fromName(instrument);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(scaleService.getScale(rootNote, scaleMode, inst));
    }
}
