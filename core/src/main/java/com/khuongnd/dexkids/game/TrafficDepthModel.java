package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;

/** Fictional traffic, never inferred from GPS. Two reusable slots, no frame allocations or sorting. */
public final class TrafficDepthModel {
    public enum Lane {
        FAR(250f, .74f, 0), NEAR(95f, 1f, 2);
        public final float groundY, scale;
        final double delay;
        Lane(float groundY, float scale, double delay) {
            this.groundY = groundY; this.scale = scale; this.delay = delay;
        }
    }
    public static final double SLOT_SECONDS = 22;
    private static final float[] SPEED = {220, 235, 185, 205, 245, 145};
    private final TrafficLayout far = new TrafficLayout(), near = new TrafficLayout();
    private final int seed;
    private final boolean storybook;
    private double seconds;
    private long farSlot = Long.MIN_VALUE, nearSlot = Long.MIN_VALUE;
    private boolean farEnabled, nearEnabled;

    public TrafficDepthModel(int seed, boolean storybook) {
        this.seed = seed; this.storybook = storybook;
    }
    public TrafficLayout far() { return far; }
    public TrafficLayout near() { return near; }
    public static float speed(int kind, Lane lane) { return SPEED[kind] * (lane == Lane.FAR ? .9f : 1f); }
    public static boolean allows(Biome biome) {
        return biome == Biome.URBAN || biome == Biome.RESIDENTIAL || biome == Biome.GENERAL;
    }
    public void advance(float delta, Biome biome) {
        if (!Float.isFinite(delta) || delta < 0 || biome == null) throw new IllegalArgumentException("traffic time/biome");
        seconds += delta;
        update(far, Lane.FAR, biome);
        update(near, Lane.NEAR, biome);
    }
    private void update(TrafficLayout out, Lane lane, Biome biome) {
        double time = seconds - lane.delay;
        long slot = (long)Math.floor(time / SLOT_SECONDS);
        // Latch eligibility only while a new vehicle is wholly offscreen. A theme or
        // chunk transition cannot delete a passing vehicle or change its depth/type.
        if (lane == Lane.FAR && slot != farSlot) { farSlot = slot; farEnabled = allows(biome); }
        if (lane == Lane.NEAR && slot != nearSlot) { nearSlot = slot; nearEnabled = allows(biome); }
        int kind = Math.floorMod(slot + seed + (lane == Lane.FAR ? 3 : 0), SceneryCast.TRAFFIC.length);
        float width = TrafficLayout.height(kind, lane) * 360f / 185f;
        float left = (float)(-width - 8 + (time - slot * SLOT_SECONDS) * speed(kind, lane));
        out.place(kind, lane, left, time >= 0 && (lane == Lane.FAR ? farEnabled : nearEnabled), storybook);
    }
}
