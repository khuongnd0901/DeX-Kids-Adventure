package com.khuongnd.dexkids.geo;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class PoiEventDetectorTest {
    private static final long NOW = 1791450000000L;
    private final Poi poi = new Poi("demo-001", "Synthetic testing point",
            10, 106, URI.create("https://example.org/test-only"), Instant.parse("2026-10-08T00:00:00Z"));

    @Test void aSingleNearFixIsNotPassing() {
        var d = new PoiEventDetector();
        assertEquals(PoiEventDetector.State.NEAR_POI,
                d.update(new GeoFix(10.0001, 106, 8, 0, NOW), poi, NOW).orElseThrow());
    }
    @Test void staleOrUncertainGpsDoesNotTrigger() {
        var d = new PoiEventDetector();
        assertTrue(d.update(new GeoFix(10, 106, 80, 3, NOW), poi, NOW).isEmpty());
        assertTrue(d.update(new GeoFix(10, 106, 4, 3, NOW - 16000), poi, NOW).isEmpty());
    }
    @Test void conservativeCrossingSequence() {
        var d = new PoiEventDetector();
        d.update(new GeoFix(10.0010, 106, 5, 10, NOW), poi, NOW);
        assertEquals(PoiEventDetector.State.APPROACHING_POI,
                d.update(new GeoFix(10.0002, 106, 5, 10, NOW), poi, NOW).orElseThrow());
        d.update(new GeoFix(10.0001, 106, 5, 10, NOW), poi, NOW);
        assertEquals(PoiEventDetector.State.PASSING_POI,
                d.update(new GeoFix(9.9994, 106, 5, 10, NOW), poi, NOW).orElseThrow());
    }
}
