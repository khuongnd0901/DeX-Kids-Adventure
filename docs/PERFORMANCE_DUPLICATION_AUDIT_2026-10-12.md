# Runtime resource & duplicated-work audit — 2026-10-12

**Repository:** `khuongnd0901/DeX-Kids-Adventure` · **Audited SHA:** `9dea74415c4fab69d36b3a832cddb930e26c9fce` · **Mode:** READ-ONLY production review, no application code modification.

## Executive conclusion

Có **chồng lấn xử lý/render thật sự** và **asset được nạp/đóng gói chưa cần thiết**, nhất là từ M10. **Không tìm thấy bằng chứng đủ để kết luận memory leak không giới hạn** qua đọc source; CPU%, Java Heap, Native Heap, GPU RAM, FPS và kích thước APK **chưa đo trên Fold3**. Phân biệt (A) duplicate runtime work, (B) unused resident GPU assets, (C) burst allocations và (D) APK storage bloat. Không tự quy RAM GPU thành Android PSS.

| ID | Ưu tiên | Điểm code đã xác minh | Tác động & ranh giới | Cách tối ưu / kiểm chứng |
|---|---|---|---|---|
| R01 | **P0 — RAM/GPU** | `AdventureScreen.java:155–165` khởi tạo `WorldPainter`, `CartoonSprites`, `PoiBackdropRenderer` bất kể `audioOnly`; `CartoonSprites.java:40–160` nạp atlas + 6 texture child; `AdventureScreen.java:327–338` mới rẽ nhánh Audio-only sau update | Texture/sprite không được vẽ vẫn resident. **Dung lượng lý thuyết** với atlas RGBA8888: 2048² `kids.atlas` ≈16 MiB + 1024² `extras.atlas` ≈4 MiB + 1–3 trang M10 1024² ≈4–12 MiB, chưa tính child sheets, GPU driver; *không phải số đo thực tế*. | Khởi tạo renderer/resource theo mode trên GL thread; khi chuyển FullHD↔Audio-only phải tạo/hủy đúng một lần, giữ logic state và TTS/POI. Đo GL resident & PSS sau khởi động và sau chuyển mode |
| R02 | **P0 — GPU/frame time** | `WorldPainter.java:81–136` `paintGround` vẽ nhà/cây/cầu đường; `CartoonSprites.java:216–298` `drawEnvironment` vẽ lại lớp nhà/cây; `AdventureScreen.java:355–365` còn vẽ panorama + extras trên cùng layout | **Layer overdraw** và nhiều quads/switch texture. Hai lớp không giống pixel hoàn toàn nên **không được xóa mù**; nhưng có các chi tiết địa hình trùng mục đích và không hữu ích khi panorama phủ lên. | Lập layer ownership `sky → base-ground → themed foreground → road/characters → HUD`; conditional render theo POI/biome, giảm draw calls nhưng giữ đúng hình/GPS provenance; so sánh screenshot 1920×1080 và `previousFrameDrawCalls` |
| R03 | **P0 — RAM + APK** | `tools/build_m10_prop_atlas.py:12–56` đóng **48 prop** `URB,VIL,FOR,CST,MTN,FRM`; `M10PropAtlas.java:30–39,56–67` chỉ có map cho **4 group** URB/VIL/FOR/FRM (32 prop), *CST+MTN (16)* không vào danh sách chọn; `M10PropAtlas.java:24` nạp TOÀN BỘ pages ngay. `tools/build_m10_atlas.py:93–153` còn ghi `m10-*.png` cho tất cả 92 SVG, `android/build.gradle.kts:46–47` đóng gói cả `../assets` | Phần art không được dùng vẫn chiếm trang RGBA; ảnh M10 gốc và derived `props-*.png` chứa nhiều pixel nguồn lặp, làm APK nặng, dù *full M10 atlas không được nạp vào GPU trong gameplay*. | Chỉ pack group được dùng vào resident atlas; lưu full atlas/gallery ở build intermediate, không vào Android production assets; hoặc tách feature page lazy-load có phép đo VRAM. Số trang 1024² thực tế do alpha crop quyết định, phải đo |
| R04 | **P1 — frame stutter** | `PoiBackdropRenderer.java:43–61` `new Texture(file)` và `image.dispose()` ngay trong `draw`; `AdventureScreen.java:360–365` đã `SpriteBatch.begin()` | Đổi POI có decode/upload/hủy GL texture đồng bộ trên render thread trong batch, dễ frame spike; hiện chỉ giữ một panorama chứ **không** giữ 22 ảnh trong RAM | Tách `prepareScene(key)` khỏi `draw`; preload/decode trên worker nếu an toàn, GL upload trong render thread **trước** `batch.begin`, LRU 1–2 assets; test 10 thay đổi POI liên tục & p95/p99 |
| R05 | **P1 — CPU/GC khi đổi chunk** | `WorldPainter.java:34–37,80–86` và `CartoonSprites.java:40,218–225,304–309` tạo **hai `WorldWindow` / `ProceduralWorldGenerator` độc lập**, cùng seed 20261008 và cùng 4 chunk | Generate/dự trữ các `WorldChunk` trùng nhau khi chuyển vùng. Ring cache cố định nên **không phải leak** và không regenerate mỗi frame. Tác động steady-state nhỏ hơn R02 | Một `WorldWindow` owner ở screen, inject view/read-only sang hai renderer; giữ deterministic GPX rewind, POI ordering và renderer tests |
| R06 | **P1 — concurrency risk, cần đo** | `TripPackCoordinator.kt:64–68`: `thread?.interrupt()` ngay sau đó `thread = null`; `:92–97` tạo worker mới khi resume. Network operation có thể chưa trả về khi thread cũ bị interrupt | Có thể tồn tại **2 HTTP worker in-flight** trong lần pause/resume nhanh trên mạng chậm. Epoch check chặn kết quả cũ nhưng không nhất thiết hủy network socket. **Chưa chứng minh duplicate request xảy ra**; limiter hiện có giúp khống chế RPM | Test fake gateway/blocking I/O + pause/resume; giữ worker state cho đến khi xác nhận kết thúc, dùng cancellable request/executor một worker, không phá 10 RPM, generation+review 1:1, consent/AI budget |
| R07 | **P2 — allocation/GC** | `PoiSceneDirector.java:101–104`, `GeoWorldDirector.java:55–60` và `DynamicSceneComposer.java:8–13` cấp phát `Scene` record mới mỗi frame kể cả khi không có POI | Vài object/60 FPS: GC nhỏ, nhưng chạy nhiều giờ sẽ có allocation churn. **Không phải heap leak** | Dùng immutable shared inactive Scene hoặc cache snapshot thay đổi, đo allocation profile trước khi tối ưu |
| R08 | **P2 — startup CPU** | `KidSoundscape.kt:38–67,119–163`: mỗi khi tạo instance đều chạy thread tổng hợp lại 6 WAV, ghi file cache, rồi `SoundPool.load`; kể cả khi sound effects/music tắt | CPU + I/O mỗi lần khởi động/recreate, tương đối ngắn; sample nhỏ, không phải nguyên nhân chính của RAM chạy dài | Reuse versioned stable WAV cache, tạo/gọi SoundPool khi cần, tính toán âm thanh offline tại build; giữ audio focus/mixing policy |
| R09 | **P2 — APK only** | `android/build.gradle.kts:14–31,43` đóng JNI cho arm64/armeabi-v7a/x86_64; `release.isMinifyEnabled = false` | APK universal chứa ABI không chạy trên Fold3 và release code không shrink; **không có nghĩa tất cả JNI libs được nạp vào RAM** | ABI split per device/store, shrink/release kiểm thử khi phân phối; vẫn giữ ABI emulator khi dev |
| R10 | **P2 — UI overhead minor** | `KidsActivity.kt:415–542` callback 1 lần/s tạo chuỗi native status; `AdventureDashboard.kt:19–80` có các TextViews riêng, `KidsActivity.kt:745–791` thêm POI overlay và banner | Có text/HUD gần như trùng ngữ nghĩa, gây layout/string allocations khi setText lại dù không thay đổi. **Một lần/s là chi phí nhỏ** | Chỉ cập nhật khi nội dung thay đổi, giảm overlay trùng, benchmark nếu UI thread thật sự nghẽn |

## Những điểm không nên xóa chỉ vì tên gần giống

- `WorldPainter` (primitive/ground) và `CartoonSprites` (sprites/characters) **đều có giá trị**; vấn đề là ownership vùng chồng lấn, không phải xóa một class.
- `PoiSceneDirector`, `GeoWorldDirector` và `DynamicSceneComposer` xử lý độ tin cậy nguồn GPS/POI khác nhau; **không nên gộp** làm mất ưu tiên POI > GeoWorld > fictional.
- `TripPackStore` và `CreativePackStore` lưu **hai định dạng nội dung/validation**; chia store là có chủ đích. Generation+review AI 1:1 là cơ chế kiểm duyệt, **không phải request thừa cần bỏ**.
- `WorldWindow` có ring cache 4 ô; `LiveJourneyFeed` dùng `AtomicReference` giữ fix mới nhất; `RenderRunMetrics` histogram cố định 10001 ô (~78 KiB); `FrameProfiler` 180 mẫu. Không có chứng cứ tăng bộ nhớ không giới hạn ở các thành phần này.
- `KidsActivity.runLater` giữ các Runnable trong `delayedTalk`, nhưng `clearTalkQueue()` dọn mỗi beat/pause và onDestroy; **không gọi đó là leak** khi chưa có heap dump. Có thể remove Runnable khỏi list khi chạy nếu cần.

## Đề xuất thứ tự refactor dựa trên dependency

1. **M0 Baseline/guards**: đo M10 hiện tại trên Fold3 real display FullHD, Audio-only, DEMO 10m/30m/60m. Chụp `RenderMetrics`, Android PSS/Native Heap/GPU if available, CPU%, thermal, GL page count, draw calls, p95, APK size. Không lấy số của commit cũ để so.
2. **M1 Residency (R01 + R03)**: chỉ pack atlas đủ dùng, giữ art chưa tích hợp ở intermediate; lazy GL resources theo chế độ, cùng 1 owner dispose; test 100 chuyển Full↔Audio-only, không bị mất scene/speech/AI.
3. **M2 Rendering (R02 + R05 + R04)**: rà soát layer/POI, dùng chung procedural chunk window, xử lý texture đổi POI bên ngoài active `SpriteBatch`; giảm draw calls và spike theo kiểm thử 10 POI transitions.
4. **M3 Long run / package (R06–R10)**: stress fake AI + rapid F10/background/resume, GC profiler, WAV cache, ABI splits. Không thay RPM hoặc cấp thêm quyền.
5. **M4 regression**: 60-min LIVE/DEMO/GPX/Audio-only, both children, captions, parent F10, no PIN, endless session no auto goodbye, GPS/POI provenance, offline TTS, no frame drops/GL errors.

**Acceptance:** đo chính xác trước/sau trên cùng Z Fold3 firmware, FullHD 1920×1080 và trạng thái thiết bị. Mục tiêu M10 lịch sử avg >=55FPS, p95 <=25ms **chỉ là target, không phải kết quả đã xác minh**. Không tăng đáng kể các PSS/CPU/FPS chỉ để giảm LOC. Chỉ gộp phần có measured benefit; visual/GPS/regression phải được giữ nguyên.

## Chạy benchmark trên máy local qua Codex (không có device execution trong audit này)

```bash
git rev-parse HEAD
bash scripts/test-local.sh
./gradlew --no-daemon :core:test :android:assembleDebug :android:assembleDebugAndroidTest
adb shell dumpsys meminfo com.khuongnd.dexkids
adb shell dumpsys gfxinfo com.khuongnd.dexkids framestats
adb shell top -b -n 1 | grep com.khuongnd.dexkids
adb logcat -d -s RenderMetrics:I PoiBackdrop:E M10Art:E
```

Theo dõi Java Heap, Native Heap, Graphics/GL khi thiết bị cung cấp, Total PSS, CPU%, draw calls, dropped frames. `dumpsys gfxinfo` có thể không phản ánh đầy đủ frame LibGDX, nên kết hợp GL `RenderMetrics` và Perfetto/Android Studio Profiler. Benchmark phải tách chi phí *build-time SVG generation*, *APK size*, *CPU khi chạy*, *GPU texture residency*, *GC churn*.

## Codex review prompt

> Read `docs/PERFORMANCE_DUPLICATION_AUDIT_2026-10-12.md` and current STATUS/TASKS. Audit source refs R01–R10 against latest main, collect real before metrics first. Plan and implement M1 residency, M2 layered renderer and POI loading, then M3 worker/audio/package optimizations in small independent branches. Do not remove GPU objects without GL-thread-safe lifecycle; do not merge POI vs GeoWorld provenance logic; preserve one DeX display, F10/Back/no PIN, perpetual viewing, 10RPM, consent, voice and GPS source rules. Re-run source/Gradle/AndroidTest and 30–60min hardware soak. Record actual baseline/delta per changed SHA and mark PASS/FAIL/BLOCKED. No false savings or unexecuted tests.

**Test status for this audit:** structural GitHub source read only. **No Java compilation, APK build, profiler run, GPU measurement or device benchmark was performed.**
