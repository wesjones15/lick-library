package org.jones.licklibrary.domain.playlist;

import org.jones.licklibrary.domain.playlist.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping
    public PlaylistSummaryResponse createPlaylist(@RequestBody CreatePlaylistRequest request) {
        return playlistService.createPlaylist(request);
    }

    @GetMapping
    public List<PlaylistSummaryResponse> getAllPlaylists() {
        return playlistService.getAllPlaylists();
    }

    @GetMapping("/{id}")
    public PlaylistDetailResponse getPlaylist(@PathVariable UUID id) {
        return playlistService.getPlaylist(id);
    }

    @PatchMapping("/{id}")
    public PlaylistSummaryResponse renamePlaylist(@PathVariable UUID id, @RequestBody CreatePlaylistRequest request) {
        return playlistService.renamePlaylist(id, request.name());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlaylist(@PathVariable UUID id) {
        playlistService.deletePlaylist(id);
    }

    @PostMapping("/{id}/entries")
    public PlaylistDetailResponse addEntry(@PathVariable UUID id, @RequestBody AddEntryRequest request) {
        return playlistService.addEntry(id, request);
    }

    @PutMapping("/{id}/entries/{entryId}")
    public PlaylistDetailResponse updateEntry(
            @PathVariable UUID id,
            @PathVariable UUID entryId,
            @RequestBody UpdateEntryRequest request) {
        return playlistService.updateEntry(id, entryId, request);
    }

    @DeleteMapping("/{id}/entries/{entryId}")
    public PlaylistDetailResponse removeEntry(@PathVariable UUID id, @PathVariable UUID entryId) {
        return playlistService.removeEntry(id, entryId);
    }

    @DeleteMapping("/{id}/entries/{entryId}/overrides")
    public PlaylistDetailResponse clearEntryOverrides(@PathVariable UUID id, @PathVariable UUID entryId) {
        return playlistService.clearEntryOverrides(id, entryId);
    }

    @GetMapping("/containing")
    public List<PlaylistContainingEntry> getContaining(@RequestParam UUID songId) {
        return playlistService.getContainingEntries(songId);
    }
}
