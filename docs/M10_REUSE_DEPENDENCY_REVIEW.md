# M10 — Audit chồng lấn, quy hoạch ownership và thứ tự ưu tiên

**Date:** 2026-10-12 · **Repository base:** `main` @ `24bf3a29e4b9618d6219cb235245482317493180` · **State:** DOC-ONLY REPLAN; no source-art or runtime implementation claimed.

## 1. Đánh giá: có trùng không?

**Kết luận: có overlap về kỹ thuật và acceptance; không có cặp task M10 đồng nhất hoàn toàn cần xóa ID.** Các task T-036..T-042 được **giữ ID nhưng đổi ownership và chia checkpoint** để không làm lại nền tảng hiện có. M10 chỉ bổ sung điều chưa có: original asset regions, staged atlas, điều phối chống lặp theo lịch sử, mapping visual profiles và gallery/metrics về *độ đa dạng*. Các thành phần cũ có nguồn sở hữu duy nhất.

| Đã có | Hiện trạng trong main | Phạm vi M10 được phép thêm | Không làm lại |
|---|---|---|---|
| T-003 / `CharacterAnimationController`, `VehicleMotionModel`, `tools/build_sprites.py` | 6 Capybara poses, bus/wheel, 5 animal friends, 6 traffic sprites, `kids.atlas` 2048², adult-approved old art direction pending | T-036 thêm hình trẻ/Capybara mới; T-037 thêm model/skin phương tiện **thật sự khác**; T-039.A packer M10 độc lập | Không reimplement six core poses, wheel math, asset-ownership |
| T-027 / `tools/build_extras.py`, `CartoonSprites` | 18 sprite props và `extras.atlas` giới hạn 14–24; chọn prop theo biome/chunk | T-038 thêm 96 ý tưởng có ID; T-040 thêm history/cooldown cho existing+new | Không vẽ đè palm/pine/bird/butterfly/kite/... hoặc nới cap `extras` vô điều kiện |
| T-026 / `PoiBackdropRenderer` | 22 SVG panorama map tới 44 POI; texture load khi đổi điểm | Biome prop minh họa không địa danh; phối lớp trước/sau POI khi được phép | Không xây lại panorama địa danh hoặc mapping 44 POI |
| T-007 / `ProceduralWorldGenerator`, `WorldWindow`, `SceneryCast` | seeded procedural districts 4 chunks + friends/traffic selection; time-of-day palette | T-040: trip-level shuffle bag + rolling 8-scene history (có fallback), phase offsets | Không nhân đôi world generator, biome randomizer, sky resolver |
| T-032 / `VisualCue` & `AdventureHud` | Vocabulary hình minh họa cố định từ lesson/verified content, HUD readable | T-041 map visual cue allowlisted → nhiều region/pose hơn | Không xây lại NLP/story-to-cue, màn hỏi/đáp |
| T-033 / `VisualPlan`, `AnimationTimeline`, `AnimationRegistry`, `DynamicSceneComposer` | QUESTION/ANSWER/INTERRUPTED, motion x/y/scale, scene priority POI>GeoWorld>fiction | T-039.B metadata/profile definitions; T-041 extend existing timeline and sprite resolver | Không tạo AnimationEngine/SceneComposer thứ hai, không lặp signaling |
| T-035 / `GeoWorldDirector` và landcover importer | Bộ geo cell theo OSM đang thiếu dữ liệu thật/route hoàn chỉnh | T-038 cung cấp props cho *fictional* fallback hoặc theme được duyệt | Không tự gắn forest/coast/farm với GPS chỉ vì asset có tên đó |
| T-010/T-011/T-012/T-018/T-021/T-026/T-033 QA | Android/JUnit, FPS/P95, source QA, 30m GPX/POI và 60m synthetic baseline ở phiên bản khác nhau | T-042 thêm art gallery, uniqueness histogram, memory/page regression vào harness cũ | Không tạo test harness GPS hoặc benchmark runner thứ hai, không reuse số đo bản cũ làm M10 PASS |

**Ranh giới task nội bộ cần sửa:** T-039 ban đầu ôm cả atlas + animation mới nên chồng với T-033 và phải chờ *toàn bộ* T-036–T-038. T-041 ban đầu lập scene composer mới nên chồng T-033. T-042 ban đầu định xây lại test suite T-026. Nay T-039 tách foundation và art packaging; T-041 là **adapter mở rộng hiện có**; T-042 chỉ là **extension của QA hiện có**.


## 1A. Normalized asset ideas (avoid actual duplicate drawings)

The seven candidate rows below were **replaced at design time** before artwork was produced. IDs and total counts remain fixed (172; 108 P0/64 P1), but source slugs are now genuinely additive and not recolors or repeated pre-existing poses:

| Canonical ID | Previous idea (duplicate/recolor risk) | Corrected source idea |
|---|---|---|
| SAU-001 | `sau_wave` | `sau_high_five` |
| ONG-001 | `ong_wave` | `ong_high_five` |
| VEH-001 | `school_bus_green` | `double_decker_tour_bus` |
| VEH-002 | `school_bus_red` | `camper_van` |
| FOR-008 | `pine_couple` | `hollow_tree` |
| CST-007 | `small_sailboat` | `beach_parasol` |
| CST-008 | `fishing_boat_cartoon` | `fishing_net_rack` |

Reasons: Sâu/Ong already have WAVE in `story-v2`/passenger-v3; the yellow bus and other vehicle sprites can be palette-tinted at runtime (do **not** count another flat recolor as a unique SVG); `pine` and `water_boat` are already packed from T-027. New P0 art must have a distinguishable shape/action/composition and an original source file. No existing source is deleted or modified by this inventory correction.

## 2. Taxonomy / reality check

`world/Biome.java` hiện có **7 enum**: `URBAN, RESIDENTIAL, PARK, RIVER, BRIDGE, COUNTRYSIDE, GENERAL`. **Sáu thư mục art không phải enum hoặc chứng cứ GPS**. Mapping minh họa hợp lệ, *không quyết định địa hình thật*:

| M10 art group | Candidate enum hiện có | Điều kiện / fallback |
|---|---|---|
| urban | URBAN, RESIDENTIAL | Có cảnh đô thị/nhà ở hoặc fiction seeded world |
| village | RESIDENTIAL, COUNTRYSIDE | Dùng như style nông thôn, không nói đây là một làng thật |
| forest | PARK, COUNTRYSIDE | Chỉ chuyển theme đúng theo POI/GeoWorld provenance |
| coast | RIVER (nhóm **water illustration**), GENERAL | **Không mặc định RIVER = biển**; chỉ minh họa nước nếu phù hợp source, nếu không giữ generic |
| mountain | COUNTRYSIDE, GENERAL | Hình núi chỉ là minh họa, không chứng minh tuyến đang đi trên núi |
| farm | COUNTRYSIDE, GENERAL | Không khẳng định có ruộng/trang trại thực tế gần xe |

Renderer giữ một source of truth: `DynamicSceneComposer` chọn provenance, M10 chỉ chọn `ArtStyleTag`/region sau khi *cảnh đã được phép*. Không thay enum/nguồn GeoWorld trong M10. Fallback dùng existing atlas. VisualCue/theme và M10 prop tags là hai khái niệm khác nhau.

## 3. Quy hoạch task và dependency đúng

**Giữ T-036–T-042; chia checkpoint A/B, không tạo số task mới:**

| Thứ tự | Checkpoint | Trách nhiệm mới | Đầu ra tái sử dụng ngay |
|---:|---|---|---|
| 0 | M10-M0 | Audit old/new IDs, pivot/face anchors, style review, 7→6 taxonomy mapping, existing FPS baseline request | Frozen manifest/schema, không đụng runtime |
| **1** | **T-039.A** | **Atlas/manifest infrastructure trước khi vẽ hàng loạt**: source registry/validation, multi-page packer, compatibility old atlases, gallery contact sheet skeleton | T-036/T-037/T-038 dùng ngay để pack/check incremental; bỏ phụ thuộc all art |
| **2** | **T-040.A** | **Chống lặp ngay với 18 props + cast hiện có**: shuffle bag/cooldown 8-scene (nếu đủ candidates), per-trip seed và phase jitter, không random mỗi frame | T-041 adapter, test 10/30m và art sau này không cần sửa selection |
| **3** | **T-041.A** | Adapter nhỏ vào `CartoonSprites`/existing `DynamicSceneComposer`, áp manifest và selector với original assets trước; fallback đầy đủ | Chứng minh M10 runtime pipeline end-to-end với zero new art |
| **4** | **T-042.A** | Mở rộng các test T-026/T-033: histogram, source/pack integrity, 1920×1080 gallery frame capture, baseline | Có test/gate cho mọi batch art tiếp theo |
| **5** | **T-038.P0** | **48 biome props** đầu (8×6), ưu tiên high-frequency scene | Dùng lại packer + selection + adapter + gallery từ bước 1–4 |
| **6** | **T-036.P0** | **24 expressions** (Capybara/Sâu/Ong mỗi nhóm 8), reuse art anchors | Mở rộng reaction thực tế, không thay scene engine |
| **7** | **T-037.P0** | **12 vehicles**; chỉ render nơi phù hợp layer/biome | Không phá wheel layout, tránh trùng cũ |
| **8** | **T-039.B** | **16 FX + 8 UI**, metadata mô tả **26 motion profiles, 8 events** để T-041 map vào *existing* animation timeline | Không xây motion engine thứ hai |
| **9** | **T-041.B** | Tích hợp P0 vào legacy renderer (POI/audio-only/caption-safe), instrument GL and fallback | P0 asset visible + no-regression |
| **10** | **T-042.B** | QA đầy đủ source/device/adult review; nếu pass phát hành art batch đầu | Tiếp tục P1 nếu P0 ổn định |
| 11 | T-038.P1, T-036.P1, T-039.P1 | 48 props + 8 expressions + 8 UI (64) sau P0 | Không chiếm VRAM/effort trước feedback |

Parallel safely: after checkpoint 1, T-040.A and M10 art sheets có thể chạy độc lập; T-042.A có thể khởi tạo harness cùng lúc. **Không để nhiều agent sửa cùng `CartoonSprites.java`/`AnimationTimeline.java`; T-041 là owner duy nhất.** T-039.A owns build/registry; T-040 owns *pure selection*; T-042 owns tests/gallery/evidence.

## 4. P0 vs P1 (không thay ID hoặc giảm tổng mục tiêu)

CSV `art/M10_ASSET_INVENTORY.csv` phân loại theo cột `priority`:

| Group | P0 | P1 | Total |
|---|---:|---:|---:|
| Capybara / Sâu / Ong | 24 | 8 | 32 |
| Vehicle | 12 | 0 | 12 |
| Six biome props | 48 | 48 | 96 |
| FX | 16 | 0 | 16 |
| UI | 8 | 8 | 16 |
| **Total** | **108** | **64** | **172** |

P0 là **first delivery target**, không phải đã hoàn thành. Chỉ declare release-ready khi **>=100/108 P0 assets** thực tế có SVG, atlas, manifest mapping, hiển thị trong demo/game và được human review; không tự cộng procedural tints vào 100. M10 task nào chưa đạt acceptance vẫn IN_PROGRESS/PLANNED.

## 5. Test ownership / explicit blocking gates

- `python3 tools/build_sprites.py`, `python3 tools/test_sprite_atlas.py`, `bash scripts/test-local.sh` từ baseline.
- T-039.A sẽ tạo `tools/build_m10_atlas.py` và `tools/test_m10_art.py`, check deterministic pages/checksum/layer IDs **trước** asset production.
- T-040 unit tests: seed reproducible; 8-scene cooldown **when >=9 eligible scenes**; no scheduler drift over idle/GPX rewind/pause, zero network.
- T-041 gallery smoke: actual one-screen DeX and fallback with old atlas, preserve existing `AnimationTimeline` and POI precedence.
- T-042 extends `docs/T033_CODEX_TEST_CASES.md` and T-026 harness using specific M10 diversity samples. Keep baseline FPS/P95 from exactly same device/build conditions; no fake performance comparisons.
- Final actual 30min Fold3 DeX: >=55FPS average/P95<=25ms **as target**; avoid regressions in narration, GPS, F10, child content quality. No QA PASS claimed by this documentation replan.

## 6. Action for next implementation session

Start **T-039.A**, not T-036: an atlas/source/manifest workflow permits every subsequent drawing batch to be validated and used immediately. Then T-040.A reduces repetition even with existing 18 props and 11 existing cast sprites. Produce a narrow T-041.A renderer slice before mass asset generation, so the first 8 props per biome are actually visible. Continue with T-038.P0 → T-036.P0 → T-037.P0 → T-039.B → T-041.B → T-042.B.

All older T-007/T-026/T-032/T-033/T-035 release/field gates remain open on their own evidence. **This is a rescope/dependency/priority update only; not code or art produced.**
