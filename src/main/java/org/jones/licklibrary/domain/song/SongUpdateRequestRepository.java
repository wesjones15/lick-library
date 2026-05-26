package org.jones.licklibrary.domain.song;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SongUpdateRequestRepository extends JpaRepository<SongUpdateRequest, UUID> {
    List<SongUpdateRequest> findByStatus(String status);
}
