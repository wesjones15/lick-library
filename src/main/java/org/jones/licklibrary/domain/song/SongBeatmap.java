package org.jones.licklibrary.domain.song;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "song_beatmap")
public class SongBeatmap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "song_id", nullable = false, unique = true)
    private UUID songId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String beats; // comma-separated integers e.g. "4,4,2,2,4"

    public UUID getId() { return id; }
    public UUID getSongId() { return songId; }
    public void setSongId(UUID songId) { this.songId = songId; }
    public String getBeats() { return beats; }
    public void setBeats(String beats) { this.beats = beats; }
}
