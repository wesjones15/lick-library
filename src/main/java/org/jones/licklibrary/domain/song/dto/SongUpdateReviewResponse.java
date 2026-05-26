package org.jones.licklibrary.domain.song.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SongUpdateReviewResponse(
        UUID id,
        UUID songId,
        String songTitle,
        String songArtist,
        String submitterUsername,
        String requestType,
        String currentValue,
        String proposedValue,
        LocalDateTime createdAt
) {}
