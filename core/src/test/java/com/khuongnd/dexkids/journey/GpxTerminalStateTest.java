package com.khuongnd.dexkids.journey;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GpxTerminalStateTest {
    @Test void completedRouteStopsSpeedAndKeepsFinalDistance() {
        var start = Instant.parse("2026-10-09T00:00:00Z");
        var feed = new GpxReplayFeed(List.of(
                new GpxReplayFeed.Point(0, 0, start),
                new GpxReplayFeed.Point(0.0001, 0, start.plusSeconds(2))));
        for (int i = 0; i < 19; i++) feed.update(0.1f);
        assertTrue(feed.speedMetersPerSecond() > 0);
        while (!feed.finished()) feed.update(0.1f);
        double finalDistance = feed.distanceMeters();
        assertEquals(0, feed.speedMetersPerSecond(), 1e-9);
        for (int i = 0; i < 300; i++) feed.update(0.1f);
        assertEquals(finalDistance, feed.distanceMeters(), 1e-9);
        assertEquals(2, feed.replayTimeSeconds(), 1e-9);
        feed.reset();
        assertFalse(feed.finished());
        assertEquals(0, feed.speedMetersPerSecond());
        assertEquals(0, feed.distanceMeters());
    }
}
