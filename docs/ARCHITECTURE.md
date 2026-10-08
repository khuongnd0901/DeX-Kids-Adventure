
# Architecture

Real LocationProvider or ReplayProvider -> JourneyTracker -> GeoContextEngine -> WorldDirector -> ProceduralWorldGenerator -> LibGDX AdventureScreen.

Verified POIRepository -> EventDetector -> TourGuideDirector -> NarrationScheduler -> AudioFocus/Narrator + subtitle animation.

Android APIs (Location, Room, TTS, DisplayManager, IPC) reside in Android adapters. Shared :core never takes Android Context.

Current running slice: DemoJourneyFeed -> ProceduralWorldGenerator -> WorldPainter. It is visual simulation and does NOT connect real geography.
Separate repositories DeX-Assistant and Dex-Assistant-UI are untouched; future IPC uses explicit components, caller authorization and PR review.
