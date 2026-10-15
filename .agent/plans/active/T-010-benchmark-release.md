# T-010 M9 Benchmark / Release ExecPlan

Implemented source: virtual 60-minute simulation JUnit test, read-only real-device sampling scripts and release checklist.
Exit gates: physical Fold3 plus DeX and navigation coexistence, full real-world 60-min capture, P95 frame/FPS PSS CPU thermal battery, signed release APK installed/upgraded, end-to-end POI/narration verified, privacy/security/license review.
Stationary physical measurements now verified in T-060 (2026-10-15):
Fold3 DeX2 FullHD,actual3600sSAU/Full,59.945FPS/P95upper20ms;112PSS/CPU/
thermal/battery samples,120checks,86resolutions,voiceUnavailable0.
Evidence `.agent/evidence/T-060-fold3-2026-10-15/SUMMARY.md`.
Actual road/navigation coexistence,signed release/content/license gates remain
NOT VERIFIED;visual semantic FAIL and Native retention finding OPEN.
Release gate remains BLOCKED;stationary result does not close M9.
CI test step is not proof of device benchmarks.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.
