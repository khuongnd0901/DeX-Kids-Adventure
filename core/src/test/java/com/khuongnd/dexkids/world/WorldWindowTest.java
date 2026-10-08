package com.khuongnd.dexkids.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorldWindowTest {
    @Test void boundedWhileScrollingAndReentryIsDeterministic() {
        var g = new ProceduralWorldGenerator(39);
        var window = new WorldWindow(g, 1, 4);
        window.prepare(0);
        WorldChunk original = window.get(0);
        assertEquals(6, window.size());
        for (long i = 1; i < 50000; i += 101) {
            window.prepare(i);
            assertEquals(6, window.size());
        }
        window.prepare(0);
        assertEquals(original, window.get(0));
        assertEquals(g.generate(-1), window.get(-1));
        assertThrows(IllegalArgumentException.class, () -> window.get(6));
    }
}
