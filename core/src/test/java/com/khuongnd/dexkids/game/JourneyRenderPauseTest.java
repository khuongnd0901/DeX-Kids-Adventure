package com.khuongnd.dexkids.game;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class JourneyRenderPauseTest {
    @Test void menuFreezesJourneyWithoutChangingNormalDelta() {
        assertEquals(0f, JourneyRenderPause.effectiveDelta(0.016f, true));
        assertEquals(0f, JourneyRenderPause.effectiveDelta(1f, true));
        assertEquals(0.016f, JourneyRenderPause.effectiveDelta(0.016f, false));
        assertEquals(0.1f, JourneyRenderPause.effectiveDelta(2f, false));
        assertEquals(0f, JourneyRenderPause.effectiveDelta(Float.NaN, false));
    }
}
