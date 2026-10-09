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
    // Read from Android UI thread, written on render thread. No coordinates exposed or persisted.
    private volatile String poiStatusText = "";
    private long poiNoticeExpireAt;

    public AdventureScreen(JourneyFeed journey) {
        this(journey, () -> false);
    }

    public AdventureScreen(JourneyFeed journey, BooleanSupplier parentMenuOpen) {
        this.journey = journey;
        this.parentMenuOpen = java.util.Objects.requireNonNull(parentMenuOpen);
        viewport = new FitViewport(WIDTH, HEIGHT, new OrthographicCamera());
        viewport.getCamera().position.set(WIDTH / 2f, HEIGHT / 2f, 0);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        painter = new WorldPainter(new ProceduralWorldGenerator(20261008L));
        cartoonSprites = new CartoonSprites();
        OfflinePoiCatalog catalogue = loadReviewedPoiCatalog();
        reviewedPoiData = !catalogue.entries().isEmpty();
        poiEngine = new OfflinePoiEngine(catalogue);
        if (!reviewedPoiData) poiStatusText = "Chưa có dữ liệu POI offline đã kiểm duyệt";
    }

    private static OfflinePoiCatalog loadReviewedPoiCatalog() {
        var path = Gdx.files.internal("poi/reviewed.tsv");
        if (!path.exists()) path = Gdx.files.internal("assets/poi/reviewed.tsv");
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
            var position = journey.position(now);
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
                    poiStatusText = prefix + " · " + wording + ": " + notice.entry().poi().name();
                    poiNoticeExpireAt = now + 9_000L;
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
