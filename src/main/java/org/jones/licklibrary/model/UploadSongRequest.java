package org.jones.licklibrary.model;

public record UploadSongRequest(
        String title,
        String artist,
        String originalKey,
        Integer capo,
        Integer tempo,
        String rawChordSheet
) {}
