# Physical Fold3 autonomous QA

T-021 uses the installed debug app and framework-only AndroidTest runner on an explicitly selected external DeX display. It does not inject GPS, grant location/microphone permissions, record sound or call AI providers. The `audio_playback` mode emits a fixed Vietnamese test sentence through the production offline narrator only when an offline Vietnamese voice exists. It first emits a one-second low-level local test tone; if media volume is zero, it temporarily selects one-quarter volume and restores the original volume/mute state in a finally block. Callback evidence does not certify human audibility or pronunciation.

Build from the current checkout using JDK17, Android SDK35 and the documented art dependencies:

```bash
./scripts/test-local.sh
./gradlew :android:assembleDebug :android:assembleDebugAndroidTest
adb -s <serial> install -r android/build/outputs/apk/debug/android-debug.apk
adb -s <serial> install -r android/build/outputs/apk/androidTest/debug/android-debug-androidTest.apk
./scripts/fold3-qa.sh <serial> <DeX-display-id> <local-output-directory>
```

The script runs parser, recreation/deadline continuity, F10, parent button, pause/Continue/deadline, visible Audio-only/AI actions, current advanced GPX menu, HCMC synthetic GPX/POI, route caption, microphone-denied privacy, local AI cache and GPS denial checks sequentially. A permission denial may change Android's USER_SET/USER_FIXED flags. Existing permission grants are never revoked to force a test; the denial check then fails with an explicit explanation. AI cache validation clears the local quiz cache, so use a fresh test installation or back it up first.

For actual full-display rendering, maximize the app on DeX first. The shell can launch the parent with `am start -W --display <id> --windowingMode 1 -n com.khuongnd.dexkids/.ParentActivity`. Confirm actual GL dimensions in test results; display resolution alone does not prove render resolution.

```bash
adb -s <serial> shell am instrument -w -r -e display_id <id> -e mode physical_soak \
  com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
adb -s <serial> shell am instrument -w -r -e display_id <id> -e mode audio_playback \
  com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
```

`physical_soak` temporarily selects a 60-minute DEMO session, checks a focused external window and advancing GL frames every 30 seconds, checks normal deadline expiry, and restores the original session-duration preference in a finally block. It rejects a small window and phone fallback. If the instrumentation process is killed, restore the duration manually from the recorded pre-test value; a finally block cannot run after process death. This is synthetic DEMO endurance, not an on-road or navigation/audio concurrency test.

`p0_perf_ab` accepts `-e perf_seconds 120` for four longer Full/Audio-only windows, but FrameProfiler still reports only the last 180 frames of each window. The cumulative `RenderMetrics` telemetry reports real GL cadence and a 1ms histogram P95 upper bound; Android `gfxinfo` is not GL FPS. Keep resource samples with actual elapsed timestamps and distinguish session expiry from a crash.

For SAF, run mode `gpx_picker` and select a uniquely named bundled synthetic GPX fixture through DocumentsUI. Keep the file until the test finishes, then delete only that fixture. Mode `gpx_sample` uses the current advanced-tools menu.

UiAutomation's default screenshot is suppressed for an explicit external target because it captures display 0. Capture the external SurfaceFlinger display instead: discover IDs using `dumpsys SurfaceFlinger --display-id`, then use `adb -s <serial> exec-out screencap -p -d <physical-display-id>`. Do not commit unrelated device screens or raw location/audio data.

After testing, remove the test APK, restore changed test preferences/permission flags, and return the app to ParentActivity on the same DeX display. Production acceptance for real GPS/POI geometry, Vietnamese ASR accuracy, human listening, Maps/Vietmap coexistence, external Assistant signing and release signing stays separate.
