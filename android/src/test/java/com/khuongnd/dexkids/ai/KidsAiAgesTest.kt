package com.khuongnd.dexkids.ai

import org.junit.Assert.*
import org.junit.Test

class KidsAiAgesTest {
    @Test fun sharedAgeUsesYoungerChildInEitherOrder() {
        assertEquals(4,KidsAiAges(7,4).shared)
        assertEquals(4,KidsAiAges(4,7).shared)
        assertEquals(6,KidsAiAges(6,7).shared)
    }
    @Test fun instructionsCarryBothConfiguredAges() {
        assertEquals("Sâu age 7, Ong age 6; shared BOTH content targets age 6.",KidsAiAges(7,6).instruction())
    }
    @Test fun rejectsUnsupportedAgeBounds() {
        for(pair in listOf(3 to 4,4 to 8,8 to 4,5 to 3)) {
            try {KidsAiAges(pair.first,pair.second);fail("Unsupported age accepted")}
            catch(_:IllegalArgumentException) {}
        }
    }
}
