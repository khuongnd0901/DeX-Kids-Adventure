# T-048 — Capybara + 2D side-bus import and gated runtime replacement

Owner: Main. Branch: `feat/T-048-capybara-bus-art-import`; PR #55 (Draft).
Baseline: main `adf282c`; T-048 branch was 2 ahead / 0 behind at audit.
Keep PR Draft and do not merge before required acceptance. Current user authorizes local runtime replacement and Windows emulator verification after M0; physical Samsung DeX remains a separate NOT VERIFIED gate.

## M0 — immutable source gate: BLOCKED ON GITHUB BINARY UPLOAD
- Locate original `DeX-Kids-Adventure-art-v1.zip` (7,294,553 bytes).
- Compare 5 PNG original SHA256, RGBA dimensions and alpha bounds against BOTH ZIP and repository manifests.
- **Local verification PASS** 2026-10-14: all five match; corruption and unexpected ZIP entry tests reject.
- `tools/import_t048_art.py` performs strict, no-overwrite local import and was tested against the actual ZIP.
- **Remote upload BLOCKED:** connected GitHub methods can write UTF-8 files/blobs, but cannot transfer original PNG binary bytes from this isolated local container; no PNG has been pushed.
- To clear: `python3 tools/import_t048_art.py /path/DeX-Kids-Adventure-art-v1.zip`, `git add art/assets-source/ai-capybara-v1/*.png`, commit and push on this branch; confirm `git ls-tree -r HEAD` contains five PNG blobs and SHA256 still matches.
- Stop before M1 until those remote blobs have been verified.

## M1 — LibGDX PNG pipeline (NOT STARTED)
- Review `tools/build_sprites.py`, `tools/build_m10_atlas.py`, `assets/generated/kids.atlas`, Android asset copy and `CartoonSprites`.
- Preserve current SVG sources/atlas and rollback switch. New atlas should have bounded page count and pre-sized textures; enforce source SHA and deterministic packing.
- Existing `VehicleLayout` assumes 660:290 / 950:417, while PNG bus is 1448:1086. Maintain actual ratio and baseline rather than stretching; validate full-HD layout and Sâu/Ong passenger seats.
- Bus image includes driver + baked wheels: composite mode MUST skip both `batch.draw(face,...)` and `drawWheel` calls. Keep vehicle bob/pitch without duplicate elements; animate only after separate approved layers.
- Avoid constructing Pixmap/Texture in render loop. Dispose atlas once; account for additional RGBA texture residency.

## M2 — expression state (NOT STARTED)
- Map IDLE, LAUGH, ASK, WAVE to 4 source PNGs via deterministic emotion cues.
- Preserve `CharacterAnimationController` narration/speed/wave/sleep behavior; do not replace story or motion profiles wholesale.
- Use consistent centered/baseline pose geometry and bounded cross-fade. Bus-composite driver remains baked and is not independently switched.

## M3 — validation (NOT RUN)
- Execute art importer verification + pipeline atlas tests, `scripts/test-local.sh`, core JUnit, Android debug Gradle build locally. Do NOT run CI.
- DeX FullHD screenshots in different journey/story states; measure physical Z Fold3 FPS/P95, GL texture residency and resource lifecycle, and compare to baseline.
- Mark tests/devices NOT VERIFIED without logs, screenshots and measured traces.

## M4 — signoff (OPEN)
- Update task/status/provenance and PR #55 with actual tested evidence and blockers; keep PR Draft and all legacy sprites for rollback.
- Merge only after parent/visual/device/engineering acceptance.

## Local continuation checkpoint — 2026-10-14

M0 is BLOCKED by missing original ZIP in this local environment, rather than
Git binary transfer capability. Existing QA venv has Pillow 12.3.0; importer
verify-only fails with FileNotFoundError. No PNG imported. Source receipt:
`.agent/evidence/T-048-2026-10-14/LOCAL_M0_BLOCKER.md`.
User requires stopping after import failure. M1/M2 remain NOT STARTED; M3
local/build/emulator NOT RUN. Windows tools/three AVDs available, no running
ADB device. Resume at M0 with accessible ZIP, then M1–M4. Do not run CI.
