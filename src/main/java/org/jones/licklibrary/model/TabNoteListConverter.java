package org.jones.licklibrary.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class TabNoteListConverter implements AttributeConverter<List<TabNote>, String> {

    @Override
    public String convertToDatabaseColumn(List<TabNote> notes) {
        if (notes == null || notes.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (TabNote n : notes) {
            if (sb.length() > 0) sb.append(",");
            sb.append(n.stringIndex()).append(":")
              .append(n.fret()).append(":")
              .append(n.columnIndex()).append(":")
              .append(n.technique() != null ? n.technique() : "");
        }
        return sb.toString();
    }

    @Override
    public List<TabNote> convertToEntityAttribute(String dbData) {
        List<TabNote> out = new ArrayList<>();
        if (dbData == null || dbData.isBlank()) return out;
        for (String token : dbData.split(",")) {
            String[] p = token.split(":", -1);
            int stringIndex  = Integer.parseInt(p[0]);
            int fret         = Integer.parseInt(p[1]);
            int columnIndex  = Integer.parseInt(p[2]);
            String technique = p.length > 3 && !p[3].isEmpty() ? p[3] : null;
            out.add(new TabNote(stringIndex, fret, columnIndex, technique));
        }
        return out;
    }
}
