package com.khuongnd.dexkids.journey;

import com.khuongnd.dexkids.geo.GeoFix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LiveJourneyFeedTest {
    @Test void validFixesAdvanceWithoutTeleport() {
        var feed = new LiveJourneyFeed();
        long now = System.currentTimeMillis();
        feed.accept(new GeoFix(10, 106, 4, 5, now - 2000));
        feed.update(0.1f);
        feed.accept(new GeoFix(10.0001, 106, 4, 5, now - 1000));
        feed.update(0.1f);
        assertTrue(feed.distanceMeters() > 5);
        double before = feed.distanceMeters();
        feed.accept(new GeoFix(11, 107, 4, 55, now));
        feed.update(0.1f);
        assertEquals(before, feed.distanceMeters(), 1e-9);
        feed.clearHistory();
        assertEquals(0, feed.distanceMeters());
    }
    @Test void poorQualityFixDoesNotAdvance() {
        var feed = new LiveJourneyFeed();
        long now = System.currentTimeMillis();
        feed.accept(new GeoFix(10, 106, 90, 4, now));
        feed.update(0.1f);
        assertEquals(0, feed.distanceMeters());
    }
}
