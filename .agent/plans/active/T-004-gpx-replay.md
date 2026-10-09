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
