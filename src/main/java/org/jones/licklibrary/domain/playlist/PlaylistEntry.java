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
    private Integer keyOffset;

    @Column(name = "override_capo")
    private Integer capoOffset;

    @Column(name = "override_instrument", length = 20)
    private String instrument;

    public UUID getId() { return id; }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }

    public UUID getSongId() { return songId; }
    public void setSongId(UUID songId) { this.songId = songId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public Integer getKeyOffset() { return keyOffset; }
    public void setKeyOffset(Integer keyOffset) { this.keyOffset = keyOffset; }

    public Integer getCapoOffset() { return capoOffset; }
    public void setCapoOffset(Integer capoOffset) { this.capoOffset = capoOffset; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
}
