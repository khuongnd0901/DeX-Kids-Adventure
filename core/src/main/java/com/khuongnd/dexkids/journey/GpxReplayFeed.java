package com.khuongnd.dexkids.journey;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
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
    private double seconds;
    private double distance;
    private boolean paused;

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
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var doc = factory.newDocumentBuilder().parse(xml);
            NodeList nodes = doc.getElementsByTagNameNS("*", "trkpt");
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

    @Override public void update(float dt) {
        if (!Float.isFinite(dt) || dt <= 0 || paused) return;
        double nextTime = Math.min(seconds + Math.min(dt, 0.1f), elapsed[elapsed.length - 1]);
        double nextDistance = positionAt(nextTime);
        double requested = Math.max(0, (nextDistance - distance) / Math.max(nextTime - seconds, 0.0001));
        speed.update(requested, nextTime - seconds);
        seconds = nextTime;
        distance = nextDistance;
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

    public void setPaused(boolean value) { paused = value; if (value) speed.reset(); }
    public boolean finished() { return seconds >= elapsed[elapsed.length - 1]; }
    public double replayTimeSeconds() { return seconds; }
    public void reset() { seconds = 0; distance = 0; speed.reset(); paused = false; }
    @Override public double distanceMeters() { return distance; }
    @Override public double speedMetersPerSecond() { return speed.value(); }
    @Override public boolean isDemo() { return true; }
}
