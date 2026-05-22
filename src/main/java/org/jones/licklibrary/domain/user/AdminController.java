package org.jones.licklibrary.domain.user;

import org.jones.licklibrary.domain.user.dto.AdminUserResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/queue")
    public List<AdminUserResponse> getPendingUsers() {
        return userService.getPendingUsers().stream()
            .map(this::toResponse)
            .toList();
    }

    @PostMapping("/approve/{userId}")
    public AdminUserResponse approveUser(@PathVariable Long userId) {
        return toResponse(userService.approveUser(userId));
    }

    @PostMapping("/reject/{userId}")
    public AdminUserResponse rejectUser(@PathVariable Long userId) {
        return toResponse(userService.rejectUser(userId));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(user.getId(), user.getEmail(), user.getUsername(),
            user.getRole(), user.getStatus(), user.getCreationTs());
    }
}
