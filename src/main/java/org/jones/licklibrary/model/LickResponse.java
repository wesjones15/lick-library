package org.jones.licklibrary.model;

import java.util.List;

public record LickResponse(
    String intervalHash,
    List<IntervalNote> intervals,
    List<Position> positions
) {}
