package com.khuongnd.dexkids.game;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class WorldMoodResolverTest {
    @Test void resolvesDayDuskAndNightBoundaries() {
        var r = new WorldMoodResolver();
        assertEquals(WorldMoodResolver.Mood.NIGHT, r.resolve(LocalTime.of(19, 0)));
        assertEquals(WorldMoodResolver.Mood.DUSK, r.resolve(LocalTime.of(17, 0)));
        assertEquals(WorldMoodResolver.Mood.DAY, r.resolve(LocalTime.of(12, 0)));
        assertEquals(WorldMoodResolver.Mood.NIGHT, r.resolve(LocalTime.of(5, 59)));
    }
}
