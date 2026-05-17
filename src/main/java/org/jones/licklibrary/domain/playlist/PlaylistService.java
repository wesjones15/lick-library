package org.jones.licklibrary.domain.playlist;

import org.jones.licklibrary.core.exception.ResourceNotFoundException;
import org.jones.licklibrary.domain.playlist.dto.*;
import org.jones.licklibrary.domain.song.Song;
import org.jones.licklibrary.domain.song.SongRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistEntryRepository entryRepository;
    private final SongRepository songRepository;

    public PlaylistService(PlaylistRepository playlistRepository,
                           PlaylistEntryRepository entryRepository,
                           SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.entryRepository = entryRepository;
        this.songRepository = songRepository;
    }

    public PlaylistSummaryResponse createPlaylist(CreatePlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.name());
        playlistRepository.save(playlist);
        return toSummary(playlist);
    }

    public List<PlaylistSummaryResponse> getAllPlaylists() {
        return playlistRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    public PlaylistDetailResponse getPlaylist(UUID id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));
        return toDetail(playlist);
    }

    @Transactional
    public PlaylistSummaryResponse renamePlaylist(UUID id, String name) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));
        playlist.setName(name);
        playlistRepository.save(playlist);
        return toSummary(playlist);
    }

    @Transactional
    public void deletePlaylist(UUID id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));
        playlistRepository.delete(playlist);
    }

    @Transactional
    public PlaylistDetailResponse addEntry(UUID playlistId, AddEntryRequest request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + playlistId));

        boolean alreadyIn = playlist.getEntries().stream()
                .anyMatch(e -> e.getSongId().equals(request.songId()));
        if (alreadyIn) throw new ResponseStatusException(HttpStatus.CONFLICT, "Song already in playlist");

        int nextPosition = playlist.getEntries().stream()
                .mapToInt(PlaylistEntry::getPosition)
                .max()
                .orElse(-1) + 1;

        PlaylistEntry entry = new PlaylistEntry();
        entry.setPlaylist(playlist);
        entry.setSongId(request.songId());
        entry.setPosition(nextPosition);
        entry.setKeyOffset(request.keyOffset());
        entry.setCapoOffset(request.capoOffset());
        entryRepository.save(entry);

        return toDetail(playlistRepository.findById(playlistId).orElseThrow());
    }

    @Transactional
    public PlaylistDetailResponse updateEntry(UUID playlistId, UUID entryId, UpdateEntryRequest request) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + playlistId));
        PlaylistEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found: " + entryId));

        if (request.keyOffset() != null || request.capoOffset() != null) {
            if (request.keyOffset() != null) entry.setKeyOffset(request.keyOffset());
            if (request.capoOffset() != null) entry.setCapoOffset(request.capoOffset());
        }

        if (request.position() != null) {
            reorderEntry(playlist, entry, request.position());
        }

        entryRepository.save(entry);
        return toDetail(playlistRepository.findById(playlistId).orElseThrow());
    }

    @Transactional
    public PlaylistDetailResponse clearEntryOverrides(UUID playlistId, UUID entryId) {
        playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + playlistId));
        PlaylistEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found: " + entryId));
        entry.setKeyOffset(null);
        entry.setCapoOffset(null);
        entryRepository.save(entry);
        return toDetail(playlistRepository.findById(playlistId).orElseThrow());
    }

    @Transactional
    public PlaylistDetailResponse removeEntry(UUID playlistId, UUID entryId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + playlistId));
        PlaylistEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found: " + entryId));

        int removedPosition = entry.getPosition();
        entryRepository.delete(entry);

        // Compact positions after the removed entry
        List<PlaylistEntry> remaining = entryRepository.findByPlaylistOrderByPositionAsc(playlist);
        for (PlaylistEntry e : remaining) {
            if (e.getPosition() > removedPosition) {
                e.setPosition(e.getPosition() - 1);
                entryRepository.save(e);
            }
        }

        return toDetail(playlistRepository.findById(playlistId).orElseThrow());
    }

    public List<PlaylistContainingEntry> getContainingEntries(UUID songId) {
        return entryRepository.findBySongId(songId).stream()
                .map(e -> new PlaylistContainingEntry(e.getPlaylist().getId(), e.getId()))
                .toList();
    }

    private void reorderEntry(Playlist playlist, PlaylistEntry entry, int newPosition) {
        List<PlaylistEntry> entries = entryRepository.findByPlaylistOrderByPositionAsc(playlist);
        int oldPosition = entry.getPosition();
        int clampedNew = Math.max(0, Math.min(newPosition, entries.size() - 1));
        if (oldPosition == clampedNew) return;

        if (newPosition < oldPosition) {
            for (PlaylistEntry e : entries) {
                if (e.getPosition() >= clampedNew && e.getPosition() < oldPosition && !e.getId().equals(entry.getId())) {
                    e.setPosition(e.getPosition() + 1);
                    entryRepository.save(e);
                }
            }
        } else {
            for (PlaylistEntry e : entries) {
                if (e.getPosition() > oldPosition && e.getPosition() <= clampedNew && !e.getId().equals(entry.getId())) {
                    e.setPosition(e.getPosition() - 1);
                    entryRepository.save(e);
                }
            }
        }
        entry.setPosition(clampedNew);
    }

    private PlaylistSummaryResponse toSummary(Playlist playlist) {
        return new PlaylistSummaryResponse(playlist.getId(), playlist.getName(), playlist.getEntries().size());
    }

    private PlaylistDetailResponse toDetail(Playlist playlist) {
        List<PlaylistEntry> entries = entryRepository.findByPlaylistOrderByPositionAsc(playlist);

        Set<UUID> songIds = new HashSet<>();
        for (PlaylistEntry e : entries) songIds.add(e.getSongId());
        Map<UUID, Song> songs = new HashMap<>();
        songRepository.findAllById(songIds).forEach(s -> songs.put(s.getId(), s));

        List<PlaylistEntryResponse> entryResponses = entries.stream()
                .map(e -> {
                    Song song = songs.get(e.getSongId());
                    String title = song != null ? song.getTitle() : "(deleted)";
                    String artist = song != null ? song.getArtist() : null;
                    return new PlaylistEntryResponse(
                            e.getId(), e.getSongId(), title, artist,
                            e.getPosition(),
                            e.getKeyOffset() != null ? e.getKeyOffset() : 0,
                            e.getCapoOffset() != null ? e.getCapoOffset() : 0,
                            song != null ? song.getOriginalKey() : null,
                            song != null && song.getCapo() != null ? song.getCapo() : 0,
                            song != null ? song.getTempo() : null);
                })
                .toList();

        return new PlaylistDetailResponse(playlist.getId(), playlist.getName(), entryResponses);
    }
}
