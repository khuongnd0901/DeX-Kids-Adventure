# T-015 — Two-way Vietnamese child dialogue (on-device ASR)

Date: 2026-10-10. **Status: IN_PROGRESS (hardware/mic/ASR acceptance open).**
Issue: https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/18

## Parent flow on one external DeX monitor
- Enter Parent controls via mouse/keyboard. Enable the **separate** voice output toggle, "Bật giọng kể offline tiếng Việt" (requires a preinstalled non-network Vietnamese TTS voice).
- Explicitly click **Cho phép nghe câu trả lời của bé (offline)**; Android displays a separate runtime **RECORD_AUDIO** permission request. The setting defaults OFF. If no on-device speech recognizer exists, the app refuses to enable the feature and keeps quizzes as subtitles.
- Start **LIVE GPS**. Only a qualifying OSM candidate near stable, real GPS can start the place-specific conversation. The question is read aloud if local TTS is ready, then *after TTS completes* the app opens the microphone for at most nine seconds. It never listens continuously; only after a question. Displays "MICRO ĐANG NGHE BÉ".
- A recognized short Vietnamese answer is interpreted locally with constrained yes/no, common category/word overlap and "con không biết" rules. Capybara acknowledges the answer and reveals a sourced answer. Later it asks another open-ended question; a second short response is interpreted locally.
- When no nearby POI exists, generic questions explicitly say **KHÔNG THEO GPS**, with the same optional listen/reply flow. If parent has not enabled microphone or a Vietnamese model is absent, the previous caption-only scripted questions remain intact.
- The dashboard/game share a **single display**; no phone touch, lock screen, PIN or Quiet mode.

## Technical/privacy boundaries
- **Android 12/API31+**: only `SpeechRecognizer.createOnDeviceSpeechRecognizer()`, never default `createSpeechRecognizer()`. On API33+, `checkRecognitionSupport` must report a **locally installed Vietnamese** model before the microphone opens; if not, stop and show a fallback. Android31–32 has no support query; on-device factory only and any model/language error fails closed.
- `EXTRA_PREFER_OFFLINE=true` is an additional hint, not the security boundary, per Android documentation; the explicit on-device factory is.
- Recognition session is **one-shot, 9s maximum** and canceled on parent menu, backgrounding, app destruction, timeout and dialogue replacement; no background mic service or always-on wake-word.
- The child answer transcript exists only in an in-memory callback, normalized/truncated by a deterministic `ChildAnswerInterpreter`; neither raw PCM/audio nor recognized texts are logged, persisted or uploaded. Feedback uses bounded canned replies, not personal information derived from a transcript.
- No `INTERNET` permission is added; no STT API key, AI cloud calls, location history, personal names or raw GPS are saved. Parent can disable further listening immediately with one click.
- A child is a **passenger**, never the driver. The app does not require screen interaction when driving.
- Stale mic callbacks are invalidated by an epoch, and no listening occurs while character TTS is playing. When offline TTS is unavailable but parent has enabled the mic, questions are displayed, then brief delayed listening is still possible.

## Acceptance matrix
- [ ] Java `ChildAnswerInterpreterTest` (short Vietnamese yes/no/keywords, uncertain, no personal transcript echoed).
- [ ] Android Compile with API35, Android Test APK and single-display regression.
- [ ] Static `tools/test_child_voice_privacy.py` verifies explicit consent, on-device-only factory, no network fallback/storage/hot microphone.
- [ ] Disposable API35 emulator `p0_child_mic_privacy`: initial permission DENIED, opt-in false, recognizer NOT constructed. Existing `p0_live_nearby` still shows POI→quiz with no consented microphone.
- [ ] **Physical Samsung Fold3/DeX**: grant mic permission, confirm local **Vietnamese** ASR actually installed and accurate over road noise; test parent voice echo/double talking, lost audio focus beside Maps/Vietmap, 60-minute leak/thermal, parent Pause, navigation, microphone permission revoked mid-turn, repeated prompts.
- [ ] Optimize renderer ≥30FPS on Fold3 hardware and expand verified roadside OSM POIs (independent tasks). CI emulator screenshots alone are not hardware acceptance.

Limitations: simple constrained deterministic Vietnamese responses, **not open-domain LLM conversational understanding**. Unsupported/offline-missing recognition does not fall back to a network recognizer. If model installation is needed, install it separately on the phone through its speech service (outside app), and recheck support.
