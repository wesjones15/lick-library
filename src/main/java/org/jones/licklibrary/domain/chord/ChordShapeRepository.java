package org.jones.licklibrary.domain.chord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChordShapeRepository extends JpaRepository<ChordShape, UUID> {
    List<ChordShape> findByChordQuality_SuffixAndInstrument(String suffix, String instrument);

    @Query("SELECT DISTINCT s.chordQuality.suffix FROM ChordShape s WHERE s.instrument = :instrument")
    List<String> findDistinctQualitiesByInstrument(@Param("instrument") String instrument);
}
