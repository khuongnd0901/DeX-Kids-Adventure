## T-024 / T-025 — Audio experience & child QA (2026-10-10)

**For Sâu age 4 and Ong age 3**, cartoon cues (bird, cat, rabbit, bus, chime) are generated offline and played at low volume. Settings → Voice/microphone lets parents toggle effects (default ON) and very quiet ambient music (default OFF for navigation compatibility). Capybara offline Vietnamese speech automatically pauses the app's ambient loop; F10/background stops audio, and navigation audio focus loss cancels stale TTS without starting child listening. No external recording, audio assets or new permissions.

F10 Parent controls show in-memory counts of the app's Sâu/Ong/BOTH activities, completions and interruption/fallbacks. **These counts do not measure whether the children enjoyed watching.** A real passenger-observer QA plan and sound matrix are in [T-024/T-025 guide](docs/T024_T025_AUDIO_CHILD_QA.md). Source integrated; run `./scripts/test-local.sh` and `./gradlew :android:assembleDebug` locally and verify on Fold3. Full build, Google Maps/VietMap mixing and Sâu/Ong observations remain unverified. [T-024](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/24) / [T-025](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/25). No CI.

## T-022 / T-023 — Sâu & Ong entertainment (2026-10-10)

For 3–4-year-old passengers on the same external Samsung DeX screen, choose **Cho Sâu (4 tuổi)**, **Cho Ong (3 tuổi)**, or **Cả Sâu và Ong cùng xem (3–4 tuổi)** (default) on the Parent dashboard. An offline `EntertainmentDirector` provides 48 short authored fantasy beats, balanced by child, with child-sized Vietnamese captions, optional on-device narration and Capybara wave/surprise gestures. Events work without GPS/POI, keys or Internet; GPS source-backed POIs remain higher priority. No additional child mic/cloud consent or CI. **Source implemented; new local Gradle and Fold3 QA still required.** Run `./scripts/test-local.sh` and `./gradlew :android:assembleDebug` locally. Details: [Two Kids guide](docs/T022_T023_TWO_KIDS_ENTERTAINMENT.md), [T-022](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/22), [T-023](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/23).

## T-018 — Tối ưu FPS LibGDX FullHD trên Samsung DeX (2026-10-10)

Đã tối ưu source render: gộp ShapeRenderer vào **một SpriteBatch**, thay hình tròn vẽ lại mỗi frame bằng một texture nhỏ tái sử dụng, chỉ vẽ **4 chunk nhìn thấy** thay vì 6; WorldWindow dùng cache ring array và bỏ công việc chuẩn bị ở các frame chưa thay chunk; chỉ xác định mood theo giờ mỗi phút; Android dùng RGB565/no depth/stencil/MSAA khi EGL hỗ trợ; log `sprite_draw_calls_prev`. Giữ nguyên GPS thật, POI, animation xe/Capybara, AI, on-device ASR/TTS, Parent F10, chỉ một màn hình DeX.

**Không có CI.** Chạy `./scripts/test-local.sh` trên source checkout với Gradle/Android SDK. Các test mới `WorldWindowTest`, `SceneryLayoutTest`, `tools/test_render_fast_path.py`. JVM harness tách riêng đã chạy trong môi trường hiện tại: **579 assertions PASS** cho ring cache và viewport geometry; **không phải** full Gradle build/test. Không thể tải Gradle hoặc clone repo tại môi trường này, nên phải chạy full unit tests trên máy local có checkout.

**Chưa đo FPS thực tế sau sửa trên Fold3.** Baseline cũ trên Android emulator (không phải thiết bị thật): full **14.15 FPS**, minimal **19.58 FPS**. Không được gọi đây là FPS sau tối ưu. Chi tiết: [T-018](docs/T018_FPS_OPTIMIZATION.md), [issue #21](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/21).

## Local-only development · compact Parent Dashboard (2026-10-10)

**GitHub Actions CI removed.** No workflow is triggered by a push/PR. Run unit and static contract tests on the developer machine only: `./scripts/test-local.sh` (or `./scripts/verify.sh`). It runs Python contracts and `./gradlew --no-daemon :core:test :android:testDebugUnitTest`; no Android emulator/cloud request/device required for this command. Earlier CI reports below are historical evidence, not current verification.

Parent home screen now has **four buttons**: **BẮT ĐẦU · GPS thật và Capybara trò chuyện**, **XEM THỬ · hoạt hình DEMO**, **Cài đặt · tuổi, thời gian, giọng nói, AI**, **Dừng hành trình**. Route selection, GPX replay, HCMC preview, microphone controls and Gemini/Groq API credentials are in Cài đặt. Same external DeX screen and F10 continue working.

Defaults: **offline voice ON**, **offline on-device microphone feature ON**, **AI Kids quiz generation ON when configured**. The app requests **Android location and microphone permission** as needed at first live launch; OS grants **cannot** be silently enabled. Missing language models/key/quota fall back to offline subtitles/questions. **Child transcript-to-cloud consent stays OFF** and separate from the feature defaults. No child audio/GPS uploaded. Details: [local test and menu guide](docs/LOCAL_TEST_PARENT_MENU.md).

## T-016 latest source acceptance (2026-10-10)
- Last code source SHA `3a7e0477e0a9d13cd99fb59691ce5319eeab040b`. [Build/unit CI **SUCCESS** #38005236008](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38005236008) and [Android API35 emulator E2E **SUCCESS** #38005236039](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38005236039).
- **PASS:** `p0_ai_cache` (ten private offline quiz cards, age and source-digest separation, AI and child cloud consent OFF by default, no HTTP), `p0_child_mic_privacy`, real Android emulator GPS→source-checked approximate POI→quiz, single-screen mouse/F10, route/GPX regression.
- Real API provider calls **NOT TESTED** (no key/model available); real Fold3/DeX child audio/geolocation still NOT VERIFIED. AI-generated drafts require adult source/fact review.
- Emulator short GL metric fails 30fps: Full **14.15 FPS**, P95 **114.63ms**; minimal **19.58 FPS**, P95 **81.76ms** (not Fold3 performance).
- [Android screenshot and log artifact #11650724200](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38005236039/artifacts/11650724200); `T-016` remains **IN_PROGRESS** / #19.

## T-016 — Hybrid AI Kids Conversation (development, 2026-10-10)

**New optional parent-configured Gemini/Groq direct API integration (no backend)**. On the same external Samsung DeX monitor, select provider, set model, save per-provider API key in Android Keystore, explicitly enable AI and acknowledge independently verified Free Tier status. Parent can generate a pack of 10–12 Vietnamese AI-drafted quiz questions per source-checked POI, caching privately by age and trusted-source fingerprint for 14 days. AI returns a numbered index of an existing sourced fact, never a new accepted answer. Cached quizzes work offline; if AI/key/quota is unavailable, prior deterministic offline quizzes still work.

For child speech, a **separate parent confirmation**, default OFF, may allow a short redacted/screened text transcript to go to Gemini/Groq for a follow-up; microphone recognition itself stays on-device, and no child audio or GPS coordinates are uploaded. This text filter is NOT perfect; provider data-use terms matter. F10 parent menu offers immediate disable for AI and child-cloud-text sharing. **Do not use this feature with children until reviewing provider terms, reviewing generated questions and testing real Fold3 voice/audio.** First [build CI](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38004929328) and [emulator E2E](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38004929284) succeeded on 71ac3950, but actual provider account calls and Fold3 hardware remain unverified. Details: [T-016 design](docs/T016_AI_KIDS_CONVERSATION.md), [CI evidence](.agent/evidence/T-016-ai-kids-android-ci-2026-10-10.md), [issue #19](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/19).

## T-015 — Two-way child dialogue (2026-10-10)

The child may answer Capybara's GPS-triggered quiz and short follow-up aloud. Enable **Bật giọng kể offline tiếng Việt** (output) and separately **Cho phép nghe câu trả lời của bé (offline)** (microphone input) on the **same DeX external monitor**. Android requests microphone permission; consent defaults OFF. Android 12+ on-device-only recognizer is mandatory, with locally installed Vietnamese support checked on Android 13+; no cloud recognition or network fallback. After local TTS completes, a microphone session opens for **at most nine seconds**, producing bounded Vietnamese reply feedback and a follow-up prompt. Audio/transcripts are not recorded or uploaded. A missing local recognition model falls back to caption-only interaction.

**CI build and emulator permission regression PASS**; real spoken Vietnamese on Fold3, vehicle noise, audio focus with Maps/Vietmap, and full DeX hardware operation **NOT YET VERIFIED**. See [T-015 plan](docs/T015_TWO_WAY_CHILD_DIALOGUE.md), [CI evidence](.agent/evidence/T-015-child-voice-ci-2026-10-10.md), [issue #18](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/18).

> **Current state (2026-10-10):** Development on `main` (all earlier PRs merged). **T-014 Android emulator LIVE GPS → POI → quiz E2E passed**; physical Samsung Z Fold3/DeX and real Vietnamese offline speech/audio focus remain unverified. 30 FPS performance acceptance still FAILS. The application has NO Quiet mode; speech requires separate explicit parent consent and a locally installed non-network Vietnamese TTS voice. Only 13 OSM/Mapcarta-crosschecked *candidate* POIs currently exist across the five journeys, with unverified actual road proximity. The character can introduce, quiz, read an answer and start a conversation prompt; it does **not yet listen to or interpret the child's spoken answers**. See [GPS nearby companion](docs/LIVE_GPS_POI_CHAT.md) and [T-014 CI evidence](.agent/evidence/T-014-live-gps-poi-chat-android-2026-10-10.md).

# DeX Kids Adventure

Offline-first, GPS-aware 2D educational companion prototype for children aged 2–6 on Samsung DeX.

**Status: DEVELOPMENT ON MAIN, NOT RELEASE READY.** Source for M1–M9 milestones has partial implementations and passing CI builds, but real device acceptance, finished cartoon artwork and verified Vietnamese geographic narration remain open.

- Android native Kotlin parent UI; Java17 LibGDX shared core; desktop LWJGL3 runner.
- Sprite-based yellow school bus and Capybara (original editable SVG artwork, 18 packed regions), wheel/suspension and actor animations; parallax city/park/river/bridge scenery. Biomes remain fictional demo contexts, not real POI claims.
- Deterministic bounded world chunks, 4-chunk districts and local day/night palette. The artwork is still first-pass and pending final art approval.
- Android single-display **GPX Replay**: select a document with SAF; bounded XML import on a worker thread, timeline pause/resume/restart overlay; no global storage permission or persistent GPS history. GPX replay with deterministic timestamp interpolation. Real GPS distance mode is opt-in from parent controls and only starts after Android permission approval; it estimates proximity to source-crosschecked, not human-approved, offline OSM candidate POIs in LIVE GPS mode; no driving-road or passing assertion.
- Provenance-gated story engine and offline Vietnamese TTS adapter source are present, but not connected to a reviewed content pack. No children’s audio is collected or sent to any server.
- The damaged-screen Fold3 uses **single-display DeX**: parent dashboard and game both run on the display from which the app was opened; in-game single mouse click on Parents menu or F10 opens parent controls, no phone-screen fallback or second-screen controls. Physical DeX validation pending.
- DeX Assistant repositories are untouched; draft signature-permission STOP IPC requires signing-contract audit before cross-package use.

## Verify locally (Ubuntu 24.04)

Prerequisites: JDK17, Android SDK35 with build tools 35.0.0, Linux desktop OpenGL.

```bash
./gradlew :core:test :desktop:classes :android:assembleDebug
./gradlew :desktop:run
./gradlew :desktop:run --args="--gpx test-data/gps/synthetic-urban-short.gpx"
```

Phone/external display hardware checks:
```bash
./scripts/device-smoke.sh <adb-serial>
./scripts/benchmark-fold3.sh <adb-serial>
```

These scripts do not grant permissions, modify system settings, install software or spoof locations. The benchmark script collects read-only samples from an already running game; do not treat virtual simulation tests as physical FPS evidence.

## Game animation preview
[Latest verified GitHub Actions](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) includes a `dex-kids-opengl-screenshots` ZIP artifact with full-HD PNG frames and `capybara-bus-animation-preview.gif`. These are captured from actual desktop software OpenGL, not an Android performance measurement.
Editable SVG sources: `art/assets-source/`. Generated assets: `assets/generated/`.

## Project management

Read `AGENTS.md`, `.agent/STATUS.md`, `.agent/TASKS.md`, and the current milestone's ExecPlan.
Original requirements are preserved in `docs/MASTER_EXECUTION_PROMPT.md`.
Actual test evidence lives under `.agent/evidence/`.

## Privacy and licensing

No cloud GPS upload, child voice collection or advertisements are implemented. Third-party content is not bundled. Attribution and copyright clearance are required for production. The repository currently exposes code publicly; this differs from the original request to use a private repository.

## M3 GPX mode

From the same-screen parent dashboard choose **Choose GPX file and start REPLAY**, select a local `.gpx` via the Android document picker, then use the in-game **Pause / Resume / Restart** controls. Parent controls remain F10/one mouse click. Input limit: 4 MiB / 20,000 track points. All GPX locations are synthetic or user-selected and do not imply verified real POI data. See [GPX Replay guide](docs/GPX_REPLAY_ANDROID.md). Physical DeX testing is deferred.

## M4 offline POI (candidate/review workflow)

The game now contains an offline proximity detector and Unicode on-screen notice, but **ships with 0 manually approved POI names**; it displays an explicit no-reviewed-data message rather than inventing a city/bridge/park. `tools/import_osm_pois.py` converts a previously downloaded Overpass JSON extract to **UNREVIEWED** candidates and provenance receipt; each place must be manually checked in OSM and recorded in `assets/poi/review-ledger.json` before `reviewed.tsv` may be populated. Build validation checks source/URL and ODbL attribution. See [data provenance and review instructions](docs/POI_DATA_PROVENANCE.md).

GPX notices are always labeled simulated, LIVE GPS notices are estimates; heading/road topology has not been map matched and there are no narrated 'we passed bridge X' claims. No network lookup or GPS upload is performed at runtime. Physical DeX verification is deferred.


## M5 HCMC narrated sample (explicit preview)
The **Start HCMC sample journey (preview)** dashboard button opens a synthetic 16-point central-HCMC GPX with four OSM-feature reference POIs and short sourced Vietnamese narration drafts. Each place/coordinate has a visible **sample / not human-approved** disclaimer; the shipped human-approved POI and narration catalogs remain empty.

Offline audio is OFF by default; in parent dashboard enable **Bật giọng kể offline tiếng Việt** before opening the sample. The app requires a locally installed Vietnamese TTS voice; without it, captions remain available. The Capybara TALKING frame activates only on real TTS utterance start and exits on done/error/focus loss/parent menu. No network TTS, no audio capture and no upload. See [HCMC sample and citations](docs/M5_HCMC_SAMPLE.md). Hardware/DeX audio-focus integration remains unverified.


## M6 — POI-inspired dynamic scenery (illustrative)
The game transitions selected **future** chunks toward park/tree, river, bridge or city artwork when a source-backed offline M4 POI notice has sufficient accuracy and proximity. These are **illustrative themes**, not road/travel assertions. Sprite props crossfade in/out over 2.5 seconds, expire after a short time/distance and reset on GPX restart. Offline HCMC preview explicitly labels its synthetic route and unapproved example coordinates; without a reviewed data pack, the default fictional procedural scenery remains unchanged. See [M6 design and test matrix](docs/M6_DYNAMIC_WORLD.md).
