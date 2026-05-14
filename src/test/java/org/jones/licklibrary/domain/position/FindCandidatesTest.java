package org.jones.licklibrary.domain.position;

import org.jones.licklibrary.domain.position.builder.GreedyPositionBuilder;
import org.jones.licklibrary.domain.position.builder.PositionBuilder;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.TabNote;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FindCandidatesTest {

    private PositionBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new GreedyPositionBuilder();
    }

    @Test
    void findCandidates_searchesWithinTwoStringsWhenNoTechnique() {
        TabNote current = new TabNote(2, 5, 0, null);
        List<TabNote> candidates = builder.findCandidates(current, Note.A, null, Guitar.STANDARD);

        assertFalse(candidates.isEmpty());
        candidates.forEach(c ->
            assertTrue(Math.abs(c.stringIndex() - 2) <= 2,
                "candidate outside ±2 string range: string " + c.stringIndex()));
        long distinctStrings = candidates.stream().mapToInt(TabNote::stringIndex).distinct().count();
        assertTrue(distinctStrings > 1, "expected candidates across multiple strings");
    }

    @Test
    void findCandidates_restrictesToSameStringWhenTechniquePresent() {
        TabNote current = new TabNote(2, 5, 0, null);
        List<TabNote> candidates = builder.findCandidates(current, Note.A, "h", Guitar.STANDARD);

        assertFalse(candidates.isEmpty());
        candidates.forEach(c ->
            assertEquals(2, c.stringIndex(), "technique should restrict to same string"));
    }

    @Test
    void findCandidates_sortedByProximityClosestFirst() {
        TabNote current = new TabNote(2, 7, 0, null);
        List<TabNote> candidates = builder.findCandidates(current, Note.A, null, Guitar.STANDARD);

        TabNote closest = candidates.get(0);
        assertEquals(2, closest.stringIndex());
        assertEquals(7, closest.fret());
    }
}
