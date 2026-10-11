# T-026 — Current build and 30-minute route QA preparation

Owner Main; dependencies T-014/T-019/T-021–T-025.
Acceptance for this request: current debug + AndroidTest build, local contracts/unit tests; reviewable 30-minute schedule covering all requested-region points with full-catalog variant, no invented GPS or runtime results; append build evidence and task/status.
Runtime route acceptance is a separate open gate: test-only fixture harness, 26 IDs/30 minutes, observed audio/render/lifecycle. Existing production guards remain intact.
See docs/T026_30_MIN_ROUTE_TEST.md and test-data/gps/T-026/*.json.

## Physical run authorized 2026-10-10
User requests actual 30-minute ADB Z Fold3 session. Main implements test-only
RouteThirtyValidation and host collector. Synthetic JourneyFeed replaces only
the instrumentation-owned screen feed; per-point original catalogs loaded with
one selected entry, actual OfflinePoiEngine/story/scene/native intro run.
Production LIVE dialogue scheduler is invoked after observed synthetic intro
for22 LIVE cards;4 HCMC cards have no dialogue. Overlay says simulated.
This does not test AndroidGpsSource/LiveFixGate or real road passage; no location
provider manipulation, permission changes, child recording or cloud requests.
Smoke first, then26 points/1800s natural deadline, FullHD/display checks, F10
pause/resume, resource logs, per-point stage observations. Restore parent/AI
preferences, remove test APK and return Parent on DeX after collecting evidence.
Retain failures and interrupted runs; never mark a shortened run as30min PASS.
