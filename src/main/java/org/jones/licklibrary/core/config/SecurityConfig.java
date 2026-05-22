package org.jones.licklibrary.core.config;

import jakarta.servlet.http.HttpServletResponse;
import org.jones.licklibrary.core.auth.GoogleOAuth2UserService;
import org.jones.licklibrary.core.auth.OAuth2LoginFailureHandler;
import org.jones.licklibrary.core.auth.OAuth2LoginSuccessHandler;
import org.jones.licklibrary.core.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final GoogleOAuth2UserService oauthUserService;
    private final OAuth2LoginSuccessHandler successHandler;
    private final OAuth2LoginFailureHandler failureHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          GoogleOAuth2UserService oauthUserService,
                          OAuth2LoginSuccessHandler successHandler,
                          OAuth2LoginFailureHandler failureHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oauthUserService = oauthUserService;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/error").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(ae -> ae.baseUri("/api/oauth2/authorize"))
                .redirectionEndpoint(re -> re.baseUri("/api/auth/callback"))
                .userInfoEndpoint(ui -> ui.userService(oauthUserService))
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
