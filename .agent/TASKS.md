## T-029 — AI Trip Companion (2026-10-11)

**PLANNED / IMPLEMENTATION NOT STARTED.** https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/33
Context-aware Capybara for Sâu 5 and Ong 4: offline first, bounded direct Gemini/Groq
lesson drafts, local English word practice, explicit separate child-text consent,
source/fiction distinction, and real DeX QA gates. Milestones M0–M6.
Plan: `.agent/plans/active/T-029-context-aware-ai-trip-companion.md`. Next action: M0 audit, no production code edits.

## T-028 — Child age update, 2026-10-11

**SOURCE IMPLEMENTED, LOCAL/DEVICE QA OPEN.** [Issue #31](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/31).
Sâu = 5 years, Ong = 4 years, BOTH = 4–5 (shared age baseline 4).
ParentSettings, Parent selector, dashboard, EntertainmentDirector and offline
learning focus-age logic updated; age regressions in JUnit and Python contracts.
No change to the original 66 authored beats or 3–6 learning card catalog.
Local tests and new Fold3 build not executed in this GitHub connector session.

## T-027 — richer artwork and trip learning (2026-10-11)

**IN_PROGRESS: source implemented, local/device QA pending.** [Issue #29](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/29).
18 new environment props using a second bounded atlas, 72 age-banded offline quizzes,
80 English vocabulary cards with optional offline EN pronunciation; sourced nearby POI
can bias the topic, never assert road passage. New static and Java tests authored;
Gradle build, screenshots and real Fold3 tests remain unverified. CI remains disabled.
See `docs/T027_SCENERY_LEARNING_ENGLISH.md`.

## Trạng thái hiện hành T-001–T-026 — 2026-10-11

Owner Main. Bảng này thay thế trạng thái checkpoint cũ bên dưới. Bằng chứng
mới được ghi trong `.agent/evidence/T-011-all-task-2026-10-11/`; kiểm thử đang
chạy không được tính PASS. Source mới 3787d19 đã qua 106 core tests/local contracts/build;
DeX đã cài/test bản cuối hai lớp avatar. Bằng chứng mới: `.agent/evidence/T-011-latest-github-2026-10-11/`; các checkpoint trước giữ đúng build.

| Task | Trạng thái | Đã xác minh / điều kiện còn cần để DONE |
| --- | --- | --- |
| T-001 | IN_PROGRESS | Build Android/desktop và Fold3 smoke qua; repo hiện public, yêu cầu private/release bootstrap còn mở. |
| T-002 | IN_PROGRESS | Render desktop, DeX và recreation qua; cadence dài của bản cuối chưa xác minh. |
| T-003 | IN_PROGRESS | Atlas 30 vùng, Sâu/Ong PNG mới và wiring ba audience qua; duyệt hình/lipsync/seam dài còn mở. |
| T-004 | IN_PROGRESS | GPX parser/sample, recreation và controls qua; SAF picker E2E FAIL: runner không chọn được fixture ở cửa sổ hệ thống; desktop GPX dài và loss/recovery ngoài đường còn mở. |
| T-005 | IN_PROGRESS | Source audit/sample qua; production reviewed POI = 0, cần duyệt hình học/nội dung và đường thật. |
| T-006 | IN_PROGRESS | Offline TTS thực tế qua; cần biên tập giọng/nội dung và đồng thời navigation. |
| T-007 | IN_PROGRESS | Mapping/crossfade/tests qua; POI smoke giả lập qua; POI thật, chuyển nhiều cảnh và duyệt cảnh còn mở. |
| T-008 | IN_PROGRESS | Parent/F10/same-display qua; cần duyệt UX/audio-only đầy đủ; PIN đã được phụ huynh loại khỏi phạm vi. |
| T-009 | IN_PROGRESS | DeX single-display/mouse qua; IPC ký cùng khóa và tích hợp Assistant cần task riêng. |
| T-010 | BLOCKED | Chưa đủ ký release/license/content/hardware nghiệm thu; không đóng từ debug APK. |
| T-011 | IN_PROGRESS | 106 tests/local contracts/build qua; QA ngắn bản cuối, ba audience/audio/recreation/POI smoke qua; SAF FAIL và gate dài/human còn mở. |
| T-012 | IN_PROGRESS | Android sample/route/privacy/cache qua; hai denial gate blocked bởi grant sẵn, nội dung/đường thật còn mở. |
| T-013 | IN_PROGRESS | 5 route context/source audit và Android route qua; biên tập con người và tuyến thật còn mở. |
| T-014 | IN_PROGRESS | Catalog/nearby logic qua; không có approved POI, chưa chạy GPS/ASR/5 corridor thật. |
| T-015 | IN_PROGRESS | Offline dialogue/scheduler qua; nhận dạng tiếng Việt/đáp tự nhiên với bé còn mở. |
| T-016 | IN_PROGRESS | Gateway/schema/cache qua; live Gemini PASS/11 validated quizzes, Gemini/text đã bật theo xác nhận phụ huynh sau QA; chất lượng cloud reply với bé chưa nghiệm thu. |
| T-017 | IN_PROGRESS | Bốn action Parent, advanced settings/local-only workflow qua; denial trên máy grant sẵn chưa xác minh. |
| T-018 | IN_PROGRESS | Bản cũ Fold3 60 FPS; bản cuối POI smoke 59.785 FPS/P95≤20ms; desktop mẫu ngắn 27.808 FPS chưa đạt30; endurance dài còn mở. |
| T-019 | IN_PROGRESS | Source-backed candidates qua; production pack chưa được duyệt. |
| T-020 | IN_PROGRESS | Fixture/harness qua; chưa có 5 GPX thực địa cần thiết. |
| T-021 | DONE | Phạm vi QA tự động bản trước đã đủ bằng chứng; retest bản mới ghi riêng, denial/road/human gate không tự đóng. |
| T-022 | IN_PROGRESS | UI chọn SAU/ONG/BOTH và persisted audience qua; quan sát đúng tuổi với hai bé còn mở. |
| T-023 | IN_PROGRESS | 66 beat, 18 opening slots/audience, recreation qua; hai lớp avatar/driver/action đã kiểm tra ảnh DeX ba audience, đa dạng/hứng thú con người còn mở. |
| T-024 | IN_PROGRESS | Audio playback qua; ba-audience/mix/F10 qua, navigation thật và chất giọng còn mở. |
| T-025 | IN_PROGRESS | Metrics/checklist qua; phụ huynh đã phản ánh lặp mở đầu, chưa có đủ quan sát sau sửa. |
| T-026 | IN_PROGRESS | 22 landmark scenes/44 ID mappings và VehicleLayout build/tests mới qua; 26-point fixture qua; smoke sửa PASS; 1800s bị ngắt ở 3 điểm do user yêu cầu build source mới 3787d19; 60min chưa chạy; bản cuối132s POI smoke PASS, không tính30min. |

## T-026 — Physical 30-minute route simulation (2026-10-10, IN_PROGRESS)

Owner Main; dependencies T-014/T-019/T-021–T-025. User authorized ADB on
actual SM-F926B/DeX2. Test-only route harness and host resource collector
implemented; smoke then26 independently sourced POI fixtures planned over
1800s. Synthetic downstream feed, production POI/render/narrator and dialogue
scheduler; no AndroidGPS/LiveFixGate/real-road or ASR claim.22 LIVE dialogues,
4 HCMC intro-only. Parent/AI preferences restored after runner, no GPS
injection/permission changes/cloud/child recording. Natural30min deadline,
F10, per-ID captions, render/resource evidence required before PASS.
Active plan `.agent/plans/active/T-026-current-build-route-test.md`.

## T-026 — Current build + 30-minute route test preparation (2026-10-10)

Owner Main; dependencies T-014/T-019/T-021–T-025. Build/preparation acceptance
met: local contracts and100 core tests PASS; current debug +AndroidTest build
SUCCESS. Prepared26 points Đồng Nai→Vũng Tàu→Đà Lạt→TP.HCM, plus44-point
full-catalog variant, exact existing coordinates and IDs. Runbook
`docs/T026_30_MIN_ROUTE_TEST.md`; manifests `test-data/gps/T-026/`.
Runtime status PLANNED/NOT_RUN: needs test-only fixture harness; production
GPX empty-reviewed catalogue and LIVE teleport guards prevent naive compressed
multi-city GPX POI playback. No device install or GPS injection performed.
Evidence `.agent/evidence/T-026-current-build-2026-10-10.md`; active plan
`.agent/plans/active/T-026-current-build-route-test.md`.

## T-021–T-025 Fold3 retest + Gemini (2026-10-10; stopped by user)

Owner Main; dependencies T-021–T-025/T-016. 12 local contracts + 100 core tests PASS;
debug/AndroidTest build SUCCESS; physical three-audience/9 completed beats,
SoundPool/TTS duck/F10 lifecycle and actual audience picker PASS. Fixed first
utterance silent fallback by warming offline TTS; parent confirms first line
has speech. Parent reports robotic voice, good effects/no overlap, both children
curious and following. Naturalness/complete child engagement gates OPEN.
Gemini `gemini-3.5-flash-lite` through Fold3 production gateway: explicit JSON
schema fixes intermittent array-root rejection; 3 consecutive calls PASS
(11/11/10 validated quizzes), no child text/audio/GPS upload; secret removed.
Final T-021 regression 9/11 PASS; two denial cases BLOCKED by pre-existing
mic/location grants, retained. Production/native offline Vietnamese audio PASS.
60-minute retest stopped at explicit user request: last resource 462.78s,
GL sample450.146s=59.949FPS/P95≤19ms, warmPSS146840–153084kB, thermal0;
**PARTIAL, not full-hour PASS**. Cleanup/preferences restored, test APK removed,
Parent on DeX2. Evidence `.agent/evidence/T-021-025-fold3-retest-2026-10-10.md`;
active `.agent/plans/active/T-021-025-fold3-retest.md`. T-022–T-025 IN_PROGRESS;
full endurance/road/navigation/ASR/cable/repeated human observation OPEN.
## T-026 — POI landmark backgrounds and 16:9 vehicle scale (2026-10-11)

**IN_PROGRESS — source implemented, local + DeX QA pending.** [Issue #27](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/27).
22 original editable POI landmark SVG scenes, 44 exact OSM ID mappings, source-backed scene trigger,
1 active landscape texture, and shared physical bus/Capybara/Sâu/Ong composition.
No claim of road-matched landmarks. New JUnit & `tools/test_poi_backdrops.py`
are authored but **not executed in this connector session**. Required: `./scripts/test-local.sh`,
`./gradlew :android:assembleDebug`, GL 1920×1080 screenshots and actual Fold3 FPS/PSS/POI-switch QA.
See `docs/POI_LANDMARK_BACKGROUND_ART.md`. CI remains disabled.

## Sâu personalized cartoon avatar integration (2026-10-10)

**SOURCE INTEGRATED / QA PENDING:** public repo stores 4 costumes of Sâu 4 tuổi as validated compressed text image data; Gradle auto-generates PNG at build; in-game SAU/BOTH display and contextual outfit change, solo ONG hides Sâu. Local image/source contract added, no CI. Validate `./scripts/test-local.sh`, `./gradlew :android:assembleDebug`, real Fold3 portrait quality/FPS and no overlap of bus/Capybara/HUD. Details: `docs/SAU_CHARACTER_ASSETS.md`.

## T-024 / T-025 — Audio Experience + Child Engagement QA (2026-10-10)

- **T-024: SOURCE IMPLEMENTED / DEVICE QA OPEN** — `KidSoundscape.kt` offline low-volume effects, optional ambient music OFF by default, Parent settings, TTS focus-loss cancellation, in-app ambient duck/pause. Real mixed navigation audio unverified. [Issue #24](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/24).
- **T-025: QA INSTRUMENTATION IMPLEMENTED / CHILD OBSERVATION OPEN** — pure Java in-memory event metrics, Parent F10 summary, JUnit + local contract, observer checklist for Sâu 4/Ong 3. Human engagement not automatically measured. [Issue #25](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/25).
- **Gates:** local Gradle build/tests, Fold3 1920×1080 audio focus and 60min soak, human 15–20 minute observation. `docs/T024_T025_AUDIO_CHILD_QA.md`. No CI and only `main`.

## T-022 / T-023 — Audience and continuous preschool entertainment (2026-10-10)

**IN_PROGRESS: source integrated on main, test gates pending.** Three local Parent choices Sâu 4 tuổi/Ong 3 tuổi/BOTH 3–4 tuổi, 48 offline authored beats, age filters, balanced turns, child-facing large text, optional offline TTS, brief Capybara gestures, GPS/POI priority and recreation-aware pacing. JUnit and local source contract added; local Gradle/real Fold3 acceptance NOT CLAIMED. See `docs/T022_T023_TWO_KIDS_ENTERTAINMENT.md`, issues #22/#23. No CI.

## T-021 — Physical Fold3 autonomous QA (2026-10-10, DONE — automatic scope)

Owner Main; dependencies T-001–T-019. Physical SM-F926B / Android 15 / DeX2. FullHD GL 1920×1080, real 60-minute DEMO PASS: 59.963 FPS / P95 upper bound 19ms, 120 progress/display checks, normal deadline expiry. Warm PSS 147007–147883kB, CPU windows 29–30%, thermal status 0; whole-device battery 96→90%, battery temperature 30.8–31.5°C. No uncontrolled upward PSS trend observed in this workload, no claim of zero leaks.
Ten local contract scripts + 93 core tests PASS; Android unit NO-SOURCE; both APK builds SUCCESS. Initial eleven physical modes including SAF PASS; final eleven script modes including expanded Parent actions PASS after fixing a hidden AlertDialog list. Sound playback PASS: local tone/media active, production Vietnamese offline narrator callbacks and native TTS onDone; volume preserved, no recording. Preferences restored, microphone/location remain denied. Endurance measured pre-menu-fix APK; final APK separately regression-tested.
Final app SHA256 `b13f3d45dffd4bdce500b04980dac3befc3ddf01041297a4bfc3cbaff797f139`. Evidence `.agent/evidence/T-021-fold3-autonomous-qa-2026-10-10.md`; completed plan `.agent/plans/completed/T-021-fold3-autonomous-qa.md`; procedure `docs/FOLD3_AUTONOMOUS_QA.md`. Main only, no CI.
Real road/POI review, Vietnamese ASR accuracy/human voice quality, navigation concurrency, cable recovery, provider API calls, external signing and signed release remain NOT VERIFIED. No GPS injection or provider calls. Earlier status sections below are historical; T-021 supersedes their pending local build/physical DEMO performance/playback checks only.

## T-018 — Tối ưu FPS LibGDX FullHD trên Samsung DeX (2026-10-10)

Đã tối ưu source render: gộp ShapeRenderer vào **một SpriteBatch**, thay hình tròn vẽ lại mỗi frame bằng một texture nhỏ tái sử dụng, chỉ vẽ **4 chunk nhìn thấy** thay vì 6; WorldWindow dùng cache ring array và bỏ công việc chuẩn bị ở các frame chưa thay chunk; chỉ xác định mood theo giờ mỗi phút; Android dùng RGB565/no depth/stencil/MSAA khi EGL hỗ trợ; log `sprite_draw_calls_prev`. Giữ nguyên GPS thật, POI, animation xe/Capybara, AI, on-device ASR/TTS, Parent F10, chỉ một màn hình DeX.

**Không có CI.** Chạy `./scripts/test-local.sh` trên source checkout với Gradle/Android SDK. Các test mới `WorldWindowTest`, `SceneryLayoutTest`, `tools/test_render_fast_path.py`. JVM harness tách riêng đã chạy trong môi trường hiện tại: **579 assertions PASS** cho ring cache và viewport geometry; **không phải** full Gradle build/test. Không thể tải Gradle hoặc clone repo tại môi trường này, nên phải chạy full unit tests trên máy local có checkout.

**Chưa đo FPS thực tế sau sửa trên Fold3.** Baseline cũ trên Android emulator (không phải thiết bị thật): full **14.15 FPS**, minimal **19.58 FPS**. Không được gọi đây là FPS sau tối ưu. Chi tiết: [T-018](docs/T018_FPS_OPTIMIZATION.md), [issue #21](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/21).

## T-017 · simplify parent / local-only verification (2026-10-10)
- Removed `.github/workflows/build.yml` and `.github/workflows/p0-android-integration.yml` (and obsolete `scripts/ci/p0-emulator-e2e.sh`). No GitHub Actions run for new code.
- Compact single-display Parent home with four actions, advanced options under Cài đặt, selected age and session settings preserved. Live launch requests GPS fine/coarse first; when supported, it requests Android RECORD_AUDIO next. Denied microphone does not block GPS/POI.
- Defaults now: `offline_tts=true`, `child_mic_optin=true`, `ai_quizzes=true`, provider active preference defaults ON **only if a key/model/Free-Tier confirmation is configured**. `child_text_cloud_explicit=false` remains private and separately confirmed. No OS runtime permission automatically granted.
- `scripts/test-local.sh` and `scripts/verify.sh` run local Python contracts and Gradle unit tests only; no CI, no emulator. Focused **local javac/java ChildAnswerInterpreter smoke: 9 assertions PASS** in working environment; full project Android/Gradle unit test requires actual cloned repo, Android SDK and dependencies, not yet run here.
- Keep only branch `main`. See `docs/LOCAL_TEST_PARENT_MENU.md`. No claims of Fold3 real-world testing.

## T-016 latest source acceptance (2026-10-10)
- Last code source SHA `3a7e0477e0a9d13cd99fb59691ce5319eeab040b`. [Build/unit CI **SUCCESS** #38005236008](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38005236008) and [Android API35 emulator E2E **SUCCESS** #38005236039](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38005236039).
- **PASS:** `p0_ai_cache` (ten private offline quiz cards, age and source-digest separation, AI and child cloud consent OFF by default, no HTTP), `p0_child_mic_privacy`, real Android emulator GPS→source-checked approximate POI→quiz, single-screen mouse/F10, route/GPX regression.
- Real API provider calls **NOT TESTED** (no key/model available); real Fold3/DeX child audio/geolocation still NOT VERIFIED. AI-generated drafts require adult source/fact review.
- Emulator short GL metric fails 30fps: Full **14.15 FPS**, P95 **114.63ms**; minimal **19.58 FPS**, P95 **81.76ms** (not Fold3 performance).
- [Android screenshot and log artifact #11650724200](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38005236039/artifacts/11650724200); `T-016` remains **IN_PROGRESS** / #19.

## T-016 – AI Kids Conversation (2026-10-10, IN_PROGRESS)
- Implemented on main: KidsAiSettings/Keys (two parent-controlled Gemini/Groq API providers; encrypted per-provider BYOK), KidsAiGateway (allowlisted HTTPS text-only, per-call limits, chosen-provider retry only), KidsAiQuizCache (10–20 per source-backed OSM POI, 14-day TTL, age/source digest), parent UI cache generator, GPS cue integration, separately consented child-text cloud follow-up with deterministic T-015 fallback.
- **Default OFF** for AI and for child cloud text sharing; separate from offline microphone and offline TTS permissions. No cloud STT, raw voice, GPS, LLM device actions or remote roadway inference. App has INTERNET permission only for opt-in HTTPS text.
- Build [#38004929328](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38004929328) SUCCESS and Android E2E [#38004929284](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38004929284) SUCCESS on 71ac3950; p0_ai_cache PASS (10 cached questions, age/source invalidation, no network and no default child sharing), old P0 regressions PASS.
- Follow-on main change [3a7e0477](https://github.com/khuongnd0901/DeX-Kids-Adventure/commit/3a7e0477e0a9d13cd99fb59691ce5319eeab040b) adds immediate F10 disable; later CI must verify separately.
- **Gates OPEN:** no real provider API key or account free-tier proof, AI-generated question factual/age review, Samsung Fold3 real ASR/TTS/navigation audio, 30FPS and roadside-reviewed OSM coverage.
- Evidence `.agent/evidence/T-016-ai-kids-android-ci-2026-10-10.md`, docs `docs/T016_AI_KIDS_CONVERSATION.md`, issue [#19](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/19). Main only.

## T-015 checkpoint — 2026-10-10, commit b339e0ba
- [Build and unit tests SUCCESS #38003066725](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38003066725) with ChildAnswerInterpreterTest and static on-device/consent privacy guard.
- [Android API35 E2E SUCCESS #38003066721](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/38003066721): `p0_child_mic_privacy` PASS (no permission by default, setting OFF, zero recognizer instance). Single-display, HCMC/route/GPS→POI→quiz regression PASS.
- **No real Vietnamese ASR audio was tested** on emulator; Fold3 local model, Vietnamese accuracy, TTS and Maps/Vietmap audio-focus OPEN.
- 30FPS FAIL in emulator: Full15.26 FPS/P95 97.57ms, minimal21.04 FPS/P95 75.01ms. No Fold3 performance claim.
- Evidence `.agent/evidence/T-015-child-voice-ci-2026-10-10.md`; task [#18](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/18). T-015 IN_PROGRESS; main-only.

## T-015 — Hội thoại hai chiều offline tiếng Việt (2026-10-10)
- Yêu cầu: Capybara hỏi theo GPS POI, nghe câu trả lời thật của bé và phản hồi; không dùng cloud/ASR network, không chạm màn hình smartphone, giữ một màn DeX.
- Thực hiện `OnDeviceChildSpeech`: Android on-device recognizer API31+, model Việt offline check API33+, runtime RECORD_AUDIO riêng, phụ huynh opt-in riêng mặc định OFF, lượt nghe 9s. Mic bị hủy khi pause/F10/menu, background, finish hoặc sang địa danh khác.
- `ChildAnswerInterpreter`: phản hồi dựa trên tiếng Việt ngắn/có-không/màu/cây/biển, không giữ/ghi log/transcript thô; hỏi → đợi TTS hết → nghe → phản hồi → gợi chuyện. Fallback phụ đề/câu đố cũ nếu máy chưa có nhận dạng offline.
- CI Java unit, Android emulator privacy-default và regression; nghiệm thu tiếng Việt thật/Fold3/TTS noise còn OPEN, không tự cho rằng máy đã có model giọng Việt.
- Issue [#18](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/18), tài liệu `docs/T015_TWO_WAY_CHILD_DIALOGUE.md`, trạng thái **IN_PROGRESS**. Giữ main-only.

## T-014 LIVE GPS nearby POIs — Android QA checkpoint (2026-10-10)
- **Source main `e324069`**; [build CI SUCCESS #37967017188](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37967017188), [Android emulator E2E SUCCESS #37967017134](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37967017134).
- QA `p0_live_nearby` PASS: real LocationManager GPS_PROVIDER injection (8–12m) → 2-fix LiveFixGate → OSM source-audited Đá Ba Chồng vicinity → Android subtitle → VN quiz, ~21.98m accepted feed distance. TTS off without parental enablement; no audio claim.
- Single-display mouse, HCMC GPX, DEMO route and regular LIVE GPS regression PASS. Quiet mode setting and toggle removed. Offline speech parent-enable maintained.
- **Performance FAIL:** 15.02 FPS/P95 116.98ms full, 19.79FPS/P95 78.21ms minimal on short software emulator windows; not Fold3 results.
- Real five-route POI coverage **sparse: 13 OSM candidate locations**, not roadway/entrance matched. No field review, no human-validated geo claims. Real spoken Vietnamese TTS, 30FPS Fold3/DeX, Maps/Vietmap audio and two-way child ASR NOT VERIFIED/NOT IMPLEMENTED.
- [T-014 issue #17](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/17), evidence `.agent/evidence/T-014-live-gps-poi-chat-android-2026-10-10.md`. Status **IN_PROGRESS**, main only.

## T-014 — Real GPS geofenced nearby-POI companion, no Quiet mode (2026-10-10)
- Owner request: real on-drive Android GPS triggers nearest sourced POI; Capybara introduces it, then asks a quiz, provides an answer and an open-ended chat prompt. Timer-only named route story cards DISABLED in LIVE GPS; route-themed DEMO remains explicitly labelled.
- 13 OSM-derived Mapcarta-backed representative POIs (Đồng Nai, Vũng Tàu, Phan Thiết, Bảo Lộc, Đà Lạt, Nha Trang); `assets/poi/live-landmarks.tsv`, `assets/narration/live-landmarks.tsv`, `assets/poi/live-dialogue.tsv`, exact audit file. OSM production approved reviewed.tsv remains EMPTY; no claim of passage, road match or field approval.
- Actual `LiveJourneyFeed` → 2-good-fix `LiveFixGate` (accuracy <=25m, freshness, teleport rejection) → `OfflinePoiEngine` → source-backed native caption, local consent-based VN TTS, 16s quiz / 32s answer / 48s chat. Generic trivia after 5m WITHOUT nearby source is explicitly non-GPS. No microphone ASR yet.
- Quiet UI/property and voice gating removed. Parent enablement for offline speech and TTS device engine still required. One external Samsung DeX monitor, no PIN/lock/phone touchscreen.
- CI pending at authoring; new JUnit, static schema/provenance, disposable Android GPS-nearby instrumentation and original regression tests.
- Performance 30 FPS and full 5-corridor POI coverage/roadside geometry/human review/real Fold3 TTS remain OPEN. Issue [#17](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/17), docs/LIVE_GPS_POI_CHAT.md. T-014 IN_PROGRESS, main only.

## T-013 Android verified checkpoint — 2026-10-09
- Direct main implementation `d04293bf`; QA fix `5b6caf4c`; [source and release notes](../docs/ROUTE_KNOWLEDGE.md).
- [CI build SUCCESS #37940969993](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37940969993): 35 facts/5 routes checks, Java unit and Android build.
- [Actual Android emulator E2E SUCCESS #37940969959](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37940969959): `p0_route` PASS (7 source-backed stories, VN captions, visible no-geolocation disclaimer, no unconsented speech); one-click single-display and HCMC/Live GPS regressions PASS.
- Performance STILL FAIL 30FPS: full render 19.51 FPS / P95 81.25ms, Audio-only 26.66FPS / P95 62.77ms in short software-emulator A/B/A/B windows. No Fold3 hardware performance claims.
- No geocoded production road POIs; selected-route stories are timed **educational context only**. Real DeX usability, human editorial review and offline TTS audio output NOT VERIFIED; T-013 remains IN_PROGRESS.
- Evidence: `.agent/evidence/T-013-five-route-knowledge-android-ci-2026-10-09.md`; issue #16.

## T-013 — 5 hành trình kiến thức offline (2026-10-09)

- Thêm 5 chủ đề hành trình Đồng Nai đến Vũng Tàu, Phan Thiết, Bảo Lộc, Đà Lạt, Nha Trang; mỗi tuyến 7 thẻ kiến thức Việt ngữ nguồn HTTPS (tổng 35), phù hợp 2–6 tuổi. Source: `assets/routes/knowledge.tsv`.
- Android phụ huynh chọn hành trình, xem tất cả câu chuyện ngoại tuyến, chọn DEMO hoặc LIVE GPS, phụ đề bằng Android TextView, tuỳ chọn giọng TTS tiếng Việt OFFLINE chỉ khi đã duyệt và tắt Quiet.
- Thuyết minh tuyến được phát theo thời gian; **KHÔNG dùng toạ độ để khẳng định xe đã đi qua địa điểm** và không thay thế GPS road matching, dữ liệu OSM reviewed.tsv vẫn TRỐNG.
- Giữ tương thích DeX một màn hình chuột/bàn phím, không PIN, không yêu cầu cảm ứng trên Fold3.
- JUnit parser + Python nguồn + Android instrument `p0_route`; dự kiến CI build/emulator; T-013 **IN_PROGRESS** cho đến khi có CI và thiết bị thật. 30FPS/Fold3 vẫn chưa đạt nghiệm thu.
- GitHub issue [#16](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/16); runbook [docs/ROUTE_KNOWLEDGE.md](../docs/ROUTE_KNOWLEDGE.md). Chỉ dùng nhánh `main`, theo yêu cầu chủ repo.

## T-012 / P0 Android integration checkpoint — 2026-10-09
- Branch `feat/T-012-android-integration-performance`; [draft PR #14](https://github.com/khuongnd0901/DeX-Kids-Adventure/pull/14) stacked on M7/M8 PR #13; no merge/main changes. [Issue #15](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/15).
- **Actual Android 15 API35 x86_64 GitHub emulator [run #37930356631](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37930356631) SUCCESS** on code commit `02e275af`: Android/core build, `single_display_mouse` PASS, `p0_hcm` PASS (bundled synthetic GPX180.66s → unreviewed Tao Đàn POI → PARK themed GL scene → Vietnamese native subtitle; TTS disabled), `p0_live` PASS with ADB emulator GPS fixes and 12.926m of measured LIVE distance, provider reported 5m accuracy.
- **Performance acceptance FAIL** (while QA collection SUCCESS): four same-APK/emulator A/B/A/B windows, 6s each with 2s warmup, rolling last 180 GL frames. Full FPS 10.94/12.40 (mean11.67), P95 174.05/117.99ms (mean146.02). Audio-only FPS16.74/16.62 (mean16.68), P95101.67/89.23ms (mean95.45). Below target 30 FPS, no causal attribution to companion artwork or hardware.
- Evidence artifact [11616275848](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37930356631/artifacts/11616275848): test logs, native screenshots, no observed fatal signatures in collected log. Screenshots require manual visual review.
- Previous CI failures #37928255349, #37928939538, #37929556218 were retained; underlying issues: brittle dialog accessibility text assertion, GL-screen readiness race, cross-thread LIVE-distance read. Regression on CI #37930356631 PASS.
- Open gates: controlled old/new artwork same-host A/B (not the same as Full/Audio-only); sustained 30FPS tuning and CPU/PSS; GPS dropout/recovery instrument integration; offline VI TTS spoken/focus/Maps+Vietmap; human-approved OSM/POI source/editorial review; real external-monitor Fold3 DeX 60min/thermal/latency and release signing/license.
- T-012 IN_PROGRESS (simulator functional slice verified, performance gate FAILED). T-008/009 and M4-M6 remain IN_PROGRESS; M9 hardware BLOCKED. No PIN/lock screen, no other repository touched.

## M7/M8 follow-up — 2026-10-09
User excludes PIN, lock screen, kiosk or phone-touch due broken built-in Fold3 screen; external monitor is controlled with mouse/keyboard.
- T-008 M7: implemented normal-click Parents menu, F10, 15/30/60m session option, Audio-only rendering, Quiet switch and local preferences reset. Emulator instrumentation authored; physical DeX gates open.
- T-009 M8: extended signature-protected IPC to STOP/PAUSE/RESUME, no unprotected START; DeX-Assistant must explicitly request signature permission and share signing identity. Only DeX-Kids-Adventure repo changed.
- Draft PR #13 on separate feature branch; CI/hardware and actual cross-APK signing validation NOT VERIFIED at documentation checkpoint.
- Status remains IN_PROGRESS for T-008 and T-009; no M9 acceptance.

# DeX Kids Adventure — task tracker

T-011 latest ADB slice (Main agent; dependencies T-001–T-009): source6c8a9df,
80 core PASS, build/install PASS, real recreation/deadline/feed + same-display
F10 controls + offline demo PASS; refusal BLOCKED by pre-existing GPS grants.
Acceptance/evidence: `.agent/evidence/T-011-adb-integration-2026-10-09.md`.
State remains IN_PROGRESS; outstanding runtime/hardware gates retained.
T-011/T-004 follow-up verified: Android Harmony rejects Xerces security feature
before parsing. Portable bounded decode/DTD rejection/resolver fix;82corePASS,
actual sample + real SAF GPX PASS, Android unsafe entities rejected. Owner Main;
dependencies M3/M5/QA; evidence `.agent/evidence/T-011-gpx-android-parser-fix-2026-10-09.md`.
Bug acceptance met on API35 emulator; milestones remain IN_PROGRESS.

Updated 2026-10-09
Allowed states: BACKLOG, PLANNED, READY, IN_PROGRESS, BLOCKED, DONE. No DONE without actual acceptance.

| ID | Milestone | Issue | Status | Evidence / unresolved gate |
| --- | --- | --- | --- | --- |
| T-001 | M0 | #2 | IN_PROGRESS | Build [37795183419](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37795183419); Fold3 smoke/private repo open |
| T-002 | M1 | #3 | IN_PROGRESS | OpenGL 720p/1080p [37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) PASS; real sustained Fold3 FPS and lifecycle open |
| T-003 | M2 | #4 | IN_PROGRESS | Capybara facial-landmark test, 5 extra characters + 6 vehicles, 29-sprite atlas and OpenGL visual review [CI 37879894270](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37879894270) SUCCESS | Penguin and companion visibility [CI 37880239651](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37880239651) PASS; final art signoff, simulator/Z Fold3/DeX, lipsync, long-run visual seams |
| T-004 | M3 | #5 | IN_PROGRESS | GPX bounded secure import + SAF picker, deferred LibGDX loading, Pause/Resume/Restart controls + tests [core CI #37884079192](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37884079192) PASS | Android integration build/AndroidTest compile and OpenGL [CI #37884357999](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37884357999) PASS; actual Android SAF picker E2E, config recreation and device test NOT_RUN |
| T-005 | M4 | #6 | IN_PROGRESS | 4 sourced HCMC sample OSM way IDs/centroids + independently sourced Vietnamese fact drafts and synthetic GPX preview; production-reviewed pack remains empty; [source audit](https://github.com/khuongnd0901/DeX-Kids-Adventure/blob/feat/T-001-bootstrap-libgdx/docs/M5_HCMC_SAMPLE.md) | Human geometry/POI review, real route matching, LIVE named display and device test open |
| T-006 | M5 | #11 | IN_PROGRESS | Bundled 4 short Vietnamese narration drafts, source-bound offline catalog/cooldown, same-screen sample preview, optional offline Android TTS with parent consent, captions and TTS-start-driven Capybara talking animation | Full [CI #37905938576](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37905938576) SUCCESS; human editorial review, actual offline Vietnamese TTS playback, navigation audio coexistence and Android runtime tests still open |
| T-007 | M6 | #7 | IN_PROGRESS | `PoiSceneDirector` source-POI→biome layer, chunk-anchored crossfades, 6-type mapping, no PASSING_CANDIDATE or false road claim, queued switch, GPX reset, freeze with parent menu; synthetic HCMC screenshot & JUnit [CI #37907989363](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37907989363) PASS, 30-sprite atlas+APK; HUD-fixed [CI #37908584642](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37908584642) PASS; production POI review/Android runtime/physical DeX acceptance open | Source content review, visual evidence, runtime DeX performance and hardware acceptance open |
| T-008 | M7 | #8 | IN_PROGRESS | Same-screen parent dashboard + in-game long-press/F10 menu; [CI #37883012846](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37883012846) PASS (source/static/unit/AndroidTest build), emulator runtime NOT_RUN; Parent UI [37797413766](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797413766); PIN/audio-only/DeX independent UX open |
| T-009 | M8 | #9 | IN_PROGRESS | DeX single-display navigation (no dual phone controller); [CI #37883012846](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37883012846) PASS build, physical DeX NOT_VERIFIED; Screen routing/stop IPC [37797528265](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797528265); physical DeX and Assistant signing/open |
| T-010 | M9 | #10 | BLOCKED | Virtual soak [37797706168](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797706168); real Fold3 60-minute run/signing/licensing open |
| T-011 | M0–M9 local simulator QA | #12 / draft PR #1 | IN_PROGRESS | Main agent; T-001–T-010 dependencies. Baseline56corePASS; integrated artwork58corePASS/29atlasPASS/buildSUCCESS; real3600s emulator child lifetime/expiry, rendering/recreation/IPC/denial/offline/post-fix smoke verified. Mandatory60cases40PASS/1BLOCKED/1NOT_RUN/8NOT_IMPLEMENTED/10NOT_VERIFIED; retained historical failures/incidents explicit. Physical Fold3/DeX/data/content/release gates open. Evidence `.agent/evidence/simulator/final-test-summary.md`; active T-011 plan. Integrated123s smokePASS; observed19.825FPS prefix below30FPS (FAIL), controlled profiling and Android GPX next. Published QA cdb9a17/receipt1b2958b; source integration worktree documented in STATUS. |

Latest source work: M2 animation test, editable art, layered renderer and GIF capture. Production voice pipeline and Device Owner from DeX-Assistant remain unchanged.


T-011 follow-up: local T-003 merge conflict resolved in8bc27c6; exact tree matches fc308b9; original unstaged changes retained. Evidence `.agent/evidence/T-011-conflict-resolution-2026-10-09.md`.

T-011 commit-all: all remaining tracked changes audited as CRLF/LF only; normalized LF with Git text attributes. Source behavior unchanged; artifact ignore rules retained.
