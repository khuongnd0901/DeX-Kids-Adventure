package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AiRequestLimiterTest {
    @Test void fivePhysicalRequestsPerRollingMinuteWithTwelveSecondSpacing() {
        var limiter = new AiRequestLimiter(5, 60_000, 12_000);
        assertTrue(limiter.tryAcquire(0));
        assertFalse(limiter.tryAcquire(0)); // second provider / concurrent caller also counted
        assertFalse(limiter.tryAcquire(11_999));
        for (long at : new long[]{12_000,24_000,36_000,48_000})
            assertTrue(limiter.tryAcquire(at));
        assertFalse(limiter.tryAcquire(59_999));
        assertTrue(limiter.tryAcquire(60_000)); // request at 0 is outside the rolling window
    }

    @Test void retryAfterBlocksAllCallers() {
        var limiter = new AiRequestLimiter(5, 60_000, 12_000);
        assertTrue(limiter.tryAcquire(100));
        limiter.observeRetryAfter(200, 60);
        assertFalse(limiter.tryAcquire(60_199));
        assertTrue(limiter.tryAcquire(60_200));
    }

    @Test void rejectedClockRewindCannotResetBudget() {
        var limiter = new AiRequestLimiter(5, 60_000, 12_000);
        assertTrue(limiter.tryAcquire(40_000));
        assertFalse(limiter.tryAcquire(20_000));
        assertFalse(limiter.tryAcquire(40_001));
    }
}
