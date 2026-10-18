# Thư viện truyện kể Sâu/Ong

File [stories.example.json](stories.example.json) hiện có **6 cốt truyện × 3 chế độ SAU/ONG/BOTH = 18 bản truyện**;
giữ nguyên chín bản T-067 và nối thêm chín bản T-073. Mỗi bản có lời của
Sâu, Ong hoặc cả hai đồng hành cùng Capybara. Các phiên bản cùng chủ đề chia sẻ
cốt truyện; đây là sáu cốt truyện, không phải mười tám cốt truyện khác nhau.

| Cốt truyện | ID gốc (thêm `-sau`, `-ong`, `-both`) | Điều cùng học |
| --- | --- | --- |
| Rùa và Thỏ | `tortoise-hare` | Kiên trì, tôn trọng bạn, biết xin lỗi |
| Rùa con tìm nhà | `little-turtle-home` | Tôn trọng chỗ ở của bạn, hiểu về mai Rùa, tình thương gia đình |
| Sói và bảy dê con | `wolf-seven-kids` | Bình tĩnh, ở cùng nhau, nhờ người lớn đáng tin giúp |
| Quạ và bình nước | `crow-pitcher` | Quan sát, thử từng bước, chăm sóc bạn trước khi khám phá |
| Sư tử và Chuột | `lion-mouse` | Tôn trọng khả năng khác nhau, lắng nghe và biết nhờ giúp |
| Những người bạn tí hon và bác thợ giày | `elves-shoemaker` | Biết ơn bằng việc cụ thể, hỏi điều bạn cần, tôn trọng lựa chọn |

Đây là lời kể mới do dự án biên soạn. Bản Sói và bảy dê con có kết thúc dịu dàng:
cả bảy bạn đều bình yên, Sói dừng việc lừa gạt; không có cảnh nuốt, mổ bụng hay
bạo lực. Rùa con tìm nhà là câu chuyện tưởng tượng riêng của bộ này, không chép
lời từ một ấn bản cùng tên.

Mỗi bản kể liền mạch qua sáu cảnh, kết thúc rõ bằng lời khép sách, rồi mới có ba
câu hỏi nhớ lại. Câu thứ nhất ở sau câu kết của cảnh sáu; hai cảnh còn lại chỉ
là lượt hỏi sau truyện. Không có câu hỏi xen vào diễn biến truyện. BOTH lần lượt
hỏi Sâu, Ong và cả hai; mỗi lượt có `maxWaitSeconds: 6`. Playback director bắt đầu chờ sau khi đọc xong câu hỏi;
Android T-068 giới hạn cửa sổ chờ thực tế 3–5 giây. Phản hồi có thể đến sớm hơn
khi bé đã trả lời.

Phần kể chính dài khoảng 1.470–1.540 đơn vị cách trắng tiếng Việt (gần với âm
tiết, không phải số từ vựng). Với giả định 220–320 âm tiết/phút, thời lượng kể
ước tính **4,6–7 phút**, chưa cộng ba câu hỏi, phản hồi và thời gian chờ. Tốc độ
TTS, dấu câu và thiết lập giọng có thể làm thời gian thực khác đi; chưa đo trọn
bản bằng giọng trên thiết bị. Kiểm tra tự động bảo vệ khoảng ước tính 4–7 phút.

Truyện được dẫn bằng Capybara đọc sách và các bé tham gia. Rùa, Sói, dê con là
nhân vật trong lời kể; app chưa có hình riêng cho các nhân vật đó. Bộ này dùng
`READ/BOOK` và `WAVE/NONE`, không gán hình Cáo thành Sói hay hình khác thành Rùa.

Nguồn vẫn là URL `main` đã cấu hình: [hướng dẫn đồng bộ](README.md). Khi app đồng
bộ thành công, 18 ID của nguồn được cập nhật; chín ID T-067 và nội dung của chúng giữ nguyên. Truyện lưu riêng trên
thiết bị không thuộc file nguồn này. File `stories.expanded.example.json` vẫn
là fixture giới thiệu hoạt cảnh riêng, không phải bộ truyện đang cấu hình.

## Các truyện nghiên cứu và biên tập T-073

Xem [nguồn và lựa chọn biên tập](RESEARCH.md). Toàn bộ lời Việt mới do dự án
biên soạn từ cốt truyện cổ, không chép bản dịch tiếng Việt hiện đại. Trong bản
BOTH, Sâu đề xuất/quan sát, Ong hỗ trợ bạn/chuẩn bị và Capybara trực tiếp cùng làm.
Hai bản đơn chuyển cả hai phần việc cho trẻ được chọn, không nhắc trẻ vắng mặt.
Mỗi trẻ hiện diện có ít nhất sáu lượt lời trong phần kể. Truyện mới 1.529–1.540
đơn vị cách trắng, ước tính 4,78–7 phút; chưa đo TTS trên máy.

Nguồn chính giữ episode schema v1, minh họa bằng người kể và các bé như chín
truyện trước; không hứa có sprite Quạ, Sư tử, Chuột hay người tí hon. Pack thế
giới schema v2 riêng vẫn dùng hoạt ảnh nguyên nhân vật của T-071. Thêm truyện
không tự chuyển đổi truyện v1 sang sân khấu v2.

`maxWaitSeconds: 6` là giá trị trong file và hợp đồng playback director. Android
có chính sách chờ câu trả lời 3–5 giây của T-068; không suy giá trị trong JSON
thành thời gian chờ đã đo trên thiết bị hiện tại.
