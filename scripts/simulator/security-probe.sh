#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
SDK=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-/home/khuongnd/Android/Sdk}}
TOOLS="$SDK/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
JAVA_DIR=${JAVA_HOME:-/home/khuongnd/.local/opt/jdk17}
ADB_BIN=${ADB_BIN:-adb}
SERIAL=
BUILD_ONLY=false
TRUSTED=false
usage() {
  echo "Usage: $0 --build-only | --serial SERIAL [--trusted]"
  echo 'Builds a QA APK requesting only project signature CONTROL permission. Runtime needs active child.'
  echo 'Overrides: ANDROID_SDK_ROOT, BUILD_TOOLS_VERSION, JAVA_HOME, ADB_BIN (adb.exe supported).'
  echo 'QA key, APK and timestamped evidence are local under ignored build/simulator-artifacts/.'
}
while (($#)); do
 case "$1" in
 --help|-h) usage; exit 0;;
 --build-only) BUILD_ONLY=true; shift;;
 --trusted) TRUSTED=true; shift;;
 --serial) [[ $# -ge 2 ]] || { usage >&2; exit 2; }; SERIAL=$2; shift 2;;
 *) usage >&2; exit 2;;
 esac
done
$BUILD_ONLY || [[ -n "$SERIAL" ]] || { usage >&2; exit 2; }
OUT="$ROOT/build/simulator-artifacts/security-probe-$(date -u +%Y%m%dT%H%M%SZ)-$$"
mkdir -p "$OUT/classes" "$OUT/dex"
exec > >(tee "$OUT/run.log") 2>&1
printf 'started_utc=%s\nserial=%s\n' "$(date -u +%FT%TZ)" "$SERIAL"
export JAVA_HOME="$JAVA_DIR"
export PATH="$JAVA_DIR/bin:$PATH"
JAR="$SDK/platforms/android-35/android.jar"
"$TOOLS/aapt2" link -I "$JAR" --manifest "$ROOT/test-data/android-probe/AndroidManifest.xml" -o "$OUT/unsigned.apk"
"$JAVA_DIR/bin/javac" -source 8 -target 8 -bootclasspath "$JAR" -d "$OUT/classes" "$ROOT/test-data/android-probe/ProbeActivity.java"
"$TOOLS/d8" --lib "$JAR" --min-api 23 --output "$OUT/dex" "$OUT/classes/com/khuongnd/dexkids/qaprobe/ProbeActivity.class"
"$JAVA_DIR/bin/jar" uf "$OUT/unsigned.apk" -C "$OUT/dex" classes.dex
"$TOOLS/zipalign" -f 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
"$JAVA_DIR/bin/keytool" -genkeypair -keystore "$OUT/qa-only.keystore" -storepass qa-only-local -keypass qa-only-local -alias qa-probe -keyalg RSA -validity 30 -dname 'CN=DeX Kids Synthetic QA Probe'
if $TRUSTED; then
 [[ -f "$HOME/.android/debug.keystore" ]] || { echo 'BLOCKED: default local debug signing key unavailable'; exit 3; }
 "$TOOLS/apksigner" sign --ks "$HOME/.android/debug.keystore" --ks-key-alias androiddebugkey --ks-pass pass:android --key-pass pass:android --out "$OUT/probe.apk" "$OUT/aligned.apk"
else
 "$TOOLS/apksigner" sign --ks "$OUT/qa-only.keystore" --ks-key-alias qa-probe --ks-pass pass:qa-only-local --key-pass pass:qa-only-local --out "$OUT/probe.apk" "$OUT/aligned.apk"
fi
"$TOOLS/apksigner" verify --print-certs "$OUT/probe.apk" > "$OUT/probe-certificate.txt"
"$TOOLS/aapt" dump badging "$OUT/probe.apk" > "$OUT/apk-badging.txt"
echo "BUILD PASS: $OUT/probe.apk"
$BUILD_ONLY && exit 0
command -v "$ADB_BIN" >/dev/null
adb() { "$ADB_BIN" -s "$SERIAL" "$@"; }
host_path() { if [[ "$ADB_BIN" == *.exe ]]; then wslpath -w "$1"; else printf '%s\n' "$1"; fi; }
[[ $(adb get-state | tr -d '\r') == device ]]
resumed_child() { adb shell dumpsys activity activities > "$OUT/$1.txt"; rg '(mResumedActivity|topResumedActivity|ResumedActivity).*com.khuongnd.dexkids/(\.KidsActivity|com.khuongnd.dexkids.KidsActivity)' "$OUT/$1.txt"; }
resumed_child precondition || { echo 'BLOCKED: child session must be resumed before probe'; exit 3; }
# Every run uses a new QA certificate, so remove only this project-owned test package.
if adb shell pm path com.khuongnd.dexkids.qaprobe | tr -d '\r' | rg '^package:' >/dev/null; then
 adb uninstall com.khuongnd.dexkids.qaprobe > "$OUT/probe-uninstall.txt"
 rg '^Success' "$OUT/probe-uninstall.txt" >/dev/null
fi
adb install "$(host_path "$OUT/probe.apk")" > "$OUT/install.txt"
rg '^Success' "$OUT/install.txt" >/dev/null
adb shell dumpsys package com.khuongnd.dexkids > "$OUT/child-package.txt"
adb shell dumpsys package com.khuongnd.dexkids.qaprobe > "$OUT/probe-package.txt"
child_uid=$(sed -n 's/.*\(userId\|appId\)=\([0-9]*\).*/\2/p' "$OUT/child-package.txt" | head -1)
probe_uid=$(sed -n 's/.*\(userId\|appId\)=\([0-9]*\).*/\2/p' "$OUT/probe-package.txt" | head -1)
[[ -n "$child_uid" && -n "$probe_uid" && "$child_uid" != "$probe_uid" ]] || { echo 'FAIL: missing or matching UIDs'; exit 4; }
child_apk=$(adb shell pm path com.khuongnd.dexkids | tr -d '\r' | sed -n 's/^package://p' | head -1)
[[ -n "$child_apk" ]]
adb pull "$child_apk" "$(host_path "$OUT/child.apk")" > "$OUT/child-apk-pull.txt"
"$TOOLS/apksigner" verify --print-certs "$OUT/child.apk" > "$OUT/child-certificate.txt"
child_cert=$(sed -n 's/^Signer #1 certificate SHA-256 digest: //p' "$OUT/child-certificate.txt")
probe_cert=$(sed -n 's/^Signer #1 certificate SHA-256 digest: //p' "$OUT/probe-certificate.txt")
[[ -n "$child_cert" && -n "$probe_cert" ]] || { echo 'FAIL: missing signing certificates'; exit 4; }
if $TRUSTED; then
 [[ "$child_cert" == "$probe_cert" ]] || { echo 'FAIL: trusted test key does not match installed debug app'; exit 4; }
else
 [[ "$child_cert" != "$probe_cert" ]] || { echo 'FAIL: untrusted test unexpectedly shares certificate'; exit 4; }
fi
adb shell am start -W -n com.khuongnd.dexkids.qaprobe/.ProbeActivity > "$OUT/launch.txt"
rg 'Status: ok' "$OUT/launch.txt" >/dev/null
sleep 2
adb shell screencap -p /sdcard/dexkids-qa-probe.png
adb pull /sdcard/dexkids-qa-probe.png "$(host_path "$OUT/probe.png")"
adb shell rm /sdcard/dexkids-qa-probe.png
probe_pid=$(adb shell pidof com.khuongnd.dexkids.qaprobe | tr -d '\r')
[[ "$probe_pid" =~ ^[0-9]+$ ]]
adb logcat -d --pid="$probe_pid" -s DexKidsQAProbe:I > "$OUT/probe-logcat.txt"
rg 'DIRECT_LAUNCH_DENIED_SECURITY' "$OUT/probe-logcat.txt" >/dev/null || { echo 'FAIL: nonexported Activity rejection absent'; exit 4; }
if rg 'DIRECT_LAUNCH_UNEXPECTED_SUCCESS|DIRECT_LAUNCH_INCONCLUSIVE' "$OUT/probe-logcat.txt"; then exit 4; fi
adb shell input keyevent KEYCODE_BACK
sleep 2
if $TRUSTED; then
 rg 'CONTROL_GRANTED' "$OUT/probe-logcat.txt" >/dev/null || { echo 'FAIL: matching signature CONTROL permission not granted'; exit 4; }
 adb shell dumpsys activity activities > "$OUT/after-back.txt"
 rg '(topResumedActivity|ResumedActivity).*com.khuongnd.dexkids/\.ParentActivity' "$OUT/after-back.txt" >/dev/null || { echo 'FAIL: signed STOP did not return to parent'; exit 4; }
 printf 'PASS: matching signature/different UID granted CONTROL; STOP ended child session\n' | tee "$OUT/result.txt"
else
 rg 'CONTROL_DENIED' "$OUT/probe-logcat.txt" >/dev/null || { echo 'FAIL: untrusted sender obtained CONTROL'; exit 4; }
 resumed_child after-back || { echo 'FAIL: unauthorized STOP or launch disrupted active child session'; exit 4; }
 printf 'PASS: different signature CONTROL denied; child survived unauthorized STOP\n' | tee "$OUT/result.txt"
fi
echo "Evidence: $OUT"
