# T-071 — personal Premium use through official YouTube

Owner-approved revision, 2026-10-18: this APK is used personally through ADB,
without store distribution. Owner has Premium and chose official YouTube on DeX,
accepting layout and automation changes. Test URL supplied by owner:
`https://www.youtube.com/watch?v=uWXvGSMrwXc` (test permission is not permanent
library approval).

## Behavior

Parent dashboard → Settings → YouTube. Add exact HTTPS video URL or 11-character
ID; supported URL hosts are youtube.com, www.youtube.com, m.youtube.com and
youtu.be. Tracking parameters and playlist context are discarded when a video ID
is present. Playlist-only links are rejected; no search, expansion or metadata
lookup. The explicit **Dán link/ID** button reads clipboard only when clicked, validates
locally and inserts the canonical URL without Vietnamese Telex composition.
Manual typing of a case-sensitive ID requires an English keyboard; the app never
guesses a different ID from accented input.
Parent supplies a title, audience, ages 4–7, estimated duration and up to
two recall questions, and explicitly reviews the video. Editing requires review
again. Enable/automatic switches default OFF; first enable shows a disclosure.
The catalog is local, at most 64 entries/64 KiB, validated before atomic writes.
Malformed revisions fail closed. Delete disables the feature and removes links;
it does not change Google history or account settings.

DEMO, LIVE and loaded GPX can hand off automatically, at the existing enabled five-completed-card
story boundary with no unfinished question, listening, story or POI cue. Default
ratio is one story then one video; optional three stories then one video. Disabled,
offline, no candidate, exhausted budget, missing official app or unsupported display
falls back to the ordinary story path. Eligible videos rotate using a persisted
last-ID pointer. Both children must fit the reviewed audience/age range.

Budget: at most two scheduled launch requests and 900 parent-estimated seconds
in a rolling hour. Requests count even if a launch later fails. This is not a
measurement of watch time and never cuts off a video/session. Parent previews
are explicit actions outside the automatic budget; stop the journey before preview.

Before handoff, stop local speech/listening and pause the scene/soundscape/AI
coordinator. Persist a pending ID before opening official package
`com.google.android.youtube` on the **current active external display**. Require
validated connectivity and Android display-start permission. No browser/default
phone fallback. Samsung may list a physical HDMI and a separate virtual Desktop;
inherit the Activity display rather than choosing HDMI by name. DeX/YouTube owns window sizing: no automatic 75/25 companion panel.
Actual display routing remains a device acceptance gate; app intent options alone
are not physical proof.

There is no supported playback-end/stop callback in this integration. Return via
DeX app switching or Back after pausing YouTube. The journey remains paused until
parent selects **Tôi đã dừng YouTube · Tiếp tục**. A process restart/recreation
restores this hold before local audio starts; controller RESUME cannot bypass it.
The return card optionally shows the parent's own recall prompts; it makes no
claim to have understood the video. Ending the journey leaves a durable hold
until a later explicit acknowledgement, since Premium may keep playing in background.

Owner explicitly accepts pausing DeX Kids journey/GPS/POI while YouTube is active.
Stop this app's GPS listener at handoff and restart on confirmed return. GPX is
paused on the render thread; remember whether it was running, and only resume
that state. No source POIs are promised during video. This does not disable
system GPS or change Maps/VietMap. GPS reacquisition is subject to the existing
fix-quality gates; no invented movement or arrival is emitted.

## Account and privacy boundary

The app does not embed a player, load YouTube scripts/thumbnails, invoke YouTube
Data API, log in, access Google tokens/cookies, scrape videos or store Google
credentials. Only the canonical video URL goes to the installed YouTube app;
child names/ages, responses, audio and GPS are not added to it. Google/YouTube
owns account history, recommendations, advertising and its privacy practices.
Native YouTube content/links are not restricted to the app's reviewed catalog.

Premium is managed by YouTube; sign in there with the subscribed account.
[YouTube's Premium help](https://support.google.com/youtube/answer/6308116?hl=en)
describes ad-free benefits and creator-included promotions that may remain.
[YouTube's troubleshooting guide](https://support.google.com/youtube/answer/7437519?hl=en)
explains why playback outside a signed-in context may still have ads. This app
cannot verify subscription or promise every video is free of promotions.

The original [iframe M0 NO-GO](T071_YOUTUBE_COMPLIANCE_MATRIX.md) remains applicable
to that deferred architecture. The owner-authorized handoff is implemented as a
separate integration, not labelled as passing embedded API notification gates or
legal certification. No Cloud project/key is needed for this URL handoff.

## Verification

Evidence and open gates: [.agent evidence](../.agent/evidence/T-071-official-youtube/SUMMARY.md).
Core tests cover URL restrictions, whole-catalog rejection, explicit review,
three audience modes, ratios, five-card atomicity, offline/LIVE/phone fallback,
rotation, rolling budget/clock rollback and recall limits. Android instrumentation
uses isolated storage to test AtomicFile and request journal; a separate actual
KidsActivity test checks persistent return hold/recreation/explicit release.
Live playback is tested only with the owner-supplied URL and reported separately
from catalog approval or Premium/human acceptance.
