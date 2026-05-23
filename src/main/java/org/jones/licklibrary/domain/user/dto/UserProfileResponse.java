package org.jones.licklibrary.domain.user.dto;

import org.jones.licklibrary.domain.user.UserRole;
import org.jones.licklibrary.domain.user.UserStatus;

import java.time.LocalDateTime;

public record UserProfileResponse(
    Long id,
    String email,
    String username,
    UserRole role,
    UserStatus status,
    LocalDateTime creationTs,
    String requestType
) {}
