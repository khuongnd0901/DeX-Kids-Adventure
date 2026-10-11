package com.khuongnd.dexkids.story;

import java.util.Objects;

/** Maps *validated* lesson cues to allowlisted animation templates entirely offline. */
public final class LocalVisualPlanner {
    public VisualPlan plan(String beatId, VisualCue cue) {
        Objects.requireNonNull(beatId);
        Objects.requireNonNull(cue);
        if (beatId.isBlank() || beatId.length() > 256) throw new IllegalArgumentException("beat id");
        VisualPlan.Motion motion = switch (cue.prop()) {
            case BUTTERFLY, BIRD, KITE -> VisualPlan.Motion.FLY;
            case CLOUD -> VisualPlan.Motion.FLOAT;
            case BOAT -> VisualPlan.Motion.SAIL;
            case VEHICLE -> VisualPlan.Motion.DRIVE;
            case TREE, FLOWER -> VisualPlan.Motion.GROW;
            case CAT, RABBIT -> VisualPlan.Motion.BOUNCE;
            case NONE -> switch (cue.action()) {
                case LOOK_UP -> VisualPlan.Motion.LOOK_UP;
                case COUNT -> VisualPlan.Motion.COUNT;
                case PET -> VisualPlan.Motion.PET;
                case WAVE -> VisualPlan.Motion.WAVE;
            };
        };
        // A different authored/AI beat produces different deterministic motion phase.
        return new VisualPlan(cue, motion, VisualPlan.Motion.CELEBRATE,
                Math.floorMod(beatId.hashCode(), 4));
    }
}
