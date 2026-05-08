package org.jones.licklibrary.service;

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
        System.out.println(LickUtils.toNoteString(notes));
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
        List<IntervalNote> intervals = LickUtils.toIntervals(notes);
        System.out.println(notes);
        System.out.println(LickUtils.toNoteString(notes));
        System.out.println(intervals);
    }

    @Test
    void parseTab_handlesTwoDigitFrets() {
        // TODO
    }

    @Test
    void parseTab_recordsTechniqueCharacters() {
        // TODO
    }

    @Test
    void parseTab_simultaneousNotesPreserved() {
        // TODO
    }
}
