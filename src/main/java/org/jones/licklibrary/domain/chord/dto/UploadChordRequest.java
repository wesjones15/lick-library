package org.jones.licklibrary.domain.chord.dto;

import java.util.List;

public record UploadChordRequest(
    String root,
    String quality,
    List<String> frets,
    String shapeName,
    String instrument
) {}
