
package com.khuongnd.dexkids.journey;

/**
 * Clearly simulated distance (not connected to GPS).
 * Brings the 2D world alive on a computer or emulator during M0/M1.
 */
public final class DemoJourneyFeed implements JourneyFeed {
    private double seconds;
    private double distance;
    private final SpeedSmoother smoother = new SpeedSmoother();

    @Override
    public void update(float dt) {
        double delta = Math.max(0, Math.min(0.1, dt));
        seconds += delta;
        boolean paused = (seconds % 70.0) >= 57.0;
        double requestedSpeed = paused ? 0 : 9.0 + 2.5 * Math.sin(seconds / 12.0);
        distance += smoother.update(requestedSpeed, delta) * delta;
    }

    @Override public double distanceMeters() { return distance; }
    @Override public double speedMetersPerSecond() { return smoother.value(); }
    @Override public boolean isDemo() { return true; }
}
