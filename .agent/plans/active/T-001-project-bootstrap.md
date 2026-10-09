
# ExecPlan T-001 – Project bootstrap

## Objective
Publish a real multi-module starter on GitHub with runnable Android and desktop launchers and self-contained task memory.

## Milestones
- M0.1 Repository access and GitHub push: SOURCE VERIFIED by connector.
- M0.2 Gradle files + Kotlin Android launcher + Java17 desktop/core: SOURCE CREATED; compilation NOT VERIFIED.
- M0.3 Deterministic demo and unit tests: SOURCE CREATED; test result NOT VERIFIED.
- M0.4 CI build/test, emulator smoke and debug APK: BLOCKED pending CI/SDK.
- M0.5 Actual repository privacy enforcement: BLOCKED; public repository.

## Tests
./gradlew :core:test :desktop:classes :android:assembleDebug
./gradlew :desktop:run (requires desktop OpenGL)
On device: adb install -r android/build/outputs/apk/debug/android-debug.apk

## Exit criteria
GitHub code and docs present; reproducible Gradle build; desktop smoke; Android debug APK produced; status/evidence reflect actual verification.

## Notes
This is implementation work, not merely a proposal. Current source renders original, procedural flat-shaded cartoon graphics and labels all movement as DEMO.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.
