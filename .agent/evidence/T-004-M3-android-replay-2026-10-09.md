# T-004 Android GPX Replay source and test evidence
Date: 2026-10-09

Source commits:
- `d20f94d4` bounds input, point count and adds GPX playback tests.
- `15b06ffc` Android SAF picker and in-game control overlay.
- `c7183ec1` corrects AndroidApplication startup lifecycle using stationary DeferredGpxJourneyFeed while parsing asynchronously.

CI [#37884079192](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37884079192): PASS for first core tests, desktop compile, Android build and GL smoke. Latest source `c7183ec1` [CI #37884357999](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37884357999) **SUCCESS**: Gradle core JUnit, Android debug + AndroidTest APK, desktop classes, single-display contract, SVG+PNG atlas and 720p/1080p OpenGL screenshots/GIF. Desktop GPX fixture smoke is actual Xvfb render, not Android SAF UI test.
Security: GPX file has a 4 MiB bound and 20K points; no GPS uploads/history persistence and no real-POI statements.
Runtime: Android picker, replay activity recreation, controls and broken-screen DeX not actually executed in this tool session. Device testing explicitly deferred by owner.
Task state: T-004 M3 IN_PROGRESS.
