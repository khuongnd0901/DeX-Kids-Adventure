package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TripPackRetryPolicyTest {
    @Test void exactlyFiveAdditionalRetries() {
        assertEquals(6,TripPackRetryPolicy.MAX_ATTEMPTS);
        long[] delays={5000,15000,35000,75000,150000};
        for(int i=0;i<5;i++) assertEquals(delays[i],TripPackRetryPolicy.delayAfterFailure(i,0));
        assertThrows(IllegalArgumentException.class,()->TripPackRetryPolicy.delayAfterFailure(5,0));
        assertThrows(IllegalArgumentException.class,()->TripPackRetryPolicy.delayAfterFailure(-1,0));
    }
    @Test void retryAfterIsHardMinimum() {
        assertEquals(90000,TripPackRetryPolicy.delayAfterFailure(0,90000));
        assertEquals(15000,TripPackRetryPolicy.delayAfterFailure(1,3000));
    }
}
