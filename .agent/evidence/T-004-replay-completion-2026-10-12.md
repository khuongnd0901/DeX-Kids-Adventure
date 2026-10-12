# T-004 / T-011 replay completion slice
Owner Main; dependencies T-004/T-006/T-011. API35 ZFold3_API35 emulator-5580,
Windows emulator 37.1.11/WHPX; not physical Samsung DeX.

Baseline actual Android regression FAILED: `GPX holder reset on recreation`.
Raw: `build/simulator-artifacts/20261012/sample-baseline.txt`.
Correction retains the bundled HCMC deferred feed like SAF replay.
Added 0.5/1/2/4x playback selection; reset preserves rate. Invalid rates rejected.

Verified 84 core tests, zero failures/errors/skips, desktop classes and debug plus
AndroidTest builds SUCCESS (`20261012/replay-build.log`). Actual runtime suite:
`runtime-suite-20261012T013805Z-3127/`: all four modes PASS (secure Android parser;
HCMC parent action, pause/frozen timeline/rate/recreation/resume/restart; demo
recreation; F10 same-display pause/end). Demo deadline 1943628 unchanged,
distance 24.6018→55.4393m, identical retained feed. Sample screenshot inspected:
bus/Capybara/rabbit and all replay controls readable. Sampled frame stats are
not a sustained performance benchmark. Test APK removed, dashboard restored.
No location grants, network/voice changes or physical hardware acceptance.

Slice acceptance met. Full milestone stays IN_PROGRESS for long replay,
real GPS/device acceptance and remaining release gates. Previous SAF picker
PASS belongs to 2026-10-09; picker was not rerun in this slice.
