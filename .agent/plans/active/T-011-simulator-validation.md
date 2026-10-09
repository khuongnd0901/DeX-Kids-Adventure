# T-011 — Local simulator validation
Owner: Main agent (integration), source matrix and isolated simulator scripts delegated.
Dependencies: T-001–T-010 source; explicit emulator serial; Android SDK/JDK17.
State: IN_PROGRESS. Date: 2026-10-09.

## Scope and gates
Audit actual local WSL/Windows environment; build existing source, install/launch emulator,
execute unit tests and actual visual/lifecycle/security checks; real 120/600/1800/3600-second
runs where possible. Record test IDs, actual outcomes and artifacts. Never infer physical
DeX/Fold3 acceptance from emulator. Source-confirmed missing features remain NOT_IMPLEMENTED.

## Execution
1. Preserve pre-existing CRLF-only working-tree changes; stage only scoped new work.
2. Use local JDK17, Linux SDK for build and Windows SDK/ADB for Windows AVD.
3. Baseline first; reproduce bugs with failing regression tests before fixing.
4. Inspect real screenshots, collect PID-scoped logs and runtime resource samples.
5. Update status/tasks, issues and draft PR with honest gates; commit verified changes only.

## Risks/decisions
AVD ZFold3_API35 is generic foldable, not Samsung hardware. Start with -no-snapshot-save,
explicit port 5580. Do not alter other repos, dangerous permissions or host security.
No verified OSM/voice pack: downstream factual narration acceptance blocked.

## Progress
- Repository docs/plans audited. Initial gradlew fails on WSL due CRLF shebang;
  only gradlew line endings normalized locally. JDK17 and both SDKs discovered.
- Windows AVD starting; no connected physical device. Evidence pending build/runtime.

## Validation decisions (2026-10-09)
- GPX end-of-route regression actually failed (nonzero terminal speed). Fix resets speed
  only at finished timeline; full regression/APK build subsequently passed.
- Two simultaneous Gradle runs conflicted on XML reports. Discard that invocation;
  run all subsequent Gradle builds sequentially.
- ADB broadcast result=0 is not IPC rejection evidence. Use a separately signed
  unprivileged probe APK and verify the active child session survives STOP.
- Add bounded aggregate render telemetry for actual Android GL frame cadence. SurfaceView
  render frames are not Android UI gfxinfo frames. Histogram reports P95 upper bound
  to 1 ms precision; log FPS/frame count every 30 seconds without location/audio data.
- For 60-minute tests, use a recorded synthetic parent-settings fixture with the already
  implemented 60-minute limit, retain/restore original prefs. Expected session expiry at
  60 minutes is distinct from crash; never bypass the limit. A 60-minute collection may
  include that final parent transition; report actual child rendering duration separately.

## M2 resource ownership regression
Actual KidsGameLifecycleTest failed: screen dispose count0 instead of1.
Inspected the resolved LibGDX1.14.2 Game bytecode: Game.dispose only invokes Screen.hide.
Decision: KidsGame owns its AdventureScreen and must hide/dispose it exactly once;
clear the screen reference to make repeated shutdown safe. No architecture expansion.
The running60-minute APK is still a4e1f34; do not substitute source fixes into its evidence.

## M7 recreation budget
Source currently creates a full new timeout on each child Activity recreation.
Decision: preserve one absolute elapsedRealtime deadline in savedInstanceState and enforce
remaining time on resume, so background/recreation cannot replenish a child budget.
Verify actual recreation using Android framework Instrumentation and screenshots after soak;
no exposed release test switches, permission grants or fabricated time-PASS.

## M3 loss freshness regression
Actual11-second core loss test failed: a rejected implausible GPS jump refreshed latestFixAt,
so the bus kept its previous speed after valid signal loss. Decision: freshness advances
only when a fix is accepted/rebased, not on rejected timestamps/jumps. Inject a LongSupplier
clock for deterministic core loss/recovery tests; default stays System.currentTimeMillis.
This does not validate GPS radio accuracy or emulator GPS injection.

## Android recreation journey continuity
Each KidsActivity currently constructs a new feed on recreation. Retain the pure JourneyFeed
through Activity's non-configuration instance (no Activity/GPS-source reference, no disk location
history) so configuration recreation keeps accumulated demo/live distance. An explicit new preview
still creates a new journey. Instrumentation will compare feed identity/distance across a real
recreation and the unchanged absolute session deadline. Process-death persistence is out of scope
and remains NOT_IMPLEMENTED/NOT_VERIFIED; never claim this is persistent GPS memory.

## Permission refusal follow-up
Actual BACK-cancelled location dialog left FINE denied but parent displayed only Session stopped:
privacy-20261009T030349Z package-after-refusal.txt / denial-ui.txt, assertion FAILED.
Root cause: ParentActivity.onResume overwrites the permission-result status after callback.
Decision: preserve the current permission outcome for resume/render, clear it on a new explicit
preview/request/action; no grants, GPS fallback or new child permission flow. Rebuild and rerun
the actual failed denial/offline test and parent-preview/recreation regression.
Earlier coordinate-based permission-dialog automation unexpectedly granted location. Both FINE
and COARSE were immediately revoked; exact input cause is unproven. Replace permission-dialog
coordinates with BACK refusal and assert denied state; finally revoke any unexpected new grant
back to original denied state. Retain failed artifacts and report the incident, not a PASS.

## Correct Android location request contract
The resource-action instrumentation proves the actual button click, denied permissions and
missing refusal callback/explanation; preserving onResume text alone did not fix the failure.
Source requests only FINE, targets35 and omits explicit COARSE. Official Android runtime-location
documentation requires requesting FINE and COARSE together on Android12+ (fine-only can be ignored):
https://developer.android.com/develop/sensors-and-location/location/permissions/runtime
Decision: declare/request both, inspect callback by FINE permission name (not array position),
and keep live child startup gated on precise permission. Coarse-only never launches live tracking.
Tests click only deny by resource ACTION_CLICK; no test grants either permission. The earlier
onResume diagnosis was incomplete; final evidence must retain failed attempted fixes.


## Final verification and correction
Early permission failures included harness Unicode/resource namespace/cache errors: actual
PermissionController package is com.google.android.permissioncontroller. A visible dialog
not clicked is a harness FAIL, not proof of ignored callback. Fine-only requests did show a
real dialog on this API35 build; the Android12 contract citation motivates portability hardening,
not a claim that this emulator ignored all fine-only requests.
With the corrected denial node action, baseline APK a4e1f34 actually FAILED refusal explanation
while both grants remained denied (permission-baseline-final.txt, dialog_denied_by_node_action=true).
Final source bad299a actually PASS refusal and offline/recreation in privacy-20261009T032118Z.
A 15-second bounded wait verifies network teardown; the earlier2-second assertion FAIL is retained.
Real3600second baseline child lifetime/expiry,56 core tests, Android builds, rendering/lifecycle,
local signed/unsigned STOP and final126-second capture completed. Read final-test-summary.md.
T-011 stays IN_PROGRESS for uncovered runtime/hardware cases; no milestone falsely marked DONE.


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
