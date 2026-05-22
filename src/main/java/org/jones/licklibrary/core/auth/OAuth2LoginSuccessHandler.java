package org.jones.licklibrary.core.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jones.licklibrary.domain.user.User;
import org.jones.licklibrary.domain.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
        Object principal = authentication.getPrincipal();
        User user;
        if (principal instanceof LickLibraryOAuth2User oauthUser) {
            user = oauthUser.getUser();
        } else if (principal instanceof OidcUser oidcUser) {
            user = userService.findOrCreateByGoogle(
                oidcUser.getSubject(),
                oidcUser.getEmail(),
                oidcUser.getFullName()
            );
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        String token = userService.issueToken(user);
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/auth?token=" + token);
    }
}
