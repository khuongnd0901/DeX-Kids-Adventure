package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

class OutfitRotationTest {
    @Test void holdsSameCostumeThroughoutBucketWithoutFrameFlicker() {
        OutfitRotation rotation = new OutfitRotation(20261014L);
        assertEquals(rotation.sau(0f), rotation.sau(239.99f));
        assertEquals(rotation.ong(0f), rotation.ong(239.99f));
        assertNotEquals(rotation.sau(239.99f), rotation.sau(240f));
        assertNotEquals(rotation.ong(239.99f), rotation.ong(240f));
        assertEquals(rotation.sau(240f), rotation.sau(479.99f));
    }

    @Test void seesAllSixDistinctOutfitsBeforeRepeat() {
        OutfitRotation rotation = new OutfitRotation(42L);
        Set<Integer> sau = new HashSet<>(), ong = new HashSet<>();
        for (int i = 0; i < OutfitRotation.COUNT; i++) {
            int a = rotation.sau(i * OutfitRotation.CHANGE_SECONDS);
            int b = rotation.ong(i * OutfitRotation.CHANGE_SECONDS);
            assertTrue(a >= 0 && a < OutfitRotation.COUNT);
            assertTrue(b >= 0 && b < OutfitRotation.COUNT);
            sau.add(a);
            ong.add(b);
        }
        assertEquals(OutfitRotation.COUNT, sau.size());
        assertEquals(OutfitRotation.COUNT, ong.size());
        assertEquals(rotation.sau(0), rotation.sau(OutfitRotation.COUNT * OutfitRotation.CHANGE_SECONDS));
        assertEquals(rotation.ong(0), rotation.ong(OutfitRotation.COUNT * OutfitRotation.CHANGE_SECONDS));
    }

    @Test void sessionSeedControlsRandomSelectionButIsReproducible() {
        OutfitRotation first = new OutfitRotation(5L);
        OutfitRotation again = new OutfitRotation(5L);
        for (int t = 0; t < 4000; t += 17) {
            assertEquals(first.sau(t), again.sau(t));
            assertEquals(first.ong(t), again.ong(t));
        }
    }

    @Test void independentChildrenAndRandomSessionStartsCoverAllDesigns() {
        Set<Integer> starts = new HashSet<>();
        boolean differing = false;
        for (long seed = 0; seed < 1000; seed++) {
            OutfitRotation r = new OutfitRotation(seed);
            starts.add(r.sau(0));
            differing |= r.sau(0) != r.ong(0) || r.sau(240) != r.ong(240);
        }
        assertEquals(6, starts.size());
        assertTrue(differing, "Children must not share a locked rotation");
    }

    @Test void invalidAndNegativeElapsedTimeSafelyUseFirstOutfit() {
        OutfitRotation r = new OutfitRotation(150L);
        assertEquals(r.sau(0f), r.sau(-45f));
        assertEquals(r.ong(0f), r.ong(Float.NaN));
        assertEquals(r.sau(0f), r.sau(Float.POSITIVE_INFINITY));
    }
}
