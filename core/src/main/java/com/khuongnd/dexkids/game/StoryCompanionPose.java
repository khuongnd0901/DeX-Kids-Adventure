package com.khuongnd.dexkids.game;

/** Authored cartoon action, derived from story topic rather than physical GPS. */
public enum StoryCompanionPose {
    WAVE, LOOK_UP, COUNT, PET;
    public static StoryCompanionPose fromCue(com.khuongnd.dexkids.story.VisualCue cue) {
        if (cue == null) return WAVE;
        return switch (cue.action()) {
            case WAVE, JUMP, DANCE, BOW -> WAVE;
            case LOOK_UP, INSPECT -> LOOK_UP;
            case COUNT, READ, SHARE -> COUNT;
            case PET, WATER -> PET;
        };
    }
    public static StoryCompanionPose fromBeat(String id) {
        if (id == null) return WAVE;
        if (id.equals("sau-count") || id.equals("ong-one") || id.equals("both-count")) return COUNT;
        if (id.contains("bird") || id.contains("cloud") || id.contains("weather")) return LOOK_UP;
        if (id.contains("rabbit") || id.contains("cat") || id.contains("fox") ||
            id.contains("panda") || id.contains("animal") || id.contains("friend")) return PET;
        return WAVE;
    }
}
