package org.jones.licklibrary.domain.song.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SongUpdateRequestSummary(
        UUID id,
        UUID songId,
        String songTitle,
        String songArtist,
        String submitterUsername,
        String requestType,
        LocalDateTime createdAt
) {}
