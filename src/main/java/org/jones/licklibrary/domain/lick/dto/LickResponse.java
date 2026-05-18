package org.jones.licklibrary.domain.lick.dto;

import org.jones.licklibrary.domain.shared.Mode;

import java.util.List;
import java.util.UUID;

public record LickResponse(
    UUID id,
    String rawTab,
    String intervalDisplayString,
    Mode mode,
    List<PositionResponse> positions,
    boolean autoImported,
    String instrument
) {}
