# T-071 — embedded YouTube setup-first, 2026-10-19

Owner Main. Dependencies T-068 five-card cadence, T-071 catalog/journal and
existing parent audio/GPS/GPX pause. Source/configuration slice implemented;
overall IN_PROGRESS. Owner accepts official branding. Owner initially requests
configuration first, then clarifies a Cloud project already exists but needs
help retrieving its key. No credential, notification receipt or real API response
has been supplied/accessed. No duplicate project, key or notification was created.

## Implemented

- Parent setup screen: Cloud/key/designation links, actual package/signing SHA-1,
  encrypted Android Keystore AES-GCM key storage, country/contact/notification
  reference, separate terms/privacy agreement and delete. Fields are owner
  declarations; not Google/legal certification. No key in HTML/source/logs.
- Embedded selected by default, playback still OFF/unready. Missing setup or
  Audio-only takes ordinary offline content; no external YouTube fallback.
  Explicit legacy mode and existing official-app audio hold are preserved.
- Bounded fresh Data API videos.list validation for reviewed ID, MFK/embedding,
  availability, region, finite duration within the parent's planned budget. No
  child profile/audio/GPS/contact in requests, scraping, cache or metadata logs.
- Visible official privacy-enhanced IFrame with client package origin, exact
  target-origin message port and per-instance nonce, long generation as string;
  no JavascriptInterface, key in JS, covering UI, hidden audio or altered ads.
  Third-party cookies are disabled per-view; no zero-Google-tracking claim.
- 75/25 sibling layout with existing animated Capybara art. Parent return button,
  guarded ENDED/error/autoplay-blocked/watchdog/network/render/resize/display/
  background teardown, one terminal transition. Foreground-only GPS/GPX/audio
  restoration; manual GPX pause retained. F10/controller PAUSE opens original
  pause menu after disposing video. STOP destroys embed. No notification access
  needed for embedded mode, no DO/Accessibility/navigation/other-repository changes.
- Native user HTTPS player links dispose the embed and retain a durable external
  acknowledgement hold. Native suggestions/links are not claimed curated. No
  new phone launch or implicit external-mode fallback.

## Actual checks

289 core tests, zero failures/errors/skips; eight new lifecycle/metadata tests.
Node executes the actual bundled HTML with mocked document/IFrame API: nonce
handshake gating, one script/player owner, controls/privacy host, ready/playing/
paused/buffering/ended/error/blocked and exact >2^53 generation all pass. This is
not browser/live YouTube evidence. scripts/test-local.sh all contracts/core pass.

Full Android app/testAPK + desktop build passed; Android unit task NO-SOURCE.
Final source APK rebuild also passed, reusing already generated/validated art with
-x generateKidsArt. Packaged HTML bytes exactly match source. APK sizes/hashes in
verification.json. Added isolated Keystore/readiness/journal instrumentation
assertions, COMPILED ONLY; not run without a device. git diff --check passed.

Initial test-fixture string compile error and unavailable hidden Activity display
callback compile error are retained. Corrected fixture and used public display
listener/layout/lifecycle/watchdog instead. Final builds passed. Earlier successful
full build plus final successful rebuild are both retained; no error omitted.

## Open gates / next steps

Windows adb.exe devices -l returned an empty list. No APK install, settings change,
real Keystore/UI, metadata/quota/key restriction/Referer, player autoplay/ENDED,
physical DeX geometry/art/performance/acoustics/nav/GPS or reboot runtime PASS.
No real API key used; no YouTube playback loaded. Google child-directed/MFK
tracking/privacy acceptance remains unverified; setup text is not its substitute.
No CI, commit or push. Existing vehicle/endurance gates remain open.

Owner next: select existing project, enable Data API v3, Credentials -> Create
credentials -> API key; restrict to API and installed APK package/SHA-1. Enter
key only in local parent setup. Establish actual applicable Google designation
for the sideloaded Android route and review privacy obligations before agreement.
Connect Fold3 and test reviewed eligible video: natural cadence, physical 75/25,
actual ENDED, errors/blocked/offline, F10/PAUSE/STOP/background/resize/display loss,
exact audio/GPS/GPX restoration, native link external hold and existing settings.
Do not label source tests, mocked events or owner-entered receipt as these gates.

Current design: docs/T071_EMBEDDED_YOUTUBE.md. Active plan retained.
