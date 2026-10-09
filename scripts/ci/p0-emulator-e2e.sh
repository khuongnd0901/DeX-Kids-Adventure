#!/usr/bin/env bash
# P0/T-012: disposable GitHub AVD only. Never execute on a physical Fold3.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"
OUT="$ROOT/build/p0-emulator-evidence"
mkdir -p "$OUT"
[[ "$(adb shell getprop ro.kernel.qemu | tr -d '\r')" == "1" ]] ||
  { echo "REFUSED: only a disposable Android emulator may run this script" >&2; exit 3; }
adb wait-for-device
adb shell input keyevent KEYCODE_WAKEUP || true
APK=$(find android/build/outputs/apk/debug -name '*.apk' -type f | head -1)
TEST_APK=$(find android/build/outputs/apk/androidTest -name '*.apk' -type f | head -1)
test -n "$APK" && test -n "$TEST_APK"
adb install -r "$APK"
adb install -r "$TEST_APK"
adb logcat -c
RUNNER="com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner"

run_mode() {
  local mode="$1"
  echo "RUNNING $mode on emulator"
  adb shell am instrument -w -e mode "$mode" "$RUNNER" > "$OUT/$mode.txt" 2>&1
  cat "$OUT/$mode.txt"
  if ! grep -Fq "qa_status=PASS" "$OUT/$mode.txt"; then
    # Retain genuine failure evidence before stopping the disposable AVD.
    adb exec-out screencap -p > "$OUT/failure-$mode.png" || true
    adb pull "/sdcard/Android/data/com.khuongnd.dexkids/files/qa-single-display/parent-menu.png" \
      "$OUT/failure-parent-menu.png" >/dev/null 2>&1 || true
    adb logcat -d -v time > "$OUT/failure-logcat.txt" || true
    echo "FAIL: $mode returned no explicit PASS" >&2
    exit 4
  fi
  if grep -Eq 'qa_status=FAIL|qa_error=|INSTRUMENTATION_FAILED|PROCESS CRASHED' "$OUT/$mode.txt"; then
    echo "FAIL: instrument reported failure in $mode" >&2
    exit 4
  fi
}

run_mode single_display_mouse
# Default/denied child mic must never instantiate a recognizer in a disposable emulator.
adb shell pm revoke com.khuongnd.dexkids android.permission.RECORD_AUDIO >/dev/null 2>&1 || true
run_mode p0_child_mic_privacy
run_mode p0_ai_cache
run_mode p0_hcm
run_mode p0_route
adb pull "/sdcard/Android/data/com.khuongnd.dexkids/files/p0-t012/p0-route.png" "$OUT/p0-route.png" >/dev/null
test -s "$OUT/p0-route.png"
adb pull "/sdcard/Android/data/com.khuongnd.dexkids/files/p0-t012/p0-hcm.png"   "$OUT/p0-hcm.png" >/dev/null
test -s "$OUT/p0-hcm.png"

# A/B/A/B windows are read from the renderer's actual FrameProfiler.
# The test returns performance_gate=FAIL if <30FPS; collection success is NOT a 30FPS PASS.
run_mode p0_perf_ab
adb pull "/sdcard/Android/data/com.khuongnd.dexkids/files/p0-t012/p0-performance.png"   "$OUT/p0-performance.png" >/dev/null
test -s "$OUT/p0-performance.png"

# Grant ONLY inside a disposable emulator (NOT a real Fold3 or general local AVD).
# Restore even if the live test or injection fails.
cleanup() {
  adb shell pm revoke com.khuongnd.dexkids android.permission.ACCESS_FINE_LOCATION >/dev/null 2>&1 || true
  adb shell pm revoke com.khuongnd.dexkids android.permission.ACCESS_COARSE_LOCATION >/dev/null 2>&1 || true
  adb shell pm revoke com.khuongnd.dexkids android.permission.RECORD_AUDIO >/dev/null 2>&1 || true
}
trap cleanup EXIT
adb shell pm grant com.khuongnd.dexkids android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant com.khuongnd.dexkids android.permission.ACCESS_FINE_LOCATION
adb shell am instrument -w -e mode p0_live "$RUNNER" > "$OUT/p0_live.txt" 2>&1 &
INSTRUMENT_PID=$!
sleep 6
adb emu geo fix 106.69300 10.77479
sleep 3
adb emu geo fix 106.69310 10.77479
sleep 3
adb emu geo fix 106.69322 10.77479
wait "$INSTRUMENT_PID"
cat "$OUT/p0_live.txt"
if ! grep -Fq "qa_status=PASS" "$OUT/p0_live.txt"; then
  adb logcat -d -v time > "$OUT/failure-live-logcat.txt" || true
  echo "FAIL: live GPS fixture not accepted; see instrumentation diagnostics" >&2
  exit 4
fi
grep -Fq "p0_live=PASS" "$OUT/p0_live.txt"
adb pull "/sdcard/Android/data/com.khuongnd.dexkids/files/p0-t012/p0-live.png"   "$OUT/p0-live.png" >/dev/null
test -s "$OUT/p0-live.png"

# Move the disposable emulator to a sourced Dong Nai roadside OSM feature,
# *before* starting a fresh LIVE activity. No app-only fake-location shortcuts.
adb emu geo fix 107.34840 11.19079
sleep 3
adb shell am instrument -w -e mode p0_live_nearby "$RUNNER" > "$OUT/p0_live_nearby.txt" 2>&1 &
NEAR_PID=$!
sleep 6
# AndroidGpsSource requests minDistance=2 metres. Each synthetic step is
# 8-12m so the real LocationManager listener emits multiple distinct fixes.
adb emu geo fix 107.34848 11.19080
sleep 3
adb emu geo fix 107.34857 11.19082
sleep 3
adb emu geo fix 107.34846 11.19083
wait "$NEAR_PID"
cat "$OUT/p0_live_nearby.txt"
if ! grep -Fq 'p0_live_nearby=PASS' "$OUT/p0_live_nearby.txt" ||
    ! grep -Fq 'qa_status=PASS' "$OUT/p0_live_nearby.txt"; then
  adb logcat -d -v time > "$OUT/failure-nearby-logcat.txt" || true
  echo "FAIL: real GPS -> nearby OSM -> quiz integration" >&2
  exit 4
fi
adb pull "/sdcard/Android/data/com.khuongnd.dexkids/files/p0-t012/p0-live-nearby.png" "$OUT/p0-live-nearby.png" >/dev/null
test -s "$OUT/p0-live-nearby.png"

cleanup
trap - EXIT

adb logcat -d -v time > "$OUT/logcat.txt"
if grep -E 'FATAL EXCEPTION|Fatal signal|ANR in com.khuongnd.dexkids' "$OUT/logcat.txt" > "$OUT/fatal-signatures.txt"; then
  echo "Check fatal-signatures.txt: observed failure signatures, NOT an automatic PASS"
  exit 4
fi
cat > "$OUT/README.txt" <<'NOTE'
Scope: real Android emulator runtime. No physical Fold3/DeX acceptance.
p0_hcm: synthetic sample GPX advanced on real GL thread to sourced, UNREVIEWED HCMC POI.
p0_perf_ab: four 6-second windows, 180-frame rolling samples, Full/Minimal/Full/Minimal.
Performance FAIL may be recorded while QA data collection PASS.
p0_live: Android emulator GPS provider fixes; not real navigation or accuracy validation.
TTS voice: disabled by default; offline Vietnamese voice & focus require physical-device test.
Screen captures must be visually reviewed, not inferred from nonempty PNG.
NOTE
echo "P0 Android emulator cases executed; verify captured data in $OUT"
