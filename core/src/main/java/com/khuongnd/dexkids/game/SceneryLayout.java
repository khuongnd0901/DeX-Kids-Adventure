package com.khuongnd.dexkids.game;

/** Pixel positioning is derived from the absolute journey distance and stable chunk index. */
public final class SceneryLayout {
    public static final float CHUNK_WIDTH = 640f;
    private SceneryLayout() {}
    public static long firstChunk(double distanceMeters) {
        if (!Double.isFinite(distanceMeters) || distanceMeters < 0)
            throw new IllegalArgumentException("distance");
        return (long) Math.floor(distanceMeters * 7.5 / CHUNK_WIDTH);
    }
    public static float left(long chunkIndex, double distanceMeters) {
        long first = firstChunk(distanceMeters);
        double scrollPx = distanceMeters * 7.5;
        double offsetPx = scrollPx - first * CHUNK_WIDTH;
        return (float) ((chunkIndex - first) * CHUNK_WIDTH - offsetPx);
    }
    public static float parallaxOffset(double distanceMeters, double multiplier, float tileWidth) {
        if (!Double.isFinite(distanceMeters) || distanceMeters < 0 ||
                !Double.isFinite(multiplier) || multiplier < 0 ||
                !Float.isFinite(tileWidth) || tileWidth <= 0)
            throw new IllegalArgumentException("Parallax input");
        return (float) ((distanceMeters * multiplier) % tileWidth);
    }
}
