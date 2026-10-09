# Final simulator validation summary — T-011

2026-10-09, owner Main Android/LibGDX + QA agent. This is a measured local checkpoint,
not physical Samsung acceptance. Current results supersede earlier RUNNING/pending checkpoints.

| Field | Result |
| --- | --- |
| Milestone / Task | T-011, issue #12 / draft PR #1; M0–M9 gate review |
| Source commit | Final tested app source bad299a723c3523a46db41259dd3f0a0b4bad790;60-minute baseline a4e1f34. Final docs snapshot publication pending branch lease verification; never main. |
| Simulator | WSL2 Ubuntu22.04 host tools; Windows ZFold3_API35 emulator-5580, Android15/API35, WHPX/gfxstream/NVIDIA RTX4060 GLES translator;1280×720/1920×1080 tested, original1768×2208/420dpi restored |
| Tests | Core56/56PASS,0FAIL. Mandatory matrix60:40PASS/0FAIL/1BLOCKED/1NOT_RUN/8NOT_IMPLEMENTED/10NOT_VERIFIED. Additional20 execution events:14PASS/3FAIL/2BLOCKED/1NOT_VERIFIED. Combined80rows54PASS/3FAIL/3BLOCKED/1NOT_RUN/8NOT_IMPLEMENTED/11NOT_VERIFIED; model PASS is explicitly scoped. |
| Build | SUCCESS core/desktop classes/Android debug/test APK.18SVG/atlas validation PASS. Installed final APK SHA256 d3ba24bb1e91741f146abf983bfe0ffb39bc679fbc722023cd942b0917730e6c |
| Visual | Actual720p/1080p/density/resume/5reload images reviewed; real Activity recreation before/after, denied-parent and offline-game PNGs reviewed;113timestamped scrolling PNGs and30-second video local |
| Performance | Real child lifetime exactly3600s,08:53:21.796→09:53:21.796 expected expiry. Recorded3571.765s prefix169562frames/47.473FPS/P95 upper43ms; full60-minute histogram NOT_VERIFIED.221PSS samples51.32–52.04MiB, last-first+134KiB.0observed crash/ANR; stable30FPS gate remains open. |
| Bugs | Seven corrected behaviors: GPX terminal speed, rejected-fix freshness, screen dispose ownership, recreation session budget, recreation journey reset, HUD overflow, parent refusal text loss. Android permission pair portability hardened. Desktop native WSLg shutdown SIGSEGV exact cause remains open; softwareGL retry PASS. |
| Regression | Core56PASS; actual baseline recreation/permission failures followed by corrected Android PASS; signed and untrusted STOP probes PASS; final123-second sampled smoke/126-second capture PASS with actual dispose log. Baseline60-minute results do not validate later fixes. |
| Evidence | This directory + ignored local build/simulator-artifacts; exact runs below. Checkpoint CI37874635344 SUCCESS, final-commit CI must be inspected separately. |
| Acceptance | M0–M2 tested subset SIMULATOR_VERIFIED, all remain IN_PROGRESS. M3 core GPX/loss/recovery subset verified, full live/hardware gates open. M4–M8 IN_PROGRESS with explicit missing data/runtime/hardware gates. M9 physical release gate BLOCKED; T-011 IN_PROGRESS for uncovered cases. No DONE/merge. |
| Next task | M3 Android GPX selection/replay + ADB synthetic GPS continuity/denied-live tests; M2 complete visual seam/biome/night review. Keep physical/data/content gates independently blocked. |

## Actual artifacts and access

All raw artifacts are **local only**, not GitHub-downloadable:
`/mnt/d/DeX-Kids-Adventure/build/simulator-artifacts/` or Windows
`D:\DeX-Kids-Adventure\build\simulator-artifacts\`. Open/copy from this workstation.
Source/report links in GitHub are accessible; no raw APK/video/log upload is claimed.

| Group | Local evidence relative to artifact root |
| --- | --- |
| Environment | env-check-20261009T013308Z-2278/; emulator-start.log (not committed; includes ADB public key) |
| Builds/tests/assets | baseline-build.log; location-contract-regression-build.log; permission-runner-final-build.log; final-core-junit/; atlas-validation.txt |
| Bug reproductions | gpx-terminal-before.log/.xml; live-loss-before.log/.xml; lifecycle-before.log/.xml; activity-recreation-20261009T025631Z-19410/; permission-baseline-final.txt |
| Final rendering | functional-20261009T025827Z/ + review-contact-sheet.png; recreation activity-recreation-20261009T025703Z-19479/ |
| IPC | security-probe-20261009T025743Z-19554/ (untrusted); security-probe-20261009T025811Z-19906/ (local debug trusted) |
| Final privacy/offline | privacy-20261009T032118Z/: permission/refusal action, actual PNGs, no-default-network state, offline recreation, denied grants and restored radios |
| Baseline60-minute run | stability-test-20261009T015322Z-7753/:221samples, app-logcat,analysis.json, installed APK/hash, final no-ANR and process-exit evidence; soak-console.txt retains strict sampler exit4 |
| Scrolling visuals | visual-series-20261009T015448Z-7917/:113frames spanning3500seconds, contact-sheet; video-soak-scroll.mp4 + actual start/end UTC files |
| Final smoke | stability-test-20261009T031619Z-22165/:123-second samples/126-second capture,9PSS samples; postfix-final-tagged-logcat.txt final disposed5975frames/172.582s/34.621FPS/P95 upper56ms describes whole short scene, not the sample window |
| Restoration | final-restore.txt; final-restored-package.txt/size.txt/density.txt; final-accessibility-services.txt; QA uninstall results; final-parent-launch.txt |

## Unfinished gates and retained failures

Strict sampler FAIL at the unchanged60-minute session timeout is retained, separately from
PASS expected expiry.113PNG capture completed with a final message, but its wrapper exit143
is retained. Desktop default WSLg native shutdown failed; actual software-GL retry passed.
Host builds, screenshot/video collection interfere with emulator frame cadence. No physical
Fold3 FPS/thermal/battery/audio/navigation or real GPS accuracy is claimed.

An early coordinate-based permission-dialog attempt was followed by unexpected grants; both
FINE/COARSE were immediately revoked. A reinstall preflight also found FINE granted again;
exact input/install-state cause remains unproven. No live child GPS launched (no external display)
and no upload/network-TTS fallback occurred. Corrected harness clicks only the deny node,
checks denied state and revokes any unexpected new grant back to its original denied state.
Failed Unicode/namespace/stale-window/monitor/teardown attempts remain local, never PASS.
Final fine/coarse grants are false; original parent prefs/display/radios restored, denial flags
cleared, QA probe/test APKs removed, app left on ParentActivity. Internal selected-accuracy flag
may remain; no claim of bit-identical permission bookkeeping. Accessibility-service list null
before/after; other repositories/device-owner/production voice untouched.

M2 all-seven-biome visual classification, exhaustive seams, dusk/night palettes and individual
GPU/texture counters remain NOT_VERIFIED. GPX is core/desktop, Android demo soak is DemoJourneyFeed;
ADB location injection and live revoke/recovery on display are NOT_RUN/NOT_VERIFIED. Verified
OSM/provenance/ground truth, Vietnamese audio/subtitles/talking/focus E2E, GeoContext, PIN/audio-only,
full IPC contract, production signing/license/security and physical DeX/Fold3 release tests remain
open. Repo is PUBLIC while M0 requested PRIVATE; owner visibility decision still open.

## Repeat and resume

Use scripts/simulator/README.md: explicit serial, JDK17/Linux SDK, Windows adb.exe path,
pinned Python venv bootstrap. Build/tests, GPX core, rendering/UI, recreation, signing probe,
privacy/offline and actual-duration sampler can run independently. Do not run state-changing
QA during a soak; use the first original settings backup for restoration. Averages/screenshots
never automatically become performance PASS.

Git CLI credential helper points to a missing temporary gh binary. Verified changes are
published with authenticated GitHub Git-data tools and lease-checked non-force updates to the
existing development branch; local/remote commit identities differ but tree equality is checked.
No reset/force/main/merge-PR operation. Existing user CRLF-only working-tree changes preserved.


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
