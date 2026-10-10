# Local-only tests and compact parent UI

Updated 2026-10-10. GitHub Actions workflows **removed**; there is no CI pipeline on pushes, PRs or schedules. CI emulator shell runner removed. Historical CI links in earlier milestone documents are *archive evidence only*.

## Run locally

On a developer machine with JDK 17, Gradle wrapper and Android SDK platform 35 installed:

```bash
./scripts/test-local.sh
```

Runs Python deterministic source/content/permission contracts, then `./gradlew --no-daemon :core:test :android:testDebugUnitTest`. Does not execute connected-device or emulator instrumentation, CI, APK deployment or cloud provider requests. For only Java core: `./gradlew --no-daemon :core:test`. If dependencies have never been downloaded, Gradle may need Internet on the **developer machine** for the initial build.

## Parent screen
The entire parent home screen has **four buttons**:
1. **BẮT ĐẦU · GPS thật và Capybara trò chuyện**: request ACCESS_COARSE_LOCATION + ACCESS_FINE_LOCATION at runtime when necessary; only after location authorization, request RECORD_AUDIO if enabled and an offline ASR service exists. Denying microphone must not prevent GPS/captions. No permission can be automatically granted without Android user/system authorization.
2. **XEM THỬ · hoạt hình DEMO**: runs a safe non-GPS demo on the same external DeX display.
3. **Cài đặt · tuổi, thời gian, giọng nói, AI**: one dialog; available categories: age, session length, voice/microphone, Gemini/Groq provider keys/models and AI cache, knowledge-route selection, advanced GPX/HCMC preview tools, reset preferences.
4. **Dừng hành trình**: asks an active session to terminate.

The in-game F10 / Parents button remains as one-screen pause/controls, no phone touch, PIN or second display.

## Defaults, OS permissions and privacy
- Offline TTS preference **ON**. Device must already have a usable non-network Vietnamese TTS voice; otherwise shows captions.
- One-shot on-device speech recognition preference **ON**. Actual microphone requires separate Android runtime `RECORD_AUDIO` grant and a viable on-device Vietnamese recognizer. No passive microphone or cloud STT. If permission is denied, offline captions/questions continue.
- AI quiz preference **ON**, but provider network calls require an entered API key, selected model, provider enabled and explicit parent confirmation of Free Tier eligibility. These cannot be enabled by Android permission alone.
- Sending **child text to cloud** remains **OFF** by default, regardless of other defaults. Parent must explicitly turn it on after a disclosure. Voice audio and GPS position must never be transmitted to AI.
- Internet is a regular declared manifest permission; it is not a runtime GPS/mic permission. Location/recording are never automatically granted.
- Reset preferences returns TTS, mic preference and AI quiz preference ON, without granting Android runtime permissions, erasing stored AI credentials, or silently opting into cloud child text.

## Known unverified areas
The external Samsung Fold3 real Vietnamese ASR/TTS, Google Maps/Vietmap audio focus, precision of nearby OSM candidates and real provider/model quota/free-tier are **not** verified by static tests or local Java unit smoke. No CI claims should be made for new changes.
