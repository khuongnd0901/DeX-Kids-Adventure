package com.khuongnd.dexkids.geo;

/** Quality-gated Android GPS reading; never a direct rendering coordinate. */
public record GeoFix(double latitude, double longitude, float accuracyMeters,
                     float speedMetersPerSecond, long epochMillis) {
    public GeoFix {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90 ||
                !Double.isFinite(longitude) || longitude < -180 || longitude > 180 ||
                !Float.isFinite(accuracyMeters) || accuracyMeters < 0 ||
                !Float.isFinite(speedMetersPerSecond) || epochMillis <= 0)
            throw new IllegalArgumentException("Invalid geo fix");
    }
    public boolean reliable(long now) {
        return accuracyMeters <= 50 && now >= epochMillis && now - epochMillis <= 15000;
    }
}
