package org.jones.licklibrary.domain.song;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jones.licklibrary.core.config.FlexibleTestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(FlexibleTestSecurityConfig.class)
class SongControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SongRepository songRepository;

    private String ownedSongId;

    @BeforeEach
    void seed() throws Exception {
        songRepository.deleteAll();

        String body = objectMapper.writeValueAsString(Map.of(
            "title", "Test Song",
            "artist", "Test Artist",
            "rawChordSheet", "[verse]\nC G Am F\nHello world"
        ));

        MvcResult result = mockMvc.perform(post("/api/song")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

        ownedSongId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    // --- updateSong ---

    @Test
    void updateSong_owner_succeeds() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", "Updated Title"));
        mockMvc.perform(put("/api/song/{id}", ownedSongId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }

    @Test
    void updateSong_nonOwner_returns403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", "Stolen Title"));
        mockMvc.perform(put("/api/song/{id}", ownedSongId)
                .header("X-Test-Role", "OTHER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateSong_admin_succeeds() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", "Admin Updated"));
        MvcResult result = mockMvc.perform(put("/api/song/{id}", ownedSongId)
                .header("X-Test-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("Admin Updated", json.get("title").asText());
    }

    // --- deleteSong ---

    @Test
    void deleteSong_nonOwner_returns403() throws Exception {
        mockMvc.perform(delete("/api/song/{id}", ownedSongId)
                .header("X-Test-Role", "OTHER"))
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteSong_admin_succeeds() throws Exception {
        mockMvc.perform(delete("/api/song/{id}", ownedSongId)
                .header("X-Test-Role", "ADMIN"))
            .andExpect(status().isNoContent());

        assertTrue(songRepository.findById(UUID.fromString(ownedSongId)).isEmpty());
    }

    // --- reparseSong ---

    @Test
    void reparseSong_nonOwner_returns403() throws Exception {
        mockMvc.perform(put("/api/song/{id}/reparse", ownedSongId)
                .header("X-Test-Role", "OTHER"))
            .andExpect(status().isForbidden());
    }

    @Test
    void reparseSong_admin_succeeds() throws Exception {
        mockMvc.perform(put("/api/song/{id}/reparse", ownedSongId)
                .header("X-Test-Role", "ADMIN"))
            .andExpect(status().isOk());
    }

    // --- beatmap ---

    @Test
    void saveBeatmap_nonOwner_returns403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("beats", List.of(120)));
        mockMvc.perform(put("/api/song/{id}/beatmap", ownedSongId)
                .header("X-Test-Role", "OTHER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    void saveBeatmap_admin_succeeds() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("beats", List.of(120, 130)));
        mockMvc.perform(put("/api/song/{id}/beatmap", ownedSongId)
                .header("X-Test-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }
}
