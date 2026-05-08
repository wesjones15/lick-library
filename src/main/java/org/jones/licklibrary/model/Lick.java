package org.jones.licklibrary.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "lick")
public class Lick {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "interval_hash", unique = true, nullable = false, length = 64)
    private String intervalHash;

    @Convert(converter = IntervalNoteListConverter.class)
    @Column(nullable = false)
    private List<IntervalNote> intervals;

    @Convert(converter = TabNoteListConverter.class)
    @Column(name = "source_notes", columnDefinition = "TEXT")
    private List<TabNote> sourceNotes;

    @Column(name = "mode_tag", length = 32)
    private String modeTag;

    @Column(name = "endpoint_degree", length = 16)
    private String endpointDegree;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public String getIntervalHash() { return intervalHash; }
    public void setIntervalHash(String intervalHash) { this.intervalHash = intervalHash; }
    public List<IntervalNote> getIntervals() { return intervals; }
    public void setIntervals(List<IntervalNote> intervals) { this.intervals = intervals; }
    public List<TabNote> getSourceNotes() { return sourceNotes; }
    public void setSourceNotes(List<TabNote> sourceNotes) { this.sourceNotes = sourceNotes; }
    public String getModeTag() { return modeTag; }
    public void setModeTag(String modeTag) { this.modeTag = modeTag; }
    public String getEndpointDegree() { return endpointDegree; }
    public void setEndpointDegree(String endpointDegree) { this.endpointDegree = endpointDegree; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return IntervalNoteListConverter.toDisplayString(intervals);
    }
}
