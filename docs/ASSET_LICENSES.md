# Asset provenance and redistribution

## 2026-10-11 story-pose illustration variants (T-003/T-023)

`art/assets-source/characters/{sau,ong}-story-v2.png` are high-resolution
illustrated variants generated with the built-in imagegen tool from the
existing illustrated Sâu/Ong sprite references, at the parent's request.
Four story poses per child: wave, look up, count, playful crouch. No original
family photograph was supplied to this generation. Exact prompt specifications
and consuming pipeline: `docs/T023_STORY_COMPANIONS.md`. These PNG sources are
copied byte-for-byte to generated APK assets. Resemblance/art acceptance and
production redistribution/license signoff remain OPEN; generation is not a
license audit. Nothing is published remotely by this local task.

| Source | Format | Author / license | Production status |
| --- | --- | --- | --- |
| art/assets-source/characters/capybara_*.svg | Original editable SVG, 4 poses | Originally created in project task T-003; project-owned placeholder art | Draft, not final |
| art/assets-source/vehicles/{bus,wheel}.svg | Original editable SVG | Originally created in project task T-003; project-owned placeholder art | Draft, not final |
| art/assets-source/environment/tree.svg | Original editable SVG | Originally created in project task T-003; project-owned placeholder art | Draft, not final |
| assets/generated/kids.{png,atlas} | Deterministically generated output | Derived only from above sources; do not edit generated files | Built by Gradle, bundled in APK |

Art references the intended Capybara/yellow bus style from the earlier design mockup, but the mockup image is **not** embedded or cropped.
There are no borrowed character illustrations or stock backgrounds in this pack.
The vector frames are initial gameplay assets and **not production-final art**.
For third-party asset additions, always record creator, exact URL, license and attribution requirements.
LibGDX license Apache-2.0 and other dependencies must be reviewed for release.


## Extended original vectors — 2026-10-08
All `art/assets-source/environment/*.svg` and `art/assets-source/effects/headlight_glow.svg` are original, project-authored SVG vectors, not stock imagery. New pieces: hills, cloud, city building, house, bridge, lamp, bush, flower, night glow. Character variants `capybara_sleep.svg` and `capybara_surprised.svg` are derivatives of the original project-owned `capybara_idle.svg`.
Atlas now uses a 2048×2048 RGBA single page (~16 MiB texture RAM); mobile GPU memory and texture-size compatibility must be tested on physical Fold3 before production. **Art remains first-pass and has not received final approval.**

## Additional characters and vehicles — 2026-10-09
- Six revised Capybara expression SVGs now share anatomical facial landmarks (eyes, cheeks, nose and mouth).
- Five original companion vectors: rabbit, fox, panda, cat, penguin.
- Six original transport vectors: car, taxi, box truck, minibus, scooter and bicycle.
- Source: `art/assets-source/characters/` and `art/assets-source/vehicles/`.
- Artwork files were authored specifically for this project. The generated concept reference board is **not** incorporated into the application binaries; independently authored vector sprites are used instead.
- These are fictional traffic/pedestrian decoration, not live tracking data.
- The new assets remain **draft**, awaiting artist review, device density testing, performance/memory profiling and license signoff.

## M6 water environment addition — 2026-10-09
- `art/assets-source/environment/river_water.svg`: project-original, hand-authored editable SVG with water gradient and ripples.
- Compiled into `river_water` in `assets/generated/kids.atlas` for RIVER/BRIDGE illustrative biome scenes. No third-party media copied. Water rendering is fictional scene art, **not** an OSM feature geometry or verified road crossing.
- Confirm atlas transparency/size via `tools/test_sprite_atlas.py`; Fold3 mobile GPU memory/performance still not profiled.
