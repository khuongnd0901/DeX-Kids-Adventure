#!/usr/bin/env bash
source "$(dirname "$0")/common.sh"
parse "$@"
adb shell dumpsys activity activities > "$OUT/activities.txt"
capture screenshot
echo 'Screenshot captured; visual acceptance requires inspection.'
