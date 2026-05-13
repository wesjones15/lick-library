package org.jones.licklibrary.repository;

import org.jones.licklibrary.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SongRepository extends JpaRepository<Song, UUID> {}
