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
