# T-071 — YouTube Entertainment / Split-screen design (Sâu + Ong)

**Design decision confirmed 2026-10-18:** 1A / 2B / 3A / 4C / 5A. **Not implemented.**
ExecPlan: [T-071](../.agent/plans/active/T-071-youtube-media-entertainment.md). Tracking: [Issue #65](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/65).

## User experience
- Parents paste exact YouTube URLs or playlists into a private approved library. Imported playlist entries are *candidates*; parents individually approve videos. NO automatic AI discovery, YouTube search, AI-generated video recommendations or random unapproved content.
- When selected, YouTube occupies **70–80% of display width**; Capybara remains visible and lightly animated in **20–30% adjacent area**, with **strictly non-overlapping** native YouTube iframe/controls/ads. 1920×1080 sample: 1440px left for the YouTube player, 480px right for Capybara and small status/parent controls OUTSIDE player bounds. Actual player remains 16:9 or native aspect; side panel never appears on top of it. Scale after actual density/DeX inspection.
- Narrated YouTube content always uses the **visible standard video player** and its own normal sound, never a hidden/mini/background sound-only workaround. No dedicated new Media3/MP3 module.
- After a safe 5-question boundary, app alternates story/video according to a **parent-configured story:video ratio** (default story then video on successive boundaries). A video is **auto-started** only when allowed by platform + policy + explicit parent opt-in; if blocked, resume normal offline story/quiz instead of simulating user interaction. Parent can manually start or skip through normal input.
- Source-backed GPS/POI, T-068 question wait (3–5sec), English model/answer order and story cursor take precedence. Session never ends automatically, only parent can stop.

## 1920×1080 layout example

    +--------------------------------------------------+------------------+
    |            YOUTUBE: 1440 x 810 (16:9)            |    CAPYBARA      |
    |   centered vertically inside left 1440 x1080    |  animated panel  |
    |   STANDARD PLAYER + ADS + NATIVE CONTROLS        |  480 x1080       |
    |                                                  | Parent controls  |
    +--------------------------------------------------+------------------+

The side panel is a sibling view, **not** a WebView overlay/transparent cover. Keep YouTube minimum player viewport >=200x200 and controls visible (recommend >=480x270), >50% player visible before initiating permitted autoplay. If a child-side panel causes performance issues, lower side animation rate or use still sprite rather than changing the 2B layout without recording evidence/decision.

## System decomposition

    ParentActivity / ParentSettings
       └── MediaLibraryActivity (manual YouTube URL/playlist input, approve)
              └── MediaCatalogRepository (local + bounded, last-good)
                     └── YouTubeMetadataVerifier (MFK/embeddable/quota)
    KidsActivity (LibGDX/GPS/TTS lifecycle)
       └── ContentOrchestrator (pure Java priority/safe boundaries)
              └── Android MediaSessionCoordinator (single player lease)
                     ├── YouTubePlayerHost (official IFrame API in OS WebView)
                     └── CapybaraSidePanel (existing art, lightweight animation)
    StorySessionController / EntertainmentDirector (unchanged owners)

State transitions: IDLE -> VALIDATING -> READY -> PLAYING | AUTOPLAY_BLOCKED | ERROR -> RESTORING -> IDLE. On each transition use generation/cancellation tokens, atomic media owner and main-thread callbacks to prevent a callback from stale WebView resuming story twice.

## Open technical gates (not user-facing preference questions)
1. Need policy GO/NO-GO on child-directed app declaration/self-designation, parental disclosure/consent, YouTube player restrictions, Made for Kids statuses, identity/referer/origin and allowed data processing.
2. Confirm real WebView autoplay restrictions on Fold3/DeX; no fake 'hands free' guarantees. If blocked, skip video or allow an explicit parent start action, while keeping requested auto-play preference as best effort.
3. Decide overlay/sibling-host vs extra Activity only after comparing actual Android lifecycle: KidsActivity.onPause currently stops live GPS, audio and tasks; don't lose POI updates or question/story state during video.
4. Measure how simultaneous Android WebView video and animated GL companion affect Fold3 CPU/GPU/PSS/FPS/audio. Navigation announcements must stay audible.
5. Explicit parent approval per video/playlist member, ensure no unreviewed recommendations or channel/related autoplay, default feature OFF. No YouTube Data API key in source/git.

## Acceptance at each phase
M0 legal+privacy matrix and official source links; M1 genuine video at 70–80% next to Capybara on DeX and fully visible YouTube UI with correct fallback; M2 durable manual library; M3 deterministic parent ratio and five-question story consistency; M4 runtime device & human acceptance with no audio leaks or FPS regressions. Missing evidence => NOT VERIFIED, not DONE.

## Official policy references (review again at implementation time)
- https://developers.google.com/youtube/terms/developer-policies
- https://developers.google.com/youtube/terms/required-minimum-functionality
- https://developers.google.com/youtube/iframe_api_reference
- https://developers.google.com/youtube/data/reference/rest/v3/videos
- https://support.google.com/youtube/answer/171780
