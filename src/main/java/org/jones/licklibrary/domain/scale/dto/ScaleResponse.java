package org.jones.licklibrary.domain.scale.dto;

import java.util.List;

public record ScaleResponse(String root, String mode, List<ScalePosition> positions) {}
