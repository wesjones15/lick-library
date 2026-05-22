package org.jones.licklibrary.core.auth;

import org.jones.licklibrary.domain.user.User;
import org.jones.licklibrary.domain.user.UserRole;
import org.jones.licklibrary.domain.user.UserService;
import org.jones.licklibrary.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2UserServiceTest {

    @Mock OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
    @Mock UserService userService;
    @Mock OAuth2UserRequest userRequest;
    @Mock OAuth2User delegateUser;

    GoogleOAuth2UserService service;

    @BeforeEach
    void setUp() {
        service = new GoogleOAuth2UserService(delegate, userService);
    }

    @Test
    void loadUser_callsFindOrCreateWithCorrectArgs() {
        Map<String, Object> attrs = Map.of(
            "sub", "google-123",
            "email", "user@example.com",
            "name", "Test User"
        );
        when(delegate.loadUser(userRequest)).thenReturn(delegateUser);
        when(delegateUser.getAttribute("sub")).thenReturn("google-123");
        when(delegateUser.getAttribute("email")).thenReturn("user@example.com");
        when(delegateUser.getAttribute("name")).thenReturn("Test User");
        when(delegateUser.getAttributes()).thenReturn(attrs);

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 42L);
        mockUser.setRole(UserRole.USER);
        mockUser.setStatus(UserStatus.PENDING);
        when(userService.findOrCreateByGoogle("google-123", "user@example.com", "Test User"))
            .thenReturn(mockUser);

        OAuth2User result = service.loadUser(userRequest);

        verify(userService).findOrCreateByGoogle("google-123", "user@example.com", "Test User");
        assertInstanceOf(LickLibraryOAuth2User.class, result);
    }

    @Test
    void loadUser_wrapsCorrectUser() {
        Map<String, Object> attrs = Map.of("sub", "g-id", "email", "a@b.com", "name", "A");
        when(delegate.loadUser(userRequest)).thenReturn(delegateUser);
        when(delegateUser.getAttribute("sub")).thenReturn("g-id");
        when(delegateUser.getAttribute("email")).thenReturn("a@b.com");
        when(delegateUser.getAttribute("name")).thenReturn("A");
        when(delegateUser.getAttributes()).thenReturn(attrs);

        User mockUser = new User();
        ReflectionTestUtils.setField(mockUser, "id", 7L);
        mockUser.setRole(UserRole.ADMIN);
        mockUser.setStatus(UserStatus.APPROVED);
        when(userService.findOrCreateByGoogle(any(), any(), any())).thenReturn(mockUser);

        LickLibraryOAuth2User result = (LickLibraryOAuth2User) service.loadUser(userRequest);

        assertEquals(mockUser, result.getUser());
        assertEquals("7", result.getName());
    }
}
