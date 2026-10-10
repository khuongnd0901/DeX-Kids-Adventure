package com.khuongnd.dexkids.game;

/**
 * Canonical 1920x1080 composition. All passengers share the same bus transform,
 * and wheel centers are derived from actual 660x290 source artwork geometry.
 */
public final class VehicleLayout {
    private VehicleLayout() {}
    public static final float X=610f, Y=188f, WIDTH=950f, HEIGHT=417f;
    public static final float WHEEL_RADIUS=66f;
    public static final float CAPYBARA_X=1218f, CAPYBARA_Y=361f;
    public static final float CAPYBARA_W=145f, CAPYBARA_H=164f;
    public static final float SAU_X=712f, ONG_X=878f;
    public static final float CHILD_Y=355f, CHILD_W=128f, CHILD_H=188f;

    public static float wheelCenterX(int wheel) {
        if (wheel != 0 && wheel != 1) throw new IllegalArgumentException("wheel");
        return X + (wheel == 0 ? 188f : 499f) / 660f * WIDTH;
    }
    public static float wheelCenterY() { return Y + (290f - 237f) / 290f * HEIGHT; }
    public static float sourceAspectError() {
        return Math.abs(WIDTH / HEIGHT - 660f / 290f);
    }
}
