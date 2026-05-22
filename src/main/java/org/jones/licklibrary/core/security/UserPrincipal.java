package org.jones.licklibrary.core.security;

import org.jones.licklibrary.domain.user.UserRole;
import org.jones.licklibrary.domain.user.UserStatus;

public record UserPrincipal(Long userId, UserRole role, UserStatus status) {}
