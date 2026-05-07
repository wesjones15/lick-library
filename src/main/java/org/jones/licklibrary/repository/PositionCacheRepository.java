package org.jones.licklibrary.repository;

import org.jones.licklibrary.model.PositionCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PositionCacheRepository extends JpaRepository<PositionCache, UUID> {
    Optional<PositionCache> findByIntervalHashAndKey(String intervalHash, String key);
}
