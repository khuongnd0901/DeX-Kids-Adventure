#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/common.sh"
usage() {
  echo "Usage: $0 --serial SERIAL [--apk TEST_APK]"
  echo 'Installs test APK, opens explicit parent preview, recreates actual Activity, checks deadline.'
  echo 'Build :android:assembleDebugAndroidTest first. ADB_BIN supports Windows adb.exe.'
  echo 'Requires no active journey/soak. Captured PNGs still require visual review.'
}
APK="$ROOT/android/build/outputs/apk/androidTest/debug/android-debug-androidTest.apk"
parse "$@"
[[ -f "$APK" ]] || { echo "BLOCKED: test APK absent: $APK" >&2; exit 3; }
adb install -r "$(host_path "$APK")" > "$OUT/install.txt"
rg '^Success\r?$' "$OUT/install.txt" >/dev/null || { cat "$OUT/install.txt" >&2; exit 4; }
# Instrumentation controls only this project's activities and writes actual screenshots.
adb shell am instrument -w com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner | tee "$OUT/instrumentation.txt"
# Framework instrumentation may end the target process on finish; screenshots and
# qa_status remain required even when there is no live PID for optional log capture.
pid=$(adb shell pidof "$PACKAGE" | tr -d '\r' || true)
if [[ "$pid" =~ ^[0-9]+$ ]]; then
  adb logcat -d --pid="$pid" > "$OUT/app-logcat.txt"
fi
adb logcat -d -s KidsSession:I RenderMetrics:I > "$OUT/project-tagged-logcat.txt"
adb pull /sdcard/Android/data/com.khuongnd.dexkids/files/qa-recreation "$(host_path "$OUT/screenshots")" > "$OUT/pull.txt"
rg '^INSTRUMENTATION_RESULT: qa_status=PASS\r?$' "$OUT/instrumentation.txt" >/dev/null || { echo 'FAIL: exact instrumentation qa_status=PASS absent' >&2; exit 4; }
[[ -s "$OUT/screenshots/before.png" && -s "$OUT/screenshots/after.png" ]] || { echo 'FAIL: actual screenshots absent' >&2; exit 4; }
printf 'PASS: actual Activity recreation and preserved session deadline; PNG visual review required\n' | tee "$OUT/result.txt"
