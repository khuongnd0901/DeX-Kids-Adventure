package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TripContextTest {
    @Test void audienceFocusRotatesWithoutVoiceAttribution() {
        var both = EntertainmentDirector.Audience.BOTH;
        assertEquals(EntertainmentDirector.Audience.SAU, TripContextResolver.focus(both, 0));
        assertEquals(EntertainmentDirector.Audience.ONG, TripContextResolver.focus(both, 1));
        assertEquals(both, TripContextResolver.focus(both, 2));
        assertEquals(EntertainmentDirector.Audience.ONG,
            TripContextResolver.focus(EntertainmentDirector.Audience.ONG, 100));
    }

    @Test void livePoiIsShortLivedAndSceneNeverBecomesEvidence() {
        var cue = new NarrationCue("place-reference", "osm:node:123",
            "Đây là thông tin kiểm duyệt của một địa danh.", URI.create("https://example.org"),
            Instant.parse("2026-01-01T00:00:00Z"), 3, 6);
        var sourced = TripContextResolver.fromAcceptedLivePoi(cue,
            EntertainmentDirector.Audience.BOTH, "sea", 1000, 0);
        assertTrue(sourced.hasReviewedPoiReference(1000));
        assertFalse(sourced.hasReviewedPoiReference(151_000));
        assertEquals(5, sourced.focusAge());
        var scene = TripContextResolver.fictionalScene(
            EntertainmentDirector.Audience.BOTH, "sea", 200, 1);
        assertEquals(TripContext.Source.FICTIONAL_SCENE, scene.source());
        assertFalse(scene.hasReviewedPoiReference(201));
        assertEquals(4, scene.focusAge());
        assertTrue(scene.reviewedFactIds().isEmpty());
        var unknown = TripContextResolver.fictionalScene(
            EntertainmentDirector.Audience.SAU, "unknown", 200, 0);
        assertEquals(TripContext.Source.GENERAL_OFFLINE, unknown.source());
        assertEquals("", unknown.topic());
    }

    @Test void rejectsForgedFactAndDoesNotAllowMutableIds() {
        assertThrows(IllegalArgumentException.class, () -> new TripContext(1,
            TripContext.Source.FICTIONAL_SCENE, EntertainmentDirector.Audience.SAU,
            EntertainmentDirector.Audience.SAU, 5, "sea", List.of("fake-poi"), List.of(), 200, 0));
        var context = TripContextResolver.fictionalScene(
            EntertainmentDirector.Audience.ONG, "garden", 100, 0);
        assertThrows(UnsupportedOperationException.class,
            () -> context.reviewedFactIds().add("unverified"));
    }

    @Test void genericCueCannotBePromotedToLivePoi() {
        var generic = new NarrationCue("generic-cue", null, "Đố vui về động vật",
            null, null, 3, 6);
        assertThrows(IllegalArgumentException.class, () ->
            TripContextResolver.fromAcceptedLivePoi(generic,
                EntertainmentDirector.Audience.ONG, "animal", 100, 0));
    }
}
