
# Architecture

Real LocationProvider or ReplayProvider -> JourneyTracker -> GeoContextEngine -> WorldDirector -> ProceduralWorldGenerator -> LibGDX AdventureScreen.

Verified POIRepository -> EventDetector -> TourGuideDirector -> NarrationScheduler -> AudioFocus/Narrator + subtitle animation.

Android APIs (Location, Room, TTS, DisplayManager, IPC) reside in Android adapters. Shared :core never takes Android Context.

Current running slice: DemoJourneyFeed -> ProceduralWorldGenerator -> WorldPainter. It is visual simulation and does NOT connect real geography.
Separate repositories DeX-Assistant and Dex-Assistant-UI are untouched; future IPC uses explicit components, caller authorization and PR review.

## Local QA lifecycle decisions — T-011, 2026-10-09
KidsGame explicitly owns/disposes its screen (LibGDX Game.dispose only hides it).
Live GPS freshness advances only for accepted fixes; an injected core clock enables deterministic
loss/recovery tests, with System.currentTimeMillis default behavior.
Android child recreation retains a pure journey feed only in non-configuration memory and
preserves an absolute elapsedRealtime deadline in saved state; no GPS history written to disk.
Framework instrumentation checks real recreation and does not suppress Accessibility services.
Whole-render-run bounded telemetry logs frame cadence/P95 histogram without GPS/audio data.
These changes do not provide process-death GPS persistence, verified POI or voice integration.
