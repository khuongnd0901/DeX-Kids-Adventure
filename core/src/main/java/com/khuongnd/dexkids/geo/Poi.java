package com.khuongnd.dexkids.geo;

import java.net.URI;
import java.time.Instant;

/** Named POIs cannot be used without human-reviewed source attribution. */
public record Poi(String id, String name, double latitude, double longitude,
                  URI source, Instant verifiedAt) {
    public Poi {
        if (id == null || id.isBlank() || name == null || name.isBlank() ||
                !Double.isFinite(latitude) || !Double.isFinite(longitude) ||
                latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180 ||
                source == null || !"https".equalsIgnoreCase(source.getScheme()) ||
                verifiedAt == null)
            throw new IllegalArgumentException("Unverified POI record");
    }
}
