## T-030 — Source implementation review (2026-10-11)

**IN_PROGRESS — SOURCE IMPLEMENTED ON `feat/T-030-session-ai-packs`; Gradle/provider/Fold3 QA NOT VERIFIED.**
Added strict reviewed-ID pack validator (15–20 cards, age/focus/topic/word), 
120 offline quizzes + 80 English words, per-audience AtomicFile last-good cache,
bounded history/cursor, new-trip generation + exhaustion refill, initial + five retries,
conservative shared persistent 5 rolling-60-second HTTP requests gate including
Gemini/Groq fallback, 429 Retry-After, OFF-by-default parent opt-in and consent
cancellation, offline playback via the existing entertainment director.
New core JUnit tests and local Python contracts added but **NOT EXECUTED** through
GitHub connector. M6 acceptance remains open; do not label DONE or merge on
assumption. Evidence: `.agent/evidence/T-030-source-review-2026-10-11.md`.

## T-030 updated quota assumption — 2026-10-11

**PLANNED, NOT IMPLEMENTED.** Parent says 5 API requests/minute.
Plan now specifies one shared rate limiter with >=12s spacing, counting
all actual HTTP attempts; no arbitrary low daily caps. Calls only when
a pack is needed; separate provider daily/token/cost ceilings still apply.
Tracked in `.agent/plans/active/T-030-session-ai-question-packs.md`
and issue #35. No actual provider-quota measurement or APK testing.

## T-030 — AI trip question packs requested (2026-10-11)

**PLANNED, not implemented:** parent confirmed fresh 15–20 Q&A plus
English words at each new journey start; atomic replace-on-success,
old pack preserved on failure, initial+5 retries per cycle and refill
at exhaustion. Use curated fact/word IDs, 4/5-age gating,
rate limits and background-only calls. Tracker issue #35, ExecPlan
`.agent/plans/active/T-030-session-ai-question-packs.md`.
Current `KidsAiGateway.generate()` is POI-specific and does NOT
implement session pack refresh. Real provider and device tests pending.

## T-029 — Context-aware trip conversation plan (2026-10-11)

**PLANNED (no implementation or new test evidence).** https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/33
ExecPlan `.agent/plans/active/T-029-context-aware-ai-trip-companion.md` includes M0–M6, offline-only fallback, POI provenance,
Sâu 5/Ong 4 focus, consent and API budget gates. Prior current code untouched.

## Audience age correction — T-028 (2026-10-11)

SOURCE on feature branch: Sâu now 5, Ong now 4. BOTH uses age 4
for common cards, and age 5/4 for the individual learning focus.
Parent UI/DeX HUD, age accessors, current docs and source tests updated.
[Issue #31](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/31).
Local Gradle build and physical Fold3 checks remain NOT VERIFIED.
Historical evidence retains old app build ages.

## T-027 — artwork diversity + offline travel quizzes and vocabulary (2026-10-11)

**SOURCE ON FEATURE BRANCH; NO LOCAL GRADLE/DEVICE TESTS CLAIMED.**
18 original art/props SVGs, secondary 1024² sprite atlas + deterministic biome foreground.
Offline UTF-8 learning catalog of 72 quiz/80 English cards across 8 scene themes and ages 3–6.
Current Sâu/Ong/BOTH flows retain existing 66 legacy beats and POI priority.
Optional on-device English TTS only if non-network EN voice installed; original Vietnamese TTS
and captions remain fallback. T-027 [issue #29](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/29).
Local build, visual clipping, age/audience regression, EN audio and Fold3 FPS/PSS still OPEN.

## Publication request — 2026-10-11 (Main)

Parent explicitly authorized commit and push to GitHub. Verified build/source
commit 2d7611d is ready; latest origin/main fetched before publishing.
Task table corrected to distinguish completed smoke from unverified long runs.
No acceptance status changed: T-021 DONE for previous automatic QA scope,
T-010 BLOCKED, remaining 24 tasks IN_PROGRESS. This publication adds no
new device/test evidence and does not close SAF/endurance/human gates.

## Final latest-source build/test checkpoint — 2026-10-11 (Main)

Fetched/integrated origin/main3787d19; final Android debug installed on
SM-F926B Android15/externalDeX2. Parent requested half-body bus avatars plus
original full-body Sâu/Ong outside bus for question actions; both layers
implemented, actual SAU/ONG/BOTH DeX screenshots reviewed. Capybara driver
in front curved glass, bus shifted left to separate activity companions.
106coretests/17sourcecontracts/debug+test+desktop build PASS. Latest-source
short regression13PASS/2BLOCKED_PREEXISTING_GRANT; final affected audience
picker/audio(9beats,0silentfallback)/recreation retry PASS. Initial recreation
overlapped unfinished install and failed; retained evidence. SAF picker FAIL
(owned file not selectable on external DocumentsUI), gate remains open.
Final132sPOIsmoke PASS,59.785FPS/P95upper20ms; warmPSS151032–197128kB,
thermal0. Desktop short sample27.808FPS/P95upper84ms, >=30gate unmet.
Live Gemini PASS11quizzes; original pre-QA prefs restored then authorized
Gemini/cloud-text enabled permanently (encryptedBYOK,FreeTier parent-confirmed).
TestAPK removed, Parent onDeX2, existingmic/GPSgrants unchanged.
Evidence `.agent/evidence/T-011-latest-github-2026-10-11/`.
30/60min latest-build endurance, SAF, human art/content/child observation,
GPS/ASR/road/navigation/signing/privacy/license gates remain open.

## Latest GitHub source integrated — 3787d19, 2026-10-11

Owner Main, active T-011 reverification. Resolved CartoonSprites merge with
HQ poses/solid turn focus preserved and new VehicleLayout cabin/bus bob;
build pipeline generates both HQ sheets and 22 bounded backdrop textures.
106 core tests zero failure/error/skip; 17 Python source contracts and
Android debug/AndroidTest/desktop classes PASS. Upstream had changed
test-local.sh mode to644; restored755 after Permission denied. Current APK
install, actual FullHD GPX landmark render and DeX retest underway.
T-026 now includes upstream landmark-art scope and existing route-QA scope;
current long-run/current-scene device gates remain open. No new DONE claimed.

## New GitHub build request — 2026-10-11

User requests fetch/build/run newest source. Fetched origin/main 3787d19
(POI landmark scenes/vehicle layout) after prior 389d058 integration. Prior
30min run INTERRUPTED at 3 completed points; no natural30min/full-hour PASS.
Smoke retry PASS, live Gemini PASS/11 quizzes and 3-audience audio PASS retained
with their prior-build scope. Original parent/AI preferences restored exactly,
mic/GPS grants unchanged. Preserve local avatar/context/HUD/opening fixes
while integrating newer source, then rebuild and test on external DeX2.

## Current integration checkpoint — 2026-10-11 (Main)

User requested reverify T-001–T-026 and sharper/story-aware Sâu/Ong, smaller
cards/captions and authorized Gemini Free Tier child-text opt-in. Integrated
origin/main 389d058 into local 6248d86 while retaining original dirty workspace
in stash. 103 core tests, expanded local contracts, Android debug/AndroidTest
and actual desktop FullHD build/render PASS. Current debug/test APK installed
on SM-F926B Android15, DeX2. Short physical regression runs sequentially;
new-session opening variety and recreation PASS, SAU/ONG/BOTH picker PASS.
Two denial cases BLOCKED by pre-existing mic/GPS grants; grants unchanged.
High-resolution transparent four-pose Sâu/Ong sheets load once per scene;
18 persisted local opening slots per audience, 66 finite authored beats.
Three-audience physical audio PASS; final prop APK recreation and live Gemini
source-only request PASS (11 validated quizzes). First route smoke FAIL at
immediate asynchronous dialog-resume assertion; test-only wait/advancing-feed
correction built and rerun in progress. 30min route and 60min current-build
endurance not yet PASS. No child recordings or GPS injection. After QA apply encrypted
Gemini/provider+child-text settings per explicit parent confirmation.
Authoritative 26-task status/remaining gates are at top of TASKS.md.

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
## T-026 — POI-specific illustrated scenes + vehicle scale (2026-10-11)

**SOURCE IMPLEMENTED ON FEATURE BRANCH; LOCAL/JUNIT/ANDROID/FOLD3 QA NOT VERIFIED.**
44 existing OSM source-backed live/sample POI IDs resolve to 22 hand-authored stylized SVG scenes
(Vũng Tàu/Nha Trang beaches, light houses, hilltop Christ statue, Cham towers,
HCMC palace/post office, Đà Lạt Lâm Viên, waterfall, tea hill, cathedral,
rock formations, park and mountain). Original SVG source; PNG at build, one active GPU backdrop.
`PoiBackdropRenderer` preserves aspect 1200:650; crossfades without claiming geospatial road match.
`VehicleLayout` updates school bus to 950×417 (from 660×290 SVG), wheel R66;
Sâu/Ong/Capybara staged within cabin and synced to bus bounce.
New local-only art checks plus JUnit contract; physical screenshot/performance and full Gradle
build remain pending. [Issue #27](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/27),
`docs/POI_LANDMARK_BACKGROUND_ART.md`.

## DeX FullHD game dashboard — 3 audiences and 66 episodes (2026-10-10)

**SOURCE/ART COMMITTED on main; LOCAL FULL GRADLE + FOLD3 VISUAL QA OPEN.**
Mode SAU (4), ONG (3) or BOTH: native Vietnamese `AdventureDashboard` (non-touch
topic card, question/story card, provenance-conscious journey strip) sits above
existing LibGDX world. CartoonSprites draws only the selected child companions,
each in 4 costumes. The actual approved Ong 4-role illustration is committed as
a compact transparent indexed PNG `art/assets-source/characters/ong-costumes.png`
(192x288, four 96x144 cells), checksum SHA256
`deef87a482965d5f5c8fe4c692e0891416869c43eb9c4170d47a24987d987bfd`;
the builder validates and packages it. Sâu art remains unchanged. There are
**66 offline age-aware entertainment beats** (18 Sâu, 18 Ong, 18 both + 12 common),
including nature, vehicles, shapes, friendship, basic safety and counting.
New local-only checks: `test_adventure_dashboard.py`, `test_ong_character_art.py`,
`test_adventure_hud_ong.py`; static source and artifact checksum checks verified
via connector, but **no Gradle unit/assembleDebug or physical Fold3 measurements
performed in this ChatGPT session**. Existing GPS source safeguards, external
DeX single-display design, parent F10 menu, audio-only mode, no CI and no new
permissions preserved. The concept image was a design reference, not a generated
APK screenshot. See [three-audience HUD](../docs/ADVENTURE_HUD_ONG.md).

## Sâu 4 tuổi personalized four-costume cartoon companion (2026-10-10)

**SOURCE + ART PAYLOAD COMMITTED on main; GRADLE/FOLD3 VALIDATION OPEN.** A transparent PNG with 4 authored cartoon portraits of Sâu (explorer, firefighter, pilot, police) is stored as 5 UTF-8 Base64 fragments (`art/assets-source/characters/sau-costumes.b64.part01..05`) with verified payload hash SHA256 `3cd623f54e37cf7c3d6e9e6a371f5dd280ba2f0d9df5c9575917125ac8427f6d`. `tools/build_sprites.py` automatically rebuilds `assets/characters/sau-costumes.png` (224x336); Gradle declares input/output and Android packages `../assets`. Added `CartoonSprites.drawSauCompanion` as one extra per-frame image draw, mode SAU/BOTH only, auto costume change via offline entertainment in `KidsActivity`. No raw personal photographs or full-resolution portraits in GitHub; only stylized low-resolution avatars. Added local source+PNG contract `tools/test_sau_character_art.py` in `scripts/test-local.sh`. No CI, no new GitHub branches, no extra sensitive telemetry. **Build/performance/visual alignment after this commit not measured on Fold3.** Docs [Sâu artwork](../docs/SAU_CHARACTER_ASSETS.md).

## T-024 / T-025 — Soundscape and child engagement QA (2026-10-10)

**SOURCE IMPLEMENTED on main; local full Gradle build, physical Fold3 DeX acoustics and real Sâu/Ong observation OPEN.** T-024 adds off-main-thread locally synthesized WAV SoundPool effects (chime/bird/cat/rabbit/bus), optional low-volume ambient music (**default OFF**), TTS ducks ambient, F10/pause/background silences audio, and Vietnamese offline voice slowed for Sâu 4/Ong 3. TTS cancellation after audio focus loss no longer triggers normal completion; child dialogue timeline recovers. Effects default ON but require sample load. No new permissions, no external media, no system volume changes or CI. **Third-party Google Maps/VietMap audio coexistence NOT field validated.**

T-025 adds in-memory `ChildEngagementMetrics` (Sâu/Ong/BOTH output turns, completions, POI preemptions, non-POI cancellations, voice unavailable, parent pauses), visible in same-display F10 menu, plus JUnit and local Python audio/engagement contract, with child-safe human observation protocol. This measures **app output, not child interest**; no child recording, GPS analytics or new cloud sharing. Document [T-024/T-025 audio and child QA](../docs/T024_T025_AUDIO_CHILD_QA.md), issues [#24](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/24) and [#25](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/25). Execute `./scripts/test-local.sh` and `./gradlew :android:assembleDebug` locally before real passenger QA; **not run here**. Source contracts written, no claim of PASS or acoustic acceptance.

## T-022 / T-023 — Sâu, Ong and Two Kids Entertainment (2026-10-10)

**SOURCE IMPLEMENTED on main; LOCAL GRADLE/PHYSICAL QA PENDING.** Parent dashboard now selects Cho Sâu (4), Cho Ong (3), or Cả Sâu và Ong (3–4, default), stored locally. New pure-Java `EntertainmentDirector` supplies 48 offline fictional preschool beats, balanced BOTH rotation, solo-safe lines, first event ~8s, subsequent starts ~42s, native Vietnamese narration and child-sized captions, with Capybara WAVE/SURPRISE. Live/GPX sourced POIs supersede fiction; no extra permissions/cloud or CI. GitHub source/data static review 13/13 PASS; **full Gradle/JUnit/device tests not executed here**. New local Python source contract and JUnit tests included in `scripts/test-local.sh`. Parent/scene/audio lifecycle requires Fold3 testing. See [guide](../docs/T022_T023_TWO_KIDS_ENTERTAINMENT.md), issues [#22](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/22) / [#23](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/23).

## T-021 — Physical Fold3 autonomous QA (2026-10-10, DONE — automatic scope)

Owner Main; dependencies T-001–T-019. Physical SM-F926B / Android 15 / DeX2. FullHD GL 1920×1080, real 60-minute DEMO PASS: 59.963 FPS / P95 upper bound 19ms, 120 progress/display checks, normal deadline expiry. Warm PSS 147007–147883kB, CPU windows 29–30%, thermal status 0; whole-device battery 96→90%, battery temperature 30.8–31.5°C. No uncontrolled upward PSS trend observed in this workload, no claim of zero leaks.
Ten local contract scripts + 93 core tests PASS; Android unit NO-SOURCE; both APK builds SUCCESS. Initial eleven physical modes including SAF PASS; final eleven script modes including expanded Parent actions PASS after fixing a hidden AlertDialog list. Sound playback PASS: local tone/media active, production Vietnamese offline narrator callbacks and native TTS onDone; volume preserved, no recording. Preferences restored, microphone/location remain denied. Endurance measured pre-menu-fix APK; final APK separately regression-tested.
Final app SHA256 `b13f3d45dffd4bdce500b04980dac3befc3ddf01041297a4bfc3cbaff797f139`. Evidence `.agent/evidence/T-021-fold3-autonomous-qa-2026-10-10.md`; completed plan `.agent/plans/completed/T-021-fold3-autonomous-qa.md`; procedure `docs/FOLD3_AUTONOMOUS_QA.md`. Main only, no CI.
Real road/POI review, Vietnamese ASR accuracy/human voice quality, navigation concurrency, cable recovery, provider API calls, external signing and signed release remain NOT VERIFIED. No GPS injection or provider calls. Earlier status sections below are historical; T-021 supersedes their pending local build/physical DEMO performance/playback checks only.

## T-020 — OSM route corridor filter (2026-10-10)

Implemented local optional `tools/prepare_osrm_route.py` (OSRM manual route fetch) and `tools/fetch_overpass_chunks.py` (bounded, rate-limited OSM fetch); implemented `tools/road_poi_pipeline.py` for saved full-detail actual-road GPX/GeoJSON, chunked Overpass QL, offline import, node/way segment-distance filtering (180m default), on-geometry nearest points, duplicate filtering and SHA/provenance JSON. Synthetic local test `tools/test_road_poi_pipeline.py` added to `scripts/test-local.sh`. Documentation: `docs/ROAD_CORRIDOR_POI_PIPELINE.md`. **No actual five-route road tracks or large OSM extracts in repo:** candidate count unverified, live 40 points unchanged, approval pack empty; no claim of production route-match or field validated POIs. CI remains removed.

## T-019 — Expand LIVE offline POIs to 40 (2026-10-10)

Added 27 OSM-ID/Mapcarta-sourced candidates, from 13 to **40** across 6 route areas (Đồng Nai 7, Vũng Tàu 6, Phan Thiết 6, Bảo Lộc 5, Đà Lạt 9, Nha Trang corridor 7). Each new point has VN narration, quiz/answer/chat, coordinates, source and `humanReviewed=false`. See `docs/POI_EXPANSION_2026-10-10.md`. LIVE GPS gating unchanged; no road matching or production approval; `reviewed.tsv` stays empty. CI remains removed. Full Gradle tests and physical Fold3 still pending.

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

## M7/M8 owner decision + implementation (2026-10-09)
- Branch feat/T-008-T-009-single-display-controls / draft PR #13 targets development branch (PR #1 remains draft).
- Parent menu now opens with ONE mouse click or F10 on the SAME DeX display; no PIN, phone-touch, kiosk or lock screen.
- M7: Audio-only setting and live toggle skips scenery/sprite rendering, leaves GPS/GPX/POI/story, Android subtitle layer and session deadline active. Quiet mode remains ON and offline speech consent OFF by default. Added 60-minute option and reset preferences.
- M8: signature-permission STOP/PAUSE/RESUME on same-process Activity; no public START, separate-phone UI or unprotected command receiver.
- Static guard and Android mouse instrument authored. CI/hardware acceptance is separate. Real Fold3 DeX, external Assistant signing/voice, Maps/Vietmap audio and physical FPS NOT VERIFIED. No milestone DONE.
- See docs/M7_M8_MOUSE_IPC.md and CI for PR #13.

# DeX Kids Adventure — verified status

## Latest ADB integration rerun — 2026-10-09
- GPX follow-up RESOLVED on emulator: reproduced Android unsupported XML security
  feature before parsing; replaced with bounded strict decode/DTD rejection and
  rejecting resolver.82corePASS/buildSUCCESS; actual bundled sample and real SAF
  Downloads GPX replay PASS, Android UTF8/UTF16 entity rejection PASS. Fixed APK
  installed. Evidence `.agent/evidence/T-011-gpx-android-parser-fix-2026-10-09.md`.
- Earlier GPX investigation lacked exception details; superseded by the reproduced
  failure and verified correction above.
- Source `6c8a9df`; AVD ZFold3_API35/API35, emulator-5580 started for actual testing.
- 80 core tests PASS; desktop classes and Android debug/AndroidTest builds SUCCESS.
- Actual recreation/deadline/feed continuity, same-display F10 parent controls/end
  and offline demo/recreation PASS. Sampled game/menu screenshots inspected.
- GPS refusal BLOCKED: existing fine/coarse grants preserved. SAF/live GPS/HCMC
  narration/theme runtime and physical DeX remain uncovered by this rerun.
- Radio state restored, test APK removed, app returned to ParentActivity.
- Evidence: `.agent/evidence/T-011-adb-integration-2026-10-09.md`; T-011 IN_PROGRESS.

Updated: 2026-10-08 (Asia/Ho_Chi_Minh)
Development branch: `feat/T-001-bootstrap-libgdx`; PR #1 is DRAFT, no production release.

## Verified code / CI
| Milestone | Last verified evidence | State |
| --- | --- | --- |
| M0 bootstrap | [37795183419](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37795183419) Android+desktop compile | IN_PROGRESS (device runtime and repo visibility) |
| M1 libGDX | [37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) Xvfb OpenGL screenshots at 1280x720 & 1920x1080 | IN_PROGRESS (hardware P95, lifecycle) |
| M2 art/animation | [37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) 18 original SVG sprites, 2048 atlas, 10 sequential GL frames/GIF and Android APK | IN_PROGRESS (art approval, hardware, narration sync) |
| M3 GPX simulation | [37796769631](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37796769631) | IN_PROGRESS |
| M4 GPS/POI scaffolding | [37798067824](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37798067824) | IN_PROGRESS |
| M5 narration scaffolding | [37797120923](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797120923) | IN_PROGRESS |
| M6 dynamic journey | [37797221624](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797221624) | IN_PROGRESS |
| M7 parent UI | [37797413766](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797413766) | IN_PROGRESS |
| M8 DeX+IPC scaffolding | [37797528265](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797528265) | IN_PROGRESS |
| M9 release benchmark | [37797706168](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797706168) virtual soak only | BLOCKED |

**M2 latest verified source SHA:** `e63e21cc2374a95d36cdba837792382534daac3c`. A HUD accuracy wording fix `b9661a0c335976af05879b87e0af85abbe44e32a` is not yet included in these CI results. The original 720p/1080p screenshot artifact and animation GIF were actually rendered by LibGDX in Xvfb/Mesa software GL; they are not AI mockups.
Artifact links: [actions run 37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673), screenshots/GIF id **11563302286**, generated PNG+TextureAtlas id **11562987678**, Android debug APK id **11563282420**.
The SVG sources are in `art/assets-source/`, generated raster content in `assets/generated/` (build outputs).

## What M2 implements
- Capybara idle, blink, wave, talk-open frame, sleep, surprised; time/state-driven model and explicit narration-active gate. Does not pretend to narrate without reviewed content.
- Original yellow bus, lighting highlights, independent rolling wheels calculated from absolute traveled meters; bounded suspension/tilt animation.
- Original cloud/hills, home/building, bridge, bush/flower, street lamp, tree and glow assets; layered 2D parallax from stable journey distance.
- Biome scenery grouped into deterministic four-chunk districts to reduce abrupt geographical scene changes. Biomes are fictional until ground truth verified.
- Unit tests for animation states, vehicle movement, chunk continuity and district stability; source-atlas validator; desktop OpenGL captures and actual GIF preview.

## Unverified and release blockers
- No Android emulator, physical Z Fold3 or Samsung DeX display has run in this execution context.
- No measured Fold3 30 FPS/P95 frame latency, 60-minute actual soak, thermal, PSS or battery evidence. Render FPS shown in screenshots belongs to CI software OpenGL only.
- No production-approved assets, voice-synced talking frame or curated real-world POI/narration pack.
- No complete DeX-Assistant cross-app signing/command and navigation audio coexistence acceptance. Existing Assistant repos unchanged.
- No signed production APK, license release sign-off or validated upgrade path.
- Repo is currently **PUBLIC**; master requirements requested PRIVATE. Owner action required.

**No task/milestone is DONE.** Next gate: Z Fold3/DeX runtime smoke → manual art visual approval → narration/audio synchronization → actual 60-minute benchmark → release readiness review.

## M2 latest art+animation evidence — 2026-10-08
- Source commit: `e63e21cc2374a95d36cdba837792382534daac3c`.
- GitHub Actions [run #37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673): **SUCCESS** (core tests, 18-region sprite/alpha validation, Android debug APK, two software OpenGL desktop runs, 10-frame GIF preview).
- Artifacts: screenshots/GIF 11563302286, APK 11563282420, sprites 11562987678.
- T-003/M2 remains **IN_PROGRESS**. Final art signoff, verified narration synchronization, seam checks and physical Z Fold3/DeX 60-minute test: **NOT VERIFIED**.
- Next hardware step: install latest CI debug APK and capture actual DeX landscape screenshots, FPS/P95, memory and animation-state behavior; do not mark DONE on CI alone.

## Local simulator checkpoint — 2026-10-09 08:53 Asia/Saigon (latest)
Task T-011 IN_PROGRESS; source commits `923f297` (GPX fix/tests + metrics), `a4e1f34` (session/final metrics logs).
- WSL2 Ubuntu22.04; build JDK17.0.20.1, Gradle8.11.1, Linux SDK35.
- Windows emulator37.1.11, WHPX, NVIDIA RTX4060 GLES translator; AVD `ZFold3_API35`,
  serial `emulator-5580`, Android15/API35. Generic foldable emulator, NOT Samsung Fold3/DeX.
- Actual local core tests: 45 PASS / 0 FAIL; desktop classes and Android debug APK build SUCCESS.
- Debug APK installed and game actually rendered. M1 UI/lifecycle automation completed;
  screenshots pending consolidated visual review. Atlas validator PASS, 123-second real smoke capture completed.
- GPX terminal-speed bug reproduced FAIL then fixed; regression PASS. No milestone DONE.
- Unauthorized IPC probe with distinct UID/certificate PASS; direct child shell launch rejected;
  external-display absent causes no phone fallback. Earlier harness failures retained, not hidden.
- Real 3600-second soak currently RUNNING: `build/simulator-artifacts/stability-test-20261009T015322Z-7753/`;
  unified exec session 86854. Do not reinstall/resize/stop app during collection.
- Synthetic parent prefs (60-minute existing limit, quiet ON/TTS OFF), initial originals absent:
  restore after collection with `ADB_BIN=/mnt/d/Android/Sdk/platform-tools/adb.exe python3 scripts/simulator/restore-soak.py --serial emulator-5580 --setup build/simulator-artifacts/soak-setup-20261009T015127Z`.
- Physical Fold3/DeX, verified OSM/content/audio and release acceptance remain blocked/open.
- New scripts/evidence pending commit; existing 140-file CRLF-only user changes preserved.
  `gradlew` locally normalized for WSL execution. No other repository accessed.

### Checkpoint 2026-10-09 09:26
- Actual56 core tests PASS and Android debug/instrumentation APK build SUCCESS; resource-dispose
  and accepted-only GPS freshness fixes verified, local source HEAD f8316fc.
- Git CLI push blocked by missing temporary credential helper (store fallback also unavailable).
  Published verified core checkpoint through GitHub connector on the existing development branch:
  remote ea8597e952e53fa9fc55f3cde08c61125c97a89f, tree d45a170478378521c86e1a5ad9830b49e0ca3413
  exactly matches local f8316fcd00396c9843e372238cade0079a7fcd66 tree. No forced update/main/PR merge.
- Soak elapsed>30minutes with same PID5991; mean~47FPS, P95 upper43ms. Final analysis pending.
- Persistent Python QA venv installed at /home/khuongnd/.local/share/dexkids-qa-venv;
  bootstrap-python.sh + tools/requirements-art.txt reproduce CI-pinned versions.
- Android absolute deadline and pure feed retention source compiled; actual recreation test pending.
  Instruments use FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES; initial emulator enabled-services list null.
- Remaining new scripts/docs/Android edits are intentionally uncommitted pending runtime evidence.


### Final checkpoint 2026-10-09 10:25 — resume here
- App source bad299a;56/56corePASS, Android/debug/test buildsSUCCESS,18SVG/atlasPASS.
- Real emulator baseline a4e1f34 child lifetime exactly3600seconds; expected60-minute expiry.
  Strict sampler exit4 retained. Recorded3571.765s prefix47.473FPS/P95 upper43ms,221PSS samples
  51.32–52.04MiB/end+134KiB;0observed crash/ANR. Full histogram/stable30FPS NOT_VERIFIED.
- Actual resize/resume/5reloads, distinct recreation preserving deadline/feed/distance,
  different/same-debug-signature STOP, denied-location explanation and truly offline preview/
  recreation PASS. Corrected baseline permission harness reproduced app refusal-message FAIL;
  final sourcePASS. Final sampled123s smoke captured126s and actual GL dispose logPASS.
- Seven behavior fixes committed. Desktop default WSLg SIGSEGV unresolved; llvmpipe retryPASS.
  Early QA permission grant incident revoked, retained honestly; final harness no grant path.
- Original first parent-pref backup restored, wm1768x2208/density420, radios restored, FINE/COARSE
  denied, USER_SET/USER_FIXED cleared; internal selected-accuracy flag may remain. Probe/test APKs
  removed; final app on ParentActivity. Accessibility listnull before/after, no other repos touched.
- Evidence `.agent/evidence/simulator/final-test-summary.md` / `test-matrix.md`; raw artifacts
  localonly in ignored `build/simulator-artifacts` (also accessible D:\DeX-Kids-Adventure\build).
- Mandatory60cases40PASS/0FAIL/1BLOCKED/1NOT_RUN/8NOT_IMPLEMENTED/10NOT_VERIFIED; historical
  failed sampler/nativeGL/QAgrant events separately retained. No milestone markedDONE.
- M0–M2 tested subset SIMULATOR_VERIFIED, all unfinished; M3 core subset verified; M4–M8 missing
  verified content/runtime/physical gates remain open; M9 physical60-minute Fold3/DeXBLOCKED.
- Publish source/scripts/reports on existing feat/T-001-bootstrap-libgdx through lease-checked
  GitHub API (CLI helper missing), verify exact content tree; keep draftPR#1 unmerged.
- Next: M3 Android GPX/ADB GPS integration and denied/live continuity; M2 full visual seam/night
  review. No new real POI/narration without verified pack. Existing user CRLF-only changes kept;
  no outstanding QA process/soak. Remaining small QA docs staged/committed in final snapshot.

## T-003 Capybara face alignment, companions & vehicles — 2026-10-09
- Edited six original Capybara SVG expressions to align eye line, muzzle, nose, philtrum, mouth and blush. Face landmark checks are executed in GitHub Actions.
- Added 5 fictional animal-companion sprites (rabbit, fox, panda, cat, penguin) and 6 traffic/transport sprites (car, taxi, truck, minibus, scooter, bicycle) to the **same** game atlas: 29 regions total.
- `SceneryCast` selects deterministic props by biome/seed; companions do not appear in rivers/on bridges, vehicles are not presented as real live GPS traffic. Shifted starting roadside companion away from the bus foreground occlusion.
- [Source CI #37879894270](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37879894270): **SUCCESS**, includes geometry test, Java unit tests, atlas alpha/regions, 720p/1080p OpenGL and generated visual cast sheet. Screenshot artifact id 11594260235; sprite atlas id 11593323780; debug APK id 11593184361.
- Penguin anatomy update `ec459c6` passed [CI #37880193189](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37880193189). First companion visibility fix `d6cd0dc` passed [CI #37880239651](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37880239651), with unit tests, atlas, actual 1920×1080 OpenGL screenshot showing a rabbit on the sidewalk outside the bus, and Android debug APK output. Artifact IDs: screenshots/cast GIF **11593769502**, atlas **11593724708**, APK **11594465217**.
- [Cast and art inventory](https://github.com/khuongnd0901/DeX-Kids-Adventure/blob/feat/T-001-bootstrap-libgdx/art/ASSET_CAST.md); artwork is **draft**, final design signoff and simulator/Fold3 acceptance not done.
- M2/T-003 stays **IN_PROGRESS**, M9 remains **BLOCKED**. Existing DeX-Assistant repos are untouched.


## Concurrent artwork integration checkpoint — 2026-10-09

Preserved remote artwork history through 1a3a7d9 in isolated worktree
`/tmp/dexkids-qa-integration-20261009`, branch `feat/T-011-simulator-validation-integration`.
Original worktree's pre-existing CRLF changes were not discarded. Merge source
`d5ee691cb81ffe6594bedaba96780891cfa1c721` compiles core/desktop/Android/test APK.
Core now **58 tests, 0 failures/errors** (two additional scenery-cast tests).
29-region atlas metadata/transparency validator PASS; face validator PASS only for
its explicit muzzle/nose/cheek/nonempty-mouth assertions, not full anatomical or
eye alignment. New companions/traffic are now in renderer/atlas; fictional props,
not GPS detections. Asset inventory was updated upstream; final art/license approval remains open.

Real emulator install SHA256 `5712f9d737558d674853ac57fcc7db3b233d08ceb98d567f1556daba363664d0`.
Actual Activity recreation PASS in `activity-recreation-20261009T034213Z-25024`: deadline
9678316 unchanged, same_feed=true, distance20.627→49.927m; actual PNGs reviewed.
Rabbit/traffic are visible; a roadside rabbit is partially occluded behind the bus
at one sampled position, so exhaustive cast visibility/art approval is NOT_VERIFIED.
Earlier face-only integration `bb9fdee` recreation/resize/reloads completed separately
(`activity-recreation-20261009T034005Z-24412`, `functional-20261009T034020Z`).
Final cast UI run and short smoke are recorded below when completed.
Raw artifacts remain local at `/mnt/d/DeX-Kids-Adventure/build/simulator-artifacts/`.
The 60-minute run remains source a4e1f34, **not validation of new artwork**.
An integration build attempted before resolving a merge conflict failed; resolved
source rebuilt successfully (`integrated-cast-final-build.log`).
Publication of final QA snapshot was delayed by concurrent remote changes;
GitHub lease/equal-tree verification is required before claiming final publication.

Latest cast UI run `functional-20261009T034229Z` completed:8 PASS UI rows and
1 inconclusive shell-broadcast NOT_VERIFIED. Actual720p/1080p/density160/240,
HOME/resume and five reloads executed; sampled resize/reload PNGs reviewed,
Capybara/bus/rabbit/traffic render intact. HUD cadence during disruptive UI tests
was low (sampled5.5–17.3FPS); this is not a stable30FPS acceptance result.
Existing matrix totals above describe baseline cases, not additional duplicate
runs. Separate final artwork smoke is running in `stability-test-20261009T034450Z-25275`;
its result must be appended after real completion.
GitHub milestone issues #2–#12 received honest checkpoint comments; all remain open.


### Final integrated-cast smoke completed
`stability-test-20261009T034450Z-25275`:123 real wall seconds,9 resource samples,
PID18912 unchanged,0 PID fatal signatures; start/end screenshots manually reviewed
with intact scene. Smoke launch/render/no-restart PASS. PSS57100–61206KiB,
last-first −4106KiB. Last logged cumulative scene prefix2979frames/150.264s
=19.825FPS/P95 upper86ms; prefix begins before sampler, so this is not120-second
window FPS. **30FPS threshold FAIL for this observed prefix**, full sustained
performance and system-wide ANR completeness NOT_VERIFIED. No hardware acceptance.
Do not attribute low FPS to new sprites without a controlled equal-host A/B.
Next task includes controlled old/new-art profiling, complete visual review and
Android GPX selection; source architecture is unchanged by this evidence update.
Original parent fixture/display settings restored; installed app remains integrated
cast APK; QA instrumentation removed. Permission/accessibility state checked separately.

Final QA publication uses a content-tree-verified API commit based on a7f8fb0;
remote lease rejects stale heads, with no force/main push or PR merge. CLI credential
helper is unavailable. If publication fails, resume from the isolated integration
worktree; original worktree retains existing uncommitted line-ending changes.


## Published receipt — 2026-10-09 10:50 ICT
Verified development branch publication: `cdb9a178d5b8140ecb7a0a6b27a35028e3e5d0d6`,
content tree `c80a19e5db87b5ec6e34dd027c8ec16e79109028` exactly equals local
integration commit6195329 tree. Commit parent a7f8fb0 preserves concurrent art
history. Never forced, never pushed main, PR#1 remains draft/unmerged.
CI run37881024109 is IN_PROGRESS at receipt, not PASS.
Milestone issues#2–#12 updated with execution/gate comments; none closed/DONE.
Actual restored fine/coarse=false, accessibility=null; no ANR since boot in
`integrated-cast-lastanr.txt`; own exit history only expected install/instrument/
force-stop events observed.

Resume source work in `/tmp/dexkids-qa-integration-20261009` on
`feat/T-011-simulator-validation-integration` (clean committed integration).
Original `/mnt/d/DeX-Kids-Adventure` retains pre-existing CRLF changes and an older
source branch history; its STATUS/evidence checkpoint is updated but **do not
build original source and call it the published integrated source**. Original
raw artifacts remain at `build/simulator-artifacts/`, shared by evidence paths.
Next: controlled equal-host old/new-art FPS profile; M3 Android GPX selection/
ADB synthetic GPS continuity; remaining visual coverage. Physical Fold3/DeX,
verified POI/narration/content, production signing/release gates stay open.

Latest receipt branch commit1b2958b was accepted with expected-head lease.
CI run37881130669 was IN_PROGRESS (not PASS) at10:51 ICT. Final CLI fetch
stalled; bounded retry does not change verified publication through GitHub API.
Only the agent-owned stalled fetch process was terminated; no emulator/app
process or unrelated service was killed. TASKS now includes integrated58-test
result and observed FPS threshold failure.


## T-011 local merge conflict resolved — 2026-10-09
Conflict was local `.agent/plans/active/T-003-game-art-atlas.md`, merging
remote fc308b9 into local a33bf0c. Retained QA checkpoints and incoming artwork
iteration, removed only three conflict marker lines. Original conflicted bytes
backed up under ignored `build/simulator-artifacts/conflict-resolution-20261009/`.
Merge commit8bc27c6 completed; no unmerged index entries or active MERGE_HEAD.
Resolved tree8a42a4c39bf8a8fe2c30b17c715e6222b3a52050 equals published fc308b9
exactly, including all source and docs. Existing unstaged CRLF changes preserved.
PR#1 mergeable=true, draft=true, merged=false at inspection. No source behavior
changed; reused exact-source CI37881295170 SUCCESS rather than inventing a new
build/test execution. Root committed source now matches integrated source;
earlier warning about older root committed source is superseded. Pre-existing
working-copy line-ending changes remain; isolated worktree is still available
for builds. This receipt is the only additional change to publish.


## T-011 commit-all checkpoint — 2026-10-09
User explicitly requested committing/publishing all remaining workspace changes.
Byte audit found150 tracked differences exclusively CRLF/LF, no semantic source
differences; no untracked nonignored source files. Converted those working-copy
text files and gradlew to LF; `.gitattributes` now normalizes text to LF across
Windows/WSL. No user logic discarded; build/log/private artifacts remain ignored.
All source matches the previously verified integrated snapshot. This supersedes
prior notes saying original working-copy line-ending changes must remain dirty.
Validation: byte-equivalence audit, staged diff checks and Gradle wrapper execution;
no new simulator/performance PASS inferred. Development branch only, no PR merge.


## Hardware constraint correction — single-display-only DeX (2026-10-09)
- User's Samsung Z Fold3 built-in display is damaged. Parent controls **cannot** run on its touchscreen while child game is on an external DeX monitor.
- Updated `ParentActivity`: dashboard and game are sequential on **same display** via ordinary Activity start; removed its calls to `DisplayRouter` cross-display launch. [Single-display spec](https://github.com/khuongnd0901/DeX-Kids-Adventure/blob/feat/T-001-bootstrap-libgdx/docs/SINGLE_DISPLAY_DEX.md).
- `KidsActivity`: native Android on-screen long-press menu and keyboard F10/Menu, Continue or End adventure, same display. Popup pauses LibGDX clocks and foreground GPS listener; elapsed-time limit still enforced.
- Source-level static contract, Java unit tests and Android debug + AndroidTest APK compilation PASS in [CI #37883012846](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37883012846). OpenGL desktop visual smoke PASS. New `single_display` Android instrumentation is compiled but NOT EXECUTED on emulator/DeX here. Do not claim physical Fold3 DeX verification. No DeX-Assistant repo changed.
- M7 parent controls and M8 display integration remain **IN_PROGRESS**. Next: test on simulator first, then physical DeX with mouse/keyboard and broken phone display.

## T-004/M3 — Android GPX Replay implementation checkpoint (2026-10-09)
- Android ParentActivity now has **Choose GPX file and start REPLAY** on same DeX screen (Storage Access Framework `ACTION_OPEN_DOCUMENT`). Preview DEMO and live location flows remain present.
- KidsActivity loads selected `content:` URI with a 4 MiB limit and 20K point ceiling, validates GPX timestamps/coordinates on worker thread, and never starts the Live GPS adapter. The AndroidApplication/LibGDX lifecycle is initialized synchronously using `DeferredGpxJourneyFeed` to avoid NPE onResume during background file parse.
- Child game has bottom-left **Pause / Resume / Restart** controls and elapsed time/distance readout. Commands execute on LibGDX render thread, status is observed through volatile snapshot fields. Parent F10/long-hold same-display menu and absolute session deadline are preserved; configuration recreation retains feed in memory (not process-death persistence).
- Invalid/unreadable file: fail closed with return-to-Dashboard dialog; no synthetic demo substituted. GPX does not claim real POIs.
- [Documentation](https://github.com/khuongnd0901/DeX-Kids-Adventure/blob/feat/T-001-bootstrap-libgdx/docs/GPX_REPLAY_ANDROID.md).
- Core bounded-input, secure parsing and replay model tests: [CI 37884079192](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37884079192) **SUCCESS**.
- The full M3 source through `c7183ec1` passed [CI #37884357999](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37884357999): core tests, Android debug + AndroidTest APK compilation, source contract and 720p/1080p Xvfb OpenGL smoke; screenshots/atlas/APK uploaded. SAF picker runtime on Android and physical DeX remain **NOT_RUN/NOT_VERIFIED**.
- **M3/T-004 remains IN_PROGRESS**, no physical test gate is incorrectly closed. User explicitly defers physical device testing.

## T-005/M4 offline OSM POI — 2026-10-09
- Source: opt-in LIVE quality-gated and GPX SIMULATED position adapters, indexed proximity/distance/accuracy confidence, deduplicated NEARBY/APPROACHING/PASSING_CANDIDATE detection, Android native Vietnamese Unicode overlay, no location history upload or named narration.
- Offline candidate importer generates **UNREVIEWED** TSV + SHA256 source receipt from a saved Overpass JSON extract; reviewed pack + review ledger/ODbL attribution validated by CI.
- **Approved pack is empty**. Until a human verifies OSM feature locations and fills review ledger, the app displays 'no reviewed POIs' and never asserts actual named places. This is deliberate safety, not a regression.
- Baseline core source [CI #37891960202](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37891960202) SUCCESS; the first engine compile test failed due to lambda capture (#37892078068), fixed and confirmed [CI #37892403773](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37892403773) SUCCESS. Integrated M4 source up through `b701a0ae` passed [CI #37892751478](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37892751478): reviewed-pack ledger/pipeline tests, JUnit, Android APK+AndroidTest compilation, OpenGL 720p+1080p smoke and uploaded artifacts. No Android runtime named-POI test has been run.
- See `docs/POI_DATA_PROVENANCE.md`, `.agent/plans/active/T-005-real-gps-poi.md`, `.agent/evidence/T-005-M4-offline-engine-2026-10-09.md`. T-005/M4 **IN_PROGRESS**; physical DeX tests deferred by user.


## M4 sample & M5 offline Vietnamese narrator — 2026-10-09
- Bundled four **source-cross-checked, NOT production-approved** sample POIs with exact OSM way IDs and approximate locations (Tao Đàn, Dinh Độc Lập, Bảo tàng Chứng tích Chiến tranh, Bưu điện Trung tâm Sài Gòn), each with independently sourced short original Vietnamese cue, synthetic GPX route and JSON source ledger. Primary OSM feature endpoints could not be accessed; secondary OSM-derived map metadata + official editorial references used. See `docs/M5_HCMC_SAMPLE.md`.
- Production catalog `assets/poi/reviewed.tsv` and `assets/narration/approved.tsv` remain empty. Preview is explicitly started via **Start HCMC sample journey (preview)** on DeX parent dashboard; same-screen game shows clear preview labeling. No live POI named narration until human source/geometry review.
- M5 narrator: offline-only Android TextToSpeech, parent speech opt-in + quiet OFF, captions even with no offline Vietnamese voice, transient ducking audio focus, activity pause and parent F10 menu stop speech, real TTS onStart/onDone/onError drives Capybara talking sprite. Age gating, 30s cue cooldown, no generated facts/network voice/location uploads.
- Added `OfflineNarrationCatalog`, `HcmSampleJourneyIntegrationTest`, `OfflineNarrationCatalogTest` and Python sample audit step.
- Final full code [CI #37905938576](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37905938576) **SUCCESS**, commit `09b5efd2`, JUnit (including synthetic GPX-to-POI-to-cue), Android debug + AndroidTest APK compile, source audit and 720p/1080p desktop OpenGL; artifacts APK 11604377961, atlas 11604801374, screenshots 11604861161. No Android simulator/physical Fold3 audio playback was executed here; audio focus and actual speech sync remain NOT_VERIFIED.
- M4 and M5 **IN_PROGRESS**, no milestone DONE, PR still DRAFT.


## M6 T-007: POI-driven dynamic art transition — 2026-10-09
- Source: `PoiSceneDirector` maps vetted/preview-only source-backed M4 POI notice types into stylized chunks, with deterministic distance anchoring and bounded crossfades. Maps PARK → flowers/trees, BRIDGE → symbolic bridge, RIVER → riverside, MUSEUM/LANDMARK → URBAN buildings, NATURE → countryside. No road map matching claimed.
- Only NEARBY/APPROACHING events with confidence >=0.55 can request themes; PASSING_CANDIDATE deliberately ignored. Themes fade over 2.5 s and naturally expire after 26 s/800m, clear on GPX reset, freeze with parent menu.
- Initial synthetic HCMC GPX + four sample POIs are eligible only in explicit sample preview, never default LIVE without approved data. Production-reviewed POIs remain ZERO. Character speech on TTS only, independent of themed scenery.
- Pure JUnit + actual Xvfb OpenGL POI preview screenshot CI being run. See `docs/M6_DYNAMIC_WORLD.md`; T-007 **IN_PROGRESS**. Hardware DeX explicitly deferred.

## Verified M6 automated checks — 2026-10-09
- [CI #37907139530](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37907139530): new `PoiSceneDirector` JUnit PASS.
- [CI #37907599024](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37907599024): deterministic synthetic GPX → source-checked HCMC POI → M6 theme integration test PASS.
- [CI #37907989363](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37907989363): **SUCCESS** including M6 water SVG, 30-region atlas alpha checks, Android debug/AndroidTest builds, desktop 1280x720+1920x1080 OpenGL and **third 1920x1080 HCMC theme preview screenshot**.
- [CI #37908099168](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37908099168): **SUCCESS** after asset license documentation.
- M6 screenshot artifact ID **11605695877** (`m6-hcm-preview-1920x1080.png` and 10 frames); atlas **11605511141**; debug APK **11605336770**.
- Actual 1920x1080 screenshot reviewed: PARK theme reaches **100%**, bus/Capybara render visibly; the preview banner clearly identifies synthetic GPX and makes no factual road claim. Source screenshot also showed new illustrative label slightly below the HUD background; corrected by `96b0ff14` (CI pending at this checkpoint).
- No physical Samsung Fold3/DeX benchmark or approved named-POI field validation claimed.


### Final HUD-fixed source acceptance — 2026-10-09
- Commit `96b0ff14` extends the HUD background to include the clearly readable **ILLUSTRATIVE SCENERY: PARK (100%) [GPX SAMPLE]** label.
- [GitHub Actions #37908584642](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37908584642) **SUCCESS** after that HUD fix, with core JUnit (including POI-to-scene deterministic replay), Android debug/AndroidTest builds, atlas alpha check, three real desktop Xvfb/Mesa render captures.
- Screenshot artifact **11606126138**, APK artifact **11605727094**, sprites artifact **11605796955**. Actual 1920×1080 HCMC screenshot visibly confirms the PARK 100% themed scene with the corrected HUD label inside the background; sampled FPS and P95 in that image reflect **software GL**, not Z Fold3.
- Dynamic theme code reaches its **software CI acceptance gate**; the T-007 **milestone remains IN_PROGRESS** pending full Android runtime/visual review, verified LIVE geographic content and postponed Fold3/DeX physical tests.
