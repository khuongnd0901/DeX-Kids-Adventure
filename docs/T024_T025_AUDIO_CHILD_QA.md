# T-024 + T-025 — Âm thanh hoạt hình và kiểm thử trải nghiệm Sâu/Ong

Ngày: 2026-10-10. **Đã tích hợp source lên `main`; chưa xác nhận Gradle/âm thanh thật/hành vi hai bé.** Không bật CI.

## 1. T-024 — Audio Experience

### Tính năng
- **Âm thanh offline không tải mẫu trên mạng:** `KidSoundscape` tự tổng hợp WAV nhỏ trong cache riêng, nạp bất đồng bộ qua `SoundPool`. Hiệu ứng **chime**, **chim**, **mèo**, **thỏ**, **xe buýt** là âm tổng hợp mô phỏng, *không phải* tiếng động vật thật.
- Âm lượng hiệu ứng ứng dụng cố định thấp (`0.14f` ở SoundPool), tránh chạm mức âm lượng thiết bị. Có thể bật/tắt qua **Cài đặt → Giọng kể và microphone → Hiệu ứng thú và xe**. Mặc định bật.
- **Nhạc nền nhẹ** (4 giây melody synth lặp) là tùy chọn **mặc định tắt**, âm lượng `0.035f`. Hữu ích khi xem DEMO; khi lái xe có Google Maps/VietMap nên cân nhắc để tắt.
- Khi TTS Capybara bắt đầu, nhạc nền dừng. Khi TTS kết thúc, chỉ chạy lại nếu app foreground, Parent Menu đóng và cài đặt cho phép. Mở F10, ứng dụng vào background, tắt game đều dừng toàn bộ; giải phóng SoundPool và xóa file cache khi shutdown.
- Offline Vietnamese TTS chỉ dùng voice `isNetworkConnectionRequired=false` và chọn chất lượng offline cao nhất. Tốc độ TTS giảm nhẹ: Ong (3) / BOTH (3) 0.88; Sâu (4) 0.94. Hủy TTS không giả lập `onFinished`, nhằm tránh khởi động microphone hoặc lời kể tiếp từ callback cũ.
- **Giới hạn:** phần nhạc nền *không yêu cầu audio focus* nên không chiếm quyền các ứng dụng chỉ đường; giọng TTS dùng focus transient may-duck. Chưa xác nhận nhạc/TTS trộn đúng với âm thanh Google Maps/VietMap trên Fold3. Không tuyên bố đã có ducking âm thanh ứng dụng thứ ba.

### QA âm thanh trên Fold3 (nghe bằng tai)
| Case | Thao tác | Kết quả kỳ vọng |
|---|---|---|
| A01 | DEMO / mặc định không mạng, chưa cấu hình AI | Thoại và phụ đề chạy; hiệu ứng thú/xe khi sự kiện phù hợp, không crash |
| A02 | Cài đặt tắt/bật hiệu ứng | Tắt không còn cue; bật lại có cue sau sự kiện mới |
| A03 | Cài đặt tắt/bật nhạc nền | Mặc định im lặng giữa thoại; bật thì nhạc cực nhỏ, không gián đoạn lời |
| A04 | TTS bắt đầu, kết thúc, TTS chưa ready | Nhạc dừng khi nói, resume có điều kiện; không kẹt hoặc lặp lời |
| A05 | F10 Parent / resume / background / foreground | Im tiếng khi menu mở/app nền, resume chỉ khi đang xem |
| A06 | GPS có POI cắt sự kiện giải trí / mở lại sau 90s | Không nghe lời cũ tiếp, địa danh được ưu tiên |
| A07 | Google Maps và VietMap live đọc chỉ dẫn, một chuyến thật | Không làm mất chỉ dẫn, không phát hai tiếng nói cùng lúc ở mức gây khó nghe; nếu thất bại ghi FAIL |
| A08 | Gỡ quyền mic / bật tắt Wi‑Fi / Bluetooth audio route | Kể offline/fallback bình thường; không phát tiếng quá lớn |
| A09 | 60 phút ở DeX 1920×1080, nhiều cue | Không ANR/crash, không tăng bộ nhớ vô hạn; đo lại FPS/P95, PSS |

## 2. T-025 — Child Engagement QA

### In-app diagnostics
`ChildEngagementMetrics` giữ **chỉ trong RAM** số lần bắt đầu các lượt Sâu, Ong, cả hai; kết thúc nội dung; POI ngắt; nội dung hủy; TTS chưa sẵn; số lần Parent/foreground pause. Mở **F10 → Parent controls** sẽ thấy thống kê này. Đây là phép đo **ứng dụng phát gì**, tuyệt đối *không phải* kết quả hai bé có vui, có theo dõi, có hiểu hay không. Không lưu thoại, hành vi nhận dạng hay tọa độ, không upload; QA không ghi âm trẻ.

### Quy trình quan sát có người lớn
Không nên tiến hành đo trong khi người quan sát đang lái xe. Ưu tiên kiểm tra DEMO tại nhà, hoặc một **người lớn ngồi ghế hành khách** quan sát khi trẻ ngồi đúng ghế an toàn. Không ép trẻ xem nếu say xe, mệt hoặc muốn nghỉ.

Thực hiện ít nhất:
1. 5–10 phút chế độ **Cho Sâu (5 tuổi)**: đủ khó nhưng lời ngắn, không nhắc Ong.
2. 5–10 phút chế độ **Cho Ong (4 tuổi)**: dễ hiểu, không nhắc Sâu.
3. 15–20 phút **Cả Sâu và Ong (4–5 tuổi)** không chạm màn hình, trước tiên DEMO không internet, sau đó GPS thật khi có người quan sát riêng. Ghi các mốc 0, 5, 10, 15, 20 phút.
4. Lặp lại ở ít nhất một buổi khác để giảm thiên lệch do tâm trạng; có thể kết thúc bất kỳ lúc nào nếu trẻ không thoải mái.

**Bảng điền thủ công cho mỗi buổi (không lưu trong app):**

| Mốc | Bé | Chú ý nhìn nội dung? (0–3) | Phản hồi vẫy tay / nói / cười? (có/không) | Hiểu câu? (có/không/khó biết) | Bỏ xem / mệt / khó chịu? | Nhận xét |
|---|---|---|---|---|---|---|
| 0–5 phút | Sâu 4 | … | … | … | … | … |
| 0–5 phút | Ong 3 | … | … | … | … | … |
| 5–10 phút | Sâu 4 | … | … | … | … | … |
| 5–10 phút | Ong 3 | … | … | … | … | … |
| 10–15 phút | Sâu 4 | … | … | … | … | … |
| 10–15 phút | Ong 3 | … | … | … | … | … |

0 = hoàn toàn không quan tâm, 1 = đôi lúc nhìn, 2 = chú ý phần lớn sự kiện, 3 = chủ động tương tác. Điểm *chỉ là quan sát chủ quan* của phụ huynh.

### Ngưỡng nghiệm thu đề xuất (chưa đạt/không đạt cho đến khi kiểm thật)
- Bắt buộc: cả ba chế độ đúng người xem, không sai tên/tuổi, không bật cloud/mic trái cài đặt; không tự phát câu định vị không nguồn.
- **Cả hai:** trong 15–20 phút, Sâu và Ong đều có ít nhất 2 lần phản hồi tự nhiên (nói, chỉ, cười, vẫy tay), không phải bắt buộc nhìn liên tục; ít nhất 1/2 các tình huống được người lớn đánh giá phù hợp từng bé.
- Từng bé: không có quá hai prompt khó/không hiểu liên tiếp, không buộc trả lời giọng nói để đi tiếp. Sâu cần đủ thử thách nhận biết/đếm; Ong cần hiểu lời ngắn.
- Có nghỉ nhẹ tự nhiên, không âm thanh quá lớn, không khiến hai bé tranh lượt hoặc khó chịu. Trường hợp say xe, mệt hoặc khó chịu: **dừng kiểm thử**, không cố đạt chỉ tiêu.
- Kỹ thuật: không ANR/crash, không nói chồng gây khó nghe, menu F10 dùng được với chuột, không cản chỉ dẫn navigation, FPS/P95/PSS không suy giảm rõ so với baseline 59.963 FPS T-021; phải đo mới chứ không lấy baseline làm kết quả T-024/T-025.

### Thiết lập trên máy dev
```bash
git pull origin main
./scripts/test-local.sh
./gradlew :android:assembleDebug
adb install -r android/build/outputs/apk/debug/android-debug.apk
```

`tools/test_t024_t025_audio_engagement.py` là **source contract**, JUnit `ChildEngagementMetricsTest` chạy bởi `:core:test`. Không có GitHub Actions. Ghi rõ tình huống PASS/FAIL, thời lượng, log lỗi không chứa giọng trẻ hoặc GPS, và thống kê ở Parent Menu sau các phiên QA. T-025 chỉ hoàn tất nghiệm thu khi nhận được quan sát thực tế của phụ huynh cho Sâu và Ong.
