#!/usr/bin/env bash
# Local unit tests only: no CI, no emulator, no connected phone.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
for spec in \
  tools/test_passenger_bounds.py \
  tools/test_runtime_children.py \
  tools/test_story_world.py \
  tools/test_whole_characters.py \
  tools/test_t048_art.py \
  tools/test_storybook_art.py \
  tools/test_t049_scenery.py \
  tools/test_t050_animal_art.py \
  tools/test_t051_coast_mountain.py \
  tools/test_t052_outfits.py \
  tools/test_t053_outfits.py \
  tools/test_ong_complete_outfits.py \
  tools/test_t052_child_ages.py \
  tools/test_traffic_depth.py \
  tools/test_parent_simplified.py \
  tools/test_unlimited_session.py \
  tools/test_dual_child_entertainment.py \
  tools/test_adventure_dashboard.py \
  tools/test_sau_character_art.py \
  tools/test_ong_character_art.py \
  tools/test_adventure_hud_ong.py \
  tools/test_t024_t025_audio_engagement.py \
  tools/test_render_fast_path.py \
  tools/test_m10_art.py \
  tools/test_m10_runtime_contract.py \
  tools/test_poi_backdrops.py \
  tools/test_trip_learning.py \
  tools/test_child_voice_privacy.py \
  tools/test_kids_ai.py \
  tools/test_t030_ai_packs.py \
  tools/test_creative_lessons.py \
  tools/test_visual_m1_m2.py \
  tools/test_t033_dynamic_world.py \
  tools/test_landcover_overpass.py \
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
