package com.khuongnd.dexkids.story.adventure;

import java.util.Objects;

/** UI-thread-owned callback clock. Stale/cancelled TTS completions cannot advance a story.
 * Answers remain ephemeral; caller owns offline ASR and audio focus. No provider is reachable here. */
public final class StoryPlaybackDirector {
    public enum Kind { NARRATION, QUESTION, FEEDBACK }
    public enum State { IDLE, READY, SPEAKING, LISTENING, PAUSED, COMPLETE }
    public sealed interface Event permits Speech, Listen, Finished {}
    public record Speech(long token, String text, Kind kind, int sceneIndex, int beatIndex,
                         StorySceneCompiler.CompiledBeat visual, StoryEpisode.Audience target) implements Event {}
    public record Listen(StoryEpisode.Audience target, int maxWaitSeconds) implements Event {}
    public record Finished() implements Event {}
    /** phase 0=narration, 1=question, 2=listening, 3=feedback, 4=next scene. */
    public record Cursor(int scene, int beat, int phase) {}
    private final StorySceneCompiler compiler = new StorySceneCompiler();
    private StoryEpisode episode;
    private State state = State.IDLE;
    private int scene, beat, phase;
    private long sequence, activeToken;
    private double listeningSeconds;
    private boolean understood;
    private State pausedState;
    public void start(StoryEpisode value) { restore(value, new Cursor(0,0,0)); }
    public void restore(StoryEpisode value, Cursor c) {
        Objects.requireNonNull(value); Objects.requireNonNull(c);
        if (c.scene()<0 || c.scene()>=value.scenes().size() || c.beat()<0
                || c.beat()>=value.scenes().get(c.scene()).beats().size() || c.phase()<0 || c.phase()>4)
            throw new IllegalArgumentException("cursor");
        episode=value; scene=c.scene(); beat=c.beat(); phase=c.phase();
        // Listening restoration speaks the question again; never restart an open microphone.
        if(phase==2)phase=1;
        activeToken=++sequence; state=State.READY; understood=false; listeningSeconds=0;
    }
    public Event next() {
        if(state!=State.READY)return null;
        var s=episode.scenes().get(scene);
        if(phase==4) {
            if(++scene>=episode.scenes().size()) { state=State.COMPLETE;return new Finished(); }
            beat=0;phase=0;s=episode.scenes().get(scene);
        }
        if(phase==0) return speak(s.beats().get(beat).utterance(),Kind.NARRATION,episode.audience());
        var q=s.question();
        if(q==null) {phase=4;return next();}
        if(phase==1)return speak(q.prompt(),Kind.QUESTION,q.target());
        if(phase==2) {state=State.LISTENING;listeningSeconds=0;return new Listen(q.target(),q.maxWaitSeconds());}
        return speak(understood?q.feedback():q.silenceFallback(),Kind.FEEDBACK,q.target());
    }
    private Speech speak(String text,Kind kind,StoryEpisode.Audience target) {
        state=State.SPEAKING;activeToken=++sequence;
        var visual=compiler.compile(episode,scene,beat);
        if(kind!=Kind.NARRATION)visual=new StorySceneCompiler.CompiledBeat(visual.id()+"-"+kind.name().toLowerCase(java.util.Locale.ROOT),text,
                target.name(),new com.khuongnd.dexkids.story.VisualCue(
                        com.khuongnd.dexkids.story.VisualCue.Theme.STORY,
                        com.khuongnd.dexkids.story.VisualCue.Action.WAVE,
                        com.khuongnd.dexkids.story.VisualCue.Prop.NONE),
                StoryEpisode.Emotion.CALM);
        return new Speech(activeToken,text,kind,scene,beat,visual,target);
    }
    public boolean speechCompleted(long token) {
        if(state!=State.SPEAKING || token!=activeToken)return false;
        if(phase==0) {if(++beat>=episode.scenes().get(scene).beats().size()){beat--;phase=1;}}
        else if(phase==1)phase=2;else if(phase==3)phase=4;
        state=State.READY;return true;
    }
    /** Explicit host watchdog/error completion; caller first offers readable caption/Next. */
    public boolean speechFailed(long token) { return speechCompleted(token); }
    public boolean answer(boolean recognizedConcept) {
        if(state!=State.LISTENING)return false;
        understood=recognizedConcept;phase=3;state=State.READY;return true;
    }
    public boolean tickListening(double seconds) {
        if(!Double.isFinite(seconds)||seconds<0)throw new IllegalArgumentException("seconds");
        if(state!=State.LISTENING)return false;
        listeningSeconds+=seconds;
        return listeningSeconds>=episode.scenes().get(scene).question().maxWaitSeconds() && answer(false);
    }
    public void pause() {
        if(state==State.IDLE||state==State.COMPLETE||state==State.PAUSED)return;
        pausedState=state;activeToken=++sequence;state=State.PAUSED;
    }
    public void resume() {
        if(state!=State.PAUSED)return;
        if(pausedState==State.LISTENING){phase=1;listeningSeconds=0;}
        state=State.READY; // replay interrupted utterance, never skip an unheard sentence.
    }
    public void stop(){activeToken=++sequence;state=State.IDLE;episode=null;}
    public State state(){return state;}
    public Cursor cursor(){return new Cursor(scene,beat,phase);}
    public StoryEpisode episode(){return episode;}
}
