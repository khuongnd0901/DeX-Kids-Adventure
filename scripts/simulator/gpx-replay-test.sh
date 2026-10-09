#!/usr/bin/env bash
set -euo pipefail
usage() { echo "Usage: $0 --core"; echo 'Runs synthetic/offline core GPX + live-feed quality tests; no ADB or physical GPS claims.'; }
[[ $# -eq 1 ]] || { usage >&2; exit 2; }
case "$1" in --help|-h) usage; exit 0;; --core) ;; *) usage >&2; exit 2;; esac
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
cd "$ROOT"
OUT="$ROOT/build/simulator-artifacts/gpx-replay-$(date -u +%Y%m%dT%H%M%SZ)-$$"
mkdir -p "$OUT"
BUILD=${GRADLE_BIN:-./gradlew}
printf 'source_sha=%s\nstarted_utc=%s\nscope=core synthetic GPS only\n' "$(git rev-parse HEAD)" "$(date -u +%FT%TZ)" > "$OUT/run.txt"
"$BUILD" :core:test --tests '*Gpx*' --tests '*Live*' --tests '*SpeedSmoother*' > "$OUT/tests.log" 2>&1
cp -r core/build/test-results/test "$OUT/junit"
printf 'completed_utc=%s\nstatus=CORE_TEST_EXECUTION_COMPLETED\n' "$(date -u +%FT%TZ)" >> "$OUT/run.txt"
echo "Evidence: $OUT (core only; no emulator injection or physical GPS verification)"
