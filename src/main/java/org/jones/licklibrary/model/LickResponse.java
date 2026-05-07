package org.jones.licklibrary.model;

import java.util.List;

public record LickResponse(
    String intervalHash,
    List<String> intervals,
    List<Position> positions,
    String sourceTab
) {}
