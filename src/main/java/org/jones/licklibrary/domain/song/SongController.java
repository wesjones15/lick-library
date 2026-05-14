package org.jones.licklibrary.domain.song;

import org.jones.licklibrary.domain.song.dto.SongDetailResponse;
import org.jones.licklibrary.domain.song.dto.SongSummaryResponse;
import org.jones.licklibrary.domain.song.dto.UploadSongRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/song")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping
    public ResponseEntity<SongSummaryResponse> uploadSong(@RequestBody UploadSongRequest request) {
        try {
            return ResponseEntity.ok(songService.uploadSong(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<SongSummaryResponse> getAllSongs() {
        return songService.getAllSongs();
    }

    @GetMapping("/{id}")
    public SongDetailResponse getSong(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int semitones) {
        return songService.getSong(id, semitones);
    }

    @PostMapping("/{id}/reparse")
    public ResponseEntity<SongDetailResponse> reparseSong(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(songService.reparseSong(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSong(@PathVariable UUID id) {
        songService.deleteSong(id);
    }
}
