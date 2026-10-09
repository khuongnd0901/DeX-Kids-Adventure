package com.khuongnd.dexkids.world;

import com.khuongnd.dexkids.game.SceneryLayout;
import com.khuongnd.dexkids.journey.GpxReplayFeed;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Synthetic core GPX replay; no measured GPS, real POIs, rendering, or runtime benchmark. */
class WorldReplayMatrixTest {
    @Test void world001Through006HundredsOfReplayChunksAreBoundedAndResetDeterministically() {
        var replay = syntheticReplay();
        var generator = new ProceduralWorldGenerator(20261008L);
        var window = new WorldWindow(generator, 1, 4);
        var first = trace(replay, generator, window);
        assertTrue(first.size() >= 100, "Must cross at least 100 actual replay chunk boundaries");
        var biomes = EnumSet.noneOf(Biome.class);
        first.forEach(chunk -> biomes.add(chunk.biome()));
        assertEquals(EnumSet.allOf(Biome.class), biomes, "All seven fictional biomes covered");
        replay.reset();
        assertEquals(0, replay.distanceMeters());
        assertEquals(first, trace(replay, generator, window), "Reset must reproduce detail seeds and biomes");
    }

    private static List<WorldChunk> trace(GpxReplayFeed replay,
            ProceduralWorldGenerator generator, WorldWindow window) {
        List<WorldChunk> trace = new ArrayList<>();
        long previous = -1;
        for (int step = 0; !replay.finished() && step < 9000; step++) {
            double before = replay.distanceMeters();
            replay.update(0.1f);
            long current = SceneryLayout.firstChunk(replay.distanceMeters());
            assertTrue(current >= previous && current <= previous + 1,
                    "Replay must not skip a scenery chunk");
            assertTrue(replay.distanceMeters() >= before);
            window.prepare(current);
            assertEquals(6, window.size(), "Data window remains bounded, not a GPU allocation assertion");
            assertSame(window.get(current), window.get(current), "Repeated access reuses resident data");
            assertEquals(SceneryLayout.CHUNK_WIDTH,
                    SceneryLayout.left(current + 1, replay.distanceMeters())
                            - SceneryLayout.left(current, replay.distanceMeters()), 0.001);
            if (current != previous) {
                WorldChunk chunk = window.get(current);
                assertEquals(generator.generate(current), chunk);
                if (current > 0 && current % 4 != 0)
                    assertEquals(trace.get(trace.size() - 1).biome(), chunk.biome());
                if (current > 0 && current % 4 == 0) {
                    // District boundaries may select the same biome; decoration identity stays stable.
                    assertEquals(generator.generate(current - 1), window.get(current - 1));
                    assertEquals(generator.generate(current), chunk);
                }
                trace.add(chunk);
                previous = current;
            }
        }
        assertTrue(replay.finished(), "Synthetic route must reach its endpoint within the step bound");
        return trace;
    }

    private static GpxReplayFeed syntheticReplay() {
        // Explicit synthetic coordinates, never a claimed physical route. ~27.8 m/s is below 55 m/s guard.
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        return new GpxReplayFeed(List.of(new GpxReplayFeed.Point(0, 0, start),
                new GpxReplayFeed.Point(0, 0.2, start.plusSeconds(800))));
    }
}
