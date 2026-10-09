# M9 Benchmark protocol (read-only)

Hardware target: actual Samsung Galaxy Z Fold3 (model SM-F926*), Android 15, Samsung DeX 1920×1080.
Run date: TBD. Actual FPS/P95 frame time, CPU, PSS, battery, thermal and GPS event latency: NOT VERIFIED.
Target stable 30 FPS, >=60-minute session, no critical crash/ANR or uncontrolled PSS growth.
Real test requires both independent parent and child displays and safe navigation-audio coexistence.

## Capture procedure
1. Verify model and DeX display manually; keep screen on with navigation and kids active.
2. Use `./scripts/device-smoke.sh <serial>` for read-only inventory.
3. Run `./scripts/benchmark-fold3.sh <serial>` while operator observes real journey conditions; collect 61 timepoint samples.
4. Manually calculate P95 frame time from gfxinfo, PSS from meminfo, CPU with an agreed method, battery drain and thermal changes.
5. Inspect crash/ANR/route consistency and USB ADB disconnect; flag missing data as NOT VERIFIED.
6. Save evidence in issue/ExecPlan; do not publish raw private GPX logs or child voice.

## Offline virtual test
`./gradlew :core:test` includes SimulationSoakTest that fast-forwards 60 simulated minutes and validates monotonic movement/chunk boundedness. **It is not a real-time 60-minute FPS or battery test**.

## Emulator/SurfaceView evidence — T-011
Android gfxinfo describes UI/Skia frames and must not be reported as LibGDX GL render FPS/P95.
RenderRunMetrics provides whole-run raw Gdx frame-cadence count/time and1ms-histogram P95 upper
bound, logged every30seconds and at actual screen disposal. Report missing final intervals.
Simulator sampler tracks PID, resumed child, PSS and actual UTC/wall duration; never auto-PASS.
Record build/capture host interference and distinguish expected parent-limit expiry from crash.
Current real emulator baseline and physical hardware acceptance are separate gates.
