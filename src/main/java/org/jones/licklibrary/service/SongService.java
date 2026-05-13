package org.jones.licklibrary.service;

import org.jones.licklibrary.controller.SongNotFoundException;
import org.jones.licklibrary.model.*;
import org.jones.licklibrary.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SongService {

    private final SongRepository songRepository;
    private final ChordSheetParser chordSheetParser;
    private final ChordTransposer chordTransposer;

    public SongService(SongRepository songRepository,
                       ChordSheetParser chordSheetParser,
                       ChordTransposer chordTransposer) {
        this.songRepository = songRepository;
        this.chordSheetParser = chordSheetParser;
        this.chordTransposer = chordTransposer;
    }

    public SongSummaryResponse uploadSong(UploadSongRequest request) {
        ChordSheetParser.ParseResult result = chordSheetParser.parse(request.rawChordSheet());
        Song song = new Song();
        song.setTitle(request.title());
        song.setArtist(request.artist());
        song.setOriginalKey(request.originalKey());
        song.setCapo(request.capo());
        song.setTempo(request.tempo());
        song.setChordLines(result.chordLines());
        song.setNumColumns(result.numColumns());
        return toSummary(songRepository.save(song));
    }

    public List<SongSummaryResponse> getAllSongs() {
        return songRepository.findAll().stream().map(this::toSummary).toList();
    }

    public SongDetailResponse getSong(UUID id, int semitones) {
        Song song = songRepository.findById(id).orElseThrow(() -> new SongNotFoundException(id));
        List<ChordLyric> lines = semitones == 0
                ? song.getChordLines()
                : chordTransposer.transpose(song.getChordLines(), semitones);
        return toDetail(song, lines);
    }

    public void deleteSong(UUID id) {
        if (!songRepository.existsById(id)) throw new SongNotFoundException(id);
        songRepository.deleteById(id);
    }

    private SongSummaryResponse toSummary(Song song) {
        return new SongSummaryResponse(song.getId(), song.getTitle(), song.getArtist(), song.getOriginalKey());
    }

    private SongDetailResponse toDetail(Song song, List<ChordLyric> chordLines) {
        return new SongDetailResponse(
                song.getId(), song.getTitle(), song.getArtist(), song.getOriginalKey(),
                song.getCapo(), song.getTempo(), chordLines, song.getNumColumns()
        );
    }
}
