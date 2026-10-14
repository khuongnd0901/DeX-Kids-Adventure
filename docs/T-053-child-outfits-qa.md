# T-053 — Child Costume Integration & Visual QA

Owner Main. Dependencies T-048/T-051/T-052. Base fetched `origin/main`
`8ee864de4ec675dd3ae8be2f7138b0ab81e738f3`, branch
`feat/T-053-windows-child-outfits-qa`, [PR #61](https://github.com/khuongnd0901/DeX-Kids-Adventure/pull/61).
Status: Windows source/runtime/visual acceptance verified; final evidence publication/merge pending.

## Defect and correction

Actual Windows Android captures confirmed that the old fixed 256×384 frame
put Sâu's headband across his eyes, placed clothing below the original torso,
and drew full-body overlays on alpha-trimmed bus portraits. PET crouches
could not share the standing outfit geometry.

The new build creates **96 garment overlays**: twelve styles × four gestures
× two coordinate systems. `child_outfit_geometry.py` samples blue fabric
from the original quadrants, retains its shading and folds, applies each SVG
palette and an original motif, adds a pose-specific skirt for Ong and anchors
her crown on the existing hat. Eyes/mouth are explicitly excluded. New capes
and forehead bands were removed to preserve gestures and identity. Original
backpacks, hats, binoculars, exposed hands, expressions and animation remain.
The original twelve SVGs remain palette/source validation inputs; their old
fixed-coordinate silhouettes are no longer runtime atlas regions.

Story overlays share the original full-cell transform. Passenger overlays
are built against the exact alpha≥128 crop used by `loadPassengerSheet`;
runtime uses the **base passenger's aspect ratio**, never the overlay tile's.
This aligns each waist to the unchanged window sill. VehicleLayout, bus,
Capybara, audio, GPS, navigation and source child PNGs are unchanged.

The atlas is one 2048×2048 RGBA8888 page (16 MiB raw GPU, previous8MiB).
It contains96preloaded128×192frames with6px gutters. Base character textures
retain their original resolution. Regions are selected without texture loads
or image generation during rendering; the atlas is disposed with the scene.
Missing optional atlas displays the original art.

## Real Windows emulator verification

Existing AVD `DeX_Stability_API35_20261005`, serial `emulator-5554`, Android15/
API35 x86_64, physical1920×1080, density160. Used Windows `D:\Android\Sdk`
adb/emulator from WSL; no new SDK, AVD or competing ADB server.
Built with existing WSL JDK17/SDK35/NDKr27c and CairoSVG/Pillow virtualenv.

The instrumentation-only `t053_outfits` fixture runs offline DEMO, preserves
and restores parent/AI preferences, forces seeded costume pairs and a paused
semantic clock, and captures all6indices ×4gestures ×SAU/ONG/BOTH=72actual
Android screenshots. It updates both the GL audience and native HUD. The
fixture uses Android UiAutomation screenshots of the running activity;
additional ADB screencap/pull is retained. Review crops derive from those
Android originals and are comparisons, not substitute emulator evidence.

Runtime checks cover240-second boundaries (production interval unchanged),
six unique choices perchild before repeat, stable atlas identity, independent
session choices, audience switching, pause/resume, actual Activity/GL
recreation and absence of optional atlas. The fallback APK was made only in
ignored build output by removing its two atlas assets and re-signing with the
existing debug key; the normal APK was restored afterward.

All five persisted-age cases were run on Android: fresh5/4, mistaken4/5
repaired once, correct5/4 preserved, custom6/7 preserved, and a newly chosen4/5
retained after restart once migration is marked. SAU/ONG/BOTH learning focus
uses5/4/min respectively. Static/local tests also cover default labels,
offline focus, AI age mapping and age-isolated cache/prompt contracts; no
cloud requests were needed for this QA.

## Evidence and limitations

[Evidence summary](../.agent/evidence/T-053-child-outfits/SUMMARY.md),
[test logs](../.agent/evidence/T-053-child-outfits/test-results/),
[real screenshots](../.agent/evidence/T-053-child-outfits/screenshots/),
[performance](../.agent/evidence/T-053-child-outfits/performance/).

The source masks and all48story variants/24paired passenger variants were
reviewed from actual captures. No costume face obstruction, substantial
body/limb misalignment, crown/roof collision or new HUD/Capybara overlap was
observed. The explorer accessories remain part of the original identity;
these are superhero/princess adventure outfits, not replacement portraits.
PET retains the existing nearby animal illustration, which can obscure a
portion of the garment in the existing production composition.

Initial gameplay samples: outfits205frames/10.020s =20.459FPS/P95upper64ms,
18drawcalls/10managed textures/PSS64379KiB; fallback211frames/10.039s =
21.017FPS/P95upper62ms,14calls/9textures/PSS65331KiB. Short sequential windows
are descriptive only, not a causal benchmark. Four additional garment quads
perframe for BOTH; observed+4calls and+1texture in those two windows. Memory
figures include process allocations, not an isolated GPU-atlas measurement.
Smooth30/60FPS and physical Samsung Fold3/DeX performance are NOT VERIFIED.

Failed attempts are retained: systemPython lackedPIL (existing venv reused),
a generated-script indentation error failed the build, and the first
face-alpha guard caught garment pixels before explicit exclusion was added.
Assertions were retained and all checks rerun. Original child hashes are
compared to8ee864d in `protected-children.json`; no CI enabled.

Final PET polygon hand mask additionally prevents knee-colored holes in the skirt; all6PET variants re-reviewed. Final gameplay217frames/9.999529s=21.701FPS/P95upper56ms,18calls/10textures/PSS64457KiB. Nearby fallback21.813FPS/P95upper58ms/14calls/9textures/PSS65513KiB. Seven short rotation-boundary windows are retained; they do not isolate causal costume-change latency.194core tests and required local/Android build checks PASS.
