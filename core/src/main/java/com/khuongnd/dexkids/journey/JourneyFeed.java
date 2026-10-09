
package com.khuongnd.dexkids.journey;

/** A testable source of journey motion. Android real GPS adapter comes in M4. */
public interface JourneyFeed {
    void update(float deltaSeconds);
    double distanceMeters();
    double speedMetersPerSecond();
    boolean isDemo();
    /** Not all journey types carry geolocation (e.g. generated cartoon DEMO). */
    default java.util.Optional<com.khuongnd.dexkids.geo.JourneyPosition> position(long nowMillis) {
        return java.util.Optional.empty();
    }
}
