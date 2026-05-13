package org.jones.licklibrary.model;

import java.util.List;
import java.util.UUID;

public record SongDetailResponse(
        UUID id,
        String title,
        String artist,
        String originalKey,
        Integer capo,
        Integer tempo,
        List<ChordLyric> chordLines,
        int numColumns
) {}
