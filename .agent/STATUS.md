# DeX Kids Adventure — verified status

Updated: 2026-10-08 (Asia/Ho_Chi_Minh)
Development branch: `feat/T-001-bootstrap-libgdx`
PR: https://github.com/khuongnd0901/DeX-Kids-Adventure/pull/1 (DRAFT; NOT RELEASE READY)
Latest verified GPS code commit: `8d0e4580c81875f18df7eadb57827cef88e6c075`
Follow-up desktop path fix commit: `d61fff9a9578590b56a4c23e018265124d85dbd4` (CI pending).
All milestones M1–M9 have source checkpoints; acceptance remains **IN_PROGRESS / BLOCKED**, NOT DONE.

## ACTUAL CI evidence (not physical hardware evidence)

| Milestone | Verified code commit | GitHub Actions run | CI |
| --- | --- | --- | --- |
| M0 | 554cb5f | 37795183419 | SUCCESS |
| M1 | 5615a1a5 | 37796489666 | SUCCESS |
| M2 | f2f011f0 | 37796618382 | SUCCESS |
| M3 | a4df7911 | 37796769631 | SUCCESS |
| M4 | f6e936a7 | 37796928794 | SUCCESS |
| M5 | 749049f3 | 37797120923 | SUCCESS |
| M6 | 7b0fbdfa | 37797221624 | SUCCESS |
| M7 | 09c3bffd | 37797413766 | SUCCESS |
| M8 | 819bea48 | 37797528265 | SUCCESS |
| M9 | 00cb48b0 | 37797706168 | SUCCESS |
| M4 Live-GPS follow-up | 8d0e4580 | 37798067824 | SUCCESS |

Each successful run executes Gradle core unit tests, desktop compilation and Android debug APK assembly, then uploads the debug APK. It does **not** run desktop OpenGL or physical Android instrumentation.

Latest verified source APK debug artifact (run 37798067824): id 11559012686, archive 3,463,104 bytes.
GitHub artifact archive sha256: f2bba152bd4e2f27b2f4a604e1392a546e89ca8d4796d994afdff97bc49213fe.
Artifact link: https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37798067824

## Implemented / partially integrated
- M1: bounded FPS/P95 samples, lifecycle and unit tests.
- M2: deterministic bounded chunks, original vector-style bus/capybara prototype and simple character states; final sprites/atlas missing.
- M3: secure GPX playback, deterministic timestamp interpolation, pause, jump filter; 30/60-minute hardware replay missing.
- M4: location adapter, conservative POI event states and source-bound POI model; parent-initiated live GPS mode now routes through smoothed distance. **No actual offline OSM content database**; no real POI claims.
- M5: provenance/age/cooldown director and optional offline-voice guard; narrator not yet wired to verified POI content or child audio.
- M6: local-time sky mood and memory in current session; no geographic scene fidelity proven.
- M7: parent phone UI, consent preferences, bounded session and stop; PIN/audio-only/pause not complete.
- M8: external display routing and signature-protected stop receiver; two-APK signing identity and Samsung DeX policy not verified.
- M9: read-only device capture script + fast-forwarded virtual 60-minute unit test; no on-device 60-minute measurements.

## Blockers and next actions
1. Repository is PUBLIC although original requirements specify PRIVATE. Owner must change visibility via GitHub settings.
2. On Ubuntu host run `./gradlew :core:test :desktop:run` and `./gradlew :desktop:run --args="--gpx test-data/gps/synthetic-urban-short.gpx"`; record screenshot and logs.
3. Install debug APK from CI on Z Fold3; check ParentActivity → explicit phone preview and DeX child window; do not change DeX-Assistant.
4. Collect actual 60-minute DeX/Google Maps/Vietmap coexistence data with `scripts/benchmark-fold3.sh <serial>`. Do not report target numbers as actual.
5. Build the reviewed OSM offline content pack, verified Vietnamese audio pack, character atlas, separate parent UX, IPC signing contract and release signing.
6. Update task-specific evidence and only move ExecPlan to completed when its hardware and functional acceptance gates pass.

No physical Fold3 connection or runtime display screenshot was available in this execution context.


## T-003 source-art & desktop visual verification (2026-10-08)
- Implemented original editable SVG source (4 Capybara frames, bus, wheel, tree) at `art/assets-source/`; deterministic CairoSVG+Pillow raster outputs at `assets/generated/` are build artifacts.
- CI [#37801172670](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37801172670): source + texture atlas + Android APK + core tests SUCCESS.
- Desktop Xvfb/OpenGL screenshot [#37802065247](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37802065247): 1280×720 and 1920×1080 both rendered and captured successfully.
- Final HUD contrast and parallax tree source [ce341a0](https://github.com/khuongnd0901/DeX-Kids-Adventure/commit/ce341a0a6ff0ba1f31341e78f0b10748ae96033b) passed all CI checks [#37802727541](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37802727541), including actual desktop OpenGL screenshots, atlas artifact and debug APK upload.
- Actual generated artifacts [run #37802727541](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37802727541): `dex-kids-opengl-screenshots` (id 11560688934), `dex-kids-sprite-artifacts` (11561538414), `dex-kids-debug-apk` (11560698816).
- Visual inspection: bus, Capybara and parallax tree are visible in both screenshots; render accepts 16:9 resolutions. Remaining: improve biome transitions and visual detail, final artist-approved atlas, audio-synced talking animation, physical Z Fold3 + DeX acceptance.
- T-003 **IN_PROGRESS**, T-002 **IN_PROGRESS** (desktop runtime verified, Android/DeX not verified), M9 remains **BLOCKED** for physical device soak. No DONE flags.
