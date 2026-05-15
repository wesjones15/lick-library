package org.jones.licklibrary.domain.song.dto;

import org.jones.licklibrary.domain.song.parsing.ChordLyric;

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
        int numColumns,
        boolean canReparse,
        String rawChordSheet
) {}
