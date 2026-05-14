package org.jones.licklibrary.domain.shared;

public record IntervalNote(Interval interval, String technique, int columnIndex) {

    @Override
    public String toString() {
        String s = interval.displayName();
        return (technique != null && !technique.isEmpty()) ? s + " " + technique : s;
    }
}
