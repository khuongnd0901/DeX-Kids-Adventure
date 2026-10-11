package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DayPeriodTest {
    @Test void allLocalHourBoundariesSelectTheCorrectPeriod() {
        for (int hour = 0; hour < 24; hour++) {
            DayPeriod expected = hour < 5 || hour >= 18 ? DayPeriod.EVENING :
                hour < 11 ? DayPeriod.MORNING : hour < 14 ? DayPeriod.NOON : DayPeriod.AFTERNOON;
            assertEquals(expected, DayPeriod.atHour(hour), "hour=" + hour);
        }
        assertThrows(IllegalArgumentException.class, () -> DayPeriod.atHour(-1));
        assertThrows(IllegalArgumentException.class, () -> DayPeriod.atHour(24));
    }
}
