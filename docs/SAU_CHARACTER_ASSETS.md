> Cập nhật local 2026-10-11: bản mới dùng hai sheet minh họa trong suốt 1024×1536, mỗi nhân vật có bốn tư thế theo câu chuyện. Trang phục cũ giữ làm fallback. Xem [T023_STORY_COMPANIONS.md](T023_STORY_COMPANIONS.md) và bằng chứng T-011 mới; các ghi chú chưa build/đo bên dưới mô tả checkpoint cũ. Chưa push hoặc phát hành các ảnh mới.

# Nhân vật Sâu 4 tuổi — artwork tích hợp trong game

Ngày 2026-10-10. Bộ nhân vật hoạt hình **lấy cảm hứng từ ảnh Sâu do phụ huynh cung cấp**, không phải ảnh chụp thật. Trong repo GitHub công khai **chỉ có sprite minh họa độ phân giải thấp**, không chứa 5 ảnh chụp gốc hay ảnh portrait full-res.

## Tài nguyên có trong source

`art/assets-source/characters/sau-costumes.b64.part01..part05` là 5 phần của **một ảnh PNG transparent** gồm 4 trang phục. Chúng được đưa vào repo dưới dạng base64 để bảo toàn byte ảnh qua connector chỉ hỗ trợ tệp văn bản, **không phải API gọi ảnh ngoài mạng**.

`tools/build_sprites.py` ghép 5 phần, giải mã, kiểm tra SHA256, kích thước, alpha và 4 quadrant, rồi tự tạo **`assets/characters/sau-costumes.png` (224 × 336 px, RGBA khi load)** trong `generateKidsArt`, cùng bước với atlas hiện tại.

Thứ tự ô ảnh (mỗi ô 112 × 168 px):

| | Cột trái | Cột phải |
|---|---|---|
| Hàng trên | **0. Nhà thám hiểm** (áo xanh, ba lô) | **1. Lính cứu hỏa** (mũ đỏ, áo xanh phản quang) |
| Hàng dưới | **2. Phi công** (đồng phục xanh đậm) | **3. Công an** (đồng phục xanh lá) |

Ở phiên bản hiện tại, người lớn không cần chọn trang phục bằng tay. `KidsActivity.playEntertainment` tự đặt vai:
- Chim/mây: phi công
- Xe/đèn giao thông: công an
- Bạn thú/mèo/thỏ: cứu hỏa
- Thiên nhiên, màu sắc, đề tài khác: thám hiểm.

Các bộ đồng phục là **hóa trang hoạt hình để nhập vai**, không phải chứng nhận chức danh hay hành vi ngoài đời thực.

## Điều kiện hiển thị

- **Cho Sâu (4 tuổi):** luôn có sprite Sâu trong cảnh; thay trang phục theo sự kiện.
- **Cả Sâu và Ong (3–4 tuổi):** Hiện cả sprite Sâu và sprite Ong cùng Capybara; chế độ hội thoại vẫn tránh gọi nhầm hai bé.
- **Cho Ong (3 tuổi):** không hiện Sâu; sprite Ong độc lập dùng bốn trang phục theo cùng chủ đề. Xem [ADVENTURE_HUD_ONG.md](ADVENTURE_HUD_ONG.md).
- **Audio-only:** không render sprite (đúng lựa chọn tối giản đồ họa).
- Trên màn hình DeX FullHD sprite xuất hiện **bên phải xe buýt**, chuyển động lên xuống nhẹ, trong cùng `SpriteBatch`. Không tạo texture/atlas mỗi frame; không ảnh hưởng các điểm GPS/POI hay TTS.

## Build và kiểm thử

```bash
git pull origin main
python3 tools/test_sau_character_art.py
./scripts/test-local.sh
./gradlew :android:assembleDebug
adb install -r android/build/outputs/apk/debug/android-debug.apk
```

Sau khi `generateKidsArt` chạy, xác nhận file `assets/characters/sau-costumes.png` được tạo và có trong APK (Android sourceSets đã dùng `../assets`). Mở DEMO ở các chế độ Sâu, Ong, Cả hai; kiểm tra 4 bộ đồ sau các beat, không có nền đen, nhân vật không che Capybara/bus/HUD, không ANR/crash. Test 60 phút trên Fold3 DeX, đo FPS/P95/PSS mới. **Chưa có chứng cứ full Gradle APK hoặc vật lý Fold3 cho thay đổi artwork này.**

## Quyền riêng tư và thay thế ảnh

Repo có thể được xem công khai; chỉ phiên bản minh họa hoạt hình kích thước nhỏ được commit, không commit ảnh mặt nguyên bản, metadata ảnh, tên thật hay ảnh kích thước đầy đủ. Phụ huynh có thể thay `assets/characters/sau-costumes.png` bằng phiên bản chất lượng cao **ở bản build local riêng**, không bắt buộc push lên GitHub. Vì Gradle tái tạo sprite từ dữ liệu nguồn, khi build lại mặc định sẽ ghi lại sprite trên; muốn sử dụng HQ thường xuyên, chỉnh bước `build_sau_art()` sang đọc tệp local không track và thay thế sau đó trước bước merge Android assets.
