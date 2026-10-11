package com.khuongnd.dexkids.story;

/** T-030 deterministic application-level retry ceiling; physical HTTP calls have a separate limiter. */
public final class TripPackRetryPolicy {
    private TripPackRetryPolicy() {}
    public static final int MAX_ATTEMPTS = 6; // initial plus five retries
    private static final long[] BACKOFF_MS = {5_000,15_000,35_000,75_000,150_000};
    /** failedAttempt: 0 means initial attempt failed; 5 means exhausted. */
    public static long delayAfterFailure(int failedAttempt, long retryAfterMs) {
        if (failedAttempt < 0 || failedAttempt >= MAX_ATTEMPTS-1)
            throw new IllegalArgumentException("Retry limit exhausted");
        return Math.max(BACKOFF_MS[failedAttempt],Math.max(0,retryAfterMs));
    }
}
