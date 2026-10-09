package com.khuongnd.dexkids.journey;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class BoundedGpxInputStreamTest {
    @Test void acceptsExactLimitAndDetectsTrailingBytes() throws Exception {
        try (var input = new BoundedGpxInputStream(new ByteArrayInputStream(new byte[]{1,2,3}), 3)) {
            assertArrayEquals(new byte[]{1,2,3}, input.readAllBytes());
        }
        try (var input = new BoundedGpxInputStream(new ByteArrayInputStream(new byte[]{1,2,3,4}), 3)) {
            assertThrows(IOException.class, input::readAllBytes);
        }
        try (var input = new BoundedGpxInputStream(new ByteArrayInputStream(new byte[]{1,2}), 1)) {
            assertEquals(1, input.read());
            assertThrows(IOException.class, input::read);
        }
    }
    @Test void unsafeXmlAndExcessPointLimitAreRejected() {
        var payload = "<gpx xmlns='http://www.topografix.com/GPX/1/1'><trk><trkseg>"
                + "<trkpt lat='10' lon='106'><time>2026-10-08T00:00:00Z</time></trkpt>".repeat(20_001)
                + "</trkseg></trk></gpx>";
        assertThrows(IllegalArgumentException.class, () ->
                GpxReplayFeed.fromGpx(new ByteArrayInputStream(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }
}
