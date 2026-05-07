package org.jones.licklibrary.repository;

import org.jones.licklibrary.model.Lick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LickRepository extends JpaRepository<Lick, UUID> {
    Optional<Lick> findByIntervalHash(String intervalHash);
}
