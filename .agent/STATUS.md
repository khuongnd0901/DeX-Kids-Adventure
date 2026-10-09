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

## T-003 Capybara face alignment, companions & vehicles — 2026-10-09
- Edited six original Capybara SVG expressions to align eye line, muzzle, nose, philtrum, mouth and blush. Face landmark checks are executed in GitHub Actions.
- Added 5 fictional animal-companion sprites (rabbit, fox, panda, cat, penguin) and 6 traffic/transport sprites (car, taxi, truck, minibus, scooter, bicycle) to the **same** game atlas: 29 regions total.
- `SceneryCast` selects deterministic props by biome/seed; companions do not appear in rivers/on bridges, vehicles are not presented as real live GPS traffic. Shifted starting roadside companion away from the bus foreground occlusion.
- [Source CI #37879894270](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37879894270): **SUCCESS**, includes geometry test, Java unit tests, atlas alpha/regions, 720p/1080p OpenGL and generated visual cast sheet. Screenshot artifact id 11594260235; sprite atlas id 11593323780; debug APK id 11593184361.
- Penguin anatomy update `ec459c6` passed [CI #37880193189](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37880193189). First companion visibility fix `d6cd0dc` passed [CI #37880239651](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37880239651), with unit tests, atlas, actual 1920×1080 OpenGL screenshot showing a rabbit on the sidewalk outside the bus, and Android debug APK output. Artifact IDs: screenshots/cast GIF **11593769502**, atlas **11593724708**, APK **11594465217**.
- [Cast and art inventory](https://github.com/khuongnd0901/DeX-Kids-Adventure/blob/feat/T-001-bootstrap-libgdx/art/ASSET_CAST.md); artwork is **draft**, final design signoff and simulator/Fold3 acceptance not done.
- M2/T-003 stays **IN_PROGRESS**, M9 remains **BLOCKED**. Existing DeX-Assistant repos are untouched.


## Concurrent artwork integration checkpoint — 2026-10-09

Preserved remote artwork history through 1a3a7d9 in isolated worktree
`/tmp/dexkids-qa-integration-20261009`, branch `feat/T-011-simulator-validation-integration`.
Original worktree's pre-existing CRLF changes were not discarded. Merge source
`d5ee691cb81ffe6594bedaba96780891cfa1c721` compiles core/desktop/Android/test APK.
Core now **58 tests, 0 failures/errors** (two additional scenery-cast tests).
29-region atlas metadata/transparency validator PASS; face validator PASS only for
its explicit muzzle/nose/cheek/nonempty-mouth assertions, not full anatomical or
eye alignment. New companions/traffic are now in renderer/atlas; fictional props,
not GPS detections. Asset inventory was updated upstream; final art/license approval remains open.

Real emulator install SHA256 `5712f9d737558d674853ac57fcc7db3b233d08ceb98d567f1556daba363664d0`.
Actual Activity recreation PASS in `activity-recreation-20261009T034213Z-25024`: deadline
9678316 unchanged, same_feed=true, distance20.627→49.927m; actual PNGs reviewed.
Rabbit/traffic are visible; a roadside rabbit is partially occluded behind the bus
at one sampled position, so exhaustive cast visibility/art approval is NOT_VERIFIED.
Earlier face-only integration `bb9fdee` recreation/resize/reloads completed separately
(`activity-recreation-20261009T034005Z-24412`, `functional-20261009T034020Z`).
Final cast UI run and short smoke are recorded below when completed.
Raw artifacts remain local at `/mnt/d/DeX-Kids-Adventure/build/simulator-artifacts/`.
The 60-minute run remains source a4e1f34, **not validation of new artwork**.
An integration build attempted before resolving a merge conflict failed; resolved
source rebuilt successfully (`integrated-cast-final-build.log`).
Publication of final QA snapshot was delayed by concurrent remote changes;
GitHub lease/equal-tree verification is required before claiming final publication.

Latest cast UI run `functional-20261009T034229Z` completed:8 PASS UI rows and
1 inconclusive shell-broadcast NOT_VERIFIED. Actual720p/1080p/density160/240,
HOME/resume and five reloads executed; sampled resize/reload PNGs reviewed,
Capybara/bus/rabbit/traffic render intact. HUD cadence during disruptive UI tests
was low (sampled5.5–17.3FPS); this is not a stable30FPS acceptance result.
Existing matrix totals above describe baseline cases, not additional duplicate
runs. Separate final artwork smoke is running in `stability-test-20261009T034450Z-25275`;
its result must be appended after real completion.
GitHub milestone issues #2–#12 received honest checkpoint comments; all remain open.


### Final integrated-cast smoke completed
`stability-test-20261009T034450Z-25275`:123 real wall seconds,9 resource samples,
PID18912 unchanged,0 PID fatal signatures; start/end screenshots manually reviewed
with intact scene. Smoke launch/render/no-restart PASS. PSS57100–61206KiB,
last-first −4106KiB. Last logged cumulative scene prefix2979frames/150.264s
=19.825FPS/P95 upper86ms; prefix begins before sampler, so this is not120-second
window FPS. **30FPS threshold FAIL for this observed prefix**, full sustained
performance and system-wide ANR completeness NOT_VERIFIED. No hardware acceptance.
Do not attribute low FPS to new sprites without a controlled equal-host A/B.
Next task includes controlled old/new-art profiling, complete visual review and
Android GPX selection; source architecture is unchanged by this evidence update.
Original parent fixture/display settings restored; installed app remains integrated
cast APK; QA instrumentation removed. Permission/accessibility state checked separately.

Final QA publication uses a content-tree-verified API commit based on a7f8fb0;
remote lease rejects stale heads, with no force/main push or PR merge. CLI credential
helper is unavailable. If publication fails, resume from the isolated integration
worktree; original worktree retains existing uncommitted line-ending changes.


## Published receipt — 2026-10-09 10:50 ICT
Verified development branch publication: `cdb9a178d5b8140ecb7a0a6b27a35028e3e5d0d6`,
content tree `c80a19e5db87b5ec6e34dd027c8ec16e79109028` exactly equals local
integration commit6195329 tree. Commit parent a7f8fb0 preserves concurrent art
history. Never forced, never pushed main, PR#1 remains draft/unmerged.
CI run37881024109 is IN_PROGRESS at receipt, not PASS.
Milestone issues#2–#12 updated with execution/gate comments; none closed/DONE.
Actual restored fine/coarse=false, accessibility=null; no ANR since boot in
`integrated-cast-lastanr.txt`; own exit history only expected install/instrument/
force-stop events observed.

Resume source work in `/tmp/dexkids-qa-integration-20261009` on
`feat/T-011-simulator-validation-integration` (clean committed integration).
Original `/mnt/d/DeX-Kids-Adventure` retains pre-existing CRLF changes and an older
source branch history; its STATUS/evidence checkpoint is updated but **do not
build original source and call it the published integrated source**. Original
raw artifacts remain at `build/simulator-artifacts/`, shared by evidence paths.
Next: controlled equal-host old/new-art FPS profile; M3 Android GPX selection/
ADB synthetic GPS continuity; remaining visual coverage. Physical Fold3/DeX,
verified POI/narration/content, production signing/release gates stay open.
