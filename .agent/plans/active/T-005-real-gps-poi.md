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


## 2026-10-09 — M4 offline proximity engine implementation

### Completed in source
- `JourneyFeed.position(now)` exposes optional immutable `JourneyPosition`: LIVE GPS quality-fresh accepted fixes, or explicitly flagged GPX simulated interpolation. DEMO reports no position, and rejected teleport segments return no coordinate.
- `OfflinePoiCatalog`: strict 512 KiB UTF-8 TSV, max 5,000 rows, exact canonical OSM ID/URL, coordinates, type, review timestamp and reviewer ID. Fail-closed missing/invalid pack.
- `OfflinePoiEngine`: 0.005-degree tile index, closest candidates, accuracy <=25m, bounded confidence >=0.55, one sample per >=800 ms, anti-duplicate and 30s global notification cooldown; **PASSING_CANDIDATE is geometry only**, not a factual crossing.
- Android/LibGDX: core detects events without a GPS logger, Android native overlay renders Vietnamese POI names with OSM attribution (not LibGDX default ASCII font); title is marked GPX simulated or GPS estimated. No automatic narration, real-road map matching or cloud API calls.
- `tools/import_osm_pois.py`: offline transformation of a *previously saved* Overpass JSON extract to **UNREVIEWED** candidate TSV and SHA256 provenance receipt. Import never edits the approved asset pack.
- `tools/validate_reviewed_pois.py`: required review-ledger mapping and ODbL license notice in CI. `assets/poi/reviewed.tsv` is currently **empty**; no geographic named-place claim will be emitted until source entries are manually checked/approved.
- JUnit test suite for simulated/fresh position, no fake teleport, catalog validation, dedup, accuracy/confidence/cooldown, reset and passing candidates.
- CI: source checkpoint 991f3ae8 [run #37891960202](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37891960202) PASS; later #37892078068 initially failed a **test-only Java lambda compile issue**, fixed in a64e7f56, [run #37892403773](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37892403773) PASS. Full latest pipeline after importer/renderer/metadata/optimizations still pending — do not claim success prematurely.

### Acceptance gates still open
- Real HCMC OSM extract and point/way centroid review ledger with actual verifier and screenshot/road-context reference: **NOT_IMPLEMENTED** in package (there are 0 approved rows).
- Real route/map matching and directed road passage verification: **NOT_IMPLEMENTED**; 'PASSING_CANDIDATE' never spoken as an observed passage.
- Android UI gameplay with actual approved POI pack, accessibility/Unicode/consent: **NOT_RUN**.
- Physical Z Fold3/DeX accuracy, latency and thermal: **DEFERRED by user**, not marked PASS.
- Full M4 **IN_PROGRESS**, do not merge/close issue. M5 needs vetted narrative content independent of OSM points.
