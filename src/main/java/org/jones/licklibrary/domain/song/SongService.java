package org.jones.licklibrary.domain.song;

import org.jones.licklibrary.core.exception.ResourceNotFoundException;
import org.jones.licklibrary.domain.song.dto.SongDetailResponse;
import org.jones.licklibrary.domain.song.dto.SongSummaryResponse;
import org.jones.licklibrary.domain.song.dto.UpdateSongRequest;
import org.jones.licklibrary.domain.song.dto.UploadSongRequest;
import org.jones.licklibrary.domain.song.parsing.ChordSheetLine;
import org.jones.licklibrary.domain.song.parsing.ChordSheetParser;
import org.jones.licklibrary.domain.song.parsing.ChordTransposer;
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
        song.setRawChordSheet(request.rawChordSheet());
        return toSummary(songRepository.save(song));
    }

    public SongDetailResponse reparseSong(UUID id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
        if (song.getRawChordSheet() == null) throw new IllegalStateException("No raw chord sheet stored for this song");
        ChordSheetParser.ParseResult result = chordSheetParser.parse(song.getRawChordSheet());
        song.setChordLines(result.chordLines());
        song.setNumColumns(result.numColumns());
        return toDetail(songRepository.save(song), result.chordLines());
    }

    public List<SongSummaryResponse> getAllSongs() {
        return songRepository.findAll().stream().map(this::toSummary).toList();
    }

    public SongDetailResponse getSong(UUID id, int semitones) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
        List<ChordSheetLine> lines = semitones == 0
                ? song.getChordLines()
                : chordTransposer.transpose(song.getChordLines(), semitones);
        return toDetail(song, lines);
    }

    public void deleteSong(UUID id) {
        if (!songRepository.existsById(id)) throw new ResourceNotFoundException("Song not found: " + id);
        songRepository.deleteById(id);
    }

    private SongSummaryResponse toSummary(Song song) {
        return new SongSummaryResponse(song.getId(), song.getTitle(), song.getArtist(), song.getOriginalKey(),
                song.getRawChordSheet() != null, song.getTempo());
    }

    public SongDetailResponse updateSong(UUID id, UpdateSongRequest req) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
        if (req.rawChordSheet() != null) {
            song.setRawChordSheet(req.rawChordSheet());
            ChordSheetParser.ParseResult result = chordSheetParser.parse(req.rawChordSheet());
            song.setChordLines(result.chordLines());
            song.setNumColumns(result.numColumns());
        } else {
            if (req.title() != null && !req.title().isBlank()) song.setTitle(req.title().trim());
            song.setArtist(req.artist() != null && !req.artist().isBlank() ? req.artist().trim() : null);
            song.setOriginalKey(req.originalKey() != null && !req.originalKey().isBlank() ? req.originalKey() : null);
            song.setTempo(req.tempo());
        }
        return toDetail(songRepository.save(song), song.getChordLines());
    }

    private SongDetailResponse toDetail(Song song, List<ChordSheetLine> chordLines) {
        return new SongDetailResponse(
                song.getId(), song.getTitle(), song.getArtist(), song.getOriginalKey(),
                song.getCapo(), song.getTempo(), chordLines, song.getNumColumns(),
                song.getRawChordSheet() != null, song.getRawChordSheet()
        );
    }
}
