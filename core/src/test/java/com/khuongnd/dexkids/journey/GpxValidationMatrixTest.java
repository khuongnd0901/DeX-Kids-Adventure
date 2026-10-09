package com.khuongnd.dexkids.journey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Core GPX replay tests; these do not represent Android GPS injection or real GPS accuracy. */
class GpxValidationMatrixTest {
    private static final Instant START = Instant.parse("2026-10-08T00:00:00Z");

    private static GpxReplayFeed fixture() throws IOException {
        // Gradle normally runs tests from core; also permit execution from repository root.
        Path path = Path.of("../test-data/gps/synthetic-urban-short.gpx");
        if (!Files.isRegularFile(path)) path = Path.of("test-data/gps/synthetic-urban-short.gpx");
        try (var input = Files.newInputStream(path)) {
            return GpxReplayFeed.fromGpx(input);
        }
    }

    private static void advance(GpxReplayFeed feed, int frames) {
        for (int i = 0; i < frames; i++) feed.update(0.1f);
    }

    @Test void realSyntheticFixtureParsesAndReplaysDeterministicallyAfterReset() throws IOException {
        var first = fixture();
        var second = fixture();
        for (int i = 0; i < 310; i++) {
            first.update(0.1f);
            second.update(0.1f);
            assertEquals(first.replayTimeSeconds(), second.replayTimeSeconds(), 0);
            assertEquals(first.distanceMeters(), second.distanceMeters(), 0);
            assertEquals(first.speedMetersPerSecond(), second.speedMetersPerSecond(), 0);
        }
        assertTrue(first.finished());
        assertEquals(30, first.replayTimeSeconds(), 1e-6);
        // Fixture moves 0.0003 degrees north, using a spherical Earth radius of 6,371 km.
        assertEquals(33.35848, first.distanceMeters(), 0.001);
        double finalDistance = first.distanceMeters();
        first.reset();
        assertFalse(first.finished());
        assertEquals(0, first.replayTimeSeconds());
        assertEquals(0, first.distanceMeters());
        assertEquals(0, first.speedMetersPerSecond());
        advance(first, 310);
        assertEquals(finalDistance, first.distanceMeters(), 0);
    }

    @Test void pauseFreezesTimelineAndResumeContinuesWithoutDistanceJump() throws IOException {
        var feed = fixture();
        advance(feed, 50);
        double time = feed.replayTimeSeconds();
        double distance = feed.distanceMeters();
        assertTrue(feed.speedMetersPerSecond() > 0);
        feed.setPaused(true);
        advance(feed, 100);
        assertEquals(time, feed.replayTimeSeconds(), 0);
        assertEquals(distance, feed.distanceMeters(), 0);
        assertEquals(0, feed.speedMetersPerSecond());
        feed.setPaused(false);
        feed.update(0.1f);
        assertEquals(time + 0.1f, feed.replayTimeSeconds(), 1e-8);
        assertEquals(0.11119493, feed.distanceMeters() - distance, 1e-5);
        assertTrue(feed.speedMetersPerSecond() > 0);
    }

    @Test void duplicateAndOutOfOrderTimestampsAreRejectedByXmlParser() {
        for (String endTime : List.of("2026-10-08T00:00:00Z", "2026-10-07T23:59:59Z")) {
            assertThrows(IllegalArgumentException.class,
                    () -> parse("10.0001", "106", endTime), endTime);
        }
    }

    @Test void invalidCoordinatesAreRejectedByXmlParser() {
        for (String latitude : List.of("91", "-91", "NaN", "Infinity", "invalid")) {
            assertThrows(IllegalArgumentException.class,
                    () -> parse(latitude, "106", "2026-10-08T00:00:10Z"), latitude);
        }
        for (String longitude : List.of("181", "-181", "NaN", "Infinity", "invalid")) {
            assertThrows(IllegalArgumentException.class,
                    () -> parse("10", longitude, "2026-10-08T00:00:10Z"), longitude);
        }
    }

    @Test void zeroNegativeAndNonfiniteDeltaLeaveEveryObservableUnchanged() throws IOException {
        var feed = fixture();
        advance(feed, 20);
        double time = feed.replayTimeSeconds();
        double distance = feed.distanceMeters();
        double speed = feed.speedMetersPerSecond();
        for (float dt : new float[]{0, -0.1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            feed.update(dt);
            assertEquals(time, feed.replayTimeSeconds(), 0);
            assertEquals(distance, feed.distanceMeters(), 0);
            assertEquals(speed, feed.speedMetersPerSecond(), 0);
            assertFalse(feed.finished());
        }
    }

    @Test void stationaryTrackCompletesWithoutMotion() {
        var feed = new GpxReplayFeed(List.of(
                new GpxReplayFeed.Point(10, 106, START),
                new GpxReplayFeed.Point(10, 106, START.plusSeconds(10))));
        for (int i = 0; i < 110; i++) {
            feed.update(0.1f);
            assertEquals(0, feed.distanceMeters());
            assertEquals(0, feed.speedMetersPerSecond());
        }
        assertTrue(feed.finished());
    }

    @Test void slowFrameDeltaIsClampedRatherThanFastForwardingReplay() throws IOException {
        var slowFrame = fixture();
        var reference = fixture();
        for (int i = 0; i < 25; i++) {
            slowFrame.update(5f);
            reference.update(0.1f);
        }
        assertEquals(2.5, slowFrame.replayTimeSeconds(), 1e-6);
        assertEquals(reference.distanceMeters(), slowFrame.distanceMeters(), 0);
        assertEquals(reference.speedMetersPerSecond(), slowFrame.speedMetersPerSecond(), 0);
        assertFalse(slowFrame.finished());
    }

    private static GpxReplayFeed parse(String latitude, String longitude, String endTime) {
        String xml = "<gpx xmlns='http://www.topografix.com/GPX/1/1'><trk><trkseg>"
                + "<trkpt lat='10' lon='106'><time>2026-10-08T00:00:00Z</time></trkpt>"
                + "<trkpt lat='" + latitude + "' lon='" + longitude + "'><time>" + endTime
                + "</time></trkpt></trkseg></trk></gpx>";
        return GpxReplayFeed.fromGpx(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
