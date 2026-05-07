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
            if (sb.length() > 0) sb.append(" ");
            sb.append(in.toString());
        }
        return sb.toString();
    }

    @Override
    public List<IntervalNote> convertToEntityAttribute(String dbData) {
        List<IntervalNote> out = new ArrayList<>();
        if (dbData == null || dbData.isBlank()) return out;
        String[] tokens = dbData.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].matches("b?[1-7]")) {
                Interval interval = Interval.fromDisplayName(tokens[i]);
                String technique = null;
                if (i + 1 < tokens.length && !tokens[i + 1].matches("b?[1-7]")) {
                    technique = tokens[++i];
                }
                out.add(new IntervalNote(interval, technique));
            }
        }
        return out;
    }
}
