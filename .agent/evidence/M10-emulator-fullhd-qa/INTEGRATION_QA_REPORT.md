# T-044 integrated QA — executed, acceptance remains open

All executions target Windows API35 x86_64 emulator display0,1920×1080 density240, SwiftShader. Legacy instrumentation labels saying external/Physical DeX describe the original runner, **not this device**. This task does not accept physical DeX.

| Milestone | Current status | Evidence / remaining gate |
|---|---|---|
| M0 | PASS | Latest main1b9f962, local stash restored/retained, Gemini config audited safely, emulator verified, official model metadata200. |
| M1 | PASS build/core/contracts |167 core tests; Android unit NO-SOURCE; debug/test APK install; real atlas loader; final release-slice logs. |
| M2 | FAIL quality / PASS plumbing | Real generation/review/accepted16 + rejection/retry; reviewer false-negative; Real ages4/5/7 tested;6 only synthetic. Broader editorial gates remain open. |
| M3 | FAIL M10 completeness / PASS verified UI slice |12 long-text age/audience variants+14 cues/11 transitions,26 screenshots; F10/Continue/stop, no-PIN/mouse, GPX/HCMC recreation pass.80 missing assets, only19 props statically reachable, unwired profiles/events. |
| M4 | FAIL10/30/60m FPS reference; functional lifetime PASS |10m actual600s:44.04FPS/P95upper29ms, zero AI/crash, real-cache Audio-only3 resolutions. Separate30m completed1,800.67s,26 introductions/22 conversations/F10/active lifetime PASS;60m completed3,600s,40.78FPS/P95upper30ms,86 resolutions,appactiveuntilcleanup. |
| M5 | PASS verified fixes/regression; acceptance OPEN | Verified worker/optics/prop/cue QA fixes pushed in f72eed9, content guard/reports in b8fddb0; Audio-only all3audiences PASS; fake5xx/recreation PASS; GPS LIVE PASS after enabling AVD GPS; final content guard167core/local/build PASS; final cached30s smoke played one resolution,FAIL FPS only; credential cleanup/scan PASS. |

Functional emulator results:creative cache/coordinator, Activity recreation, parent controls/no-PIN, single-display mouse + explicit stop to dashboard, GPX secure parser and pause/resume/restart/rate/recreation, source-backed HCMC preview, audience picker, long-text/cue matrix PASS. Initial LIVE failed because AVD hw.gps=no even after grants. Reversible hw.gps=yes cold restart: injected existing GPX fixture accepted,live distance8.05m,accuracy5m (synthetic),PASS. p0_ai_cache PASS after isolated offline settings. Audio bounded-wait regression all3audiences PASS,6loaded samples,TTS duck/F10/Continue/shutdown/media volume preserved.

10-minute cached real-AI window:600 actual seconds,60 display checks,1 reviewed pack,6 observed creative beats,14 total resolutions,3 Audio-only resolutions, F10/Continue PASS, no unavailable voice. Provider disabled throughout. Overall harness FAIL is performance44.04FPS/P95upper29ms; functional sub-results are retained.

Scope distinctions:Core/Python contracts use mocks/synthetic fixtures. Real Gemini was invoked through production Android gateway. FullHD UI was rendered/captured on the emulator. POI30m is synthetic downstream source-backed replay, not actual driving/road-match/Android LiveFixGate validation. GPS injection passed; physical Samsung DeX remains a separate unverified gate. No CI modifications, other repository access, actual child recordings, or GPS sent to provider.
