package org.jones.licklibrary.domain.user;

import org.jones.licklibrary.core.exception.ResourceNotFoundException;
import org.jones.licklibrary.core.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

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
}
