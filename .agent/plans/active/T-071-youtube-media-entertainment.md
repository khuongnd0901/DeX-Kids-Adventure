# T-071 — YouTube Media Entertainment: approved plan for Codex

Date: 2026-10-18. Owner: Main Agent (Android/DeX + Story/Core + QA with separate scopes).
Status: **IN_PROGRESS — official-app source/device slice verified; vehicle acceptance NOT VERIFIED**. Source branch: **main** per main-only repo decision.
Tracking: [Issue #65](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/65).
Dependencies: T-068 question and interlude clock, T-022/023 EntertainmentDirector, T-024/025 TTS/audio, StorySessionController, existing display/GPS rules.

## Verification checkpoint — 2026-10-18

| Revised milestone | Result | Evidence / remaining gate |
| --- | --- | --- |
| M0 scope/notice | IMPLEMENTED | Owner explicitly chooses personal official-app/Premium route; default-OFF parent disclosure. Embedded M0 remains deferred, no legal certification claim. |
| M1 official app/display/lifecycle | VERIFIED bounded slice | Physical Desktop2 screenshots, owner confirms video/audio/no ads seen in trial; persistent hold/recreation and no controller bypass. |
| M2 local library | IMPLEMENTED / storage verified | Bounded core codec and real Android AtomicFile/settings/journal tests; native parent controls on physical Desktop2. English keyboard exact-ID native save/readback and explicit clipboard paste verified; QA row removed. |
| M3 five-card selection | VERIFIED technical slice | 11 media unit tests; actual production scheduler/launcher at a synthetic five-card boundary, request/rotation persisted; full natural five-answer/media run not claimed. |
| M4 QA/field | IN_PROGRESS | 276 core tests, app/testAPK/desktop build and local contracts; actual LIVE listener and synthetic GPX pause restoration; physical official playback + acknowledged return. Vehicle Maps/VietMap audio/moving-road/endurance NOT VERIFIED: owner has no car available. |

Owner separately requests then defers T-074 multi-audio at Samsung login. Do not
enable it or remove T-071's return audio hold. No permanent test catalog approval
or automatic enablement retained; test fixture/settings restored.
Evidence `.agent/evidence/T-071-official-youtube/SUMMARY.md`, verification.json.
Plan stays active; no overall DONE or original embedded-compliance PASS claim.

## Latest owner steering — LIVE/GPX allowed with explicit pause (2026-10-18)

Owner selects: video allowed in LIVE/GPX; pause this app's journey/GPS/POI during
YouTube, resume after parent confirms stopped. This supersedes the provisional
DEMO-only restriction below. System GPS and Maps/VietMap are unchanged. Pause
GPX via its render-thread setter; remember whether it was running and restore
only that state. Do not promise source POIs while YouTube is foreground.
Physical display audit: API enumeration listed HDMI logical6 but physical HDMI
continued rendering Samsung Desktop logical2. The first display6 launch was NOT
a physical display PASS; retain the failure and reverify on actual desktop2.
Always inherit the current Activity's valid active external display, including
Samsung's virtual Desktop; never select a display by HDMI name or hardcoded ID.

## Revised scope approved by owner (2026-10-18, supersedes embedded implementation below)

Owner confirms personal/family use only, APK built/installed through ADB, no store
or third-party distribution. Owner has YouTube Premium and selects official
YouTube on DeX, accepting layout and automatic-turn adjustments to real platform
capabilities. No YouTube Data API project/key exists. Do not provision one for
this route: the app hands a canonical parent-approved video URL to installed
`com.google.android.youtube`; it does not embed a player, query YouTube APIs,
search, scrape, log in, share cookies or collect Google credentials.

The embedded M0 NO-GO remains valid for the deferred iframe design. This revision
does not claim registration/legal certification; official YouTube owns its player,
login, Premium entitlement, recommendations, ads and playback history.

Revised measurable milestones:
- M0: record scope and show parent disclosure before enabling local handoff.
- M1: explicit official-package launch on current external display; no browser/phone
  fallback; stop local audio/listening before launch. YouTube cannot report end or
  accept a stop command through this integration. On return, hold the journey until
  parent confirms YouTube is stopped. Layout is managed by DeX, no promised 75/25
  or simultaneous Capybara. Physical verification required. LIVE/GPX automatic
  handoff disabled until a verified GPS-safe multiwindow design exists; preserve
  original GPS/navigation lifecycle. DEMO is the bounded first implementation.
- M2: bounded atomic local video library, manual review/ages/audience, default OFF,
  no playlist expansion/network. Optional playlist import deferred with API route.
- M3: deterministic five-question story/video scheduling in DEMO, configurable
  ratios OFF/1 video per 2 or 4 interludes, approved-video rotation, persistent
  cursor, bounded planned budget; unavailable/unsupported mode -> offline story.
- M4: optional parent-authored recall prompts on confirmed return, local builds/
  tests, actual Premium/device/display/audio return and human acceptance.

No claim of automatic video completion, forced official-app stopping, Premium
guarantee, LIVE continuity, or physical acceptance without observed evidence.
ADB currently has no devices; parent asked for connection and approved test URL.

## Original embedded user-approved product decisions (2026-10-18; supersede earlier draft)
1. **1A — Curated-only/manual input.** Parent alone adds YouTube video links or playlist URLs. No AI YouTube search/discovery, no autonomous suggestions/recommendations, and never bypass parent approval.
2. **2B — Split screen.** YouTube player uses roughly **70–80% of screen width**, Capybara stays visible/animated in the remaining **20–30%**. Areas are disjoint; **NO HUD, overlay, character or control may cover ANY part of the YouTube player**. Apply responsive sizing so actual embedded viewport and native player controls remain compliant; prioritize legibility and performance. If viable split layout cannot be achieved, record BLOCKED and do NOT silently switch to fullscreen.
3. **3A — Automatic playback at scheduled turns**, after opt-in and eligibility checks; no child touches. Comply with YouTube visible-player/autoplay requirements and Android/WebView restrictions. If autoplay blocked, do not simulate touch/circumvent rules; keep app responsive, report skipped/blocked and resume offline interlude. Parent may manually retry.
4. **4C — Parent-configurable interleaving ratio**, default to **4A pattern**: 5 completed questions/English words -> one approved/offline story -> 5 questions -> one approved YouTube video (if eligible) -> normal quiz. Parent may configure how frequently video vs stories appear, but T-068 5-question base cadence stays authoritative and POI/question/story atomicity is preserved. No adventure duration limit.
5. **5A — YouTube audio story with video player VISIBLE** in its standard split-screen area; do not split/mute video to create audio-only mode. **Do not build a separately licensed audio provider/Media3 feature** in T-071. In the existing app Audio-only setting, show the embedded video if explicitly authorized or skip YouTube; decide the exact UX and test it, never play YouTube in hidden/background mode.

## Goals, constraints and operating context
Sâu 5, Ong 4, default audience BOTH, ride in back seats, a single non-touch Samsung DeX 1920x1080 external monitor. Enrich existing learning/AI stories/POI with selected cartoons, Vietnamese narrated stories, English nursery and educational videos. Default online YouTube feature OFF until parent consents and enables it; the journey runs normally offline without new keys or service. Do not add backend or CI.

Protect existing audio focus and navigation alerts (Maps/VietMap), GPS/POI correctness, existing on-device child ASR and TTS, Sâu/Ong costumes and expressive Capybara, unlimited session (parent ends it), F10/menu, same external DeX display; never redirect child to phone. No upload of live GPS, child audio, child identity or child replies to YouTube; embedded player will inherently communicate with Google — disclose appropriately. Do not modify DeX-Assistant repositories, Device Owner or Accessibility. No platform copyright bypass, MP3 extraction, background/hidden video, ad suppression, overlay obstruction, endscreen hacks, alternate YouTube video clients or unofficial stream URLs.

## Existing implementation touchpoints (audit before editing)
- `android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt`: LibGDX AndroidApplication; `questionsSinceStory`, `playEntertainment`, `storySession`, `talkEpoch`, live GPS, audio focus and activity lifecycle.
- `android/src/main/java/com/khuongnd/dexkids/StorySessionController.kt`: checkpoint/pause/resume/stop of stories; don't resume twice.
- `android/src/main/java/com/khuongnd/dexkids/ParentActivity.kt` + `ParentSettings.kt`: age, audience, parent controls. T-068 external learning-source settings are independent.
- `android/src/main/java/com/khuongnd/dexkids/OfflineVietnameseNarrator.kt`, `KidSoundscape.kt`: audio ownership/soundscape; avoid overlapping TTS/YouTube/nav.
- `android/src/main/java/com/khuongnd/dexkids/DisplayRouter.kt`: same external screen, no phone fallback.
- `core/src/main/java/com/khuongnd/dexkids/story/EntertainmentDirector.java`: pure story/quiz selection. Prefer additive media decision model, not Android APIs in pure core.
- `android/build.gradle.kts`: compileSdk35, minSdk30, targetSdk35; Kotlin/Java17 and LibGDX1.14.2. Avoid unrelated dependency upgrades.

## M0 — Mandatory YouTube/child-directed compliance gate (P0, do FIRST)
- Review **current** official API Terms/Developer Policies, embeddable-player requirements, child-directed app self-designation/notification, COPPA/other applicable privacy rules, tracking for Made for Kids, privacy disclosure and parent consent. Implement an explicit parent notice before any YouTube WebView is loaded; don't claim a child-directed player is compliant solely because a video is labeled MFK.
- Validate each candidate via the official YouTube Data API (videos.list for status.madeForKids, status.embeddable, contentDetails, restrictions and metadata) or current official equivalent. Document API key/quota and eligibility; missing keys/status, restricted, removed, age-inappropriate, non-embeddable, network failure => fail closed/offline. MFK != safe content review. Approved catalogs remain strictly manual. No arbitrary scraping or account login.
- OS Android WebView + official YouTube IFrame Player API; evaluate maintained wrapper such as android-youtube-player only if it respects up-to-date policies and has working source/identity on device. Provide correct app identity / referer/origin where required. No insecure JavaScript bridge or third-party browsing.
- Minimum player size 200x200; aim >=480x270 (16:9); player >50% visible at autoplay initiation; exactly one auto-playing player; no overlays at all on player. Native YouTube controls, ads, attribution and link handling must remain intact.
- Child-directed embedded playback requires self-designation and correct privacy handling. Autoplay may collect data at load and may be blocked by WebView even if allowed by YouTube. No deceptive silent-start workaround. Test with a parent-approved known embeddable MFK video and document privacy implications.
- **Gate M0 output**: `docs/T071_YOUTUBE_COMPLIANCE_MATRIX.md` with linked, dated authoritative requirements, decisions, GO/NO-GO and parent-facing notice; if unresolved STOP before shipping YouTube playback.

Official references:
- https://developers.google.com/youtube/terms/developer-policies
- https://developers.google.com/youtube/terms/required-minimum-functionality
- https://developers.google.com/youtube/iframe_api_reference
- https://developers.google.com/youtube/data/guides/made_for_kids_status
- https://developers.google.com/youtube/data/reference/rest/v3/videos
- https://support.google.com/youtube/answer/171780

## M1 — Split-screen IFrame player POC with Capybara (P0)
1. Develop Android `MediaPlaybackHost` (candidate: a single overlay container inside KidsActivity or controlled Activity on SAME DeX display). Run a comparative lifecycle POC: a separate Activity currently causes KidsActivity.onPause to stop GPS; decide the approach using real behavior, not guesswork.
2. Layout: at FullHD width 1920, allocate about **1344–1536 px** (70–80%) to a fully visible **16:9 YouTube WebView** and the rest to a non-overlapping Capybara/child-friendly animated side panel; fit player safely within vertical bounds (e.g. centered with padding) and native YouTube controls unobstructed. On aspect/density changes, preserve >=200x200 player and player-visible condition; no overlapping translucent HUD, ad cover, animated companion hands or captions on top of video.
3. Prefer cost-bounded animation on sidebar: reuse existing Capybara pose/animation assets, minimal render workload. Keep GPS available but do not narrate over video; avoid two independent active GL/Android video surfaces causing memory/FPS regression. Record actual performance vs current app on Fold3/DeX.
4. Restrict imported video/playlist URLs to canonical YouTube IDs; no untrusted JS or redirects; validate metadata and parental whitelist before opening. Player state IDLE->VALIDATING->READY->PLAYING/BUFFERING/PAUSED->ENDED/ERROR/AUTOPLAY_BLOCKED->RESTORING->IDLE. Generation token + single owner prevents double completion, stale callbacks or leaked sound.
5. At scheduled start: pause TTS/ASR/AI/question clocks, pause/duck soundscape, preserve story/quiz counters, safely freeze main story stage as appropriate but continue lightweight Capybara sidebar. Maintain navigation audio focus and do not mutate global volume.
6. At end/error/skip/parent F10/back/background/display change: stop/dispose WebView/player, reconcile pause/resume once, restore game/scene/GPS/parent controls, then continue T-068 flow. No Youtube audio when screen hidden. If `onAutoplayBlocked` or no active network, skip to offline story/quiz with local note rather than hanging.
7. Real SM-F926B/API35 external DeX FullHD smoke matrix: successful parent-approved video, adjacent animated Capybara, 70–80% player, policy UI/ads, autoplay (when permitted), blocked autoplay, load error/404/unembeddable, network lost, return, GPS, TTS and Maps/VietMap audio arbitration, F10/end, reopen, repeated player creation, actual PSS/FPS. **No device PASS claims until observed.**

M1 acceptance: functioning legal/visible YouTube 70–80% + Capybara sidebar on same DeX display; game returns exactly once. If platform blocks required autoplay, ship documented safe fallback and mark autoplay gate NOT VERIFIED/FAILED (do not fake success).

## M2 — Parent-curated Media Library, no AI discovery (P0)
- Parent > Cài đặt > Thư viện YouTube: add single YouTube link/video ID; optional playlist URL import lists candidate videos needing individual parent approval; edit/enable/disable/delete, approve audience SAU/ONG/BOTH and age range, category, title/channel, manual educational takeaway and optional review questions. No search recommendations from AI or YouTube.
- Bounded catalog fields: stable ID, videoId, metadata(title/channel/duration/madeForKids/embeddable/lastValidatedAt), category, approved audience/ages, enabled, approvedByParent, autoEligible, review time and optional parent-written questions. Keep local only; atomic import + validation/last-good recovery and dedupe; avoid storing child identifiers/GPS or sending data beyond video ID for metadata.
- Optional playlist expansion **only as parent-provided links**, not open internet recommendations. Fail closed on Data API quota, missing MFK state, stale eligibility or restricted videos.
- Toggle YouTube enable OFF by default, auto-interleave OFF until parent chooses ON; settings for interleave ratio as a bounded, comprehensible control, not per-frame random trigger.
- Existing Audio-only mode: only play YouTube when full visible player explicitly allowed by parent; otherwise skip to offline audio storytelling. No YouTube audio extraction.
M2 acceptance: only manually-approved eligible videos ever play; settings survive restart and preserve existing age/learning-story settings.

## M3 — Configurable interleaving and priority (P1)
- Add deterministic `ContentOrchestrator` / `MediaDirector` pure decision layer: inputs completed cards, T-068 five-question boundary, story phase, active POI, network, media lease, parent enable/ratio, eligibility/audience ages, recent play history and budget.
- Default (**1A/4C**): block #1 5 completed QUIZ/WORD -> story; block #2 5 completed QUIZ/WORD -> approved video; repeat only when eligible. If video unavailable/blocked, fall back to story/quiz. Do not start YouTube in middle of prompt/listen/answer/story nor interrupt source-backed POI. Existing story interleaving remains ON.
- Parent may choose ratio via intuitive settings such as 'mỗi N lượt xen kẽ chọn 1 video, còn lại là truyện', including OFF and 1:1 default. Scheduler must be deterministic, restore after process recreation, round-robin eligible/approved IDs and avoid repeated video until all eligible candidates used; no cloud AI selection.
- Proposed sane default insertion caps <=2 items/hour, <=15 minutes of planned videos/hour, configurable by parents; *eligibility budget* only and **not** adventure session duration cutoff. Do not abruptly terminate active YouTube playback or hide it to satisfy quota; report state.
- Do not change T-068 3–5 s answering/2–5 s transition, question uniqueness or English speech order. Protect `talkEpoch`, current story cursor, TTS timers and no duplicate media end callbacks.
M3 acceptance: pure unit test suite covers parent ratios including 0/1:1/1:3, unavailable or outdated MFK metadata, blocked autoplay, video repeat avoidance, 5-question boundaries, active POI or story, stale callback, offline/recreation and full audience SAU/ONG/BOTH.

## M4 — Optional parent-reviewed post-video questions and end-to-end QA (P1)
- 1–2 Capybara recall questions **only from parent-reviewed metadata/synopsis**, never pretend that title/thumbnail means full transcript understood. Parent may leave none, in which case resume quiz directly. This user chose NO AI video discovery; AI should not scrape video transcripts or create undocumented video facts.
- User controls on SAME external display: F10/Back/parent menu, visible native YouTube controls, optional *existing* authorized DeX-Assistant IPC only within this repo if already supported. Do not add separate gestures/hardware.
- Local-only telemetry: video IDs, count started/ended/skipped/errors, cooldown, ratio step and last player state; do not upload identity, behavior or child audio.
- Local builds/tests (no CI): `./scripts/test-local.sh`; `./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug :android:assembleDebugAndroidTest`; desktop build command from current repo. Preserve and link raw evidence, real APK hash and screenshot/video test receipts. Never label Fold3/DeX proof from simulator.
- Field acceptance: F10, same external display, Full/Audio-only, story/quiz/AI cadence, both audiences, privacy, livestream GPS correctness, Google Maps/VietMap audible alerts, screen/no-touch autoplay and real child-safe parent content approval. Driver should not handle media while driving; passenger-only assessment.
M4 DONE only when source + policy + local tests + actual Z Fold3 DeX and human acceptance meet all gates. Else IN_PROGRESS/NOT VERIFIED.

## Implementation sequence for Codex
1. Read `AGENTS.md`, `.agent/STATUS.md`, `.agent/TASKS.md`, `.agent/DECISIONS.md`, this plan and `docs/T071_YOUTUBE_MEDIA_ARCHITECTURE.md`.
2. M0 compliance/design GO/NO-GO first; then M1 isolated policy-compliant split-screen POC, M2 parent catalog, M3 orchestrator, M4 QA.
3. Each milestone: source + focused tests + real evidence; update plan/TASKS/STATUS/DECISIONS, no CI. Existing unrelated work/branches preserved; main-only ordinary fast-forward push, check origin/main and do not force.
4. Never equate documentation merge with runtime implementation. Report policy/autoplay blockers honestly; don't change approved choices without recording a follow-up decision.

## Log
2026-10-18 — 5 user selections confirmed (1A, 2B, 3A, 4C, 5A). Plan and tracking approved to be written to main. No application runtime code, external DeX acceptance, legal signoff or automated tests performed in this planning change.

2026-10-18 — Main executed M0 research against current official sources.
Matrix: `docs/T071_YOUTUBE_COMPLIANCE_MATRIX.md`. NO-GO: child-directed
notification route for sideloaded Android unresolved; no notification receipt,
YouTube privacy/consent implementation or verified metadata project/test video.
Native related-video/link behavior also needs reconciliation with reviewed-only
UX (`rel=0` does not remove related videos). Per M0 stop rule and issue #65,
M1–M4 not started. Research deliverable complete; compliance acceptance NOT PASS.
Evidence `.agent/evidence/T-071-youtube-m0/`; existing runtime unchanged.
