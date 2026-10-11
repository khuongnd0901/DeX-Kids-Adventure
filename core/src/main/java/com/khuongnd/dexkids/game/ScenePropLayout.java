package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;

/** Surface anchors in the authored illustrations, not geographic measurements. */
public final class ScenePropLayout {
    private ScenePropLayout() {}
    public static Biome biome(String key, Biome fallback) {
        if (key == null) return fallback;
        return switch (key) {
            case "vungtau_beach", "nha_trang_beach", "dalat_lake" -> Biome.RIVER;
            default -> fallback;
        };
    }
    public static float waterY(String key) {
        if (key == null) return 341f;
        return switch (key) {
            case "vungtau_beach", "nha_trang_beach" -> 520f;
            case "dalat_lake" -> 430f;
            default -> 341f;
        };
    }
}
