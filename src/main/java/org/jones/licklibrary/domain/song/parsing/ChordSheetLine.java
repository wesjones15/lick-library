package org.jones.licklibrary.domain.song.parsing;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = ChordLyric.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChordLyric.class,    name = "chord"),
    @JsonSubTypes.Type(value = GuitarTabLine.class, name = "tab")
})
public sealed interface ChordSheetLine permits ChordLyric, GuitarTabLine {}
