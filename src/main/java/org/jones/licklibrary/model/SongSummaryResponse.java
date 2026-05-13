package org.jones.licklibrary.model;

import java.util.UUID;

public record SongSummaryResponse(
        UUID id,
        String title,
        String artist,
        String originalKey,
        boolean canReparse
) {}
