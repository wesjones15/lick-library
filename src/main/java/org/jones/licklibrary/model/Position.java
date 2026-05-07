package org.jones.licklibrary.model;

import java.util.List;

// Each int[] is [stringIndex, fret] — one entry per note in the sequence
public record Position(List<int[]> locations) {}
