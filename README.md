# DeX Kids Adventure

Offline-first, GPS-aware 2D educational companion prototype for children aged 2–6 on Samsung DeX.

**Status: DEVELOPMENT / DRAFT PR #1, NOT RELEASE READY.** Source for M1–M9 milestones has partial implementations and passing CI builds, but real device acceptance, finished cartoon artwork and verified Vietnamese geographic narration remain open.

- Android native Kotlin parent UI; Java17 LibGDX shared core; desktop LWJGL3 runner.
- Sprite-based yellow school bus and Capybara (original editable SVG artwork, 18 packed regions), wheel/suspension and actor animations; parallax city/park/river/bridge scenery. Biomes remain fictional demo contexts, not real POI claims.
- Deterministic bounded world chunks, 4-chunk districts and local day/night palette. The artwork is still first-pass and pending final art approval.
- Android single-display **GPX Replay**: select a document with SAF; bounded XML import on a worker thread, timeline pause/resume/restart overlay; no global storage permission or persistent GPS history. GPX replay with deterministic timestamp interpolation. Real GPS distance mode is opt-in from parent controls and only starts after Android permission approval; it never asserts any POI name because no reviewed offline dataset is bundled.
- Provenance-gated story engine and offline Vietnamese TTS adapter source are present, but not connected to a reviewed content pack. No children’s audio is collected or sent to any server.
- The damaged-screen Fold3 uses **single-display DeX**: parent dashboard and game both run on the display from which the app was opened; in-game long-press/F10 opens parent controls, no phone-screen fallback or second-screen controls. Physical DeX validation pending.
- DeX Assistant repositories are untouched; draft signature-permission STOP IPC requires signing-contract audit before cross-package use.

## Verify locally (Ubuntu 24.04)

Prerequisites: JDK17, Android SDK35 with build tools 35.0.0, Linux desktop OpenGL.

```bash
./gradlew :core:test :desktop:classes :android:assembleDebug
./gradlew :desktop:run
./gradlew :desktop:run --args="--gpx test-data/gps/synthetic-urban-short.gpx"
```

Phone/external display hardware checks:
```bash
./scripts/device-smoke.sh <adb-serial>
./scripts/benchmark-fold3.sh <adb-serial>
```

These scripts do not grant permissions, modify system settings, install software or spoof locations. The benchmark script collects read-only samples from an already running game; do not treat virtual simulation tests as physical FPS evidence.

## Game animation preview
[Latest verified GitHub Actions](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) includes a `dex-kids-opengl-screenshots` ZIP artifact with full-HD PNG frames and `capybara-bus-animation-preview.gif`. These are captured from actual desktop software OpenGL, not an Android performance measurement.
Editable SVG sources: `art/assets-source/`. Generated assets: `assets/generated/`.

## Project management

Read `AGENTS.md`, `.agent/STATUS.md`, `.agent/TASKS.md`, and the current milestone's ExecPlan.
Original requirements are preserved in `docs/MASTER_EXECUTION_PROMPT.md`.
Actual test evidence lives under `.agent/evidence/`.

## Privacy and licensing

No cloud GPS upload, child voice collection or advertisements are implemented. Third-party content is not bundled. Attribution and copyright clearance are required for production. The repository currently exposes code publicly; this differs from the original request to use a private repository.

## M3 GPX mode

From the same-screen parent dashboard choose **Choose GPX file and start REPLAY**, select a local `.gpx` via the Android document picker, then use the in-game **Pause / Resume / Restart** controls. Parent controls remain F10/long-press. Input limit: 4 MiB / 20,000 track points. All GPX locations are synthetic or user-selected and do not imply verified real POI data. See [GPX Replay guide](docs/GPX_REPLAY_ANDROID.md). Physical DeX testing is deferred.

## M4 offline POI (candidate/review workflow)

The game now contains an offline proximity detector and Unicode on-screen notice, but **ships with 0 manually approved POI names**; it displays an explicit no-reviewed-data message rather than inventing a city/bridge/park. `tools/import_osm_pois.py` converts a previously downloaded Overpass JSON extract to **UNREVIEWED** candidates and provenance receipt; each place must be manually checked in OSM and recorded in `assets/poi/review-ledger.json` before `reviewed.tsv` may be populated. Build validation checks source/URL and ODbL attribution. See [data provenance and review instructions](docs/POI_DATA_PROVENANCE.md).

GPX notices are always labeled simulated, LIVE GPS notices are estimates; heading/road topology has not been map matched and there are no narrated 'we passed bridge X' claims. No network lookup or GPS upload is performed at runtime. Physical DeX verification is deferred.


## M5 HCMC narrated sample (explicit preview)
The **Start HCMC sample journey (preview)** dashboard button opens a synthetic 16-point central-HCMC GPX with four OSM-feature reference POIs and short sourced Vietnamese narration drafts. Each place/coordinate has a visible **sample / not human-approved** disclaimer; the shipped human-approved POI and narration catalogs remain empty.

Offline audio is OFF by default; in parent dashboard enable **Allow offline speech** and switch **Quiet mode: OFF** before opening the sample. The app requires a locally installed Vietnamese TTS voice; without it, captions remain available. The Capybara TALKING frame activates only on real TTS utterance start and exits on done/error/focus loss/parent menu. No network TTS, no audio capture and no upload. See [HCMC sample and citations](docs/M5_HCMC_SAMPLE.md). Hardware/DeX audio-focus integration remains unverified.


## M6 — POI-inspired dynamic scenery (illustrative)
The game transitions selected **future** chunks toward park/tree, river, bridge or city artwork when a source-backed offline M4 POI notice has sufficient accuracy and proximity. These are **illustrative themes**, not road/travel assertions. Sprite props crossfade in/out over 2.5 seconds, expire after a short time/distance and reset on GPX restart. Offline HCMC preview explicitly labels its synthetic route and unapproved example coordinates; without a reviewed data pack, the default fictional procedural scenery remains unchanged. See [M6 design and test matrix](docs/M6_DYNAMIC_WORLD.md).
