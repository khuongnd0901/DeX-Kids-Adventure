package com.khuongnd.dexkids.game;

/**
 * Canonical 1920x1080 composition. All passengers share the same bus transform,
 * and wheel centers are derived from actual 660x290 source artwork geometry.
 */
public final class VehicleLayout {
    private VehicleLayout() {}
    public static final float X=130f, Y=188f, WIDTH=950f, HEIGHT=417f;
    // Trimmed T-048 bus canvas: 1432x1010. Same road baseline, no stretching.
    public static final float PNG_WIDTH=850f, PNG_HEIGHT=PNG_WIDTH*1010f/1432f;
    public static final float PNG_CHILD_HEIGHT=120f;
    public static final float PNG_CHILD_Y=Y+(1029f-600f)/1010f*PNG_HEIGHT;
    public static final float PNG_SAU_X=X+145f/1432f*PNG_WIDTH;
    public static final float PNG_ONG_X=X+456f/1432f*PNG_WIDTH;
    public static final float PNG_CHILD_WIDTH=140f;
    public static final float PNG_CAPYBARA_X=990f, PNG_CAPYBARA_Y=Y;
    public static final float PNG_CAPYBARA_H=190f, PNG_CAPYBARA_W=PNG_CAPYBARA_H*224f/288f;
    public static final float WHEEL_RADIUS=66f;
    public static final float CAPYBARA_X=X+748f, CAPYBARA_Y=365f;
    public static final float CAPYBARA_W=120f, CAPYBARA_H=148f;
    public static final float SAU_X=X+102f, ONG_X=X+268f;
    public static final float CHILD_Y=367f, CHILD_W=128f, CHILD_H=152f;

    /** Tire baseline from trimmed T-048 canvas or independently rendered legacy wheels. */
    public static float groundY(boolean png) {
        return png ? Y : wheelCenterY() - WHEEL_RADIUS;
    }

    public static float wheelCenterX(int wheel) {
        if (wheel != 0 && wheel != 1) throw new IllegalArgumentException("wheel");
        return X + (wheel == 0 ? 188f : 499f) / 660f * WIDTH;
    }
    public static float wheelCenterY() { return Y + (290f - 237f) / 290f * HEIGHT; }
    public static float sourceAspectError() {
        return Math.abs(WIDTH / HEIGHT - 660f / 290f);
    }
}
