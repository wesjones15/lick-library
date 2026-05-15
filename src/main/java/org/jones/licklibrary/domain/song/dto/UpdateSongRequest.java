package org.jones.licklibrary.domain.song.dto;

public record UpdateSongRequest(
        String title,
        String artist,
        String originalKey,
        Integer capo,
        Integer tempo,
        String rawChordSheet
) {}
