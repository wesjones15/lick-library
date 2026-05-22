package org.jones.licklibrary.domain.playlist;

import org.jones.licklibrary.core.security.UserPrincipal;
import org.jones.licklibrary.domain.playlist.dto.*;
import org.jones.licklibrary.domain.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public PlaylistSummaryResponse createPlaylist(@RequestBody CreatePlaylistRequest request,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return playlistService.createPlaylist(request, principal.userId());
    }

    @GetMapping
    public List<PlaylistSummaryResponse> getAllPlaylists(@AuthenticationPrincipal UserPrincipal principal) {
        return playlistService.getAllPlaylists(principal.userId(), principal.role());
    }

    @GetMapping("/{id}")
    public PlaylistDetailResponse getPlaylist(@PathVariable UUID id,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        return playlistService.getPlaylist(id, principal.userId());
    }

    @PatchMapping("/{id}")
    public PlaylistSummaryResponse renamePlaylist(@PathVariable UUID id, @RequestBody CreatePlaylistRequest request,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return playlistService.renamePlaylist(id, request.name(), principal.userId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlaylist(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        playlistService.deletePlaylist(id, principal.userId());
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Void> setVisibility(@PathVariable UUID id,
                                               @RequestParam boolean isPublic,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        playlistService.setVisibility(id, isPublic, principal.userId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/entries")
    public PlaylistDetailResponse addEntry(@PathVariable UUID id, @RequestBody AddEntryRequest request,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        return playlistService.addEntry(id, request, principal.userId());
    }

    @PutMapping("/{id}/entries/{entryId}")
    public PlaylistDetailResponse updateEntry(
            @PathVariable UUID id,
            @PathVariable UUID entryId,
            @RequestBody UpdateEntryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return playlistService.updateEntry(id, entryId, request, principal.userId());
    }

    @DeleteMapping("/{id}/entries/{entryId}")
    public PlaylistDetailResponse removeEntry(@PathVariable UUID id, @PathVariable UUID entryId,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        return playlistService.removeEntry(id, entryId, principal.userId());
    }

    @DeleteMapping("/{id}/entries/{entryId}/overrides")
    public PlaylistDetailResponse clearEntryOverrides(@PathVariable UUID id, @PathVariable UUID entryId,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        return playlistService.clearEntryOverrides(id, entryId, principal.userId());
    }

    @GetMapping("/containing")
    public List<PlaylistContainingEntry> getContaining(@RequestParam UUID songId) {
        return playlistService.getContainingEntries(songId);
    }
}
