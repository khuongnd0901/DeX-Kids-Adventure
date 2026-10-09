# M4 T-005 Android location and offline POI ExecPlan

Code milestone: quality-gated GeoFix, immutable source-bound POI, distance/event detector and opt-in GPS Android adapter.
Current source requires explicit runtime ACCESS_FINE_LOCATION grant and foreground start call; tracking is not enabled automatically.
Real OSM extract, offline indexed database, direction map matching, permissions UX, battery test and physical POI truth set remain pending.
Acceptance: actual HCMC dataset with ODbL attribution, no false PASSED claims, verified on Fold3. Keep IN_PROGRESS.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.
