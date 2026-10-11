## T-011 / T-028 latest GitHub build retest — 2026-10-11

Owner Main; dependencies T-027/T-028, existing T-011 QA plan.
User requested latest GitHub pull and rebuild/test. Acceptance for this slice:
fast-forward source, run existing contracts/core/target builds and physical
short regression, record actual evidence. Overall acceptance NOT MET: failures remain.

Source main fast-forwarded a2570aa → d9a48f0 (origin/main at fetch).
Local STATUS/TASKS notes preserved by stash; overlapping additions resolved
by keeping both sections. Stash retained as backup. No commit/push.
No production or test source edits; only QA documentation/evidence added.

Build: JDK17, Android SDK, PYTHONPATH=/tmp/dexkids-art-python.
Gradle :core:test :android:testDebugUnitTest :android:assembleDebug
:android:assembleDebugAndroidTest :desktop:classes BUILD SUCCESSFUL in55s.
111 core tests:0 failures,0 errors,0 skipped, including AudienceAgeGateTest.
Android unit task NO-SOURCE. Desktop compiled only; no desktop runtime claim.
18 source contracts independently executed:16 PASS/2 FAIL.
- dashboard count expects66 `new Beat(` expressions, actual67.
- audio test expects prior exact speech-rate expression; upstream includes English branch.
Full local suite is NOT PASS. See per-contract logs and build.log.

Physical SM-F926B Android15, external DeX display2 (1920x1080):
new debug and AndroidTest APK install both SUCCESS.
- gpx_parser PASS.
- single_display_mouse PASS: same-display dashboard/game, mouse menu,
  paused journey and stop returns to dashboard.
- recreation FAIL twice: child window hidden or unfocused.
- parent_controls FAIL twice: GL distance read timed out.
- audience_picker FAIL: visible audience option not clickable. Test still
  searches Sâu4/Ong3/BOTH3–4 labels while production now uses5/4/4–5;
  physical new-label acceptance is NOT VERIFIED by this stale test.
Retain original and retry failures. Focus/GL causes not established.
No full physical suite, English audio, new artwork visual review,30/60min
endurance, navigation, ASR or live GPS acceptance claimed.
Test APK removed successfully; ParentActivity launched explicitly on DeX2.
No permission grants/revocations, GPS injection, cloud test or child recordings.
T-011/T-028 remain IN_PROGRESS; no task transitioned to DONE.
APK SHA256 saved in apk-sha256.txt. All logs are in this evidence directory.
