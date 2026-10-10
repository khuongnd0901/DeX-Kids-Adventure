package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Game;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final AtomicBoolean parentMenuOpen = new AtomicBoolean();
    private final AtomicBoolean narrationActive = new AtomicBoolean();
    private final AtomicBoolean audioOnly = new AtomicBoolean();
    private final java.util.concurrent.atomic.AtomicReference<String> entertainmentReaction =
            new java.util.concurrent.atomic.AtomicReference<>();
    private final boolean samplePreview;
    private final int narrationAge;

    public KidsGame() { this(new DemoJourneyFeed()); }
    public KidsGame(JourneyFeed journey) { this(journey, 0, null, false, 4); }
    public KidsGame(JourneyFeed journey, boolean samplePreview) {
        this(journey, 0, null, samplePreview, 4);
    }
    public KidsGame(JourneyFeed journey, boolean samplePreview, int narrationAge) {
        this(journey, 0, null, samplePreview, narrationAge);
    }
    public KidsGame(JourneyFeed journey, int captureAfterFrames, String screenshotPath) {
        this(journey, captureAfterFrames, screenshotPath, false, 4);
    }
    public KidsGame(JourneyFeed journey, int captureAfterFrames, String screenshotPath,
                    boolean samplePreview, int narrationAge) {
        if (narrationAge < 2 || narrationAge > 6) throw new IllegalArgumentException("age");
        this.narrationAge = narrationAge;
        this.samplePreview = samplePreview;
        if (journey == null || captureAfterFrames < 0 ||
                (captureAfterFrames > 0 && (screenshotPath == null || screenshotPath.isBlank())))
            throw new IllegalArgumentException("Invalid capture configuration");
        this.journey = journey;
        this.captureAfterFrames = captureAfterFrames;
        this.screenshotPath = screenshotPath;
    }

    /** Thread-safe Android UI -> LibGDX render-loop pause signal. */
    public void setParentMenuOpen(boolean open) { parentMenuOpen.set(open); }
    public void setNarrationActive(boolean active) { narrationActive.set(active); }
    public void setAudioOnly(boolean enabled) { audioOnly.set(enabled); }
    /** Android UI posts non-geographic animation actions; GL thread consumes them. */
    public void showEntertainmentReaction(String reaction) {
        if ("WAVE".equals(reaction) || "SURPRISE".equals(reaction))
            entertainmentReaction.set(reaction);
    }
    public com.khuongnd.dexkids.story.NarrationCue pollNarrationCue() {
        return screen instanceof AdventureScreen scene ? scene.pollNarrationCue() : null;
    }
    public com.khuongnd.dexkids.story.PoiDialogueCatalog.Dialogue pollPoiDialogue() {
        return screen instanceof AdventureScreen scene ? scene.pollPoiDialogue() : null;
    }
    public String poiStatusText() {
        return screen instanceof AdventureScreen scene ? scene.poiStatusText() : "";
    }
    @Override public void create() {
        setScreen(new AdventureScreen(journey, parentMenuOpen::get, narrationActive::get, samplePreview, narrationAge, audioOnly::get, entertainmentReaction));
    }

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
