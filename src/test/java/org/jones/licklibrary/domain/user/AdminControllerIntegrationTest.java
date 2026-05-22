package org.jones.licklibrary.domain.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:admintest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class AdminControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    private Long pendingUserId;

    @BeforeEach
    void seed() {
        userRepository.deleteAll();
        User pending = new User();
        pending.setEmail("pending@test.com");
        pending.setUsername("pendinguser");
        pending.setRole(UserRole.USER);
        pending.setStatus(UserStatus.PENDING);
        pending.setCreationTs(LocalDateTime.now());
        pendingUserId = userRepository.save(pending).getId();
    }

    @Test
    void getQueue_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/queue"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getQueue_nonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/queue"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQueue_admin_returnsPendingUsers() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/queue"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, json.size());
        assertEquals("pending@test.com", json.get(0).get("email").asText());
        assertEquals("PENDING", json.get(0).get("status").asText());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approve_flipsStatusToApproved() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/approve/" + pendingUserId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("APPROVED", json.get("status").asText());
        assertEquals(UserStatus.APPROVED, userRepository.findById(pendingUserId).get().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reject_flipsStatusToRejected() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/reject/" + pendingUserId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("REJECTED", json.get("status").asText());
        assertEquals(UserStatus.REJECTED, userRepository.findById(pendingUserId).get().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approve_unknownUser_returns404() throws Exception {
        mockMvc.perform(post("/api/admin/approve/99999"))
            .andExpect(status().isNotFound());
    }
}
