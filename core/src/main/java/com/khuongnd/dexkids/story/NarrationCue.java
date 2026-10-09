package com.khuongnd.dexkids.story;

import java.net.URI;
import java.time.Instant;

/** Narration requires human-reviewed, sourced facts, or explicit generic mode. */
public record NarrationCue(String id, String poiId, String textVi, URI source,
                           Instant lastVerifiedAt, int minimumAge, int maximumAge) {
    public NarrationCue {
        if (id == null || id.isBlank() || textVi == null || textVi.isBlank() ||
                textVi.length() > 240 || minimumAge < 2 || maximumAge > 6 ||
                maximumAge < minimumAge)
            throw new IllegalArgumentException("Unsafe or invalid narration");
        if (poiId != null &&
                (source == null || !"https".equalsIgnoreCase(source.getScheme()) || lastVerifiedAt == null))
            throw new IllegalArgumentException("Named POI narration lacks verified attribution");
    }
    public boolean isGeneric() { return poiId == null; }
}
