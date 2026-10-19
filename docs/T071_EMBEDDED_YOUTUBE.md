# T-071 — embedded YouTube, setup-first implementation

2026-10-19, Main. Owner chooses Data API v3 + official IFrame and visible
YouTube branding; supplies a local test key; explicitly chooses personal/family
sideloaded APK only, manual video review and no Google declaration or store
publication. Local readiness is not Google designation or compliance approval.
YouTube III.J notification obligations remain documented; personal use is not
claimed as an exemption. No notification was sent or fabricated.

## Parent setup

Parent > Settings > YouTube > **Cấu hình YouTube API v3 / trình phát trong app**.

1. Create an owner-controlled Google Cloud project, enable YouTube Data API v3
   and create an API key restricted to that API and the actual Android package /
   signing SHA-1. Both identifiers are shown in the setup screen. Use the signing
   identity of the installed APK; changing signing keys requires updating the
   restriction. Do not paste keys into chat, Git, HTML or evidence.
2. Confirm personal/family use and manually review each video. This app does
   not submit a Google designation; its setup no longer requires a notification
   reference under the owner's explicit revision. The official policy still
   requires child-directed clients to notify Google. This change removes a local
   configuration gate, not an external obligation.
3. Enter operator contact and viewing country, review the inline YouTube privacy
   notice and accept YouTube Terms before enabling the feature. No prechecked
   privacy consent or false Google-completion checkbox is added.
4. Enter the key locally. Android Keystore AES-GCM encrypts it in app-private
   preferences; app backup is disabled. Blank key input preserves the old key.
   Explicit deletion removes encrypted key, settings/consent and the local AES key.
5. Keep **Cách phát: Trong app** selected; enable YouTube and automatic turns in
   the existing library. Add/fully review individual video links, audience/ages,
   and duration rounded up. No playlist expansion, discovery or Google login.

Until key/contact/country/consent are present, the scheduled route uses offline content
without metadata requests, thumbnails, WebViews or IFrame scripts. Changing to
legacy official YouTube requires an explicit mode choice; there is no automatic
external-app fallback. An existing official-app pending hold remains protected.
Audio-only skips embedded video rather than playing hidden/background audio.

## Runtime

Existing five-completed-card boundary, story/video ratio, audiences, rotation and
request/hour budget remain authoritative. Pause this app's story/quiz/TTS/ASR/
AI/soundscape/GPS/POI, journal embedded ownership and whether GPX was running.
Never change system GPS, Maps/VietMap, Accessibility or Device Owner.

A bounded worker GET requests `videos.list` parts `status,contentDetails,snippet`
by approved video ID. Only API key/app signing headers/video ID leave the app;
child names/ages, contact/reference, GPS/audio/replies do not enter this request.
No redirects, 8s connect/read limits, 128 KiB bound, no response cache/log bodies.
Require matching single video, explicit madeForKids/embeddable, processed upload,
public/unlisted availability, no live broadcast/age restriction, parent-selected
country region eligibility and finite duration <= parent-entered estimate (<=30m).
The existing request budget therefore conservatively covers actual duration.
Unknown/missing/restricted/deleted metadata, quota/HTTP/key/network errors fail
closed to offline. Parent review is still essential; MFK is not content approval.

A full native container uses 75% width for a centered 16:9 official WebView player
and 25% for animated existing Capybara art, status and journey-return button. The
WebView is >=200x200 CSS pixels; no HUD, character or custom control overlaps it.
Unsupported size/display or resize cancels the turn. Actual geometry is a device
gate, not established by layout code. No app-global cookies/volume changes.

The bundled local HTML uses `loadDataWithBaseURL(https://<actual package>/)` for
client identity. An exact-origin native message-port transfer with a random
per-player nonce connects only the local main page; no JavascriptInterface is
exposed to remote frames. The long generation is encoded as a string, so JS
cannot round it. API key never enters HTML/JS. Only official IFrame API code and
privacy-enhanced youtube-nocookie player are loaded after metadata passes.
Third-party cookies are disabled for this view; no app analytics are attached.
This is not a claim that nocookie or cookie blocking eliminates Google's data
collection or independently satisfies MFK tracking/legal requirements.

Official controls/branding/ads/links stay functional. Parent opts into visible
scripted playback; Android's documented WebView playback setting is used, with
no synthetic taps or muted-start tricks. `onAutoplayBlocked` closes to offline.
The player must have entered PLAYING before ENDED can complete the turn. First
ENDED/error/blocked/cancelled wins; old/duplicate callbacks cannot restore twice.
Buffer/loading watchdog is 45s; valid manual pause has no deadline. Network loss,
render-process loss, Back/F10, app background or screen change tears down WebView
and message ports. GPX/GPS/audio restore only on foreground, preserving a manual
GPX pause. Background/process recreation leaves an embedded journal that is safe
to release on foreground because no official-app playback was launched.

F10/controller PAUSE closes video and opens the existing parent pause menu.
Parent STOP/Activity finish destroys embedded playback. A direct native HTTPS
player link first destroys the embed, marks the pending journal as external, then
opens the user-requested URL on the current permitted external display. Returning
from that link uses the durable official-app/manual audio acknowledgement path;
it cannot silently resume over external playback. No phone fallback. Native
recommendations are not promised to be inside the approved scheduled catalog.

## Scope and verification limits

The parent library offers immediate **Xem thử trong app** in embedded mode. A
nonexported parent-only Activity uses the same external display and production
player, independent of scheduled feature switches. Pending/disabled rows can be
reviewed without changing or faking approval flags. Normal scheduled metadata
validation still requires approved/enabled; parent review shares all technical
MFK/embedding/region/duration checks with a separate explicit entry point. Key,
contact and privacy consent remain required. Active child journeys or unresolved
external playback block preview. Natural end/Back/F10 closes to the library;
background/error tears down the player. Native external links retain a durable
manual-return hold. No playlist import. Recall text is retained in the catalog;
automatic embedded completion currently resumes ordinary learning.

Tests/evidence: `.agent/evidence/T-071-embedded-youtube/`. Pure fixtures and mocked
HTML API events are not live Google response/device evidence. Owner setup and
actual Android Keystore/UI, Referer/identity, MFK tracking, quota/restrictions,
visible autoplay, ENDED/error/background teardown, DeX geometry/PSS/audio/GPS/
navigation remain physical/service acceptance gates. No full task DONE.

## Official sources checked 2026-10-19

- [Data API video resource](https://developers.google.com/youtube/data/reference/rest/v3/videos)
- [Made for Kids](https://developers.google.com/youtube/data/guides/made_for_kids_status)
- [IFrame API events](https://developers.google.com/youtube/iframe_api_reference)
- [Minimum functionality / client identity / visibility](https://developers.google.com/youtube/terms/required-minimum-functionality)
- [Developer policies / child-directed clients](https://developers.google.com/youtube/terms/developer-policies)
- [Designation tools](https://support.google.com/policies/answer/9664901)
- [Android local page origin](https://developer.android.com/develop/ui/views/layout/webapps/load-local-content)
- [API key restrictions](https://docs.cloud.google.com/docs/authentication/api-keys)

## Private env-key test APK and Windows simulator (2026-10-19)

For an explicitly authorized local test, put only the owner key in ignored `.env`
as `YOUTUBE_KEY=...`, then build with `-PinjectYouTubeTestKey=true`. A generated
**debug-only** asset carries this key in the private test APK. Never publish or
commit that APK/asset. Normal debug (flag absent) deletes a previous generated
seed; release does not include debug assets. No key literal goes into source,
Gradle arguments, HTML, test output or evidence. Key-import code also requires
Android's FLAG_DEBUGGABLE.

The settings constructor imports a new seed once into the existing Keystore
ciphertext, preserving contact/reference/consent, source selection and library
switches. Deleting settings remembers the handled seed fingerprint, so the same
APK cannot silently reimport a deleted key. A deliberately newly provisioned key
can seed a different private test APK. Key injection does not declare Google
child-directed notification or enable automatic/child playback.

`tools/prepare_youtube_test_key.py` parses the single env field without executing
shell content or copying other env values. Synthetic tests exercise opt-in,
missing/invalid/duplicate entries, stale-file removal and output redaction.
`tools/probe_youtube_test_key.py` makes an explicitly authorized adult/developer
metadata request using APK signing headers, and emits only selected sanitized
status fields. It does not bypass production playback gates or load a player.

Windows Android emulator instrumentation `t071_env_key` verifies actual APK seed,
Keystore/ciphertext/reload/delete behavior and native setup; then requests the
owner test video's metadata through the production Android HTTPS client. The
IFrame page/port lifecycle is exercised separately with the API mocked and all
WebView network loads blocked. These outcomes must not be labelled live YouTube
video playback or physical DeX. No fake notification reference is inserted.

The real Android WebView test exposed JSONObject.getLong rounding numeric strings
above 2^53. Native callbacks now compare the decimal generation string directly
through EmbeddedYouTubeSession.matchesGeneration; a pure regression and actual
Windows WebView test cover that boundary. See `.agent/evidence/T-071-env-youtube-key/`.

## Personal APK live simulator test (2026-10-19)

Owner explicitly removed the local Google-notification-reference requirement,
confirmed personal/family APK distribution only and manual video review. No
store publication or Google submission. The setup consent no longer claims a
notification was completed. Existing optional stored references are retained;
contact, region, encrypted key and privacy opt-in remain required for readiness.
The Android storage test covers missing consent/contact and successful reload
with an empty reference, without making any Google approval assertion.

The emulator-only `t071_embed_live` instrumentation mode runs the production
host/HTML against the owner test video with real metadata and YouTube networking,
then checks cancellation, natural ENDED, one terminal callback and WebView removal.
`t071_embed_journey` starts a DEMO KidsActivity on the emulator external display,
invokes its actual embedded-turn method with a temporary owner-authorized fixture,
checks natural end restores durable hold/menu/soundscape state and F10 closes the
player. This exercises the actual journey callback but does not claim a natural
five-answer scheduler replay. Neither mode adds a permanent approved catalog row;
original playback preferences are restored. Results and limits are recorded in
`.agent/evidence/T-071-personal-live-youtube/`.

## Optional starter stories (2026-10-19)

Parent library > **Thêm video kể chuyện mẫu · chờ phụ huynh duyệt** adds three
missing links only; existing rows/edits/review flags are preserved. New rows have
approved=false, enabled=false, no invented recall questions, BOTH audience and
suggested4–7age bounds for parents to review. No privacy/feature/automatic switch
changes, Google login, playlist expansion, thumbnail fetching or in-app search.
Import is local; normal fresh metadata checks still apply before actual playback.

| Candidate | Creator | Checked duration | Planned ceiling |
| --- | --- | --- | --- |
| [Dê đen và dê trắng](https://www.youtube.com/watch?v=uWXvGSMrwXc) | Chị Kẹo Ngọt |4m7s|5min|
| [Cáo, thỏ và gà trống](https://www.youtube.com/watch?v=LIKg2CYTp4Y) | Chị Kẹo Ngọt |9m18s|10min|
| [Rùa và thỏ](https://www.youtube.com/watch?v=TXN0gMj2GZs) | Little & Little |1m36s|2min|

An explicit adult Data API diagnostic on2026-10-19 returned public/MFK/embeddable,
no region/age-rating/live restriction for these three. This is not a full content
review or parental approval; metadata may change. Parent opens each row >
Sửa / duyệt lại, watches the whole video, adjusts audience/ages/duration and ticks
both review and enable boxes. Bulk import respects the64-row cap and never
replaces existing entries. Evidence: `.agent/evidence/T-071-youtube-story-samples/`.

## Watch a pending link before approval

Open the parent YouTube library, select a saved video, then **Xem thử trong app**.
This works with pending/disabled links and automatic interleaving turned off.
Complete API/contact/privacy setup if prompted. The preview does not approve the
video, enable its link, change budgets or turn on automatic child playback.
After watching, return to the row > **Sửa / duyệt lại** and explicitly choose
review/enable settings. Active child journeys must end before parent preview.
The preview stays on the same external display; no phone/external-app fallback.
