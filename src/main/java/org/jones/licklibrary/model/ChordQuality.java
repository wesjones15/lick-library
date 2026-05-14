package org.jones.licklibrary.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "chord_quality")
public class ChordQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String suffix;

    public ChordQuality() {}

    public ChordQuality(String suffix) {
        this.suffix = suffix;
    }

    public UUID getId() { return id; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
}
