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
            if ("gpx_parser".equals(mode)) {
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
