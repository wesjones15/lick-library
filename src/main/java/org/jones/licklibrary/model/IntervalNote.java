package org.jones.licklibrary.model;

import org.jones.licklibrary.constants.Interval;

public record IntervalNote(Interval interval, String technique) {

    @Override
    public String toString() {
        String s = interval.displayName();
        return (technique != null && !technique.isEmpty()) ? s + " " + technique : s;
    }
}
