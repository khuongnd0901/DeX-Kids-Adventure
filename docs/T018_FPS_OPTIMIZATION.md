# T-018 — FullHD Samsung DeX 2D render fast path (source optimization)

Status: **SOURCE IMPLEMENTED / DEVICE FPS NOT VERIFIED** (2026-10-10). Issue [#21](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/21).

## Prior measured baseline (old code, Android emulator API35; NOT physical Z Fold3)
Previous 2026-10-10 emulator GL run full graphics: **14.15 average FPS / 114.63ms P95**; minimal visuals: **19.58 FPS / 81.76ms P95**. These are short software-emulator windows, not a valid estimate for GPU-accelerated Samsung DeX.

## Render improvements
1. Replace per-frame `ShapeRenderer` vector sky, six chunks, circles, ground and transparent HUD with one tiny, once-created `130x130 RGBA` primitive texture (a solid white pixel and a filled circle region) drawn through the SAME LibGDX `SpriteBatch` as the existing character atlas. One animated `batch.begin()/end()` per frame (audio-only retains its own single pass); no per-frame CPU circle tessellation, fewer draw submissions and GL state transitions. Alpha HUD now painted after world sprites and before text.
2. **Four tiles** [`first..first+3`] of width 640 instead of six [`first-1..first+4`]; provably cover the FullHD width (unit-tested across chunk boundaries) and omit offscreen scenery/traffic. Trim far parallax tile loops.
3. `WorldWindow` uses fixed ring of `WorldChunk[]`, with zero compute on unchanged-chunk frames. On move/seek it only regenerates newly entered tiles; deterministic re-entry and negative chunk indices preserved. Removes per-frame boxed keys and map eviction scanning.
4. Cache `WorldMoodResolver` time check to once per minute, not on every graphics frame. Reuse a single themed `PoiSceneDirector.Scene` record per frame.
5. Request Android EGL `RGB565, alpha=0, depth=0, stencil=0, numSamples=0` for 2D external-display rendering, if device EGL supports it. Output can have lower gradient color precision; if unsupported the EGL chooser may pick an alternate configuration. Compare visuals on Fold3.
6. Emit `sprite_draw_calls_prev` with `RenderMetrics` 30s logging for in-person A/B investigation. Rendering keeps real GPS/POI gate, on-device ASR, TTS, AI opt-ins, one-screen F10, and scene/bus motion unchanged.

## Local tests only
`./scripts/test-local.sh` (existing local contract/Python source guards plus `./gradlew --no-daemon :core:test :android:testDebugUnitTest`). New `tools/test_render_fast_path.py` checks no ShapeRenderer in hot path, one batch, 4 chunks, ring cache, native no-depth framebuffer. New JUnit `WorldWindowTest` and `SceneryLayoutTest` cover unchanged 100,000 frames, determinism, boundary positions and negative/replay seeks.

**This execution environment cannot clone GitHub or resolve Gradle dependencies over network.** An isolated local JVM harness using the exact ring-window algorithm and SceneryLayout formulas has **579 assertions PASS** (not full repository Gradle). Therefore full Android and `:core:test` are to be run by operator locally; no CI job runs and no device benchmark is claimed.

## Physical device acceptance
On Samsung Z Fold3 with external DeX FullHD only, launch app in its actual GPS/GPX/demonstration mode and inspect `adb -s <serial> logcat -s RenderMetrics:I`. Over a repeated comparable >=2 minute window compare full-scene FPS and P95, `sprite_draw_calls_prev`, GPU/thermal, spoken prompts and input quality. Optionally `./scripts/benchmark-fold3.sh <serial>` captures 60min endurance. If color gradients/dithering become unacceptable, revert RGB565 to RGBA8888 independently of the batching/ring optimizations.

**30 FPS hardware target NOT VERIFIED**. Still need real device FPS; prior software emulator is not proof of performance gain.
