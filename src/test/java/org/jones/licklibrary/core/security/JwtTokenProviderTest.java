package org.jones.licklibrary.core.security;

import org.jones.licklibrary.domain.user.User;
import org.jones.licklibrary.domain.user.UserRole;
import org.jones.licklibrary.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret", "test-secret-key-for-unit-tests-min-256-bits!!");
        ReflectionTestUtils.setField(provider, "expiryMs", 86400000L);
    }

    private User adminUser() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.APPROVED);
        user.setEmail("admin@example.com");
        user.setUsername("admin");
        return user;
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = provider.generateToken(adminUser());
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken_acceptsValidToken() {
        String token = provider.generateToken(adminUser());
        assertTrue(provider.validateToken(token));
    }

    @Test
    void validateToken_rejectsGarbage() {
        assertFalse(provider.validateToken("not.a.jwt"));
    }

    @Test
    void validateToken_rejectsEmpty() {
        assertFalse(provider.validateToken(""));
    }

    @Test
    void getPrincipal_extractsCorrectClaims() {
        String token = provider.generateToken(adminUser());
        UserPrincipal principal = provider.getPrincipal(token);
        assertEquals(1L, principal.userId());
        assertEquals(UserRole.ADMIN, principal.role());
        assertEquals(UserStatus.APPROVED, principal.status());
    }
}
