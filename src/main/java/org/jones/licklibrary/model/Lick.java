package org.jones.licklibrary.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @Column(name = "raw_tab", columnDefinition = "TEXT")
    private String rawTab;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Mode mode;

    @Column(name = "endpoint_degree", length = 16)
    private String endpointDegree;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public String getIntervalHash() { return intervalHash; }
    public void setIntervalHash(String intervalHash) { this.intervalHash = intervalHash; }
    public List<IntervalNote> getIntervals() { return intervals; }
    public void setIntervals(List<IntervalNote> intervals) { this.intervals = intervals; }
    public String getRawTab() { return rawTab; }
    public void setRawTab(String rawTab) { this.rawTab = rawTab; }
    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }
    public String getEndpointDegree() { return endpointDegree; }
    public void setEndpointDegree(String endpointDegree) { this.endpointDegree = endpointDegree; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return IntervalNoteListConverter.toDisplayString(intervals);
    }
}
