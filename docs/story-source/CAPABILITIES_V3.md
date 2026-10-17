# Bộ thế giới truyện T-070 — khả năng v3

Bổ sung **60 đối tượng mới, 10 cảm xúc, 16 hành động, 8 phông cảnh** cho nội dung
quanh trẻ 5–10 tuổi. Tổng cộng 86 đối tượng có hình (87 giá trị prop kể cả NONE),
14 cảm xúc và 27 hành động. Danh mục có thể mở rộng tiếp bằng tài nguyên/mã mới.
Ba actor có lời thoại vẫn là CAPYBARA, SAU, ONG.

Cần APK T-070 trước khi dùng episode schema v2. App mới vẫn đọc truyện v1.
App cũ từ chối pack có v2 và giữ cache hợp lệ trước đó. Không đổi nguồn mẫu chính;
[pack minh họa riêng](stories.world.example.json) có ba bản SAU/ONG/BOTH, dùng đủ
60 đối tượng mới, mọi cảm xúc/hành động và tám phông cảnh. Đây là pack giới thiệu
hoạt cảnh, không phải truyện kể dài 4–7 phút hay nội dung đã được phụ huynh duyệt.

## Danh mục mới

| Chủ đề | Mã đối tượng và nhãn |
| --- | --- |
| Gia đình | `TABLE` (Bàn), `CHAIR` (Ghế), `BED` (Giường), `PILLOW` (Gối), `BLANKET` (Chăn), `LAMP` (Đèn bàn) |
| Đồ dùng cá nhân | `CUP` (Cốc), `BOWL` (Bát), `SPOON` (Thìa), `WATER_BOTTLE` (Bình nước), `TOOTHBRUSH` (Bàn chải), `TOWEL` (Khăn) |
| Học tập | `PENCIL` (Bút chì), `ERASER` (Tẩy), `RULER` (Thước), `NOTEBOOK` (Vở), `BACKPACK` (Ba lô), `CRAYONS` (Bút sáp) |
| Ăn uống | `BANANA` (Chuối), `ORANGE` (Cam), `CARROT` (Cà rốt), `BREAD` (Bánh mì), `RICE` (Cơm), `MILK` (Sữa) |
| Đồ chơi | `TEDDY_BEAR` (Gấu bông), `BLOCKS` (Khối xếp hình), `PUZZLE` (Tranh ghép), `DOLL` (Búp bê), `TOY_TRAIN` (Tàu đồ chơi), `BUBBLES` (Bong bóng xà phòng) |
| Vận động | `BICYCLE` (Xe đạp), `HELMET` (Mũ bảo hiểm), `JUMP_ROPE` (Dây nhảy), `SCOOTER` (Xe scooter), `HOOP` (Vòng thể dục), `GOAL` (Khung thành) |
| Thiên nhiên | `SUN` (Mặt trời), `MOON` (Mặt trăng), `RAIN` (Mưa), `RAINBOW` (Cầu vồng), `MUSHROOM` (Nấm), `ROCK` (Đá) |
| Động vật | `DOG` (Chó), `TURTLE` (Rùa), `ELEPHANT` (Voi), `MONKEY` (Khỉ), `FISH` (Cá), `SNAIL` (Ốc sên) |
| Không gian sống | `HOUSE` (Ngôi nhà), `SCHOOL` (Trường học), `BRIDGE` (Cầu), `BENCH` (Ghế công viên), `BIN` (Thùng rác), `RECYCLE_BIN` (Thùng tái chế) |
| Khám phá khoa học | `CLOCK` (Đồng hồ), `COMPASS` (La bàn), `MAGNET` (Nam châm), `GLOBE` (Quả địa cầu), `TELESCOPE` (Kính thiên văn), `THERMOMETER` (Nhiệt kế) |

Mỗi prop là một hình minh họa đã tích hợp, không có số lượng/màu tùy ý.
SUN/MOON/RAIN/RAINBOW/BUBBLES đặt phía trên; các vật còn lại theo vị trí sân khấu.
Con vật không có lượt nói hoặc chuyển động xương riêng như ba nhân vật chính.

## Biểu cảm và hành động

Emotion: HAPPY, CURIOUS, CALM, SURPRISED, EXCITED, PROUD, SAD, WORRIED, AFRAID, ANGRY, SHY, CONFUSED, TIRED, GRATEFUL.

Biểu cảm dùng đầu minh họa riêng, giữ nhận diện nhân vật. HAPPY/CALM có thể chuyển
đầu mở/đóng miệng khi thuyết minh đang phát; cảm xúc khác giữ nét mặt đã chọn.
Đây là trạng thái nói chuyện, không phải đồng bộ âm vị/lip-sync chính xác.

| Hành động mới | Cách biểu diễn và prop |
| --- | --- |
| IDLE | Tư thế đứng chờ; NONE hoặc vật liên quan |
| TALK | Cử chỉ tay khi trò chuyện; miệng theo tín hiệu thuyết minh |
| WALK / RUN | Tay/chân luân phiên, chuyển động tại chỗ trong vùng sân khấu |
| SIT | Gập chân qua khớp gối; CHAIR, BENCH hoặc NONE |
| THINK | Tay đưa lên gần cằm, nghiêng đầu |
| POINT | Duỗi tay chỉ giới thiệu đối tượng |
| NOD / SHAKE_HEAD | Nhịp gật/lắc đầu nhẹ trong mặt phẳng 2D |
| CLAP | Hai cẳng tay di chuyển vào trước thân |
| PICK_UP / PUT_DOWN | Tay và vật di chuyển cùng nhau, nhặt/đặt trong vùng tương tác gần thân |
| CARRY | Hai tay giữ vật trước thân |
| DRAW | NOTEBOOK, tự kèm bút chì chuyển động |
| EAT | APPLE, BANANA, ORANGE, CARROT, BREAD hoặc RICE; đưa vật tới miệng |
| DRINK | CUP, WATER_BOTTLE hoặc MILK; đưa vật tới miệng |

PICK_UP/PUT_DOWN/CARRY chỉ nhận vật di chuyển được theo StoryWorldCatalog.
Chúng không mô phỏng vật lý, sở hữu đồ vật hay hành trình đi tới một vật ở xa.
Giữ các luật READ→BOOK, WATER→SEEDLING/FLOWER, SHARE→BOOK/BALL/GIFT/APPLE/BASKET/FLOWER,
INSPECT→LEAF/SHELL/STAR/FLOWER. COUNT→NONE và không có supportingProps.
11 động tác cũ vẫn đọc được; khi dùng trong v2 chúng đi qua bộ khớp mới.
Mọi chuyển động thuộc nhân vật tưởng tượng, không yêu cầu trẻ thực hiện trong xe.

Ba nhân vật dùng đầu, thân, tay và chân được vẽ thành các phần rời với điểm xoay.
Mỗi bé có 10 bộ: sáu bộ luân phiên hiện tại và bốn bộ nghề nghiệp cũ. Các phần
mang trang phục hoàn chỉnh; Ong không chồng váy lên sprite cũ. Bộ đồ trong phần
truyện luân phiên chậm theo thời gian hành trình, không thêm trường chọn đồ vào JSON.
Toàn bộ hình gốc giữ nguyên; nguồn mới và prompt ở art/assets-source/story-world/.

## Cảnh nhiều đối tượng

Pack bao ngoài vẫn `schemaVersion: 1`; mỗi episode mới dùng `schemaVersion: 2`.
Beat giữ actor/utterance/emotion/action/prop và thêm `supportingProps` tùy chọn:

```json
{
  "actor": "SAU",
  "utterance": "Sâu vẽ một ngôi nhà vào vở trong câu chuyện tưởng tượng.",
  "emotion": "PROUD",
  "action": "DRAW",
  "prop": "NOTEBOOK",
  "supportingProps": [
    {"prop": "TABLE", "slot": "BACK_LEFT"},
    {"prop": "BACKPACK", "slot": "RIGHT"}
  ]
}
```

Tối đa năm đối tượng phụ, mỗi slot dùng một lần: BACK_LEFT, BACK_RIGHT, LEFT,
RIGHT, FOREGROUND. Một đối tượng chính + năm đối tượng phụ = tối đa sáu.
Không dùng NONE trong supportingProps; không có x/y, URL, tên file hoặc mã lệnh.
Công cụ phụ của động tác (bút, kính lúp, bình tưới) do renderer cấp.
Danh sách đối tượng áp dụng cho từng beat, không tích lũy từ lượt trước.

Phông mới: HOME, CLASSROOM, PLAYGROUND, GARDEN, FOREST, BEACH, MARKET, LIBRARY.
Đây là tám địa điểm hư cấu. Chủ đề v1 khi dùng trong v2 được ánh xạ tới phông gần
nghĩa: NATURE→GARDEN, ANIMALS→FOREST, WATER→BEACH, LANGUAGE→LIBRARY; còn lại HOME.
Trong câu hỏi/phản hồi, giữ phông nhưng xóa đạo cụ và về tư thế trung tính.
Dừng/kết thúc truyện khôi phục cue hành trình trước đó. Audio-only giải phóng
renderer như trước; trở lại Full dựng lại từ beat đang có.

Schema v1 không nhận trường mới, emotion/action/prop/backdrop mới. Codec giữ cách
ghi v1 để fingerprint, trạng thái duyệt và cursor cũ không đổi. SQLite vẫn lưu JSON
trong bảng hiện có, không cần di trú database. Trình sửa văn bản giữ hoạt cảnh v2;
bản sửa vẫn là bản nháp cần xét duyệt theo luồng hiện có.

## Build và kiểm tra

```bash
python3 tools/build_sprites.py
python3 tools/test_story_world.py
./gradlew :core:test :core:checkStoryPack -PstoryPack=docs/story-source/stories.world.example.json
./gradlew :desktop:storyWorldGallery :desktop:storyWorldGameSmoke
./gradlew :android:assembleDebug :android:assembleDebugAndroidTest :desktop:classes
```

Giữ quy trình Python/Pillow/CairoSVG hiện tại. `create_story_world_sources.py` là
công cụ soạn SVG nguồn; build thông thường chỉ đọc nguồn đã lưu. Tác giả cần cập
nhật đồng thời danh mục Java và objects.json; kiểm tra tài nguyên phát hiện lệch.
Tái tạo fixture ghi vào build/story-samples bằng :core:writeWorldStoryPackSample,
không ghi đè nguồn truyện phụ huynh.

Ba atlas nhân vật 1024², một atlas đồ vật 2048×1024 và tối đa hai phông
1280×720: chặn trên **28.344.320 byte**
texture backing mới, dưới ngân sách 32 MiB. Đây không phải PSS hay phép đo GPU.
Nguồn AI được tạo bằng built-in image_gen; provenance không thay thế xác nhận
quyền phân phối hoặc đánh giá mỹ thuật. DeX FPS/PSS, âm thanh và người dùng duyệt
hình chỉ được đánh dấu xác minh khi có bằng chứng thực tế.

Tỷ lệ hình thể, điểm cầm nắm và kích thước đồ vật: [ART_DIRECTION.md](ART_DIRECTION.md).
