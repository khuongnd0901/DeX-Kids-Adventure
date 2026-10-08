
package com.khuongnd.dexkids.world;

import java.util.SplittableRandom;

/**
 * Deterministic stylized scenery, not a real location or a claim about a POI.
 * M4 will supply verified GeoContext and nearby POIs.
 */
public final class ProceduralWorldGenerator {
    private static final Biome[] BIOMES = Biome.values();
    private final long journeySeed;

    public ProceduralWorldGenerator(long journeySeed) {
        this.journeySeed = journeySeed;
    }

    public WorldChunk generate(long index) {
        long mixed = mix64(journeySeed ^ (index * 0x9E3779B97F4A7C15L));
        SplittableRandom random = new SplittableRandom(mixed);
        return new WorldChunk(index, BIOMES[random.nextInt(BIOMES.length)],
                random.nextInt());
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }
}
