package com.khuongnd.dexkids.world;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BiomeDistrictTest {
    @Test void consecutiveChunksShareBiomeForLongerStoryTransitions() {
        var generator = new ProceduralWorldGenerator(20261008L);
        for(long start = -80; start < 80; start += 4) {
            Biome districtBiome = generator.generate(start).biome();
            for(long i = start + 1; i < start + 4; i++)
                assertEquals(districtBiome, generator.generate(i).biome());
        }
    }
    @Test void distinctChunksRemainDeterministicDespiteGrouping() {
        var a = new ProceduralWorldGenerator(41L);
        var b = new ProceduralWorldGenerator(41L);
        for(long id = -15; id <= 15; id++) assertEquals(a.generate(id), b.generate(id));
        assertNotEquals(a.generate(0).detailSeed(), a.generate(1).detailSeed());
    }
}
