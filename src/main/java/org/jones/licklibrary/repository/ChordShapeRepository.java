package org.jones.licklibrary.repository;

import org.jones.licklibrary.model.ChordShape;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChordShapeRepository extends JpaRepository<ChordShape, UUID> {
    List<ChordShape> findByChordQuality_SuffixAndInstrument(String suffix, String instrument);
}
