# T-076 — ứng viên truyện YouTube bổ sung (2026-10-20)

Đã lọc tiêu đề/mô tả và kiểm tra metadata hiện tại; chưa xem duyệt toàn bộ hình/tiếng.
Độ tuổi4–7/BOTH là phạm vi ứng viên để phụ huynh đánh giá, không phải chứng nhận phù hợp.
Không tự đánh dấu đã duyệt, không bật link hoặc tự động phát.

| Truyện | Kênh | Thời lượng API | Link |
| --- | --- | --- | --- |
| Nhổ củ cải | Kể Bé Nghe | 3:33 | [YouTube](https://www.youtube.com/watch?v=Tfkf3NStsx4) |
| Chú đỗ con | Kênh Thiếu Nhi - BHMEDIA | 2:11 | [YouTube](https://www.youtube.com/watch?v=82bWFKrP8rg) |
| Cây táo | Cô Nga Mầm non | 1:32 | [YouTube](https://www.youtube.com/watch?v=TgUeLqOpj5Y) |
| Giọt nước tí xíu | BEE KIDS TV | 3:27 | [YouTube](https://www.youtube.com/watch?v=-pII-it4wrA) |
| Chiếc ô của thỏ con | THƯ VIỆN MẦM NON | 1:26 | [YouTube](https://www.youtube.com/watch?v=7hKzVZfuwz0) |
| Gấu con chia quà | Kể Bé Nghe | 3:35 | [YouTube](https://www.youtube.com/watch?v=RuRjL37Xl4I) |

Mỗi thời lượng cấu hình làm tròn lên phút. Cả sáu video hiện MFK=true, cho phép nhúng, không livestream/giới hạn tuổi/chặn VN và không quá4phút. App kiểm tra lại metadata trước khi phát.

Các chủ đề/điểm cần phụ huynh xem lại:
- Nhổ củ cải: Hợp tác; phụ huynh kiểm tra cách kể và vai trò của các nhân vật.
- Chú đỗ con: Thiên nhiên; phụ huynh phân biệt nhân hóa trong truyện với kiến thức về cây.
- Cây táo: Chăm sóc cây; truyện đơn giản, phụ huynh đánh giá mức độ phù hợp với từng bé.
- Giọt nước tí xíu: Thiên nhiên; phụ huynh kiểm tra độ chính xác của cách mô tả vòng tuần hoàn nước.
- Chiếc ô của thỏ con: Ứng viên về quan hệ bạn bè; cần xem hết bản video trước khi duyệt.
- Gấu con chia quà: Chia sẻ; cần xem hết bản video trước khi duyệt.

Tìm kiếm bằng YouTube Data API theo6chủ đề chung (safeSearch=strict, max3kết quả/chủ đề); không gửi thông tin trẻ.
Loại4kết quả MFK=false; bỏ bản trùng/chủ đề quá thiên về trẻ2–3tuổi.
Nguồn đối chiếu cho link Chú đỗ con: [Mầm non Thạch Cầu](https://mnthachcau.hanoi.edu.vn/tho-truyen-cau-do/truyen-chu-do-con/ctmb/3814/508259).
Web fetch trang xem bị throttled; metadata lấy trực tiếp qua API, không tuyên bố đã xem toàn bộ video.
