package com.khuongnd.dexkids.story;

import java.util.Objects;

/** Safe, local animation instructions; no arbitrary AI resources, GPS, or textures. */
public record VisualPlan(VisualCue cue, Motion questionMotion, Motion answerMotion, int variation) {
    public enum Motion { IDLE, WAVE, LOOK_UP, COUNT, PET, FLY, FLOAT, SAIL, DRIVE, GROW, BOUNCE, CELEBRATE }
    public VisualPlan {
        Objects.requireNonNull(cue);
        Objects.requireNonNull(questionMotion);
        Objects.requireNonNull(answerMotion);
        if (variation < 0 || variation >= 4) throw new IllegalArgumentException("variation");
    }
}
