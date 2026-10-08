package com.khuongnd.dexkids.game;

/** Pure-time animation machine, independent of libGDX rendering or GPS polling. */
public final class CharacterAnimationController {
    public enum State { IDLE, ROLLING, TALKING, WAVING, SLEEPING }
    private State state = State.IDLE;
    private double stateTime;
    private double elapsed;

    public void setState(State next) {
        if (next == null) throw new IllegalArgumentException("Null state");
        if (next != state) {
            state = next;
            stateTime = 0;
        }
    }
    public void advance(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0)
            throw new IllegalArgumentException("Bad delta");
        stateTime += seconds;
        elapsed += seconds;
    }
    public State state() { return state; }
    public double stateTime() { return stateTime; }
    public double elapsedTotal() { return elapsed; }
    public boolean isBlinking() {
        return state != State.SLEEPING && (stateTime % 4.3) > 4.17;
    }
    public int talkingFrame() {
        return state == State.TALKING ? (int) Math.floor(stateTime * 5.0) % 2 : 0;
    }
}
