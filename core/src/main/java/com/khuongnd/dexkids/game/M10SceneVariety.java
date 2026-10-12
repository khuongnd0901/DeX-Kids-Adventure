package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;
import java.util.Arrays;

/**
 * Render-thread-only bounded selection of existing scenery assets.
 * No new world generator, GPS state, allocation or network access per frame.
 * Reuses the legacy biome pools in CartoonSprites; one seed per trip.
 */
final class M10SceneVariety {
    private final long tripSeed;
    private final long[] cachedKey = new long[16];
    private final int[] cachedChoice = new int[16];
    private final int[] lastEight = new int[8];
    private int nextCache, nextRecent;
    private long previousWindow = Long.MIN_VALUE;

    M10SceneVariety(long tripSeed) {
        this.tripSeed = tripSeed;
        reset();
    }

    /** Called once per render window; replay rewind starts a fresh sequence. */
    void beginVisibleWindow(long firstChunk) {
        if (previousWindow != Long.MIN_VALUE && firstChunk < previousWindow) reset();
        previousWindow = firstChunk;
    }

    void reset() {
        Arrays.fill(cachedKey, Long.MIN_VALUE);
        Arrays.fill(lastEight, -1);
        nextCache = nextRecent = 0;
        previousWindow = Long.MIN_VALUE;
    }

    int pick(long chunk, Biome biome, int[] pool) {
        if (biome == null || pool == null || pool.length == 0)
            throw new IllegalArgumentException("M10 biome/asset pool required");
        final long key = (chunk << 4) ^ biome.ordinal();
        for (int i=0; i<cachedKey.length; i++)
            if (cachedKey[i] == key) return cachedChoice[i];
        final int offset = Math.floorMod(mix(tripSeed ^ chunk ^ ((long)biome.ordinal() << 40)), pool.length);
        int best = -1;
        // Bounded probe: skip any of the last 8 selections when there are alternatives.
        for (int j=0; j<pool.length; j++) {
            int selected = pool[(offset+j)%pool.length];
            int signature = biome.ordinal()*256 + selected;
            boolean recent = false;
            for (int prior : lastEight) if (prior == signature) { recent = true; break; }
            if (!recent) { best = selected; break; }
        }
        if (best < 0) {
            // All available candidates are on cooldown. Repeat legally, not spin forever.
            best = pool[offset];
        }
        cachedKey[nextCache] = key;
        cachedChoice[nextCache] = best;
        nextCache = (nextCache + 1) % cachedKey.length;
        lastEight[nextRecent] = biome.ordinal()*256+best;
        nextRecent = (nextRecent+1) % lastEight.length;
        return best;
    }

    private static int mix(long x) {
        x ^= x >>> 33;
        x *= 0xff51afd7ed558ccdL;
        x ^= x >>> 33;
        x *= 0xc4ceb9fe1a85ec53L;
        x ^= x >>> 33;
        return (int)x;
    }
}
