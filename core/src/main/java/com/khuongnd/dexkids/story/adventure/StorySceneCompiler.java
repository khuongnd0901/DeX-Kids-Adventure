package com.khuongnd.dexkids.story.adventure;

import com.khuongnd.dexkids.story.VisualCue;

/** Pure local compilation. No asset discovery, I/O, HTTP or text inference at playback. */
public final class StorySceneCompiler {
    public record CompiledBeat(String id, String caption, String focus, VisualCue cue,
                               StoryEpisode.Emotion emotion) {}
    public CompiledBeat compile(StoryEpisode episode, int scene, int beat) {
        var s = episode.scenes().get(scene);
        var b = s.beats().get(beat);
        String focus = b.actor() == StoryEpisode.Actor.CAPYBARA
                ? episode.audience().name() : b.actor().name();
        return new CompiledBeat("story-" + episode.id() + "-r" + episode.revision() + "-" + scene + "-" + beat,
                b.utterance(), focus, new VisualCue(s.backdrop(), b.action(), b.prop()), b.emotion());
    }
}
