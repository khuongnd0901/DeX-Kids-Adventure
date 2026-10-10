#!/usr/bin/env bash
# Installed debug + AndroidTest APKs only. No GPS injection, permission grants or cloud calls.
set -euo pipefail
if [[ $# -ne 3 ]]; then
  echo "Usage: $0 <adb-serial> <DeX-display-id> <local-output-directory>" >&2
  exit 2
fi
qa_serial="$1"
qa_display="$2"
qa_output="$3"
[[ "$qa_display" =~ ^[1-9][0-9]*$ ]] || exit 2
adb -s "$qa_serial" get-state | grep -qx device
adb -s "$qa_serial" shell getprop ro.product.model | grep -q 'SM-F926'
mkdir -p "$qa_output"
qa_failed=0
for qa_mode in gpx_parser recreation single_display single_display_mouse parent_controls gpx_sample p0_hcm p0_route p0_child_mic_privacy p0_ai_cache permission; do
  qa_log="$qa_output/$qa_mode.log"
  if adb -s "$qa_serial" shell am instrument -w -r \
      -e display_id "$qa_display" -e mode "$qa_mode" \
      com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner > "$qa_log" 2>&1 \
      && grep -q 'qa_status=PASS' "$qa_log"; then
    echo "$qa_mode PASS"
  else
    echo "$qa_mode FAIL (see $qa_log)"
    qa_failed=1
  fi
done
exit "$qa_failed"
