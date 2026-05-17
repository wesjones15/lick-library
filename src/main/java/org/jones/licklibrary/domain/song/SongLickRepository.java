package org.jones.licklibrary.domain.song;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface SongLickRepository extends JpaRepository<SongLick, UUID> {
    List<SongLick> findAllBySongId(UUID songId);

    @Modifying
    @Transactional
    void deleteBySongId(UUID songId);
}
