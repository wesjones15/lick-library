package org.jones.licklibrary.controller;

import org.jones.licklibrary.model.LickResponse;
import org.jones.licklibrary.service.LickService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lick")
public class LickController {

    private final LickService lickService;

    public LickController(LickService lickService) {
        this.lickService = lickService;
    }

    @PostMapping
    public ResponseEntity<Void> uploadLick(@RequestBody String tab) {
        lickService.uploadLick(tab);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public Page<LickResponse> getLicks(
            @RequestParam String key,
            @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "0") int page) {
        return lickService.getLicks(key, mode, page);
    }
}
