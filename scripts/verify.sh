#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew --version
./gradlew :core:test :desktop:classes :android:assembleDebug
