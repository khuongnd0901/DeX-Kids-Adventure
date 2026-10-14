# T-049 scenery V2 — source inventory and verification receipt

Date: 2026-10-14
Parent integration: PR #56 merged to main at `e1754210c2133627a3643a065af9dd73db78f434`.
Follow-up branch: `feat/T-049-scenery-qa-followup`.

## Source delivered

- 10 environment sprites (bridge, building, bush, cloud, flower, hills,
  house, lamp, river water, tree);
- 18 180x160 original foreground props (plants, animals, signs, market,
  water assets, crops and kites);
- 8 each for M10 urban, village, forest and farm, all native 160x160.
- Total new scenery sources: **60 SVG**. With T-049's 23 companion/vehicle
  sources, **83 new or detail-enriched Storybook V2 SVGs** are present in main.
- All source overrides use `art/assets-source/storybook-v2/` and the existing
  `select_source()` build-time selector. The corresponding originals and
  T-048 original five PNGs are not edited by this scenery work.
- Sâu/Ong source art and character logic have not been touched by this work.

## Static inspection completed

- GitHub main/branch trees: **60/60 new scenery sources present**.
- Reviewed full source text for all 60 via GitHub content retrieval; valid
  expected native SVG width/height/viewBox, balanced SVG tags, and no
  disallowed external/embedded reference patterns: **PASS (static only)**.
- This is *not* proof of correct CairoSVG rasterization, atlas memory,
  screenshot appearance, or DeX runtime geometry.
- Physical Samsung DeX, sustained thermal/FPS, parent/child visual
  sign-off and release rights remain open.

## Local QA to execute (not claimed PASS)

```bash
python3 -m pip install -r tools/requirements-art.txt
python3 tools/test_t049_scenery.py
python3 tools/render_t049_scenery_gallery.py art/review/t049-scenery
python3 tools/build_sprites.py
python3 tools/test_sprite_atlas.py
python3 tools/test_m10_art.py
bash scripts/test-local.sh
./gradlew :core:test :android:assembleDebug :desktop:classes
```

Inspect all six contact sheets and original 1920x1080 Windows API35 emulator
compositions with the T-048 bus and T-049 tire-ground traffic ordering:
ground contact, alpha halos, no cropping, original POI landmark silhouette,
HDR/backdrop layering and Sâu/Ong portrait protection.

## Scope not yet redrawn

22 named POI backdrops, non-runtime M10 coast/mountain packs, effects,
UI stickers and special Capybara gesture vectors are not all individually
re-illustrated. They continue to receive earlier color-only treatment where
applicable. Overall **T-049 IN_PROGRESS / PARTIAL**; no CI enabled.
