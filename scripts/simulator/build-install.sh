#!/usr/bin/env bash
source "$(dirname "$0")/common.sh"
parse "$@"
cd "$ROOT"
if [[ -n "${GRADLE_BIN:-}" ]]; then BUILD="$GRADLE_BIN"; elif [[ -x ./gradlew ]]; then BUILD=./gradlew; else BUILD=gradle; fi
"$BUILD" --version > "$OUT/gradle-version.txt" 2>&1
"$BUILD" :core:test :desktop:classes :android:assembleDebug > "$OUT/build.txt" 2>&1
[[ -s "$APK" ]] || { echo "APK missing: $APK" >&2; exit 4; }
AAPT_BIN=${AAPT_BIN:-aapt}
command -v "$AAPT_BIN" >/dev/null || { echo 'Set AAPT_BIN to SDK build-tools aapt (or aapt.exe)' >&2; exit 3; }
AAPT_APK=$APK
[[ "$AAPT_BIN" == *.exe ]] && AAPT_APK=$(wslpath -w "$APK")
"$AAPT_BIN" dump badging "$AAPT_APK" > "$OUT/apk-badging.txt"
rg -q "^package: name='com.khuongnd.dexkids'" "$OUT/apk-badging.txt"
rg -q "^launchable-activity: name='com.khuongnd.dexkids.ParentActivity'" "$OUT/apk-badging.txt"
sha256sum "$APK" > "$OUT/apk-sha256.txt"
adb install -r "$(host_path "$APK")" > "$OUT/install.txt" 2>&1
rg '^Success\r?$' "$OUT/install.txt" >/dev/null || { cat "$OUT/install.txt" >&2; exit 4; }
adb shell am start -W -n "$PACKAGE/.ParentActivity" > "$OUT/launch.txt"
rg -q '^Status: ok' "$OUT/launch.txt"
capture parent-launch
echo 'Build/install/parent launch completed. Child preview requires consent via parent UI; inspect image before visual PASS.'
