# T-071 official YouTube — implementation and bounded device verification

2026-10-18. Owner Main. Baseline `56e60e9`. Scope was changed by explicit owner
answers: personal APK/ADB use, Premium through official YouTube, layout and
turn automation adjusted to platform capabilities; LIVE/GPX may pause this app's
journey/GPS/POI until parent returns and confirms video stopped. Embedded player,
YouTube Data API project/key, automatic playlist discovery and account access
are outside this revision. Distinct artwork task also named T-071 is unchanged.

Implemented: canonical single-video link parser; immutable bounded local catalog
(64 videos/64 KiB), explicit review/age/audience, atomic file persistence, default
OFF notice/automatic settings, configurable 1:1/3:1 story/video cadence at existing
five-card boundaries, deterministic rotation and rolling planned request budget.
Official package on current Activity's external display only; no browser/phone
fallback. Pause local speech/mic/soundscape and app GPS/POI, render-thread GPX pause
and previous-state restoration, durable return hold and controller-resume refusal.
Optional parent's recall prompts are displayed on the return card. No video-end
or stop callback is invented. DeX/YouTube owns window layout, account and playback.

## Actual checks

- Core: 276 tests, zero failures/errors/skips (11 new media tests). Android app,
  instrumentation APK and desktop compile successful. Android unit task NO-SOURCE.
- `bash scripts/test-local.sh` completed, plus final relevant single-display,
  audio/engagement and learning source contracts. No CI/emulator used.
- SM-F926B/API35, wireless ADB; actual physical HDMI FullHD is Samsung Desktop2.
  Device storage test: isolated default OFF/notice, disk reload, invalid-revision
  retention, AtomicFile failure recovery, durable journal/budget/single owner/delete.
- Actual KidsActivity recreation restores audio hold; narration/soundscape stopped;
  trusted RESUME cannot bypass acknowledgement; explicit parent button releases it.
- Actual LIVE listener absent during hold, registered again after acknowledgement.
  Bundled **synthetic** GPX running/manual-pause variants restore correct pause
  state; frozen timeline observed. No road matching/location-accuracy claim.
- Production scheduler hook requests official YouTube at a **synthetic five-card
  boundary**, persists request/rotation and consumes boundary once. This is not
  evidence of five naturally answered live questions. Temporary catalog/settings
  are backed up, then restored on paused/acknowledged return. No permanent test
  video approval or default enablement remains.
- Owner supplied only `uWXvGSMrwXc` for playback testing. Actual physical HDMI
  screenshots show “Dê đen và dê trắng”; official media session PLAYING. Owner
  explicitly confirms visible video + audible sound + no ads seen in this trial.
  This is not a subscription verification or universal no-ad promise.
- Corrected native Parent Settings -> YouTube library opened by mouse on Desktop2;
  main enable unchecked, automatic unchecked/disabled, no permanent catalog rows.

## Retained failures and corrections

1. Initial build called ActivityManager's display-start method statically; corrected
   to the instance API and verified compile.
2. Initial instrumentation asserted before AlertDialog's posted click handler ran;
   corrected test waits for UI idle, then observes actual state. Initial failure
   retained, final device modes return instrumentation code0.
3. First QA launch explicitly chose API HDMI6. YouTube PLAYING on logical6 did NOT
   appear on physical HDMI, which still rendered Desktop2. Owner reported failure;
   physical screenshot confirmed it. Retest on actual Desktop2 succeeds. Production
   inherits current Activity display, including virtual DeX Desktop, without picking
   a display by HDMI name or fixed ID. The display6 test is not physical acceptance.
4. Test-only backup helper initially used Java Files text helpers absent from Android
   stubs; replaced with byte/UTF-8 APIs. Final instrumentation build succeeds.
5. Native Vietnamese IME composed the typed video ID into accented characters.
   Parser rejected it and no row was saved. URL field updated to URI/no-suggestions,
   ASCII IME hint and no personalized learning; title/recall keep Vietnamese input.
   Samsung physical Telex still composed the ID despite these public IME hints.
   Add explicit parent-only clipboard Paste action: validates then sets the exact
   canonical URL without IME key events. No automatic clipboard access; preserve
   Vietnamese input for titles/recall. Typing manually requires English keyboard.
   Failed typed result retained in url-keyboard.json; native paste test separately
   records the final behavior: device-paste-display2.txt returns code0;
   final-paste-build.txt succeeds and installed APK SHA-256 matches local APK.
6. Owner switched DeX keyboard to English. Retyped exact `uWXvGSMrwXc`,
   saved via native form and read back exact case-sensitive ID with approved=false.
   Removed QA row through native delete confirmation; catalog empty and feature OFF.
   Save initially covered by Samsung floating keyboard; closed keyboard then saved.
   Evidence url-keyboard-english.json.

## Open acceptance

**Overall T-071 remains IN_PROGRESS.** Source/device slice is implemented; owner
has no car available for actual Maps/VietMap audible navigation, moving-road
GPS reacquisition and vehicle endurance acceptance. These are NOT VERIFIED.
No alteration of those apps, Device Owner, Accessibility, original voice path
or other repositories. No credentials, GPS, child audio or child replies sent
by the handoff. Existing age/source/AI configuration retained apart from normal
new-session rotation/source sync timestamps.

T-074 simultaneous audio: owner requested it, approved required Galaxy Store
terms (optional personalization OFF), then explicitly deferred when Store required
Samsung login for Sound Assistant. No account login/creation; Sound Assistant
not installed, Multi sound not configured, no simultaneous-audio PASS claim.
T-071's safe return hold remains.

Raw screenshots stay local (some initial screenshots contain device/account UI);
only concise verified reports/logs/hashes are intended for source publication.

Published text build logs normalize trailing whitespace only; device result content
is preserved. Physical UI screenshots remain local.
