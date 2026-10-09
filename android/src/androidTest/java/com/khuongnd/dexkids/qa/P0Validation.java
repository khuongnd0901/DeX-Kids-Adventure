package com.khuongnd.dexkids.qa;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.badlogic.gdx.Gdx;
import com.khuongnd.dexkids.game.AdventureScreen;
import com.khuongnd.dexkids.game.FrameProfiler;
import com.khuongnd.dexkids.game.KidsGame;
import com.khuongnd.dexkids.game.PoiSceneDirector;
import com.khuongnd.dexkids.journey.DeferredGpxJourneyFeed;
import com.khuongnd.dexkids.journey.GpxReplayFeed;
import com.khuongnd.dexkids.journey.LiveJourneyFeed;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * P0/T-012 real Android framework/LibGDX tests. Synthetic HCMC data is preview
 * only. No real road matching or approved landmark claim. No production QA switch.
 */
public final class P0Validation {
    private static final String PKG = "com.khuongnd.dexkids";
    private P0Validation() {}

    public static void run(Instrumentation runner, String mode, Bundle result) throws Exception {
        Instrumentation.ActivityMonitor monitor = runner.addMonitor(PKG + ".KidsActivity", null, false);
        Activity parent = null, child = null;
        try {
            parent = runner.startActivitySync(new Intent().setClassName(PKG, PKG + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            final Activity dashboard = parent;
            String label = "p0_hcm".equals(mode) ? "Start HCMC sample journey (preview)"
                    : "p0_live".equals(mode) ? "Start LIVE GPS on this screen"
                    : "Start DEMO on this screen";
            boolean[] clicked = {false};
            runner.runOnMainSync(() -> {
                Button button = findButton(dashboard.getWindow().getDecorView(), label);
                clicked[0] = button != null && button.performClick();
            });
            require(clicked[0], "P0 dashboard launch control not clickable: " + label);
            child = runner.waitForMonitorWithTimeout(monitor, 12000);
            require(child != null && !child.isFinishing(), "KidsActivity did not launch: " + mode);
            require(child.getDisplay().getDisplayId() == parent.getDisplay().getDisplayId(),
                    "Different display IDs: not a single-display test");
            KidsGame game = (KidsGame) child.getClass().getMethod("getApplicationListener").invoke(child);
            require(game != null, "LibGDX game listener missing");
            if ("p0_hcm".equals(mode)) validateHcm(runner, child, game, result);
            else if ("p0_perf_ab".equals(mode)) benchmark(runner, child, game, result);
            else if ("p0_live".equals(mode)) validateInjectedLive(runner, child, game, result);
            else throw new AssertionError("Unknown P0 mode: " + mode);
            result.putString("source_scope", "Android emulator/runtime only; Fold3 DeX NOT_VERIFIED");
        } finally {
            final Activity capturedChild = child, capturedParent = parent;
            runner.runOnMainSync(() -> {
                if (capturedChild != null && !capturedChild.isFinishing()) capturedChild.finish();
                if (capturedParent != null && !capturedParent.isFinishing()) capturedParent.finish();
            });
            runner.removeMonitor(monitor);
        }
    }

    private static void validateHcm(Instrumentation runner, Activity child, KidsGame game,
                                    Bundle result) throws Exception {
        require(child.getIntent().getBooleanExtra(PKG + ".extra.HCM_SAMPLE_PREVIEW", false),
                "Must explicitly opt in to HCMC preview");
        Object holder = field(child, "journeyFeed");
        require(holder instanceof DeferredGpxJourneyFeed, "Bundled GPX holder not active");
        DeferredGpxJourneyFeed deferred = (DeferredGpxJourneyFeed) holder;
        long limit = SystemClock.elapsedRealtime() + 12000;
        while (!deferred.isLoaded() && SystemClock.elapsedRealtime() < limit)
            SystemClock.sleep(100);
        GpxReplayFeed replay = deferred.replay();
        require(replay != null && replay.totalTimeSeconds() == 900d, "Bundled sample GPX not loaded");
        // Progress the actual production replay on its OWN render thread.
        // This avoids a 180-second wall wait and DOES NOT fake a POI event.
        gl(() -> { for (int i = 0; i < 1800; i++) replay.update(0.1f); });
        require(replay.replayTimeSeconds() >= 179, "GPX replay did not advance");
        long until = SystemClock.elapsedRealtime() + 14000;
        String nativeText = "";
        PoiSceneDirector.Scene themed = null;
        while (SystemClock.elapsedRealtime() < until) {
            AdventureScreen scene = (AdventureScreen) game.getScreen();
            PoiSceneDirector director = (PoiSceneDirector) field(scene, "sceneDirector");
            themed = director.scene();
            TextView overlay = (TextView) field(child, "poiNativeStatus");
            nativeText = overlay == null ? "" : String.valueOf(overlay.getText());
            if (themed.active() && nativeText.contains("Công viên Tao Đàn")
                    && nativeText.contains("MẪU THUYẾT MINH")) break;
            SystemClock.sleep(350);
        }
        require(themed != null && themed.active(), "No real LibGDX themed scene");
        require("PARK".equals(themed.biome().name()), "Expected PARK preview, got " + themed.biome());
        require(themed.simulated(), "Must label GPX theme as simulated");
        require(nativeText.contains("[MẪU CHƯA DUYỆT]"), "Unapproved POI was not visibly marked");
        require(nativeText.contains("MẪU THUYẾT MINH"), "Android Vietnamese subtitle cue missing");
        require(field(child, "narrator") == null, "Unexpected TTS without parent opt-in");
        result.putString("p0_hcm", "PASS: Android GPX -> sourced sample POI -> PARK GL scene -> native VI cue; TTS disabled");
        result.putDouble("gpx_seconds", replay.replayTimeSeconds());
        result.putString("biome", themed.biome().name());
        result.putString("sample_not_reviewed", "true");
        result.putString("image", screenshot(runner, "p0-hcm.png"));
        result.putString("spoken_audio", "NOT_VERIFIED: default quiet mode / opt-in disabled");
    }

    private static void validateInjectedLive(Instrumentation runner, Activity child,
                                             KidsGame game, Bundle result) throws Exception {
        require(child.getIntent().getBooleanExtra(PKG + ".extra.LIVE_GPS", false),
                "Test never entered LIVE mode");
        require(child.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED, "Fine grant missing");
        Object holder = field(child, "journeyFeed");
        require(holder instanceof LiveJourneyFeed, "Expected actual AndroidGpsSource -> LiveJourneyFeed");
        LiveJourneyFeed live = (LiveJourneyFeed) holder;
        // External harness must send 'adb emu geo fix LONG LAT' to a disposable AVD;
        // no instrumentation mock and no location permission changes here.
        long until = SystemClock.elapsedRealtime() + 35000;
        final double[] distance = new double[1];
        do {
            // LiveJourneyFeed is mutated on GL thread; reading non-volatile
            // distance on instrumentation thread is a data race.
            gl(() -> distance[0] = live.distanceMeters());
            if (distance[0] > 2) break;
            SystemClock.sleep(350);
        } while (SystemClock.elapsedRealtime() < until);

        android.location.LocationManager manager =
                (android.location.LocationManager) child.getSystemService(android.content.Context.LOCATION_SERVICE);
        result.putBoolean("gps_provider_enabled",
                manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER));
        android.location.Location last =
                manager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
        result.putBoolean("android_has_gps_fix", last != null);
        if (last != null) {
            result.putFloat("gps_accuracy_m", last.hasAccuracy() ? last.getAccuracy() : -1);
            result.putLong("gps_fix_age_ms", System.currentTimeMillis() - last.getTime());
        }
        result.putBoolean("feed_accepted_at_least_one_fix", field(live, "accepted") != null);
        result.putDouble("live_distance_m", distance[0]);
        require(distance[0] > 2, "No distance after ADB emulator GPS fixes; inspect provider/accuracy/time/accepted diagnostics");
        result.putString("p0_live", "PASS: injected emulator GPS moved live feed (not real GPS accuracy)");
        result.putString("image", screenshot(runner, "p0-live.png"));
    }

    private static void benchmark(Instrumentation runner, Activity child,
                                  KidsGame game, Bundle result) throws Exception {
        // The Activity monitor can fire before LibGDX calls Game.create().
        // Wait for a real attached AdventureScreen instead of assuming readiness.
        AdventureScreen scene = null;
        long readyUntil = SystemClock.elapsedRealtime() + 12000;
        while (scene == null && SystemClock.elapsedRealtime() < readyUntil) {
            if (game.getScreen() instanceof AdventureScreen ready) scene = ready;
            else SystemClock.sleep(100);
        }
        require(scene != null, "GL scene never became ready within 12 seconds");
        FrameProfiler profiler = (FrameProfiler) field(scene, "frameProfiler");
        ArrayList<String> windows = new ArrayList<>();
        double[] fps = new double[4], p95 = new double[4];
        // A/B/A/B same device, process, APK, resolution, world and time budget.
        // No AVD shell work during timed windows, no developer CPU assertions.
        for (int i = 0; i < 4; i++) {
            boolean audioOnly = i % 2 == 1;
            game.setAudioOnly(audioOnly);
            SystemClock.sleep(2000);
            gl(profiler::reset);
            SystemClock.sleep(6000);
            final double[] metrics = new double[3];
            gl(() -> {
                metrics[0] = profiler.size();
                metrics[1] = profiler.averageFps();
                metrics[2] = profiler.p95FrameMs();
            });
            require(metrics[0] >= 30, "Insufficient real GL frame samples in window " + i);
            fps[i] = metrics[1];
            p95[i] = metrics[2];
            windows.add(String.format(Locale.ROOT, "%s#%d: samples=%.0f fps=%.2f p95_ms=%.2f",
                    audioOnly ? "minimal" : "full", i, metrics[0], fps[i], p95[i]));
        }
        result.putString("p0_windows", String.join(" | ", windows));
        result.putDouble("full_fps_avg", (fps[0] + fps[2]) / 2.0);
        result.putDouble("minimal_fps_avg", (fps[1] + fps[3]) / 2.0);
        result.putDouble("full_p95_ms_avg", (p95[0] + p95[2]) / 2.0);
        result.putDouble("minimal_p95_ms_avg", (p95[1] + p95[3]) / 2.0);
        result.putString("performance_gate",
                fps[0] >= 30 && fps[2] >= 30 && p95[0] <= 33.34 && p95[2] <= 33.34
                    ? "PASS (two short full-render windows only)" : "FAIL (two short full-render windows)");
        result.putString("limit", "last 180 frame samples per window; NOT sustained Fold3 or art-version A/B");
        game.setAudioOnly(false);
        result.putString("image", screenshot(runner, "p0-performance.png"));
    }

    private static void gl(Runnable work) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        require(Gdx.app != null, "GL application unavailable");
        Gdx.app.postRunnable(() -> {
            try { work.run(); } catch (Throwable t) { error.set(t); } finally { done.countDown(); }
        });
        require(done.await(10, TimeUnit.SECONDS), "GL render thread stalled");
        if (error.get() != null) throw new AssertionError("GL failed", error.get());
    }

    private static Object field(Object object, String name) throws Exception {
        Field f = object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(object);
    }

    private static Button findButton(View view, String label) {
        if (view instanceof Button && label.contentEquals(((Button)view).getText())) return (Button)view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i=0; i<group.getChildCount();i++) {
                Button found = findButton(group.getChildAt(i),label);
                if (found!=null) return found;
            }
        }
        return null;
    }

    private static String screenshot(Instrumentation runner, String filename) throws Exception {
        Bitmap bitmap = runner.getUiAutomation(
                android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).takeScreenshot();
        require(bitmap != null, "Android screenshot unavailable");
        File directory = new File(runner.getTargetContext().getExternalFilesDir(null),"p0-t012");
        require(directory.exists() || directory.mkdirs(),"Cannot make screenshot directory");
        File dest = new File(directory,filename);
        try (FileOutputStream output = new FileOutputStream(dest)) {
            require(bitmap.compress(Bitmap.CompressFormat.PNG,100,output),"Screenshot encode failed");
        } finally { bitmap.recycle(); }
        require(dest.length()>0,"Missing screenshot bytes");
        return dest.getAbsolutePath();
    }

    private static void require(boolean condition, String detail) {
        if (!condition) throw new AssertionError(detail);
    }
}
