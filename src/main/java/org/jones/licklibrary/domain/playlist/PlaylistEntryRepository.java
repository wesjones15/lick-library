package org.jones.licklibrary.domain.playlist;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PlaylistEntryRepository extends JpaRepository<PlaylistEntry, UUID> {
    List<PlaylistEntry> findByPlaylistOrderByPositionAsc(Playlist playlist);
    List<PlaylistEntry> findBySongId(UUID songId);
}
