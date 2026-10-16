# Thư viện truyện mẫu T-064

Bộ `stories.example.json` có **30 truyện**, gồm 10 chủ đề × 3 chế độ:
SAU (Sâu), ONG (Ong), BOTH (cả hai). Cùng chủ đề được chuyển lời thoại và lượt hỏi
cho từng chế độ; đây là 10 cốt truyện, không phải 30 cốt truyện độc lập.

Ba truyện khu vườn cũ giữ nguyên. Thêm ba bản giới thiệu ngày hội khám phá và
24 bản thuộc tám chủ đề mới. Mỗi truyện có ba câu hỏi; BOTH lần lượt hỏi Sâu,
Ong rồi cả hai. Câu hỏi dựa vào lời vừa kể; các hình ở lượt trước có thể đã đổi.

| Chủ đề | ID gốc (thêm `-sau`, `-ong`, `-both`) | Hoạt động chính |
| --- | --- | --- |
| Khu vườn của những người bạn | `garden` | WAVE, LOOK_UP, PET |
| Ngày hội khám phá của những người bạn | `discovery-day` | JUMP, DANCE, BOW, READ, WATER, SHARE, INSPECT, WAVE |
| Cuốn sách dành cho bạn Cáo | `little-library` | WAVE, READ, SHARE, BOW |
| Lời hẹn với cây non | `seedling-care` | WAVE, WATER, INSPECT, BOW |
| Chiếc giỏ và lời mời | `picnic-sharing` | WAVE, SHARE, BOW, LOOK_UP, DANCE |
| Vỏ sò của bạn Vịt | `shell-listener` | WAVE, BOW, INSPECT, READ |
| Ngày hội ngôi sao giấy | `paper-star-party` | WAVE, INSPECT, BOW, COUNT, JUMP, SHARE, DANCE |
| Đám mây và chiếc ô | `cloud-and-umbrella` | WAVE, LOOK_UP, BOW, READ, SHARE |
| Một lời hỏi trước | `gentle-animal-friends` | WAVE, BOW, PET, SHARE |
| Bí mật của món quà nhỏ | `tiny-clue-quest` | READ, WAVE, INSPECT, BOW, SHARE, DANCE |

Bộ truyện dùng đủ 11 action và 27 giá trị prop (kể cả NONE) đang có trong APK T-063.
Đọc sách đi với BOOK; tưới đi với SEEDLING/FLOWER; kính lúp quan sát vật nhỏ.
Các bạn thú là hình minh họa, actor nói vẫn là SAU/ONG/CAPYBARA.

Cài APK T-063 trước khi dùng bộ này. [Hướng dẫn URL và đồng bộ](README.md).
Dùng [prompt tạo truyện](AUTHORING_PROMPT.md) để nối truyện mới, giữ các ID cũ.
Kiểm tra bằng `./gradlew :core:checkStoryPack` trước khi đăng.
