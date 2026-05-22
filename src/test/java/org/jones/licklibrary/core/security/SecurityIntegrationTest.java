package org.jones.licklibrary.core.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jones.licklibrary.domain.user.User;
import org.jones.licklibrary.domain.user.UserRepository;
import org.jones.licklibrary.domain.user.UserRole;
import org.jones.licklibrary.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:sectest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    private Long adminId;

    @BeforeEach
    void seedAdminUser() {
        userRepository.deleteAll();
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setUsername("admin");
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.APPROVED);
        admin.setCreationTs(LocalDateTime.now());
        adminId = userRepository.save(admin).getId();
    }

    @Test
    void unauthenticated_request_returns_401() throws Exception {
        mockMvc.perform(get("/api/lick"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void dev_login_returns_token() throws Exception {
        String body = mockMvc.perform(post("/api/auth/dev/login")
                .param("userId", adminId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        String token = json.get("token").asText();
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void authenticated_request_returns_200() throws Exception {
        String body = mockMvc.perform(post("/api/auth/dev/login")
                .param("userId", adminId.toString()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(body).get("token").asText();

        mockMvc.perform(get("/api/lick")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void invalid_token_returns_401() throws Exception {
        mockMvc.perform(get("/api/lick")
                .header("Authorization", "Bearer invalid.token.here"))
            .andExpect(status().isUnauthorized());
    }
}
