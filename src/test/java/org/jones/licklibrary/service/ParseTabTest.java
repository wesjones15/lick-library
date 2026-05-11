package org.jones.licklibrary.service;

import org.jones.licklibrary.constants.Guitar;
import org.jones.licklibrary.constants.Note;
import org.jones.licklibrary.model.IntervalNote;
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
class ParseTabTest {

    @Mock private LickRepository lickRepository;
    @Mock private PositionCacheRepository positionCacheRepository;

    private LickService lickService;

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository);
    }

    @Test
    void parseTab_sample() {
        String inputTab =
                "E|------------------------------------|\n" +
                "B|------1-----------------------------|\n" +
                "G|--1/2-----2p0---0-------------------|\n" +
                "D|--------------3---3-0---------------|\n" +
                "A|------------------------0--3--5-----|\n" +
                "E|------------------------------------|";
        List<TabNote> notes = lickService.parseTab(inputTab);
        System.out.println(notes);
    }

    @Test
    void parseTab_simpleSample() {
        String inputTab =
                "E|----------------|\n" +
                "B|------1---------|\n" +
                "G|--1/2-----2p0---|\n" +
                "D|--------------3-|\n" +
                "A|----------------|\n" +
                "E|----------------|";
        List<TabNote> notes = lickService.parseTab(inputTab);
        Note root = Guitar.STANDARD.getNoteAt(notes.get(0).stringIndex(), notes.get(0).fret());
        List<IntervalNote> intervals = LickUtils.toIntervals(notes, root, Guitar.STANDARD);
        System.out.println(notes);
        System.out.println(intervals);
    }

    @Test
    void parseTab_handlesTwoDigitFrets() {
        String inputTab =
                "e|---------|\n" +
                "B|---------|\n" +
                "G|---------|\n" +
                "D|---------|\n" +
                "A|-12-4----|\n" +
                "E|---------|";
        List<TabNote> notes = lickService.parseTab(inputTab);
        assertEquals(2, notes.size());
        assertEquals(12, notes.get(0).fret());
    }

    @Test
    void parseTab_recordsTechniqueCharacters() {
        String inputTab =
                "e|---------|\n" +
                "B|---------|\n" +
                "G|---------|\n" +
                "D|---------|\n" +
                "A|-5h7-9/--|\n" +
                "E|---------|";
        List<TabNote> notes = lickService.parseTab(inputTab);
        assertEquals(3, notes.size());
        assertEquals(5,   notes.get(0).fret());
        assertEquals("h", notes.get(0).technique());
        assertEquals(7,   notes.get(1).fret());
        assertEquals("",  notes.get(1).technique());
        assertEquals(9,   notes.get(2).fret());
        assertEquals("/", notes.get(2).technique());
    }

    @Test
    void parseTab_recordsBackslashTechnique() {
        String inputTab =
                "e|---------|\n" +
                "B|---------|\n" +
                "G|---------|\n" +
                "D|---------|\n" +
                "A|-7\\5----|\n" +
                "E|---------|";
        List<TabNote> notes = lickService.parseTab(inputTab);
        assertEquals(2, notes.size());
        assertEquals(7,    notes.get(0).fret());
        assertEquals("\\", notes.get(0).technique());
        assertEquals(5,    notes.get(1).fret());
        assertEquals("",   notes.get(1).technique());
    }

    @Test
    void parseTab_simultaneousNotesPreserved() {
        String inputTab =
                "e|--0------|\n" +
                "B|--3------|\n" +
                "G|---------|\n" +
                "D|---------|\n" +
                "A|---------|\n" +
                "E|---------|";
        List<TabNote> notes = lickService.parseTab(inputTab);
        assertEquals(2, notes.size());
        assertEquals(notes.get(0).columnIndex(), notes.get(1).columnIndex());
    }
}
