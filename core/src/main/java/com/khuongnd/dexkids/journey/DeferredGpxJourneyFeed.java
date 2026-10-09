package com.khuongnd.dexkids.journey;

/**
 * Safe LibGDX startup while a SAF GPX document is parsed on a worker thread.
 * install() is called on the render thread (Gdx.app.postRunnable), and the
 * adapter is retained across Activity recreation without keeping an Activity.
 */
public final class DeferredGpxJourneyFeed implements JourneyFeed {
    private volatile GpxReplayFeed delegate;

    public boolean isLoaded() { return delegate != null; }
    public GpxReplayFeed replay() { return delegate; }

    public void install(GpxReplayFeed loaded) {
        if (loaded == null || delegate != null)
            throw new IllegalStateException("Replay already installed or missing");
        delegate = loaded;
    }

    @Override public void update(float dt) {
        GpxReplayFeed active = delegate;
        if (active != null) active.update(dt);
    }
    @Override public double distanceMeters() {
        GpxReplayFeed active = delegate;
        return active == null ? 0 : active.distanceMeters();
    }
    @Override public double speedMetersPerSecond() {
        GpxReplayFeed active = delegate;
        return active == null ? 0 : active.speedMetersPerSecond();
    }
    @Override public java.util.Optional<com.khuongnd.dexkids.geo.JourneyPosition> position(long nowMillis) {
        GpxReplayFeed active = delegate;
        return active == null ? java.util.Optional.empty() : active.position(nowMillis);
    }
    @Override public boolean isDemo() { return true; }
}
