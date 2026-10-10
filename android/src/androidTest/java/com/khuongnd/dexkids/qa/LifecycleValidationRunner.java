package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;

/** Framework-only real Activity recreation check; screenshots require visual review. */
public final class LifecycleValidationRunner extends Instrumentation {
    private static final String PACKAGE = "com.khuongnd.dexkids";
    private static final String DEADLINE = "kids.session.deadline.elapsed";
    private Activity parent, child;
    private ActivityMonitor monitor;
    private String mode;
    private int targetDisplay = -1;
    private int performanceSeconds = 6;

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mode = arguments == null ? "recreation" : arguments.getString("mode", "recreation");
        if (arguments != null && arguments.containsKey("display_id"))
            targetDisplay = Integer.parseInt(arguments.getString("display_id"));
        if (arguments != null && arguments.containsKey("perf_seconds"))
            performanceSeconds = Integer.parseInt(arguments.getString("perf_seconds"));
        require(performanceSeconds >= 6 && performanceSeconds <= 120,
                "Performance window must be 6–120 seconds");
        start();
    }

    public int performanceSeconds() { return performanceSeconds; }
    public int targetDisplay() { return targetDisplay; }

    @Override public void runOnMainSync(Runnable action) {
        java.util.concurrent.atomic.AtomicReference<Throwable> failure = new java.util.concurrent.atomic.AtomicReference<>();
        super.runOnMainSync(() -> {
            try { action.run(); } catch (Throwable error) { failure.set(error); }
        });
        if (failure.get() != null) throw new AssertionError("Main-thread QA action failed: " + failure.get(), failure.get());
    }

    @Override public Activity startActivitySync(Intent intent) {
        if (targetDisplay < 0) return super.startActivitySync(intent);
        android.app.ActivityOptions options = android.app.ActivityOptions.makeBasic();
        options.setLaunchDisplayId(targetDisplay);
        Activity activity = super.startActivitySync(intent, options.toBundle());
        require(activity.getDisplay().getDisplayId() == targetDisplay,
                "Requested QA display unavailable; refusing phone fallback");
        return activity;
    }

    @Override public void onStart() {
        Bundle result = new Bundle();
        long started = SystemClock.elapsedRealtime();
        int code = 1;
        try {
            getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            if ("audience_picker".equals(mode)) {
                validateAudiencePicker(result);
            } else if ("audience_audio".equals(mode)) {
                validateAudienceAudio(result);
            } else if ("gemini_live".equals(mode)) {
                validateGeminiLive(result);
            } else if ("parent_controls".equals(mode)) {
                validateParentControls(result);
            } else if ("audio_playback".equals(mode)) {
                validateAudioPlayback(result);
            } else if ("physical_soak".equals(mode)) {
                validatePhysicalSoak(result);
            } else if ("gpx_parser".equals(mode)) {
                try (java.io.InputStream stream = getTargetContext().getAssets().open("gps/hcm-preview.gpx")) {
                    com.khuongnd.dexkids.journey.GpxReplayFeed feed =
                            com.khuongnd.dexkids.journey.GpxReplayFeed.fromGpx(stream);
                    require(feed.totalTimeSeconds() > 0, "Sample GPX duration missing");
                    result.putDouble("duration_seconds", feed.totalTimeSeconds());
                    String unsafe = "<?xml version='1.0'?><!DOCTYPE gpx [<!ENTITY x SYSTEM 'file:///not-readable'>]><gpx>&x;</gpx>";
                    for (java.nio.charset.Charset encoding : new java.nio.charset.Charset[] {
                            java.nio.charset.StandardCharsets.UTF_8, java.nio.charset.StandardCharsets.UTF_16,
                            java.nio.charset.StandardCharsets.UTF_16LE, java.nio.charset.StandardCharsets.UTF_16BE}) {
                        boolean rejected = false;
                        try {
                            com.khuongnd.dexkids.journey.GpxReplayFeed.fromGpx(new java.io.ByteArrayInputStream(unsafe.getBytes(encoding)));
                        } catch (IllegalArgumentException expected) {
                            rejected = expected.getCause().getMessage().contains("declarations are forbidden");
                        }
                        require(rejected, "Unsafe declaration accepted: " + encoding);
                    }
                    result.putString("security", "DTD/entity rejected in UTF8/UTF16/UTF16LE/UTF16BE");
                } catch (Throwable failure) {
                    throw new AssertionError(android.util.Log.getStackTraceString(failure));
                }
            } else if ("gpx_sample".equals(mode) || "gpx_picker".equals(mode)) {
                validateGpxJourney(result);
            } else if ("permission".equals(mode)) {
                validatePermissionRefusal(result);
            } else if ("p0_hcm".equals(mode) || "p0_child_mic_privacy".equals(mode) || "p0_ai_cache".equals(mode) || "p0_perf_ab".equals(mode) || "p0_live".equals(mode) || "p0_live_nearby".equals(mode) || "p0_route".equals(mode)) {
                P0Validation.run(this, mode, result);
            } else if ("single_display".equals(mode) || "single_display_mouse".equals(mode)) {
                validateSingleDisplay(result, "single_display_mouse".equals(mode));
            } else {
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            Intent intent = new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            parent = startActivitySync(intent);
            boolean[] click = new boolean[2];
            runOnMainSync(() -> {
                Button preview = findPreview(parent.getWindow().getDecorView());
                click[0] = preview != null;
                if (preview != null) click[1] = preview.performClick();
            });
            require(click[0], "Explicit parent preview button absent");
            require(click[1], "Parent preview click failed");
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null, "Child Activity did not start");
            SystemClock.sleep(3000);
            checkDemo(child);
            File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-recreation");
            require(directory.isDirectory() || directory.mkdirs(), "Screenshot directory unavailable");
            result.putString("before_png", screenshot(directory, "before.png"));
            Bundle before = savedState(child);
            long deadline = before.getLong(DEADLINE, 0);
            Object originalFeed = journey(child);
            double beforeDistance = distance(originalFeed);
            Activity old = child;
            removeMonitor(monitor);
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            runOnMainSync(old::recreate);
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null && child != old, "Recreation did not create a distinct instance");
            SystemClock.sleep(3000);
            checkDemo(child);
            result.putString("after_png", screenshot(directory, "after.png"));
            long restored = savedState(child).getLong(DEADLINE, 0);
            Object afterFeed = journey(child);
            result.putLong("deadline_before", deadline);
            result.putLong("deadline_after", restored);
            result.putDouble("distance_before", beforeDistance);
            result.putDouble("distance_after", distance(afterFeed));
            result.putBoolean("same_feed", afterFeed == originalFeed);
            require(deadline > SystemClock.elapsedRealtime(), "Valid session deadline missing");
            require(afterFeed == originalFeed, "Journey feed reset on recreation");
            require(distance(afterFeed) >= beforeDistance, "Journey distance decreased on recreation");
            require(restored == deadline, "Session deadline changed: " + deadline + " -> " + restored);
            result.putString("assertions", "distinct_instance,absolute_deadline_preserved,journey_feed_and_distance_preserved,demo_gps_inactive");
            }
            result.putString("qa_status", "PASS");
            result.putString("visual_status", "NOT_VERIFIED: review screenshot artifacts");
            code = 0;
        } catch (Throwable failure) {
            result.putString("qa_status", "FAIL");
            result.putString("qa_error", failure.toString());
        } finally {
            try {
                runOnMainSync(() -> {
                    if (child != null && !child.isFinishing()) child.finish();
                    if (parent != null && !parent.isFinishing()) parent.finish();
                });
                if (monitor != null) removeMonitor(monitor);
            } catch (Throwable cleanup) {
                code = 1;
                result.putString("qa_status", "FAIL");
                result.putString("cleanup_error", cleanup.toString());
            }
            result.putLong("elapsed_ms", SystemClock.elapsedRealtime() - started);
            finish(code, result);
        }
    }

    private Object field(Object object, String name) throws Exception {
        Field f = object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(object);
    }

    private void restorePreferences(android.content.SharedPreferences prefs,
            java.util.Map<String, ?> values) {
        android.content.SharedPreferences.Editor e = prefs.edit().clear();
        for (java.util.Map.Entry<String, ?> entry : values.entrySet()) {
            Object v = entry.getValue(); String k = entry.getKey();
            if (v instanceof Boolean) e.putBoolean(k, (Boolean)v);
            else if (v instanceof Integer) e.putInt(k, (Integer)v);
            else if (v instanceof Long) e.putLong(k, (Long)v);
            else if (v instanceof Float) e.putFloat(k, (Float)v);
            else if (v instanceof String) e.putString(k, (String)v);
            else throw new AssertionError("Unsupported QA preference type");
        }
        require(e.commit(), "Preference restore failed");
    }

    private void validateGeminiLive(Bundle result) throws Exception {
        android.content.Context context = getTargetContext();
        File fixture = new File(context.getFilesDir(), "qa-gemini-secret.json");
        android.content.SharedPreferences settings = context.getSharedPreferences("kids_ai_settings_v1", 0);
        android.content.SharedPreferences keys = context.getSharedPreferences("kids_ai_keys_v1", 0);
        java.util.Map<String, ?> oldSettings = settings.getAll(), oldKeys = keys.getAll();
        com.khuongnd.dexkids.ai.KidsAiKeys credentials = new com.khuongnd.dexkids.ai.KidsAiKeys(context);
        com.khuongnd.dexkids.ai.KidsAiProvider provider = com.khuongnd.dexkids.ai.KidsAiProvider.GEMINI;
        boolean hadKey = credentials.configured(provider);
        try {
            require(fixture.isFile(), "Missing app-private Gemini fixture");
            org.json.JSONObject config = new org.json.JSONObject(new String(
                    java.nio.file.Files.readAllBytes(fixture.toPath()), java.nio.charset.StandardCharsets.UTF_8));
            require(fixture.delete(), "Cannot remove secret fixture");
            credentials.save(provider, config.getString("key"));
            com.khuongnd.dexkids.ai.KidsAiSettings prefs = new com.khuongnd.dexkids.ai.KidsAiSettings(context);
            prefs.setEnabled(true); prefs.setCloudChildReply(false); prefs.setProvider(provider);
            prefs.setModel(provider, config.getString("model"));
            // Temporary test gate, not an assertion about account billing/free-tier eligibility.
            prefs.setFreeTierAcknowledged(provider, true); prefs.setActive(provider, true);
            prefs.setActive(com.khuongnd.dexkids.ai.KidsAiProvider.GROQ, false);
            java.util.List<com.khuongnd.dexkids.ai.KidsQuiz> quizzes =
                new com.khuongnd.dexkids.ai.KidsAiGateway(context).generate(
                    "osm:way:530247697",
                    "Công viên Xuân An là không gian xanh ở Long Khánh. Cây trong công viên cho chúng ta bóng mát.",
                    new com.khuongnd.dexkids.ai.KidsQuiz("Cây xanh cho chúng ta điều gì?",
                        "Cây trong công viên cho chúng ta bóng mát.", "Con thích nhìn cây xanh không?"), 4);
            require(quizzes.size() >= 10, "Gateway did not validate ten quizzes");
            result.putInt("validated_quizzes", quizzes.size());
            result.putString("provider", "GEMINI");
            result.putString("model", config.getString("model"));
            result.putString("payload", "Authored sourced fact and age only; no GPS/audio/child reply");
        } finally {
            fixture.delete();
            if (!hadKey) credentials.delete(provider);
            restorePreferences(keys, oldKeys);
            restorePreferences(settings, oldSettings);
        }
    }

    private void validateAudiencePicker(Bundle result) throws Exception {
        require(targetDisplay > 0, "Explicit DeX display required");
        android.content.SharedPreferences prefs = getTargetContext().getSharedPreferences("parent_settings", 0);
        java.util.Map<String, ?> original = prefs.getAll();
        try {
            parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            android.accessibilityservice.AccessibilityServiceInfo service = getUiAutomation().getServiceInfo();
            service.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            getUiAutomation().setServiceInfo(service);
            String[] ids = {"SAU", "ONG", "BOTH"};
            String[] labels = {"Cho Sâu (4 tuổi)", "Cho Ong (3 tuổi)", "Cả Sâu và Ong cùng xem (3–4 tuổi)"};
            for (int i = 0; i < ids.length; i++) {
                String buttonText = "Chọn người xem · " + new com.khuongnd.dexkids.ParentSettings(getTargetContext()).getAudienceLabel();
                runOnMainSync(() -> require(findButton(parent.getWindow().getDecorView(), buttonText).performClick(), "Audience picker click failed"));
                boolean clicked = false; long until = SystemClock.elapsedRealtime() + 5000;
                while (!clicked && SystemClock.elapsedRealtime() < until) {
                    java.util.List<android.view.accessibility.AccessibilityWindowInfo> windows = getUiAutomation().getWindowsOnAllDisplays().get(targetDisplay);
                    if (windows != null) for (android.view.accessibility.AccessibilityWindowInfo window : windows) {
                        android.view.accessibility.AccessibilityNodeInfo root = window.getRoot();
                        if (root == null) continue;
                        for (android.view.accessibility.AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(labels[i])) {
                            if (labels[i].contentEquals(node.getText()) && node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)) { clicked = true; break; }
                        }
                        if (clicked) break;
                    }
                    if (!clicked) SystemClock.sleep(100);
                }
                require(clicked, "Visible audience option not clickable on DeX");
                SystemClock.sleep(250);
                require(ids[i].equals(prefs.getString("audience_mode", "")), "UI choice did not persist");
                result.putString(ids[i], "PASS: visible picker choice persisted on same DeX display");
            }
        } finally { restorePreferences(prefs, original); }
    }

    private void validateAudienceAudio(Bundle result) throws Exception {
        require(targetDisplay > 0, "Explicit DeX display required");
        android.content.SharedPreferences prefs = getTargetContext().getSharedPreferences("parent_settings", 0);
        java.util.Map<String, ?> original = prefs.getAll();
        android.media.AudioManager audio = (android.media.AudioManager)getTargetContext().getSystemService("audio");
        int volume = audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
        try {
            for (String audience : new String[]{"SAU", "ONG", "BOTH"}) {
                require(prefs.edit().putString("audience_mode", audience).putBoolean("ambient_music", true)
                    .putBoolean("audio_effects", true).putBoolean("offline_tts", true)
                    .putBoolean("child_mic_optin", false).putBoolean("audio_only", false).commit(), "Fixture settings failed");
                monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
                parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                com.khuongnd.dexkids.ParentSettings selected = new com.khuongnd.dexkids.ParentSettings(getTargetContext());
                require(selected.getActiveAge() == ("SAU".equals(audience) ? 4 : 3), "Audience age mismatch");
                runOnMainSync(() -> require(findPreview(parent.getWindow().getDecorView()).performClick(), "Demo click failed"));
                child = waitForMonitorWithTimeout(monitor, 10000);
                require(child != null && child.getDisplay().getDisplayId() == targetDisplay, "Child missing on DeX");
                SystemClock.sleep(3000);
                Object sound = field(child, "soundscape");
                require(((java.util.Set<?>)field(sound, "loaded")).size() == 6, "Six SoundPool samples not loaded");
                require(((Number)field(sound, "musicStream")).intValue() > 0, "Ambient stream not started");
                long until = SystemClock.elapsedRealtime() + 160000;
                com.khuongnd.dexkids.story.ChildEngagementMetrics.Snapshot[] snapshot = {null};
                boolean[] speechDuck = {false};
                while (SystemClock.elapsedRealtime() < until) {
                    runOnMainSync(() -> {
                        try {
                            snapshot[0] = ((com.khuongnd.dexkids.story.ChildEngagementMetrics)field(child, "engagementMetrics")).snapshot();
                            if ((Boolean)field(sound, "speechActive")) {
                                require(((Number)field(sound, "musicStream")).intValue() == 0, "Ambient overlaps speech");
                                speechDuck[0] = true;
                            }
                            String line = (String)field(child, "subtitleText");
                            if (line != null && line.contains("CAPYBARA") && line.contains("DÀNH CHO")) {
                                if ("SAU".equals(audience)) require(!line.contains("Ong"), "Wrong child in Sau mode");
                                if ("ONG".equals(audience)) require(!line.contains("Sâu"), "Wrong child in Ong mode");
                            }
                        } catch (Exception e) { throw new AssertionError(e); }
                    });
                    if (snapshot[0].starts() >= 3 && snapshot[0].resolutions() >= 3) break;
                    SystemClock.sleep(250);
                }
                require(snapshot[0].starts() >= 3 && snapshot[0].resolutions() >= 3, "Three actual beats did not complete");
                require(speechDuck[0], "No production speech duck observed");
                require(snapshot[0].voiceUnavailable() == 0, "A beat silently fell back without voice");
                if ("SAU".equals(audience)) require(snapshot[0].ongBeats() == 0 && snapshot[0].togetherBeats() == 0, "Solo metrics mismatch");
                if ("ONG".equals(audience)) require(snapshot[0].sauBeats() == 0 && snapshot[0].togetherBeats() == 0, "Solo metrics mismatch");
                if ("BOTH".equals(audience)) require(snapshot[0].sauBeats() > 0 && snapshot[0].ongBeats() > 0 && snapshot[0].togetherBeats() > 0, "Unbalanced both rotation");
                result.putString(audience + "_metrics", snapshot[0].toString());
                openParentMenu();
                require((Boolean)field(sound, "paused") && ((Number)field(sound, "musicStream")).intValue() == 0, "F10 did not pause ambient");
                runOnMainSync(() -> parentDialog().getButton(android.app.AlertDialog.BUTTON_NEGATIVE).performClick());
                SystemClock.sleep(700);
                require(!(Boolean)field(sound, "paused"), "Continue did not resume soundscape");
                runOnMainSync(() -> child.finish());
                SystemClock.sleep(700);
                require((Boolean)field(sound, "disposed"), "SoundPool not disposed");
                removeMonitor(monitor); monitor = null;
            }
            require(audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) == volume, "Device volume changed");
            result.putString("audio_contract", "6 loaded samples, ambient starts, TTS ducks, F10 pauses, Continue resumes, shutdown disposes, volume preserved");
        } finally { restorePreferences(prefs, original); }
    }

    private void validateParentControls(Bundle result) throws Exception {
        android.content.SharedPreferences prefs = getTargetContext()
                .getSharedPreferences("parent_settings", android.content.Context.MODE_PRIVATE);
        boolean hadAudioOnly = prefs.contains("audio_only");
        boolean oldAudioOnly = prefs.getBoolean("audio_only", false);
        android.content.SharedPreferences ai = getTargetContext().getSharedPreferences(
                "kids_ai_settings_v1", android.content.Context.MODE_PRIVATE);
        boolean hadAi = ai.contains("ai_quizzes"), oldAi = ai.getBoolean("ai_quizzes", true);
        boolean hadCloud = ai.contains("child_text_cloud_explicit");
        boolean oldCloud = ai.getBoolean("child_text_cloud_explicit", false);
        try {
            require(prefs.edit().putBoolean("audio_only", false).commit(), "Cannot set animated fixture");
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runOnMainSync(() -> {
                Button preview = findPreview(parent.getWindow().getDecorView());
                require(preview != null && preview.performClick(), "DEMO button missing");
            });
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null, "DEMO child missing");
            SystemClock.sleep(1500);
            long deadline = savedState(child).getLong(DEADLINE, 0);
            openParentMenu();
            double paused = glDistance();
            SystemClock.sleep(700);
            require(Math.abs(glDistance() - paused) < 0.001, "Journey advances while Parent menu is open");
            runOnMainSync(() -> require(parentDialog().getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                    .performClick(), "Continue button failed"));
            SystemClock.sleep(700);
            require(glDistance() > paused, "Continue did not resume journey");
            require(savedState(child).getLong(DEADLINE, 0) == deadline, "Parent menu reset session deadline");
            result.putString("pause_resume", "PASS: frozen distance, Continue advances, deadline unchanged");
            openParentMenu();
            boolean[] selected = {false};
            runOnMainSync(() -> {
                android.widget.ListView list = parentDialog().getListView();
                require(list != null && list.isShown() && list.getChildCount() > 0,
                        "Parent action list is hidden; Audio-only/AI actions inaccessible");
                selected[0] = list.performItemClick(list.getChildAt(0), 0, list.getAdapter().getItemId(0));
            });
            require(selected[0] && prefs.getBoolean("audio_only", false), "Visible Audio-only action failed");
            result.putString("audio_only_toggle", "PASS: visible parent list toggled preference");
            SystemClock.sleep(350);
            if (oldAi && !oldCloud) {
                openParentMenu();
                runOnMainSync(() -> {
                    android.widget.ListView list = parentDialog().getListView();
                    require(list != null && list.isShown() && list.getChildCount() > 1,
                            "AI disable action not visible");
                    require(list.getAdapter().getItem(1).toString().startsWith("Tắt AI Kids"),
                            "Unexpected AI action; refusing another selection");
                    require(list.performItemClick(list.getChildAt(1), 1, list.getAdapter().getItemId(1)),
                            "AI disable action failed");
                });
                require(!ai.getBoolean("ai_quizzes", true)
                        && !ai.getBoolean("child_text_cloud_explicit", false), "AI disable not immediate");
                result.putString("ai_disable", "PASS: visible action immediately disables AI/cloud sharing");
            } else result.putString("ai_disable", "NOT_VERIFIED: existing AI/consent settings preserved");
        } finally {
            android.content.SharedPreferences.Editor edit = prefs.edit();
            if (hadAudioOnly) edit.putBoolean("audio_only", oldAudioOnly);
            else edit.remove("audio_only");
            require(edit.commit(), "Cannot restore Audio-only setting");
            android.content.SharedPreferences.Editor aiEdit = ai.edit();
            if (hadAi) aiEdit.putBoolean("ai_quizzes", oldAi); else aiEdit.remove("ai_quizzes");
            if (hadCloud) aiEdit.putBoolean("child_text_cloud_explicit", oldCloud);
            else aiEdit.remove("child_text_cloud_explicit");
            require(aiEdit.commit(), "Cannot restore AI settings");
        }
    }

    private android.app.AlertDialog parentDialog() {
        try {
            Field dialog = child.getClass().getDeclaredField("parentDialog");
            dialog.setAccessible(true);
            return (android.app.AlertDialog) dialog.get(child);
        } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
    }

    private void openParentMenu() {
        runOnMainSync(() -> child.dispatchKeyEvent(new android.view.KeyEvent(
                android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_F10)));
        SystemClock.sleep(350);
        runOnMainSync(() -> require(parentDialog() != null && parentDialog().isShowing(), "Parent menu missing"));
    }

    private double glDistance() throws Exception {
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
        double[] value = {0};
        com.badlogic.gdx.Gdx.app.postRunnable(() -> {
            try { value[0] = distance(journey(child)); }
            catch (Throwable failure) { error.set(failure); }
            finally { done.countDown(); }
        });
        require(done.await(5, java.util.concurrent.TimeUnit.SECONDS), "GL distance read timed out");
        require(error.get() == null, "GL distance read failed: " + error.get());
        return value[0];
    }

    private void validateAudioPlayback(Bundle result) throws Exception {
        parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        android.media.AudioManager audio = (android.media.AudioManager) getTargetContext()
                .getSystemService(android.content.Context.AUDIO_SERVICE);
        int originalVolume = audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
        boolean originallyMuted = audio.isStreamMute(android.media.AudioManager.STREAM_MUSIC);
        result.putInt("original_media_volume", originalVolume);
        result.putInt("media_max_volume", audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC));
        java.util.ArrayList<String> outputs = new java.util.ArrayList<>();
        for (android.media.AudioDeviceInfo output : audio.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS))
            outputs.add(String.valueOf(output.getType()));
        result.putString("available_output_types", String.join(",", outputs));
        result.putBoolean("on_device_asr_service_available",
                android.speech.SpeechRecognizer.isOnDeviceRecognitionAvailable(getTargetContext()));
        com.khuongnd.dexkids.OfflineVietnameseNarrator[] narrator = {null};
        try {
            if (originalVolume == 0) {
                audio.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.ADJUST_UNMUTE, 0);
                audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        Math.max(1, audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC) / 4), 0);
            }
            result.putInt("playback_media_volume", audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC));
            android.media.ToneGenerator tone = new android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_MUSIC, 25);
            try {
                // PROP_BEEP has an intrinsic short duration, even with a longer
                // duration argument. Use a continuous tone for the active-media sample.
                require(tone.startTone(android.media.ToneGenerator.TONE_SUP_DIAL, 1000),
                        "Local test tone did not start");
                SystemClock.sleep(200);
                result.putBoolean("media_active_during_tone", audio.isMusicActive());
                require(audio.isMusicActive(), "Media playback not active during local tone");
                SystemClock.sleep(1000);
                result.putString("local_tone", "PASS: generated tone and active media playback");
            } finally { tone.release(); }
            runOnMainSync(() -> narrator[0] = new com.khuongnd.dexkids.OfflineVietnameseNarrator(getTargetContext()));
            Field ready = narrator[0].getClass().getDeclaredField("ready");
            ready.setAccessible(true);
            long until = SystemClock.elapsedRealtime() + 15000;
            while (!ready.getBoolean(narrator[0]) && SystemClock.elapsedRealtime() < until)
                SystemClock.sleep(100);
            require(ready.getBoolean(narrator[0]), "Local TTS engine did not initialize");
            Field engineField = narrator[0].getClass().getDeclaredField("engine");
            engineField.setAccessible(true);
            android.speech.tts.TextToSpeech engine = (android.speech.tts.TextToSpeech) engineField.get(narrator[0]);
            result.putString("tts_engine", engine.getDefaultEngine());
            int vietnameseOffline = 0;
            if (engine.getVoices() != null) for (android.speech.tts.Voice voice : engine.getVoices())
                if ("vi".equals(voice.getLocale().getLanguage()) && !voice.isNetworkConnectionRequired())
                    vietnameseOffline++;
            result.putInt("vietnamese_offline_voices", vietnameseOffline);
            require(vietnameseOffline > 0, "No offline Vietnamese TTS voice installed; no network fallback");
            java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.CountDownLatch finished = new java.util.concurrent.CountDownLatch(1);
            boolean[] queued = {false};
            runOnMainSync(() -> {
                narrator[0].setParentApproved(true);
                queued[0] = narrator[0].speakReviewed(
                        "Xin chào! Đây là kiểm tra âm thanh tiếng Việt của DeX Kids Adventure. Chúc bạn một ngày vui vẻ!",
                        () -> { started.countDown(); return kotlin.Unit.INSTANCE; },
                        () -> { finished.countDown(); return kotlin.Unit.INSTANCE; },
                        () -> kotlin.Unit.INSTANCE);
            });
            require(queued[0], "Production offline narrator rejected audio focus or voice");
            require(started.await(10, java.util.concurrent.TimeUnit.SECONDS), "TTS playback never started");
            require(finished.await(30, java.util.concurrent.TimeUnit.SECONDS), "TTS playback did not finish");
            result.putString("audio_playback", "Production offline Vietnamese narrator queued, started and finished");
            // Production onFinished also handles cancellation/errors. Separately require
            // a native onDone for the same selected offline voice, without recording audio.
            require(engine.getVoice() != null && "vi".equals(engine.getVoice().getLocale().getLanguage())
                    && !engine.getVoice().isNetworkConnectionRequired(), "Selected TTS voice is not offline Vietnamese");
            java.util.concurrent.CountDownLatch nativeDone = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicBoolean completed = new java.util.concurrent.atomic.AtomicBoolean(false);
            engine.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
                @Override public void onStart(String id) {}
                @Override public void onDone(String id) {
                    if ("fold3_qa_native_done".equals(id)) { completed.set(true); nativeDone.countDown(); }
                }
                @Override public void onError(String id) {
                    if ("fold3_qa_native_done".equals(id)) nativeDone.countDown();
                }
            });
            android.media.AudioFocusRequest focus = new android.media.AudioFocusRequest.Builder(
                    android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(new android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    .setOnAudioFocusChangeListener(change -> {}).build();
            try {
                require(audio.requestAudioFocus(focus) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED,
                        "Native TTS verification audio focus denied");
                require(engine.speak("Kiểm tra âm thanh đã hoàn tất. Cảm ơn bạn!",
                        android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "fold3_qa_native_done")
                        == android.speech.tts.TextToSpeech.SUCCESS, "Native TTS queue failed");
                require(nativeDone.await(20, java.util.concurrent.TimeUnit.SECONDS) && completed.get(),
                        "Offline Vietnamese native playback failed or timed out");
                result.putBoolean("native_tts_on_done", true);
            } finally { audio.abandonAudioFocusRequest(focus); }
            result.putString("audibility_quality", "NOT_VERIFIED: no human listening or recording");
        } finally {
            runOnMainSync(() -> { if (narrator[0] != null) narrator[0].shutdown(); });
            if (originalVolume == 0) {
                audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, originalVolume, 0);
                if (originallyMuted) audio.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.ADJUST_MUTE, 0);
            }
            result.putInt("restored_media_volume", audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC));
        }
    }

    private void validatePhysicalSoak(Bundle result) throws Exception {
        require(targetDisplay > 0, "Physical soak requires explicit external display");
        android.content.SharedPreferences prefs = getTargetContext()
                .getSharedPreferences("parent_settings", android.content.Context.MODE_PRIVATE);
        boolean hadDuration = prefs.contains("session_minutes");
        int oldDuration = prefs.getInt("session_minutes", 30);
        require(prefs.edit().putInt("session_minutes", 60).commit(), "Cannot set soak duration");
        try {
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runOnMainSync(() -> {
                Button preview = findPreview(parent.getWindow().getDecorView());
                require(preview != null && preview.performClick(), "DEMO launch missing");
            });
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null && child.getDisplay().getDisplayId() == targetDisplay,
                    "Soak child must stay on external display");
            SystemClock.sleep(3000);
            checkDemo(child);
            result.putInt("gl_width", com.badlogic.gdx.Gdx.graphics.getWidth());
            result.putInt("gl_height", com.badlogic.gdx.Gdx.graphics.getHeight());
            require(com.badlogic.gdx.Gdx.graphics.getWidth() >= 1900
                    && com.badlogic.gdx.Gdx.graphics.getHeight() >= 900,
                    "Physical soak requires maximized FullHD DeX window");
            long deadline = savedState(child).getLong(DEADLINE, 0);
            require(deadline > SystemClock.elapsedRealtime() + 3_590_000L,
                    "60-minute session deadline missing");
            int checks = 0;
            long previousFrames = -1;
            while (SystemClock.elapsedRealtime() < deadline - 5000) {
                checkDemo(child);
                require(child.getDisplay().getDisplayId() == targetDisplay, "Child moved off DeX");
                java.util.concurrent.CountDownLatch sampled = new java.util.concurrent.CountDownLatch(1);
                long[] frames = {-1};
                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                    try {
                        com.khuongnd.dexkids.game.KidsGame game = (com.khuongnd.dexkids.game.KidsGame)
                                ((com.khuongnd.dexkids.KidsActivity) child).getApplicationListener();
                        Field metricField = game.getScreen().getClass().getDeclaredField("runMetrics");
                        metricField.setAccessible(true);
                        frames[0] = ((com.khuongnd.dexkids.game.RenderRunMetrics)
                                metricField.get(game.getScreen())).frames();
                    } catch (ReflectiveOperationException failure) {
                        android.util.Log.e("Fold3QA", "Cannot sample GL frame counter");
                    } finally { sampled.countDown(); }
                });
                require(sampled.await(5, java.util.concurrent.TimeUnit.SECONDS), "GL thread stalled");
                require(frames[0] > previousFrames, "GL frames stopped advancing");
                previousFrames = frames[0];
                checks++;
                android.util.Log.i("Fold3QA", "soak_check=" + checks);
                SystemClock.sleep(Math.min(30000, Math.max(1,
                        deadline - 5000 - SystemClock.elapsedRealtime())));
            }
            while (!child.isFinishing() && SystemClock.elapsedRealtime() < deadline + 15000)
                SystemClock.sleep(250);
            require(child.isFinishing(), "Session did not expire at 60-minute limit");
            com.khuongnd.dexkids.story.ChildEngagementMetrics.Snapshot[] counts = {null};
            runOnMainSync(() -> {
                try { counts[0] = ((com.khuongnd.dexkids.story.ChildEngagementMetrics)field(child, "engagementMetrics")).snapshot(); }
                catch (Exception error) { throw new AssertionError(error); }
            });
            result.putString("engagement_metrics", counts[0].toString());
            require(counts[0].starts() >= 80, "Too few real-time entertainment beats in one hour");
            require(counts[0].voiceUnavailable() == 0, "Silent voice fallback during endurance");
            result.putInt("external_display_checks", checks);
            result.putLong("last_sampled_gl_frames", previousFrames);
            result.putString("assertions", "60_min_demo,external_display,no_live_gps,focused_window,advancing_gl_frames,session_expiry");
        } finally {
            android.content.SharedPreferences.Editor edit = prefs.edit();
            if (hadDuration) edit.putInt("session_minutes", oldDuration);
            else edit.remove("session_minutes");
            require(edit.commit(), "Cannot restore session duration");
        }
    }

    private void validateGpxJourney(Bundle result) throws Exception {
        monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
        parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            try {
                java.lang.reflect.Method tools = parent.getClass().getDeclaredMethod("showAdvancedTools");
                tools.setAccessible(true);
                tools.invoke(parent);
            } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
        });
        SystemClock.sleep(500);
        android.view.accessibility.AccessibilityNodeInfo root = getUiAutomation(
                android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).getRootInActiveWindow();
        require(root != null && PACKAGE.contentEquals(root.getPackageName()), "Advanced tools dialog missing");
        String label = "gpx_sample".equals(mode) ? "Xem mẫu HCMC (mô phỏng)" : "GPX REPLAY · chọn file";
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> tools = root.findAccessibilityNodeInfosByText(label);
        require(tools.size() == 1, "Advanced GPX action absent or ambiguous");
        clicked[0] = tools.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
        require(clicked[0], "GPX parent action missing");
        // In picker mode, host automation selects a synthetic local fixture in real SAF.
        child = waitForMonitorWithTimeout(monitor, 60000);
        require(child != null, "GPX child did not launch");
        Object holder = journey(child);
        Object replay = null;
        long until = SystemClock.elapsedRealtime() + 10000;
        while (replay == null && SystemClock.elapsedRealtime() < until) {
            replay = holder.getClass().getMethod("replay").invoke(holder);
            SystemClock.sleep(100);
        }
        require(replay != null, "GPX load failed; no parsed replay installed");
        SystemClock.sleep(1200);
        double time = ((Number) replay.getClass().getMethod("replayTimeSeconds").invoke(replay)).doubleValue();
        require(time > 0, "GPX timeline not advancing");
        checkDemo(child);
        File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-" + mode);
        require(directory.isDirectory() || directory.mkdirs(), "GPX screenshot directory unavailable");
        result.putString("game_png", screenshot(directory, "loaded.png"));
        result.putDouble("replay_seconds", time);
        result.putString("assertions", "parent_action,parsed_replay_installed,timeline_advances,no_live_gps");
    }

    /** Runtime test: dashboard, child and parent menu share the only emulator display. */
    private void validateSingleDisplay(Bundle result, boolean viaMouse) throws Exception {
        monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
        parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        int parentDisplay = parent.getDisplay().getDisplayId();
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            Button start = findPreview(parent.getWindow().getDecorView());
            clicked[0] = start != null && start.performClick();
        });
        require(clicked[0], "Same-display start button not clickable");
        child = waitForMonitorWithTimeout(monitor, 10000);
        require(child != null, "KidsActivity missing after same-display start");
        require(child.getDisplay().getDisplayId() == parentDisplay,
                "Game launched on a different display than the parent dashboard");
        SystemClock.sleep(700);
        if (viaMouse) {
            boolean[] clickedMenu = new boolean[1];
            runOnMainSync(() -> {
                Button button = findButton(child.getWindow().getDecorView(), "Parents · menu");
                clickedMenu[0] = button != null && button.performClick();
            });
            require(clickedMenu[0], "One-click parent menu unavailable");
        } else {
            runOnMainSync(() -> child.dispatchKeyEvent(new android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_F10)));
        }
        SystemClock.sleep(450);
        Object listener = child.getClass().getMethod("getApplicationListener").invoke(child);
        java.lang.reflect.Field field = listener.getClass().getDeclaredField("parentMenuOpen");
        field.setAccessible(true);
        require(((java.util.concurrent.atomic.AtomicBoolean) field.get(listener)).get(),
                "F10 did not pause the journey or open parent menu");
        // Android 15 accessibility-root text discovery is asynchronous and
        // occasionally returns an older surface, even while the real dialog
        // is visible. Check the SAME Activity's actual visible native modal.
        boolean[] modalVisible = new boolean[1];
        for (int attempt = 0; attempt < 25 && !modalVisible[0]; attempt++) {
            runOnMainSync(() -> {
                try {
                    java.lang.reflect.Field dialogField = child.getClass().getDeclaredField("parentDialog");
                    dialogField.setAccessible(true);
                    android.app.AlertDialog dialog = (android.app.AlertDialog) dialogField.get(child);
                    modalVisible[0] = dialog != null && dialog.isShowing();
                } catch (ReflectiveOperationException failure) {
                    throw new AssertionError("Parent dialog reflection failed", failure);
                }
            });
            if (!modalVisible[0]) SystemClock.sleep(120);
        }
        require(modalVisible[0], "Parent modal not visible in same-display game");
        File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-single-display");
        require(directory.isDirectory() || directory.mkdirs(), "No screenshot directory");
        result.putString("menu_png", screenshot(directory, "parent-menu.png"));
        // Android 15 accessibility search can return zero/multiple text nodes for
        // AlertDialog material-styled buttons. Interact with the actual dialog's
        // positive Button instead of guessing an accessibility text node count.
        boolean[] pressedEnd = new boolean[1];
        runOnMainSync(() -> {
            try {
                java.lang.reflect.Field dialogField = child.getClass().getDeclaredField("parentDialog");
                dialogField.setAccessible(true);
                android.app.AlertDialog dialog = (android.app.AlertDialog) dialogField.get(child);
                if (dialog != null && dialog.isShowing()) {
                    android.widget.Button end = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                    pressedEnd[0] = end != null && "End adventure".contentEquals(end.getText())
                            && end.isEnabled() && end.performClick();
                }
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError("Parent dialog reflection failed", failure);
            }
        });
        require(pressedEnd[0], "Visible dialog positive End adventure button not clickable");
        SystemClock.sleep(450);
        require(child.isFinishing() || child.isDestroyed(), "Child did not close after parent action");
        result.putInt("display_id", parentDisplay);
        result.putString("assertions",
                "same_display_dashboard_game," + (viaMouse ? "one_click_mouse_menu" : "keyboard_f10_parent_menu") +
                ",journey_paused,stop_returns_to_dashboard");
    }

    private void validatePermissionRefusal(Bundle result) throws Exception {
        android.app.UiAutomation automation = getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        android.accessibilityservice.AccessibilityServiceInfo info = automation.getServiceInfo();
        info.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        automation.setServiceInfo(info);
        require(getTargetContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED, "Existing fine grant; test must not revoke/regrant it");
        parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            Button button = findButton(parent.getWindow().getDecorView(),
                    "BẮT ĐẦU · GPS thật và Capybara trò chuyện");
            clicked[0] = button != null && button.performClick();
        });
        require(clicked[0], "Actual parent permission button not clicked");
        SystemClock.sleep(2000);
        android.view.accessibility.AccessibilityNodeInfo root = automation.getRootInActiveWindow();
        require(root != null, "Permission test active window unavailable");
        result.putString("root_package", String.valueOf(root.getPackageName()));
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> deny = permissionDenyNodes(root);
        for (int attempt = 0; deny.isEmpty() && attempt < 4
                && String.valueOf(root.getPackageName()).contains("permissioncontroller"); attempt++) {
            if (!scrollPermissionDialog(root)) break;
            SystemClock.sleep(350);
            root = automation.getRootInActiveWindow();
            require(root != null && String.valueOf(root.getPackageName()).contains("permissioncontroller"),
                    "Permission window disappeared during scroll");
            deny = permissionDenyNodes(root);
        }
        String denialIdentity = "resource_id";
        if (deny.isEmpty()) {
            denialIdentity = "exact_refusal_text";
            deny = root.findAccessibilityNodeInfosByText("Don’t allow");
            if (deny.isEmpty()) deny = root.findAccessibilityNodeInfosByText("Don't allow");
            deny.removeIf(node -> node.getText() == null ||
                    !"don't allow".equals(node.getText().toString().replace('’', '\'').toLowerCase(java.util.Locale.ROOT)));
        }
        boolean permissionWindow = String.valueOf(root.getPackageName()).contains("permissioncontroller");
        require(!permissionWindow || !deny.isEmpty(), "Visible permission window but deny control inaccessible");
        boolean dialog = !deny.isEmpty();
        if (dialog) {
            require(permissionWindow, "Deny control outside permission controller");
            require(deny.size() == 1, "Ambiguous denial control");
            require(!root.findAccessibilityNodeInfosByText("DeX Kids Adventure").isEmpty(), "Unrelated permission dialog");
            require(deny.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK),
                    "Resource-ID denial ACTION_CLICK failed");
        }
        SystemClock.sleep(2000);
        require(getTargetContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED, "Unexpected fine grant");
        require(getTargetContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED, "Unexpected coarse grant");
        boolean[] explained = new boolean[1];
        runOnMainSync(() -> explained[0] = containsText(parent.getWindow().getDecorView(),
                "Cần quyền vị trí chính xác để nhận biết địa danh. Có thể xem DEMO."));
        File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-permission");
        require(directory.isDirectory() || directory.mkdirs(), "Permission evidence directory unavailable");
        result.putString("refusal_png", screenshot(directory, "permission-refused.png"));
        result.putBoolean("dialog_denied_by_node_action", dialog);
        result.putString("denial_identity", dialog ? denialIdentity : "already_refused_no_dialog");
        result.putBoolean("fine_denied", true);
        result.putBoolean("coarse_denied", true);
        require(explained[0], "Denied permission explanation missing after actual callback/resume");
        result.putString("assertions", "actual_parent_button,deny_only_node_ACTION_CLICK,fine_and_coarse_denied,parent_explanation_preserved");
    }

    private static java.util.List<android.view.accessibility.AccessibilityNodeInfo> permissionDenyNodes(
            android.view.accessibility.AccessibilityNodeInfo root) {
        for (String prefix : new String[] {String.valueOf(root.getPackageName()), "com.android.permissioncontroller"}) {
            for (String id : new String[] {"permission_deny_button", "permission_deny_and_dont_ask_again_button"}) {
                java.util.List<android.view.accessibility.AccessibilityNodeInfo> nodes =
                        root.findAccessibilityNodeInfosByViewId(prefix + ":id/" + id);
                if (!nodes.isEmpty()) return nodes;
            }
        }
        return new java.util.ArrayList<>();
    }

    private static boolean scrollPermissionDialog(android.view.accessibility.AccessibilityNodeInfo node) {
        if (node.isScrollable() && node.performAction(
                android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) return true;
        for (int i = 0; i < node.getChildCount(); i++) {
            android.view.accessibility.AccessibilityNodeInfo child = node.getChild(i);
            if (child != null && scrollPermissionDialog(child)) return true;
        }
        return false;
    }

    private static boolean containsText(View view, String expected) {
        if (view instanceof android.widget.TextView && expected.contentEquals(((android.widget.TextView) view).getText())) return true;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) if (containsText(group.getChildAt(i), expected)) return true;
        }
        return false;
    }

    private Object journey(Activity activity) throws Exception {
        Object listener = activity.getClass().getMethod("getApplicationListener").invoke(activity);
        Field field = listener.getClass().getDeclaredField("journey");
        field.setAccessible(true);
        return field.get(listener);
    }
    private double distance(Object feed) throws Exception {
        return ((Number) feed.getClass().getMethod("distanceMeters").invoke(feed)).doubleValue();
    }

    private Bundle savedState(Activity activity) {
        Bundle state = new Bundle();
        runOnMainSync(() -> callActivityOnSaveInstanceState(activity, state));
        return state;
    }

    private void checkDemo(Activity activity) throws Exception {
        require(!activity.isFinishing(), "Child Activity unexpectedly finishing");
        require(!activity.getIntent().getBooleanExtra(PACKAGE + ".extra.LIVE_GPS", false), "Live GPS enabled");
        for (String name : new String[] {"gps", "liveFeed"}) {
            Field field = activity.getClass().getDeclaredField(name);
            field.setAccessible(true);
            require(field.get(activity) == null, "Unexpected active " + name);
        }
        boolean[] focused = new boolean[1];
        runOnMainSync(() -> focused[0] = activity.getWindow().getDecorView().isShown()
                && activity.getWindow().getDecorView().hasWindowFocus());
        require(focused[0], "Child window hidden or unfocused; possible permission prompt");
    }

    private String screenshot(File directory, String name) throws Exception {
        // UiAutomation.takeScreenshot() captures display 0, not the DeX target.
        // Host must capture the external SurfaceFlinger display explicitly.
        if (targetDisplay > 0) return "NOT_CAPTURED: external screencap required";
        Bitmap bitmap = getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).takeScreenshot();
        require(bitmap != null, "Actual device screenshot unavailable");
        File output = new File(directory, name);
        try (FileOutputStream stream = new FileOutputStream(output)) {
            require(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream), "Screenshot encoding failed");
        } finally { bitmap.recycle(); }
        require(output.length() > 0, "Empty screenshot");
        return output.getAbsolutePath();
    }

    private static Button findPreview(View view) {
        return findButton(view, "XEM THỬ · hoạt hình DEMO");
    }

    private static Button findButton(View view, String text) {
        if (view instanceof Button && text.contentEquals(((Button) view).getText())) return (Button) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                Button found = findButton(group.getChildAt(index), text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
