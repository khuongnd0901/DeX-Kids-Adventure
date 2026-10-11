package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AiRequestLimiterTest {
    @Test void tenPhysicalRequestsPerRollingMinuteWithSixSecondSpacing() {
        var limiter = new AiRequestLimiter(10, 60_000, 6_100);
        assertTrue(limiter.tryAcquire(0));
        assertFalse(limiter.tryAcquire(0)); // second provider / concurrent caller also counted
        assertFalse(limiter.tryAcquire(6_099));
        for (long at : new long[]{6_100,12_200,18_300,24_400,30_500,36_600,42_700,48_800,54_900})
            assertTrue(limiter.tryAcquire(at));
        assertFalse(limiter.tryAcquire(59_999));
        assertFalse(limiter.tryAcquire(60_000)); // earliest slot expired, spacing still blocks
        assertTrue(limiter.tryAcquire(61_000)); // ten starts in rolling window, >=6100ms apart
    }

    @Test void retryAfterBlocksAllCallers() {
        var limiter = new AiRequestLimiter(10, 60_000, 6_100);
        assertTrue(limiter.tryAcquire(100));
        limiter.observeRetryAfter(200, 60);
        assertFalse(limiter.tryAcquire(60_199));
        assertTrue(limiter.tryAcquire(60_200));
    }

    @Test void rejectedClockRewindCannotResetBudget() {
        var limiter = new AiRequestLimiter(10, 60_000, 6_100);
        assertTrue(limiter.tryAcquire(40_000));
        assertFalse(limiter.tryAcquire(20_000));
        assertFalse(limiter.tryAcquire(40_001));
    }
}
