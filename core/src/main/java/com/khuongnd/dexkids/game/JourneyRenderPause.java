package com.khuongnd.dexkids.game;

/** Freeze simulated journey/animation while same-display parent controls are open. */
public final class JourneyRenderPause {
    private JourneyRenderPause() {}
    public static float effectiveDelta(float raw, boolean menuOpen) {
        if (menuOpen) return 0f;
        return Float.isFinite(raw) ? Math.min(0.1f, Math.max(0f, raw)) : 0f;
    }
}
