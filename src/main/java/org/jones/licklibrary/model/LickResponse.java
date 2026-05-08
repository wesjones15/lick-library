package org.jones.licklibrary.model;

import java.util.List;
import java.util.UUID;

public record LickResponse(
    UUID id,
    String rawTab,
    String intervalDisplayString,
    Mode mode,
    List<Position> positions
) {}
