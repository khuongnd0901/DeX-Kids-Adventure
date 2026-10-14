# T-049 — Storybook V2 artwork: delivery and limitations

## Goal and boundaries
Align the non-child illustration system with the T-048 Capybara and yellow bus
direction: friendly 2D picture-book drawing, rounded edges, restrained warm
palette, non-scary silhouettes, bright facial highlights, readable at DeX 1080p.

**Scope protected:** Original Sâu/Ong SVGs, portrait/passenger PNG sheets,
build/copy logic, child dialogue and animations are untouched. Five original
T-048 Capybara/bus PNGs, their SHA allowlist, existing T-048 atlas and composite
render bounds are untouched. Existing legacy Capybara/bus/wheel sprites remain
unchanged so their T-048 rollback remains viable.

## Current source delivery
- **New illustrations (23 SVG):** five companion animals, twelve varied M10 vehicles,
  and six currently displayed road-traffic vehicles in
  `art/assets-source/storybook-v2/`. The new SVGs preserve the original
  frame sizes and names and are substituted only when mode is enabled.
- **Shared visual treatment (not new illustration):** original non-child
  `kids`, `extras`, `M10`, and POI backdrops are color-graded with
  subtle storybook warm/cool shading, inner strokes for sprites, and preserved
  RGBA dimensions. This gives existing assets a cohesive palette but does
  NOT replace their shapes with bespoke AI-made illustrations.
- **Protected:** 16 Sâu/Ong M10 pose files plus Sâu/Ong sheets/images are
  source- and runtime-untouched. Current T-048 PNG driver/guide remain byte-exact.

## Technical contract
Set `assets/art-style.txt` to `storybook-v2` and run existing
`generateKidsArt` / `python3 tools/build_sprites.py`.
Set it to `classic`, rebuild and reinstall to restore the original SVG pack.
`assets/art-mode.txt` remains the independent T-048 PNG/legacy switch.
SVGs and PNGs are rasterized **at build time only**. No new texture page,
network dependency, image-generation API, or render-loop CPU work is introduced.
Region names, native source dimensions, scene selection, FPS logic and story flow
are unchanged; road traffic display geometry is enlarged intentionally. Source paths in the existing inventory remain authoritative.

## Secondary vehicle detail and on-screen size (2026-10-14)

The six active road vehicles (car, taxi, truck, minibus, scooter, bicycle)
are redrawn in 360x185 SVG: distinct glass/window shapes, lights, mirrors,
door handles, bumpers, wheel hubs/spokes and appropriate vehicle hardware.

The twelve M10 catalog vehicles retain 280x160 source frames and receive
enlarged silhouettes plus ambulance emergency lighting, fire-engine equipment
compartments, camper fittings, train structure, electric-bus rooftop
units/doors, recycling truck body and sailboat rigging. Note that **M10 VEH
is currently an art catalog; the 12 sprites are not yet rendered as moving
traffic at runtime**. The six active road traffic sprites are rendered today.

Road traffic now uses `TrafficDepthModel`, `TrafficLayout` and
`TrafficRenderOrder`: tire ground Y250 for far,95 for near, nominal bus188
(PNG) or198.21 (legacy). Canvas heights by kind:160/160/225/215/115/105,
far scale0.74. Width follows the360:185 atlas region aspect; tire padding is
removed from the ground anchor. Traffic is drawn after the blended panorama,
with far traffic before bus/passengers and near traffic after them. Story/HUD
remain above all traffic. Bus, passenger and Capybara art/animation are unchanged.

Two reused lane slots move right, matching the source headlights/front wheel.
The existing20261008 seed selects deterministic types; each has a distinct speed.
Every22s a new vehicle enters wholly offscreen, staggered2s across lanes.
All types exit before replacement; pause uses the existing effective render
delta. Chunk transitions do not spawn/despawn moving traffic. Biome eligibility
is latched on entry, so theme transitions cannot erase a passing vehicle.
This is fictional scenery, never a claim about GPS traffic or real road lanes.

The6 active SVG shadows now meet their tire planes; scooter/bicycle shadows
have narrower footprints, and truck cargo has an appropriately taller silhouette.
M10 VEH remains catalog only. No extra texture pages or runtime asset loading.

Evidence and actual build/emulator results:
[traffic-depth report](../.agent/evidence/T-049-traffic-depth/summary.md).
PR #56 was merged into main at e1754210 on 2026-10-14 following separate user authorization. T-049 art acceptance remains IN_PROGRESS; all 60 new scenery SVGs need fresh build, raster-bound and emulator visual acceptance.

## Additional detailed non-child scenery art (2026-10-14)

This batch adds **60 individually redrawn source SVGs** under the existing
`art/assets-source/storybook-v2/` override path:

| Family | Count | Runtime integration |
|---|---:|---|
| Environment / sky / urban road | 10 | Existing `kids.atlas` environments |
| Foreground scene extras | 18 | Existing `extras.atlas` and current scene chooser |
| M10 urban environment | 8 | M10 resident `props.atlas`, selected by existing biome chooser |
| M10 village environment | 8 | M10 resident `props.atlas`, selected by existing biome chooser |
| M10 forest environment | 8 | M10 resident `props.atlas`, selected by existing biome chooser |
| M10 farm environment | 8 | M10 resident `props.atlas`, selected by existing biome chooser |

Existing source region keys and required SVG native frame dimensions are
unchanged; current four asset-builder scripts automatically select the
Storyboard V2 override when enabled. The original editable assets remain
untouched and `assets/art-style.txt=classic` continues as rollback.

The illustrations include layered leaf shapes, bark/trunk highlights, window
frames, shutters, storefront goods, bamboo segments, bamboo/bush outlines,
water ripples, bridge structure, tiled roofs, crop grains, waterwheels,
ground shadows, and animal facial details. These are new vector shapes,
not merely color grading. Real POI landmarks/backdrop SVGs remain unchanged
apart from existing non-destructive build-time color treatment; they must
not be redrawn inaccurately or represented as satellite/GPS reality.

### QA and gallery

```bash
python3 tools/test_t049_scenery.py
python3 tools/render_t049_scenery_gallery.py art/review/t049-scenery
python3 tools/build_sprites.py
python3 tools/test_sprite_atlas.py
python3 tools/test_m10_art.py
bash scripts/test-local.sh
```

The first script validates all 60 authored SVGs, expected native viewport,
safe inline SVG, alpha and 1-pixel edge clipping of the 32 M10 props.
The gallery creates six before/after sheets (environment, foreground extras,
urban, village, forest and farm) for art review.

**QA state:** SVG source committed; no local Python/Android build or emulator
executed in this GitHub-only continuation. Do not mark PASS or DONE based on
source checks alone. Confirm GPU atlas count, scene occlusion with T-049
traffic-depth layering, FPS and actual Samsung DeX visuals before merge.

## Local checks (must be executed on developer machine before merge)
```bash
python3 tools/test_storybook_art.py
python3 tools/build_sprites.py
python3 tools/test_sprite_atlas.py
python3 tools/test_m10_art.py
bash scripts/test-local.sh
./gradlew :core:test :android:assembleDebug :desktop:classes
```
Use the same Windows API 35 / 1920x1080 emulator from T-048 to check
transparent edges, clipping, font/text, avatar proportions, atlas lifecycle,
scene overlap and that Sâu/Ong appearances remain unchanged.
Compare source SHA-256 for all protected assets on `main` vs feature branch.
See the traffic-depth report for executed local builds and API35 screenshot review.
Physical DeX FPS/thermal, parent art approval and redistribution clearance remain open.

## What's still needed to fulfill "replace ALL drawings"
The 83 new or detail-enhanced SVGs now cover companions, vehicles, runtime scenery and M10 urban/village/forest/farm props. Other families still receive build-time restyling rather than bespoke redraws. Full bespoke redraw requires the remaining
remaining named POI backdrops, non-runtime M10 coast/mountain, FX/UI and special Capybara gestures
be illustrated individually against T-048 PNGs and accepted with contact
sheets and per-region before/after emulator screenshots. Do not equate the
visual treatment with a finished T-048-quality illustration overhaul.
