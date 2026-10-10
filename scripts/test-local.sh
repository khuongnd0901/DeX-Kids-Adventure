#!/usr/bin/env bash
# Local unit tests only: no CI, no emulator, no connected phone.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
for spec in \
  tools/test_parent_simplified.py \
  tools/test_dual_child_entertainment.py \
  tools/test_adventure_dashboard.py \
  tools/test_sau_character_art.py \
  tools/test_ong_character_art.py \
  tools/test_adventure_hud_ong.py \
  tools/test_t024_t025_audio_engagement.py \
  tools/test_render_fast_path.py \
  tools/test_poi_backdrops.py \
  tools/test_child_voice_privacy.py \
  tools/test_kids_ai.py \
  tools/test_single_display_contract.py \
  tools/test_route_knowledge.py \
  tools/test_live_gps_pois.py \
  tools/test_road_poi_pipeline.py \
  tools/test_hcm_sample.py \
  tools/validate_reviewed_pois.py; do
  python3 "$spec"
done
./gradlew --no-daemon :core:test :android:testDebugUnitTest
echo "PASS: local unit/contract tests; no CI/emulator executed"
