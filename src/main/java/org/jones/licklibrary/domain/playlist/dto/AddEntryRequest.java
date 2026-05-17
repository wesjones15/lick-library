package org.jones.licklibrary.domain.playlist.dto;

import java.util.UUID;

public record AddEntryRequest(UUID songId, int keyOffset, int capoOffset) {}
