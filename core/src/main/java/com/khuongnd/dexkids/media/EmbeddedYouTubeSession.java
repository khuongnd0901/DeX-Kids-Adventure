package com.khuongnd.dexkids.media;

/** Single terminal transition; callbacks from a disposed player never affect its successor. */
public final class EmbeddedYouTubeSession {
    public enum State { LOADING, PLAYING, PAUSED, BUFFERING, ENDED, ERROR, BLOCKED, CANCELLED }
    private final long generation;
    private State state = State.LOADING;
    private boolean played;
    private boolean finished;
    public EmbeddedYouTubeSession(long generation) { this.generation = generation; }
    public boolean matchesGeneration(String value) { return Long.toString(generation).equals(value); }
    public State state() { return state; }
    public boolean finished() { return finished; }
    /** Returns true only for the first accepted terminal event. */
    public boolean event(long source, String event) {
        if (source != generation || finished || event == null) return false;
        switch (event) {
            case "playing": played = true; state = State.PLAYING; return false;
            case "paused": state = State.PAUSED; return false;
            case "buffering": state = State.BUFFERING; return false;
            case "ended": if (!played) return false; state = State.ENDED; break;
            case "error": state = State.ERROR; break;
            case "blocked": state = State.BLOCKED; break;
            case "cancelled": state = State.CANCELLED; break;
            default: return false;
        }
        finished = true;
        return true;
    }
}
