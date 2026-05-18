package org.jones.licklibrary.domain.lick.dto;

import org.jones.licklibrary.domain.shared.Mode;
import org.jones.licklibrary.domain.shared.Note;

public record UploadLickRequest(String rawTab, Mode mode, Note inputKey, String instrument) {}
