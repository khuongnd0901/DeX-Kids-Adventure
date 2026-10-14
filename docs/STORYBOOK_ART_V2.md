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

Road traffic display height in `CartoonSprites.drawEnvironment` is **176px**
for cars/trucks/minibus and **158px** for scooter/bicycle, previously 140px
for all six. Width derives from each `TextureRegion` aspect ratio, so
no stretching occurs. Ground anchor y=110, x=chunk+265; art remains inside
the 640px scenery chunk. T-048 primary bus geometry is unchanged.

Run `python3 tools/render_t049_gallery.py art/review/t049-before-after.png`
for side-by-side comparisons, then inspect on Windows 1920x1080 emulator.
Added source/raster bounds tests and core layout tests; neither tests nor
emulator verification have been executed in this GitHub-only session.
T-049 branch should remain draft until visually accepted.

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
Actual screenshot review, runtime builds, DeX hardware FPS/thermal, parental
art approval, and redistribution clearance are **not yet recorded**.

## What's still needed to fulfill "replace ALL drawings"
The 23 new SVGs are new or detail-enhanced vector illustrations, but the rest are build-time
restyling of existing shapes. Full bespoke redraw requires the remaining
environment/props/backdrops/FX/UI and special Capybara gestures
be illustrated individually against T-048 PNGs and accepted with contact
sheets and per-region before/after emulator screenshots. Do not equate the
visual treatment with a finished T-048-quality illustration overhaul.
