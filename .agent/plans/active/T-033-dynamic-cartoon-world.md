# ExecPlan T-033 — Dynamic Cartoon World

Owner Main. Source base main at 7ad2eda (T-034).
Scope M0 contracts, M1 animation, M2 local storyboard, M3 OSM GeoWorld, M4 scene composition, M5 QA and optional AI expansion.
Source implementation integrates with existing `PoiSceneDirector`, `VisualCue`, `CreativeLessonPack`, `KidsGame`, `KidsActivity`; no new backend or CI.

## Checkpoints
- [x] Code: local allowlisted VisualPlan, registry, timeline and tests
- [x] Code: Android question-answer semantic events; GL render synchronization
- [x] Code: OSM raster importer, geo index/director, conservative sourced visual feedback and tests
- [x] Code: scene priority POI > GeoWorld > procedural fallback; ambient props
- [x] Docs: full Codex test matrix, source contracts, QA evidence template
- [ ] Execute local Gradle/build and source-contract suite (connector cannot execute)
- [ ] Install/test physical Fold3 FullHD DeX and concurrent navigation audio
- [ ] Obtain five verified road-corridor OSM polygon extracts; manually review and bundle production grid
- [ ] Real 30–60m creative AI + GPU thermal/resource soak, adult content review
- [ ] Optional T-033.M5 AI-authored visual JSON (disabled to preserve T-034 two-HTTP first-pass policy and content-safety QA)

Never mark unrun QA PASS. `docs/T033_CODEX_TEST_CASES.md` is canonical local execution checklist.
