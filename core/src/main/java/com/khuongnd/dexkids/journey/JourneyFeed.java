
package com.khuongnd.dexkids.journey;

/** A testable source of journey motion. Android real GPS adapter comes in M4. */
public interface JourneyFeed {
    void update(float deltaSeconds);
    double distanceMeters();
    double speedMetersPerSecond();
    boolean isDemo();
}
