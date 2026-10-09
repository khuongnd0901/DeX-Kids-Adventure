# P0 Android simulator E2E and renderer A/B

Scope: **emulator testing only**, not hardware or release acceptance.

Workflow: [P0 Android emulator E2E](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37930356631).
Runbook: `scripts/ci/p0-emulator-e2e.sh` requires an **isolated Android emulator** (`ro.kernel.qemu=1`). Do not run the automated permission-grant fixture on a physical Samsung device.

Modes in `LifecycleValidationRunner`:
- `single_display_mouse`: parent dashboard → game → one-click parent dialog → End (one display).
- `p0_hcm`: source-cross-checked but unreviewed synthetic GPX → PARK themed scene + native Vietnamese subtitle (TTS off by default).
- `p0_perf_ab`: real GL rolling 180-frame data, Full/Audio-only/Full/Audio-only on same emulator.
- `p0_live`: emulator GPS-provider location updates → positive live distance. ADB sends `geo fix` during instrumentation.

GitHub run #37930356631: overall CI SUCCESS. Mouse, HCMC and LIVE GPS **PASS**. Performance **FAIL** vs 30FPS: Full 11.67 FPS / P95 146.02ms, Audio-only 16.68 FPS / P95 95.45ms (means of two samples per mode). These are software-emulator results and do not demonstrate Fold3 30FPS or prove the extra artwork caused regressions.

[Runtime logs and screenshots](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37930356631/artifacts/11616275848). Screenshot files captured, manual art evaluation still open. GPS history is not persisted by the application. Production POI approved catalog remains empty. Real Vietnamese TTS playback and audio focus NOT VERIFIED.

Do not mark P0 performance, M9 release or physical Samsung DeX hardware gates DONE.
