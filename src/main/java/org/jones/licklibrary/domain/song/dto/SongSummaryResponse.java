package org.jones.licklibrary.domain.song.dto;

import java.util.UUID;

public record SongSummaryResponse(
        UUID id,
        String title,
        String artist,
        String originalKey,
        String mode,
        boolean canReparse,
        Integer tempo
) {}
