# T-071 — managed official YouTube follow-up, 2026-10-19

Owner Main. Dependencies T-071 official handoff, T-068 safe cadence, existing
parent pause/GPS/GPX restoration. Overall IN_PROGRESS; physical acceptance OPEN.

Implemented optional default-OFF notification-access MediaSession control after
explicit parent disclosure and manual Android grant. No notification payloads are
read, logged or stored. All pause commands are filtered to official YouTube;
no global media keys, navigation, Accessibility or Device Owner changes.

Scheduled turns persist a bounded parent-duration elapsed-time deadline, boot
count, original task/display. The system-bound listener attempts targeted pause
at deadline; exact nonempty token-set PAUSED/STOPPED confirmation precedes
best-effort original live KidsActivity task foregrounding. A live weak reference
checks the actual Activity display; no hidden taskInfo display API, new task,
phone fallback or finished-journey resurrection. Reconnect can retry. A missing
or killed listener cannot guarantee cutoff. Reboot invalidates the old clock.

On return, managed mode attempts confirmed pause before releasing the existing
hold exactly once. Failed/unknown/replaced sessions retain manual parent recovery.
Ending KidsActivity attempts targeted pause while retaining the durable journal.
Missing permission prevents managed launches and uses the existing offline path.
Parent library preview remains manual. No permanent device enablement/settings,
Google account, video library, or notification-access grant was changed here.

## Actual verification

- 281 core JUnit tests, zero failures/errors/skips; five new tests cover missing
  sessions, a dispatched-but-unapplied command, all-session pause, token replacement
  and other-package isolation. Raw XML remains under core/build/test-results/test.
- Full Android debug + test APK, desktop classes, core tests and Android unit task
  BUILD SUCCESSFUL. Android unit task has NO-SOURCE; instrumentation only compiled.
- Required scripts/test-local.sh completed successfully (all source/art/privacy/
  audio/display contracts plus core tests); protected art sources remain unchanged.
- Final APK repackage after last source edits also BUILD SUCCESSFUL; reused the
  already-generated/validated art using -x generateKidsArt. Hashes in verification.json.
- Added isolated-storage instrumentation assertions for default OFF, persisted
  task/display/deadline and journal cancellation. NOT RUN: no ADB device.
- git diff --check verified separately. No CI, install, commit or push performed.

Initial failures are retained: Windows gradlew.bat absent, initial Python Pillow
missing, then one compilation failure from unavailable public taskInfo.displayId.
Installed pinned art dependencies only in /tmp virtualenv and replaced the hidden
field with actual live Activity display verification. Final builds above passed.

## Limits and next physical gates

This is a parent-duration cutoff including startup/buffering, NOT real video-end
detection. Official YouTube branding/recommendations/links remain visible, and a
shorter clip can expose related content before the timer. Package pause is not
force-stop, app suspension or blocking a child from manually opening YouTube.
Fully hiding the provider cannot be promised with this route.

adb.exe devices -l returned no devices (device-status.txt). Actual notification
permission UI, YouTube pause response, timer/reconnect/reboot behavior, DeX task
foregrounding/background-launch restrictions, same-display behavior and sound
restoration remain NOT VERIFIED. Actual vehicle/navigation/endurance gates remain
open from the previous slice. This is not a release or full task DONE.

Physical next run: connect Fold3; install exact hashed APK/testAPK; let parent
read/grant notification access; use only owner-reviewed temporary test video;
observe scheduled boundary, timed pause and actual external-display return;
verify missing/revoked permission, replaced/disappeared sessions, recreation,
finished journey and disconnected display retain safe hold/no phone fallback;
restore test library and settings. Do not grant broad permission silently with ADB.
