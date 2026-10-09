package com.khuongnd.dexkids.game;

/**
 * Pure animation transform: wheel angle is distance-based (no accumulated angle drift);
 * suspension response follows speed/acceleration but never affects GPS distance.
 */
public final class VehicleMotionModel {
    private static final double PIXELS_PER_METER = 7.5;
    private static final double WHEEL_RENDER_RADIUS = 46.0;
    private double animationSeconds;
    private double filteredAcceleration;
    private double previousSpeed;
    private boolean hadSample;

    public void advance(double deltaSeconds, double speedMetersPerSecond) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0 ||
                !Double.isFinite(speedMetersPerSecond) || speedMetersPerSecond < 0)
            throw new IllegalArgumentException("Invalid vehicle motion");
        if (deltaSeconds == 0) return;
        animationSeconds += deltaSeconds;
        double acceleration = hadSample
                ? (speedMetersPerSecond - previousSpeed) / Math.max(deltaSeconds, 0.001) : 0;
        // Filter raw speed steps and bound frame-to-frame suspension changes.
        filteredAcceleration += (clamp(acceleration, -10, 10) - filteredAcceleration) *
                (1 - Math.exp(-deltaSeconds * 4));
        previousSpeed = speedMetersPerSecond;
        hadSample = true;
    }

    public float wheelDegrees(double distanceMeters) {
        if (!Double.isFinite(distanceMeters) || distanceMeters < 0)
            throw new IllegalArgumentException("Invalid journey distance");
        return (float) (-(distanceMeters * PIXELS_PER_METER / WHEEL_RENDER_RADIUS)
                * 180.0 / Math.PI % 360);
    }

    public float bodyBob(double speedMetersPerSecond) {
        double speedFactor = clamp(speedMetersPerSecond / 9.0, 0, 1);
        return (float) (Math.sin(animationSeconds * 6.1) * 3.2 * speedFactor);
    }

    public float bodyPitchDegrees() { return (float) clamp(-filteredAcceleration * 0.35, -3.5, 3.5); }
    public void reset() { animationSeconds = filteredAcceleration = previousSpeed = 0; hadSample = false; }
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
