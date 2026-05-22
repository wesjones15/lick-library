package org.jones.licklibrary.domain.user;

import org.jones.licklibrary.core.exception.ResourceNotFoundException;
import org.jones.licklibrary.core.security.JwtTokenProvider;
import org.jones.licklibrary.domain.user.dto.AdminUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.admin.email}")
    private String adminEmail;

    public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public String issueToken(User user) {
        return jwtTokenProvider.generateToken(user);
    }

    public User findOrCreateByGoogle(String googleId, String email, String name) {
        return userRepository.findByGoogleId(googleId).orElseGet(() -> {
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(name);
                newUser.setCreationTs(LocalDateTime.now());
                newUser.setRole(UserRole.USER);
                newUser.setStatus(UserStatus.PENDING);
                return newUser;
            });
            if (adminEmail.equalsIgnoreCase(email)) {
                user.setRole(UserRole.ADMIN);
                user.setStatus(UserStatus.APPROVED);
            }
            user.setGoogleId(googleId);
            return userRepository.save(user);
        });
    }

    public List<User> getPendingUsers() {
        return userRepository.findByStatus(UserStatus.PENDING);
    }

    public User approveUser(Long userId) {
        User user = findById(userId);
        user.setStatus(UserStatus.APPROVED);
        return userRepository.save(user);
    }

    public User rejectUser(Long userId) {
        User user = findById(userId);
        user.setStatus(UserStatus.REJECTED);
        return userRepository.save(user);
    }

    public String getUsernameById(Long userId) {
        if (userId == null) return "unknown";
        return userRepository.findById(userId).map(User::getUsername).orElse("unknown");
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(u -> new AdminUserResponse(u.getId(), u.getEmail(), u.getUsername(),
                                            u.getRole(), u.getStatus(), u.getCreationTs()))
            .toList();
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
