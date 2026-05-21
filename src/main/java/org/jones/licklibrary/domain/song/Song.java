package org.jones.licklibrary.domain.song;

import jakarta.persistence.*;
import org.jones.licklibrary.domain.song.parsing.ChordSheetLine;
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

    @Column(name = "original_key", length = 20)
    private String originalKey;

    @Column(name = "mode", length = 20)
    private String mode;

    @Column(name = "instrument", length = 20)
    private String instrument;

    private Integer capo;
    private Integer tempo;

    @Convert(converter = ChordLyricListConverter.class)
    @Column(name = "chord_lines", columnDefinition = "TEXT", nullable = false)
    private List<ChordSheetLine> chordLines = new ArrayList<>();

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

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public Integer getCapo() { return capo; }
    public void setCapo(Integer capo) { this.capo = capo; }

    public Integer getTempo() { return tempo; }
    public void setTempo(Integer tempo) { this.tempo = tempo; }

    public List<ChordSheetLine> getChordLines() { return chordLines; }
    public void setChordLines(List<ChordSheetLine> chordLines) { this.chordLines = chordLines; }

    public int getNumColumns() { return numColumns; }
    public void setNumColumns(int numColumns) { this.numColumns = numColumns; }

    public String getRawChordSheet() { return rawChordSheet; }
    public void setRawChordSheet(String rawChordSheet) { this.rawChordSheet = rawChordSheet; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
