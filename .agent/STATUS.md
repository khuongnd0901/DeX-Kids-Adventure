
# DeX Kids Adventure - verified status

Date: 2026-10-08
Active milestone: M0 PROJECT BOOTSTRAP / M1 FOUNDATION
Active task: T-001 (IN_PROGRESS); T-002 (IN_PROGRESS)
Branch: feat/T-001-bootstrap-libgdx
Last verified source commit: pending this documentation commit.

## Implemented in source
- Gradle multi-module definitions for Android and desktop.
- Android launcher, desktop launcher and libGDX Game/Screen.
- Procedural deterministic world chunks and original code-drawn capybara bus.
- Frame-rate-independent simulated movement and pure JUnit test sources.
- Main Agent / task / ExecPlan structure.

## Evidence and blockers
- GitHub repository accessible and commit/push possible via connected GitHub integration.
- Environment in this execution context: JDK21 and git available; local Android SDK and Gradle were NOT found.
- No successful Gradle build/test, no APK, no desktop OpenGL smoke test, no Fold3, no DeX measurement obtained yet.
- Repo visibility currently PUBLIC, contrary to requirement PRIVATE. Must change in repository settings before sensitive materials are added.
- No real GPS, offline POI DB, Vietnamese narration, local assets pack, DeX routing, parent controls or IPC yet.
- 30 FPS / 60-minute performance not verified.

## Next actions
1. Run GitHub Actions workflow; fix dependency/compile/test issues it surfaces.
2. On Ubuntu host run ./gradlew :core:test :desktop:run and :android:assembleDebug with API35 SDK.
3. Capture runnable desktop screenshot and verify 30FPS diagnostics after instrumentation.
4. Complete T-001 with actual build evidence; then develop T-002 and T-003.
5. Change repository visibility to PRIVATE, if desired, via GitHub Settings.
