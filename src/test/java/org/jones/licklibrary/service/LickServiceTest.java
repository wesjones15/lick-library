package org.jones.licklibrary.service;

import org.jones.licklibrary.repository.LickRepository;
import org.jones.licklibrary.repository.PositionCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LickServiceTest {

    @Mock private LickRepository lickRepository;
    @Mock private PositionCacheRepository positionCacheRepository;

    private LickService lickService;

    @BeforeEach
    void setUp() {
        lickService = new LickService(lickRepository, positionCacheRepository);
    }

    // --- uploadLick ---

    @Test
    void uploadLick_storesNewLick() {
        // TODO
    }

    @Test
    void uploadLick_doesNotDuplicateExistingLick() {
        // TODO
    }

    // --- getLicks / resolvePositions ---

    @Test
    void getLicks_returnsCachedPositionsOnHit() {
        // TODO
    }

    @Test
    void getLicks_computesAndCachesPositionsOnMiss() {
        // TODO
    }
}
