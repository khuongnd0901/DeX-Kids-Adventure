package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;

/** Surface anchors in the authored illustrations, not geographic measurements. */
public final class ScenePropLayout {
    private ScenePropLayout() {}
    public static final float PERCHED_BIRD_Y=382f;
    /** Fit visible atlas pixels in their existing envelope without stretching the silhouette. */
    public static float fitWidth(float maxWidth,float maxHeight,int sourceWidth,int sourceHeight) {
        if (!(maxWidth>0) || !(maxHeight>0) || sourceWidth<=0 || sourceHeight<=0)
            throw new IllegalArgumentException("Positive prop dimensions required");
        return Math.min(maxWidth,maxHeight*sourceWidth/sourceHeight);
    }
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
