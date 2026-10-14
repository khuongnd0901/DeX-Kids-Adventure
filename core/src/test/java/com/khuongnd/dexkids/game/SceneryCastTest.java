package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class SceneryCastTest {
    @Test void friendAndVehicleSelectionsAreStableAndDiverse() {
        Set<Integer> friends = new HashSet<>(), vehicles = new HashSet<>();
        for(long chunk = -250; chunk < 250; chunk++) {
            int a=SceneryCast.friendIndex(chunk, 12345);
            int b=SceneryCast.trafficIndex(chunk, 12345);
            assertEquals(a, SceneryCast.friendIndex(chunk, 12345));
            assertEquals(b, SceneryCast.trafficIndex(chunk, 12345));
            assertTrue(a >= 0 && a < SceneryCast.FRIENDS.length);
            assertTrue(b >= 0 && b < SceneryCast.TRAFFIC.length);
            friends.add(a);
            vehicles.add(b);
        }
        assertEquals(SceneryCast.FRIENDS.length, friends.size());
        assertEquals(SceneryCast.TRAFFIC.length, vehicles.size());
    }
    @Test void t049TrafficIsLargerAndKeepsAspectRatio() {
        for (int i = 0; i < SceneryCast.TRAFFIC.length; i++) {
            float height = SceneryCast.trafficHeight(i);
            float originalWidth = i >= 4 ? 230f : 275f;
            assertTrue(height > 140f, "T-049 must enlarge traffic art");
            float width = height * 360f / 185f;
            assertTrue(width > originalWidth);
            assertEquals(360f / 185f, width / height, 1e-5f);
            assertTrue(265f + width < SceneryLayout.CHUNK_WIDTH, "vehicle crosses chunk boundary");
            assertTrue(110f + height < 300f, "traffic overlaps roadside character area");
        }
        assertThrows(IllegalArgumentException.class, () -> SceneryCast.trafficHeight(-1));
        assertThrows(IllegalArgumentException.class, () -> SceneryCast.trafficHeight(6));
    }

    @Test void neverPlaceCharactersInRiverOrOnBridgeOrMotorcarsInParks() {
        for(long chunk = -12; chunk <= 12; chunk++) {
            assertFalse(SceneryCast.showFriend(chunk, Biome.BRIDGE));
            assertFalse(SceneryCast.showFriend(chunk, Biome.RIVER));
            assertFalse(SceneryCast.showTraffic(chunk, Biome.PARK));
            assertFalse(SceneryCast.showTraffic(chunk, Biome.RIVER));
            assertFalse(SceneryCast.showTraffic(chunk, Biome.BRIDGE));
        }
        assertTrue(SceneryCast.showFriend(4, Biome.PARK));
        assertTrue(SceneryCast.showTraffic(1, Biome.URBAN));
        assertFalse(SceneryCast.showTraffic(2, Biome.URBAN));
    }
}
