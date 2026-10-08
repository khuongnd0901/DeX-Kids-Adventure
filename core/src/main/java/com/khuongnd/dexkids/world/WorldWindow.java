package com.khuongnd.dexkids.world;

import java.util.HashMap;
import java.util.Map;

/** Bounded chunk window; deterministic chunks survive eviction and re-entry. */
public final class WorldWindow {
    private final ProceduralWorldGenerator generator;
    private final int behind;
    private final int ahead;
    private final Map<Long, WorldChunk> chunks = new HashMap<>();
    private long left = 1;
    private long right = 0;

    public WorldWindow(ProceduralWorldGenerator generator, int behind, int ahead) {
        if (generator == null || behind < 0 || ahead < 0 || behind + ahead > 32)
            throw new IllegalArgumentException("Invalid window");
        this.generator = generator;
        this.behind = behind;
        this.ahead = ahead;
    }

    public void prepare(long currentChunk) {
        left = currentChunk - behind;
        right = currentChunk + ahead;
        chunks.keySet().removeIf(k -> k < left || k > right);
        for (long id = left; id <= right; id++)
            chunks.computeIfAbsent(id, generator::generate);
    }

    public WorldChunk get(long chunkId) {
        if (chunkId < left || chunkId > right)
            throw new IllegalArgumentException("Chunk outside preloaded window");
        return chunks.get(chunkId);
    }

    public int size() { return chunks.size(); }
}
