# T-052 — Random Sâu superhero and Ong princess wardrobes

Status: **IMPLEMENTED IN SOURCE; VISUAL QA PENDING**. No original child artwork was edited.

## Feature

Adds **six new original superhero-styled outfits for Sâu** and **six new
fairytale princess outfits for Ong**. These are child-friendly authored
vector clothing overlays, not copies of copyrighted Marvel/Disney characters,
logos, franchise names or costume designs.

| Character | SVG regions in `art/assets-source/child-outfits/` |
|---|---|
| Sâu | `sau_solar` (sun guardian), `sau_tech` (tech knight), `sau_web` (web explorer), `sau_thunder` (storm hero), `sau_shield` (star sentinel), `sau_shadow` (night guardian) |
| Ong | `ong_rose`, `ong_ocean`, `ong_sky`, `ong_forest`, `ong_starlight`, `ong_lavender` |

Every file is **256×384 transparent SVG** with garment, cape/collar/belt or
tiara/gown artwork. It overlays the original 4-pose Sâu/Ong *Story V2* sprites
without replacing face, expression, animation, childhood identity or original
story assets. The same random outfit is layered on bus passenger sprites.
Costume colors and designs remain distinct enough to recognize in FullHD
when the proportions/placement are validated on emulator.

## Technical implementation

- `tools/build_child_outfits.py` rasterizes the 12 SVGs at build time,
  alpha-packs them in a single `2048×1024` `RGBA8888` atlas
  (`assets/generated/child-outfits.png` + `.atlas`). Approximate raw GPU
  memory is **8 MiB**, not counting driver/renderer overhead.
- `build_sprites.py` invokes this new builder in the same local art workflow,
  and `generateKidsArt` tracks the new script.
- `CartoonSprites` loads it at most once during initialization, caches the
  twelve `TextureRegion` references, draws outfit after the existing child
  sprite using the exact same transform, and disposes it with the screen.
- `OutfitRotation` uses a session seed to choose one of six unique outfits per
  child, stepping through all six without repeats every **240 simulated
  seconds**. No random per-frame allocations, no flicker. Sâu and Ong vary
  independently. Passenger and story use the same selection at each clock.
- Legacy four-job costume PNGs, pose sheets, T-048 bus/Capybara, gestures,
  GPS, voice, child dialogue and parental controls are unchanged.
- If the optional new atlas is absent, existing child sprites still render
  (no failure on optional clothing resource).

**Limitation:** A new character overlay must align against the existing
512×768 story PNG quadrants and trimmed bus passenger PNGs. Alignment of
tiaras, cape/shoulders, suits and skirt/feet is **NOT YET VERIFIED**
on a real Windows Android API35 1920×1080 emulator or physical Samsung DeX.
For optimal appearance, adjust the overlay SVG paths *only* after inspecting
actual image captures. Do not change Sâu/Ong original images as a shortcut.

## Testing checklist (run on Windows/WSL dev machine)

```bash
python3 tools/test_t052_outfits.py
python3 tools/build_sprites.py
python3 tools/test_sau_character_art.py
python3 tools/test_ong_character_art.py
python3 tools/test_m10_art.py
bash scripts/test-local.sh
./gradlew :core:test :android:assembleDebug
```

1. Run the installed APK using the actual Windows Android 15 API35 AVD at
   1920×1080 horizontal resolution. Preserve original Samsung DeX positioning.
2. Set audience to Sâu, then Ong, then BOTH; test WAVE, LOOK_UP, COUNT, PET.
3. Take actual screenshots: single Sâu hero, single Ong princess, BOTH next
   to the bus, both kids in the bus. Verify no facial obstruction, clothing
   clipping, atlas bleeding, logo/IP copy, or HUD overlap.
4. Validate costume 0–5 individually using a debug-only fixture or a seeded
   `OutfitRotation` and assert no changes within 240 seconds, then next outfit
   at each four-minute boundary. Confirm after six outfit changes one cycle
   repeats and there are no frame-time allocations.
5. Review runtime texture pages and memory impact, and check device-only FPS.
6. Collect QA under `.agent/evidence/T-052/` and attach screenshots if
   genuinely captured from Android Emulator. Do not claim PASS otherwise.

**Current evidence:** 12 SVG documents exist on GitHub, checked for balanced
XML tags, correct 256×384 viewport, safe in-document gradient IDs. Source
implementation and JUnit/Python regression tests were committed. No local
Gradle build, PNG atlas raster QA, emulator screenshots or physical DeX
measurements were run in this GitHub-only environment. Mark task PARTIAL
until actual screenshots show clothes fitting the original characters.
