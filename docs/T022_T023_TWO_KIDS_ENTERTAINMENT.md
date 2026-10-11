> Current local checkpoint 2026-10-11: 66 authored beats (18 SAU, 18 ONG,
> 18 BOTH, 12 COMMON), 18 opening slots/audience, compact captions and
> high-resolution story poses. Local 103 core tests/build and three-audience
> physical speech checks PASS. See [T023_STORY_COMPANIONS.md](T023_STORY_COMPANIONS.md)
> and current T-011 evidence; historical pending/48-beat notes below are
> superseded for source/build/audio scope only. Human/road/navigation gates stay open.

# T-022 + T-023 — Chế độ Sâu / Ong / Cả hai và giải trí liên tục

Ngày: 2026-10-10. Source tích hợp trên nhánh main. **Chưa chạy full local Gradle/ADB sau thay đổi trong môi trường kết nối GitHub; hardware child-engagement gate OPEN.** Không bật GitHub Actions CI.

## Chọn đối tượng xem (T-022)

Ngay Parent Dashboard trên cùng màn hình DeX, chọn:
- **Cho Sâu (4 tuổi)**: tập SAU + tập nội dung COMMON, không nhắc tên Ong.
- **Cho Ong (3 tuổi)**: tập ONG + tập COMMON, không nhắc tên Sâu.
- **Cả Sâu và Ong cùng xem (3–4 tuổi)**, mặc định: luân phiên SAU → ONG → BOTH, tôn trọng lượt của từng bé.

Cấu hình cục bộ `parent_settings.audience_mode` (`SAU | ONG | BOTH`), age đang dùng cho POI narration / knowledge / AI quiz cache: Sâu=4; Ong=3; cả hai=3 (chọn mức dễ hơn để cả hai cùng theo dõi). Legacy `ageGroup` được giữ cho tương thích, nhưng chọn người xem là nguồn cấu hình đang dùng. Reset local options đưa về BOTH. Không nhận dạng danh tính trẻ bằng microphone, không lưu hồ sơ riêng, không gửi dữ liệu mới ra mạng.

## Trò chơi trên màn hình (T-023)

- Pure-Java `EntertainmentDirector`: 48 tình huống hoàn toàn offline, 12 SAU + 12 ONG + 12 BOTH + 12 COMMON. Số lượng hữu hạn, sau khi hết vòng sẽ chơi lại theo thứ tự xoay vòng. Kiểm thử JUnit kiểm tra cân bằng lượt và không nhắc tên sai trong chế độ một bé.
- Mỗi tình huống có lời mời ngắn + câu trả lời/kết, phụ đề chữ lớn dành cho trẻ, TTS Việt offline tùy quyền đã bật và cử chỉ Capybara WAVE/SURPRISE (không thêm texture nặng vào hot path).
- Bắt đầu sau khoảng 8s; khoảng cách giữa các **lần bắt đầu** 42s (bao gồm đọc, ngừng cho trẻ phản ứng và giải thích). Không nghe microphone trong riêng chế độ giải trí thụ động. Không Internet/AI key/GPS cũng chạy.
- Địa danh có source ở LIVE/GPX có ưu tiên: hủy tác vụ thoại trước, hoãn trò chơi ít nhất 90s. Không tạo câu nói định vị khi chỉ có sự kiện hư cấu. Các bản Route DEMO vẫn tồn tại, khi phát route card sẽ hoãn sự kiện giải trí tối thiểu 38s.
- Câu đố chung legacy 5 phút vẫn là fallback; các sự kiện giải trí thường đặt lại khoảng đợi để không phát chồng tiếng.
- Pause, Activity background, Parent Menu sẽ dừng giọng và ẩn thẻ trẻ. Session deadline giữ nguyên. Activity recreation ghi nhớ vị trí nội dung và thời điểm sự kiện tiếp theo.
- Luồng lời nói phản hồi qua Android TTS callback có epoch guard, không phát tiếp callback của tình huống đã bị hủy. Không đổi thiết lập permission, child transcript consent, AI key, GPS gate hay IPC signing.

## Nghiệm thu

Local checkout:
```bash
./scripts/test-local.sh
./gradlew :android:assembleDebug
```
Trong `scripts/test-local.sh` đã thêm `tools/test_dual_child_entertainment.py`; `EntertainmentDirectorTest` chạy trong `:core:test`. Đây là **test đã thêm**, không phải chứng cứ đã PASS.

QA Fold3 FullHD/DeX: kiểm tra cả 3 lựa chọn (mặc định BOTH); nghe nội dung phát âm chuẩn tên hai bé, tốc độ thoại và 3 lần event; chắc chắn SOLO không đọc tên bé còn lại; kiểm tra 15–20 phút xem chung, cảnh thực tế không có POI, mất GPS, Parent F10, mất quyền microphone, không key AI, TTS chưa khởi động, GPS POI xen vào, kéo cáp màn hình, navigation voice overlap, không ANR/giật FPS. Ghi nhận phản ứng của Sâu/Ong do phụ huynh quan sát, **không ghi âm trẻ**.

Chưa thêm hệ thống tương tác chạm, game thắng/thua, nhận dạng hai giọng riêng, hoặc animation riêng cho từng động vật; đó là các hạng mục mở rộng dựa trên thử nghiệm thực tế.
