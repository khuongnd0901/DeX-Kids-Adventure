# T-012 — P0 Android integration and performance

**Status: IN_PROGRESS**. User request 2026-10-09; CI functional slice verified, 30FPS gate FAILED.
Issue [#15](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/15), draft [PR #14](https://github.com/khuongnd0901/DeX-Kids-Adventure/pull/14). Based on M7/M8 draft PR #13; PR #1 remains unmerged.

## Implemented
- Disposable Android 15/API35 x86_64 GitHub emulator via `.github/workflows/p0-android-integration.yml` and `scripts/ci/p0-emulator-e2e.sh`; explicit `ro.kernel.qemu=1` gate prevents physical device privilege fixture.
- Custom `LifecycleValidationRunner` modes `single_display_mouse`, `p0_hcm`, `p0_perf_ab`, `p0_live`.
- HCMC E2E: real LibGDX render thread GPX fast-forward (using production update) → *explicitly unreviewed sample* HCMC POI → PARK scene → Android native Vietnamese caption; no TTS voice assertion.
- LIVE GPS: permission grants only inside isolated CI AVD, external `adb emu geo fix` inputs, Android GPS provider and render-thread `LiveJourneyFeed` distance; permissions revoked before exit. Raw coordinates intentionally omitted from result logs.
- Same binary/process/density A/B/A/B Full vs Audio-only graphics with 2s warmup + 6s actual GL sampling per mode; captures FPS and P95 on rolling last 180 frames. QA completion and performance acceptance are separate assertions.

## CI evidence (2026-10-09)
[Run #37930356631](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37930356631) **SUCCESS** on `02e275af`. [Artifacts](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37930356631/artifacts/11616275848).

| Check | Measured outcome | Result |
| --- | --- | --- |
| Android/core compilation & unit tests | Compiled Debug + AndroidTest; core tests passed | PASS |
| Single display mouse parent menu | F10 and/or click, pause, End, same display 0 | PASS |
| HCMC sample M3–M6 | GPX ~180.66s; PARK; VI subtitle, unreviewed source label | PASS on emulator |
| LIVE GPS native provider | GPS accuracy 5m; accepted fix; distance 12.926m | PASS on emulator |
| Full animated GL, window A then A | 10.94 / 12.40 FPS; P95 174.05 / 117.99ms | FAIL 30FPS gate |
| Audio-only GL, window B then B | 16.74 / 16.62 FPS; P95 101.67 / 89.23ms | FAIL 30FPS gate |
| Actual spoken offline VI TTS and navigation audio focus | No playback tested; default consent OFF | NOT VERIFIED |
| Physical Fold3 DeX, 60-min CPU/PSS/thermal, release | No physical phone connected | BLOCKED |

Earlier historical CI failures #37928255349 (brittle accessibility lookup), #37928939538 (LibGDX create race), #37929556218 (instrumentation reading GL-owned nonvolatile feed distance from wrong thread) remain documented. Fixes were verified in #37930356631; do not erase those failures.

## Remaining work
1. Profile single atlas/shape/environment render costs; controlled old/new art version benchmarks **on the same hardware**. Full vs Audio-only is not an art-version comparison and does not establish why earlier art smoke was 19.825FPS.
2. Optimize 30FPS target without falsely claiming Fold3 acceptance from software GPU. Add sustained frame histogram, CPU and PSS when test fixture is available.
3. Add Android GPS dropout/recovery and permission-denied regression without unexpectedly changing shared device grants.
4. Verify offline Vietnamese voice installed, talk-animation real onStart/onDone, caption synchronization, audio focus vs Google Maps/Vietmap.
5. Source-reviewed OSM POI/narration production pack, geometry/road matching, legal/editorial review.
6. Physical Samsung Z Fold3 broken-screen single-display DeX; thermal/battery/60min and signed release gate.

## Safety
Never introduce PIN, child lock, touchscreen-only control, cross-display parent UI, public IPC START, GPS upload or silent cloud TTS. No changes to DeX-Assistant or DeX-Assistant-UI. Keep all release/milestone gates open until real evidence.
