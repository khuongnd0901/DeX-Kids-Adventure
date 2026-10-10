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
    @Test void ringCacheReusesExactlyTheFourVisibleChunks() {
        var generator = new ProceduralWorldGenerator(20261008L);
        var window = new WorldWindow(generator, 0, 3);
        assertEquals(0, window.size());
        window.prepare(100);
        var first = window.get(100);
        var next = window.get(101);
        assertEquals(4, window.size());
        for (int i = 0; i < 100_000; i++) window.prepare(100);
        assertSame(first, window.get(100), "Stable frames must not regenerate the chunk");
        window.prepare(101);
        assertSame(next, window.get(101), "Overlapping chunk identity is retained");
        assertEquals(4, window.size());
        assertThrows(IllegalArgumentException.class, () -> window.get(100));
        assertEquals(generator.generate(104), window.get(104));
        window.prepare(100);
        assertEquals(first, window.get(100), "Re-entry must remain deterministic");
    }

    @Test void negativeChunkIndicesAndLargeSeekStayBounded() {
        var window = new WorldWindow(new ProceduralWorldGenerator(39), 0, 3);
        for (long first : new long[]{-1001, -17, -1, 0, 1, 99999, 1}) {
            window.prepare(first);
            assertEquals(4, window.size());
            for (long c = first; c <= first + 3; c++)
                assertEquals(c, window.get(c).index());
            assertThrows(IllegalArgumentException.class, () -> window.get(first - 1));
        }
    }
}
