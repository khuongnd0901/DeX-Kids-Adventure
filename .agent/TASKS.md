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
