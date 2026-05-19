package org.jones.licklibrary.domain.song;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SongBeatmapRepository extends JpaRepository<SongBeatmap, UUID> {
    Optional<SongBeatmap> findBySongId(UUID songId);
}
