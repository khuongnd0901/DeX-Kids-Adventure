package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.khuongnd.dexkids.journey.JourneyFeed;
import com.khuongnd.dexkids.journey.GpxReplayFeed;
import com.khuongnd.dexkids.journey.DeferredGpxJourneyFeed;
import com.khuongnd.dexkids.geo.OfflinePoiCatalog;
import com.khuongnd.dexkids.geo.OfflinePoiEngine;
import com.khuongnd.dexkids.geo.LiveFixGate;
import com.khuongnd.dexkids.journey.LiveJourneyFeed;
import com.khuongnd.dexkids.story.PoiDialogueCatalog;
import com.khuongnd.dexkids.story.NarrationCue;
import com.khuongnd.dexkids.story.OfflineNarrationCatalog;
import com.khuongnd.dexkids.story.TourGuideDirector;
import java.util.concurrent.atomic.AtomicReference;
import java.io.InputStream;
import java.util.Optional;
import com.khuongnd.dexkids.world.ProceduralWorldGenerator;

import java.util.Locale;
import java.util.function.BooleanSupplier;

/** Render loop does not directly consume Android GPS or call external services. */
public final class AdventureScreen extends ScreenAdapter {
    public static final int WIDTH = 1920, HEIGHT = 1080;
    private final JourneyFeed journey;
    private final BooleanSupplier parentMenuOpen;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final WorldPainter painter;
    private final CartoonSprites cartoonSprites;
    private final FrameProfiler frameProfiler = new FrameProfiler(180);
    private final WorldMoodResolver worldMood = new WorldMoodResolver();
    private final RenderRunMetrics runMetrics = new RenderRunMetrics();
    private double nextMetricsLog = 30;
    private int previousFrameDrawCalls;
    private float clock;
    private float textRefreshTimer;
    private long nextMoodCheckAt;
    private WorldMoodResolver.Mood cachedMood = WorldMoodResolver.Mood.DAY;
    private String motionText = "";
    private String profilerText = "";
    private final OfflinePoiEngine poiEngine;
    private final PoiSceneDirector sceneDirector = new PoiSceneDirector();
    private final boolean reviewedPoiData;
    private final boolean liveGpsMode;
    private final LiveFixGate liveFixGate = new LiveFixGate();
    private final PoiDialogueCatalog dialogueCatalog;
    private final AtomicReference<PoiDialogueCatalog.Dialogue> dialogueQueue = new AtomicReference<>();
    private final boolean hcmSamplePreview;
    private final OfflineNarrationCatalog storyCatalog;
    private final TourGuideDirector storyDirector = new TourGuideDirector(30_000);
    private final AtomicReference<NarrationCue> narrationQueue = new AtomicReference<>();
    private final BooleanSupplier narrationActive;
    private final BooleanSupplier audioOnly;
    private final AtomicReference<String> entertainmentReaction;
    private float surpriseRemainingSeconds;
    private final int narrationAge;
    // Read from Android UI thread, written on render thread. No coordinates exposed or persisted.
    private volatile String poiStatusText = "";
    private long poiNoticeExpireAt;
    private long lastPoiQueryAt; // avoid O(n) GPX segment scans on every render frame

    public AdventureScreen(JourneyFeed journey) {
        this(journey, () -> false, () -> false, false, 4);
    }
    public AdventureScreen(JourneyFeed journey, BooleanSupplier parentMenuOpen) {
        this(journey, parentMenuOpen, () -> false, false, 4);
    }
    public AdventureScreen(JourneyFeed journey, BooleanSupplier parentMenuOpen,
                           BooleanSupplier narrationActive, boolean hcmSamplePreview, int narrationAge) {
        this(journey, parentMenuOpen, narrationActive, hcmSamplePreview, narrationAge, () -> false);
    }

    public AdventureScreen(JourneyFeed journey, BooleanSupplier parentMenuOpen,
                           BooleanSupplier narrationActive, boolean hcmSamplePreview,
                           int narrationAge, BooleanSupplier audioOnly) {
        this(journey, parentMenuOpen, narrationActive, hcmSamplePreview,
                narrationAge, audioOnly, new AtomicReference<>());
    }

    public AdventureScreen(JourneyFeed journey, BooleanSupplier parentMenuOpen,
                           BooleanSupplier narrationActive, boolean hcmSamplePreview,
                           int narrationAge, BooleanSupplier audioOnly,
                           AtomicReference<String> entertainmentReaction) {
        this.entertainmentReaction = java.util.Objects.requireNonNull(entertainmentReaction);
        this.journey = journey;
        this.audioOnly = java.util.Objects.requireNonNull(audioOnly);
        this.narrationAge = narrationAge;
        this.parentMenuOpen = java.util.Objects.requireNonNull(parentMenuOpen);
        this.narrationActive = java.util.Objects.requireNonNull(narrationActive);
        this.hcmSamplePreview = hcmSamplePreview;
        this.liveGpsMode = journey instanceof LiveJourneyFeed;
        viewport = new FitViewport(WIDTH, HEIGHT, new OrthographicCamera());
        viewport.getCamera().position.set(WIDTH / 2f, HEIGHT / 2f, 0);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        painter = new WorldPainter(new ProceduralWorldGenerator(20261008L));
        cartoonSprites = new CartoonSprites();
        OfflinePoiCatalog catalogue = loadPoiCatalog(hcmSamplePreview, liveGpsMode);
        reviewedPoiData = !catalogue.entries().isEmpty();
        poiEngine = new OfflinePoiEngine(catalogue);
        storyCatalog = loadNarrationCatalog(hcmSamplePreview, liveGpsMode);
        dialogueCatalog = loadDialogueCatalog(liveGpsMode);
        if (!reviewedPoiData) poiStatusText = "Chưa có dữ liệu địa danh GPS offline";
        else if (liveGpsMode) poiStatusText = "GPS thực · địa danh bản đồ cần xác minh ngoài thực địa";
    }

    private static OfflinePoiCatalog loadPoiCatalog(boolean samplePreview, boolean liveGps) {
        String name = samplePreview ? "poi/sample-hcm.tsv" : liveGps ? "poi/live-landmarks.tsv" : "poi/reviewed.tsv";
        var path = Gdx.files.internal(name);
        if (!path.exists()) path = Gdx.files.internal("assets/" + name);
        if (!path.exists()) {
            Gdx.app.error("OfflinePOI", "No approved POI catalogue; disable all named announcements");
            return OfflinePoiCatalog.empty();
        }
        try (InputStream in = path.read()) {
            return OfflinePoiCatalog.parse(in);
        } catch (Exception ex) {
            Gdx.app.error("OfflinePOI", "Invalid approved catalogue; disable named POIs", ex);
            return OfflinePoiCatalog.empty();
        }
    }

    private static OfflineNarrationCatalog loadNarrationCatalog(boolean samplePreview, boolean liveGps) {
        String name = samplePreview ? "narration/sample-hcm.tsv" : liveGps ? "narration/live-landmarks.tsv" : "narration/approved.tsv";
        var path = Gdx.files.internal(name);
        if (!path.exists()) path = Gdx.files.internal("assets/" + name);
        if (!path.exists()) return OfflineNarrationCatalog.empty();
        try (InputStream in = path.read()) {
            return OfflineNarrationCatalog.parse(in);
        } catch (Exception ex) {
            Gdx.app.error("OfflineNarration", "Invalid or missing narration content; silence", ex);
            return OfflineNarrationCatalog.empty();
        }
    }

    private static PoiDialogueCatalog loadDialogueCatalog(boolean liveGps) {
        if (!liveGps) return PoiDialogueCatalog.empty();
        var file = Gdx.files.internal("poi/live-dialogue.tsv");
        if (!file.exists()) file = Gdx.files.internal("assets/poi/live-dialogue.tsv");
        if (!file.exists()) return PoiDialogueCatalog.empty();
        try (InputStream in = file.read()) {
            return PoiDialogueCatalog.parse(in);
        } catch (Exception e) {
            Gdx.app.error("OfflinePOI", "Invalid dialogue pack; questions disabled", e);
            return PoiDialogueCatalog.empty();
        }
    }

    @Override public void show() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override public void render(float rawDelta) {
        frameProfiler.record(rawDelta);
        runMetrics.record(rawDelta);
        if (runMetrics.seconds() >= nextMetricsLog) {
            Gdx.app.log("RenderMetrics", String.format(Locale.US,
                    "frames=%d render_seconds=%.3f avg_fps=%.3f p95_upper_ms=%.1f sprite_draw_calls_prev=%d",
                    runMetrics.frames(), runMetrics.seconds(),
                    runMetrics.averageFps(), runMetrics.p95UpperMs(), previousFrameDrawCalls));
            nextMetricsLog = runMetrics.seconds() + 30;
        }
        float delta = JourneyRenderPause.effectiveDelta(rawDelta, parentMenuOpen.getAsBoolean());
        clock += delta;
        journey.update(delta);
        sceneDirector.update(delta, journey.distanceMeters());
        if (delta > 0) {
            String reaction = entertainmentReaction.getAndSet(null);
            if ("WAVE".equals(reaction)) cartoonSprites.requestWave();
            else if ("SURPRISE".equals(reaction)) surpriseRemainingSeconds = 1.7f;
            surpriseRemainingSeconds = Math.max(0f, surpriseRemainingSeconds - delta);
            cartoonSprites.setSurprised(surpriseRemainingSeconds > 0f);
        }
        if (delta > 0 && reviewedPoiData) {
            long now = System.currentTimeMillis();
            var position = (lastPoiQueryAt == 0 || now - lastPoiQueryAt >= 900)
                    ? journey.position(now) : java.util.Optional.<com.khuongnd.dexkids.geo.JourneyPosition>empty();
            if (position.isPresent()) lastPoiQueryAt = now;
            if (position.isPresent() && (!liveGpsMode || liveFixGate.accept(position.get()))) {
                var event = poiEngine.observe(position.get(), now);
                if (event.isPresent()) {
                    var notice = event.orElseThrow();
                    if (sceneDirector.onNotice(notice, journey.distanceMeters())) {
                        Gdx.app.log("PoiScene", "event=illustrative_theme_requested type="
                                + notice.entry().type() + " simulated=" + notice.simulated());
                    }
                    String prefix = notice.simulated() ? "GPX mô phỏng" : "GPS ước tính";
                    String wording = switch (notice.event()) {
                        case NEARBY -> "Địa danh gần đây";
                        case APPROACHING -> "Có thể đang đến gần";
                        // NOT verified passage/direction; no "passed" assertion.
                        case PASSING_CANDIDATE -> "Có thể vừa ở gần";
                    };
                    poiStatusText = (hcmSamplePreview ? "[MẪU CHƯA DUYỆT] · " :
                            liveGpsMode ? "[OSM CHƯA KHẢO SÁT ĐƯỜNG] · " : "") +
                            prefix + " · " + wording + ": " + notice.entry().poi().name();
                    poiNoticeExpireAt = now + 9_000L;
                    // No inferred place facts, navigation assertions or unsourced generated speech.
                    if ((hcmSamplePreview || liveGpsMode) &&
                            notice.event() != OfflinePoiEngine.EventKind.PASSING_CANDIDATE) {
                        storyCatalog.findByPoiId(notice.entry().poi().id()).ifPresent(cue ->
                            storyDirector.select(cue, narrationAge, true, now).ifPresent(selected -> {
                                narrationQueue.set(selected);
                                if (liveGpsMode) dialogueCatalog.find(selected.poiId())
                                        .ifPresent(dialogueQueue::set);
                            }));
                    }
                    Gdx.app.log("OfflinePOI", "type=" + notice.event() +
                            " kind=" + notice.entry().type() +
                            " source=OSM map preview (not lane-matched; no GPS coordinates logged)");
                }
            }
            if (poiNoticeExpireAt > 0 && now >= poiNoticeExpireAt) {
                poiStatusText = "Dữ liệu địa danh © OpenStreetMap contributors (ODbL)";
                poiNoticeExpireAt = 0;
            }
        }
        textRefreshTimer += delta;
        if (textRefreshTimer >= 0.5f) {
            textRefreshTimer = 0;
            motionText = String.format(Locale.US, "%s speed: %.0f km/h   Distance: %.1f km",
                    (journey instanceof GpxReplayFeed || journey instanceof DeferredGpxJourneyFeed) ? "GPX replay" :
                        journey.isDemo() ? "Simulated" : "Estimated GPS",
                    journey.speedMetersPerSecond() * 3.6, journey.distanceMeters() / 1000.0);
            profilerText = String.format(Locale.US, "Render samples (current run): %.1f FPS   P95 %.1f ms",
                    frameProfiler.averageFps(), frameProfiler.p95FrameMs());
        }
        if (audioOnly.getAsBoolean()) {
            // Keep location/POI/story state moving, render no scenery or sprites.
            // The same monitor and native parent controls remain available.
            Gdx.gl.glClearColor(0.07f, 0.11f, 0.16f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            font.draw(batch, "AUDIO-ONLY JOURNEY", 70, 970);
            font.draw(batch, "POI captions remain | F10 / Parents menu", 70, 885);
            font.draw(batch, motionText, 70, 790);
            batch.end();
            previousFrameDrawCalls = batch.renderCalls;
            return;
        }
        Gdx.gl.glClearColor(0.73f, 0.87f, 0.99f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // LocalTime and its timezone lookup are not necessary on every GL frame.
        long moodNow = System.currentTimeMillis();
        if (moodNow >= nextMoodCheckAt) {
            cachedMood = worldMood.resolve(java.time.LocalTime.now());
            nextMoodCheckAt = moodNow + 60_000L;
        }
        WorldMoodResolver.Mood mood = cachedMood;
        var themed = sceneDirector.scene();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        // Single SpriteBatch begin/end replaces the previous four-pass GL renderer.
        batch.begin();
        painter.paintSky(batch, mood);
        cartoonSprites.drawFar(batch, journey.distanceMeters(), clock, mood);
        painter.paintGround(batch, journey.distanceMeters());
        cartoonSprites.drawEnvironment(batch, journey.distanceMeters(), mood, themed);
        cartoonSprites.setNarrationActive(narrationActive.getAsBoolean());
        cartoonSprites.drawVehicle(batch, delta, clock,
                journey.distanceMeters(), journey.speedMetersPerSecond(), mood);
        painter.paintHud(batch);
        font.draw(batch, "DeX KIDS ADVENTURE", 50, 1015);
        font.draw(batch, (journey instanceof GpxReplayFeed || journey instanceof DeferredGpxJourneyFeed)
                ? "GPX REPLAY - SYNTHETIC ROUTE / NO REAL POI CLAIM"
                : journey.isDemo() ? "DEMO WORLD - NO REAL GPS / POI"
                : "LIVE GPS - OSM NEARBY CANDIDATE / NOT ROAD MATCHED", 50, 968);
        font.draw(batch, motionText, 50, 914);
        font.draw(batch, profilerText, 50, 865);
        if (themed.active()) {
            String provenance = themed.simulated() ? "GPX SAMPLE" : "OSM POI ESTIMATE";
            font.draw(batch, String.format(Locale.US,
                    "ILLUSTRATIVE SCENERY: %s (%.0f%%) [%s]",
                    themed.biome(), themed.alpha() * 100f, provenance), 50, 815);
        }
        batch.end();
        previousFrameDrawCalls = batch.renderCalls;
    }

    public String poiStatusText() { return poiStatusText; }
    public PoiDialogueCatalog.Dialogue pollPoiDialogue() { return dialogueQueue.getAndSet(null); }
    /** One-shot transfer from LibGDX render thread to Android UI thread. */
    public NarrationCue pollNarrationCue() { return narrationQueue.getAndSet(null); }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void resume() { frameProfiler.reset(); }
    @Override public void dispose() {
        Gdx.app.log("RenderMetrics", String.format(Locale.US,
                "event=disposed frames=%d render_seconds=%.3f avg_fps=%.3f p95_upper_ms=%.1f",
                runMetrics.frames(), runMetrics.seconds(),
                runMetrics.averageFps(), runMetrics.p95UpperMs()));
        painter.dispose();
        cartoonSprites.dispose();
        font.dispose();
        batch.dispose();
    }
}
