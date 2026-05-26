package org.jones.licklibrary.domain.song;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jones.licklibrary.core.security.UserPrincipal;
import org.jones.licklibrary.domain.song.dto.BeatmapResponse;
import org.jones.licklibrary.domain.song.dto.SongDetailResponse;
import org.jones.licklibrary.domain.song.dto.SongSummaryResponse;
import org.jones.licklibrary.domain.song.dto.SongUpdateRequestSummary;
import org.jones.licklibrary.domain.song.dto.UpdateSongRequest;
import org.jones.licklibrary.domain.song.dto.UploadSongRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/song")
public class SongController {

    private final SongService songService;
    private final SongRepository songRepository;
    private final SongBeatmapRepository beatmapRepository;
    private final ObjectMapper objectMapper;

    public SongController(SongService songService, SongRepository songRepository,
                          SongBeatmapRepository beatmapRepository, ObjectMapper objectMapper) {
        this.songService = songService;
        this.songRepository = songRepository;
        this.beatmapRepository = beatmapRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<SongSummaryResponse> uploadSong(@RequestBody UploadSongRequest request,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        try {
            return ResponseEntity.ok(songService.uploadSong(request, principal.userId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<SongSummaryResponse> getAllSongs(@RequestParam(defaultValue = "false") boolean mine,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return mine ? songService.getMySongs(principal.userId()) : songService.getAllSongs(principal.userId(), principal.role());
    }

    @GetMapping("/{id}")
    public SongDetailResponse getSong(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int semitones,
            @AuthenticationPrincipal UserPrincipal principal) {
        return songService.getSong(id, semitones, principal.userId(), principal.role());
    }

    @PutMapping("/{id}/reparse")
    public ResponseEntity<SongDetailResponse> reparseSong(@PathVariable UUID id,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        try {
            return ResponseEntity.ok(songService.reparseSong(id, principal.userId(), principal.role()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public SongDetailResponse updateSong(@PathVariable UUID id, @RequestBody UpdateSongRequest request,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return songService.updateSong(id, request, principal.userId(), principal.role());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSong(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        songService.deleteSong(id, principal.userId(), principal.role());
    }

    @GetMapping("/{id}/beatmap")
    public ResponseEntity<BeatmapResponse> getBeatmap(@PathVariable UUID id) {
        return beatmapRepository.findBySongId(id)
                .map(b -> ResponseEntity.ok(toResponse(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/beatmap")
    public BeatmapResponse saveBeatmap(@PathVariable UUID id,
                                       @RequestBody BeatmapResponse request,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new org.jones.licklibrary.core.exception.ResourceNotFoundException("Song not found: " + id));
        songService.checkOwner(song, principal.userId(), principal.role());
        SongBeatmap bm = beatmapRepository.findBySongId(id).orElse(new SongBeatmap());
        bm.setSongId(id);
        bm.setBeats(request.beats().stream()
                .map(String::valueOf).collect(Collectors.joining(",")));
        return toResponse(beatmapRepository.save(bm));
    }

    @PostMapping("/{id}/update-request")
    public ResponseEntity<SongUpdateRequestSummary> submitUpdateRequest(
            @PathVariable UUID id,
            @RequestBody UpdateSongRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            String requestType = request.rawChordSheet() != null ? "SONG_CHART" : "SONG_METADATA";
            String payload = objectMapper.writeValueAsString(request);
            return ResponseEntity.accepted().body(songService.submitSongUpdate(id, principal.userId(), requestType, payload));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/beatmap-request")
    public ResponseEntity<SongUpdateRequestSummary> submitBeatmapRequest(
            @PathVariable UUID id,
            @RequestBody BeatmapResponse request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of("beats", request.beats()));
            return ResponseEntity.accepted().body(songService.submitSongUpdate(id, principal.userId(), "SONG_BEATMAP", payload));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private BeatmapResponse toResponse(SongBeatmap bm) {
        List<Integer> beats = Arrays.stream(bm.getBeats().split(","))
                .map(Integer::parseInt).toList();
        return new BeatmapResponse(beats);
    }
}
