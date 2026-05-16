package org.jones.licklibrary.domain.playlist.dto;

public record UpdateEntryRequest(Integer overrideSemitones, Integer overrideCapo, Integer position) {}
