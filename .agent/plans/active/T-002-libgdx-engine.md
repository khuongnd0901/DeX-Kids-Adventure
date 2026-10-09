
# ExecPlan T-002 – Engine foundation

## Goal
30FPS-oriented rendering, lifecycle, diagnostics and clean separation of journey state from rendering.

## Current source
OrthographicCamera/FitViewport, ShapeRenderer, SpriteBatch, procedural world, DemoJourneyFeed and SpeedSmoother.

## Remaining
Add observable FPS and P95 frame-time metrics; test pause/resume, rotate, background/foreground; measure scene memory and long-run frame stability on desktop and Android emulator.

## Acceptance
Runnable desktop and emulator evidence with screenshots/logs. Stable targeted 30 FPS cannot be marked achieved before measurement. No production GPS claims.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.
