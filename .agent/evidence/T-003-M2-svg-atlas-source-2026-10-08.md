# T-003 M2 follow-up: actual original art pipeline

Implementation checkpoint: editable SVGs for Capybara (idle/blink/talk/wave), school bus, independent rotating wheel, and tree. Python CairoSVG/Pillow packs a deterministic single-page LibGDX TextureAtlas; Gradle generates raster assets before Android merge and desktop run. `CartoonSprites` uses SpriteBatch and releases atlas resources in dispose. Existing WorldPainter remains behind sprites for procedurally generated biomes.

The PNGs are generated binary files and not committed; editable SVGs are committed under `art/assets-source/`. Visual style intentionally remains a first-pass concept.
Actual CI compile: PENDING. Physical Z Fold3/DeX and scene screenshot: NOT VERIFIED.
T-003 status: IN_PROGRESS; production asset approval, lipsync and visual seam acceptance OPEN.
