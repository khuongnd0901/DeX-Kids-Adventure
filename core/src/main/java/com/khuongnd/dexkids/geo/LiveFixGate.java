package com.khuongnd.dexkids.geo;

/**
 * Only use a stable sequence of actual GPS samples for named-place messages.
 * This guards one-off GPS jumps; it is NOT map matching / proof a road was passed.
 */
public final class LiveFixGate {
    private JourneyPosition last;
    public boolean accept(JourneyPosition current) {
        if (current == null || current.simulated() || !current.sufficientQuality()) {
            last = null;
            return false;
        }
        JourneyPosition prev = last;
        last = current;
        if (prev == null || current.sampleId() <= prev.sampleId()) return false;
        long dt = current.sampleId() - prev.sampleId();
        if (dt < 700 || dt > 12_000) return false;
        double distance = GeoDistance.meters(prev.latitude(),prev.longitude(),
                current.latitude(),current.longitude());
        return distance <= 120 && distance <= 55.0 * dt / 1000.0 + 18.0;
    }
    public void reset() { last = null; }
}
