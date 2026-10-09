package com.khuongnd.dexkids.game;

/** Deterministic, renderer-independent animation state machine. No fake narration. */
public final class CharacterAnimationController {
    public enum State { IDLE, ROLLING, TALKING, WAVING, SLEEPING }
    public enum Frame { IDLE, BLINK, TALK_OPEN, TALK_CLOSED, WAVE, SLEEP, SURPRISED }

    private State state = State.IDLE;
    private double stateTime;
    private double elapsed;
    private double stationarySeconds;
    private double waveRemainingSeconds;
    private boolean narrationActive;
    private boolean surprised;

    public void setNarrationActive(boolean value) { narrationActive = value; }
    public void requestWave() { waveRemainingSeconds = Math.max(waveRemainingSeconds, 1.4); }
    public void setSurprised(boolean value) { surprised = value; }

    /** Use the render delta and smoothed speed, never a raw GPS frame interval. */
    public void drive(double dt, double smoothedSpeedMetersPerSecond) {
        if (!Double.isFinite(dt) || dt < 0 || !Double.isFinite(smoothedSpeedMetersPerSecond)
                || smoothedSpeedMetersPerSecond < 0)
            throw new IllegalArgumentException("Invalid animation sample");
        if (dt == 0) return;
        double moving = smoothedSpeedMetersPerSecond >= 0.65 ? 1.0 : 0.0;
        stationarySeconds = moving > 0 ? 0 : stationarySeconds + dt;
        State desired;
        if (narrationActive) desired = State.TALKING;
        else if (waveRemainingSeconds > 0) desired = State.WAVING;
        else if (stationarySeconds >= 50) desired = State.SLEEPING;
        else if (moving > 0) desired = State.ROLLING;
        else desired = State.IDLE;
        setState(desired);
        advance(dt);
        waveRemainingSeconds = Math.max(0, waveRemainingSeconds - dt);
    }

    public Frame frame() {
        if (surprised && !narrationActive) return Frame.SURPRISED;
        return switch (state) {
            case SLEEPING -> Frame.SLEEP;
            case WAVING -> Frame.WAVE;
            case TALKING -> ((int) Math.floor(stateTime * 5.0) % 2 == 0)
                    ? Frame.TALK_OPEN : Frame.TALK_CLOSED;
            case IDLE, ROLLING -> isBlinking() ? Frame.BLINK : Frame.IDLE;
        };
    }

    // Legacy pure-time API is retained for existing deterministic tests.
    public void setState(State next) {
        if (next == null) throw new IllegalArgumentException("Null state");
        if (next != state) { state = next; stateTime = 0; }
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
        return state != State.SLEEPING && (elapsed % 4.3) > 4.17;
    }
    public int talkingFrame() {
        return state == State.TALKING ? (int) Math.floor(stateTime * 5.0) % 2 : 0;
    }
}
