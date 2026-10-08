package com.khuongnd.dexkids.story;

import java.util.HashMap;
import java.util.Map;

/** Ephemeral session-only memory. No raw coordinates or cloud synchronization. */
public final class JourneyMemory {
    private final Map<String, Integer> visitCounts = new HashMap<>();
    public int recordVisit(String poiId) {
        if (poiId == null || poiId.isBlank()) throw new IllegalArgumentException("poiId");
        return visitCounts.merge(poiId, 1, Integer::sum);
    }
    public int variantIndex(String poiId, int variants) {
        if (variants <= 0) throw new IllegalArgumentException("variants");
        return Math.floorMod(visitCounts.getOrDefault(poiId, 0), variants);
    }
    public void clear() { visitCounts.clear(); }
    public int trackedPoiCount() { return visitCounts.size(); }
}
