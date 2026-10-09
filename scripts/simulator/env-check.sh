#!/usr/bin/env bash
source "$(dirname "$0")/common.sh"
parse "$@"
uname -a > "$OUT/host.txt"
java -version > "$OUT/java.txt" 2>&1
"$ADB_BIN" version > "$OUT/adb-version.txt"
"$ADB_BIN" devices -l > "$OUT/devices.txt"
adb shell getprop > "$OUT/device-properties.txt"
adb shell wm size > "$OUT/size.txt"
adb shell wm density > "$OUT/density.txt"
adb shell dumpsys display > "$OUT/display.txt"
adb shell dumpsys meminfo > "$OUT/system-memory.txt"
adb shell dumpsys SurfaceFlinger > "$OUT/surfaceflinger.txt"
if command -v "${EMULATOR_BIN:-emulator}" >/dev/null; then
  "${EMULATOR_BIN:-emulator}" -list-avds > "$OUT/avds.txt"
else
  echo 'BLOCKED: emulator executable not on PATH; connected device remains inspectable.' > "$OUT/avds.txt"
fi
echo 'Environment capture completed; no DeX or hardware acceptance implied.'
