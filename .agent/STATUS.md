# DeX Kids Adventure — verified status
Updated: 2026-10-08 (Asia/Ho_Chi_Minh)
Development branch: `feat/T-001-bootstrap-libgdx`; PR #1 is DRAFT, no production release.

## Verified code / CI
| Milestone | Last verified evidence | State |
| --- | --- | --- |
| M0 bootstrap | [37795183419](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37795183419) Android+desktop compile | IN_PROGRESS (device runtime and repo visibility) |
| M1 libGDX | [37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) Xvfb OpenGL screenshots at 1280x720 & 1920x1080 | IN_PROGRESS (hardware P95, lifecycle) |
| M2 art/animation | [37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) 18 original SVG sprites, 2048 atlas, 10 sequential GL frames/GIF and Android APK | IN_PROGRESS (art approval, hardware, narration sync) |
| M3 GPX simulation | [37796769631](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37796769631) | IN_PROGRESS |
| M4 GPS/POI scaffolding | [37798067824](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37798067824) | IN_PROGRESS |
| M5 narration scaffolding | [37797120923](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797120923) | IN_PROGRESS |
| M6 dynamic journey | [37797221624](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797221624) | IN_PROGRESS |
| M7 parent UI | [37797413766](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797413766) | IN_PROGRESS |
| M8 DeX+IPC scaffolding | [37797528265](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797528265) | IN_PROGRESS |
| M9 release benchmark | [37797706168](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797706168) virtual soak only | BLOCKED |

**M2 latest verified source SHA:** `e63e21cc2374a95d36cdba837792382534daac3c`. A HUD accuracy wording fix `b9661a0c335976af05879b87e0af85abbe44e32a` is not yet included in these CI results. The original 720p/1080p screenshot artifact and animation GIF were actually rendered by LibGDX in Xvfb/Mesa software GL; they are not AI mockups.
Artifact links: [actions run 37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673), screenshots/GIF id **11563302286**, generated PNG+TextureAtlas id **11562987678**, Android debug APK id **11563282420**.
The SVG sources are in `art/assets-source/`, generated raster content in `assets/generated/` (build outputs).

## What M2 implements
- Capybara idle, blink, wave, talk-open frame, sleep, surprised; time/state-driven model and explicit narration-active gate. Does not pretend to narrate without reviewed content.
- Original yellow bus, lighting highlights, independent rolling wheels calculated from absolute traveled meters; bounded suspension/tilt animation.
- Original cloud/hills, home/building, bridge, bush/flower, street lamp, tree and glow assets; layered 2D parallax from stable journey distance.
- Biome scenery grouped into deterministic four-chunk districts to reduce abrupt geographical scene changes. Biomes are fictional until ground truth verified.
- Unit tests for animation states, vehicle movement, chunk continuity and district stability; source-atlas validator; desktop OpenGL captures and actual GIF preview.

## Unverified and release blockers
- No Android emulator, physical Z Fold3 or Samsung DeX display has run in this execution context.
- No measured Fold3 30 FPS/P95 frame latency, 60-minute actual soak, thermal, PSS or battery evidence. Render FPS shown in screenshots belongs to CI software OpenGL only.
- No production-approved assets, voice-synced talking frame or curated real-world POI/narration pack.
- No complete DeX-Assistant cross-app signing/command and navigation audio coexistence acceptance. Existing Assistant repos unchanged.
- No signed production APK, license release sign-off or validated upgrade path.
- Repo is currently **PUBLIC**; master requirements requested PRIVATE. Owner action required.

**No task/milestone is DONE.** Next gate: Z Fold3/DeX runtime smoke → manual art visual approval → narration/audio synchronization → actual 60-minute benchmark → release readiness review.

## M2 latest art+animation evidence — 2026-10-08
- Source commit: `e63e21cc2374a95d36cdba837792382534daac3c`.
- GitHub Actions [run #37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673): **SUCCESS** (core tests, 18-region sprite/alpha validation, Android debug APK, two software OpenGL desktop runs, 10-frame GIF preview).
- Artifacts: screenshots/GIF 11563302286, APK 11563282420, sprites 11562987678.
- T-003/M2 remains **IN_PROGRESS**. Final art signoff, verified narration synchronization, seam checks and physical Z Fold3/DeX 60-minute test: **NOT VERIFIED**.
- Next hardware step: install latest CI debug APK and capture actual DeX landscape screenshots, FPS/P95, memory and animation-state behavior; do not mark DONE on CI alone.

## Local simulator checkpoint — 2026-10-09 08:53 Asia/Saigon (latest)
Task T-011 IN_PROGRESS; source commits `923f297` (GPX fix/tests + metrics), `a4e1f34` (session/final metrics logs).
- WSL2 Ubuntu22.04; build JDK17.0.20.1, Gradle8.11.1, Linux SDK35.
- Windows emulator37.1.11, WHPX, NVIDIA RTX4060 GLES translator; AVD `ZFold3_API35`,
  serial `emulator-5580`, Android15/API35. Generic foldable emulator, NOT Samsung Fold3/DeX.
- Actual local core tests: 45 PASS / 0 FAIL; desktop classes and Android debug APK build SUCCESS.
- Debug APK installed and game actually rendered. M1 UI/lifecycle automation completed;
  screenshots pending consolidated visual review. Atlas validator PASS, 123-second real smoke capture completed.
- GPX terminal-speed bug reproduced FAIL then fixed; regression PASS. No milestone DONE.
- Unauthorized IPC probe with distinct UID/certificate PASS; direct child shell launch rejected;
  external-display absent causes no phone fallback. Earlier harness failures retained, not hidden.
- Real 3600-second soak currently RUNNING: `build/simulator-artifacts/stability-test-20261009T015322Z-7753/`;
  unified exec session 86854. Do not reinstall/resize/stop app during collection.
- Synthetic parent prefs (60-minute existing limit, quiet ON/TTS OFF), initial originals absent:
  restore after collection with `ADB_BIN=/mnt/d/Android/Sdk/platform-tools/adb.exe python3 scripts/simulator/restore-soak.py --serial emulator-5580 --setup build/simulator-artifacts/soak-setup-20261009T015127Z`.
- Physical Fold3/DeX, verified OSM/content/audio and release acceptance remain blocked/open.
- New scripts/evidence pending commit; existing 140-file CRLF-only user changes preserved.
  `gradlew` locally normalized for WSL execution. No other repository accessed.

### Checkpoint 2026-10-09 09:26
- Actual56 core tests PASS and Android debug/instrumentation APK build SUCCESS; resource-dispose
  and accepted-only GPS freshness fixes verified, local source HEAD f8316fc.
- Git CLI push blocked by missing temporary credential helper (store fallback also unavailable).
  Published verified core checkpoint through GitHub connector on the existing development branch:
  remote ea8597e952e53fa9fc55f3cde08c61125c97a89f, tree d45a170478378521c86e1a5ad9830b49e0ca3413
  exactly matches local f8316fcd00396c9843e372238cade0079a7fcd66 tree. No forced update/main/PR merge.
- Soak elapsed>30minutes with same PID5991; mean~47FPS, P95 upper43ms. Final analysis pending.
- Persistent Python QA venv installed at /home/khuongnd/.local/share/dexkids-qa-venv;
  bootstrap-python.sh + tools/requirements-art.txt reproduce CI-pinned versions.
- Android absolute deadline and pure feed retention source compiled; actual recreation test pending.
  Instruments use FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES; initial emulator enabled-services list null.
- Remaining new scripts/docs/Android edits are intentionally uncommitted pending runtime evidence.


### Final checkpoint 2026-10-09 10:25 — resume here
- App source bad299a;56/56corePASS, Android/debug/test buildsSUCCESS,18SVG/atlasPASS.
- Real emulator baseline a4e1f34 child lifetime exactly3600seconds; expected60-minute expiry.
  Strict sampler exit4 retained. Recorded3571.765s prefix47.473FPS/P95 upper43ms,221PSS samples
  51.32–52.04MiB/end+134KiB;0observed crash/ANR. Full histogram/stable30FPS NOT_VERIFIED.
- Actual resize/resume/5reloads, distinct recreation preserving deadline/feed/distance,
  different/same-debug-signature STOP, denied-location explanation and truly offline preview/
  recreation PASS. Corrected baseline permission harness reproduced app refusal-message FAIL;
  final sourcePASS. Final sampled123s smoke captured126s and actual GL dispose logPASS.
- Seven behavior fixes committed. Desktop default WSLg SIGSEGV unresolved; llvmpipe retryPASS.
  Early QA permission grant incident revoked, retained honestly; final harness no grant path.
- Original first parent-pref backup restored, wm1768x2208/density420, radios restored, FINE/COARSE
  denied, USER_SET/USER_FIXED cleared; internal selected-accuracy flag may remain. Probe/test APKs
  removed; final app on ParentActivity. Accessibility listnull before/after, no other repos touched.
- Evidence `.agent/evidence/simulator/final-test-summary.md` / `test-matrix.md`; raw artifacts
  localonly in ignored `build/simulator-artifacts` (also accessible D:\DeX-Kids-Adventure\build).
- Mandatory60cases40PASS/0FAIL/1BLOCKED/1NOT_RUN/8NOT_IMPLEMENTED/10NOT_VERIFIED; historical
  failed sampler/nativeGL/QAgrant events separately retained. No milestone markedDONE.
- M0–M2 tested subset SIMULATOR_VERIFIED, all unfinished; M3 core subset verified; M4–M8 missing
  verified content/runtime/physical gates remain open; M9 physical60-minute Fold3/DeXBLOCKED.
- Publish source/scripts/reports on existing feat/T-001-bootstrap-libgdx through lease-checked
  GitHub API (CLI helper missing), verify exact content tree; keep draftPR#1 unmerged.
- Next: M3 Android GPX/ADB GPS integration and denied/live continuity; M2 full visual seam/night
  review. No new real POI/narration without verified pack. Existing user CRLF-only changes kept;
  no outstanding QA process/soak. Remaining small QA docs staged/committed in final snapshot.
