package com.khuongnd.dexkids.game;

/** Visual-only PNG mapping; does not mutate the narration/motion controller. */
public enum CapybaraArtPose {
    IDLE("capybara_idle"), LAUGH("capybara_laugh"), ASK("capybara_ask"), WAVE("capybara_wave");
    public final String region;
    CapybaraArtPose(String region) { this.region = region; }

    public static CapybaraArtPose resolve(CharacterAnimationController.Frame frame,
                                         String beatId, AnimationTimeline.Phase phase) {
        // Existing greeting/wave motion remains visible; conversation uses the
        // existing semantic question/answer events, with no child grading.
        if (frame == CharacterAnimationController.Frame.WAVE || "welcome".equals(beatId)) return WAVE;
        if (beatId != null && !beatId.isBlank() && phase != AnimationTimeline.Phase.INTERRUPTED)
            return phase == AnimationTimeline.Phase.ANSWER ? LAUGH : ASK;
        return frame == CharacterAnimationController.Frame.SURPRISED ? ASK : IDLE;
    }

    /** Short bounded fade, driven by the same paused render delta as motion. */
    public static final class Transition {
        public static final float SECONDS = .18f;
        private CapybaraArtPose from = IDLE, current = IDLE;
        private float elapsed = SECONDS;
        public void advance(CapybaraArtPose next, float delta) {
            if (next == null || !Float.isFinite(delta) || delta < 0) throw new IllegalArgumentException("pose/delta");
            if (delta == 0) return;
            // Complete a short fade before accepting another target; no third
            // overlapping pose or per-frame allocation on rapid cue changes.
            if (next != current && elapsed >= SECONDS) {
                from = current; current = next; elapsed = 0;
            }
            elapsed = Math.min(SECONDS, elapsed + delta);
        }
        public CapybaraArtPose from() { return from; }
        public CapybaraArtPose current() { return current; }
        public float alpha() { float t = elapsed / SECONDS; return t * t * (3 - 2 * t); }
    }
}
