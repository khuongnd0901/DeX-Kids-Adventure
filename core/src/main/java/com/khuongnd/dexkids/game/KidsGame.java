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
    private final java.util.concurrent.atomic.AtomicReference<String> audienceMode =
            new java.util.concurrent.atomic.AtomicReference<>("BOTH");
    private final java.util.concurrent.atomic.AtomicReference<String> sauCostume =
            new java.util.concurrent.atomic.AtomicReference<>("EXPLORER");
    private final java.util.concurrent.atomic.AtomicReference<AdventureHud.Prompt> hudPrompt =
            new java.util.concurrent.atomic.AtomicReference<>(AdventureHud.WELCOME);
    private final java.util.concurrent.atomic.AtomicReference<AdventureHud.Prompt> journeyPrompt =
            new java.util.concurrent.atomic.AtomicReference<>(AdventureHud.WELCOME);
    private void publishJourneyPrompt(AdventureHud.Prompt prompt) {journeyPrompt.set(prompt);hudPrompt.set(prompt);}
    /** Restore the most recent journey cue after completion/cancellation; resources remain GL-owned. */
    AdventureHud.Prompt currentVisual() {return hudPrompt.get();}
    public void clearStoryBeat() {hudPrompt.updateAndGet(prompt->prompt.story()?journeyPrompt.get():prompt);}
    private final java.util.concurrent.atomic.AtomicReference<AnimationTimeline.Event> visualEvent =
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
    public void setAudienceMode(String mode) {
        if ("SAU".equals(mode) || "ONG".equals(mode) || "BOTH".equals(mode))
            audienceMode.set(mode);
    }
    public void showSauCostume(String role) {
        if ("EXPLORER".equals(role) || "FIREFIGHTER".equals(role)
                || "PILOT".equals(role) || "POLICE".equals(role))
            sauCostume.set(role);
    }
    /** M2: publish one immutable visual cue atomically with its associated lesson.
     * It is interpreted on the GL thread only; no arbitrary AI asset paths.
     */
    public void showEntertainmentPrompt(com.khuongnd.dexkids.story.EntertainmentDirector.Beat beat,
                    com.khuongnd.dexkids.story.EntertainmentDirector.Audience focus) {
        if (beat != null && focus != null)
            publishJourneyPrompt(AdventureHud.Prompt.fromBeat(beat,focus));
    }

    /** Atomic immutable story cue; GL resources remain owned by the render thread. */
    public void showStoryBeat(com.khuongnd.dexkids.story.adventure.StorySceneCompiler.CompiledBeat beat) {
        if (beat != null) {
            hudPrompt.set(AdventureHud.Prompt.fromStory(beat));
            if (beat.emotion() == com.khuongnd.dexkids.story.adventure.StoryEpisode.Emotion.SURPRISED)
                entertainmentReaction.set("SURPRISE");
        }
    }

    /** Legacy API for authored non-lesson captions. */
    public void showEntertainmentPrompt(String beatId, String text) {
        if (beatId != null && text != null && !text.isBlank())
            publishJourneyPrompt(AdventureHud.Prompt.fromBeat(beatId, text));
    }
    /** UI posts semantic phases only, never timestamps, arbitrary assets or child speech. */
    public void showEntertainmentPhase(String beatId, String phase) {
        if (beatId == null || phase == null) return;
        try {
            visualEvent.set(new AnimationTimeline.Event(beatId,
                    AnimationTimeline.Phase.valueOf(phase)));
        } catch (IllegalArgumentException ignored) {
            // Invalid provider/client state never affects GL resource safety.
        }
    }
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
    public String geoTopic() { return screen instanceof AdventureScreen scene ? scene.geoTopic() : ""; }
    public String poiStatusText() {
        return screen instanceof AdventureScreen scene ? scene.poiStatusText() : "";
    }
    private Runnable sceneryLoadedHook = () -> {};
    public void setSceneryLoadedHook(Runnable hook) { sceneryLoadedHook = java.util.Objects.requireNonNull(hook); }
    @Override public void create() {
        var adventure = new AdventureScreen(journey, parentMenuOpen::get, narrationActive::get,
                samplePreview, narrationAge, audioOnly::get, entertainmentReaction,
                audienceMode, sauCostume, hudPrompt, visualEvent);
        adventure.setSceneryLoadedHook(sceneryLoadedHook);
        setScreen(adventure);
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
