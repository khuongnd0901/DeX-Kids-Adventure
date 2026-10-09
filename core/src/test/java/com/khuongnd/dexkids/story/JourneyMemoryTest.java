package com.khuongnd.dexkids.story;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JourneyMemoryTest {
    @Test void contentVariantCanRotateWithoutSavingLocation() {
        var m = new JourneyMemory();
        assertEquals(0, m.variantIndex("demo", 3));
        assertEquals(1, m.recordVisit("demo"));
        assertEquals(1, m.variantIndex("demo", 3));
        assertEquals(2, m.recordVisit("demo"));
        assertEquals(2, m.variantIndex("demo", 3));
        m.clear();
        assertEquals(0, m.trackedPoiCount());
    }
}
