package org.jones.licklibrary.model;

import org.jones.licklibrary.constants.Note;

public record UploadLickRequest(String rawTab, Mode mode, Note inputKey) {}
