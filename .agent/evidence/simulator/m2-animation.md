# M2 animation/world/art

Core model checks actually passed in56-test suite: defaultIDLE, blink30/60FPS timing,
wave/rolling/sleep/resume, explicit model narration flag/open-close frames, surprise priority,
zero/slow delta, stationary wheel/bob, same-distance wheel angles and bounded pitch.
Synthetic GPX replay exercised >100chunks, all7 enum biomes, four-chunk districts,
bounded windows and deterministic reset. These are pure model tests, not GPU/hardware soak.
Exact test names are mapped in source-test-map.md and current core/src/test.

18 original SVG regions generated and actual PNG/TextureAtlas alpha validator PASS.
Real Android render images show yellow bus/Capybara/scenery at1280×720/1920×1080;
real scrolling screenshot series currently being captured under build/simulator-artifacts/
visual-series-20261009T015448Z-7917. Review of all biome seams/blink/sleep frames pending;
no narration-audio synchronization exists in Android runtime. Talking model PASS does not
prove spoken audio or permit artificial talking in a child session.

Actual bug: KidsGame inherited Game.dispose, which hides but does not dispose Screen.
JUnit failed disposal count0; ownership fix + full regression PASS; desktop actual dispose
log/clean software-GL shutdown PASS. Android actual recreation/post-fix runtime pending.

M2 remains IN_PROGRESS: production art/license approval, actual narration sync, full seam
review and physical Fold3/DeX stability not accepted.


## Actual Android follow-up
Post-fix Activity recreation PASS with visible before/after PNGs reviewed (m1-rendering.md).
HUD background now contains metrics in real Android1080p captures. Actual113PNG series spanning
3500seconds plus30-second screenrecord confirm real scenery scrolling and visible bus/Capybara;
contact sheet inspected across residential/city/tree/river/bridge scenes. Full seven-biome visual
classification, complete seam inspection, individual GPU allocation count and actual talking
synchronized to audio remain NOT_VERIFIED/NOT_IMPLEMENTED. No fake narration signal injected
into a real child session. Actual60-minute baseline PSS stays51.32–52.04MiB; source fixes were
installed afterward, so baseline metrics do not inherit post-fix acceptance.

Final source bad299a follow-up completed: actual permission/offline/recreation PASS `privacy-20261009T032118Z/`; final post-fix smoke capture126seconds and GL disposal log PASS. See final-test-summary.md for current gates and restoration.


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
