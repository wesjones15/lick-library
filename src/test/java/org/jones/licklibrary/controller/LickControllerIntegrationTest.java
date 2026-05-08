package org.jones.licklibrary.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jones.licklibrary.repository.LickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LickControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired LickRepository lickRepository;
    @Autowired ObjectMapper objectMapper;

    // A string: open=A(ONE), fret2=B(TWO), fret4=C#(THREE) — no flats → IONIAN
    static final String MAJOR_TAB = """
            e|---------|
            B|---------|
            G|---------|
            D|---------|
            A|-0-2-4---|
            E|---------|""";

    // A string: open=A(ONE), fret2=B(TWO), fret3=C(FLAT_THREE) — b3 → AEOLIAN
    static final String MINOR_TAB = """
            e|---------|
            B|---------|
            G|---------|
            D|---------|
            A|-0-2-3---|
            E|---------|""";

    @BeforeEach
    void cleanDb() {
        lickRepository.deleteAll();
    }

    @Test
    void uploadLick_storesNewLick() throws Exception {
        String body = objectMapper.writeValueAsString(
            new java.util.HashMap<>() {{ put("rawTab", MAJOR_TAB); }});

        MvcResult result = mockMvc.perform(post("/api/lick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("1 2 3", json.get("intervalDisplayString").asText());
        assertEquals("IONIAN", json.get("mode").asText());
        assertNotNull(json.get("id").asText());
        assertEquals(1, lickRepository.count());
    }

    @Test
    void uploadLick_deduplicates() throws Exception {
        String body = objectMapper.writeValueAsString(
            new java.util.HashMap<>() {{ put("rawTab", MAJOR_TAB); }});

        MvcResult first = mockMvc.perform(post("/api/lick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

        MvcResult second = mockMvc.perform(post("/api/lick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

        String firstId = objectMapper.readTree(first.getResponse().getContentAsString()).get("id").asText();
        String secondId = objectMapper.readTree(second.getResponse().getContentAsString()).get("id").asText();

        assertEquals(firstId, secondId);
        assertEquals(1, lickRepository.count());
    }

    @Test
    void uploadLick_detectsMode() throws Exception {
        String body = objectMapper.writeValueAsString(
            new java.util.HashMap<>() {{ put("rawTab", MINOR_TAB); }});

        MvcResult result = mockMvc.perform(post("/api/lick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("AEOLIAN", json.get("mode").asText());
    }

    @Test
    void getAllLicks_returnsStoredLicks() throws Exception {
        String body = objectMapper.writeValueAsString(
            new java.util.HashMap<>() {{ put("rawTab", MAJOR_TAB); }});

        mockMvc.perform(post("/api/lick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/lick"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, json.size());
        assertEquals("1 2 3", json.get(0).get("intervalDisplayString").asText());
    }

    @Test
    void getLick_returnsPositionsForKey() throws Exception {
        String body = objectMapper.writeValueAsString(
            new java.util.HashMap<>() {{ put("rawTab", MAJOR_TAB); }});

        MvcResult upload = mockMvc.perform(post("/api/lick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

        String id = objectMapper.readTree(upload.getResponse().getContentAsString()).get("id").asText();

        MvcResult result = mockMvc.perform(get("/api/lick/{id}", id).param("key", "A"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode positions = json.get("positions");
        assertNotNull(positions);
        assertTrue(positions.size() > 0);
        assertFalse(positions.get(0).get("notes").isEmpty());
    }
}
