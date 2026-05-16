package org.jones.licklibrary.domain.playlist;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "playlist_entry")
public class PlaylistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @Column(name = "song_id", nullable = false)
    private UUID songId;

    @Column(nullable = false)
    private int position;

    @Column(name = "override_semitones")
    private Integer overrideSemitones;

    @Column(name = "override_capo")
    private Integer overrideCapo;

    public UUID getId() { return id; }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }

    public UUID getSongId() { return songId; }
    public void setSongId(UUID songId) { this.songId = songId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public Integer getOverrideSemitones() { return overrideSemitones; }
    public void setOverrideSemitones(Integer overrideSemitones) { this.overrideSemitones = overrideSemitones; }

    public Integer getOverrideCapo() { return overrideCapo; }
    public void setOverrideCapo(Integer overrideCapo) { this.overrideCapo = overrideCapo; }
}
