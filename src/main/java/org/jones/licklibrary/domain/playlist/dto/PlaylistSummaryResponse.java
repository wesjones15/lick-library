package org.jones.licklibrary.domain.playlist.dto;

import java.util.UUID;

public record PlaylistSummaryResponse(UUID id, String name, int songCount, String authorName, boolean ownedByCurrentUser, boolean isPublic) {}
