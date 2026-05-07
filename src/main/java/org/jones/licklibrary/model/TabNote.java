package org.jones.licklibrary.model;

import org.jones.licklibrary.constants.Guitar;
import org.jones.licklibrary.constants.Note;

public record TabNote(int stringIndex, Integer fret, int columnIndex, String technique) {
    public Note toNote() {
        return Guitar.getNoteAt(this.stringIndex,this.fret);
    }
}
