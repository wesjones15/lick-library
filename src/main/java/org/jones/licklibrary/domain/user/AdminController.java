package org.jones.licklibrary.domain.user;

import org.jones.licklibrary.domain.song.SongService;
import org.jones.licklibrary.domain.song.dto.SongUpdateRequestSummary;
import org.jones.licklibrary.domain.song.dto.SongUpdateReviewResponse;
import org.jones.licklibrary.domain.user.dto.AdminUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final SongService songService;

    public AdminController(UserService userService, SongService songService) {
        this.userService = userService;
        this.songService = songService;
    }

    @GetMapping("/queue")
    public List<AdminUserResponse> getPendingUsers() {
        return userService.getPendingUsers().stream()
            .map(this::toResponse)
            .toList();
    }

    @PostMapping("/approve/{userId}")
    public ResponseEntity<?> approveUser(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if ("ACCOUNT_DELETION".equals(user.getRequestType())) {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(toResponse(userService.approveUser(userId)));
    }

    @PostMapping("/reject/{userId}")
    public AdminUserResponse rejectUser(@PathVariable Long userId) {
        return toResponse(userService.rejectUser(userId));
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/song-updates")
    public List<SongUpdateRequestSummary> getPendingSongUpdates() {
        return songService.getPendingSongUpdates();
    }

    @GetMapping("/song-updates/{id}")
    public SongUpdateReviewResponse getSongUpdateForReview(@PathVariable UUID id) {
        return songService.getSongUpdateForReview(id);
    }

    @PostMapping("/song-updates/{id}/approve")
    public ResponseEntity<Void> approveSongUpdate(@PathVariable UUID id) {
        songService.approveSongUpdate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/song-updates/{id}/reject")
    public ResponseEntity<Void> rejectSongUpdate(@PathVariable UUID id) {
        songService.rejectSongUpdate(id);
        return ResponseEntity.ok().build();
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(user.getId(), user.getEmail(), user.getUsername(),
            user.getRole(), user.getStatus(), user.getCreationTs(), user.getRequestType());
    }
}
