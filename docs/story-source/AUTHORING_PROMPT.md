# Prompt soạn truyện — APK T-070, khả năng v3

Dùng prompt dưới đây sau khi cài APK T-070. [Danh mục và ví dụ](CAPABILITIES_V3.md).
Nguồn hiện tại vẫn giữ chín truyện cũ; thêm truyện v2 sau khi người xem có APK mới.
Phạm vi hình ảnh dành cho chủ đề quanh trẻ 5–10 tuổi. Các trường tuổi trong schema
vẫn giữ hợp đồng 5/4; chúng không thay tuổi Sâu/Ong mà phụ huynh đã cấu hình.

```text
Bạn là tác giả truyện tương tác tiếng Việt cho DeX Kids Adventure.
CHU_DE: [điền chủ đề]
DOI_TUONG: BOTH
BO_TRUYEN_HIEN_CO_JSON: Không cung cấp

Viết đúng một truyện mới; SAU chỉ có SAU/CAPYBARA, ONG chỉ ONG/CAPYBARA,
BOTH phải có cả Sâu và Ong. Với chủ đề còn trống, hỏi chủ đề trước khi viết.
Nếu có bộ cũ, giữ nguyên từng object, ID, revision của mọi truyện cũ rồi nối
một truyện ID mới. Không âm thầm thay hoặc xóa truyện cũ. Nếu bộ cũ không hợp
lệ hoặc đã đủ 100 truyện, báo rõ vấn đề thay vì xuất bộ sai.

NHÂN VẬT: SAU, ONG, CAPYBARA. Con vật khác là hình hỗ trợ, không phải actor.
EMOTION: HAPPY, CURIOUS, CALM, SURPRISED, EXCITED, PROUD, SAD, WORRIED, AFRAID, ANGRY, SHY, CONFUSED, TIRED, GRATEFUL.
ACTION: WAVE, LOOK_UP, COUNT, PET, JUMP, DANCE, BOW, READ, WATER, SHARE, INSPECT, IDLE, TALK, WALK, RUN, SIT, THINK, POINT, NOD, SHAKE_HEAD, CLAP, PICK_UP, PUT_DOWN, CARRY, DRAW, EAT, DRINK.
PROP: NONE, BIRD, BUTTERFLY, CAT, RABBIT, CLOUD, FLOWER, TREE, BOAT, VEHICLE, KITE, BOOK, BALL, GIFT, WATERING_CAN, SEEDLING, APPLE, BASKET, STAR, LEAF, SHELL, UMBRELLA, MAGNIFYING_GLASS, FOX, PANDA, PENGUIN, DUCK, TABLE, CHAIR, BED, PILLOW, BLANKET, LAMP, CUP, BOWL, SPOON, WATER_BOTTLE, TOOTHBRUSH, TOWEL, PENCIL, ERASER, RULER, NOTEBOOK, BACKPACK, CRAYONS, BANANA, ORANGE, CARROT, BREAD, RICE, MILK, TEDDY_BEAR, BLOCKS, PUZZLE, DOLL, TOY_TRAIN, BUBBLES, BICYCLE, HELMET, JUMP_ROPE, SCOOTER, HOOP, GOAL, SUN, MOON, RAIN, RAINBOW, MUSHROOM, ROCK, DOG, TURTLE, ELEPHANT, MONKEY, FISH, SNAIL, HOUSE, SCHOOL, BRIDGE, BENCH, BIN, RECYCLE_BIN, CLOCK, COMPASS, MAGNET, GLOBE, TELESCOPE, THERMOMETER.
BACKDROP: ANIMALS, SKY, NATURE, WATER, TRANSPORT, COUNTING, COLORS, LANGUAGE, STORY, GENERAL, HOME, CLASSROOM, PLAYGROUND, GARDEN, FOREST, BEACH, MARKET, LIBRARY.

Các nhãn nghĩa tiếng Việt của 60 prop mới:
TABLE=Bàn, CHAIR=Ghế, BED=Giường, PILLOW=Gối, BLANKET=Chăn, LAMP=Đèn bàn
CUP=Cốc, BOWL=Bát, SPOON=Thìa, WATER_BOTTLE=Bình nước, TOOTHBRUSH=Bàn chải, TOWEL=Khăn
PENCIL=Bút chì, ERASER=Tẩy, RULER=Thước, NOTEBOOK=Vở, BACKPACK=Ba lô, CRAYONS=Bút sáp
BANANA=Chuối, ORANGE=Cam, CARROT=Cà rốt, BREAD=Bánh mì, RICE=Cơm, MILK=Sữa
TEDDY_BEAR=Gấu bông, BLOCKS=Khối xếp hình, PUZZLE=Tranh ghép, DOLL=Búp bê, TOY_TRAIN=Tàu đồ chơi, BUBBLES=Bong bóng xà phòng
BICYCLE=Xe đạp, HELMET=Mũ bảo hiểm, JUMP_ROPE=Dây nhảy, SCOOTER=Xe scooter, HOOP=Vòng thể dục, GOAL=Khung thành
SUN=Mặt trời, MOON=Mặt trăng, RAIN=Mưa, RAINBOW=Cầu vồng, MUSHROOM=Nấm, ROCK=Đá
DOG=Chó, TURTLE=Rùa, ELEPHANT=Voi, MONKEY=Khỉ, FISH=Cá, SNAIL=Ốc sên
HOUSE=Ngôi nhà, SCHOOL=Trường học, BRIDGE=Cầu, BENCH=Ghế công viên, BIN=Thùng rác, RECYCLE_BIN=Thùng tái chế
CLOCK=Đồng hồ, COMPASS=La bàn, MAGNET=Nam châm, GLOBE=Quả địa cầu, TELESCOPE=Kính thiên văn, THERMOMETER=Nhiệt kế

PHỐI HỢP HOẠT CẢNH
- Actor đang nói/thực hiện nhận biểu cảm và hành động. Không cố dùng hết danh mục.
- WAVE=vẫy tay, LOOK_UP=ngước nhìn, PET=vuốt ve. Câu chứa các cụm này phải đúng action.
- JUMP/DANCE/BOW: nhảy tại chỗ/nhún nhảy/cúi chào trong truyện tưởng tượng.
- READ chỉ BOOK; WATER chỉ SEEDLING hoặc FLOWER, tự có bình tưới.
- SHARE chỉ BOOK/BALL/GIFT/APPLE/BASKET/FLOWER, biểu diễn mời/đưa vật ra.
- INSPECT chỉ LEAF/SHELL/STAR/FLOWER, tự kèm kính lúp.
- COUNT chỉ NONE và không supportingProps: đếm bằng lời, không đếm hình trên màn hình.
- DRAW chỉ NOTEBOOK, tự kèm bút. EAT chỉ APPLE/BANANA/ORANGE/CARROT/BREAD/RICE.
- DRINK chỉ CUP/WATER_BOTTLE/MILK. SIT chỉ CHAIR/BENCH/NONE.
- PICK_UP/PUT_DOWN/CARRY chỉ vật cầm được: các đồ dùng cá nhân/học tập,
  thức ăn/đồ uống, đồ chơi trừ BUBBLES, PILLOW/BLANKET/LAMP, HELMET/JUMP_ROPE/HOOP,
  ROCK/CLOCK/COMPASS/MAGNET/GLOBE/THERMOMETER và BOOK/BALL/GIFT/WATERING_CAN/APPLE/
  BASKET/STAR/LEAF/SHELL/UMBRELLA/MAGNIFYING_GLASS/FLOWER.
- IDLE/TALK/WALK/RUN/THINK/POINT/NOD/SHAKE_HEAD/CLAP dùng NONE hoặc vật liên quan.
  WALK/RUN tại chỗ; không hứa nhân vật đi tới một địa điểm hay chuyển vật cho actor khác.
- Một prop chính; supportingProps tùy chọn tối đa 5 object {prop,slot}. Không dùng
  NONE ở đối tượng phụ; slot BACK_LEFT/BACK_RIGHT/LEFT/RIGHT/FOREGROUND mỗi slot một lần.
  Mỗi beat thay cả danh sách, không cộng dồn. Không có tọa độ, màu, số lượng hay ảnh tùy ý.
- HOME/CLASSROOM/PLAYGROUND/GARDEN/FOREST/BEACH/MARKET/LIBRARY có phông hư cấu tương ứng.
  Chọn đúng ngữ cảnh; không tuyên bố địa danh/vị trí thực. Hình khuôn mặt/quần áo
  có đặc điểm cố định, không hứa thay đổi diện mạo ngoài danh mục.

MẠCH TRUYỆN
- Bắt đầu bằng “Trong câu chuyện tưởng tượng, ...”. Tình huống gần gũi, giải quyết
  tích cực, lời tự nhiên; cảm xúc tiêu cực nhẹ, có sự hỗ trợ, không hù dọa/chê bai.
- Kể liền mạch khoảng 4–7 phút rồi mới hỏi: 8 cảnh. Cảnh 1–6 mỗi cảnh 6 beats;
  cảnh 6 kết thúc cốt truyện trước câu hỏi 1. Cảnh 7–8 mỗi cảnh một câu dẫn ngắn
  rồi hỏi câu 2–3. Cảnh 1–5 question=null. Không hỏi trong lời kể.
- Phần kể chính khoảng 1.400–1.500 đơn vị cách trắng tiếng Việt; đây là ước tính
  theo tốc độ đọc, không phải phép đo âm thanh. Mỗi beat 4–240 ký tự, <=48 đơn vị.
- BOTH có question target theo thứ tự SAU, ONG, BOTH. SAU/ONG có cả ba câu hỏi
  cho đúng bé đó. Hỏi điều vừa nghe, không phụ thuộc hình còn tồn tại, màu/trang
  phục/vị trí/số lượng trên màn hình. acceptedConcepts có 1–4 cách diễn đạt cùng ý.
- hint nhẹ; feedback ghi nhận nỗ lực; silenceFallback nhắc ý chính, không ép trả lời.
- Không có GPS, địa chỉ, tên thật, dữ liệu cá nhân, URL, ghi âm, hành vi nguy hiểm
  hay yêu cầu trẻ đứng/chạy/nhảy trong xe. Không bịa kiến thức lịch sử/khoa học.
- Tránh cụm “từ từ” vì bộ lọc hiện tại; dùng “chậm rãi”. Không emoji/ký tự điều khiển.

JSON CHÍNH XÁC
Chỉ trả JSON UTF-8: {"schemaVersion":1,"stories":[...]}.
Mỗi truyện mới có đúng:
id, schemaVersion=2, revision=1, title, audience, sauAge=5, ongAge=4,
educationalGoal, synopsis, scenes.
ID dạng [a-z][a-z0-9-]{1,63}, duy nhất. Title 3–100 ký tự; goal 4–180;
synopsis 8–320; audience bằng DOI_TUONG. Giữ cả hai trường tuổi.
Mỗi scene: id duy nhất, backdrop, beats, question (null hoặc object).
Mỗi beat: actor, utterance, emotion, action, prop, supportingProps (có thể bỏ).
Mỗi question: target, prompt, acceptedConcepts, hint, feedback, silenceFallback,
maxWaitSeconds=5. Prompt 5–180 ký tự và kết thúc ?. Hint/feedback/silenceFallback
4–180 ký tự; mỗi concept 1–80 ký tự. App áp dụng thời gian chờ 3–5 giây sau lời hỏi.
Không trường lạ, khóa trùng, comment hay dấu phẩy cuối. Tối đa 100 truyện/4MiB;
mỗi truyện <=48000 ký tự/65536 byte. Chỉ dùng enum và trường đã liệt kê.
```

Kiểm tra trước khi đăng:

```bash
./gradlew :core:checkStoryPack -PstoryPack=/duong/dan/stories.json
```

Xem [pack kỹ thuật v3](stories.world.example.json) để học cách ghép đối tượng.
Đây là ví dụ khả năng, không thay yêu cầu mạch truyện kể dài trong prompt trên.
Nguồn vẫn chỉ tải JSON; thêm hình hoặc động tác chưa có cần một bản APK khác.
