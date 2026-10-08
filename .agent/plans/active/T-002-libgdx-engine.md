
# ExecPlan T-002 – Engine foundation

## Goal
30FPS-oriented rendering, lifecycle, diagnostics and clean separation of journey state from rendering.

## Current source
OrthographicCamera/FitViewport, ShapeRenderer, SpriteBatch, procedural world, DemoJourneyFeed and SpeedSmoother.

## Remaining
Add observable FPS and P95 frame-time metrics; test pause/resume, rotate, background/foreground; measure scene memory and long-run frame stability on desktop and Android emulator.

## Acceptance
Runnable desktop and emulator evidence with screenshots/logs. Stable targeted 30 FPS cannot be marked achieved before measurement. No production GPS claims.
