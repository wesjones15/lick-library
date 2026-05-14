package org.jones.licklibrary.domain.chord;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChordQualityRepository extends JpaRepository<ChordQuality, UUID> {
    Optional<ChordQuality> findBySuffix(String suffix);
}
