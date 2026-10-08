package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FrameProfilerTest {
    @Test void emptyAndInvalidSamplesDoNotProduceFakeMetrics() {
        var p = new FrameProfiler(10);
        assertEquals(0, p.averageFps());
        assertEquals(0, p.p95FrameMs());
        p.record(-1);
        p.record(Float.NaN);
        assertEquals(0, p.size());
    }
    @Test void boundedBufferAndP95() {
        var p = new FrameProfiler(10);
        for (int i = 0; i < 10; i++) p.record(0.02f);
        assertEquals(50.0, p.averageFps(), 0.01);
        assertEquals(20, p.p95FrameMs(), 0.01);
        p.record(0.10f);
        assertEquals(10, p.size());
        assertEquals(100, p.p95FrameMs(), 0.01);
        p.reset();
        assertEquals(0, p.size());
    }
    @Test void rejectsInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new FrameProfiler(1));
    }
}
