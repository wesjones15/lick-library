package org.jones.licklibrary.domain.user;

import org.jones.licklibrary.core.security.UserPrincipal;
import org.jones.licklibrary.domain.user.dto.UpdateUsernameRequest;
import org.jones.licklibrary.domain.user.dto.UserProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserProfileResponse getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.findById(principal.userId());
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getUsername(),
            user.getRole(), user.getStatus(), user.getCreationTs(), user.getRequestType());
    }

    @PostMapping("/request-deletion")
    public ResponseEntity<Void> requestDeletion(@AuthenticationPrincipal UserPrincipal principal) {
        userService.requestDeletion(principal.userId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteUser(principal.userId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/username")
    public UserProfileResponse updateUsername(@AuthenticationPrincipal UserPrincipal principal,
                                              @RequestBody UpdateUsernameRequest req) {
        return userService.updateUsername(principal.userId(), req.username());
    }
}
