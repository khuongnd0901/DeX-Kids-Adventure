package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;

/** Reproducible fictional companions and road props; NEVER actual GPS traffic. */
public final class SceneryCast {
    public static final String[] FRIENDS = {"friend_rabbit", "friend_fox", "friend_panda",
            "friend_cat", "friend_penguin"};
    public static final String[] TRAFFIC = {"traffic_car", "traffic_taxi", "traffic_truck",
            "traffic_minibus", "traffic_scooter", "traffic_bicycle"};
    private SceneryCast() {}

    public static boolean showFriend(long chunk, Biome biome) {
        if (biome == null) throw new IllegalArgumentException("Biome required");
        return biome != Biome.RIVER && biome != Biome.BRIDGE && Math.floorMod(chunk, 3) == 0;
    }

    public static boolean showTraffic(long chunk, Biome biome) {
        if (biome == null) throw new IllegalArgumentException("Biome required");
        return switch (biome) {
            case URBAN, RESIDENTIAL, GENERAL -> Math.floorMod(chunk, 2) == 1;
            case PARK, RIVER, BRIDGE, COUNTRYSIDE -> false;
        };
    }

    public static int friendIndex(long chunk, int seed) {
        return Math.floorMod(mix(seed, chunk), FRIENDS.length);
    }

    public static int trafficIndex(long chunk, int seed) {
        return Math.floorMod(mix(seed ^ 0x45D9F3B, chunk), TRAFFIC.length);
    }

    private static int mix(int seed, long chunk) {
        long x = chunk ^ (seed * 0x9E3779B97F4A7C15L);
        x ^= x >>> 29;
        x *= 0x165667B19E3779F9L;
        x ^= x >>> 32;
        return (int) x;
    }
}
