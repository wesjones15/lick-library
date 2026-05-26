package org.jones.licklibrary.core.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jones.licklibrary.core.security.UserPrincipal;
import org.jones.licklibrary.domain.user.UserRole;
import org.jones.licklibrary.domain.user.UserStatus;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Test-only security config that authenticates via X-Test-Role header:
 *   (none)  → userId=1,   role=USER  (the song owner in tests)
 *   OTHER   → userId=2,   role=USER  (a different non-owner user)
 *   ADMIN   → userId=999, role=ADMIN
 */
@TestConfiguration
public class FlexibleTestSecurityConfig {

    public static final Long OWNER_ID = 1L;
    public static final Long OTHER_ID = 2L;
    public static final Long ADMIN_ID = 999L;

    @Bean
    @Primary
    public SecurityFilterChain flexibleTestFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(new FlexibleTestAuthFilter(),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }

    static class FlexibleTestAuthFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            String roleHeader = request.getHeader("X-Test-Role");
            UserRole role;
            Long userId;
            if ("ADMIN".equals(roleHeader)) {
                role = UserRole.ADMIN;
                userId = ADMIN_ID;
            } else if ("OTHER".equals(roleHeader)) {
                role = UserRole.USER;
                userId = OTHER_ID;
            } else {
                role = UserRole.USER;
                userId = OWNER_ID;
            }
            UserPrincipal principal = new UserPrincipal(userId, role, UserStatus.APPROVED);
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        }
    }
}
