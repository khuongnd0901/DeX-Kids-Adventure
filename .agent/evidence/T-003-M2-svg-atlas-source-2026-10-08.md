# T-003 M2 follow-up: actual original art pipeline

Implementation checkpoint: editable SVGs for Capybara (idle/blink/talk/wave), school bus, independent rotating wheel, and tree. Python CairoSVG/Pillow packs a deterministic single-page LibGDX TextureAtlas; Gradle generates raster assets before Android merge and desktop run. `CartoonSprites` uses SpriteBatch and releases atlas resources in dispose. Existing WorldPainter remains behind sprites for procedurally generated biomes.

The PNGs are generated binary files and not committed; editable SVGs are committed under `art/assets-source/`. Visual style intentionally remains a first-pass concept.
Actual CI compile+Android APK+sprite packaging: SUCCESS, run #37801172670 and post-HUD run #37802727541. Actual desktop OpenGL screenshots (1280×720, 1920×1080): VERIFIED via run #37802727541, artifact 11560688934. Physical Z Fold3/DeX: NOT VERIFIED.
T-003 status: IN_PROGRESS; production asset approval, lipsync and visual seam acceptance OPEN.


## Actual screenshot inspection and remaining issues
- Source commit ce341a0: CI run #37802727541 **SUCCESS** for unit tests, Android debug APK, Xvfb software OpenGL render, 2 screenshots and atlas artifact uploads.
- 1920x1080 image shows yellow bus, Capybara in front window, wheels, skyline, sky, tree parallax and dark developer HUD. 1280x720 image also renders.
- Visual limitations: city silhouettes remain basic, geography is stylized rather than real, abrupt transitions between adjacent biomes can be seen; tree and bus are first-pass vector art rather than approved final character sheets.
- No physical Fold3 / actual DeX frame-rate, power, memory, thermal or long-run benchmark was performed.
- CI artifacts: [screenshot/atlas/APK run 37802727541](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37802727541).
