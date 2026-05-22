package org.jones.licklibrary.domain.playlist.dto;

import java.util.List;
import java.util.UUID;

public record PlaylistDetailResponse(UUID id, String name, List<PlaylistEntryResponse> entries, boolean ownedByCurrentUser, boolean isPublic) {}
