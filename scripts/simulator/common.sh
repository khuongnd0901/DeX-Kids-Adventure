#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
ADB_BIN=${ADB_BIN:-adb}
PACKAGE=com.khuongnd.dexkids
SERIAL=
DURATION=120
INTERVAL=15
APK="$ROOT/android/build/outputs/apk/debug/android-debug.apk"
usage() {
  echo "Usage: $0 --serial SERIAL [--duration SECONDS] [--interval SECONDS] [--apk APK]"
  echo 'ADB_BIN may point to adb.exe. AAPT_BIN and GRADLE_BIN may override SDK/build tools.'
  echo 'Artifacts: build/simulator-artifacts/<command>-<UTC timestamp>. No permission grants.'
}
parse() {
  while (($#)); do
    case "$1" in
      --help|-h) usage; exit 0 ;;
      --serial|--duration|--interval|--apk)
        (($# >= 2)) || { echo "Missing value: $1" >&2; exit 2; }
        case "$1" in --serial) SERIAL=$2;; --duration) DURATION=$2;; --interval) INTERVAL=$2;; --apk) APK=$2;; esac
        shift 2 ;;
      *) echo "Unknown argument: $1" >&2; usage >&2; exit 2 ;;
    esac
  done
  [[ -n "$SERIAL" ]] || { usage >&2; exit 2; }
  [[ "$DURATION" =~ ^[1-9][0-9]*$ && "$INTERVAL" =~ ^[1-9][0-9]*$ ]] || { echo 'Positive integer times required' >&2; exit 2; }
  ((INTERVAL <= 30)) || { echo 'Interval must be <=30 seconds' >&2; exit 2; }
  command -v "$ADB_BIN" >/dev/null || { echo "ADB unavailable: $ADB_BIN" >&2; exit 3; }
  [[ $(adb get-state | tr -d '\r') == device ]] || { echo 'Selected device unavailable' >&2; exit 3; }
  OUT="$ROOT/build/simulator-artifacts/$(basename "$0" .sh)-$(date -u +%Y%m%dT%H%M%SZ)-$$"
  mkdir -p "$OUT"
  printf 'serial=%s\ncommand=%s\nstarted_utc=%s\n' "$SERIAL" "$0" "$(date -u +%FT%TZ)" > "$OUT/run.txt"
  echo "Artifacts: $OUT"
}
adb() { "$ADB_BIN" -s "$SERIAL" "$@"; }
host_path() {
  if [[ "$ADB_BIN" == *.exe ]]; then wslpath -w "$1"; else printf '%s\n' "$1"; fi
}
capture() {
  local name=$1 remote="/sdcard/dexkids-test-$$.png"
  adb shell screencap -p "$remote"
  adb pull "$remote" "$(host_path "$OUT/$name.png")" > "$OUT/$name-pull.txt"
  adb shell rm "$remote"
  [[ -s "$OUT/$name.png" ]] || { echo 'Screenshot absent' >&2; exit 4; }
}
