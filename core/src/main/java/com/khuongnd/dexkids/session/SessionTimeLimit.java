package com.khuongnd.dexkids.session;

/** Monotonic elapsed time only, never relies on wall-clock/GPS. */
public final class SessionTimeLimit {
    private final long maxSeconds;
    private double usedSeconds;
    private boolean paused;

    public SessionTimeLimit(int limitMinutes) {
        if (limitMinutes < 1 || limitMinutes > 120) throw new IllegalArgumentException("minutes");
        maxSeconds = limitMinutes * 60L;
    }

    public boolean tick(double deltaSeconds) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds < 0)
            throw new IllegalArgumentException("delta");
        if (!paused && !finished()) usedSeconds += deltaSeconds;
        return finished();
    }

    public void setPaused(boolean value) { paused = value; }
    public boolean finished() { return usedSeconds >= maxSeconds; }
    public long remainingSeconds() {
        return Math.max(0, (long) Math.ceil(maxSeconds - usedSeconds));
    }
}
