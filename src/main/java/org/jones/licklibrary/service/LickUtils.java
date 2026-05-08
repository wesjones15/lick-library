package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Interval;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
import org.jones.licklibrary.model.TabNote;

import java.util.ArrayList;
import java.util.List;

public class LickUtils {

    private LickUtils() {}

    public static List<IntervalNote> toIntervals(List<TabNote> notes) {
        Note first = notes.get(0).toNote();
        List<IntervalNote> out = new ArrayList<>();
        int normalizedCol = 0;
        int lastRawCol = Integer.MIN_VALUE;
        for (TabNote tabNote : notes) {
            if (tabNote.columnIndex() != lastRawCol && lastRawCol != Integer.MIN_VALUE) {
                normalizedCol++;
            }
            lastRawCol = tabNote.columnIndex();
            Note note = tabNote.toNote();
            Interval interval = Interval.values()[(note.ordinal() - first.ordinal() + 12) % 12];
            out.add(new IntervalNote(interval, tabNote.technique(), normalizedCol));
        }
        return out;
    }

    public static List<Note> toAbsoluteNotes(List<IntervalNote> intervals, Note key) {
        List<Note> out = new ArrayList<>();
        for (IntervalNote in : intervals) {
            out.add(key.shift(in.interval().ordinal()));
        }
        return out;
    }

    public static int proximityScore(TabNote from, TabNote to) {
        return Math.abs(from.fret() - to.fret()) + Math.abs(from.stringIndex() - to.stringIndex());
    }

    public static String toNoteString(List<TabNote> tabnotes) {
        StringBuilder sb = new StringBuilder();
        for (TabNote tabnote : tabnotes) {
            Note note = tabnote.toNote();
            sb.append(note.toString());
            sb.append(" ").append(tabnote.technique());
        }
        return sb.toString();
    }

    public static String hashIntervals(List<IntervalNote> intervals) {
        throw new UnsupportedOperationException("TODO");
    }
}
