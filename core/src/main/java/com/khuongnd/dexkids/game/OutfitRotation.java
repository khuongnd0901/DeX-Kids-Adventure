package com.khuongnd.dexkids.game;

import java.util.Random;

/**
 * Session-seeded, time-bucketed child outfit choice.
 *
 * No frame-to-frame randomness: every outfit lasts an entire four-minute
 * simulated interval. Sâu and Ong use independent, reproducible sequences.
 * A six-outfit circular traversal shows all designs before one repeats.
 * No mutable state is changed while rendering and no frame-time allocations.
 */
final class OutfitRotation {
    static final int COUNT = 6;
    static final int CHANGE_SECONDS = 240;
    private final int sauStart;
    private final int ongStart;
    private final int sauDirection;
    private final int ongDirection;

    OutfitRotation(long sessionSeed) {
        Random random = new Random(sessionSeed);
        sauStart = random.nextInt(COUNT);
        ongStart = random.nextInt(COUNT);
        sauDirection = random.nextBoolean() ? 1 : -1;
        ongDirection = random.nextBoolean() ? 1 : -1;
    }

    int sau(float elapsedSeconds) {
        return pick(sauStart, sauDirection, elapsedSeconds);
    }

    int ong(float elapsedSeconds) {
        return pick(ongStart, ongDirection, elapsedSeconds);
    }

    private static int pick(int start, int direction, float elapsedSeconds) {
        // LibGDX clocks are finite and nonnegative in normal use; defend tests
        // and temporary lifecycle pauses without ever selecting a bogus region.
        if (!Float.isFinite(elapsedSeconds) || elapsedSeconds < 0f) elapsedSeconds = 0f;
        long bucket = (long) (elapsedSeconds / CHANGE_SECONDS);
        int phase = (int) (bucket % COUNT);
        return Math.floorMod(start + phase * direction, COUNT);
    }
}
