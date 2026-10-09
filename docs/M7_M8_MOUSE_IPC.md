# M7/M8 — Mouse-first single-display DeX, no lock screen

Status: IMPLEMENTED IN FEATURE BRANCH, NOT HARDWARE VERIFIED (2026-10-09).

## Product decision
The Fold3 built-in screen is damaged and cannot be used. Only the external Samsung DeX display with mouse/keyboard is supported. There is **no PIN, child lock, kiosk, long-press requirement or touch-screen fallback**. The hardware having no touch does not itself prevent a user with a mouse/keyboard from opening controls; use adult supervision.

## Parent flow (same display)
1. From the DeX monitor launcher open ParentActivity, set age group, 15/30/60 minute limit, Quiet (default ON), offline Vietnamese TTS consent (default OFF), and optional Audio-only.
2. Start DEMO, bundled HCMC GPX sample, SAF GPX replay or location-permitted LIVE GPS on this monitor.
3. In KidsActivity click **Parents · menu** once, press F10/Menu, or Android Back. The modal pauses journey animation and stops live GPS + TTS; absolute session timeout continues.
4. The dialog offers: toggle **Audio-only / animated scenery**, toggle **Quiet**, Continue and End. Quiet OFF does **not** grant offline TTS consent.
5. Audio-only runs the journey/POI/story logic but skips animated GL scenery/sprites; Android native subtitles and controls remain available. It never locks, blanks or redirects the display.
6. **Reset local preferences** requires confirmation and restores settings including TTS OFF and Quiet ON. GPS traces are not persisted by this app.

## Trusted IPC contract (DeX Assistant)
The exported KidsCommandReceiver in package `com.khuongnd.dexkids` is protected by signature-level permission `com.khuongnd.dexkids.permission.CONTROL`.
- `com.khuongnd.dexkids.action.KIDS_PAUSE` — opens parent modal; leaves deadline running.
- `com.khuongnd.dexkids.action.KIDS_RESUME` — dismisses an open parent modal; no new session is created.
- `com.khuongnd.dexkids.action.KIDS_STOP` — ends active game.

Example for a **same-signed and authorized** companion app:
```kotlin
// AndroidManifest.xml in the Assistant: <uses-permission android:name="com.khuongnd.dexkids.permission.CONTROL"/>
val command = Intent("com.khuongnd.dexkids.action.KIDS_PAUSE")
    .setClassName("com.khuongnd.dexkids", "com.khuongnd.dexkids.KidsCommandReceiver")
context.sendBroadcast(command)
```
Receiver protection is enforced by Android. Different signing certificates or missing permission must be treated as denied; do NOT downgrade to normal permission, unprotected broadcast, Accessibility automation or exported free-form IPC. Debug and release signing identities must be checked before integrating the separate DeX-Assistant app.

There is **no remote START** command: foreground DeX launcher starts the same-display session, avoiding unsafe background activity launches and broken-phone screen routing. This branch does not change DeX-Assistant or DeX-Assistant-UI source.

## Acceptance matrix
| Test | Environment | Gate |
| --- | --- | --- |
| Source contract no dual-display/lock, protected IPC | CI Python | Automated |
| Java core, Android debug + AndroidTest build | CI | Automated |
| `single_display`: F10 pause/end | Android emulator | NOT RUN for this commit |
| `single_display_mouse`: one-click pause/end | Android emulator | NOT RUN for this commit |
| Audio-only screen + native POI captions, GPX progression | Android emulator | NOT VERIFIED |
| Quiet/TTS opt-in, focus interruption with Maps/Vietmap | Real Fold3 DeX | NOT VERIFIED |
| Signature-matched authorized commands and differently signed rejection | Signed APK integration | NOT VERIFIED |
| Mouse/keyboard-only start/menu/permission/resize at external monitor | Actual Fold3 DeX | NOT VERIFIED |

The previous simulator's 19.825 FPS art-integrated prefix is a known performance investigation, not resolved by adding Audio-only. Keep M7/M8 issues IN_PROGRESS and M9 BLOCKED until actual evidence meets those acceptance gates.
