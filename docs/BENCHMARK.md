# M9 Benchmark protocol (read-only)

Hardware target: actual Samsung Galaxy Z Fold3 (model SM-F926*), Android 15, Samsung DeX 1920×1080.
Latest physical DEMO run: 2026-10-10, T-021. FullHD GL 1920×1080 for 60 real minutes: 59.963 FPS, 1ms-histogram P95 upper bound 19ms. Warm PSS 147007–147883 kB; dumpsys CPU sample windows 29–30%; battery 96→90%, battery temperature 30.8–31.5°C, thermal status 0. GPS latency and navigation coexistence NOT VERIFIED. Evidence: `.agent/evidence/T-021-fold3-autonomous-qa-2026-10-10.md`.
Target stable 30 FPS, >=60-minute session, no critical crash/ANR or uncontrolled PSS growth.
Current product uses one external DeX display for Parent and child. Real journey acceptance also requires safe navigation-audio coexistence.

## Capture procedure
1. Verify model and DeX display manually; keep screen on with navigation and kids active.
2. Use `./scripts/device-smoke.sh <serial>` for read-only inventory.
3. Run `./scripts/benchmark-fold3.sh <serial>` while operator observes real journey conditions; collect 61 timepoint samples.
4. Use cumulative RenderMetrics for GL FPS/P95 (gfxinfo measures Android UI, not GL). Collect PSS from meminfo, CPU with its stated sampling window, battery and thermal changes.
5. Inspect crash/ANR/route consistency and USB ADB disconnect; flag missing data as NOT VERIFIED.
6. Save evidence in issue/ExecPlan; do not publish raw private GPX logs or child voice.

The T-021 endurance APK predates the Parent menu-only fix; the final APK is separately rebuilt and regression-tested. Battery readings cover the whole device, not app-only energy use. No conclusion of zero leaks or acoustic quality follows from these checks. See `docs/FOLD3_AUTONOMOUS_QA.md` for autonomous procedures.

## Offline virtual test
`./gradlew :core:test` includes SimulationSoakTest that fast-forwards 60 simulated minutes and validates monotonic movement/chunk boundedness. **It is not a real-time 60-minute FPS or battery test**.

## Emulator/SurfaceView evidence — T-011
Android gfxinfo describes UI/Skia frames and must not be reported as LibGDX GL render FPS/P95.
RenderRunMetrics provides whole-run raw Gdx frame-cadence count/time and1ms-histogram P95 upper
bound, logged every30seconds and at actual screen disposal. Report missing final intervals.
Simulator sampler tracks PID, resumed child, PSS and actual UTC/wall duration; never auto-PASS.
Record build/capture host interference and distinguish expected parent-limit expiry from crash.
Current real emulator baseline and physical hardware acceptance are separate gates.
