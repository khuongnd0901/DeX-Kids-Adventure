package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TourGuideDirectorTest {
    @Test void unverifiedNamedFactsNeverPlay() {
        var cue = new NarrationCue("p1", "poi1", "Đây là thông tin đã xác minh.",
                URI.create("https://example.org/source"), Instant.parse("2026-10-08T00:00:00Z"), 2, 6);
        var director = new TourGuideDirector(10000);
        assertTrue(director.select(cue, 4, false, 1000).isEmpty());
        assertTrue(director.select(cue, 4, true, 1000).isPresent());
        assertTrue(director.select(cue, 4, true, 5000).isEmpty());
        assertTrue(director.select(cue, 4, true, 12000).isPresent());
    }
    @Test void genericFallbackDoesNotRequireNamedPoi() {
        var cue = new NarrationCue("generic", null, "Con thử nhìn xem có bao nhiêu màu nhé.",
                null, null, 2, 6);
        assertTrue(new TourGuideDirector(15000).select(cue, 4, false, 1000).isPresent());
    }
    @Test void rejectUnattributedPoiAndWrongAge() {
        assertThrows(IllegalArgumentException.class, () ->
                new NarrationCue("bad", "poi", "Mô tả", null, null, 2, 6));
        var cue = new NarrationCue("a", null, "Tìm đám mây nhé.", null, null, 4, 6);
        assertTrue(new TourGuideDirector(0).select(cue, 2, false, 100).isEmpty());
    }
}
