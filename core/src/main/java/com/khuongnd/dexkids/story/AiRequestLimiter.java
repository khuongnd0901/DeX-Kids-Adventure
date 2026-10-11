package com.khuongnd.dexkids.story;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Shared in-process throttle for physical HTTPS starts (not generated questions).
 * Fail closed on monotonic clock anomalies; does not claim a provider daily budget.
 */
public final class AiRequestLimiter {
    public static final AiRequestLimiter SHARED = new AiRequestLimiter(5, 60_000, 12_000);
    private final int maximum;
    private final long windowMs;
    private final long spacingMs;
    private final Deque<Long> startedAt = new ArrayDeque<>();
    private long lastClockMs = -1;
    private long blockedUntilMs = 0;

    public AiRequestLimiter(int maximum, long windowMs, long spacingMs) {
        if (maximum < 1 || windowMs < 1 || spacingMs < 1
            || spacingMs > windowMs || maximum > 100)
            throw new IllegalArgumentException("Invalid request budget");
        this.maximum = maximum;
        this.windowMs = windowMs;
        this.spacingMs = spacingMs;
    }

    public synchronized boolean tryAcquire(long elapsedRealtimeMs) {
        if (elapsedRealtimeMs < 0 || elapsedRealtimeMs < lastClockMs)
            return false;
        lastClockMs = elapsedRealtimeMs;
        while (!startedAt.isEmpty() && elapsedRealtimeMs - startedAt.peekFirst() >= windowMs)
            startedAt.removeFirst();
        if (elapsedRealtimeMs < blockedUntilMs
            || startedAt.size() >= maximum
            || (!startedAt.isEmpty() && elapsedRealtimeMs - startedAt.peekLast() < spacingMs))
            return false;
        startedAt.addLast(elapsedRealtimeMs);
        return true;
    }

    /** 429 cooldown shared across all providers, failovers and newly constructed gateways. */
    public synchronized void observeRetryAfter(long elapsedRealtimeMs, long retrySeconds) {
        if (elapsedRealtimeMs < 0 || elapsedRealtimeMs < lastClockMs) return;
        lastClockMs = elapsedRealtimeMs;
        long delayMs = Math.max(1L, Math.min(3_600L, retrySeconds)) * 1000L;
        long end = elapsedRealtimeMs > Long.MAX_VALUE - delayMs
            ? Long.MAX_VALUE : elapsedRealtimeMs + delayMs;
        blockedUntilMs = Math.max(blockedUntilMs, end);
    }
}
