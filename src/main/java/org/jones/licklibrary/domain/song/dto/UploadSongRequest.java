package org.jones.licklibrary.domain.song.dto;

public record UploadSongRequest(
        String title,
        String artist,
        String originalKey,
        String mode,
        String instrument,
        Integer capo,
        Integer tempo,
        String rawChordSheet,
        Integer timeSignature
) {}
