# T-026 — Kịch bản mô phỏng POI 30 phút

Owner: Main. Dependencies: T-014/T-019/T-021–T-025. Trạng thái: PLANNED; chưa chạy phiên 30 phút.

Mục tiêu: kiểm tra toàn bộ 26 point hiện có thuộc Đồng Nai → Vũng Tàu → Đà Lạt → TP.HCM. Tọa độ/ID lấy nguyên từ catalog trong repo; không phải tuyến đường thật hoặc GPS ngoài thực địa. Không tải tọa độ/audio/câu trả lời trẻ lên server.

| Thời gian | Chặng | Số point |
| --- | --- | --- |
| 00:00–01:00 | Chuẩn bị DeX, Full, Cả Sâu và Ong, offline TTS/effects; AI/cloud OFF | — |
| 01:00–08:00 | Đồng Nai | 7 |
| 08:00–14:00 | Vũng Tàu | 6 |
| 14:00–23:00 | Đà Lạt | 9 |
| 23:00–27:00 | TP.HCM sample | 4 |
| 27:00–28:00 | F10: pause/resume; âm thanh dừng, không mất phiên | — |
| 28:00–29:00 | Thu FPS/P95/PSS/thermal, đối chiếu ID đã phát | — |
| 29:00–30:00 | Chờ hết hạn phiên, trở về Parent cùng màn DeX | — |

## Quy trình mỗi point (60 giây)

Mỗi point là một fixture độc lập: khởi tạo/reset feed và POI engine, đưa fix cách point khoảng 40m rồi tiến qua tọa độ và ra khoảng 60m ở tốc độ 3–5m/s, accuracy 5m, fix mới mỗi giây. Cần hai fix tốt trước khi kỳ vọng event. Giữ đủ thời gian theo dõi giới thiệu, quiz +16s, answer +32s, chat +48s tính từ event thực tế. Ghi ID event, phụ đề, TTS/cancel, cảnh, không coi chuyển tọa độ là PASS. Nếu setup/event làm thiếu thời gian thì đánh dấu PARTIAL, không ép toàn bộ hội thoại thành PASS. Không chờ cooldown giữa các fixture bằng cách sửa đồng hồ production.

## Điều kiện thực thi trên bản hiện tại

GPX import thường tải reviewed.tsv đang trống; chỉ LIVE tải 40 landmarks. HCMC sample tải catalog riêng 4 point. GPX loại đoạn vượt tốc độ hợp lý; LIVE chặn teleport. Vì thế không thể chỉ import GPX 30 phút nhảy liên tỉnh rồi kỳ vọng 26 event. Chạy tự động cần test-only harness khởi tạo từng fixture bằng catalog đúng và reset trạng thái, nối vào màn/giọng production; chưa có harness toàn tuyến trong build hiện tại. Không bỏ bộ lọc production. Trên Fold3 không tự bật mock-location toàn hệ thống, để tránh ảnh hưởng Maps/VietMap. Chưa chạy hay cài thay APK trong tác vụ chuẩn bị này.

## Danh sách đầy đủ theo thứ tự

| Slot | Khu vực | Point | ID |
| --- | --- | --- | --- |
| 01:00–02:00 | Đồng Nai | Công viên Xuân An | osm:way:530247697 |
| 02:00–03:00 | Đồng Nai | Đá Ba Chồng | osm:node:9974267816 |
| 03:00–04:00 | Đồng Nai | Khu du lịch Suối Tre | osm:way:717517497 |
| 04:00–05:00 | Đồng Nai | Công viên Biên Hùng | osm:way:527470174 |
| 05:00–06:00 | Đồng Nai | Công viên Quyết Thắng | osm:way:294557192 |
| 06:00–07:00 | Đồng Nai | Công viên Dương Tử Giang | osm:way:974991933 |
| 07:00–08:00 | Đồng Nai | Công viên Cách mạng Tháng 8 | osm:way:527470176 |
| 08:00–09:00 | Vũng Tàu | Bãi Sau Vũng Tàu | osm:way:234800751 |
| 09:00–10:00 | Vũng Tàu | Quảng trường Bãi Sau | osm:way:612283152 |
| 10:00–11:00 | Vũng Tàu | Núi Đinh | osm:node:5537927332 |
| 11:00–12:00 | Vũng Tàu | Hồ Đá Xanh | osm:node:5666383122 |
| 12:00–13:00 | Vũng Tàu | Hải đăng Vũng Tàu | osm:way:229301316 |
| 13:00–14:00 | Vũng Tàu | Tượng Chúa Kitô Vua Vũng Tàu | osm:way:235008112 |
| 14:00–15:00 | Đà Lạt | Thác Datanla | osm:node:4502225092 |
| 15:00–16:00 | Đà Lạt | Chùa Linh Phước | osm:way:522159670 |
| 16:00–17:00 | Đà Lạt | Thác Prenn | osm:node:4096525696 |
| 17:00–18:00 | Đà Lạt | Quảng trường Lâm Viên | osm:way:1303837487 |
| 18:00–19:00 | Đà Lạt | Vườn hoa thành phố Đà Lạt | osm:node:11916349146 |
| 19:00–20:00 | Đà Lạt | Điểm ngắm Hồ Tuyền Lâm | osm:node:11598941137 |
| 20:00–21:00 | Đà Lạt | Đồi chè Cầu Đất | osm:node:6546337786 |
| 21:00–22:00 | Đà Lạt | Thác Pongour | osm:node:11964064649 |
| 22:00–23:00 | Đà Lạt | Vườn hoa cẩm tú cầu | osm:node:5666385423 |
| 23:00–24:00 | TP.HCM | Công viên Tao Đàn | osm:way:500888743 |
| 24:00–25:00 | TP.HCM | Dinh Độc Lập | osm:way:39598493 |
| 25:00–26:00 | TP.HCM | Bảo tàng Chứng tích Chiến tranh | osm:way:186249226 |
| 26:00–27:00 | TP.HCM | Bưu điện Trung tâm Sài Gòn | osm:way:39514793 |

## Biến thể “hết mọi point trong repo”

Nếu hiểu “hết các point” là toàn bộ catalog: all-catalog-points.json có 44 ID duy nhất (40 LIVE + 4 HCMC), bao gồm thêm 18 point Phan Thiết/Bảo Lộc/hành lang Nha Trang. 00:00–01:00 setup, 44 slot ×36s đến 27:24, còn 2:36 kiểm tra lifecycle/kết thúc. Biến thể này chỉ kiểm tra trigger/giới thiệu/quiz; không đủ +48s chat mỗi point. Muốn nghiệm thu trọn hội thoại 44 point cần phiên dài hơn 30 phút.

## Acceptance và evidence

- 26/26 ID phát event đúng fixture, không phát tên POI khác; lưu kết quả từng ID (manifest khởi tạo NOT_RUN).
- Không crash/ANR; một màn DeX, không tự chuyển sang phone.
- Caption/âm thanh/cảnh đúng catalog; F10 dừng/resume; hết hạn đúng 30 phút.
- Ghi FPS/P95/PSS/thermal bằng công cụ hiện có; FPS mục tiêu ≥30, không ghi số liệu giả.
- Chất lượng giọng, ASR và phản ứng trẻ do người quan sát đánh giá; không ghi âm trẻ.
- Lưu log/screenshot và preferences trước/sau; cleanup fixture/test APK, khôi phục tùy chọn.

Các manifest nằm tại test-data/gps/T-026/. Phiên 30 phút và toàn bộ gate runtime vẫn NOT_RUN.
