# T-015 — on-device Vietnamese child dialogue / CI evidence

Date: 2026-10-10 Asia/Ho_Chi_Minh. Implementation commit `b339e0ba58e804f7871681740c4a72a2023d51d2`. Issue: [#18](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/18).

## Verified tests
- [Android and desktop build **SUCCESS** #38003066725](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38003066725): validated T-015 privacy guards, `ChildAnswerInterpreterTest`, core unit tests, Android APK/AndroidTest build, OpenGL smoke.
- [Android API35 emulator framework/GL E2E **SUCCESS** #38003066721](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38003066721): all existing modes plus new `p0_child_mic_privacy`.
- `p0_child_mic_privacy` **PASS**: `RECORD_AUDIO` actually denied in disposable emulator, parent microphone consent defaults OFF and runtime `childSpeech` recognizer object never instantiated. No microphone recording attempted.
- `single_display_mouse` **PASS** on display ID0: one-click parent controls, pause/end, no second device display.
- `p0_hcm` and `p0_route` **PASS**: synthetic GPX and offline route display, no unconsented TTS.
- `p0_live` and `p0_live_nearby` **PASS**: real Android GPS_PROVIDER injected with synthetic geographic coordinates, >21m accepted, sourced OSM approximate POI and short Vietnamese quiz; no raw GPS logged. Tests do NOT inject real Vietnamese child voice into recognizer.
- [Screenshot/log artifact #11650107209](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38003066721/artifacts/11650107209): screenshots from actual emulator require human visual review.
- CI `p0_perf_ab`: data collection PASS; **30 FPS performance gate FAIL**, Full render avg **15.26 FPS**, P95 **97.57ms**; minimal visuals avg **21.04 FPS**, P95 **75.01ms**. 6-sec software GPU windows do not establish Fold3 real FPS.

## T-015 changes shipped as SOURCE, not field acceptance
- Added explicit microphone opt-in in ParentActivity, separate Android runtime `RECORD_AUDIO` grant, default OFF and immediate parent setting disable. Existing Vietnamese offline narrator opt-in unchanged. No Quiet mode.
- `OnDeviceChildSpeech` uses only Android API31+ **on-device** SpeechRecognizer. On API33+ it checks locally installed Vietnamese support before opening the mic. No cloud API, default recognizer or persistent recordings; recognition bounded to nine seconds and canceled on menu/pause/background/destroy/next POI.
- Local `ChildAnswerInterpreter` handles simple Vietnamese yes/no, uncertain, color/nature words, bounded canned answer feedback. It does not repeat child transcript or operate as LLM; no personal voice stored.
- LIVE GPS POI starts introductory caption/voice → quiz → potential on-device ASR (ONLY with separate consent/model) → reply → new question. Without permission/model, captions/fallback remain.

## **OPEN, NOT VERIFIED**
1. **On-device Vietnamese child speech ASR has NOT been run with actual audio**. Android emulator lacks a validated Vietnamese offline model/test child audio. CI proves static/logic/permission gating, not spoken recognition precision. Test on Samsung Z Fold3 with an installed local vi-VN service, mic permission, child speech in moving car (passenger only).
2. Local Vietnamese TTS voice actually produces audio on hardware; no ASR/TTS overlap, interruption/ducking with Maps/Vietmap. Audio focus and responsiveness not accepted.
3. No driving-route/roadway location geometry is human-verified. Current OSM starter list 13 representative POIs, sparse on five travel corridors.
4. 30 FPS Fold3 external-DeX performance, 60-min thermal/PSS, long trip GPS continuity and screen legibility.
5. Free-form open-domain conversation beyond bounded answer intents is out of scope of this offline v1; explicit independent feature and safety/quality gate.

Task status **IN_PROGRESS**. Only the `main` branch is used.
