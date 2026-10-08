package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SceneryLayoutTest {
    @Test void absoluteOffsetIsContinuousAcrossChunks() {
        double before = (640.0 - 0.1) / 7.5, after = (640.0 + 0.1) / 7.5;
        assertEquals(-0.2, SceneryLayout.left(0, after) - SceneryLayout.left(0, before), 0.0002); // float pixels
        assertEquals(640, SceneryLayout.left(1, before) - SceneryLayout.left(0, before), 0.001);
        assertEquals(640, SceneryLayout.left(1, after) - SceneryLayout.left(0, after), 0.001);
        assertEquals(1, SceneryLayout.firstChunk(after));
    }
    @Test void parallaxOffsetRepeatsExactlyWithoutAccumulatedFrameDrift() {
        assertEquals(SceneryLayout.parallaxOffset(600, 1.3, 640),
                SceneryLayout.parallaxOffset(600, 1.3, 640), 0.000001);
        assertThrows(IllegalArgumentException.class, () -> SceneryLayout.left(0, -3));
    }
}
