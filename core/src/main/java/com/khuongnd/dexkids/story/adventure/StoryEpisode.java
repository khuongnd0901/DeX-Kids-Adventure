package com.khuongnd.dexkids.story.adventure;

import com.khuongnd.dexkids.story.VisualCue;
import java.util.List;

/** Immutable content only. Review/parent approval applies to an exact persisted revision. */
public record StoryEpisode(String id, int schemaVersion, int revision, String title,
        Audience audience, int sauAge, int ongAge, String educationalGoal,
        String synopsis, List<Scene> scenes) {
    public static final int SCHEMA_VERSION = 1;
    public enum Audience { SAU, ONG, BOTH }
    public enum Actor { SAU, ONG, CAPYBARA }
    public enum Emotion { HAPPY, CURIOUS, CALM, SURPRISED }
    public StoryEpisode { scenes=List.copyOf(scenes); }
    public int learningAge() { return audience==Audience.SAU?sauAge:ongAge; }
    public record Scene(String id, VisualCue.Theme backdrop, List<Beat> beats, Question question) {
        public Scene { beats=List.copyOf(beats); }
    }
    public record Beat(Actor actor, String utterance, Emotion emotion,
                       VisualCue.Action action, VisualCue.Prop prop) {}
    public record Question(Audience target, String prompt, List<String> acceptedConcepts,
            String hint, String feedback, String silenceFallback, int maxWaitSeconds) {
        public Question { acceptedConcepts=List.copyOf(acceptedConcepts); }
    }
}
