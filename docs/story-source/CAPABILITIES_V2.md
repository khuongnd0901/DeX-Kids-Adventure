# Bộ hoạt cảnh mở rộng T-063

Bản app T-063 có **11 hành động, 27 lựa chọn đạo cụ**: thêm7hành động,
12đạo cụ gốc và4bạn thú đã có sẵn. Các hình gốc Sâu/Ong/Capybara và quần áo
được giữ nguyên; chuyển động mới dùng pose có sẵn kết hợp biến đổi toàn thân,
vật dụng gắn theo nhân vật và hiệu ứng nhỏ. Không có bộ khung tay/chân mới.

Cần cài APK T-063 một lần trước khi dùng enum mới. Schema JSON vẫn là1 và
truyện cũ vẫn đọc được. App T-062 sẽ từ chối pack chứa enum mới và giữ cache cũ;
bộ mẫu hiện tại trên main cần APK T-063. Bộ 30 truyện T-064 đã được thay bằng
[chín bản truyện kể dài](STORY_LIBRARY.md) trong T-067. Các kết quả kiểm tra
truyện cũ không phải bằng chứng đã nghe trọn các truyện mới.
Sau khi cài app mới, tác giả tiếp tục thêm/sửa truyện qua URL, không build lại.

| Action | Minh họa thực tế | Prop hợp lệ |
| --- | --- | --- |
| WAVE | Chào/vẫy tay từ pose gốc | Đạo cụ quen thuộc hoặc NONE |
| LOOK_UP | Pose ngước nhìn + chuyển động nhỏ | Nên dùng BIRD/BUTTERFLY/CLOUD/KITE/TREE; FLOWER trên cành |
| COUNT | Nhịp đếm bằng lời | Chỉ NONE; không biểu diễn số lượng hình |
| PET | Pose vuốt ve gốc | Nên dùng CAT/RABBIT |
| JUMP | Nhân vật bật lên và về vị trí ban đầu, bóng dưới chân | NONE hoặc vật bên cạnh |
| DANCE | Nhún lên, nghiêng và lắc người theo nhịp | NONE hoặc vật bên cạnh |
| BOW | Nghiêng toàn thân cúi chào nhẹ | NONE hoặc vật bên cạnh |
| READ | Sách mở trước thân, điểm sáng trang sách | Chỉ BOOK |
| WATER | Pose cúi, bình tưới nghiêng và giọt nước tới cây | SEEDLING hoặc FLOWER |
| SHARE | Đưa một vật ra trước thân, nhịp nghiêng nhẹ | BOOK/BALL/GIFT/APPLE/BASKET/FLOWER |
| INSPECT | Kính lúp cạnh vật quan sát | LEAF/SHELL/STAR/FLOWER |

SHARE biểu diễn đưa vật ra mời/chia sẻ, không có mô hình sở hữu hay chuyển vật
hoàn chỉnh giữa hai bàn tay. READ/WATER/INSPECT không làm thay hình tay gốc.
Các hành động thuộc nhân vật trong truyện tưởng tượng; không yêu cầu bé thật
đứng dậy, nhảy hoặc di chuyển trong xe. Chưa chứng nhận chất lượng mỹ thuật bởi
người dùng hay hiệu năng trên DeX thật.

Đạo cụ mới: BOOK (sách), BALL (bóng), GIFT (hộp quà), WATERING_CAN (bình tưới),
SEEDLING (cây non), APPLE (táo), BASKET (giỏ), STAR (hình sao giấy), LEAF (lá),
SHELL (vỏ sò), UMBRELLA (ô), MAGNIFYING_GLASS (kính lúp).
Bốn bạn thú mở thêm: FOX, PANDA, PENGUIN, DUCK. Đây là hình hỗ trợ, không thêm
actor nói; actor vẫn chỉ SAU/ONG/CAPYBARA. WATER tự kèm bình tưới, INSPECT tự kèm
kính lúp; trường prop của chúng chỉ vật cây/hoa/lá/vỏ sò đang được chăm/quan sát.

Danh mục đầy đủ và luật ghép nằm trong `StoryActionCatalog` của core và được gửi
cùng prompt tạo/viết lại/duyệt truyện trong app. [Prompt bên ngoài](AUTHORING_PROMPT.md)
cũng chứa danh mục mới. [Pack mẫu mở rộng](stories.expanded.example.json) có ba
truyện cho SAU/ONG/BOTH, mỗi truyện8cảnh với đủ7hành động mới.

Kiểm tra trước khi dùng:

```bash
./gradlew :core:checkStoryPack -PstoryPack=/duong/dan/stories.expanded.example.json
```

Không thêm số lượng, màu tùy ý, ảnh mạng, tên animation mới hoặc tọa độ vào JSON.
Câu hỏi vẫn dùng cue trung tính; nên hỏi điều vừa nghe, không đòi vật của lượt trước
còn hiển thị. Các cách ghép mới sai sẽ bị validator chặn toàn bộ pack trước khi
thay bộ đang sử dụng. Các tổ hợp legacy vẫn giữ cách chấp nhận trước đó để không
làm hỏng truyện cũ; prompt hướng dẫn tổ hợp tự nhiên hơn cho truyện mới.
