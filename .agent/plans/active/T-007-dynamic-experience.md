# M6 T-007 Dynamic experience ExecPlan

Source checkpoint: time-of-day sky palette, pure local resolver + tests, ephemeral POI visit counts for content rotation.
Remaining: real GeoContext to biome transition, child-appropriate story events, persistent parent-controlled retention/deletion, end-to-end tests on device.
Do not treat procedural random biome as a factual location match.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.


## M6 POI-themed dynamic art layer — 2026-10-09
- Authored `PoiSceneDirector` pure-Java type-to-biome mapping and distance-anchored fade-in/out state machine, queued transitions, confidence/nearby guards and replay reset; unit test `PoiSceneDirectorTest`.
- Renderer crossfades only themed chunk decorations (`CartoonSprites.drawBiomeDecorations`) while preserving default procedural generation and fictional traffic. Bus, narration consent and same-display DeX controls unchanged.
- `AdventureScreen` consumes existing M4 POI event for **illustrative theme only**, never from raw unsourced GPS nor PASSING_CANDIDATE; adds explicit 'ILLUSTRATIVE SCENERY' HUD label.
- Desktop CI adds actual software OpenGL screenshot of HCMC GPX source-preview POI after reproducible preseek; JUnit integration verifies bundled synthetic route and scene event deterministic replay.
- [Design and QA guide](https://github.com/khuongnd0901/DeX-Kids-Adventure/blob/feat/T-001-bootstrap-libgdx/docs/M6_DYNAMIC_WORLD.md), [issue #7](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/7).
- Current CI result PENDING at this checkpoint. Actual physical Fold3/DeX testing explicitly deferred by owner; source content human approval remains OPEN.
- Status: **IN_PROGRESS**, draft PR, no release merge.
