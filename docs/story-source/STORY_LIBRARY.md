# Thư viện truyện kể Sâu/Ong

File [stories.example.json](stories.example.json) thay toàn bộ bộ 30 truyện mẫu cũ
bằng **3 cốt truyện × 3 chế độ SAU/ONG/BOTH = 9 bản truyện**. Mỗi bản có lời của
Sâu, Ong hoặc cả hai đồng hành cùng Capybara. Các phiên bản cùng chủ đề chia sẻ
cốt truyện; đây không phải chín cốt truyện khác nhau.

| Cốt truyện | ID gốc (thêm `-sau`, `-ong`, `-both`) | Điều cùng học |
| --- | --- | --- |
| Rùa và Thỏ | `tortoise-hare` | Kiên trì, tôn trọng bạn, biết xin lỗi |
| Rùa con tìm nhà | `little-turtle-home` | Tôn trọng chỗ ở của bạn, hiểu về mai Rùa, tình thương gia đình |
| Sói và bảy dê con | `wolf-seven-kids` | Bình tĩnh, ở cùng nhau, nhờ người lớn đáng tin giúp |

Đây là lời kể mới do dự án biên soạn. Bản Sói và bảy dê con có kết thúc dịu dàng:
cả bảy bạn đều bình yên, Sói dừng việc lừa gạt; không có cảnh nuốt, mổ bụng hay
bạo lực. Rùa con tìm nhà là câu chuyện tưởng tượng riêng của bộ này, không chép
lời từ một ấn bản cùng tên.

Mỗi bản kể liền mạch qua sáu cảnh, kết thúc rõ bằng lời khép sách, rồi mới có ba
câu hỏi nhớ lại. Câu thứ nhất ở sau câu kết của cảnh sáu; hai cảnh còn lại chỉ
là lượt hỏi sau truyện. Không có câu hỏi xen vào diễn biến truyện. BOTH lần lượt
hỏi Sâu, Ong và cả hai; mỗi lượt có `maxWaitSeconds: 6`. App bắt đầu chờ sau khi
đọc xong câu hỏi; khi chưa có câu trả lời, không phát lời tiếp trong sáu giây.
Nếu nhận được câu trả lời sớm, app có thể phản hồi sớm theo luồng hiện tại.

Phần kể chính dài khoảng 1.470–1.540 đơn vị cách trắng tiếng Việt (gần với âm
tiết, không phải số từ vựng). Với giả định 220–320 âm tiết/phút, thời lượng kể
ước tính **4,6–7 phút**, chưa cộng ba câu hỏi, phản hồi và thời gian chờ. Tốc độ
TTS, dấu câu và thiết lập giọng có thể làm thời gian thực khác đi; chưa đo trọn
bản bằng giọng trên thiết bị. Kiểm tra tự động bảo vệ khoảng ước tính 4–7 phút.

Truyện được dẫn bằng Capybara đọc sách và các bé tham gia. Rùa, Sói, dê con là
nhân vật trong lời kể; app chưa có hình riêng cho các nhân vật đó. Bộ này dùng
`READ/BOOK` và `WAVE/NONE`, không gán hình Cáo thành Sói hay hình khác thành Rùa.

Nguồn vẫn là URL `main` đã cấu hình: [hướng dẫn đồng bộ](README.md). Khi app đồng
bộ thành công, chín ID mới thay các ID của bộ nguồn cũ. Truyện lưu riêng trên
thiết bị không thuộc file nguồn này. File `stories.expanded.example.json` vẫn
là fixture giới thiệu hoạt cảnh riêng, không phải bộ truyện đang cấu hình.
