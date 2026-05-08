package org.jones.licklibrary.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "position_cache", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"interval_hash", "key"})
})
public class PositionCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "interval_hash", nullable = false, length = 64)
    private String intervalHash;

    @Column(name = "note_key", nullable = false, length = 8)
    private String key;

    @Column(name = "positions_json", nullable = false, columnDefinition = "TEXT")
    private String positionsJson;

    public UUID getId() { return id; }
    public String getIntervalHash() { return intervalHash; }
    public void setIntervalHash(String intervalHash) { this.intervalHash = intervalHash; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getPositionsJson() { return positionsJson; }
    public void setPositionsJson(String positionsJson) { this.positionsJson = positionsJson; }
}
