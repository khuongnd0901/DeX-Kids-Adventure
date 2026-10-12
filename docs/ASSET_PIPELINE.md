## M10 source and tooling checkpoint — 2026-10-12 (feature branch only)

`tools/build_m10_atlas.py` implements a staged 1024px multi-page
packer from the inventory CSV. It packs actual checked-in SVGs only,
checks SVG scripts/external references and duplicate IDs, crops alpha,
writes `assets/generated/m10/m10.atlas` + `manifest.json`.
The Gradle art command already invokes `tools/build_sprites.py`,
which invokes the M10 builder. `tools/test_m10_art.py` and
`tools/render_m10_gallery.py` are local tools. 92 P0 SVG source
files are authored but unreviewed; Sâu/Ong P0 (16) and P1 (64) remain
absent. The **runtime-scoped props-only** derived atlas is now generated as
`props.atlas` and `props-map.tsv` via `tools/build_m10_prop_atlas.py`.
`M10PropAtlas` loads at most 3 pages once at game initialization and
`CartoonSprites` chooses new prop illustrations for some existing biome
pools with old-art fallback. The full M10 atlas pages are never loaded
into gameplay wholesale. FX/UI, CAP and vehicle runtime mapping, animation
profiles, actual device texture residency and Android tests remain **OPEN**;
this is not a fully implemented M10 release. Use
`docs/M10_CODEX_IMPLEMENTATION_REVIEW.md` to close this gate
before merging. Historical section below described design stage only.

## M10 execution-order correction (2026-10-12)

The new 172-row art inventory is now staged **108 P0 / 64 P1**.
**Do T-039.A manifest/packer + tests before asset bulk generation**.
Unlike the old plan, it must accept incremental art batches; no dependence
on completion of T-036..T-038. A narrow T-041.A must reuse current
`CartoonSprites`/`AnimationTimeline` with existing sprites, and T-040.A
must reuse the existing procedural world/SceneryCast, before adding new art.
Only T-039.B handles future FX/UI source files and *animation profile*
metadata. T-041 is the sole owner of runtime renderer integration.
Don't add any new M10 SVG into the existing 14–24-only `extras.atlas`
packer. See `docs/M10_REUSE_DEPENDENCY_REVIEW.md`.
No new packer has been implemented by the documentation update.

# T-003 M2: source-controlled SVG → generated LibGDX assets

**Editable originals:** `art/assets-source/characters/`, `vehicles/`, `environment/`, `effects/`. Original 18 SVG sprite regions (not derived from third-party stock art). The initial AI image mockup is an art direction reference; it is not shipped as-is.

**Generated outputs:** `assets/generated/sprites/*.png`, `kids.png`, `kids.atlas`. These are gitignored; Android `../assets` and Desktop repo working directory load the same atlas.

## Build and verify
```bash
python3 -m pip install cairosvg==2.8.2 Pillow==12.3.0
./gradlew :core:test :android:assembleDebug
python3 tools/test_sprite_atlas.py
./gradlew :desktop:run
./gradlew :desktop:run --args="--gpx test-data/gps/synthetic-urban-short.gpx"
```
Gradle creates raster art before Android assets are packaged and before the Desktop run. The test validates named atlas regions and alpha transparency.

## Drawing order and animations
1. Sky with day/dusk/night palette.
2. 640px looping hill backgrounds and drifting original SVG clouds (far parallax).
3. 640px scenery chunks grouped as 4-chunk biome districts, road and a developer-only HUD backdrop.
4. Buildings, houses, bridge, bushes, flowers, trees, street lamps and conditional dusk/night glow.
5. Yellow bus plus Capybara (idle/blink/wave/sleep/surprised/talk), physically rotating wheels and bounded suspension bob/pitch.

`CharacterAnimationController` has an explicit `setNarrationActive` gate. TALKING is never triggered by arbitrary GPS or fake narration. The actor greets nonverbally when the game begins.

## Atlas memory budget
Packed PNG has a **2048×2048 RGBA** page; worst-case uncompressed texture RAM ≈16MiB, before mipmaps/additional textures. This is a prototype budget, not hardware validated. Future art optimization should remove blank atlas space, consider multi-page atlas/compression and test actual Samsung GPU/PSS measurements.

## OpenGL visual evidence
GitHub Actions uses Xvfb+Mesa software OpenGL and saves full-resolution screenshots plus 10 real frames/scene; Pillow assembles a 960×540 illustrative GIF. The GIF cadence does not measure true real-time FPS.
[Verified source run 37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) includes all three outputs (APK, sprite atlas and screenshots/GIF).

## Not yet accepted
Art direction remains first-pass, real-world geo → accurate biome scene mapping, improved cinematic lighting, voice-driven lip sync, Samsung DeX 60-minute FPS/power/thermal and full accessibility/parent controls.


## T-027 standalone extras atlas

A separate 1024×1024 `extras.atlas` contains 18 additional original
illustrated foreground prop sprites, rasterized from `art/assets-source/props/*.svg`
by `tools/build_extras.py` inside the existing Gradle artwork task.
One permanent small GPU atlas is used per screen; existing 2048px atlas remains unchanged.

## M10 expansion contract (2026-10-12 — design only)

The M10 source registry is `art/M10_ASSET_INVENTORY.csv` and execution plan
is `.agent/plans/active/T-036-T-042-art-variety-and-animation-pack.md`.
Plan exactly **172 new art source regions**; **none has been drawn/packed by
this documentation commit**. Existing `kids.atlas` (2048², fixed region set)
and `extras.atlas` (1024², 14–24 SVG guard) must remain compatible. **Do not
append 96 new biome SVGs into `art/assets-source/props/` without replacing
the intentionally bounded packer.**

The proposed `art/assets-source/m10/` is a new original SVG tree.
Implement a deterministic multi-page packer `tools/build_m10_atlas.py` in
T-039, with stable allowlisted IDs, 1024² pages, 4px padding, alpha trimming,
page/region audits, reproducible output, and explicit texture lifecycle.
Target incremental resident GPU memory <=12 MiB, subject to benchmark; lazy
load only relevant biome pages. Do not allocate textures on the GL render
hot path. T-040 selects deterministic scene variants with 8-scene cooldown
while preserving pause, POI priority and non-GPS-fiction fallback.

Full brief: `docs/ASSET_VARIETY_GUIDE.md`; animation/scene rules:
`docs/ART_EXPANSION_PACK.md`. Runtime tests and hardware benchmarks are
future work, not completed in this planning change.

