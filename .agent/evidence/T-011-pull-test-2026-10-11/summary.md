## T-011 / T-027 latest-source pull and local QA — 2026-10-11

Owner Main; dependencies T-011/T-027. User requested latest pull and tests.
Clean main fast-forwarded from e9138c4 to origin/main a2570aa.
Acceptance for this slice: pull, execute local contracts/core tests and target builds,
record actual outcomes. Full task/device acceptance remains open.

18 local source contracts executed independently: 16 PASS, 2 FAIL.
- test_adventure_dashboard.py: expects 66 occurrences of `new Beat(`; upstream now has 67.
- test_t024_t025_audio_engagement.py: expects exact prior speech-rate expression;
  upstream adds English 0.82f branch, retaining Vietnamese age rates.
Original `bash scripts/test-local.sh` stops at first failure; full suite is NOT PASS.

Gradle retry: 109 core tests, zero failures/errors/skips. Android unit NO-SOURCE.
Android debug APK, AndroidTest APK and desktop classes BUILD SUCCESSFUL.
First build failed because Python art modules were not on import path; retry used
existing /tmp/dexkids-art-python CairoSVG/Pillow via PYTHONPATH, JDK17 and Android SDK.
No source/test behavior changed. Upstream test-local.sh mode is 644, invoked with bash.
No install, device instrumentation, runtime visual/audio/English voice/performance
or endurance tests executed in this slice. T-011/T-027 remain IN_PROGRESS.
Logs preserved alongside this file; no commit/push performed.
