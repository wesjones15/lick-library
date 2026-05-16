package org.jones.licklibrary.domain.playlist;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {}
