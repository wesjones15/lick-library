package org.jones.licklibrary.domain.song.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class ChordLyricListConverter implements AttributeConverter<List<ChordLyric>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ChordLyric> chordLyrics) {
        if (chordLyrics == null || chordLyrics.isEmpty()) return "[]";
        try {
            return MAPPER.writeValueAsString(chordLyrics);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize chord lyrics", e);
        }
    }

    @Override
    public List<ChordLyric> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new ArrayList<>();
        try {
            return MAPPER.readValue(dbData, new TypeReference<List<ChordLyric>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize chord lyrics", e);
        }
    }
}
