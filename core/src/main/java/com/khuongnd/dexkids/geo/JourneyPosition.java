package com.khuongnd.dexkids.geo;

/** Position for proximity calculations only; never a direct sprite coordinate. */
public record JourneyPosition(double latitude, double longitude, float accuracyMeters,
                              float speedMetersPerSecond, long sampleId, boolean simulated) {
    public JourneyPosition {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90 ||
                !Double.isFinite(longitude) || longitude < -180 || longitude > 180 ||
                !Float.isFinite(accuracyMeters) || accuracyMeters < 0 ||
                !Float.isFinite(speedMetersPerSecond) || speedMetersPerSecond < 0 ||
                sampleId <= 0)
            throw new IllegalArgumentException("Invalid geographic observation");
    }
    public boolean sufficientQuality() { return accuracyMeters <= 25.0f; }
}
