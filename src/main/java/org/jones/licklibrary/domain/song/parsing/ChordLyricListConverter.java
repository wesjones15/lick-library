package org.jones.licklibrary.domain.song.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class ChordLyricListConverter implements AttributeConverter<List<ChordSheetLine>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final TypeReference<List<ChordSheetLine>> LIST_TYPE =
            new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<ChordSheetLine> lines) {
        if (lines == null || lines.isEmpty()) return "[]";
        try {
            return MAPPER.writerFor(LIST_TYPE).writeValueAsString(lines);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize chord lines", e);
        }
    }

    @Override
    public List<ChordSheetLine> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return new ArrayList<>();
        try {
            return MAPPER.readValue(dbData, LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize chord lines", e);
        }
    }
}
