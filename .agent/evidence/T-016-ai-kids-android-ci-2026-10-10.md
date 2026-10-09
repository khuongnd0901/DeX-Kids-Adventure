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
