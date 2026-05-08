package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.TabNote;
import org.jones.licklibrary.repository.LickRepository;
import org.jones.licklibrary.repository.PositionCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FindCandidatesTest {

    @Mock private LickRepository lickRepository;
    @Mock private PositionCacheRepository positionCacheRepository;

    private LickService lickService;

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository);
    }

    @Test
    void findCandidates_searchesAllStringsWhenNoTechnique() {
        // D string (2), fret 5. With no technique, all 6 strings are searched.
        TabNote current = new TabNote(2, 5, 0, null);
        List<TabNote> candidates = lickService.findCandidates(current, Note.A, null);

        assertFalse(candidates.isEmpty());
        candidates.forEach(c ->
            assertTrue(c.stringIndex() >= 0 && c.stringIndex() <= 5,
                "candidate outside valid string range: " + c.stringIndex()));
        // Candidates should span more than just adjacent strings
        long distinctStrings = candidates.stream().mapToInt(TabNote::stringIndex).distinct().count();
        assertTrue(distinctStrings > 2, "expected candidates across multiple strings");
    }

    @Test
    void findCandidates_restrictesToSameStringWhenTechniquePresent() {
        TabNote current = new TabNote(2, 5, 0, null);
        List<TabNote> candidates = lickService.findCandidates(current, Note.A, "h");

        assertFalse(candidates.isEmpty());
        candidates.forEach(c ->
            assertEquals(2, c.stringIndex(), "technique should restrict to same string"));
    }

    @Test
    void findCandidates_sortedByProximityClosestFirst() {
        // D string (2), fret 7 = A. Searching for A — same position scores 0 and should be first.
        TabNote current = new TabNote(2, 7, 0, null);
        List<TabNote> candidates = lickService.findCandidates(current, Note.A, null);

        TabNote closest = candidates.get(0);
        assertEquals(2, closest.stringIndex());
        assertEquals(7, closest.fret());
    }
}
