package com.khuongnd.dexkids.session;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionTimeLimitTest {
    @Test void limitAccumulatesOnlyRunningTime() {
        var limit = new SessionTimeLimit(1);
        assertFalse(limit.tick(30));
        limit.setPaused(true);
        assertFalse(limit.tick(300));
        assertEquals(30, limit.remainingSeconds());
        limit.setPaused(false);
        assertTrue(limit.tick(30));
        assertEquals(0, limit.remainingSeconds());
    }
}
