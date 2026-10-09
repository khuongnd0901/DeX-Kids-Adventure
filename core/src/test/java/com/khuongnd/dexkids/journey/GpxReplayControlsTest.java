package com.khuongnd.dexkids.journey;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GpxReplayControlsTest {
    @Test void pauseResumeResetAndEndStayDeterministic() {
        var feed = GpxReplayFeedTest.replay();
        feed.update(0.1f);
        double initial = feed.replayTimeSeconds();
        feed.setPaused(true);
        assertTrue(feed.isPaused());
        for (int i = 0; i < 20; i++) feed.update(0.1f);
        assertEquals(initial, feed.replayTimeSeconds(), 0.00001);
        feed.setPaused(false);
        assertFalse(feed.isPaused());
        for (int i=0; i<250; i++) feed.update(0.1f);
        assertTrue(feed.finished());
        assertEquals(0.0, feed.speedMetersPerSecond(), 0.000001);
        assertEquals(20.0, feed.totalTimeSeconds(), 0.00001);
        feed.reset();
        assertFalse(feed.finished());
        assertFalse(feed.isPaused());
        assertEquals(0.0, feed.replayTimeSeconds(), 0.00001);
        assertEquals(0.0, feed.distanceMeters(), 0.00001);
        feed.update(0.1f);
        assertTrue(feed.distanceMeters() > 0.0);
    }
}
