# T-029 M0 audit — source review, 2026-10-11

Status: source audit completed; **Gradle/Android/Fold3 baseline NOT VERIFIED** in this GitHub-only session.

- KidsActivity: accepted LIVE POI cue preempts passive entertainment, stops prior narrator, increments talkEpoch. Live POI quiz is sourced from PoiDialogueCatalog and can fall back to authored base; DEMO/GPX cues are not promoted to live conversation. Passive entertainment runs every 42s, delayed after POI by 90s. A generic question uses learning-topic cache for up to 150s.
- KidsAiGateway: direct Gemini/Groq HTTPS only; 5s connect + 12s read timeout; 20 calls per gateway object, **not a shared rate limiter**; provider fallback can produce multiple physical HTTP requests. Baseline had no global 5 RPM gate; T-029 source branch subsequently added a shared in-process physical-request throttle with 429 cooldown (not yet compiled/device-tested).
- KidsAiQuizCache: local SharedPreferences, digest of source/base, 14 day TTL, 10–20 cached variants. This does not implement a fresh pack per journey.
- KidsAiSettings: legacy AI quizzes enabled by default only with provider/key/model/free acknowledgement. Independent child-text-cloud consent starts OFF. T-029 new companion opt-in must be independent and OFF by default.
- Local ASR: OnDeviceChildSpeech checks Android on-device recognition, requests vi-VN only and does not persist utterances. Local VI and EN TTS require installed non-network voices and preserve transient audio focus.
- Hazard: generic question could retain a stale currentSourcedFact from prior POI; new code must gate child-cloud sharing on current LIVE sourced POI and monotonic freshness, not just text length.
- Provenance: approved NarrationCue has source URI and lastVerifiedAt but an AI model's chosen fact index is NOT proof its question is true. Any future cloud episode must map factual answers to a strictly local reviewed fact ID; do not claim such validation implemented yet.
- Learning corpus: existing 66 authored beats and 72 quizzes/80 words stay in use. Offline first, Sâu 5/Ong 4/BOTH age 4 shared. No new CI, backend or device-owner changes.

Pending QA: run ./scripts/test-local.sh and ./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug locally; test actual Fold3 DeX (1920x1080), GPS, F10 interruption, local vi/en TTS and ASR. Record measurements before M6 closure.
