package com.khuongnd.dexkids.journey;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GpxReplayFeedTest {
    static GpxReplayFeed replay() {
        return new GpxReplayFeed(List.of(
                new GpxReplayFeed.Point(10.0, 106.0, Instant.parse("2026-10-08T00:00:00Z")),
                new GpxReplayFeed.Point(10.0001, 106.0, Instant.parse("2026-10-08T00:00:10Z")),
                new GpxReplayFeed.Point(10.0002, 106.0, Instant.parse("2026-10-08T00:00:20Z"))));
    }
    @Test void replayIsDeterministicAndPauses() {
        var first = replay();
        var second = replay();
        for (int i = 0; i < 300; i++) {
            first.update(0.1f); second.update(0.1f);
        }
        assertEquals(first.distanceMeters(), second.distanceMeters(), 1e-8);
        assertTrue(first.finished());
        double finalDistance = first.distanceMeters();
        first.setPaused(true); first.update(0.1f);
        assertEquals(finalDistance, first.distanceMeters(), 1e-8);
        first.reset();
        assertEquals(0, first.distanceMeters());
    }
    @Test void improbableTeleportDoesNotMoveTheCartoonBus() {
        var feed = new GpxReplayFeed(List.of(
                new GpxReplayFeed.Point(10, 106, Instant.parse("2026-10-08T00:00:00Z")),
                new GpxReplayFeed.Point(11, 107, Instant.parse("2026-10-08T00:00:01Z"))));
        for (int i = 0; i < 50; i++) feed.update(0.1f);
        assertEquals(0, feed.distanceMeters(), 1e-7);
    }
    @Test void safeXmlParserAndTimestampOrder() {
        String xml = "<gpx xmlns='http://www.topografix.com/GPX/1/1'><trk><trkseg>" +
                "<trkpt lat='10' lon='106'><time>2026-10-08T00:00:00Z</time></trkpt>" +
                "<trkpt lat='10.0001' lon='106'><time>2026-10-08T00:00:10Z</time></trkpt>" +
                "</trkseg></trk></gpx>";
        assertNotNull(GpxReplayFeed.fromGpx(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        assertThrows(IllegalArgumentException.class, () -> GpxReplayFeed.fromGpx(
                new ByteArrayInputStream("<!DOCTYPE gpx [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><gpx/>".getBytes(StandardCharsets.UTF_8))));
    }
}
