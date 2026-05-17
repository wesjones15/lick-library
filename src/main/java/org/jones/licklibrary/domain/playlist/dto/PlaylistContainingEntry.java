package org.jones.licklibrary.domain.playlist.dto;

import java.util.UUID;

public record PlaylistContainingEntry(UUID playlistId, UUID entryId) {}
