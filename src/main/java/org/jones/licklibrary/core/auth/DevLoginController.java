package org.jones.licklibrary.core.auth;

import org.jones.licklibrary.domain.user.User;
import org.jones.licklibrary.domain.user.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Profile("dev")
public class DevLoginController {

    private final UserService userService;

    public DevLoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/dev-login")
    public ResponseEntity<Map<String, String>> devLogin(@RequestParam Long userId) {
        User user = userService.findById(userId);
        String token = userService.issueToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
