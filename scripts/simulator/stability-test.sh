#!/usr/bin/env bash
source "$(dirname "$0")/common.sh"
parse "$@"
START=$(date +%s)
END=$((START + DURATION))
LOG_PID=
cleanup() { if [[ -n "$LOG_PID" ]]; then kill "$LOG_PID" 2>/dev/null || true; wait "$LOG_PID" 2>/dev/null || true; fi; }
trap cleanup EXIT
BASE_PID=
SAMPLE=0
while :; do
  NOW=$(date +%s)
  PID=$(adb shell pidof "$PACKAGE" | tr -d '\r')
  [[ "$PID" =~ ^[0-9]+$ ]] || { echo 'FAIL: app process absent or ambiguous' >&2; exit 4; }
  adb shell dumpsys activity activities > "$OUT/activities-$SAMPLE.txt"
  if ! rg -q '(mResumedActivity|topResumedActivity|ResumedActivity).*com\.khuongnd\.dexkids/\.KidsActivity' "$OUT/activities-$SAMPLE.txt"; then
    echo 'FAIL: KidsActivity is not resumed; start preview through parent UI first' >&2; exit 4
  fi
  if [[ -z "$BASE_PID" ]]; then
    BASE_PID=$PID
    adb logcat --pid="$PID" -v threadtime -T 1 > "$OUT/app-logcat.txt" 2>&1 & LOG_PID=$!
    capture start
  else
    [[ "$PID" == "$BASE_PID" ]] || { echo 'FAIL: app process restarted' >&2; exit 4; }
    kill -0 "$LOG_PID" 2>/dev/null || { echo 'FAIL: PID-scoped logcat collector stopped' >&2; exit 4; }
  fi
  printf 'timestamp_utc=%s elapsed_seconds=%s pid=%s\n' "$(date -u +%FT%TZ)" "$((NOW-START))" "$PID" >> "$OUT/samples.txt"
  adb shell dumpsys meminfo "$PACKAGE" > "$OUT/meminfo-$SAMPLE.txt"
  adb shell dumpsys gfxinfo "$PACKAGE" framestats > "$OUT/gfxinfo-$SAMPLE.txt"
  ((NOW >= END)) && break
  REMAIN=$((END-$(date +%s)))
  if ((REMAIN > 0)); then sleep "$((REMAIN < INTERVAL ? REMAIN : INTERVAL))"; fi
  SAMPLE=$((SAMPLE+1))
done
capture end
cleanup
LOG_PID=
if rg -q 'FATAL EXCEPTION|Fatal signal|ANR in com\.khuongnd\.dexkids' "$OUT/app-logcat.txt"; then
  echo 'FAIL: fatal/ANR signature in app log' >&2; exit 4
fi
printf 'completed_utc=%s\nactual_wall_seconds=%s\nstatus=CAPTURE_COMPLETED_REQUIRES_ANALYSIS\n' "$(date -u +%FT%TZ)" "$(($(date +%s)-START))" >> "$OUT/run.txt"
echo 'Real wall-clock capture completed. Analyze images, PSS and frames before recording PASS; emulator never closes physical Fold3 gates.'
