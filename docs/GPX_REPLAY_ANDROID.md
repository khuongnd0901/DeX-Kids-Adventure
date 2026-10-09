# M3 Android GPX Replay — DeX Kids Adventure

## Use on one display
1. On the single external DeX screen (or single virtual Android display), open **DeX Kids Adventure** parent Dashboard.
2. Select **Choose GPX file and start REPLAY**.
3. Android system document picker opens on the **same display**; select a locally available GPX XML file with timestamps in `trkpt/time` (2 or more trackpoints, strictly increasing).
4. The game initializes immediately with stationary/loading state while the GPX is validated on a worker thread.
5. Use the lower-left **Pause**, **Resume** and **Restart** buttons. The status shows GPX loaded/playing/paused/finished, elapsed/total seconds and simulated traveled kilometers. Animation/wheels and world scrolling use replay-derived journey distance and speed.
6. **Parents · hold** / F10 opens same-display pause/end dialog. This freezes journey time even if the GPX player is otherwise playing. Session expiry still runs on Android monotonic wall clock.

The game **never** claims that its GPX-replay POIs, roads or backdrop are actual observed traffic/geography. The GPX route is a simulation source, not a route map or verified OSM data.

## Security / privacy
- Android SAF `ACTION_OPEN_DOCUMENT` and temporary `content:` read URI; app does **not** request all-files storage access.
- No `takePersistableUriPermission`, no database or cache copy of route data, no upload to server.
- `BoundedGpxInputStream` enforces max 4 MiB read; GPX parser caps trackpoints at 20K and rejects missing/out-of-order timestamps, invalid coordinates, unsafe XML entities and malformed tracks. No XML external entity processing.
- Invalid file or revoked URI permission returns a parent-visible error; no silent demo/live GPS substitution.
- Android provider picker may list cloud document providers; choose a local file for true offline operation. App itself doesn't upload the GPX.

## Lifecycle
`KidsActivity` is a LibGDX `AndroidApplication`; its rendering backend MUST initialize inside `onCreate` before lifecycle `onResume`. `DeferredGpxJourneyFeed` starts stationary while a worker parses GPX. Once valid, the UI posts installation to LibGDX render thread (`Gdx.app.postRunnable`). UI control commands are posted to that same render thread; status fields are observed safely.

On same-process Activity recreation, `onRetainNonConfigurationInstance` retains the journey feed and its progress, and the session deadline stays absolute. The full process-death route restore, Android cloud-backed provider offline constraints, and comprehensive SAF picker runtime tests are NOT_IMPLEMENTED/NOT_VERIFIED. No invented test passes.

## Test matrix
| ID | Acceptance | Method |
| --- | --- | --- |
| M3-ANDROID-01 | GPX mode visible in same-display dashboard | Static contract; emulator UI E2E pending |
| M3-ANDROID-02 | SAF selection passes temporary content read permission | Source review; emulator UI E2E pending |
| M3-ANDROID-03 | Invalid XML/XXE/large file fail closed | JUnit parser and size-limit tests; Android error dialog runtime pending |
| M3-ANDROID-04 | Valid GPX progresses with correct deterministic timeline | JUnit GpxReplayFeedTest / GpxReplayControlsTest |
| M3-ANDROID-05 | Pause stops GPX timeline; Resume continues; Restart resets | JUnit GpxReplayControlsTest; in-game controls runtime pending |
| M3-ANDROID-06 | At end of track, speed is zero, finished flag true | JUnit GPX terminal speed regression |
| M3-ANDROID-07 | During async load, LibGDX lifecycle initialized and render displays stationary | Source initialization contract; emulator runtime pending |
| M3-ANDROID-08 | Recreation retains route progress and session deadline | Source retention; emulator instrumentation replay mode pending |
| M3-ANDROID-09 | Menu F10 pauses player without renewing session budget | JourneyRenderPauseTest, source review; UI E2E pending |
| M3-ANDROID-10 | No dependence on broken phone screen | Source contract; physical DeX acceptance deferred |

No physical-device test or sustained 30 FPS performance test is claimed in this M3 sprint.

## Build/test
```bash
./gradlew :core:test :android:assembleDebug :android:assembleDebugAndroidTest
python3 tools/test_single_display_contract.py
```
Actual Gradle wrapper/global Gradle must follow repository's documented setup. CI also runs Xvfb software OpenGL screenshots, but Android SAF file-picker cannot be verified from desktop GL smoke.

## Next milestone
M4: curated offline real OSM/POI pack for sample HCMC GPX route, with provenance and verified spatial events. M5 narrator must only speak from reviewed facts; separate firmware/DeX hardware gate remains open.
