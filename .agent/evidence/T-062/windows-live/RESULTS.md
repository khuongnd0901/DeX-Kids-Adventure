# T-062 — real GitHub source on Windows simulator — 2026-10-16

Owner Main. User explicitly requested Windows simulator testing. PASS for the
live-host/foreground/library/playback/offline-restart cases below. Full T-062
remains IN_PROGRESS pending physical DeX acceptance; no physical hardware used.

Windows emulator DeX_Command_Test_API35, API35 x86_64, WHPX, SwiftShader,
1920×1080/density160, display0 explicitly selected. Visible Windows window, ADB
server5038/127.0.0.1:5583. Product APK unchanged from prior T-062 verification:
SHA256 `6c2b5a0ca48edd621d06f208c4693fe607b482d3ef8b13c849b2258427e04f96`.
Only new framework instrumentation modes and QA runner registration were added.
Final test APK SHA256
`84154634d7edf93bbecc7c840d4bfa503ea3d958dd6c497a39dae348d9d08d0f`.

## Actual results

Source:
https://raw.githubusercontent.com/khuongnd0901/DeX-Kids-Adventure/main/docs/story-source/stories.example.json?source=app

No injected HTTP connection/client/provider. Parent source dialog was actually
opened, URL entered, checkbox enabled and Save clicked using Accessibility UI.
Source content SHA256 equals published sample:
`d743184f8ef2a63f751c668fa9a35714d334724a431feb3b06e9d02bdcca0953`.

| Case | Verified result |
| --- | --- |
| Source configuration and real HTTPS | PASS: source dialog/save, successful GitHub sync, three immediately playable source-parent library entries, no approval/AI step |
| Conditional request on Android | PASS: actual StorySourceClient GET with persisted ETag/Last-Modified returned unchanged=true/304 |
| SAU | PASS: actual rendered narration advances to scene1 question; persisted cursor11 |
| ONG | PASS: actual rendered narration advances to scene1 question; persisted cursor11 |
| BOTH | PASS: actual rendered narration advances to scene1 question targeted at SAU; persisted cursor21 |
| External force-stop | PASS: process4622 stopped by adb; offline verification in different process4916 |
| No-network restart | PASS: airplane enabled + Wi-Fi/mobile disabled; Android ConnectivityManager.activeNetwork asserted null; exact cache SHA and cursor21 survived; BOTH question visibly resumed |
| Offline failed fetch | PASS: real network error recorded while cached content remained unchanged |
| Local library isolation | PASS: digest of local episode/revision/playback tables unchanged through sync and offline playback |
| Recovery/cleanup | PASS: radio settings equal before/after (airplane0,wifi1,mobile1); source refresh succeeds after reconnect; source error empty; original parent/AI/autoplay preferences restored |
| Handoff UI | PASS: normal exported ParentActivity → Settings → Library flow; UI XML contains three NGUỒN PHỤ HUYNH rows; simulator left running with requested public source enabled |

Real speechStarted observations (100ms samples): SAU0, ONG22, BOTH45; offline
restart0. SAU/cold offline runs validate the caption/read-along path and question
progression, not Vietnamese voice output. ONG/BOTH online runs observed genuine
local narration callbacks. No audible-quality, real child ASR or answer-recognition
accuracy claim. Microphone preference disabled and AI/creative/automatic generation
preferences disabled during playback tests, then restored.

## Reproduce and evidence

JDK17/SDK/NDK paths as in ../SUMMARY.md. Build:

```text
./gradlew :android:assembleDebugAndroidTest --console=plain
```

Final test build SUCCESSFUL in23s. Existing product APK and new test APK were
installed with `adb.exe -P 5038 -s 127.0.0.1:5583 install -r ...`.

```text
adb.exe -P 5038 -s 127.0.0.1:5583 shell am instrument -w -e display_id 0 -e mode t062_live_prepare com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
adb.exe -P 5038 -s 127.0.0.1:5583 shell am force-stop com.khuongnd.dexkids
adb.exe -P 5038 -s 127.0.0.1:5583 shell cmd connectivity airplane-mode enable
adb.exe -P 5038 -s 127.0.0.1:5583 shell svc wifi disable
adb.exe -P 5038 -s 127.0.0.1:5583 shell svc data disable
```

Run t062_live_offline, restore original radio states, then t062_live_restore.
`run.log` and three `t062_live_*.txt` contain actual instrumentation results.
`verified-results.json` checks final artifact hashes, network restoration and
three visible remote rows. `device/results.json` contains live observations,
process IDs and content/cache digests, with private preference backups excluded.
The private backup file is removed after preferences are restored. Screenshots
under `device/`; narration, question, offline and final-library frames captured.
Online SAU narration and offline BOTH question frames visually inspected, as well
as final library. `git diff --check` clean.

The earlier220core tests and emulator mocked-failure/lease/migration regressions
apply to the same unchanged product APK. This run supplements them with actual
Android-to-GitHub/network-disconnection evidence; it is not an endurance run or
all-scenes completion/physical DeX/content-editorial certification.

## Retained initial failures

- Initial test compilation used Java11 Files.writeString/readString, absent from
  the Android compile API surface. Replaced with Files.write/readAllBytes plus
  explicit UTF-8 in QA code; failed log retained.
- First live UI attempt was blocked by a System UI ANR dialog after emulator
  startup. Diagnostic XML and screenshot retained under initial-failure/. Closed
  the System UI dialog and reran; no product change was needed.
- First playback attempt had a QA null dereference while polling subtitleText
  before the first caption existed. Corrected to null-safe polling; retained in
  caption-wait-failure/. This was instrumentation failure, not app crash.
- Initial host handoff tried to start non-exported StoryManagerActivity from adb
  and correctly received SecurityException. Replaced with the normal exported
  ParentActivity → Settings → Library UI route; export policy unchanged.
