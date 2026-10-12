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

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mode = arguments == null ? "recreation" : arguments.getString("mode", "recreation");
        start();
    }

    @Override public void onStart() {
        Bundle result = new Bundle();
        long started = SystemClock.elapsedRealtime();
        int code = 1;
        try {
            getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            if ("parent_gate".equals(mode)) {
                validateParentGate(result);
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
            } else if ("single_display".equals(mode)) {
                validateSingleDisplay(result);
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
            result.putString("assertions", "distinct_instance,absolute_deadline_preserved,journey_feed_and_distance_preserved,demo_gps_inactive,two_real_screenshots");
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

    private void validateParentGate(Bundle result) throws Exception {
        android.content.SharedPreferences prefs = getTargetContext().getSharedPreferences("parent_settings", 0);
        java.util.Map<String, ?> original = new java.util.HashMap<>(prefs.getAll());
        String[] keys = {"pin_credential", "pin_failures", "pin_retry_after", "audio_only"};
        try {
            prefs.edit().remove("pin_credential").putBoolean("audio_only", false).commit();
            parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            clickParentButton("Set parent PIN");
            SystemClock.sleep(250);
            fillSetupPin("2468", "2469");
            clickUi("Save PIN");
            SystemClock.sleep(250);
            require(prefs.getString("pin_credential", null) == null, "Mismatched PIN was saved");
            fillSetupPin("2468", "2468");
            clickUi("Save PIN");
            long setupUntil = SystemClock.elapsedRealtime() + 10000;
            while (prefs.getString("pin_credential", null) == null && SystemClock.elapsedRealtime() < setupUntil)
                SystemClock.sleep(100);
            require(com.khuongnd.dexkids.parent.ParentPin.verify("2468", prefs.getString("pin_credential", null)),
                    "Setup did not persist hashed PIN");
            clickParentButton("Remove parent PIN");
            SystemClock.sleep(250);
            submitPin("2468");
            require(prefs.getString("pin_credential", null) == null, "Authorized PIN removal failed");
            clickParentButton("Audio-only: OFF");
            require(prefs.getBoolean("audio_only", false), "Audio-only toggle failed");
            result.putBoolean("setup_mismatch_hash_remove_audio_toggle", true);
            runOnMainSync(parent::finish);
            prefs.edit().putString("pin_credential", com.khuongnd.dexkids.parent.ParentPin.encode("2468"))
                    .putInt("pin_failures", 0).putLong("pin_retry_after", 0).putBoolean("audio_only", true).commit();
            parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            SystemClock.sleep(500);
            requireDashboardLocked();
            submitPin("0000");
            requireDashboardLocked();
            require(prefs.getInt("pin_failures", 0) == 1, "Wrong PIN not counted");
            submitPin("0000");
            submitPin("0000");
            require(prefs.getLong("pin_retry_after", 0) > System.currentTimeMillis(), "PIN retry delay absent");
            submitPin("2468");
            requireDashboardLocked();
            // Expire only the synthetic fixture's cooldown; no waiting or production bypass.
            prefs.edit().putLong("pin_retry_after", 0).commit();
            submitPin("2468");
            boolean[] clicked = new boolean[1];
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            runOnMainSync(() -> {
                Button button = findPreview(parent.getWindow().getDecorView());
                clicked[0] = button != null && button.performClick();
            });
            require(clicked[0], "Correct PIN did not unlock dashboard");
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null, "Child missing");
            SystemClock.sleep(900);
            Object game = child.getClass().getMethod("getApplicationListener").invoke(child);
            Field audio = game.getClass().getDeclaredField("audioOnly");
            audio.setAccessible(true);
            require(((java.util.concurrent.atomic.AtomicBoolean)audio.get(game)).get(), "Audio-only setting not applied");
            Object feed = journey(child);
            double before = distance(feed);
            SystemClock.sleep(600);
            require(distance(feed) > before, "Audio-only stopped foreground journey");
            File dir = new File(getTargetContext().getExternalFilesDir(null), "qa-parent-gate");
            require(dir.isDirectory() || dir.mkdirs(), "Screenshot directory unavailable");
            result.putString("audio_png", screenshot(dir, "audio-only.png"));
            openChildGate();
            submitPin("0000");
            require(!child.isFinishing(), "Wrong PIN closed child");
            clickUi("Cancel");
            SystemClock.sleep(300);
            Field paused = game.getClass().getDeclaredField("parentMenuOpen");
            paused.setAccessible(true);
            require(!((java.util.concurrent.atomic.AtomicBoolean)paused.get(game)).get(), "Cancel did not resume journey");
            openChildGate();
            submitPin("2468");
            clickUi("End adventure");
            SystemClock.sleep(600);
            require(child.isFinishing() || child.isDestroyed(), "Authorized End failed");
            requireDashboardLocked();
            submitPin("2468");
            Activity old = parent;
            ActivityMonitor parentMonitor = addMonitor(PACKAGE + ".ParentActivity", null, false);
            runOnMainSync(old::recreate);
            parent = waitForMonitorWithTimeout(parentMonitor, 10000);
            removeMonitor(parentMonitor);
            require(parent != null && parent != old, "Parent recreation failed");
            SystemClock.sleep(300);
            requireDashboardLocked();
            result.putString("assertions", "wrong_pin_refused,three_attempt_delay,correct_pin_unlocks,cancel_resumes,protected_end,dashboard_relocks,recreation_relocks,audio_only_journey_advances");
        } finally {
            runOnMainSync(() -> {
                if (child != null) child.finish();
                if (parent != null) parent.finish();
            });
            android.content.SharedPreferences.Editor editor = prefs.edit();
            for (String key : keys) {
                Object value = original.get(key);
                if (value == null) editor.remove(key);
                else if (value instanceof String) editor.putString(key, (String)value);
                else if (value instanceof Integer) editor.putInt(key, (Integer)value);
                else if (value instanceof Long) editor.putLong(key, (Long)value);
                else if (value instanceof Boolean) editor.putBoolean(key, (Boolean)value);
            }
            require(editor.commit(), "Failed to restore parent settings");
            result.putBoolean("original_settings_restored", true);
        }
    }

    private void clickParentButton(String title) {
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            Button button = findButton(parent.getWindow().getDecorView(), title);
            clicked[0] = button != null && button.performClick();
        });
        require(clicked[0], "Parent action missing: " + title);
    }
    private void collectEditable(android.view.accessibility.AccessibilityNodeInfo node,
            java.util.List<android.view.accessibility.AccessibilityNodeInfo> fields) {
        if (node == null) return;
        if (node.isEditable()) fields.add(node);
        for (int i = 0; i < node.getChildCount(); i++) collectEditable(node.getChild(i), fields);
    }
    private void fillSetupPin(String pin, String confirmation) {
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> fields = new java.util.ArrayList<>();
        collectEditable(getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).getRootInActiveWindow(), fields);
        require(fields.size() == 2, "PIN confirmation fields missing");
        String[] values = {pin, confirmation};
        for (int i = 0; i < 2; i++) {
            Bundle args = new Bundle();
            args.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, values[i]);
            require(fields.get(i).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, args), "PIN setup input failed");
        }
    }
    private void requireDashboardLocked() {
        boolean[] locked = new boolean[1];
        runOnMainSync(() -> locked[0] = findPreview(parent.getWindow().getDecorView()) == null);
        require(locked[0], "Dashboard exposed behind PIN gate");
    }
    private void openChildGate() {
        runOnMainSync(() -> child.dispatchKeyEvent(new android.view.KeyEvent(
                android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_F10)));
        SystemClock.sleep(250);
    }
    private android.view.accessibility.AccessibilityNodeInfo editable(android.view.accessibility.AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.isEditable()) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            android.view.accessibility.AccessibilityNodeInfo found = editable(node.getChild(i));
            if (found != null) return found;
        }
        return null;
    }
    private void submitPin(String value) {
        android.view.accessibility.AccessibilityNodeInfo field = editable(getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).getRootInActiveWindow());
        require(field != null, "PIN input missing");
        Bundle args = new Bundle();
        args.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value);
        require(field.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, args), "PIN input failed");
        clickUi("Unlock");
        long until = SystemClock.elapsedRealtime() + 10000;
        while (SystemClock.elapsedRealtime() < until) {
            SystemClock.sleep(100);
            android.view.accessibility.AccessibilityNodeInfo root = getUiAutomation(
                    android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).getRootInActiveWindow();
            if (root == null) continue;
            java.util.List<android.view.accessibility.AccessibilityNodeInfo> buttons = root.findAccessibilityNodeInfosByText("Unlock");
            buttons.removeIf(n -> n.getText() == null || !"Unlock".equalsIgnoreCase(n.getText().toString()));
            if (buttons.isEmpty() || buttons.get(0).isEnabled()) return;
        }
        throw new AssertionError("PIN worker did not finish within 10 seconds");
    }
    private void clickUi(String title) {
        android.view.accessibility.AccessibilityNodeInfo root = getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).getRootInActiveWindow();
        require(root != null, "Active dialog absent");
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> matches = root.findAccessibilityNodeInfosByText(title);
        matches.removeIf(n -> n.getText() == null || !title.equalsIgnoreCase(n.getText().toString()));
        require(matches.size() == 1 && matches.get(0).performAction(
                android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK), "Dialog action missing: " + title);
    }

    private void validateGpxJourney(Bundle result) throws Exception {
        monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
        parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            Button button = findButton(parent.getWindow().getDecorView(), "gpx_sample".equals(mode)
                    ? "Start HCMC sample journey (preview)" : "Choose GPX file and start REPLAY");
            clicked[0] = button != null && button.performClick();
        });
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
        Object timeline = replay;
        clickChildButton("Pause");
        SystemClock.sleep(650);
        require((Boolean) timeline.getClass().getMethod("isPaused").invoke(timeline), "Pause failed");
        double frozen = ((Number) timeline.getClass().getMethod("replayTimeSeconds").invoke(timeline)).doubleValue();
        SystemClock.sleep(500);
        require(((Number) timeline.getClass().getMethod("replayTimeSeconds").invoke(timeline)).doubleValue() == frozen,
                "Paused timeline advanced");
        clickChildButton("Speed 1.0×");
        SystemClock.sleep(650);
        require(((Number) timeline.getClass().getMethod("playbackRate").invoke(timeline)).doubleValue() == 2,
                "Playback speed action failed");
        long deadline = savedState(child).getLong(DEADLINE);
        Activity old = child;
        removeMonitor(monitor);
        monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
        runOnMainSync(old::recreate);
        child = waitForMonitorWithTimeout(monitor, 10000);
        require(child != null && child != old, "GPX recreation failed");
        SystemClock.sleep(1000);
        require(journey(child) == holder, "GPX holder reset on recreation");
        require(holder.getClass().getMethod("replay").invoke(holder) == replay,
                "Parsed timeline replaced on recreation");
        require(savedState(child).getLong(DEADLINE) == deadline, "GPX session deadline changed");
        require(((Number) replay.getClass().getMethod("replayTimeSeconds").invoke(replay)).doubleValue() >= time,
                "GPX playback moved backwards on recreation");
        require((Boolean) timeline.getClass().getMethod("isPaused").invoke(timeline), "Paused state lost on recreation");
        require(((Number) timeline.getClass().getMethod("playbackRate").invoke(timeline)).doubleValue() == 2,
                "Playback speed lost on recreation");
        clickChildButton("Resume");
        SystemClock.sleep(900);
        require(((Number) timeline.getClass().getMethod("replayTimeSeconds").invoke(timeline)).doubleValue() > frozen,
                "Resume did not advance timeline");
        clickChildButton("Restart");
        SystemClock.sleep(200);
        require(((Number) timeline.getClass().getMethod("replayTimeSeconds").invoke(timeline)).doubleValue() < frozen,
                "Restart did not reset timeline");
        result.putBoolean("recreation_preserved", true);
        result.putBoolean("pause_resume_restart_rate", true);
        result.putDouble("replay_seconds", time);
        result.putString("assertions", "parent_action,parsed_replay_installed,timeline_advances,no_live_gps");
    }

    private void clickChildButton(String title) {
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            Button button = findButton(child.getWindow().getDecorView(), title);
            clicked[0] = button != null && button.performClick();
        });
        require(clicked[0], "Child control missing: " + title);
    }

    /** Runtime test: dashboard, child and parent menu share the only emulator display. */
    private void validateSingleDisplay(Bundle result) throws Exception {
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
        runOnMainSync(() -> child.dispatchKeyEvent(new android.view.KeyEvent(
                android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_F10)));
        SystemClock.sleep(450);
        Object listener = child.getClass().getMethod("getApplicationListener").invoke(child);
        java.lang.reflect.Field field = listener.getClass().getDeclaredField("parentMenuOpen");
        field.setAccessible(true);
        require(((java.util.concurrent.atomic.AtomicBoolean) field.get(listener)).get(),
                "F10 did not pause the journey or open parent menu");
        android.view.accessibility.AccessibilityNodeInfo window = getUiAutomation(
                android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
                .getRootInActiveWindow();
        require(window != null && !window.findAccessibilityNodeInfosByText("Parent controls").isEmpty(),
                "Parent modal not visible in same-display game");
        File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-single-display");
        require(directory.isDirectory() || directory.mkdirs(), "No screenshot directory");
        result.putString("menu_png", screenshot(directory, "parent-menu.png"));
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> end =
                window.findAccessibilityNodeInfosByText("End adventure");
        require(end.size() == 1, "No unique End adventure button");
        require(end.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK),
                "End adventure click failed");
        SystemClock.sleep(450);
        require(child.isFinishing() || child.isDestroyed(), "Child did not close after parent action");
        result.putInt("display_id", parentDisplay);
        result.putString("assertions",
                "same_display_dashboard_game,keyboard_f10_parent_menu,journey_paused,stop_returns_to_dashboard");
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
                    "Start LIVE GPS on this screen");
            clicked[0] = button != null && button.performClick();
        });
        require(clicked[0], "Actual parent permission button not clicked");
        SystemClock.sleep(2000);
        android.view.accessibility.AccessibilityNodeInfo root = automation.getRootInActiveWindow();
        require(root != null, "Permission test active window unavailable");
        result.putString("root_package", String.valueOf(root.getPackageName()));
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> deny = root.findAccessibilityNodeInfosByViewId(
                String.valueOf(root.getPackageName()) + ":id/permission_deny_button");
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
                "Location permission declined; no location data collected."));
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
        return findButton(view, "Start DEMO on this screen");
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
