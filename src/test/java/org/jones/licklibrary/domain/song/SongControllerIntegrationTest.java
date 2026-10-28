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

    // --- shared song licks ---

    @Autowired org.jones.licklibrary.domain.lick.LickRepository lickRepository;

    private static final String SHARED_TAB_SHEET =
            "G\nHello\n"
            + "e|-----------|\n"
            + "B|-----------|\n"
            + "G|-----------|\n"
            + "D|-----------|\n"
            + "A|-----------|\n"
            + "E|--3--5--7--|\n";

    private String uploadSong(String title) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "title", title, "originalKey", "G", "rawChordSheet", SHARED_TAB_SHEET));
        MvcResult result = mockMvc.perform(post("/api/song")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String songLickId(String songId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/song/{id}", songId)).andExpect(status().isOk()).andReturn();
        JsonNode licks = objectMapper.readTree(result.getResponse().getContentAsString()).get("songLicks");
        return licks.elements().next().get("lickId").asText();
    }

    @Test
    void deletingOneSongKeepsALickAnotherSongStillUses() throws Exception {
        String first = uploadSong("First");
        String second = uploadSong("Second");
        String lickId = songLickId(second);
        assertEquals(songLickId(first), lickId, "same tab shape should share one lick");

        mockMvc.perform(delete("/api/song/{id}", first)).andExpect(status().isNoContent());

        assertTrue(lickRepository.existsById(UUID.fromString(lickId)));
        mockMvc.perform(get("/api/lick/{id}", lickId).param("key", "G")).andExpect(status().isOk());
    }

    @Test
    void reparsingKeepsTheSameLickId() throws Exception {
        String songId = uploadSong("Stable");
        String before = songLickId(songId);
        mockMvc.perform(put("/api/song/{id}/reparse", songId)).andExpect(status().isOk());
        assertEquals(before, songLickId(songId));
    }

    @Test
    void songWithLegacyEnumKeyStillImportsItsTab() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "title", "Legacy", "originalKey", "F_SHARP", "rawChordSheet", SHARED_TAB_SHEET));
        MvcResult result = mockMvc.perform(post("/api/song")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andReturn();
        String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        assertNotNull(songLickId(id));
    }
}
