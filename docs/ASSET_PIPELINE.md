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
