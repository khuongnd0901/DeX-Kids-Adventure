package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue.Action;

/** Normalized actor-relative attachment bounds. No face overlap, independent of hero/outfit scale. */
public final class StoryInteractionLayout {
    public record Box(float x,float y,float width,float height) {}
    private static final Box BOOK=new Box(.14f,.34f,.72f,.27f);
    private static final Box OFFER=new Box(.45f,.28f,.48f,.27f);
    private static final Box PLANT=new Box(.82f,-.04f,.50f,.30f);
    private static final Box SPECIMEN=new Box(.78f,.15f,.36f,.22f);
    public static Box primary(Action action) {
        return switch(action) {case READ->BOOK;case SHARE->OFFER;case WATER->PLANT;case INSPECT->SPECIMEN;default->throw new IllegalArgumentException("No attached action");};
    }
    public static float offerOffset(float elapsed) {return (float)(.14*(.5-.5*Math.cos(phase(elapsed)*Math.PI)));}
    public static float phase(float elapsed) {return Float.isFinite(elapsed)?(float)(Math.max(0d,(double)elapsed)%4d):0f;}
    private StoryInteractionLayout(){}
}
