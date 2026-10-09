package com.khuongnd.dexkids.game;

/** Bounded whole-run GL cadence telemetry. P95 is a 1ms histogram upper bound. */
public final class RenderRunMetrics {
    private final long[] histogram = new long[10001];
    private long frames;
    private double seconds;
    public void record(float delta) {
        if (!Float.isFinite(delta) || delta <= 0) return;
        frames++;
        seconds += delta;
        histogram[Math.min(10000, (int) (delta * 1000.0))]++;
    }
    public long frames() { return frames; }
    public double seconds() { return seconds; }
    public double averageFps() { return seconds > 0 ? frames / seconds : 0; }
    public double p95UpperMs() {
        long rank = (long) Math.ceil(frames * 0.95), count = 0;
        if (frames == 0) return 0;
        for (int ms = 0; ms < histogram.length; ms++) {
            count += histogram[ms];
            if (count >= rank) return ms == 10000 ? Double.POSITIVE_INFINITY : ms + 1;
        }
        throw new IllegalStateException("Missing histogram samples");
    }
}
