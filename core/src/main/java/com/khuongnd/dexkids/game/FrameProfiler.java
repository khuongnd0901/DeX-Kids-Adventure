package com.khuongnd.dexkids.game;

import java.util.Arrays;

/** Fixed-size, allocation-free samples on the render thread; snapshots allocate only on demand. */
public final class FrameProfiler {
    private final float[] samples;
    private int count;
    private int cursor;
    private double totalMs;

    public FrameProfiler(int capacity) {
        if (capacity < 2) throw new IllegalArgumentException("capacity must be >= 2");
        samples = new float[capacity];
    }

    public void record(float deltaSeconds) {
        if (!Float.isFinite(deltaSeconds) || deltaSeconds <= 0) return;
        float milliseconds = deltaSeconds * 1000f;
        if (count == samples.length) totalMs -= samples[cursor];
        else count++;
        samples[cursor] = milliseconds;
        totalMs += milliseconds;
        cursor = (cursor + 1) % samples.length;
    }

    public int size() { return count; }

    public double averageFps() {
        return count == 0 || totalMs <= 0 ? 0 : 1000.0 * count / totalMs;
    }

    public double p95FrameMs() {
        if (count == 0) return 0;
        float[] ordered = Arrays.copyOf(samples, count);
        Arrays.sort(ordered);
        return ordered[Math.max(0, (int) Math.ceil(0.95 * count) - 1)];
    }

    public void reset() {
        count = cursor = 0;
        totalMs = 0;
        Arrays.fill(samples, 0);
    }
}
