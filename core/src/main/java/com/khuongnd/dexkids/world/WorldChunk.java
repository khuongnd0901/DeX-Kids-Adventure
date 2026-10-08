
package com.khuongnd.dexkids.world;

/** World data is independent of libGDX and can be replayed from seed and index. */
public record WorldChunk(long index, Biome biome, int detailSeed) {}
