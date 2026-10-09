# T-004 M3 GPX Replay ExecPlan

Scope: deterministic offline GPX parser+replayer, speed smoothing, pause/resume, GPS jump guards, desktop entry `--gpx synthetic-route.gpx`.
Implementation checkpoint: source and JUnit written.
Acceptance open: desktop full 30-minute and 60-minute simulated runs, screenshot and measured actual replay timings, real location loss/recovery.
Evidence: .agent/evidence/T-004-M3-source-2026-10-08.md
Do not claim physical GPS from a GPX fixture.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.

## M3 Android Single-Display GPX Replay — 2026-10-09

Implementation commits:
- `d20f94d4`: limit GPX to 4 MiB input bytes and 20,000 points; replay state getters, pause/resume/reset/end-of-track tests, XXE protection retained.
- `15b06ffc`: Android DocumentProvider/Saf file selection in ParentActivity, launch KidsActivity on SAME display, controls Pause/Resume and Restart, explicit synthetic GPX HUD.
- `c7183ec1`: **important runtime lifecycle correction**. LibGDX must initialize synchronously in AndroidApplication.onCreate before AndroidApplication.onResume. Introduced DeferredGpxJourneyFeed (stationary/loading until background parse attaches on GDX thread). On configuration recreation retains adapter & timeline, without storing GPS location in files/database.

Accepted source contract: only `content:` URIs from document picker, no direct filesystem or broad storage permission, no URI persistable grants, no cloud transport in app code. File parsed with a 4 MiB byte limit, max 20K track points, secure DOM validation. Model never substitutes Demo for a failed GPX.
Current source implementation status: IN_PROGRESS until latest CI confirmed. Emulator SAF-picker E2E and actual Z Fold3/DeX are NOT_RUN and not required for this software sprint; do not mark M3 DONE.
Test plan: [Android GPX Replay guide](../../../docs/GPX_REPLAY_ANDROID.md).
Next: final CI, emulator optional for file picker and timeline UI, then M4 verified offline POI pack.
