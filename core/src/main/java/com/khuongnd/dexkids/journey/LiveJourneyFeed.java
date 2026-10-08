package com.khuongnd.dexkids.journey;

import com.khuongnd.dexkids.geo.GeoDistance;
import com.khuongnd.dexkids.geo.GeoFix;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Real GPS fixes drive distance on the render thread, not sprite coordinates.
 * Stale, low-accuracy, invalid-speed or discontinuous fixes never teleport the world.
 */
public final class LiveJourneyFeed implements JourneyFeed {
    private final AtomicReference<GeoFix> pending = new AtomicReference<>();
    private final SpeedSmoother smoother = new SpeedSmoother();
    private GeoFix accepted;
    private double distanceMeters;
    private double displayedSpeed;
    private long latestFixAt;

    public void accept(GeoFix fix) { if (fix != null) pending.set(fix); }

    @Override public void update(float delta) {
        if (!Float.isFinite(delta) || delta < 0) return;
        double seconds = Math.min(delta, 0.1f);
        GeoFix received = pending.getAndSet(null);
        long now = System.currentTimeMillis();
        if (received != null && received.reliable(now)) {
            if (accepted == null) {
                accepted = received;
                latestFixAt = now;
            } else {
                double dt = (received.epochMillis() - accepted.epochMillis()) / 1000.0;
                double deltaMeters = GeoDistance.meters(accepted.latitude(), accepted.longitude(),
                        received.latitude(), received.longitude());
                if (dt > 30) {
                    // Resume after long signal loss; do not count unknown distance.
                    accepted = received;
                    displayedSpeed = 0;
                } else if (dt > 0 && deltaMeters / dt <= 55.0) {
                    distanceMeters += deltaMeters;
                    displayedSpeed = Math.max(0, deltaMeters / dt);
                    accepted = received;
                }
                latestFixAt = now;
            }
        }
        double target = now - latestFixAt > 10_000 ? 0 : displayedSpeed;
        smoother.update(target, seconds);
    }

    @Override public double distanceMeters() { return distanceMeters; }
    @Override public double speedMetersPerSecond() { return smoother.value(); }
    @Override public boolean isDemo() { return false; }
    public void clearHistory() {
        pending.set(null);
        accepted = null;
        distanceMeters = displayedSpeed = 0;
        latestFixAt = 0;
        smoother.reset();
    }
}
