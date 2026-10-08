package com.khuongnd.dexkids.geo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Conservative proximity classifier. A single NEAR fix never makes PASSING.
 * A PASSING result still needs road direction/ground-truth acceptance before
 * the guide may *claim* it was passed in real life.
 */
public final class PoiEventDetector {
    public enum State { NEAR_POI, APPROACHING_POI, PASSING_POI, VISITED_POI }
    private static final class Track {
        double lastDistance = Double.POSITIVE_INFINITY;
        double minDistance = Double.POSITIVE_INFINITY;
        boolean approached;
        boolean passed;
    }
    private final Map<String, Track> tracks = new HashMap<>();

    public Optional<State> update(GeoFix fix, Poi poi, long nowMillis) {
        if (!fix.reliable(nowMillis)) return Optional.empty();
        double d = GeoDistance.meters(fix.latitude(), fix.longitude(), poi.latitude(), poi.longitude());
        Track t = tracks.computeIfAbsent(poi.id(), ignored -> new Track());
        State state = null;
        if (t.passed) {
            if (d < 100) state = State.VISITED_POI;
        } else if (t.approached && t.minDistance <= 25 && d > 55 &&
                d > t.lastDistance + 8 && fix.speedMetersPerSecond() >= 2.0f) {
            t.passed = true;
            state = State.PASSING_POI;
        } else if (d < 150) {
            if (t.lastDistance < Double.POSITIVE_INFINITY &&
                    d + 5 < t.lastDistance && fix.speedMetersPerSecond() >= 2.0f) {
                t.approached = true;
                state = State.APPROACHING_POI;
            } else state = State.NEAR_POI;
        }
        t.lastDistance = d;
        t.minDistance = Math.min(t.minDistance, d);
        return Optional.ofNullable(state);
    }

    public void resetJourney() { tracks.clear(); }
}
