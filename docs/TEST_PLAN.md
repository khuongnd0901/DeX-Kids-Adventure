
# Test plan

M0: Gradle core JUnit; desktop classes; Android debug APK. CI must report result.
M1: Desktop smoke at multiple resolutions, FPS logging, lifecycle/pause/resume.
M2: 30/60-minute chunk-soak, visual seams and GC review.
M3: GPX deterministic replay, GPS jumps, stale fixes, signal loss/recovery.
M4: POI ground truth/direction confidence and OSM offline checks.
M5: Narration cooldown, verified facts, Vietnamese offline voice, audio focus.
M8: Actual Z Fold3 DeX external display and co-running DeX-Assistant / navigation.
M9: 60-minute frame/memory/thermal/power capture.

Evidence goes to .agent/evidence/ with commands, environment and ACTUAL results; never infer test PASS from authored test source.

## Reproducible local simulator QA — T-011
See scripts/simulator/README.md and .agent/evidence/simulator/ for actual run results.
Explicit ADB serial required; local Windows adb.exe interoperates with WSL through wslpath.
Core56-test regression includes GPS end/loss/recovery, cadence and >100chunk replay; actual
Android UI/recreation tests and real elapsed soak are separate. ADB broadcast result is not
permission evidence: use distinct-UID/certificate and matching-signature test APK probes.
Do not treat core virtual60minutes, desktop softwareGL or emulatorPSS as Fold3 benchmarks.
