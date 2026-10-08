# DeX Kids Adventure

Offline-first, GPS-aware 2D educational companion prototype for children aged 2–6 on Samsung DeX.

**Status: DEVELOPMENT / DRAFT PR #1, NOT RELEASE READY.** Source for M1–M9 milestones has partial implementations and passing CI builds, but real device acceptance, finished cartoon artwork and verified Vietnamese geographic narration remain open.

- Android native Kotlin parent UI; Java17 LibGDX shared core; desktop LWJGL3 runner.
- Demo 2D world with original code-drawn yellow bus and capybara, bounded deterministic world chunks and local day/night palette.
- GPX replay with deterministic timestamp interpolation. Real GPS distance mode is opt-in from parent controls and only starts after Android permission approval; it never asserts any POI name because no reviewed offline dataset is bundled.
- Provenance-gated story engine and offline Vietnamese TTS adapter source are present, but not connected to a reviewed content pack. No children’s audio is collected or sent to any server.
- Child screen targets external display when explicitly selected by parent; Samsung DeX launch unverified. No automatic fallback to phone.
- DeX Assistant repositories are untouched; draft signature-permission STOP IPC requires signing-contract audit before cross-package use.

## Verify locally (Ubuntu 24.04)

Prerequisites: JDK17, Android SDK35 with build tools 35.0.0, Linux desktop OpenGL.

```bash
./gradlew :core:test :desktop:classes :android:assembleDebug
./gradlew :desktop:run
./gradlew :desktop:run --args="--gpx test-data/gps/synthetic-urban-short.gpx"
```

Phone/external display hardware checks:
```bash
./scripts/device-smoke.sh <adb-serial>
./scripts/benchmark-fold3.sh <adb-serial>
```

These scripts do not grant permissions, modify system settings, install software or spoof locations. The benchmark script collects read-only samples from an already running game; do not treat virtual simulation tests as physical FPS evidence.

## Project management

Read `AGENTS.md`, `.agent/STATUS.md`, `.agent/TASKS.md`, and the current milestone's ExecPlan.
Original requirements are preserved in `docs/MASTER_EXECUTION_PROMPT.md`.
Actual test evidence lives under `.agent/evidence/`.

## Privacy and licensing

No cloud GPS upload, child voice collection or advertisements are implemented. Third-party content is not bundled. Attribution and copyright clearance are required for production. The repository currently exposes code publicly; this differs from the original request to use a private repository.
