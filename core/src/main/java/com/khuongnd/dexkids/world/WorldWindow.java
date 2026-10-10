package com.khuongnd.dexkids.world;

/**
 * Fixed ring cache for procedural chunks. No HashMap boxing, removeIf scan or
 * allocations on the normal render frame (when the current chunk is unchanged).
 * Evicted chunks are deterministic and regenerated on re-entry.
 */
public final class WorldWindow {
    private final ProceduralWorldGenerator generator;
    private final int behind;
    private final int ahead;
    private final WorldChunk[] slots;
    private long left = 1;
    private long right = 0;
    private long preparedFor;
    private boolean prepared;

    public WorldWindow(ProceduralWorldGenerator generator, int behind, int ahead) {
        if (generator == null || behind < 0 || ahead < 0 || behind + ahead > 32)
            throw new IllegalArgumentException("Invalid window");
        this.generator = generator;
        this.behind = behind;
        this.ahead = ahead;
        this.slots = new WorldChunk[behind + ahead + 1];
    }

    public void prepare(long currentChunk) {
        if (prepared && preparedFor == currentChunk) return;
        left = currentChunk - behind;
        right = currentChunk + ahead;
        // Index-based ring preserves reused overlapping chunks when moving +/-1.
        for (long id = left; id <= right; id++) {
            int position = (int) Math.floorMod(id, slots.length);
            if (slots[position] == null || slots[position].index() != id)
                slots[position] = generator.generate(id);
        }
        prepared = true;
        preparedFor = currentChunk;
    }

    public WorldChunk get(long chunkId) {
        if (!prepared || chunkId < left || chunkId > right)
            throw new IllegalArgumentException("Chunk outside preloaded window");
        WorldChunk candidate = slots[(int) Math.floorMod(chunkId, slots.length)];
        if (candidate == null || candidate.index() != chunkId)
            throw new IllegalStateException("Chunk cache slot mismatch");
        return candidate;
    }

    public int size() { return prepared ? slots.length : 0; }
}
