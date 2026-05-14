package org.jones.licklibrary.domain.song;

import jakarta.persistence.*;
import org.jones.licklibrary.domain.song.parsing.ChordLyric;
import org.jones.licklibrary.domain.song.parsing.ChordLyricListConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "song")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String artist;

    @Column(name = "original_key", length = 8)
    private String originalKey;

    private Integer capo;
    private Integer tempo;

    @Convert(converter = ChordLyricListConverter.class)
    @Column(name = "chord_lines", columnDefinition = "TEXT", nullable = false)
    private List<ChordLyric> chordLines = new ArrayList<>();

    @Column(name = "num_columns")
    private int numColumns = 2;

    @Column(name = "raw_chord_sheet", columnDefinition = "TEXT")
    private String rawChordSheet;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getOriginalKey() { return originalKey; }
    public void setOriginalKey(String originalKey) { this.originalKey = originalKey; }

    public Integer getCapo() { return capo; }
    public void setCapo(Integer capo) { this.capo = capo; }

    public Integer getTempo() { return tempo; }
    public void setTempo(Integer tempo) { this.tempo = tempo; }

    public List<ChordLyric> getChordLines() { return chordLines; }
    public void setChordLines(List<ChordLyric> chordLines) { this.chordLines = chordLines; }

    public int getNumColumns() { return numColumns; }
    public void setNumColumns(int numColumns) { this.numColumns = numColumns; }

    public String getRawChordSheet() { return rawChordSheet; }
    public void setRawChordSheet(String rawChordSheet) { this.rawChordSheet = rawChordSheet; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
