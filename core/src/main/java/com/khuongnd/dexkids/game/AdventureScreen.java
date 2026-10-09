package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.khuongnd.dexkids.journey.JourneyFeed;
import com.khuongnd.dexkids.journey.GpxReplayFeed;
import com.khuongnd.dexkids.journey.DeferredGpxJourneyFeed;
import com.khuongnd.dexkids.geo.OfflinePoiCatalog;
import com.khuongnd.dexkids.geo.OfflinePoiEngine;
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
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final WorldPainter painter;
    private final CartoonSprites cartoonSprites;
    private final FrameProfiler frameProfiler = new FrameProfiler(180);
    private final WorldMoodResolver worldMood = new WorldMoodResolver();
    private final RenderRunMetrics runMetrics = new RenderRunMetrics();
    private double nextMetricsLog = 30;
    private float clock;
    private float textRefreshTimer;
    private String motionText = "";
    private String profilerText = "";
    private final OfflinePoiEngine poiEngine;
    private final boolean reviewedPoiData;
    private final boolean hcmSamplePreview;
    private final OfflineNarrationCatalog storyCatalog;
    private final TourGuideDirector storyDirector = new TourGuideDirector(30_000);
    private final AtomicReference<NarrationCue> narrationQueue = new AtomicReference<>();
    private final BooleanSupplier narrationActive;
    // Read from Android UI thread, written on render thread. No coordinates exposed or persisted.
    private volatile String poiStatusText = "";
    private long poiNoticeExpireAt;
    private long lastPoiQueryAt; // avoid O(n) GPX segment scans on every render frame

    public AdventureScreen(JourneyFeed journey) {
        this(journey, () -> false, () -> false, false);
    }
    public AdventureScreen(JourneyFeed journey, BooleanSupplier parentMenuOpen) {
        this(journey, parentMenuOpen, () -> false, false);
    }
    public AdventureScreen(JourneyFeed journey, BooleanSupplier parentMenuOpen,
                           BooleanSupplier narrationActive, boolean hcmSamplePreview) {
        this.journey = journey;
        this.parentMenuOpen = java.util.Objects.requireNonNull(parentMenuOpen);
        this.narrationActive = java.util.Objects.requireNonNull(narrationActive);
        this.hcmSamplePreview = hcmSamplePreview;
        viewport = new FitViewport(WIDTH, HEIGHT, new OrthographicCamera());
        viewport.getCamera().position.set(WIDTH / 2f, HEIGHT / 2f, 0);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        painter = new WorldPainter(new ProceduralWorldGenerator(20261008L));
        cartoonSprites = new CartoonSprites();
        OfflinePoiCatalog catalogue = loadPoiCatalog(hcmSamplePreview);
        reviewedPoiData = !catalogue.entries().isEmpty();
        poiEngine = new OfflinePoiEngine(catalogue);
        storyCatalog = loadNarrationCatalog(hcmSamplePreview);
        if (!reviewedPoiData) poiStatusText = "Chưa có dữ liệu POI offline đã kiểm duyệt";
    }

    private static OfflinePoiCatalog loadPoiCatalog(boolean samplePreview) {
        String name = samplePreview ? "poi/sample-hcm.tsv" : "poi/reviewed.tsv";
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

    private static OfflineNarrationCatalog loadNarrationCatalog(boolean samplePreview) {
        String name = samplePreview ? "narration/sample-hcm.tsv" : "narration/approved.tsv";
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

    @Override public void show() {
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override public void render(float rawDelta) {
        frameProfiler.record(rawDelta);
        runMetrics.record(rawDelta);
        if (runMetrics.seconds() >= nextMetricsLog) {
            Gdx.app.log("RenderMetrics", String.format(Locale.US,
                    "frames=%d render_seconds=%.3f avg_fps=%.3f p95_upper_ms=%.1f",
                    runMetrics.frames(), runMetrics.seconds(),
                    runMetrics.averageFps(), runMetrics.p95UpperMs()));
            nextMetricsLog = runMetrics.seconds() + 30;
        }
        float delta = JourneyRenderPause.effectiveDelta(rawDelta, parentMenuOpen.getAsBoolean());
        clock += delta;
        journey.update(delta);
        if (delta > 0 && reviewedPoiData) {
            long now = System.currentTimeMillis();
            var position = (lastPoiQueryAt == 0 || now - lastPoiQueryAt >= 900)
                    ? journey.position(now) : java.util.Optional.<com.khuongnd.dexkids.geo.JourneyPosition>empty();
            if (position.isPresent()) lastPoiQueryAt = now;
            if (position.isPresent()) {
                var event = poiEngine.observe(position.get(), now);
                if (event.isPresent()) {
                    var notice = event.orElseThrow();
                    String prefix = notice.simulated() ? "GPX mô phỏng" : "GPS ước tính";
                    String wording = switch (notice.event()) {
                        case NEARBY -> "Địa danh gần đây";
                        case APPROACHING -> "Có thể đang đến gần";
                        // NOT verified passage/direction; no "passed" assertion.
                        case PASSING_CANDIDATE -> "Có thể vừa ở gần";
                    };
                    poiStatusText = (hcmSamplePreview ? "[MẪU CHƯA DUYỆT] · " : "") +
                            prefix + " · " + wording + ": " + notice.entry().poi().name();
                    poiNoticeExpireAt = now + 9_000L;
                    // No inferred place facts, navigation assertions or unsourced generated speech.
                    if (hcmSamplePreview && notice.event() != OfflinePoiEngine.EventKind.PASSING_CANDIDATE) {
                        storyCatalog.findByPoiId(notice.entry().poi().id()).ifPresent(cue ->
                            storyDirector.select(cue, 4, true, now).ifPresent(narrationQueue::set));
                    }
                    Gdx.app.log("OfflinePOI", "type=" + notice.event() +
                            " kind=" + notice.entry().type() + " source=OSM reviewed (no raw GPS logged)");
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
        Gdx.gl.glClearColor(0.73f, 0.87f, 0.99f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        WorldMoodResolver.Mood mood = worldMood.resolve(java.time.LocalTime.now());
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        painter.paintSky(shapes, clock, mood);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        cartoonSprites.drawFar(batch, journey.distanceMeters(), clock, mood);
        batch.end();
        painter.paintGround(shapes, journey.distanceMeters(), clock);
        batch.begin();
        cartoonSprites.drawEnvironment(batch, journey.distanceMeters(), mood);
        cartoonSprites.setNarrationActive(narrationActive.getAsBoolean());
        cartoonSprites.drawVehicle(batch, delta, clock,
                journey.distanceMeters(), journey.speedMetersPerSecond(), mood);
        font.draw(batch, "DeX KIDS ADVENTURE", 50, 1015);
        font.draw(batch, (journey instanceof GpxReplayFeed || journey instanceof DeferredGpxJourneyFeed)
                ? "GPX REPLAY - SYNTHETIC ROUTE / NO REAL POI CLAIM"
                : journey.isDemo() ? "DEMO WORLD - NO REAL GPS / POI"
                : "LIVE GPS - NO VERIFIED POI / NARRATION", 50, 968);
        font.draw(batch, motionText, 50, 914);
        font.draw(batch, profilerText, 50, 865);
        batch.end();
    }

    public String poiStatusText() { return poiStatusText; }
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
        shapes.dispose();
    }
}
