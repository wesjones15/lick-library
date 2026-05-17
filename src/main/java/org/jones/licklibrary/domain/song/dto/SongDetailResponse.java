package org.jones.licklibrary.domain.song.dto;

import org.jones.licklibrary.domain.song.parsing.ChordSheetLine;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SongDetailResponse(
        UUID id,
        String title,
        String artist,
        String originalKey,
        Integer capo,
        Integer tempo,
        List<ChordSheetLine> chordLines,
        int numColumns,
        boolean canReparse,
        String rawChordSheet,
        Map<Integer, SongLickInfo> songLicks
) {}
