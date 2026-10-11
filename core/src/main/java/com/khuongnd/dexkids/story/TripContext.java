package com.khuongnd.dexkids.story;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable, short-lived educational context. Deliberately contains no GPS coordinates,
 * heading, speed, child voice, name, route, or transcript.
 */
public record TripContext(
    int version, Source source, EntertainmentDirector.Audience audience,
    EntertainmentDirector.Audience focus, int focusAge, String topic,
    List<String> reviewedFactIds, List<String> approvedWordIds,
    long expiresAtElapsedMs, long sequence) {

    public enum Source { SOURCED_POI_ESTIMATE, FICTIONAL_SCENE, GENERAL_OFFLINE }
    private static final Set<String> TOPICS = Set.of(
        "", "sea", "city", "road", "nature", "weather", "animal", "garden", "safety");

    public TripContext {
        Objects.requireNonNull(source);
        Objects.requireNonNull(audience);
        Objects.requireNonNull(focus);
        Objects.requireNonNull(topic);
        reviewedFactIds = List.copyOf(reviewedFactIds);
        approvedWordIds = List.copyOf(approvedWordIds);
        if (version != 1 || sequence < 0 || expiresAtElapsedMs <= 0
            || !TOPICS.contains(topic) || reviewedFactIds.size() > 8
            || approvedWordIds.size() > 12 || focusAge != focus.age()
            || (audience != EntertainmentDirector.Audience.BOTH && focus != audience)
            || reviewedFactIds.stream().anyMatch(id -> !safeId(id))
            || approvedWordIds.stream().anyMatch(id -> !safeId(id))
            || (source == Source.SOURCED_POI_ESTIMATE && reviewedFactIds.isEmpty())
            || (source != Source.SOURCED_POI_ESTIMATE && !reviewedFactIds.isEmpty())
            || (source == Source.GENERAL_OFFLINE && !topic.isEmpty()))
            throw new IllegalArgumentException("Invalid privacy-safe trip context");
    }

    private static boolean safeId(String id) {
        return id != null && id.matches("[a-z][a-z0-9_-]{2,63}");
    }

    public boolean active(long nowElapsedMs) {
        return nowElapsedMs >= 0 && nowElapsedMs < expiresAtElapsedMs;
    }

    public TripContext withSequence(long next) {
        var newFocus = TripContextResolver.focus(audience, next);
        return new TripContext(version, source, audience, newFocus, newFocus.age(),
            topic, reviewedFactIds, approvedWordIds, expiresAtElapsedMs, next);
    }

    /** Factual enrichment still requires a separate reviewed ID-to-answer lookup. */
    public boolean hasReviewedPoiReference(long nowElapsedMs) {
        return active(nowElapsedMs) && source == Source.SOURCED_POI_ESTIMATE
            && !reviewedFactIds.isEmpty();
    }
}
