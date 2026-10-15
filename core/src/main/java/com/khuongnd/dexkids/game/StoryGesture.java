package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue;

/** Finite sprite gestures, preserving original faces/clothes; sampled on the existing beat clock. */
public final class StoryGesture {
    public record Transform(float x, float y, float rotation) {}
    // Precomputed immutable tables avoid allocating on the render thread.
    private static final Transform[][] SAMPLES = new Transform[VisualCue.Action.values().length][120];
    static {
        for(var action:VisualCue.Action.values())for(int i=0;i<120;i++) {
            float t=i/30f;
            SAMPLES[action.ordinal()][i]=switch(action) {
                case WAVE -> new Transform(0,Math.abs((float)Math.sin(t*3))*6,(float)Math.sin(t*3)*3);
                case LOOK_UP -> new Transform(0,8+(float)Math.sin(t*2)*3,-2);
                case COUNT -> new Transform((float)Math.sin(t*2)*5,Math.abs((float)Math.sin(t*2))*7,0);
                case PET -> new Transform(8+(float)Math.sin(t*2)*7,-5,3+(float)Math.sin(t*2)*2);
            };
        }
    }
    public static Transform sample(VisualCue.Action action,float elapsed) {
        int index=Float.isFinite(elapsed)?(int)(Math.max(0,elapsed)*30)%120:0;
        return SAMPLES[action.ordinal()][index];
    }
    private StoryGesture(){}
}
