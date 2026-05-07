package org.jones.licklibrary.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lick")
public class Lick {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "interval_hash", unique = true, nullable = false, length = 64)
    private String intervalHash;

    @Column(nullable = false)
    private String intervals;          // e.g. "ONE,FLAT_THREE,FOUR,FIVE"

    @Column(name = "source_tab", columnDefinition = "TEXT")
    private String sourceTab;

    @Column(name = "mode_tag", length = 32)
    private String modeTag;

    @Column(name = "endpoint_degree", length = 16)
    private String endpointDegree;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public String getIntervalHash() { return intervalHash; }
    public void setIntervalHash(String intervalHash) { this.intervalHash = intervalHash; }
    public String getIntervals() { return intervals; }
    public void setIntervals(String intervals) { this.intervals = intervals; }
    public String getSourceTab() { return sourceTab; }
    public void setSourceTab(String sourceTab) { this.sourceTab = sourceTab; }
    public String getModeTag() { return modeTag; }
    public void setModeTag(String modeTag) { this.modeTag = modeTag; }
    public String getEndpointDegree() { return endpointDegree; }
    public void setEndpointDegree(String endpointDegree) { this.endpointDegree = endpointDegree; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
