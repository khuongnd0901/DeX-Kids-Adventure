package com.khuongnd.dexkids.geo;

import java.util.*;

/**
 * Lightweight pre-indexed proximity events. No road map-matching: PASSING is
 * explicitly described as a geometric candidate, never a factual claim.
 */
public final class OfflinePoiEngine {
    public enum EventKind { NEARBY, APPROACHING, PASSING_CANDIDATE }
    public record Notice(OfflinePoiCatalog.Entry entry, EventKind event, double meters,
                         double confidence, boolean simulated) {
        public Notice {
            if (!Double.isFinite(confidence) || confidence < 0 || confidence > 1)
                throw new IllegalArgumentException("Invalid POI confidence");
        }
    }
    private final Map<Long, List<OfflinePoiCatalog.Entry>> tiles = new HashMap<>();
    private final PoiEventDetector detector = new PoiEventDetector();
    private final Set<String> emittedNear = new HashSet<>();
    private final Set<String> emittedApproaching = new HashSet<>();
    private final Set<String> emittedPassing = new HashSet<>();
    private long lastSample = -1;
    private long lastProcessedMillis = Long.MIN_VALUE;
    private long lastEmittedMillis = Long.MIN_VALUE;
    private static final double CELL_DEGREES = .005;
    private static final long SAMPLE_INTERVAL_MS = 800;
    private static final long NOTIFICATION_COOLDOWN_MS = 30_000;

    public OfflinePoiEngine(OfflinePoiCatalog catalogue) {
        Objects.requireNonNull(catalogue);
        for (OfflinePoiCatalog.Entry entry : catalogue.entries()) {
            int lat = cell(entry.poi().latitude()), lon = cell(entry.poi().longitude());
            tiles.computeIfAbsent(key(lat, lon), ignored -> new ArrayList<>()).add(entry);
        }
    }
    public void reset() {
        detector.resetJourney();
        emittedNear.clear(); emittedApproaching.clear(); emittedPassing.clear();
        lastSample = -1;
        lastProcessedMillis = lastEmittedMillis = Long.MIN_VALUE;
    }
    public Optional<Notice> observe(JourneyPosition pos, long nowMillis) {
        if (pos == null || !pos.sufficientQuality() || nowMillis <= 0) return Optional.empty();
        if (lastSample > 0 && pos.sampleId() < lastSample) reset(); // GPX restart.
        if (pos.sampleId() == lastSample) return Optional.empty();
        lastSample = pos.sampleId();
        if (lastProcessedMillis != Long.MIN_VALUE &&
                nowMillis >= lastProcessedMillis &&
                nowMillis - lastProcessedMillis < SAMPLE_INTERVAL_MS) return Optional.empty();
        lastProcessedMillis = nowMillis;
        if (tiles.isEmpty()) return Optional.empty();
        int lat = cell(pos.latitude()), lon = cell(pos.longitude());
        List<OfflinePoiCatalog.Entry> nearby = new ArrayList<>();
        for (int a = -1; a <= 1; a++)
            for (int b = -1; b <= 1; b++)
                nearby.addAll(tiles.getOrDefault(key(lat+a,lon+b), List.of()));
        nearby.sort(Comparator.comparingDouble(p ->
                GeoDistance.meters(pos.latitude(), pos.longitude(),
                        p.poi().latitude(), p.poi().longitude())));
        Optional<Notice> candidate = Optional.empty();
        GeoFix fix = new GeoFix(pos.latitude(), pos.longitude(), pos.accuracyMeters(),
                pos.speedMetersPerSecond(), nowMillis);
        for (OfflinePoiCatalog.Entry entry : nearby) {
            double meters = GeoDistance.meters(pos.latitude(), pos.longitude(),
                    entry.poi().latitude(), entry.poi().longitude());
            if (meters > 200) continue;
            // Confidence is only for selection quality, NOT verification of road passage.
            double quality = Math.max(0, (25.0 - pos.accuracyMeters()) / 25.0);
            double proximity = Math.max(0, 1.0 - meters / 200.0);
            double confidence = Math.min(1.0, 0.65 * quality + 0.35 * proximity);
            if (confidence < 0.55) continue;
            Optional<PoiEventDetector.State> updated = detector.update(fix, entry.poi(), nowMillis);
            if (updated.isEmpty()) continue;
            String id = entry.poi().id();
            EventKind kind = switch (updated.get()) {
                case NEAR_POI -> EventKind.NEARBY;
                case APPROACHING_POI -> EventKind.APPROACHING;
                case PASSING_POI -> EventKind.PASSING_CANDIDATE;
                case VISITED_POI -> null;
            };
            if (kind == null) continue;
            boolean fresh = switch (kind) {
                case NEARBY -> emittedNear.add(id);
                case APPROACHING -> emittedApproaching.add(id);
                case PASSING_CANDIDATE -> emittedPassing.add(id);
            };
            if (!fresh || candidate.isPresent()) continue;
            candidate = Optional.of(new Notice(entry, kind, meters, confidence, pos.simulated()));
        }
        if (candidate.isEmpty()) return candidate;
        if (lastEmittedMillis != Long.MIN_VALUE &&
                nowMillis - lastEmittedMillis < NOTIFICATION_COOLDOWN_MS)
            return Optional.empty();
        lastEmittedMillis = nowMillis;
        return candidate;
    }
    private static int cell(double degrees) { return (int) Math.floor(degrees / CELL_DEGREES); }
    private static long key(int lat, int lon) { return ((long)lat << 32) | ((long)lon & 0xffffffffL); }
}
