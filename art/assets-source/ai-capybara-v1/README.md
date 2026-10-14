# T-048 — Capybara + 2D side bus art source pack

**State (2026-10-14): ORIGINAL ZIP VERIFIED LOCALLY; PNG BINARY FILES NOT COMMITTED ON GITHUB.** Do not treat this branch as game-ready.

A set of five original ChatGPT-generated transparent RGBA PNG source images was prepared on 2026-10-14. Its original ZIP was recovered from ChatGPT Library and all five image hashes, dimensions and alpha bounds were verified independently. The GitHub connector cannot transfer the actual PNG bytes from the isolated working container; this branch still contains metadata only. The binary files must be committed by a machine with local Git access before runtime integration.

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

On a Git-enabled checkout of this T-048 branch, run `python3 tools/import_t048_art.py /path/to/DeX-Kids-Adventure-art-v1.zip`, then `git add art/assets-source/ai-capybara-v1/*.png && git commit -m 'T-048: import 5 verified PNG source binaries' && git push`. Confirm the five PNGs exist remotely and match `manifest.json`, then continue the ExecPlan. **No production image replacement has happened yet.**

### Local continuation — 2026-10-14

The original ZIP and PNG bytes are not available in the current local checkout.
Importer execution with the existing QA Pillow environment fails with
FileNotFoundError; no new identity/alpha verification or runtime replacement
occurred. Earlier Library verification remains historical. Current blocker:
accessible original ZIP path; local Git supports binary commit/push once present.
See `.agent/evidence/T-048-2026-10-14/LOCAL_M0_BLOCKER.md`.
Licensing/redistribution and parent visual acceptance remain OPEN.
