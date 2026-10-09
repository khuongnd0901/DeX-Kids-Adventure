package com.khuongnd.dexkids.journey;
import com.khuongnd.dexkids.geo.GeoFix;
import com.khuongnd.dexkids.geo.JourneyPosition;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JourneyPositionTest {
    @Test void projectedGpxCoordinatesAreSimulatedAndRejectedTeleportIsNotExposed() {
        var feed = GpxReplayFeedTest.replay();
        feed.update(0.1f);
        var sample = feed.position(System.currentTimeMillis()).orElseThrow();
        assertTrue(sample.simulated());
        assertTrue(sample.sufficientQuality());
        assertTrue(sample.latitude() >= 10.0);
        assertTrue(sample.latitude() <= 10.0002);
        var jump = new GpxReplayFeed(java.util.List.of(
            new GpxReplayFeed.Point(10,106,java.time.Instant.parse("2026-10-08T00:00:00Z")),
            new GpxReplayFeed.Point(11,107,java.time.Instant.parse("2026-10-08T00:00:01Z"))));
        jump.update(.1f);
        assertTrue(jump.position(System.currentTimeMillis()).isEmpty());
    }
    @Test void realFixMustBeFreshBeforePoiQuery() {
        long now = 1791450000000L;
        var feed = new LiveJourneyFeed(() -> now);
        feed.accept(new GeoFix(10,106,5,0,now - 1000));
        feed.update(.1f);
        assertFalse(feed.position(now).orElseThrow().simulated());
        assertTrue(feed.position(now + 16_000).isEmpty());
    }
    @Test void deferredTrackExposesNoPositionBeforeLoaded() {
        var holder = new DeferredGpxJourneyFeed();
        assertTrue(holder.position(System.currentTimeMillis()).isEmpty());
        holder.install(GpxReplayFeedTest.replay());
        assertTrue(holder.position(System.currentTimeMillis()).isPresent());
    }
}
