## Final code CI confirmation (source 3a7e0477)

## T-016 latest source acceptance (2026-10-10)
- Last code source SHA `3a7e0477e0a9d13cd99fb59691ce5319eeab040b`. [Build/unit CI **SUCCESS** #38005236008](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38005236008) and [Android API35 emulator E2E **SUCCESS** #38005236039](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38005236039).
- **PASS:** `p0_ai_cache` (ten private offline quiz cards, age and source-digest separation, AI and child cloud consent OFF by default, no HTTP), `p0_child_mic_privacy`, real Android emulator GPS→source-checked approximate POI→quiz, single-screen mouse/F10, route/GPX regression.
- Real API provider calls **NOT TESTED** (no key/model available); real Fold3/DeX child audio/geolocation still NOT VERIFIED. AI-generated drafts require adult source/fact review.
- Emulator short GL metric fails 30fps: Full **14.15 FPS**, P95 **114.63ms**; minimal **19.58 FPS**, P95 **81.76ms** (not Fold3 performance).
- [Android screenshot and log artifact #11650724200](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38005236039/artifacts/11650724200); `T-016` remains **IN_PROGRESS** / #19.

# T-016 — AI Kids Conversation: build/emulator evidence

Timestamp: 2026-10-10, Asia/Ho_Chi_Minh.
Source commits: [71ac3950](https://github.com/khuongnd0901/DeX-Kids-Adventure/commit/71ac395078effadbc71bf04315bf06181c8a40b8), [3a7e0477](https://github.com/khuongnd0901/DeX-Kids-Adventure/commit/3a7e0477e0a9d13cd99fb59691ce5319eeab040b).
Issue: [T-016 #19](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/19).

## Automated, VERIFIED on code commit 71ac3950
- [Android/desktop build SUCCESS #38004929328](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38004929328): static `tools/test_kids_ai.py`, amended `tools/test_child_voice_privacy.py`, build and unit tests, Android debug/test APK build, desktop OpenGL smoke.
- [Android emulator API35 E2E SUCCESS #38004929284](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38004929284):
  - `p0_ai_cache` PASS: app AI OFF by default and child-cloud-text sharing OFF by default; personal-looking child text blocked; exactly ten dummy questions stored offline, correctly selected, age- and fact-source-separated, cleared; **NO live HTTP**.
  - `p0_child_mic_privacy` PASS: no permission and no recognizer until consent.
  - `single_display_mouse`, `p0_hcm`, `p0_route`, `p0_live` and `p0_live_nearby` PASS: same-screen mouse/F10, synthetic GPX, verified-provider Android GPS injection, sourced OSM proximity → Vietnamese quiz.
  - Short emulator GL performance FAIL: full render **14.87 FPS, P95 107.45ms**; minimal render **21.64 FPS, P95 67.82ms**. These are virtual software-GPU short windows, NOT physical Fold3 results.
- Additional source update on 3a7e0477: F10 parent menu can immediately disable AI Kids and child cloud-text opt-in, preserving a one-monitor non-touchscreen UI. This is a source change; report its new CI separately before claiming it passed.

## NOT VERIFIED / open acceptance gates
- No real Gemini or Groq key/account/model available in CI. HTTPS requests, model JSON ability, claimed Free Tier eligibility, rate limits, model hallucination prevention and real per-turn latency have NOT been measured. Acknowledgment in UI is NOT proof a model is free.
- The questions are **AI drafts** (only their answer text is constrained to an existing sourced answer/factIndex). Such a constraint does not guarantee a model's phrasing matches its answer; adult editorial review before letting children use the drafts is recommended.
- NO raw audio or GPS is sent to cloud; optionally user-consented child **text** MAY be sent, with imperfect local PII screening. Provider may retain/process the text per its terms; users should review current provider policy.
- Real Fold3 external-only DeX, child Vietnamese offline ASR in car noise, offline TTS voice, Maps/Vietmap sound ducking, full road POI geometry, 30FPS thermal/long trip NOT VERIFIED.
- Only **13** sourced OSM **candidate** POIs so far, not roadside field-approved; production reviewed.tsv remains empty.

Task T-016 **IN_PROGRESS**, not production-release accepted.
