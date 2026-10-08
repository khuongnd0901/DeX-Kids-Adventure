
# DeX Kids Adventure

An offline-first, GPS-driven 2D educational adventure for children on Samsung DeX.

**Status: EARLY DEVELOPMENT / DEMO ONLY.** This source currently implements a running simulation, not location-aware narration. All real GPS/POI/DeX acceptance requires later milestones and physical-device validation.

## Product

- A capybara explorer in a yellow bus travels through a stylized, infinite 2D world.
- World scenes eventually respond to verified GPS and POIs; unreliable location must not produce factual claims.
- Child display is passive; parent control is separate. No ads, cloud location upload or voice collection by default.
- The app remains independent of DeX-Assistant until a separately audited IPC integration.

## Modules

- core/: libGDX world simulation and original vector-style cartoon rendering.
- desktop/: LWJGL3 launcher to test graphics without Android hardware.
- android/: Android launcher for API 30+, target/compile API 35.
- .agent/: durable plans, acceptance criteria, status and evidence.
- docs/: specifications and test/release gates.

## Quick start

Requires JDK 17+, Android SDK Platform 35 for Android builds, and network access to Maven Central and Google Maven.

Desktop: ./gradlew :desktop:run
Core tests: ./gradlew :core:test
Android debug APK: ./gradlew :android:assembleDebug

This initial implementation defaults to a DEMO JourneyFeed; on-screen banner explicitly states "NO REAL GPS / POI". A desktop run is not evidence of compatibility with Samsung DeX. Validate on SM-F926B before claiming compatibility.

Read AGENTS.md and .agent/STATUS.md before continuing development.

## Roadmap

M0 bootstrap -> M1 foundation -> M2 characters and world -> M3 GPS replay -> M4 real location/POI -> M5 narration -> M6 dynamic experience -> M7 parental controls -> M8 DeX+Assistant IPC -> M9 optimization/release.

License: original code/art attribution requires policy approval before public distribution. Third-party dependencies retain their respective licenses.
