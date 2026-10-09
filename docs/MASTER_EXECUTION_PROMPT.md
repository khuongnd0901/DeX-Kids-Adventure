# MASTER EXECUTION PROMPT
# PROJECT: DeX Kids Adventure
# MODE: PLAN → IMPLEMENT → TEST → BENCHMARK → RELEASE
# ROLE: Principal Android Engineer + Game Architect + AI Engineer

Bạn là Main Agent chịu trách nhiệm xây dựng hoàn chỉnh
dự án DeX Kids Adventure từ con số 0.

Nhiệm vụ bao gồm:
- Tạo repository GitHub mới.
- Thiết kế kiến trúc hệ thống.
- Xây dựng ứng dụng Android.
- Phát triển game engine LibGDX.
- Tạo bộ đồ họa và animation 2D.
- Tích hợp GPS và dữ liệu địa lý thực.
- Xây dựng AI Tour Guide.
- Xây dựng hệ thống thuyết minh tiếng Việt.
- Tích hợp DeX-Assistant hiện có.
- Viết unit test, integration test và benchmark.
- Kiểm thử trên Samsung Z Fold3.
- Hoàn thiện tài liệu, build và phát hành APK.
- Commit/push source code lên GitHub.

Không dừng công việc sau khi tạo plan.
Phải triển khai tuần tự qua các milestone, kiểm thử
và cập nhật trạng thái đến khi hoàn thiện.

Không đánh dấu DONE nếu chưa đáp ứng acceptance criteria.
Nếu thiếu thiết bị thật hoặc quyền cần thiết, ghi rõ
BLOCKED/NOT VERIFIED và tiếp tục các task độc lập.

==================================================
1. PROJECT INFORMATION
==================================================

Project name:
DeX Kids Adventure

GitHub repository:
khuongnd0901/DeX-Kids-Adventure

Repository visibility:
PRIVATE by default.

Android package:
com.khuongnd.dexkids

Technology:
- Kotlin
- Java 17
- LibGDX stable release, ưu tiên 1.14.2
- Android SDK 35
- Min SDK 30
- Gradle Kotlin DSL
- AndroidX
- Room Database
- GPS / Android Location API
- OpenStreetMap
- Android TextToSpeech
- Android IPC

Target device:
Samsung Galaxy Z Fold3
Android 15
Samsung DeX

Target display:
1920x1080
Landscape 16:9
Fullscreen

Development environment:
Ubuntu 24.04

Target performance:
30 FPS stable trong điều kiện sử dụng thực tế.

Backend:
Không bắt buộc.
Game runtime phải hoạt động offline.

Existing projects:
- khuongnd0901/DeX-Assistant
- khuongnd0901/Dex-Assistant-UI

Hai project trên đang được phát triển.
Không thay đổi source hiện tại nếu chưa đến
milestone tích hợp và chưa audit dependencies.

==================================================
2. PRODUCT VISION
==================================================

DeX Kids Adventure là một hướng dẫn viên du lịch
ảo dành cho trẻ em ngồi trên ô tô.

Sản phẩm không phải:
- Video player thông thường.
- Ứng dụng YouTube Kids.
- GPS navigation dành cho tài xế.
- Game yêu cầu trẻ phải chạm màn hình.

Sản phẩm mong muốn:

Một thế giới hoạt hình 2D liên tục.

Nhân vật Capybara ngồi trên xe buýt vàng,
đồng hành cùng trẻ em trong suốt chuyến đi.

Khi ô tô thực di chuyển:
- Thế giới hoạt hình cũng di chuyển.
- Phong cảnh tự động thay đổi.
- Nhân vật thể hiện cảm xúc.
- GPS phát hiện địa điểm thực tế.
- Hướng dẫn viên giới thiệu địa điểm.
- Nội dung thay đổi theo thời gian.
- Các sự kiện xuất hiện theo hành trình.

Ví dụ:

Ô tô đi qua cầu:
→ Xe hoạt hình đi qua cây cầu.
→ Capybara giới thiệu cây cầu và dòng sông.
→ Xuất hiện cá, thuyền và các hoạt ảnh phù hợp.

Ô tô đi gần công viên:
→ Phong cảnh chuyển sang nhiều cây xanh.
→ Xuất hiện chim, bướm.
→ Capybara giới thiệu về thiên nhiên.

Ô tô đi qua sân bay:
→ Xuất hiện máy bay hoạt hình.
→ Capybara giải thích máy bay hoạt động thế nào.

Ô tô dừng đèn đỏ:
→ Xe hoạt hình giảm tốc hoặc dừng.
→ Nhân vật tiếp tục cử động.
→ Không để màn hình đứng hình.

Buổi tối:
→ Bầu trời chuyển sang màu tối.
→ Nhạc nhẹ hơn.
→ Nhân vật kể chuyện thư giãn.

Mục tiêu cuối cùng:

"Giải thích thế giới thực cho trẻ em thông qua
một chuyến phiêu lưu hoạt hình tự động."

==================================================
3. CORE DESIGN PRINCIPLES
==================================================

3.1. Infinite Procedural World

KHÔNG xây dựng bản đồ thủ công cho từng hành trình.

Phải xây dựng hệ thống có thể tự tạo thế giới
hoạt hình từ dữ liệu địa lý và bộ assets.

Kiến trúc:

Real GPS
   ↓
Journey Tracker
   ↓
Geo Context Engine
   ↓
World Context
   ↓
Procedural World Generator
   ↓
LibGDX Rendering Engine
   ↓
2D Animated World

Mỗi chuyến đi phải tạo ra trải nghiệm khác nhau.

Game phải hỗ trợ:
- Tuyến đường chưa biết trước.
- Tuyến đường đã biết.
- GPS offline.
- GPS simulator.
- Replay hành trình từ GPX.
- Mất tín hiệu GPS.
- Sai số hoặc nhảy GPS.

Không được làm nhân vật teleport
khi vị trí GPS thay đổi đột ngột.

3.2. GPS-Driven Animation

GPS quyết định trạng thái hành trình.

LibGDX game loop quyết định rendering.

Không dùng GPS để cập nhật vị trí sprite
trực tiếp từng frame.

Phải có:
- GPS smoothing.
- Speed smoothing.
- Position interpolation.
- Animation state machine.
- Camera smoothing.
- Frame-independent movement.

Game render mục tiêu 30 FPS.
GPS có thể cập nhật chậm hơn.

3.3. Real-World Knowledge

Hướng dẫn viên chỉ giới thiệu thông tin
địa lý thực tế khi có nguồn đáng tin cậy.

Không được tự bịa:
- Tên địa danh.
- Vị trí cầu.
- Tên sông.
- Lịch sử công trình.
- Đặc điểm địa điểm.

Nếu không đủ dữ liệu:
→ Chuyển sang nội dung khám phá tổng quát.

Nếu GPS có độ tin cậy thấp:
→ Không khẳng định đang đi qua địa điểm cụ thể.

3.4. Offline First

Các chức năng cốt lõi phải chạy offline:
- Render animation.
- GPS tracking.
- World generation.
- Local knowledge lookup.
- Local narration.
- Journey progress.

Internet chỉ phục vụ:
- Tải content pack.
- Cập nhật dữ liệu địa danh.
- Tạo nội dung mới.
- Đồng bộ tùy chọn.

Không yêu cầu kết nối Spring Boot liên tục.

==================================================
4. GAME ENGINE ARCHITECTURE
==================================================

Sử dụng LibGDX với các thành phần:

- OrthographicCamera
- SpriteBatch
- TextureAtlas
- AssetManager
- Scene2D
- Viewport
- Animation
- TiledMap nếu thực sự cần

Thiết kế các subsystem:

A. GAME CORE
- GameLifecycleManager
- GameStateMachine
- AdventureScreen
- GameEventBus
- FrameProfiler

B. WORLD ENGINE
- WorldDirector
- ProceduralWorldGenerator
- WorldChunkManager
- WorldBiomeResolver
- ParallaxRenderer
- EnvironmentAnimator
- WorldTransitionManager

C. VEHICLE AND CHARACTERS
- CapybaraActor
- VehicleActor
- CharacterAnimationController
- EmotionStateMachine
- VehicleMovementController

D. GEO ENGINE
- JourneyTracker
- LocationProvider
- GPSFilter
- RouteProjector
- GeoContextEngine
- POIRepository
- POIEventDetector

E. STORY ENGINE
- TourGuideDirector
- StoryStateMachine
- KnowledgeBase
- ContentSelector
- StoryMemory
- EventCooldownManager

F. AUDIO
- GuideNarrator
- VoiceAssetRepository
- AudioFocusController
- BackgroundMusicManager
- SoundEffectManager

G. INTEGRATION
- AssistantIPCService
- KidsCommandReceiver
- KidsStatusPublisher
- DisplayRouter

Tên class có thể điều chỉnh khi audit kiến trúc.
Tránh tạo abstraction không cần thiết.

Game core không phụ thuộc trực tiếp Android Context.
Android-specific API phải đi qua interfaces/adapters.

==================================================
5. VISUAL DESIGN & ASSET GENERATION
==================================================

Phong cách:

2D cartoon.
Bright pastel colors.
Soft rounded shapes.
Friendly children's animation.

Ưu tiên thiết kế cho trẻ 2-6 tuổi.

Nhân vật chính:
Capybara Explorer.

Trang phục:
- Mũ thám hiểm.
- Ba lô nhỏ.
- Biểu cảm thân thiện.

Phương tiện:
Xe buýt vàng.

Bắt buộc tạo asset packs:

01. CHARACTER PACK
- Idle
- Talking
- Happy
- Surprised
- Pointing
- Waving
- Sleeping

02. VEHICLE PACK
- Body
- Front wheel
- Rear wheel
- Moving animation
- Idle animation

03. CITY PACK
- Buildings
- Houses
- Trees
- Street lights
- Road
- Sidewalk

04. NATURE PACK
- Sky
- Clouds
- Mountains
- Grass
- Forest
- Flowers
- Birds
- Butterflies

05. POI PACK
- Bridge
- River
- Park
- Airport
- Countryside

06. UI PACK
- Dialogue bubble
- Location card
- Journey progress
- Achievement badge

Asset requirements:

- Transparent PNG where appropriate.
- Separate animation frames/layers.
- TextureAtlas compatible.
- Support parallax scrolling.
- Avoid visible seams when looping backgrounds.
- Support 1920x1080.
- Optimize texture memory.

Không sử dụng ảnh mockup hoàn chỉnh làm background game.

Không sử dụng asset không rõ license.

Nếu môi trường có image-generation capability:
→ Tạo tài nguyên đồ họa gốc nhất quán phong cách.

Nếu không có:
→ Tự xây dựng vector assets bằng SVG/code,
   hoặc sử dụng tài nguyên có license phù hợp.

Tuyệt đối không để final release chỉ có
hình chữ nhật placeholder.

Ghi nguồn và giấy phép vào:
docs/ASSET_LICENSES.md

==================================================
6. PROCEDURAL WORLD GENERATION
==================================================

World phải được chia thành các chunk.

Ví dụ:

WorldChunk
- chunkId
- biome
- roadType
- backgroundLayers
- foregroundObjects
- pointsOfInterest
- animationEvents

World biomes:
- URBAN
- RESIDENTIAL
- RIVER
- BRIDGE
- PARK
- COUNTRYSIDE
- AIRPORT
- GENERAL

WorldGenerator nhận:

GeoContext {
    location
    speed
    heading
    timeOfDay
    nearbyPOIs
    confidence
}

Output:

WorldScene {
    biome
    assets
    animations
    environment
    storyEvents
}

Không cần tái tạo chính xác hình học toàn bộ
đường phố ngoài đời thực.

Địa hình hoạt hình có thể cách điệu.

Tuy nhiên, địa điểm và lời thuyết minh
phải phù hợp dữ liệu thực tế.

Yêu cầu:

- Chunk generation deterministic.
- Seed dựa trên journeyId và context.
- Có thể replay kết quả.
- Preload chunk tiếp theo.
- Không xuất hiện loading screen.
- Hạn chế garbage collection.
- Giải phóng texture không còn sử dụng.
- Giữ animation ổn định khi GPS đứng yên.

==================================================
7. GPS & GEOGRAPHIC INTELLIGENCE
==================================================

Sử dụng Android Location API.

Ưu tiên:
FusedLocationProviderClient nếu khả dụng.

Có fallback:
LocationManager.

Không phụ thuộc Google Maps UI.

Không giả định có thể đọc route nội bộ
của ứng dụng Google Maps.

Sử dụng OpenStreetMap để xác định POI.

Cần nghiên cứu:
- OSM data extraction.
- Offline POI indexing.
- Spatial queries.
- GeoJSON / compact local database.
- Route map matching nếu cần.

MVP sử dụng một khu vực địa lý kiểm thử,
ví dụ TP.HCM, nhưng kiến trúc phải mở rộng
được cho nhiều tỉnh/thành Việt Nam.

Không đóng gói toàn bộ dữ liệu thô
của Việt Nam vào APK nếu không cần thiết.

POI Engine phải có:
- Distance threshold.
- Direction awareness.
- GPS accuracy filter.
- Confidence scoring.
- Event deduplication.
- Cooldown.
- Journey history.

Phân biệt:
- NEAR_POI
- APPROACHING_POI
- PASSING_POI
- VISITED_POI

Không biến điều kiện NEAR_POI
thành khẳng định PASSING_POI.

Ghi dữ liệu nguồn và attribution
theo giấy phép OSM/ODbL.

==================================================
8. AI TOUR GUIDE & KNOWLEDGE BASE
==================================================

Mỗi POI cần dữ liệu:

POI {
    id
    name
    location
    category
    description
    verifiedFacts
    source
    lastVerifiedAt
}

Tạo Knowledge Base có kiểm chứng.

Nội dung hướng tới trẻ em:
- Từ vựng đơn giản.
- Câu ngắn.
- Giải thích trực quan.
- Không dùng khái niệm quá trừu tượng.
- Không chứa nội dung đáng sợ.

Story Director quyết định:
- Khi nào được nói.
- Nói về địa điểm nào.
- Nội dung nào phù hợp độ tuổi.
- Có nên kể tiếp không.
- Có nên chuyển sang nhạc không.

Phải có cooldown tránh nói liên tục.

Không phát lời thuyết minh chỉ vì
xe di chuyển thêm vài mét.

Một POI có thể có nhiều nội dung:
- Giới thiệu.
- Kiến thức.
- Câu chuyện.
- Câu đố.
- Fun facts.

Không lặp lại y nguyên mỗi lần đi qua.

Với câu đố:
Không yêu cầu trẻ phải chạm màn hình.
Có thể đặt câu hỏi, chờ ngắn rồi giải thích.

AI-generated content:

Chỉ sử dụng trong bước chuẩn bị content pack.

Không tự động phát nội dung LLM
chưa được kiểm tra cho trẻ nhỏ.

Các fact về địa danh phải gắn nguồn.

Nếu không có nguồn hoặc nguồn mâu thuẫn:
Không phát biểu như một sự thật.

==================================================
9. VIETNAMESE NARRATION
==================================================

Nhân vật Capybara phải có khả năng
thuyết minh tiếng Việt.

Cơ chế:

Story Event
   ↓
Narration Scheduler
   ↓
Vietnamese Audio
   ↓
Character Talking Animation

Ưu tiên:
- Audio đã tạo và kiểm tra trước.
- Vietnamese offline TTS nếu khả dụng.

Không giả định Z Fold3 đã cài
giọng TTS tiếng Việt offline.

Nếu offline TTS không khả dụng:
Sử dụng prerecorded narration assets.

Không tự động chuyển sang network TTS
khi chưa được phụ huynh cho phép.

Khi nhân vật nói:
- Có talking animation.
- Có phụ đề ngắn.
- Có thể thay đổi biểu cảm.

Narration không được làm mất
cảnh báo điều hướng quan trọng.

Phải kiểm thử Audio Focus và Audio Routing
với DeX-Assistant và Vietmap Live Pro.

==================================================
10. SAMSUNG DEX FULL HD
==================================================

Kids Adventure chạy trên màn hình DeX phía sau.

Parent controls chạy trên điện thoại.

Child display:
- Fullscreen.
- Landscape.
- 16:9.
- Không có menu thao tác.
- Không có nút điều khiển dành cho trẻ.
- Không hiển thị dữ liệu GPS nhạy cảm.
- Không hiển thị màn hình debug.

Phone display:
- Start
- Pause
- Stop
- Settings
- Status
- Preview

Sử dụng DisplayManager và ActivityOptions
để lựa chọn màn hình khi được hệ thống cho phép.

Không hardcode displayId.

Không tự động fallback sang màn hình điện thoại
nếu không xác định được màn hình DeX.

Phải kiểm thử trên Samsung Z Fold3 thực tế.

Không sử dụng Lock Task như giải pháp
điều khiển cửa sổ DeX nếu không tương thích.

==================================================
11. DEX-ASSISTANT INTEGRATION
==================================================

DeX Kids là một APK độc lập.

DeX-Assistant điều khiển qua Android IPC.

Các command dự kiến:

KIDS_START
KIDS_STOP
KIDS_PAUSE
KIDS_RESUME
KIDS_SET_VOLUME
KIDS_SET_AGE_GROUP
KIDS_GET_STATUS

Ví dụ:

"Hey hey, bật hướng dẫn viên cho hai bé."

"Hey hey, tạm dừng hướng dẫn viên."

"Hey hey, tắt Kids Adventure."

IPC requirements:
- Explicit component.
- Permission validation.
- Caller identity validation.
- Không dùng implicit exported receiver không bảo vệ.
- Không truyền token backend qua IPC.

Ưu tiên signature permission
nếu hai APK dùng signing identity tương thích.

Nếu signing identity khác:
Phải thiết kế cơ chế xác thực tương đương,
không bỏ kiểm tra caller.

Trước khi chỉnh repo DeX-Assistant:
- Đọc .agent/STATUS.md.
- Đọc .agent/TASKS.md.
- Đọc protocol hiện tại.
- Audit LocalCommandEngine.
- Audit ActionRouter.
- Audit signing identity.

Chỉ thay đổi bằng feature branch/PR riêng.

Không phá vỡ production:
- Wake word.
- STT.
- TTS.
- Accessibility.
- Device Owner.
- DPC.
- Existing local commands.

==================================================
12. SECURITY, PRIVACY & CHILD SAFETY
==================================================

Ứng dụng dành cho trẻ nhỏ.

Không:
- Hiển thị quảng cáo.
- Theo dõi hành vi trẻ.
- Thu thập giọng nói trẻ mặc định.
- Gửi lịch sử GPS lên cloud mặc định.
- Phát nội dung do người dùng không tin cậy cung cấp.
- Yêu cầu trẻ thao tác khi xe đang di chuyển.

GPS history lưu local.
Có retention policy và chức năng xóa dữ liệu.

Parent settings phải nằm ngoài
giao diện trẻ em.

Không phát nhạc hoặc thuyết minh liên tục
trong thời gian dài.

Hỗ trợ:
- Screen time limit.
- Audio-only mode.
- Quiet mode.
- Night mode.
- Emergency stop.

Cần có logic giảm hoặc tạm dừng âm thanh
khi navigation audio được ưu tiên.

Không sử dụng trạng thái Kids Adventure
để can thiệp vào lái xe.

==================================================
13. PROJECT REPOSITORY STRUCTURE
==================================================

Tạo repository với cấu trúc đề xuất:

DeX-Kids-Adventure/
│
├── AGENTS.md
├── README.md
├── LICENSES.md
│
├── .agent/
│   ├── STATUS.md
│   ├── TASKS.md
│   ├── DECISIONS.md
│   ├── BACKLOG.md
│   │
│   ├── plans/
│   │   ├── active/
│   │   └── completed/
│   │
│   └── evidence/
│
├── docs/
│   ├── BRD.md
│   ├── SRS.md
│   ├── ARCHITECTURE.md
│   ├── GAME_DESIGN.md
│   ├── GPS_DESIGN.md
│   ├── WORLD_GENERATION.md
│   ├── CONTENT_PIPELINE.md
│   ├── POI_DATA_PROVENANCE.md
│   ├── ASSET_LICENSES.md
│   ├── SECURITY_PRIVACY.md
│   ├── TEST_PLAN.md
│   ├── BENCHMARK.md
│   └── RELEASE_CHECKLIST.md
│
├── android/
│   └── Android Launcher + Platform Adapters
│
├── core/
│   └── LibGDX Game Engine
│
├── desktop/
│   └── Desktop Debug Launcher
│
├── assets/
│   ├── characters/
│   ├── vehicles/
│   ├── environments/
│   ├── worlds/
│   ├── effects/
│   ├── audio/
│   └── ui/
│
├── tools/
│   ├── gps-replay/
│   ├── asset-packer/
│   └── poi-data-builder/
│
├── test-data/
│   ├── gps/
│   └── poi/
│
└── scripts/

Có thể điều chỉnh cấu trúc nếu có
lý do kiến trúc rõ ràng.

==================================================
14. AGENT PROJECT MANAGEMENT
==================================================

Sử dụng hệ thống quản lý task tương tự
DeX-Assistant hiện tại.

.agent/STATUS.md:
- Current milestone.
- Current task.
- Latest verified evidence.
- Known blockers.
- Next actions.
- Latest commit SHA.

.agent/TASKS.md:
- Task ID.
- Description.
- Dependencies.
- Owner.
- Priority.
- Acceptance Criteria.
- State.
- Evidence.

Task states:
BACKLOG
PLANNED
READY
IN_PROGRESS
BLOCKED
DONE

Mỗi task đủ lớn phải có ExecPlan.

Ví dụ:

.agent/plans/active/
T-001-project-bootstrap.md
T-002-libgdx-engine.md
T-003-procedural-world.md

Task hoàn tất:
Chuyển ExecPlan sang plans/completed/.

Không xóa tài liệu evidence.

Mỗi task phải có:
- Mục tiêu.
- Phạm vi.
- Các bước triển khai.
- Điều kiện nghiệm thu.
- Tests.
- Kết quả thực tế.
- Những gì chưa xác minh.

==================================================
15. DELIVERY MILESTONES
==================================================

M0 — PROJECT BOOTSTRAP

- Kiểm tra GitHub CLI authentication.
- Kiểm tra repository đã tồn tại chưa.
- Tạo private repository nếu chưa có.
- Khởi tạo Git.
- Tạo project LibGDX.
- Tạo Android launcher.
- Tạo Desktop launcher.
- Tạo .agent structure.
- Tạo BRD/SRS/Architecture.
- Commit/push initial source.

Acceptance:
Project build thành công.
Repository có source và tài liệu ban đầu.

M1 — GAME ENGINE FOUNDATION

- Game loop.
- OrthographicCamera.
- World viewport.
- Main game screen.
- Resource lifecycle.
- FPS diagnostics.
- Desktop smoke test.

Acceptance:
Game hiển thị ổn định trên desktop và
Android emulator khi môi trường hỗ trợ.

M2 — CHARACTER & WORLD

- Capybara animations.
- Yellow bus.
- Roads.
- Parallax backgrounds.
- World chunks.
- Infinite scrolling.
- Procedural environment.

Acceptance:
Xe chạy liên tục qua nhiều chunk.
Không xuất hiện khoảng trống hoặc giật cảnh
trong bộ test được xác định.

M3 — GPS SIMULATION

- GPX replay engine.
- GPS state machine.
- Speed interpolation.
- Stop/resume.
- GPS jump handling.
- Location abstraction.

Acceptance:
Game replay được hành trình giả lập.
Cùng input và seed tạo kết quả tái lập được.

M4 — REAL LOCATION

- Android GPS provider.
- GeoContextEngine.
- Offline POI database.
- Spatial queries.
- POI event detection.
- GPS confidence filtering.

Acceptance:
Hệ thống nhận diện đúng POI trong
bộ dữ liệu kiểm thử có ground truth.

M5 — TOUR GUIDE ENGINE

- Vietnamese narration.
- Curated Knowledge Base.
- Story Director.
- Story cooldown.
- Character talking animation.
- Subtitle display.

Acceptance:
Capybara thuyết minh đúng ngữ cảnh.
Không khẳng định địa danh chưa xác minh.
Không lặp liên tục.

M6 — DYNAMIC EXPERIENCE

- Day/night.
- Different journey seeds.
- Location-based content.
- Dynamic biome transitions.
- Journey memory.
- Exploration achievements.

Acceptance:
Hai hành trình khác nhau tạo trải nghiệm khác nhau.
Hành trình lặp lại không nhất thiết
phát cùng một nội dung.

M7 — PARENT CONTROLS

- Phone control screen.
- Kids playback controls.
- Screen time limits.
- Audio-only mode.
- Settings persistence.
- Parent-only access.

Acceptance:
Trẻ không cần thao tác cảm ứng.
Phụ huynh điều khiển được phiên Kids.

M8 — DEX INTEGRATION

- External display support.
- Full HD fullscreen.
- IPC integration.
- Voice commands.
- Audio routing.
- Navigation coexistence.

Acceptance:
DeX Kids và DeX-Assistant cùng hoạt động.
Không phá chức năng điều hướng và voice.

M9 — OPTIMIZATION & RELEASE

- Performance profiling.
- Memory profiling.
- Thermal testing.
- GPS reliability testing.
- Offline testing.
- Security review.
- Asset license audit.
- APK signing.
- Release documentation.

Acceptance:
Tất cả release gates có evidence.
APK cài đặt được bằng upgrade path hợp lệ.
GitHub có release artifact và checksum.

==================================================
16. BENCHMARK REQUIREMENTS
==================================================

Benchmark phải thực hiện trên Z Fold3 thật
khi thiết bị có sẵn.

Metrics:
- Average FPS.
- P95 frame time.
- CPU usage.
- PSS memory.
- Battery consumption.
- Device temperature / thermal status.
- GPS event latency.
- World chunk generation time.
- Texture loading latency.
- Audio interruption behavior.
- Crash / ANR.
- Performance over continuous use.

Target:
- 1920x1080.
- Stable 30 FPS.
- Continuous runtime >= 60 minutes.
- No critical crash.
- No unbounded memory growth.

Các target chỉ là acceptance thresholds
đề xuất, không phải số đo đã đạt.

Benchmark scenario:
- DeX Kids active.
- DeX-Assistant active.
- Google Maps active.
- Vietmap Live Pro active nếu có.
- GPS replay và GPS thật.
- Offline mode.
- Day/night.
- Repeated location transitions.

Ghi rõ:
ACTUAL / TARGET / NOT VERIFIED.

Không công bố kết quả benchmark
nếu chưa thực sự đo trên thiết bị.

==================================================
17. AUTOMATED TESTING
==================================================

Bắt buộc tạo test suites:

Unit tests:
- GPS smoothing.
- Route projection.
- World generation.
- Deterministic seeds.
- POI matching.
- Event cooldown.
- Story state machine.
- Content selection.

Integration tests:
- Journey replay.
- Scene transitions.
- Content loading.
- IPC command validation.

Android tests:
- Activity lifecycle.
- External display selection.
- Permission handling.
- Background/foreground.
- Audio focus.

Game tests:
- 30-minute simulated trip.
- 60-minute simulated trip.
- Repeated world chunks.
- Asset reload.
- GPS loss/recovery.

Cung cấp script chạy test.

Không đánh dấu test PASS nếu chưa chạy.

Nếu Samsung DeX không có trong emulator,
ghi rõ hardware validation vẫn còn mở.

==================================================
18. GITHUB WORKFLOW
==================================================

Sử dụng GitHub CLI nếu có quyền hợp lệ.

Bước đầu:
1. Kiểm tra gh auth status.
2. Kiểm tra repository tồn tại.
3. Nếu chưa có, tạo private repository.
4. Push initial scaffold.
5. Tạo task board.
6. Bắt đầu M0.

Không ghi token vào source.

Không tự ý force push.

Mỗi task phát triển trên feature branch.

Commit message:
feat(kids): ...
fix(gps): ...
feat(world): ...
test(story): ...
docs(agent): ...

Mỗi milestone:
- Code implementation.
- Test evidence.
- STATUS update.
- Commit.
- Push.
- PR hoặc merge theo workflow đã xác định.

Không làm mất thay đổi chưa commit.

Nếu GitHub không khả dụng:
Tiếp tục local development.
Ghi blocker.
Không tuyên bố đã push.

==================================================
19. MAIN AGENT & SUBAGENTS
==================================================

Main Agent chịu trách nhiệm:
- Architecture.
- Planning.
- Task dependencies.
- Cross-module contracts.
- Code review.
- Integration.
- Acceptance.
- GitHub release.

Nếu Codex hỗ trợ subagents:

Game Agent:
LibGDX + Graphics + Animation.

Geo Agent:
GPS + OSM + POI.

Story Agent:
Knowledge Base + Narration.

Android Agent:
Samsung DeX + IPC + Audio + Permissions.

QA Agent:
Tests + Benchmark + Release evidence.

Các subagent phải có phạm vi file riêng.
Tránh nhiều agent cùng chỉnh một file.

Các task độc lập có thể chạy song song.
Task có dependency phải thực hiện theo thứ tự.

Sau mỗi task:
- Commit kết quả.
- Cập nhật STATUS.
- Cập nhật TASKS.
- Ghi evidence.
- Cập nhật next actions.

Để tối ưu token:
Mỗi session mới chỉ cần đọc:
- AGENTS.md
- .agent/STATUS.md
- Relevant ExecPlan
- Relevant source files

Không cần đọc lại toàn bộ project.

Không tự nhận đã clear session nếu
runtime không hỗ trợ thao tác đó.

==================================================
20. DEFINITION OF DONE
==================================================

Dự án chỉ được đánh dấu RELEASE READY khi:

[ ] Có GitHub repository.
[ ] Có source code đầy đủ.
[ ] Build Android thành công.
[ ] Có APK được ký hợp lệ.
[ ] Có assets 2D hoàn chỉnh.
[ ] Có Capybara animation.
[ ] Có Infinite World.
[ ] Có Procedural World Generator.
[ ] Có GPS tracking.
[ ] Có POI database.
[ ] Có real-world knowledge.
[ ] Có Vietnamese narration.
[ ] Có offline mode.
[ ] Có Journey Memory.
[ ] Có Parent Controls.
[ ] Có DeX fullscreen.
[ ] Có DeX-Assistant IPC.
[ ] Có unit/integration tests.
[ ] Có benchmark trên Z Fold3 thật.
[ ] Có kiểm tra coexistence với Vietmap.
[ ] Có security/privacy review.
[ ] Có license review.
[ ] Có release artifact.
[ ] Có tài liệu sử dụng.

Các mục chưa đạt phải giữ trạng thái OPEN/BLOCKED.
Không được đánh dấu DONE dựa trên giả định.

==================================================
21. START EXECUTION NOW
==================================================

Thực hiện theo thứ tự:

STEP 1:
Audit môi trường Ubuntu, Android SDK,
JDK, Gradle, GitHub CLI và dependencies.

STEP 2:
Đọc cấu trúc .agent của DeX-Assistant
để kế thừa cách quản lý project.

STEP 3:
Tạo repository:
khuongnd0901/DeX-Kids-Adventure

STEP 4:
Khởi tạo .agent, BRD, SRS, Architecture,
Game Design và milestone plans.

STEP 5:
Tạo project LibGDX Android + Desktop.

STEP 6:
Bắt đầu M0, sau đó tiếp tục các milestone
khi điều kiện nghiệm thu đã đạt.

YÊU CẦU:

Không dừng ở bước lập kế hoạch.

Phải tạo source code thực tế.

Không chỉ tạo interfaces hoặc TODO.

Các chức năng chưa hoàn thiện phải
được quản lý bằng task rõ ràng.

Chủ động phân tích vấn đề kỹ thuật và
đề xuất giải pháp phù hợp.

Ưu tiên code chạy được, kiến trúc rõ ràng,
test được và dễ mở rộng.

Không tự ý bỏ qua acceptance criteria.

Không ghi nhận kết quả giả.

Sau mỗi milestone, báo cáo:
- Completed tasks.
- Actual test results.
- Git commit SHA.
- GitHub PR/URL.
- Remaining blockers.
- Next milestone.

BẮT ĐẦU THỰC HIỆN M0 NGAY.