package org.jones.licklibrary.domain.playlist.dto;

import java.util.UUID;

public record PlaylistEntryResponse(
        UUID entryId,
        UUID songId,
        String title,
        String artist,
        int position,
        int keyOffset,
        int capoOffset,
        String originalKey,
        int defaultCapo,
        Integer tempo,
        String mode
) {}
