# T-003 / M2 ExecPlan — Capybara, yellow bus and layered world

## Goal
Replace disposable code-painted sprites with controlled SVG vector source, packed transparent PNG TextureAtlas and deterministic game rendering, retaining 1920×1080 scaling and real-GPS/demonstration separation.

## Milestones and actual checkpoints
- [x] Original SVG project-owned artwork: 18 frames/props across `art/assets-source`; tool generates `assets/generated/kids.png` and `.atlas`. CI [37804928269](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37804928269) SUCCESS.
- [x] Pure animation driver and physical-distance wheel rotation, unit tests. [37804695570](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37804695570) SUCCESS.
- [x] Layered parallax renderer, distinct per-biome props, GPU texture lifecycle via atlas dispose. [37805524313](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37805524313) SUCCESS after rounding tolerance test fix.
- [x] Software OpenGL at 1280×720 and 1920×1080, 10 real frames and storyboard GIF, Android APK. [37805970056](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37805970056) SUCCESS.
- [x] Four-chunk district grouping with deterministic seed test and actual OpenGL capture. [37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) SUCCESS.
- [ ] Final artistic direction signoff: visual style remains flatter/simpler than initial high-detail game mockup.
- [ ] Verify actual transition seams, 30–60 minute world scrolling and GPU texture memory on Z Fold3/DeX.
- [ ] Connect TALKING frame to verified Vietnamese narrator lifecycle and subtitles; speech must never be fabricated.
- [ ] Physical DeX actual 1920x1080 review with Google Maps/navigation coexistence.
- [ ] Release art license audit and hardware power/battery acceptance.

## Key implementation
`CharacterAnimationController`: stationary, rolling, sleeping, waving, narrator-gated talking and surprise.
`VehicleMotionModel`: frame-independent rotation, bounded pitch and bounce.
`CartoonSprites`: one 2048×2048 (16 MiB uncompressed) TextureAtlas; far/mid/near props; per-biome sprites; GL batch owned and disposed.
`WorldPainter` and `SceneryLayout`: same 640px chunk offsets, deterministic four-chunk biome districts.
`KidsGame` desktop smoke: 10 render frame PNG files per run, GIF generated from genuine OpenGL frames.

## Status
**IN_PROGRESS.** Compilation/GIF PASS is not device or release approval. Do not close issue #4 or merge PR #1.
Evidence: `.agent/evidence/T-003-M2-verified-2026-10-08.md`.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.
