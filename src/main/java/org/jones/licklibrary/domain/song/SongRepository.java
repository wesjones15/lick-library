package org.jones.licklibrary.domain.song;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SongRepository extends JpaRepository<Song, UUID> {
    List<Song> findByUserId(Long userId);
}
