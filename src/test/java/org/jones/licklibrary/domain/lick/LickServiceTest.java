package org.jones.licklibrary.domain.lick;

import org.jones.licklibrary.domain.lick.dto.LickResponse;
import org.jones.licklibrary.domain.lick.dto.UploadLickRequest;
import org.jones.licklibrary.domain.position.PositionCache;
import org.jones.licklibrary.domain.position.PositionCacheRepository;
import org.jones.licklibrary.domain.shared.Mode;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LickServiceTest {

    @Mock private LickRepository lickRepository;
    @Mock private PositionCacheRepository positionCacheRepository;

    private LickService lickService;

    static final String MAJOR_TAB =
            "e|---------|\n" +
            "B|---------|\n" +
            "G|---------|\n" +
            "D|---------|\n" +
            "A|-0-2-4---|\n" +
            "E|---------|";

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository);
    }

    @Test
    void uploadLick_storesNewLick() {
        when(lickRepository.findByIntervalHash(anyString())).thenReturn(Optional.empty());
        when(lickRepository.save(any(Lick.class))).thenAnswer(inv -> {
            Lick l = inv.getArgument(0);
            return new Lick() {{
                setIntervalHash(l.getIntervalHash());
                setIntervals(l.getIntervals());
                setRawTab(l.getRawTab());
                setMode(l.getMode());
            }};
        });

        LickResponse response = lickService.uploadLick(new UploadLickRequest(MAJOR_TAB, null, null));

        verify(lickRepository).save(any(Lick.class));
        assertEquals(Mode.IONIAN, response.mode());
        assertEquals("1 2 3", response.intervalDisplayString());
    }

    @Test
    void uploadLick_doesNotDuplicateExistingLick() {
        Lick existing = new Lick();
        existing.setIntervalHash("somehash");
        existing.setIntervals(List.of());
        existing.setRawTab(MAJOR_TAB);
        existing.setMode(Mode.IONIAN);

        when(lickRepository.findByIntervalHash(anyString())).thenReturn(Optional.of(existing));

        lickService.uploadLick(new UploadLickRequest(MAJOR_TAB, null, null));

        verify(lickRepository, never()).save(any());
    }

    @Test
    void uploadLick_respectsModeOverride() {
        when(lickRepository.findByIntervalHash(anyString())).thenReturn(Optional.empty());
        when(lickRepository.save(any(Lick.class))).thenAnswer(inv -> inv.getArgument(0));

        LickResponse response = lickService.uploadLick(new UploadLickRequest(MAJOR_TAB, Mode.DORIAN, null));

        assertEquals(Mode.DORIAN, response.mode());
    }

    @Test
    void parseTab_twoDigitFret_parsedAsSingleNote() {
        String tab =
            "e|--10--|\n" +
            "B|------|\n" +
            "G|------|\n" +
            "D|------|\n" +
            "A|------|\n" +
            "E|------|";
        var notes = lickService.parseTab(tab);
        assertEquals(1, notes.size());
        assertEquals(10, notes.get(0).fret());
    }

    @Test
    void parseTab_twoDigitFretWithTechnique() {
        String tab =
            "e|--10h12--|\n" +
            "B|---------|\n" +
            "G|---------|\n" +
            "D|---------|\n" +
            "A|---------|\n" +
            "E|---------|";
        var notes = lickService.parseTab(tab);
        assertEquals(2, notes.size());
        assertEquals(10, notes.get(0).fret());
        assertEquals("h", notes.get(0).technique());
        assertEquals(12, notes.get(1).fret());
        assertEquals("", notes.get(1).technique());
    }

    @Test
    void parseTab_singleDigitFretUnchanged() {
        String tab =
            "e|--5h7--|\n" +
            "B|-------|\n" +
            "G|-------|\n" +
            "D|-------|\n" +
            "A|-------|\n" +
            "E|-------|";
        var notes = lickService.parseTab(tab);
        assertEquals(2, notes.size());
        assertEquals(5, notes.get(0).fret());
        assertEquals("h", notes.get(0).technique());
        assertEquals(7, notes.get(1).fret());
    }

    @Disabled("position cache not yet wired into resolvePositions")
    @Test
    void resolvePositions_returnsCachedPositionsOnHit() {
        String hash = "somehash";
        String positionsJson = "[]";

        Lick lick = new Lick();
        lick.setIntervalHash(hash);
        lick.setIntervals(List.of());
        lick.setRawTab(MAJOR_TAB);
        lick.setMode(Mode.IONIAN);

        PositionCache cached = new PositionCache();
        cached.setIntervalHash(hash);
        cached.setKey("A");
        cached.setPositionsJson(positionsJson);

        when(lickRepository.findById(any())).thenReturn(Optional.of(lick));
        when(positionCacheRepository.findByIntervalHashAndKey(hash, "A"))
            .thenReturn(Optional.of(cached));

        lickService.getLick(UUID.randomUUID(), Note.A, "greedy", Guitar.STANDARD);

        verify(positionCacheRepository, never()).save(any());
    }

    @Disabled("position cache not yet wired into resolvePositions")
    @Test
    void resolvePositions_computesAndCachesPositionsOnMiss() {
        Lick lick = new Lick();
        lick.setIntervalHash("somehash");
        lick.setIntervals(List.of());
        lick.setRawTab(MAJOR_TAB);
        lick.setMode(Mode.IONIAN);

        when(lickRepository.findById(any())).thenReturn(Optional.of(lick));
        when(positionCacheRepository.findByIntervalHashAndKey(anyString(), anyString()))
            .thenReturn(Optional.empty());

        lickService.getLick(UUID.randomUUID(), Note.A, "greedy", Guitar.STANDARD);

        verify(positionCacheRepository).save(any(PositionCache.class));
    }
}
