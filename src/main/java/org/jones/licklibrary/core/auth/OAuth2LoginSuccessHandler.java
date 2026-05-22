package org.jones.licklibrary.core.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jones.licklibrary.domain.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final UserService userService;

    public OAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        LickLibraryOAuth2User oauthUser = (LickLibraryOAuth2User) authentication.getPrincipal();
        String token = userService.issueToken(oauthUser.getUser());
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/auth?token=" + token);
    }
}
