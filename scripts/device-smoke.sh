#!/usr/bin/env bash
set -euo pipefail
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <explicit-adb-serial>" >&2
  exit 2
fi
SERIAL="$1"
adb -s "$SERIAL" get-state | grep -qx device
echo "Device model:"
adb -s "$SERIAL" shell getprop ro.product.model
echo "Android API level:"
adb -s "$SERIAL" shell getprop ro.build.version.sdk
echo "Detected displays:"
adb -s "$SERIAL" shell dumpsys display | grep -E 'mDisplayId=|DisplayInfo\{' | head -30 || true
echo "Package:"
adb -s "$SERIAL" shell pm path com.khuongnd.dexkids || true
echo "No APK installed, settings changed, permissions granted or screen launched by this script."
