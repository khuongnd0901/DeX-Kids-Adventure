
package com.khuongnd.dexkids.journey;

public final class SpeedSmoother {
    private double speed;

    /** Exponential smoothing that is independent of render frequency. */
    public double update(double targetMetersPerSecond, double deltaSeconds) {
        if (!Double.isFinite(targetMetersPerSecond) || !Double.isFinite(deltaSeconds)
                || deltaSeconds < 0) {
            throw new IllegalArgumentException("Invalid speed or delta time");
        }
        double alpha = 1.0 - Math.exp(-Math.min(deltaSeconds, 10.0) * 2.5);
        speed += (Math.max(0, targetMetersPerSecond) - speed) * alpha;
        return speed;
    }

    public double value() { return speed; }
    public void reset() { speed = 0; }
}
