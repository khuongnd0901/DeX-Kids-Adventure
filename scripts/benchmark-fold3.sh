#!/usr/bin/env bash
set -euo pipefail

# READ-ONLY Fold3 DeX benchmark capture. Never changes package, permissions, CPU/governor or settings.
# Run on operator-controlled device: ./scripts/benchmark-fold3.sh <adb-serial>
# Requires manual verification that game, DeX Assistant, Maps and navigation coexist.
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <explicit-adb-serial>" >&2
  exit 2
fi
SERIAL="$1"
adb -s "$SERIAL" get-state | grep -qx device
MODEL="$(adb -s "$SERIAL" shell getprop ro.product.model | tr -d '\r')"
echo "ADB target: $SERIAL model=$MODEL"
if [[ "$MODEL" != *"SM-F926"* && "$MODEL" != *"Galaxy Z Fold3"* ]]; then
  echo "Not a confirmed Z Fold3; refusing to mark as device benchmark." >&2
  exit 3
fi
mkdir -p ".agent/evidence/device-runs"
OUT=".agent/evidence/device-runs/fold3-$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$OUT"
echo "TARGET 60 minutes; ACTUAL capture status: running" > "$OUT/README.txt"
adb -s "$SERIAL" shell getprop ro.build.version.release > "$OUT/android-version.txt"
adb -s "$SERIAL" shell dumpsys display > "$OUT/displays-before.txt"
adb -s "$SERIAL" shell dumpsys package com.khuongnd.dexkids > "$OUT/package-before.txt"
date -u +%FT%TZ > "$OUT/started-utc.txt"
for i in $(seq 0 60); do
  {
    echo "timestamp_utc=$(date -u +%FT%TZ) minute=$i"
    adb -s "$SERIAL" shell dumpsys gfxinfo com.khuongnd.dexkids framestats
  } > "$OUT/gfxinfo-$i.txt"
  adb -s "$SERIAL" shell dumpsys meminfo com.khuongnd.dexkids > "$OUT/meminfo-$i.txt"
  adb -s "$SERIAL" shell dumpsys battery > "$OUT/battery-$i.txt"
  adb -s "$SERIAL" shell dumpsys thermalservice > "$OUT/thermal-$i.txt" || true
  if [[ $i -lt 60 ]]; then sleep 60; fi
done
date -u +%FT%TZ > "$OUT/ended-utc.txt"
echo "Capture completed; requires analyst to calculate ACTUAL FPS/P95/PSS and inspect all results." > "$OUT/README.txt"
echo "Evidence path: $OUT"
