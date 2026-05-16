package org.jones.licklibrary.domain.song.parsing;

import java.util.List;

public record GuitarTabLine(String header, List<String> tabLines, double fontSize)
        implements ChordSheetLine {}
