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

2026-10-11 passenger-v3 Sâu/Ong: built-in imagegen identity-preserving edits
of project story-v2 cartoons, transparent2x2 half-body sheets. Local parent
requested bus-avatar derivatives; likeness/license/release review remains
open. Prompt/provenance in T023_STORY_COMPANIONS.md.

## T-048 AI Capybara + side bus concept pack — 2026-10-14

Five transparent RGBA concept illustrations were generated within ChatGPT at
user request: `capybara_idle.png`, `capybara_laugh.png`, `capybara_ask.png`,
`capybara_wave.png`, `bus_side_with_driver.png`. Origin: user-provided ZIP
`DeX-Kids-Adventure-art-v1.zip` in ChatGPT Library. All five original binary
SHA256 values and alpha bounds were independently checked against manifests on
2026-10-14 (receipt: `.agent/evidence/T-048-2026-10-14/M0_VERIFY.md`).
Original binary sources are now committed/pushed and verified from remote Git blobs (faba918); derived PNG atlas is integrated (df955fa). **NOT PRODUCTION APPROVED**.
No third-party image or photograph knowingly imported. Generation does not
establish exclusive rights or substitute for a release/redistribution policy
review. Parent visual acceptance, licensing/redistribution signoff, device
profiling and runtime integration all OPEN. Preserve legacy SVG sprite sources
and actual license notices; never report new art as production-ready yet.

### Local continuation — 2026-10-14

The original ZIP and PNG bytes are not available in the current local checkout.
Importer execution with the existing QA Pillow environment fails with
FileNotFoundError; no new identity/alpha verification or runtime replacement
occurred. Earlier Library verification remains historical. Current blocker:
accessible original ZIP path; local Git supports binary commit/push once present.
See `.agent/evidence/T-048-2026-10-14/LOCAL_M0_BLOCKER.md`.
Licensing/redistribution and parent visual acceptance remain OPEN.

### T-048 integration acceptance update — 2026-10-14

Original PNG bytes unchanged; reproducible resize/alpha crop/baseline padding/atlas
packing only. New bus driver/wheels remain baked; four source poses show in a
standing guide. Local art/core/build and Windows emulator functional checks PASS;
actual screenshots in `.agent/evidence/T-048-2026-10-14/SUMMARY.md`. Earlier local
missing-ZIP note is superseded by supplied D: ZIP and verified binary commit.
No new license inference; parent/child art approval and redistribution policy
remain OPEN. Physical Samsung DeX/FPS/GPU lifecycle remains NOT VERIFIED.


## T-071/T-072 generated artwork provenance (2026-10-18)

art/assets-source/story-characters-v3 contains nine generated transparent
whole-character pose sheets derived from the existing fictional road illustration
references. art/assets-source/app-icon-v1 contains the generated Capybara icon.
Both were created with built-in image_gen; generation.json files retain prompts
and references. Original protected project PNGs/SVGs remain unchanged. No stock
asset, real-child photo, GPS, recording or child response was supplied. Generation
provenance does not establish a new asset license or human aesthetic approval.
