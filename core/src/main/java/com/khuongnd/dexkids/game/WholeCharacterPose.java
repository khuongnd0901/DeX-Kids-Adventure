package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue.Action;
import com.khuongnd.dexkids.story.adventure.StoryEpisode.Emotion;

/** Authored complete paintings. Only the whole illustration moves; no body-part transforms. */
public final class WholeCharacterPose {
    public static final int COUNT = 48;
    public static final int FRAMES_PER_PAGE = 4;
    public record Frame(int index, float x, float y, float rotation) {
        public int page() { return index / FRAMES_PER_PAGE; }
    }
    public static Frame sample(Action action, Emotion emotion, boolean speaking, float elapsed) {
        double t = Float.isFinite(elapsed) ? Math.max(0d, elapsed) % 8d : 0;
        float wave = (float)Math.sin(t * Math.PI * 2);
        int alternate = (int)(t * 2) % 2;
        int index = switch (action) {
            case IDLE, TALK -> 32 + emotion.ordinal();
            case WAVE -> 1 + alternate;
            case LOOK_UP -> 3;
            case COUNT -> 4;
            case PET -> 5;
            case JUMP -> 6 + alternate;
            case DANCE -> 8 + alternate;
            case BOW -> 10;
            case READ -> 11;
            case WATER -> 12;
            case SHARE -> 13;
            case INSPECT -> 14;
            case SIT -> 15;
            case WALK -> 16 + alternate;
            case RUN -> 18 + alternate;
            case THINK -> 20;
            case POINT -> 21;
            case NOD -> 22;
            case SHAKE_HEAD -> 23;
            case CLAP -> 24 + alternate;
            case PICK_UP -> 26;
            case PUT_DOWN -> 27;
            case CARRY -> 28;
            case DRAW -> 29;
            case EAT -> 30;
            case DRINK -> 31;
        };
        if ((action == Action.IDLE || action == Action.TALK) && speaking &&
                (emotion == Emotion.HAPPY || emotion == Emotion.CALM)) index = 46 + alternate;
        float x = switch(action) {
            case DANCE -> wave * 5;
            case SHAKE_HEAD -> wave * 2;
            default -> 0;
        };
        float y = switch(action) {
            case JUMP -> Math.max(0, wave) * 24;
            case WALK -> Math.abs(wave) * 2;
            case RUN -> Math.abs(wave) * 5;
            case TALK -> wave;
            default -> 0;
        };
        // A small rigid tilt supports a drawn bow/nod; the painting is never stretched.
        float rotation = action == Action.NOD ? wave * 1.2f : 0;
        return new Frame(index, x, y, rotation);
    }
    private WholeCharacterPose() {}
}
