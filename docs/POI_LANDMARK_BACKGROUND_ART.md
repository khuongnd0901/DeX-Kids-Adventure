# POI Landmark Backgrounds — artwork and scaling

**2026-10-11 — First illustrated pass, not photos or survey-grade reconstruction.**

22 editable project-authored SVG landscape scenes at `art/assets-source/backdrops/`,
mapped to the 44 GPS/sample POIs by exact OSM IDs in `assets/poi/backdrop-map.tsv`.
Grouped locations share a landscape *only where visually related* (e.g., Datanla/Prenn:
waterfalls; Tháp Bà/Pô Sah Inư: Cham towers); unique recognisable silhouettes for the
Vũng Tàu lighthouse, Christ the King statue, Kê Gà lighthouse, Hòn Chồng rocks,
Dinh Độc Lập, Saigon central post office and Đà Lạt Lâm Viên Square.
These are **illustrative interpretations** and must not be passed off as photographs,
surveyed street geometry, exact routes or verified POI passage.

## Scene layering / texture memory

- `tools/build_sprites.py` invokes `tools/build_backdrops.py`, rasterizing the
  1200×650 native-ratio source SVGs to 960×520 PNGs in
  `assets/generated/backdrops/`. Existing character atlas is unchanged.
- `PoiBackdropRenderer` loads **at most one 960×520** texture into GPU memory
  (~1.9 MiB RGBA) on a POI change. Never loads 22 full-screen textures at once.
  Beware that sync texture creation on the render thread may create a short
  hitch at first encounter; do not claim measured smoothness without DeX QA.
- The backdrop keeps its **native aspect ratio**, scales 1.6 to 1920×1040 and
  anchors at the road boundary y=268, cropping only top sky outside viewport.
  It fades with `PoiSceneDirector`; unlabeled/unknown POIs use old seeded biome.
- Existing 2.5s fade, ≤200m proximity, 800m/26s expiry, reset and privacy
  constraints are unchanged. Live POIs are still OSM-estimated and
  **not matched to an actual road segment**.

## Bus / character layout

- `VehicleLayout` declares one 1920×1080 coordinate system for bus/wheels/Sâu/
  Ong/Capybara. The bus preserves source SVG aspect 660:290; new rendered
  footprint is 950×417, with wheel centers derived from source coordinates.
- Sâu (4) and Ong (3) are passengers *within the illustrated bus cabin*; solo
  mode places its active child in the first passenger window.
- Animation bob is synchronized to the bus rather than bouncing out of the
  windows; subtle per-child breathing is capped at 1.4 px. Wheel rotation uses
  matching 66px radius. Artwork depicts a stylized bus, not a road-safety guide.

## Verify locally (CI intentionally disabled)

```bash
python3 tools/build_sprites.py
./gradlew --no-daemon :core:test :android:assembleDebug
python3 tools/test_render_fast_path.py
./gradlew --no-daemon :desktop:run --args="--gpx test-data/gps/synthetic-hcm-poi-loop.gpx --sample-preview true --start-seconds 170 --smoke-frames 150 --width 1920 --height 1080"
```

Review actual DeX screenshot: 16:9 scale, background cropped sky only,
readable native HUD, no legs floating outside bus, no landscape label implying
verified street position. Record frame-time spikes at first scene change; confirm
single-texture lifecycle after repeated POI transitions. Do not mark physical QA
complete from static code checks.
