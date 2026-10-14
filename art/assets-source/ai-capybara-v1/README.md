# T-048 — Capybara + 2D side bus art source pack

**State: STAGED METADATA ONLY — PNG BINARY FILES NOT YET COMMITTED.** Do not treat this branch as game-ready.

A set of five original ChatGPT-generated transparent RGBA PNG source images was prepared on 2026-10-14. Its downloadable ZIP is in the originating ChatGPT conversation. The binary image files must be copied from the ZIP (or attached to the new session) into this directory before any runtime integration.

Expected files:
- `capybara_idle.png`: full-body neutral/idle explorer.
- `capybara_laugh.png`: full-body laughing.
- `capybara_ask.png`: full-body questioning.
- `capybara_wave.png`: full-body waving.
- `bus_side_with_driver.png`: yellow-and-blue side-view tour bus facing right, Capybara already pictured as driver.

The ZIP also contains `manifest.json` with file SHA256, dimensions and alpha bounds. Image format: transparent RGBA PNG. Source images are conceptual illustrations awaiting parent acceptance and game-scale review, not final exported sprites.

## Integration constraints

1. Preserve current `art/assets-source/characters/capybara_*.svg`, `art/assets-source/vehicles/{bus,wheel}.svg` and production mappings for rollback.
2. Audit `tools/build_sprites.py`, `tools/build_m10_atlas.py`, current atlas regions, game renderer/VehicleLayout, animation and resource disposal. They currently consume SVG / generated atlases; add PNG conversion/import safely instead of renaming incompatible sources.
3. The bus PNG **already includes** a driver and rendered wheels. Do not render a duplicate driver or overlay rotating wheels on top of baked-in wheels. For dynamic driver/wheel animation, derive separate approved layers first; otherwise render the bus as a static composite initially.
4. Scale all poses consistently with a stable baseline, no edge clipping, correct alpha blending and filtering. Profile texture RAM / FPS on actual Samsung Z Fold3 DeX FullHD.
5. Keep all children/voice/POI/GPS behavior unchanged. Local tests and APK build precede Fold3 device acceptance. Do not invent PASS results or artwork redistribution rights.
6. Record provenance/release review in `docs/ASSET_LICENSES.md`, and update `.agent/TASKS.md`, `.agent/STATUS.md`, and an ExecPlan when implementation starts.

## Next session

Attach or unzip `DeX-Kids-Adventure-art-v1.zip` from the originating chat, import its five PNGs into this directory, verify against `manifest.json`, then continue source integration. **No production image replacement has happened yet.**
