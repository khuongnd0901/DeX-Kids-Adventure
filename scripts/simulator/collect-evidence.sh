#!/usr/bin/env bash
source "$(dirname "$0")/common.sh"
parse "$@"
PID=$(adb shell pidof "$PACKAGE" | tr -d '\r')
[[ "$PID" =~ ^[0-9]+$ ]] || { echo 'App process unavailable or ambiguous' >&2; exit 4; }
adb logcat -d --pid="$PID" -v threadtime > "$OUT/app-logcat.txt"
adb shell dumpsys meminfo "$PACKAGE" > "$OUT/meminfo.txt"
adb shell dumpsys gfxinfo "$PACKAGE" framestats > "$OUT/gfxinfo.txt"
adb shell dumpsys package "$PACKAGE" > "$OUT/package.txt"
adb shell dumpsys activity activities > "$OUT/activities.txt"
capture screenshot
printf 'source_checkout_sha=%s\n' "$(git -C "$ROOT" rev-parse HEAD)" >> "$OUT/run.txt"
echo 'Collected actual app evidence; source checkout SHA does not prove installed APK identity.'
