#!/usr/bin/env bash
# Scoped framework instrumentation; does not grant location or change parent settings.
source "$(dirname "$0")/common.sh"
parse "$@"
TEST_APK="$ROOT/android/build/outputs/apk/androidTest/debug/android-debug-androidTest.apk"
[[ -f "$APK" && -f "$TEST_APK" ]] || { echo 'Build debug + AndroidTest APKs first' >&2; exit 3; }
adb install -r "$(host_path "$APK")" > "$OUT/install.txt"
adb install -r "$(host_path "$TEST_APK")" > "$OUT/install-test.txt"
cleanup() {
  adb uninstall "$PACKAGE.test" > "$OUT/remove-test.txt" 2>&1 || true
  adb shell am start -W -n "$PACKAGE/.ParentActivity" > "$OUT/restore-parent.txt" 2>&1 || true
}
trap cleanup EXIT
for mode in gpx_parser gpx_sample recreation single_display; do
  adb shell am instrument -w -e mode "$mode" "$PACKAGE.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner" > "$OUT/$mode.txt"
  cat "$OUT/$mode.txt"
  rg '^INSTRUMENTATION_RESULT: qa_status=PASS\r?$' "$OUT/$mode.txt" >/dev/null || exit 4
done
adb pull "/sdcard/Android/data/$PACKAGE/files/qa-gpx_sample" "$(host_path "$OUT/sample")" > "$OUT/pull-sample.txt"
adb pull "/sdcard/Android/data/$PACKAGE/files/qa-single-display" "$(host_path "$OUT/menu")" > "$OUT/pull-menu.txt"
adb logcat -d -s KidsSession:I RenderMetrics:I PoiScene:I > "$OUT/project-logcat.txt"
sha256sum "$APK" > "$OUT/apk-sha256.txt"
echo "Runtime assertions passed; screenshots require visual review: $OUT"
