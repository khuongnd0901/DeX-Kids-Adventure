# T-011 — ADB integration rerun, 2026-10-09

Owner: Main agent. Dependencies: T-001–T-009. State: IN_PROGRESS.
Source: `6c8a9dfa25f4c9e6836462a50e6ad78cb8a26390`, clean before execution.
Target: Windows AVD ZFold3_API35, emulator-5580, API35; generic emulator, not Samsung DeX.
Acceptance for this slice: build latest source, execute real framework recreation,
same-display parent controls and offline preview; retain artifacts and explicit gaps.

| Check | Result | Evidence |
| --- | --- | --- |
| Core unit tests | PASS: 80 tests, 0 failures/errors/skips | JUnit XML in core/build/test-results/test |
| Desktop classes, Android debug + AndroidTest APK | PASS | integration-20261009-latest/build.log |
| APK install | PASS | ADB streamed install returned Success |
| Distinct Activity recreation | PASS | activity-recreation-20261009T100858Z-31952/instrumentation.txt |
| Session deadline and journey continuity | PASS | deadline 2544123 unchanged; same_feed=true; distance 22.579→53.375m |
| Same-display dashboard/game, F10 menu, pause, end adventure | PASS | integration-20261009-latest/single-display.txt; display_id=0 |
| Manifest privacy | PASS | No INTERNET or RECORD_AUDIO requested by installed app |
| Actual offline demo/recreation | PASS | privacy-20261009T100928Z/results.json; no active default network |
| Location refusal | BLOCKED | Fine/coarse already granted before test; preserved, no revoke/regrant |
| Sampled visual review | PASS for inspected images only | Recreation after.png and parent-menu.png inspected: game, HUD and modal visible |

APK SHA256: `a69ee4cbb1b0e2509793bfcaf8cd7bde95191fe570001bf1594ab122d537f8ca`.
Raw artifact directories above are relative to ignored `build/simulator-artifacts/`.
Build initially failed on read-only Gradle cache; approved escalated rerun succeeded.
WSL Windows-executable interoperability also required approved access.
Privacy harness restored original airplane-disabled/Wi-Fi-disabled state. Location
grants stayed identical before/after. Test APK removed; app returned to ParentActivity.
No production source changed, no other repository accessed.

Not covered by this rerun: SAF GPX picker E2E, ADB live GPS injection, HCMC sample
runtime POI/themes, actual Vietnamese TTS/audio coexistence, IPC probes, prolonged
performance/soak, exhaustive visual seams, physical Fold3/DeX. Screenshot FPS is
a short renderer sample, not sustained performance acceptance. Existing historical
evidence remains separate. No milestone is DONE.
