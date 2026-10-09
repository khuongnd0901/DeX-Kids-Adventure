package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.journey.DemoJourneyFeed;
import com.khuongnd.dexkids.world.ProceduralWorldGenerator;
import com.khuongnd.dexkids.world.WorldWindow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimulationSoakTest {
    /**
     * Fast virtual 60-minute simulation. NOT a GPU, battery, thermal or device soak.
     * This checks monotonic distance and bounded chunk count only.
     */
    @Test void simulatedSixtyMinuteTripNeverExpandsChunkCacheUnboundedly() {
        var journey = new DemoJourneyFeed();
        var chunks = new WorldWindow(new ProceduralWorldGenerator(20261008L), 1, 4);
        double last = 0;
        for (int frame = 0; frame < 60 * 60 * 30; frame++) {
            journey.update(1f / 30);
            assertTrue(journey.distanceMeters() >= last);
            last = journey.distanceMeters();
            long chunk = (long) Math.floor(last * 7.5 / 640);
            chunks.prepare(chunk);
            assertEquals(6, chunks.size());
            assertNotNull(chunks.get(chunk));
        }
        assertTrue(last > 10000);
    }
}
