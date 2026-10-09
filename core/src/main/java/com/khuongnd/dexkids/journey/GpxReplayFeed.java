package com.khuongnd.dexkids.journey;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Time-based offline GPX journey. Invalid speed leaps are ignored in motion distance;
 * GPS coordinates are never fed directly to sprites.
 */
public final class GpxReplayFeed implements JourneyFeed {
    public record Point(double lat, double lon, Instant at) {
        public Point {
            if (!Double.isFinite(lat) || lat < -90 || lat > 90 || !Double.isFinite(lon)
                    || lon < -180 || lon > 180 || at == null)
                throw new IllegalArgumentException("Invalid GPX point");
        }
    }
    private static final double MAX_PLAUSIBLE_SPEED = 55; // m/s, generous passenger-car limit
    private final List<Point> points;
    private final double[] elapsed;
    private final double[] cumulative;
    private final SpeedSmoother speed = new SpeedSmoother();
    // Read by Android control labels; only LibGDX render thread mutates replay state.
    private volatile double seconds;
    private volatile double distance;
    private volatile boolean paused;

    public GpxReplayFeed(List<Point> input) {
        if (input == null || input.size() < 2)
            throw new IllegalArgumentException("At least 2 track points needed");
        this.points = List.copyOf(input);
        elapsed = new double[points.size()];
        cumulative = new double[points.size()];
        Instant first = points.get(0).at();
        for (int i = 1; i < points.size(); i++) {
            elapsed[i] = (points.get(i).at().toEpochMilli() - first.toEpochMilli()) / 1000.0;
            if (elapsed[i] <= elapsed[i - 1])
                throw new IllegalArgumentException("GPX timestamps must increase");
            double d = haversineMeters(points.get(i - 1), points.get(i));
            double secondsBetween = elapsed[i] - elapsed[i - 1];
            // Ignore implausible jumps in projected distance, not just on-screen sprites.
            cumulative[i] = cumulative[i - 1] + (d / secondsBetween <= MAX_PLAUSIBLE_SPEED ? d : 0);
        }
    }

    public static GpxReplayFeed fromGpx(InputStream xml) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // Android's Harmony DOM factory rejects Xerces security feature URIs.
            // Reject declarations BEFORE the parser sees any XML, on the same decoded
            // character stream it will parse. Never silently ignore a security option.
            String content = readSafeXml(xml);
            var builder = factory.newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> {
                throw new SAXException("External XML entities are forbidden");
            });
            var doc = builder.parse(new InputSource(new StringReader(content)));
            NodeList nodes = doc.getElementsByTagNameNS("*", "trkpt");
            if (nodes.getLength() > 20_000)
                throw new IllegalArgumentException("GPX contains too many track points");
            ArrayList<Point> parsed = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element e = (Element) nodes.item(i);
                NodeList timestamps = e.getElementsByTagNameNS("*", "time");
                if (timestamps.getLength() != 1) throw new IllegalArgumentException("Missing GPX time");
                parsed.add(new Point(Double.parseDouble(e.getAttribute("lat")),
                        Double.parseDouble(e.getAttribute("lon")),
                        Instant.parse(timestamps.item(0).getTextContent().trim())));
            }
            return new GpxReplayFeed(parsed);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or unsafe GPX content", e);
        }
    }

    private static String readSafeXml(InputStream xml) throws Exception {
        var bytes = new ByteArrayOutputStream();
        var bounded = new BoundedGpxInputStream(xml);
        byte[] buffer = new byte[8192];
        int count;
        while ((count = bounded.read(buffer)) != -1) bytes.write(buffer, 0, count);
        byte[] data = bytes.toByteArray();
        Charset charset = StandardCharsets.UTF_8;
        int offset = 0;
        if (data.length >= 2 && (data[0] & 255) == 0xfe && (data[1] & 255) == 0xff) {
            charset = StandardCharsets.UTF_16BE; offset = 2;
        } else if (data.length >= 2 && (data[0] & 255) == 0xff && (data[1] & 255) == 0xfe) {
            charset = StandardCharsets.UTF_16LE; offset = 2;
        } else if (data.length >= 3 && (data[0] & 255) == 0xef
                && (data[1] & 255) == 0xbb && (data[2] & 255) == 0xbf) {
            offset = 3;
        } else if (data.length >= 4 && data[0] == 0 && data[1] == '<' && data[2] == 0 && data[3] == '?') {
            charset = StandardCharsets.UTF_16BE;
        } else if (data.length >= 4 && data[0] == '<' && data[1] == 0 && data[2] == '?' && data[3] == 0) {
            charset = StandardCharsets.UTF_16LE;
        }
        String content = charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(data, offset, data.length - offset)).toString();
        // XML keywords are case-sensitive. This intentionally also rejects declaration
        // text inside comments/CDATA; GPX has no need for either DTD or custom entities.
        if (content.indexOf('\0') >= 0 || content.contains("<!DOCTYPE") || content.contains("<!ENTITY"))
            throw new IllegalArgumentException("DTD and entity declarations are forbidden in GPX");
        return content;
    }

    @Override public void update(float dt) {
        if (!Float.isFinite(dt) || dt <= 0 || paused) return;
        double nextTime = Math.min(seconds + Math.min(dt, 0.1f), elapsed[elapsed.length - 1]);
        double nextDistance = positionAt(nextTime);
        double requested = Math.max(0, (nextDistance - distance) / Math.max(nextTime - seconds, 0.0001));
        speed.update(requested, nextTime - seconds);
        seconds = nextTime;
        distance = nextDistance;
        // No movement remains at end-of-route; do not freeze the previous smoothed speed.
        if (finished()) speed.reset();
    }

    private double positionAt(double time) {
        for (int i = 1; i < elapsed.length; i++)
            if (time <= elapsed[i]) {
                double fraction = (time - elapsed[i - 1]) / (elapsed[i] - elapsed[i - 1]);
                return cumulative[i - 1] + (cumulative[i] - cumulative[i - 1]) * Math.max(0, fraction);
            }
        return cumulative[cumulative.length - 1];
    }

    private static double haversineMeters(Point a, Point b) {
        double deltaLat = Math.toRadians(b.lat() - a.lat());
        double deltaLon = Math.toRadians(b.lon() - a.lon());
        double h = Math.pow(Math.sin(deltaLat / 2), 2) +
                Math.cos(Math.toRadians(a.lat())) * Math.cos(Math.toRadians(b.lat())) *
                Math.pow(Math.sin(deltaLon / 2), 2);
        return 2 * 6371000.0 * Math.asin(Math.min(1, Math.sqrt(h)));
    }

    /** Invoke from render thread via Gdx.app.postRunnable; UI reads volatile getters. */
    public void setPaused(boolean value) { paused = value; if (value) speed.reset(); }
    public boolean isPaused() { return paused; }
    public double totalTimeSeconds() { return elapsed[elapsed.length - 1]; }
    public boolean finished() { return seconds >= elapsed[elapsed.length - 1]; }
    public double replayTimeSeconds() { return seconds; }
    public void reset() { seconds = 0; distance = 0; speed.reset(); paused = false; }
    @Override public java.util.Optional<com.khuongnd.dexkids.geo.JourneyPosition> position(long nowMillis) {
        if (points.size() < 2) return java.util.Optional.empty();
        int segment = 1;
        while (segment < elapsed.length - 1 && seconds > elapsed[segment]) segment++;
        double segmentSeconds = elapsed[segment] - elapsed[segment - 1];
        double segmentMeters = haversineMeters(points.get(segment - 1), points.get(segment));
        // Never expose a fabricated location inside a rejected teleport segment.
        if (segmentSeconds <= 0 || segmentMeters / segmentSeconds > MAX_PLAUSIBLE_SPEED)
            return java.util.Optional.empty();
        double t = Math.max(0.0, Math.min(1.0, (seconds - elapsed[segment - 1]) / segmentSeconds));
        Point a = points.get(segment - 1), b = points.get(segment);
        double lat = a.lat() + t * (b.lat() - a.lat());
        double lon = a.lon() + t * (b.lon() - a.lon());
        return java.util.Optional.of(new com.khuongnd.dexkids.geo.JourneyPosition(
                lat, lon, 8.0f, (float)speedMetersPerSecond(),
                Math.max(1, (long)(seconds * 1000.0) + 1), true));
    }

    @Override public double distanceMeters() { return distance; }
    @Override public double speedMetersPerSecond() { return speed.value(); }
    @Override public boolean isDemo() { return true; }
}
