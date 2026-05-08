package org.jones.licklibrary.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jones.licklibrary.constants.Interval;

import java.util.ArrayList;
import java.util.List;

@Converter
public class IntervalNoteListConverter implements AttributeConverter<List<IntervalNote>, String> {

    @Override
    public String convertToDatabaseColumn(List<IntervalNote> intervals) {
        if (intervals == null || intervals.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (IntervalNote in : intervals) {
            if (sb.length() > 0) sb.append(",");
            sb.append(in.interval().displayName())
              .append(":").append(in.columnIndex())
              .append(":").append(in.technique() != null ? in.technique() : "");
        }
        return sb.toString();
    }

    @Override
    public List<IntervalNote> convertToEntityAttribute(String dbData) {
        List<IntervalNote> out = new ArrayList<>();
        if (dbData == null || dbData.isBlank()) return out;
        for (String token : dbData.split(",")) {
            String[] parts = token.split(":", 3);
            Interval interval = Interval.fromDisplayName(parts[0]);
            int columnIndex = Integer.parseInt(parts[1]);
            String technique = parts.length > 2 && !parts[2].isEmpty() ? parts[2] : null;
            out.add(new IntervalNote(interval, technique, columnIndex));
        }
        return out;
    }

    public static String toDisplayString(List<IntervalNote> intervals) {
        if (intervals == null || intervals.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (IntervalNote in : intervals) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(in.toString());
        }
        return sb.toString();
    }
}
