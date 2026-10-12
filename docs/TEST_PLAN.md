## Indefinite child session acceptance update (2026-10-12)

LIVE, DEMO and GPX Activity lifetime is no longer linked to 15/30/60-minute
watching limits. Test child remains open after 30m and 60m, including
pause/F10/Continue, Android Activity recreation, and GPX finish/stationary
state. Only explicit **End adventure** or **Dừng hành trình**, user/OS
closing the Activity or unrecoverable setup permission/import failures
should stop it. The previous time-limit tests have been removed, not
skipped. Run local `tools/test_unlimited_session.py`, core JUnit, Android
unit and AndroidTest, and physical 1920x1080 Fold3 DeX soak. Historic
duration-limit guidance below is superseded.



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
