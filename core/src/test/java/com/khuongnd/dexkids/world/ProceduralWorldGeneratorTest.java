
package com.khuongnd.dexkids.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProceduralWorldGeneratorTest {
    @Test void sameSeedAndIndexProduceSameChunk() {
        var a = new ProceduralWorldGenerator(123);
        var b = new ProceduralWorldGenerator(123);
        for (int i = -100; i < 100; i++) assertEquals(a.generate(i), b.generate(i));
    }

    @Test void neighborsAreNotAllIdentical() {
        var generator = new ProceduralWorldGenerator(123);
        assertNotEquals(generator.generate(0), generator.generate(1));
    }

    @Test void indexHandlesLargeAndNegativeValues() {
        var generator = new ProceduralWorldGenerator(444);
        assertNotNull(generator.generate(-100_000));
        assertNotNull(generator.generate(100_000_000));
    }
}
