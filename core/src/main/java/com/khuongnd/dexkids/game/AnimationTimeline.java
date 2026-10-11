package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.LocalVisualPlanner;
import com.khuongnd.dexkids.story.VisualPlan;
import com.khuongnd.dexkids.story.VisualCue;

/** Bounded frame-timed animation state; only render thread mutates it. */
public final class AnimationTimeline {
    public enum Phase { QUESTION, ANSWER, INTERRUPTED }
    public record Event(String beatId, Phase phase) {
        public Event { if (beatId == null || phase == null) throw new IllegalArgumentException("event"); }
    }

    private final LocalVisualPlanner planner = new LocalVisualPlanner();
    private final AnimationRegistry registry = new AnimationRegistry();
    private String beatId = "";
    private VisualPlan plan = planner.plan("welcome", VisualCue.DEFAULT);
    private Phase phase = Phase.INTERRUPTED;
    private float elapsed;

    public void prompt(String id, VisualCue cue) {
        if (id == null || cue == null) return;
        if (!id.equals(beatId)) {
            VisualPlan suggested = planner.plan(id.isBlank() ? "welcome" : id, cue);
            // Defensive capability gate even though planner output is allowlisted.
            plan = registry.supports(AnimationRegistry.Actor.ILLUSTRATION,
                    suggested.questionMotion(),cue.prop()) || cue.prop() == VisualCue.Prop.NONE
                    ? suggested : new VisualPlan(cue,VisualPlan.Motion.IDLE,
                            VisualPlan.Motion.CELEBRATE,suggested.variation());
            beatId = id;
            phase = Phase.QUESTION;
            elapsed = 0f;
        }
    }
    public void signal(Event event) {
        if (event == null) return;
        if (event.phase() == Phase.INTERRUPTED) {
            phase = Phase.INTERRUPTED;
            elapsed = 0f;
        } else if (event.beatId().equals(beatId) && phase != event.phase()) {
            phase = event.phase();
            elapsed = 0f;
        }
    }
    public void advance(float delta) {
        if (!Float.isFinite(delta) || delta < 0) throw new IllegalArgumentException("delta");
        if (phase != Phase.INTERRUPTED) elapsed += Math.min(delta, 0.1f);
    }
    public Phase phase() { return phase; }
    public VisualPlan plan() { return plan; }
    public float elapsed() { return elapsed; }
    public float x() {
        if (phase == Phase.INTERRUPTED) return 0f;
        float t = elapsed + plan.variation() * .7f;
        if (phase == Phase.ANSWER) return (float)Math.sin(t * 4) * 5f;
        return switch (plan.questionMotion()) {
            case FLY, DRIVE, SAIL -> (float)Math.sin(t * 1.3f) * 32f;
            case FLOAT -> (float)Math.sin(t * .7f) * 13f;
            default -> 0f;
        };
    }
    public float y() {
        if (phase == Phase.INTERRUPTED) return 0f;
        float t = elapsed + plan.variation() * .5f;
        if (phase == Phase.ANSWER) return Math.abs((float)Math.sin(t * 4f)) * 28f;
        return switch (plan.questionMotion()) {
            case FLY -> (float)Math.sin(t * 2.1f) * 24f;
            case FLOAT, SAIL -> (float)Math.sin(t * 1.4f) * 9f;
            case BOUNCE -> Math.abs((float)Math.sin(t * 2.5f)) * 15f;
            default -> 0f;
        };
    }
    public float scale() {
        if (phase == Phase.INTERRUPTED) return 1f;
        if (phase == Phase.ANSWER) return 1f + .08f * Math.abs((float)Math.sin(elapsed * 4));
        return plan.questionMotion() == VisualPlan.Motion.GROW
                ? 1f + .07f * Math.abs((float)Math.sin(elapsed * 1.3f)) : 1f;
    }
    public StoryCompanionPose pose(StoryCompanionPose original) {
        if (phase == Phase.ANSWER) return StoryCompanionPose.WAVE;
        return original;
    }
}
