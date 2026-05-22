package org.jones.licklibrary.domain.lick;

import org.jones.licklibrary.core.exception.ResourceNotFoundException;
import org.jones.licklibrary.domain.lick.dto.LickResponse;
import org.jones.licklibrary.domain.lick.dto.UploadLickRequest;
import org.jones.licklibrary.domain.position.PositionCache;
import org.jones.licklibrary.domain.position.PositionCacheRepository;
import org.jones.licklibrary.domain.shared.Interval;
import org.jones.licklibrary.domain.shared.IntervalNote;
import org.jones.licklibrary.domain.shared.Mode;
import org.jones.licklibrary.domain.shared.Note;
import org.jones.licklibrary.domain.shared.Position;
import org.jones.licklibrary.domain.shared.instrument.Bass;
import org.jones.licklibrary.domain.shared.instrument.Guitar;
import org.jones.licklibrary.domain.user.UserService;
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
    @Mock private UserService userService;

    private LickService lickService;

    // Three-note major scale fragment (intervals 1-2-3) used in resolvePositions tests
    static final List<IntervalNote> THREE_NOTE_MAJOR = List.of(
            new IntervalNote(Interval.ONE,   "", 0),
            new IntervalNote(Interval.TWO,   "", 1),
            new IntervalNote(Interval.THREE, "", 2)
    );

    // Five-note minor pentatonic (1-b3-4-5-b7) used in getAllLicks filter tests
    static final List<IntervalNote> MINOR_PENTA = List.of(
            new IntervalNote(Interval.ONE,         "", 0),
            new IntervalNote(Interval.FLAT_THREE,  "", 1),
            new IntervalNote(Interval.FOUR,        "", 2),
            new IntervalNote(Interval.FIVE,        "", 3),
            new IntervalNote(Interval.FLAT_SEVEN,  "", 4)
    );

    static final String MAJOR_TAB =
            "e|---------|\n" +
            "B|---------|\n" +
            "G|---------|\n" +
            "D|---------|\n" +
            "A|-0-2-4---|\n" +
            "E|---------|";

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository, userService);
        lenient().when(userService.getUsernameById(any())).thenReturn("testuser");
    }

    @Test
    void uploadLick_storesNewLick() {
        when(lickRepository.findByIntervalHashAndInstrumentAndAutoImportedFalse(anyString(), anyString())).thenReturn(Optional.empty());
        when(lickRepository.save(any(Lick.class))).thenAnswer(inv -> {
            Lick l = inv.getArgument(0);
            return new Lick() {{
                setIntervalHash(l.getIntervalHash());
                setIntervals(l.getIntervals());
                setRawTab(l.getRawTab());
                setMode(l.getMode());
            }};
        });

        LickResponse response = lickService.uploadLick(new UploadLickRequest(MAJOR_TAB, null, null, null), Guitar.STANDARD, "GUITAR", 1L);

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

        when(lickRepository.findByIntervalHashAndInstrumentAndAutoImportedFalse(anyString(), anyString())).thenReturn(Optional.of(existing));

        lickService.uploadLick(new UploadLickRequest(MAJOR_TAB, null, null, null), Guitar.STANDARD, "GUITAR", 1L);

        verify(lickRepository, never()).save(any());
    }

    @Test
    void uploadLick_respectsModeOverride() {
        when(lickRepository.findByIntervalHashAndInstrumentAndAutoImportedFalse(anyString(), anyString())).thenReturn(Optional.empty());
        when(lickRepository.save(any(Lick.class))).thenAnswer(inv -> inv.getArgument(0));

        LickResponse response = lickService.uploadLick(new UploadLickRequest(MAJOR_TAB, Mode.DORIAN, null, null), Guitar.STANDARD, "GUITAR", 1L);

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

    // -----------------------------------------------------------------------
    // deleteLick
    // -----------------------------------------------------------------------

    @Test
    void deleteLick_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(lickRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> lickService.deleteLick(id, 1L));
    }

    @Test
    void deleteLick_returnsFalseForAutoImportedLick() {
        UUID id = UUID.randomUUID();
        Lick lick = new Lick();
        lick.setIntervals(List.of());
        lick.setAutoImported(true);
        when(lickRepository.findById(id)).thenReturn(Optional.of(lick));

        boolean result = lickService.deleteLick(id, 1L);

        assertFalse(result);
        verify(lickRepository, never()).deleteById(any());
    }

    @Test
    void deleteLick_returnsTrueAndDeletesNonAutoImportedLick() {
        UUID id = UUID.randomUUID();
        Lick lick = new Lick();
        lick.setIntervals(List.of());
        lick.setAutoImported(false);
        lick.setUserId(1L);
        when(lickRepository.findById(id)).thenReturn(Optional.of(lick));

        boolean result = lickService.deleteLick(id, 1L);

        assertTrue(result);
        verify(lickRepository).deleteById(id);
    }

    // -----------------------------------------------------------------------
    // getLick — not-found guard
    // -----------------------------------------------------------------------

    @Test
    void getLick_throwsResourceNotFoundForUnknownId() {
        UUID id = UUID.randomUUID();
        when(lickRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> lickService.getLick(id, Note.A, "greedy", Guitar.STANDARD, 1L));
    }

    // -----------------------------------------------------------------------
    // resolvePositions — algorithm routing and spanLimit
    // -----------------------------------------------------------------------

    private Lick lickWithIntervals(List<IntervalNote> intervals, Integer tabSpan) {
        Lick lick = new Lick();
        lick.setIntervals(intervals);
        lick.setTabSpan(tabSpan);
        lick.setMode(Mode.IONIAN);
        return lick;
    }

    @Test
    void resolvePositions_nullAlgo_usesGreedy_returnsNonEmpty() {
        List<Position> positions = lickService.resolvePositions(
                lickWithIntervals(THREE_NOTE_MAJOR, 4), Note.G, null, Guitar.STANDARD);
        assertFalse(positions.isEmpty(), "Greedy (null algo) should find positions");
    }

    @Test
    void resolvePositions_unknownAlgo_defaultsToGreedy_returnsNonEmpty() {
        List<Position> positions = lickService.resolvePositions(
                lickWithIntervals(THREE_NOTE_MAJOR, 4), Note.G, "unknown", Guitar.STANDARD);
        assertFalse(positions.isEmpty(), "Unknown algo should fall back to greedy");
    }

    @Test
    void resolvePositions_dfsAlgo_returnsNonEmpty() {
        List<Position> positions = lickService.resolvePositions(
                lickWithIntervals(THREE_NOTE_MAJOR, 4), Note.G, "dfs", Guitar.STANDARD);
        assertFalse(positions.isEmpty(), "DFS algo should find positions");
    }

    @Test
    void resolvePositions_chordAlgo_returnsNonEmpty() {
        List<Position> positions = lickService.resolvePositions(
                lickWithIntervals(THREE_NOTE_MAJOR, 4), Note.G, "chord", Guitar.STANDARD);
        assertFalse(positions.isEmpty(), "Chord (loser-bracket) algo should find positions");
    }

    @Test
    void resolvePositions_nullTabSpan_defaultsToSpanLimitFour() {
        // tabSpan=null → spanLimit = max(4, 4) = 4; a 3-note major fragment fits easily
        List<Position> positions = lickService.resolvePositions(
                lickWithIntervals(THREE_NOTE_MAJOR, null), Note.G, "greedy", Guitar.STANDARD);
        assertFalse(positions.isEmpty(), "Null tabSpan should default to spanLimit 4 and still find positions");
    }

    @Test
    void resolvePositions_largeTabSpan_allowsWiderFingering() {
        List<Position> positions = lickService.resolvePositions(
                lickWithIntervals(THREE_NOTE_MAJOR, 10), Note.G, "greedy", Guitar.STANDARD);
        assertFalse(positions.isEmpty(), "Large tabSpan should still find positions");
    }

    // -----------------------------------------------------------------------
    // getAllLicks — filtering
    // -----------------------------------------------------------------------

    private Lick makeLick(List<IntervalNote> intervals, String instrument, Mode mode) {
        Lick lick = new Lick();
        lick.setIntervals(intervals);
        lick.setInstrument(instrument);
        lick.setMode(mode);
        lick.setRawTab("");
        lick.setIntervalHash(instrument + mode);
        return lick;
    }

    @Test
    void getAllLicks_excludesSongLicksWhenFlagFalse() {
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of());
        lickService.getAllLicks(false, null, null, null, null, null, false, 1L);
        verify(lickRepository).findAllByAutoImportedFalse();
        verify(lickRepository, never()).findAll();
    }

    @Test
    void getAllLicks_includesSongLicksWhenFlagTrue() {
        when(lickRepository.findAll()).thenReturn(List.of());
        lickService.getAllLicks(true, null, null, null, null, null, false, 1L);
        verify(lickRepository).findAll();
        verify(lickRepository, never()).findAllByAutoImportedFalse();
    }

    @Test
    void getAllLicks_instrumentFilter_excludesNonMatchingInstrument() {
        Lick guitarLick = makeLick(THREE_NOTE_MAJOR, "GUITAR", Mode.IONIAN);
        Lick bassLick   = makeLick(THREE_NOTE_MAJOR, "BASS",   Mode.IONIAN);
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(guitarLick, bassLick));

        List<LickResponse> result = lickService.getAllLicks(false, "BASS", null, null, null, null, false, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void getAllLicks_instrumentFilter_caseInsensitive() {
        Lick lick = makeLick(THREE_NOTE_MAJOR, "GUITAR", Mode.IONIAN);
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(lick));

        List<LickResponse> result = lickService.getAllLicks(false, "guitar", null, null, null, null, false, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void getAllLicks_instrumentNull_treatsNullInstrumentAsGuitar() {
        // A lick with null instrument is treated as "GUITAR" by the filter
        Lick lick = makeLick(THREE_NOTE_MAJOR, null, Mode.IONIAN);
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(lick));

        List<LickResponse> result = lickService.getAllLicks(false, "GUITAR", null, null, null, null, false, 1L);

        assertEquals(1, result.size(), "Lick with null instrument should match GUITAR filter");
    }

    @Test
    void getAllLicks_modeFilter_excludesNonMatchingMode() {
        Lick ionianLick  = makeLick(THREE_NOTE_MAJOR, "GUITAR", Mode.IONIAN);
        Lick aeolianLick = makeLick(THREE_NOTE_MAJOR, "GUITAR", Mode.AEOLIAN);
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(ionianLick, aeolianLick));

        List<LickResponse> result = lickService.getAllLicks(false, null, "IONIAN", null, null, null, false, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void getAllLicks_minLengthFilter_excludesShortLicks() {
        Lick shortLick = makeLick(THREE_NOTE_MAJOR, "GUITAR", Mode.IONIAN); // 3 intervals
        Lick longLick  = makeLick(MINOR_PENTA,      "GUITAR", Mode.AEOLIAN); // 5 intervals
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(shortLick, longLick));

        List<LickResponse> result = lickService.getAllLicks(false, null, null, 4, null, null, false, 1L);

        assertEquals(1, result.size(), "minLength=4 should exclude the 3-note lick");
    }

    @Test
    void getAllLicks_maxLengthFilter_excludesLongLicks() {
        Lick shortLick = makeLick(THREE_NOTE_MAJOR, "GUITAR", Mode.IONIAN); // 3 intervals
        Lick longLick  = makeLick(MINOR_PENTA,      "GUITAR", Mode.AEOLIAN); // 5 intervals
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(shortLick, longLick));

        List<LickResponse> result = lickService.getAllLicks(false, null, null, null, 4, null, false, 1L);

        assertEquals(1, result.size(), "maxLength=4 should exclude the 5-note lick");
    }

    @Test
    void getAllLicks_intervalSubsequenceFilter_matchesContiguousSubstring() {
        // MINOR_PENTA = [1, b3, 4, 5, b7]; filter "b3,4" = contiguous match at index 1-2
        Lick pentaLick = makeLick(MINOR_PENTA,      "GUITAR", Mode.AEOLIAN);
        Lick majorLick = makeLick(THREE_NOTE_MAJOR, "GUITAR", Mode.IONIAN);
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(pentaLick, majorLick));

        List<LickResponse> result = lickService.getAllLicks(false, null, null, null, null, "b3,4", false, 1L);

        assertEquals(1, result.size(), "Only the minor penta lick should match filter 'b3,4'");
    }

    @Test
    void getAllLicks_intervalSubsequenceFilter_noMatchReturnsEmpty() {
        Lick lick = makeLick(THREE_NOTE_MAJOR, "GUITAR", Mode.IONIAN);
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(lick));

        List<LickResponse> result = lickService.getAllLicks(false, null, null, null, null, "b7,b2", false, 1L);

        assertTrue(result.isEmpty(), "Filter 'b7,b2' should not match major scale fragment");
    }

    @Test
    void getAllLicks_noFilters_returnsAll() {
        Lick l1 = makeLick(THREE_NOTE_MAJOR, "GUITAR", Mode.IONIAN);
        Lick l2 = makeLick(MINOR_PENTA,     "BASS",   Mode.AEOLIAN);
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(l1, l2));

        List<LickResponse> result = lickService.getAllLicks(false, null, null, null, null, null, false, 1L);

        assertEquals(2, result.size());
    }

    @Test
    void getAllLicks_intervalSubsequenceFilter_nonContiguousDoesNotMatch() {
        // MINOR_PENTA = [1, b3, 4, 5, b7]; tokens ["1","4"] are not adjacent → no match
        Lick lick = makeLick(MINOR_PENTA, "GUITAR", Mode.AEOLIAN);
        when(lickRepository.findAllByAutoImportedFalse()).thenReturn(List.of(lick));

        List<LickResponse> result = lickService.getAllLicks(false, null, null, null, null, "1,4", false, 1L);

        assertTrue(result.isEmpty(), "Non-contiguous token sequence should not match");
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

        lickService.getLick(UUID.randomUUID(), Note.A, "greedy", Guitar.STANDARD, 1L);

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

        lickService.getLick(UUID.randomUUID(), Note.A, "greedy", Guitar.STANDARD, 1L);

        verify(positionCacheRepository).save(any(PositionCache.class));
    }
}
