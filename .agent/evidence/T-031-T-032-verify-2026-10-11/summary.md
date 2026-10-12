# T-031/T-032 verification — 2026-10-11

Owner: Main. Dependencies: integrated T-029–T-032, source HEAD a379117.
User requested verification. Production source unchanged; test-only harness added.
Plan: `.agent/plans/active/T-031-T-032-local-device-verification.md`.

## Executed evidence

- 21/21 Python contracts PASS. Initial combined script stopped because JAVA_HOME
  was unset; preserved `local-tests.log`. JDK17/Android SDK/PYTHONPATH retry PASS.
- Fresh `:core:test --rerun-tasks`: 140 tests, zero failures/errors/skips.
  CreativeLessonPackTest 5/5; VisualCueTest 5/5. See core-results.txt and
  core-tests-fresh.log. Android unit task NO-SOURCE.
- Android debug APK, AndroidTest APK and desktop classes BUILD SUCCESSFUL.
  New test harness initially used Files.readString/writeString, unavailable in
  Android compile API; corrected to UTF-8 readAllBytes/write, rebuilt SUCCESS.
  Original compile failure retained in qa-build.log.
  Desktop compile only. APK hash in apk-sha256.txt.
- Physical SM-F926B, external DeX display2, ARZOPA 1920×1080.
  Debug + AndroidTest installs SUCCESS.
- `creative_cache` PASS for SAU/ONG/BOTH: production Android AtomicFile roundtrip,
  recreated store cursor 7, heard-question rejection, last-good retained after
  rejected swap, age mismatch and corrupted SHA rejection. Disposable namespace,
  no user cache changes. No actual reboot/power-loss claim.
- `visual_hud` layout assertions PASS in 12 sessions (SAU/ONG/BOTH × age pairs
  4/5,5/4,7/7,10/10). Full synthetic question uses 4 lines and answer 3 lines;
  exact text, last character, vertical bounds and no ellipsis checked.
  Actual configured ages flowed through Activity creation, not a HUD override.
  12 external SurfaceFlinger screenshots captured. This test uses a synthetic
  Beat and production HUD/game prompt APIs, not real AI pack playback.
- Physical gpx_parser, recreation, single_display_mouse, parent_controls PASS.
  Recreation preserves feed/deadline; parent controls pause/resume, audio-only
  and immediate AI/cloud disable verified by existing harness.
- Initial audience_picker FAIL: test expected obsolete BOTH `4–5 tuổi` label;
  production now uses configured `5 và 4 tuổi`. Test fixed to read configured
  ages; original failure retained. Rebuilt test APK and retry PASS for all
  SAU/ONG/BOTH choices (`audience-picker-retry.log`).

## Visual acceptance FAILED

Reviewed BOTH-4-5.png, ONG-5-4.png, SAU-10-10.png, ONG-7-7.png and BOTH-10-10.png.
Question readable, configured labels correct, selected cabin/outside companions
visible, one butterfly prop visible. These samples do not verify all themes or
pose/reaction/POI interruption transitions.

The right-hand GPS/provenance panel overlaps the answer banner near its top-right
edge, covering part of the first answer line. Examples: BOTH-10-10.png and
ONG-5-4.png. Native TextView self-layout PASS does not establish unobscured
composed UI. T-032 M1 visual acceptance FAIL; no production fix made in this
verification task. Narrow prompt/answer sample does not prove every maximum
length string will fit.

## Open gates / final task status

T-031 and T-032 remain IN_PROGRESS, not DONE or release verified.
Real creative provider generation + second review, invalid/offline/timeout/429
and initial+5 retries, refill/coordinator lifecycle, billing/privacy payload
observation, adult factual/translation/child-age QA, all visual cue themes,
POI interruption, Capybara reaction/audio/nav coexistence, thermal/FPS/P95
and natural 30–60 minute endurance NOT VERIFIED in this session.
Creative consent is OFF (flag absent); no provider call or enabling of creative
opt-in performed. No cloud child text, GPS injection, permission grant/revoke,
voice pipeline/navigation/Device Owner changes. QA preferences restored via
finally blocks. Original untracked evidence kept. Test APK removed and ParentActivity explicitly reopened on DeX2; see cleanup.log.
No commit/push.
