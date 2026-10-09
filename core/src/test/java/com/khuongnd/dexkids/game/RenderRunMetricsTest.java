package com.khuongnd.dexkids.game;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RenderRunMetricsTest {
    @Test void aggregateFpsUsesElapsedTimeAndP95IncludesSlowFrames() {
        var m = new RenderRunMetrics();
        for (int i = 0; i < 94; i++) m.record(0.016f);
        for (int i = 0; i < 6; i++) m.record(0.1f);
        assertEquals(100, m.frames());
        assertEquals(100 / (94 * (double) 0.016f + 6 * (double) 0.1f), m.averageFps(), 1e-9);
        assertEquals(101, m.p95UpperMs());
    }
    @Test void invalidSamplesDoNotPolluteStatisticsAndVerySlowFramesAreExplicit() {
        var m = new RenderRunMetrics();
        m.record(0); m.record(-1); m.record(Float.NaN); m.record(Float.POSITIVE_INFINITY);
        assertEquals(0, m.frames()); assertEquals(0, m.averageFps()); assertEquals(0, m.p95UpperMs());
        m.record(11);
        assertEquals(Double.POSITIVE_INFINITY, m.p95UpperMs());
    }
}
