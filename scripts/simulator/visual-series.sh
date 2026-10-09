#!/usr/bin/env bash
source "$(dirname "$0")/common.sh"
parse "$@"
START=$(date +%s)
END=$((START+DURATION))
N=0
while :; do
  printf 'index=%s timestamp_utc=%s elapsed_seconds=%s\n' "$N" "$(date -u +%FT%TZ)" "$(($(date +%s)-START))" >> "$OUT/frames.txt"
  capture "frame-$(printf '%04d' "$N")"
  (( $(date +%s) >= END )) && break
  REMAIN=$((END-$(date +%s)))
  ((REMAIN <= 0)) && break
  sleep "$((REMAIN < INTERVAL ? REMAIN : INTERVAL))"
  N=$((N+1))
done
echo 'Actual screenshot sequence captured; inspect originals, do not infer FPS from screenshot cadence.'
