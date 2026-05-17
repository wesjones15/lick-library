package org.jones.licklibrary.domain.song;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "song_lick", uniqueConstraints = @UniqueConstraint(columnNames = {"song_id", "tab_order"}))
public class SongLick {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "song_id", nullable = false)
    private UUID songId;

    @Column(name = "tab_order", nullable = false)
    private int tabOrder;

    @Column(name = "lick_id")
    private UUID lickId;

    @Column(name = "raw_tab", columnDefinition = "TEXT", nullable = false)
    private String rawTab;

    public UUID getId() { return id; }
    public UUID getSongId() { return songId; }
    public void setSongId(UUID songId) { this.songId = songId; }
    public int getTabOrder() { return tabOrder; }
    public void setTabOrder(int tabOrder) { this.tabOrder = tabOrder; }
    public UUID getLickId() { return lickId; }
    public void setLickId(UUID lickId) { this.lickId = lickId; }
    public String getRawTab() { return rawTab; }
    public void setRawTab(String rawTab) { this.rawTab = rawTab; }
}
