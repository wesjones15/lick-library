package org.jones.licklibrary.core.auth;

import org.jones.licklibrary.domain.user.User;
import org.jones.licklibrary.domain.user.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
    private final UserService userService;

    public GoogleOAuth2UserService(
            @Qualifier("googleOAuth2Delegate") OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate,
            UserService userService) {
        this.delegate = delegate;
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(userRequest);
        String googleId = oauthUser.getAttribute("sub");
        String email    = oauthUser.getAttribute("email");
        String name     = oauthUser.getAttribute("name");
        User user = userService.findOrCreateByGoogle(googleId, email, name);
        return new LickLibraryOAuth2User(user, oauthUser.getAttributes());
    }
}
