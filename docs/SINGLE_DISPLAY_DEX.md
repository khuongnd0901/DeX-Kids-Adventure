# Single-display Samsung DeX — broken Z Fold3 screen

## Hard hardware constraint
The Z Fold3's internal touch display is damaged/unusable. The external Samsung DeX monitor with mouse/keyboard is the **sole user interface**.

## Application UX
1. Open **DeX Kids Adventure** from the **DeX external monitor launcher** with mouse/keyboard. The launcher opens `ParentActivity` *on that display*.
2. Parent config (age, duration, quiet, offline speech) and DEMO/LIVE GPS launch are done **on the same display**. `ParentActivity` starts `KidsActivity` using standard Activity launch, without `launchDisplayId` and without targeting the phone screen.
3. On top of the LibGDX scene, **long-press** "Parents · hold" with a mouse/trackpad or press **F10** / Menu key. This shows a modal `AlertDialog` on **the game display**. Continue closes the menu; End adventure finishes the game and returns to the dashboard on the same display. Android Back also opens the menu.
4. On opening controls, the journey/animation delta is zero and GPS foreground listener stops. When returning to game, live GPS listener resumes only while the session is valid. **The session deadline uses SystemClock.elapsedRealtime and keeps running even when controls are open.**
5. Android runtime GPS permission request appears in the dashboard's same task/display. Declining permission does not launch live GPS; DEMO remains available.
6. Optional signature-protected STOP receiver from DeX-Assistant remains unchanged, not a prerequisite to using the menu.

No dual-screen parent+child workflow, no mandatory touch interaction, no `Presentation` API, no guessed display ID, and no fallback from DeX to the broken phone for start/stop.

## Behavior if there is no external display
A normal Android emulator has a single default virtual display. It is an acceptable **simulator** for game/menu testing, not proof of Samsung DeX compatibility. On the actual broken-screen device, start the app **from the external DeX monitor launcher** so that the dashboard's Activity is placed on that display. Android may ignore forced launch-display requests on unsupported/secure displays, so we do not use them in normal navigation.

## Local test matrix (needs connected simulator or physical device)

| ID | Test | Expected |
| --- | --- | --- |
| SD-001 | Open app using DeX monitor mouse | Dashboard visible on DeX; nothing requires phone touch |
| SD-002 | Change session duration, age, quiet, consent | Changes performed on same monitor |
| SD-003 | Start DEMO | Game replaces dashboard on **same display** |
| SD-004 | Long-press Parents button with mouse | Parent dialog appears on game display |
| SD-005 | Continue after dialog | Game resumes; delta paused during dialog |
| SD-006 | Open using keyboard F10 or Menu key | Same dialog appears, focus remains on display |
| SD-007 | End adventure from dialog | Same-display ParentActivity resumes |
| SD-008 | Start LIVE GPS, deny/allow permission | Deny keeps dashboard and DEMO available; allow uses live feed |
| SD-009 | Pause/resume, background/foreground, resize DeX window | Renderer/controls remain usable, session does not reset |
| SD-010 | Open menu near session expiry | Deadline still enforced, game does not continue beyond limit |
| SD-011 | Single-display emulator without secondary display | DASHBOARD→GAME→DASHBOARD works, no external display requirement |
| SD-012 | Actual Z Fold3 DeX HDMI connected, broken built-in screen | Full workflow works with external monitor and mouse ONLY |

`SD-012` is hardware-only. Emulator/CI cannot claim it PASS. Hardware results belong under `.agent/evidence/` with display IDs, screenshots and logs.

## ADB hints — test from local Codex host
```bash
adb devices -l
adb shell dumpsys display
adb shell am start -n com.khuongnd.dexkids/.ParentActivity
adb shell input keyevent KEYCODE_F10
adb shell dumpsys activity activities
# Emulator instrumentation (after assembling AND installing both debug + androidTest APKs):
adb shell am instrument -w -e mode single_display \
  com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
```
When an external display is enumerated, the host may inspect launch-target support using `adb shell am start --help` and use a **grounded** display ID with `--display`. Do not assume display ID is 1 or that DeX supports all Android simulated-secondary-display APIs. Avoid treating shell launches as proof of DeX launcher behavior.

The instrumentation mode `single_display` opens ParentActivity, starts a DEMO game,
checks that both Activities use the same display ID, sends F10, confirms the menu freezes the
journey, clicks End adventure and checks that the game closes. The test is meaningful on a
local Android emulator. Do **not** mark physical Samsung DeX tests PASS from this alone.

## Open gates
- The on-screen parent button uses a long-press only, **not** a PIN/password; child-lock/PIN acceptance remains open.
- Actual Samsung One UI DeX window focus, keyboard handling, permission dialogs, resize/lifecycle, GPS during navigation coexistence and thermal/performance are **NOT VERIFIED**.
- User's local Codex should run SD-001..SD-012 as applicable, collect evidence, and keep M7/M8 `IN_PROGRESS` until every physical acceptance gate is fulfilled.
