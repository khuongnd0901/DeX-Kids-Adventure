package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.files.FileHandle;
import com.khuongnd.dexkids.journey.DemoJourneyFeed;
import com.khuongnd.dexkids.journey.JourneyFeed;

/** Shared game supports optional finite-frame desktop OpenGL screenshot capture. */
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

    @Override public void create() {
        setScreen(new AdventureScreen(journey));
    }

    @Override public void render() {
        super.render();
        if (captureAfterFrames > 0 && ++rendered >= captureAfterFrames) {
            FileHandle destination = Gdx.files.local(screenshotPath);
            destination.parent().mkdirs();
            Pixmap screenshot = Pixmap.createFromFrameBuffer(
                    0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            try {
                PixmapIO.writePNG(destination, screenshot, 6, true);
                Gdx.app.log("KidsSmoke", "Saved OpenGL frame to " + destination.path());
            } finally {
                screenshot.dispose();
            }
            Gdx.app.exit();
        }
    }
}
