package com.khuongnd.dexkids.game;

/** Reused screen-space bounds. Source wheel contacts include tire stroke, not canvas padding. */
public final class TrafficLayout {
    private static final float[] HEIGHT = {160, 160, 225, 215, 115, 105};
    private static final float[] CONTACT = {172, 172, 174, 174, 171.5f, 180};
    private static final float[] CLASSIC_CONTACT = {170.5f, 170.5f, 170.5f, 170.5f, 170.5f, 185};
    public int kind;
    public float x, y, width, height, groundY;
    public boolean visible;

    public static float height(int kind, TrafficDepthModel.Lane lane) {
        return HEIGHT[kind] * lane.scale;
    }
    public static float contact(int kind, boolean storybook) {
        return (storybook ? CONTACT : CLASSIC_CONTACT)[kind] / 185f;
    }
    void place(int kind, TrafficDepthModel.Lane lane, float left, boolean enabled, boolean storybook) {
        this.kind = kind;
        groundY = lane.groundY;
        height = height(kind, lane);
        width = height * 360f / 185f; // both source packs use identical native canvases
        x = left;
        y = groundY - height * (1f - contact(kind, storybook));
        visible = enabled && x + width > 0 && x < 1920;
    }
}
