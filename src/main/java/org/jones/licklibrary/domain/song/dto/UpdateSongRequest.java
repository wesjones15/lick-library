package org.jones.licklibrary.domain.song.dto;

public record UpdateSongRequest(
        String title,
        String artist,
        String originalKey,
        String mode,
        String instrument,
        Integer capo,
        Integer tempo,
        String rawChordSheet
) {}
