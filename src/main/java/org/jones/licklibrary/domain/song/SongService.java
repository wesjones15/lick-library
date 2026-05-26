package org.jones.licklibrary.domain.song;

import org.jones.licklibrary.core.exception.ResourceNotFoundException;
import org.jones.licklibrary.domain.lick.LickService;
import org.jones.licklibrary.domain.song.dto.SongDetailResponse;
import org.jones.licklibrary.domain.song.dto.SongLickInfo;
import org.jones.licklibrary.domain.song.dto.SongSummaryResponse;
import org.jones.licklibrary.domain.song.dto.UpdateSongRequest;
import org.jones.licklibrary.domain.song.dto.UploadSongRequest;
import org.jones.licklibrary.domain.song.parsing.ChordSheetLine;
import org.jones.licklibrary.domain.song.parsing.ChordSheetParser;
import org.jones.licklibrary.domain.song.parsing.ChordTransposer;
import org.jones.licklibrary.domain.song.parsing.GuitarTabLine;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.NoteParser;
import org.jones.licklibrary.domain.user.UserRole;
import org.jones.licklibrary.domain.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class SongService {

    private final SongRepository songRepository;
    private final SongLickRepository songLickRepository;
    private final ChordSheetParser chordSheetParser;
    private final ChordTransposer chordTransposer;
    private final LickService lickService;
    private final UserService userService;

    public SongService(SongRepository songRepository,
                       SongLickRepository songLickRepository,
                       ChordSheetParser chordSheetParser,
                       ChordTransposer chordTransposer,
                       LickService lickService,
                       UserService userService) {
        this.songRepository = songRepository;
        this.songLickRepository = songLickRepository;
        this.chordSheetParser = chordSheetParser;
        this.chordTransposer = chordTransposer;
        this.lickService = lickService;
        this.userService = userService;
    }

    @Transactional
    public SongSummaryResponse uploadSong(UploadSongRequest request, Long userId) {
        ChordSheetParser.ParseResult result = chordSheetParser.parse(request.rawChordSheet());
        Song song = new Song();
        song.setTitle(request.title());
        song.setArtist(request.artist());
        song.setOriginalKey(request.originalKey());
        song.setMode(request.mode());
        song.setInstrument(request.instrument() != null && !request.instrument().isBlank() ? request.instrument() : null);
        song.setCapo(request.capo());
        song.setTempo(request.tempo());
        song.setChordLines(result.chordLines());
        song.setNumColumns(result.numColumns());
        song.setRawChordSheet(request.rawChordSheet());
        song.setUserId(userId);
        song = songRepository.save(song);
        extractAndStoreSongLicks(song);
        return toSummary(song, userId, UserRole.USER);
    }

    @Transactional
    public SongDetailResponse reparseSong(UUID id, Long currentUserId, UserRole role) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
        checkOwner(song, currentUserId, role);
        if (song.getRawChordSheet() == null) throw new IllegalStateException("No raw chord sheet stored for this song");
        ChordSheetParser.ParseResult result = chordSheetParser.parse(song.getRawChordSheet());
        song.setChordLines(result.chordLines());
        song.setNumColumns(result.numColumns());
        song = songRepository.save(song);
        extractAndStoreSongLicks(song);
        return toDetail(song, song.getChordLines(), currentUserId, role);
    }

    public List<SongSummaryResponse> getAllSongs(Long currentUserId, UserRole role) {
        return songRepository.findAll().stream().map(s -> toSummary(s, currentUserId, role)).toList();
    }

    public List<SongSummaryResponse> getMySongs(Long currentUserId) {
        return songRepository.findByUserId(currentUserId).stream().map(s -> toSummary(s, currentUserId, UserRole.USER)).toList();
    }

    public SongDetailResponse getSong(UUID id, int semitones, Long currentUserId, UserRole role) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
        List<ChordSheetLine> lines = semitones == 0
                ? song.getChordLines()
                : chordTransposer.transpose(song.getChordLines(), semitones);
        return toDetail(song, lines, currentUserId, role);
    }

    @Transactional
    public void deleteSong(UUID id, Long currentUserId, UserRole role) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
        checkOwner(song, currentUserId, role);
        List<SongLick> songLicks = songLickRepository.findAllBySongId(id);
        List<UUID> lickIds = songLicks.stream().map(SongLick::getLickId).filter(lid -> lid != null).toList();
        songLickRepository.deleteBySongId(id);
        lickService.deleteAutoImportedLicks(lickIds);
        songRepository.deleteById(id);
    }

    private SongSummaryResponse toSummary(Song song, Long currentUserId, UserRole role) {
        boolean owned = role == UserRole.ADMIN || Objects.equals(song.getUserId(), currentUserId);
        return new SongSummaryResponse(song.getId(), song.getTitle(), song.getArtist(), song.getOriginalKey(),
                song.getMode(), song.getRawChordSheet() != null, song.getTempo(),
                userService.getUsernameById(song.getUserId()),
                owned);
    }

    @Transactional
    public SongDetailResponse updateSong(UUID id, UpdateSongRequest req, Long currentUserId, UserRole role) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
        checkOwner(song, currentUserId, role);
        if (req.rawChordSheet() != null) {
            song.setRawChordSheet(req.rawChordSheet());
            ChordSheetParser.ParseResult result = chordSheetParser.parse(req.rawChordSheet());
            song.setChordLines(result.chordLines());
            song.setNumColumns(result.numColumns());
            song = songRepository.save(song);
            extractAndStoreSongLicks(song);
        } else {
            if (req.title() != null && !req.title().isBlank()) song.setTitle(req.title().trim());
            song.setArtist(req.artist() != null && !req.artist().isBlank() ? req.artist().trim() : null);
            song.setOriginalKey(req.originalKey() != null && !req.originalKey().isBlank() ? req.originalKey() : null);
            song.setMode(req.mode() != null && !req.mode().isBlank() ? req.mode() : null);
            song.setInstrument(req.instrument() != null && !req.instrument().isBlank() ? req.instrument() : null);
            song.setTempo(req.tempo());
            song.setCapo(req.capo());
            song = songRepository.save(song);
        }
        return toDetail(song, song.getChordLines(), currentUserId, role);
    }

    public void checkOwner(Song song, Long currentUserId, UserRole role) {
        if (role == UserRole.ADMIN) return;
        if (!Objects.equals(song.getUserId(), currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your song");
        }
    }

    private SongDetailResponse toDetail(Song song, List<ChordSheetLine> chordLines, Long currentUserId, UserRole role) {
        boolean owned = role == UserRole.ADMIN || Objects.equals(song.getUserId(), currentUserId);
        List<SongLick> songLicks = songLickRepository.findAllBySongId(song.getId());
        Map<Integer, SongLickInfo> songLickMap = new LinkedHashMap<>();
        for (SongLick sl : songLicks) {
            songLickMap.put(sl.getTabOrder(), new SongLickInfo(sl.getLickId(), sl.getRawTab()));
        }
        return new SongDetailResponse(
                song.getId(), song.getTitle(), song.getArtist(), song.getOriginalKey(),
                song.getMode(), song.getInstrument(), song.getCapo(), song.getTempo(),
                chordLines, song.getNumColumns(),
                song.getRawChordSheet() != null, song.getRawChordSheet(),
                songLickMap,
                owned
        );
    }

    private void extractAndStoreSongLicks(Song song) {
        List<SongLick> existing = songLickRepository.findAllBySongId(song.getId());
        List<UUID> oldLickIds = existing.stream().map(SongLick::getLickId).filter(id -> id != null).toList();
        songLickRepository.deleteBySongId(song.getId());
        lickService.deleteAutoImportedLicks(oldLickIds);

        Note songKey = song.getOriginalKey() != null ? NoteParser.parse(song.getOriginalKey()) : null;
        int tabOrder = 0;
        for (ChordSheetLine line : song.getChordLines()) {
            if (line instanceof GuitarTabLine tab) {
                String rawTab = String.join("\n", tab.tabLines());
                UUID lickId = lickService.uploadSongLick(rawTab, chordSheetParser.detectInstrument(tab.tabLines()), songKey, song.getUserId());
                if (lickId != null) {
                    SongLick sl = new SongLick();
                    sl.setSongId(song.getId());
                    sl.setTabOrder(tabOrder);
                    sl.setLickId(lickId);
                    sl.setRawTab(rawTab);
                    songLickRepository.save(sl);
                }
                tabOrder++;
            }
        }
    }
}
