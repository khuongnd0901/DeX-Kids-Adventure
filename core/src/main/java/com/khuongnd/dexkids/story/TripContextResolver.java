package com.khuongnd.dexkids.story;

import java.util.List;
import java.util.Objects;

/**
 * Creates bounded, non-identifying snapshots only from an accepted LIVE cue.
 * Illustrations and remembered scenes can never become real-world POI evidence.
 */
public final class TripContextResolver {
    public static final long POI_CONTEXT_TTL_MS = 150_000L;
    public static final long SCENE_CONTEXT_TTL_MS = 60_000L;
    private TripContextResolver() {}

    public static EntertainmentDirector.Audience focus(
        EntertainmentDirector.Audience audience, long sequence) {
        Objects.requireNonNull(audience);
        if (sequence < 0) throw new IllegalArgumentException("sequence");
        if (audience != EntertainmentDirector.Audience.BOTH) return audience;
        return switch ((int) (sequence % 3)) {
            case 0 -> EntertainmentDirector.Audience.SAU;
            case 1 -> EntertainmentDirector.Audience.ONG;
            default -> EntertainmentDirector.Audience.BOTH;
        };
    }

    public static TripContext fromAcceptedLivePoi(NarrationCue cue,
        EntertainmentDirector.Audience audience, String topic, long nowElapsedMs, long sequence) {
        Objects.requireNonNull(cue);
        if (cue.isGeneric() || cue.poiId().isBlank() || cue.source() == null
            || cue.lastVerifiedAt() == null
            || !"https".equalsIgnoreCase(cue.source().getScheme()))
            throw new IllegalArgumentException("Not a sourced live cue");
        var child = focus(audience, sequence);
        return new TripContext(1, TripContext.Source.SOURCED_POI_ESTIMATE,
            audience, child, child.age(), allowedTopic(topic), List.of(cue.id()),
            List.of(), expiry(nowElapsedMs, POI_CONTEXT_TTL_MS), sequence);
    }

    public static TripContext fictionalScene(
        EntertainmentDirector.Audience audience, String approvedTopic,
        long nowElapsedMs, long sequence) {
        var child = focus(audience, sequence);
        var topic = allowedTopic(approvedTopic);
        return new TripContext(1,
            topic.isEmpty() ? TripContext.Source.GENERAL_OFFLINE : TripContext.Source.FICTIONAL_SCENE,
            audience, child, child.age(), topic, List.of(), List.of(),
            expiry(nowElapsedMs, SCENE_CONTEXT_TTL_MS), sequence);
    }

    private static long expiry(long now, long ttl) {
        if (now < 0 || now > Long.MAX_VALUE - ttl)
            throw new IllegalArgumentException("Invalid monotonic time");
        return now + ttl;
    }

    private static String allowedTopic(String candidate) {
        if (candidate == null) return "";
        return switch (candidate) {
            case "sea", "city", "road", "nature", "weather", "animal", "garden", "safety" -> candidate;
            default -> "";
        };
    }
}
