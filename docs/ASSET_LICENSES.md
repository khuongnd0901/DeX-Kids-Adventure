# Asset provenance and redistribution

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
