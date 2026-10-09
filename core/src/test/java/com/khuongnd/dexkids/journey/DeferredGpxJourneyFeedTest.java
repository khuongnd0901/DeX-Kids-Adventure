package com.khuongnd.dexkids.journey;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeferredGpxJourneyFeedTest {
    @Test void startsStationaryThenBindsOnlyOneParsedRoute() {
        var deferred = new DeferredGpxJourneyFeed();
        assertFalse(deferred.isLoaded());
        assertTrue(deferred.isDemo());
        for(int i = 0; i < 100; i++) deferred.update(0.1f);
        assertEquals(0, deferred.distanceMeters());
        var replay = GpxReplayFeedTest.replay();
        deferred.install(replay);
        assertSame(replay, deferred.replay());
        deferred.update(0.1f);
        assertTrue(deferred.distanceMeters() > 0);
        assertThrows(IllegalStateException.class, () -> deferred.install(GpxReplayFeedTest.replay()));
        replay.setPaused(true);
        double before = deferred.distanceMeters();
        deferred.update(0.1f);
        assertEquals(before, deferred.distanceMeters(), 0.00001);
    }
}
