# Regression checkpoint — T-011

- GPX terminal speed: actual failing JUnit1/1 retained `gpx-terminal-before.log/.xml`,
  fixed reset at route completion; full suites subsequently PASS.
- Live GPS freshness: actual11-second core loss regression failed after a rejected jump;
  `live-loss-before.log/.xml`. Accepted-only freshness + injectable core clock fix;
  deterministic loss/recovery, duplicate/out-of-order/accuracy matrix PASS in expanded suite.
- KidsGame ownership: actual test failed with screen disposal count0; bytecode inspection
  confirms LibGDX1.14.2 Game.dispose only hides. Explicit screen disposal/regression PASS.
- Latest expanded local core suite56 tests/0fail and Android debug/instrumentation APK
  build SUCCESS: `build/simulator-artifacts/expanded-regression-build.log`.
- New numeric cadence tests initially treated IEEE -0.0 as unequal to0.0. Corrected physical
  zero comparisons with1e-6 tolerance; this was a test assertion issue, not a product bug.
- Two overlapping earlier Gradle invocations conflicted writing XML. Those reports discarded;
  later builds sequential. Early UI scripts had task-stack/ADB-output assumptions; retained
  failed runs in local artifacts, corrected using actual Android task behavior and probe APK.
- Desktop WSLg native runtime rendered/captured, then JVM SIGSEGV after dispose; command FAILED,
  `desktop-fixed.log` and root `hs_err_pid11263.log`. This is not an Android emulator crash.
  Software-GL retry pending; no exact native root cause or PASS inferred.
- Session absolute deadline restoration and actual Android GL resource lifecycle runtime
  regression pending until60-minute baseline soak finishes. Source fixes cannot inherit
  baseline soak acceptance. Current emulator continues APK source a4e1f34 unchanged.


## Completed follow-up
Actual software-GL desktop retry SUCCESS (desktop-software.log); first native WSLg crash remains
an unresolved host-backend issue, not an Android crash. Actual baseline Android recreation
FAILED (missing deadline, changed feed), then the same assertions/screenshots PASS after the
source fix9c25ae3. See m1-rendering.md for exact runs. Source build and56-core regression PASS;
full60-minute baseline sourcea4e1f34 is separate from shorter post-fix Android regression.


## Final regression result
Source bad299a.56 core tests/0fail retained in final-core-junit/, final Android/test APK build
SUCCESS (location-contract-regression-build.log, permission-runner-final-build.log).
Actual baseline corrected permission test FAIL followed by final denial/offline/recreation PASS
in privacy-20261009T032118Z/; earlier namespace/dialog/teardown harness failures and grant incident retained.
Final post-fix smoke sampled123seconds, actual capture126seconds, samePID16077/no fatal signatures,
9PSS samples51753–52201KiB. After BACK, actual GL screen disposal logged frames5975,
render_seconds172.582, avgFPS34.621, P95 upper56ms (postfix-final-tagged-logcat.txt).
These short whole-scene metrics are not the60-minute baseline or stable30FPS proof.


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
