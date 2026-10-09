package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.files.FileHandle;
import com.khuongnd.dexkids.journey.DemoJourneyFeed;
import com.khuongnd.dexkids.journey.JourneyFeed;

/** Finite-frame desktop capture writes an actual rendered sequence, never fabricated FPS. */
public final class KidsGame extends Game {
    private final JourneyFeed journey;
    private final int captureAfterFrames;
    private final String screenshotPath;
    private int rendered;

    public KidsGame() { this(new DemoJourneyFeed()); }
    public KidsGame(JourneyFeed journey) { this(journey, 0, null); }

    public KidsGame(JourneyFeed journey, int captureAfterFrames, String screenshotPath) {
        if (journey == null || captureAfterFrames < 0 ||
                (captureAfterFrames > 0 && (screenshotPath == null || screenshotPath.isBlank())))
            throw new IllegalArgumentException("Invalid capture configuration");
        this.journey = journey;
        this.captureAfterFrames = captureAfterFrames;
        this.screenshotPath = screenshotPath;
    }

    @Override public void create() { setScreen(new AdventureScreen(journey)); }

    @Override public void render() {
        super.render();
        if (captureAfterFrames <= 0) return;
        rendered++;
        int stride = Math.max(1, captureAfterFrames / 10);
        if (rendered % stride == 0 || rendered == captureAfterFrames) {
            FileHandle output = Gdx.files.local(screenshotPath);
            // Captures only occur in explicit desktop smoke mode, not Android production.
            FileHandle frames = output.parent().child("frames-" + output.nameWithoutExtension());
            frames.mkdirs();
            String number = String.format(java.util.Locale.ROOT, "%04d.png", rendered);
            writeFrame(frames.child(number));
            if (rendered == captureAfterFrames) {
                writeFrame(output);
                Gdx.app.log("KidsSmoke", "Saved actual OpenGL frame(s) to " + output.path());
                Gdx.app.exit();
            }
        }
    }

    @Override public void dispose() {
        var owned = screen;
        if (owned == null) return;
        super.dispose(); // LibGDX Game hides its screen, but does not dispose it.
        screen = null;
        owned.dispose();
    }

    private void writeFrame(FileHandle destination) {
        destination.parent().mkdirs();
        Pixmap image = Pixmap.createFromFrameBuffer(
                0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try { PixmapIO.writePNG(destination, image, 6, true); }
        finally { image.dispose(); }
    }
}
