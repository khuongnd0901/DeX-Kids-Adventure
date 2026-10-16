# Truyện Sâu/Ong từ nguồn online (T-062)

App tải một file JSON do phụ huynh biên soạn, lưu trên thiết bị rồi sử dụng cùng
bộ kể truyện tương tác hiện có. Sau khi cài bản app có tính năng này, thêm hoặc sửa
truyện trên nguồn không cần build/cài lại APK. Nguồn không sử dụng AI để chuyển đổi
hay duyệt truyện; phụ huynh biên tập nội dung trước khi đăng.

## Nguồn mẫu đã đăng trên repo app

Bộ mẫu trong nhánh `feat/T-063-story-actions-props` hiện có **30 truyện**: 10 chủ đề,
mỗi chủ đề có bản SAU, ONG và BOTH (10 truyện cho mỗi chế độ). Ba truyện ban đầu
giữ nguyên ID, revision và nội dung. Xem [danh sách chủ đề](STORY_LIBRARY.md).
Cần APK T-063 để đọc đủ bộ hoạt cảnh. Dùng link nhánh sau để thử bộ mới:

```text
https://raw.githubusercontent.com/khuongnd0901/DeX-Kids-Adventure/feat/T-063-story-actions-props/docs/story-source/stories.example.json
```

URL đã cấu hình trước đây theo `main` vẫn dùng phiên bản đã merge. Sau khi nhánh
được merge, URL ấy tự nhận bộ mới khi đồng bộ; phụ huynh cần cập nhật APK trước.
Dán URL nguồn vào **Thư viện truyện → Nguồn truyện online**, bật đồng bộ và lưu:

```text
https://raw.githubusercontent.com/khuongnd0901/DeX-Kids-Adventure/main/docs/story-source/stories.example.json?source=app
```

Bộ ba truyện T-062 trước đây đã được xác minh tải HTTPS bằng chính `StorySourceClient` và `StoryPackCodec` trên
JVM và simulator Android API35 chạy trên Windows: đọc đủ ba truyện, yêu cầu có
ETag tiếp theo trả304. Android đã phát SAU/ONG/BOTH đến câu hỏi và khôi phục
truyện/cursor sau force-stop khi mất mạng. Chưa xác minh trên DeX thật. Tham số `source=app` tránh cache404 của link
Raw đã được kiểm tra trước lúc file xuất bản; link vẫn theo `main` khi sửa truyện.

## Thiết lập

1. Sao chép `stories.example.json` trong thư mục này vào repo GitHub public, hoặc
   nơi lưu file HTTPS công khai. File mẫu gồm truyện SAU, ONG và BOTH tự biên soạn.
2. Trên GitHub mở file, chọn **Raw**, sao chép URL nội dung. Ví dụ minh họa:
   `https://raw.githubusercontent.com/<owner>/<repo>/main/stories.json`.
   Đây là mẫu URL, không phải một nguồn đã được triển khai. Không dùng trang
   `github.com/.../blob/...` vì trang đó trả về HTML.
3. Trong app: **Thư viện truyện → Nguồn truyện online**, nhập URL, bật
   **Tự cập nhật khi mở app**, lưu. Có thể bấm **Cập nhật truyện online ngay**.
4. Truyện hợp lệ hiện nhãn **NGUỒN PHỤ HUYNH** và phát được ngay. Muốn app tự chọn
   kể, bật tùy chọn tự động kể trong thư viện như trước. Đồng bộ không tự bật nó.

## Bộ hoạt cảnh mở rộng T-063

[Bộ khả năng v2](CAPABILITIES_V2.md) thêm nhảy, nhún nhảy, cúi chào, đọc sách,
tưới cây, chia sẻ, kính lúp cùng sách/bóng/quà/cây non và các đạo cụ khác.
Cài APK T-063 trước khi dùng [pack mẫu mở rộng](stories.expanded.example.json).
File `stories.example.json` trong nhánh này đã mở rộng và cần APK T-063.
Pack `stories.expanded.example.json` giữ ba truyện giới thiệu đủ bảy hành động mới.

## Tạo truyện theo chủ đề bằng prompt

Dùng [prompt soạn truyện](AUTHORING_PROMPT.md): thay chủ đề và chọn Sâu/Ong/cả hai.
Prompt có sẵn danh mục nhân vật, hành động, đạo cụ và quy tắc ghép cue với lời kể;
kết quả là JSON theo schema của app. Khi thêm vào bộ đang dùng, dán cả JSON hiện
có vào prompt để giữ nguyên truyện cũ. Ví dụ đầu vào: chủ đề “biết chia sẻ với bạn”,
đối tượng BOTH. Prompt này dùng để soạn file bên ngoài app, không bật thêm API.

## Soạn file

```json
{
  "schemaVersion": 1,
  "stories": []
}
```

Khung trên là bộ rỗng hợp lệ; sao chép các object trong `stories.example.json` vào danh sách `stories` để có truyện.
Mỗi truyện giữ `id` ổn định, duy nhất trong bộ. `revision` là số nguyên dương
1–100000; app nhận biết thay đổi bằng nội dung, không yêu cầu tăng số này khi sửa.
Đổi `id` nghĩa là thêm truyện khác. Xóa phần tử khỏi `stories` để ngừng chọn phát;
`stories: []` là bộ rỗng hợp lệ. Không thêm comment hay dấu phẩy cuối.

Mỗi truyện dùng schema `StoryEpisode` v1:

| Trường | Nội dung |
| --- | --- |
| `id`, `title` | ID dạng `[a-z][a-z0-9-]{1,63}`; tiêu đề 3–100 ký tự |
| `schemaVersion`, `revision` | `1` và số phiên bản dương |
| `audience` | `SAU`, `ONG`, `BOTH` |
| `sauAge`, `ongAge` | `5`, `4` theo bộ phát truyện hiện có |
| `educationalGoal`, `synopsis` | Mục tiêu 4–180; tóm tắt 8–320 ký tự |
| `scenes` | 4–8 cảnh, ID cảnh duy nhất |

Cảnh gồm `id`, `backdrop`, `beats` (1–6 lượt), `question` (object hoặc bỏ trống/null).
Lượt gồm `actor`, `utterance` (4–240 ký tự, tối đa 48 từ), `emotion`, `action`, `prop`.

| Thuộc tính | Giá trị hỗ trợ |
| --- | --- |
| `actor` | `SAU`, `ONG`, `CAPYBARA` |
| `emotion` | `HAPPY`, `CURIOUS`, `CALM`, `SURPRISED` |
| `backdrop` | `ANIMALS`, `SKY`, `NATURE`, `WATER`, `TRANSPORT`, `COUNTING`, `COLORS`, `LANGUAGE`, `STORY`, `GENERAL` |
| `action` | `WAVE`, `LOOK_UP`, `COUNT`, `PET`, `JUMP`, `DANCE`, `BOW`, `READ`, `WATER`, `SHARE`, `INSPECT` |
| `prop` | `NONE`, `BIRD`, `BUTTERFLY`, `CAT`, `RABBIT`, `CLOUD`, `FLOWER`, `TREE`, `BOAT`, `VEHICLE`, `KITE`, `BOOK`, `BALL`, `GIFT`, `WATERING_CAN`, `SEEDLING`, `APPLE`, `BASKET`, `STAR`, `LEAF`, `SHELL`, `UMBRELLA`, `MAGNIFYING_GLASS`, `FOX`, `PANDA`, `PENGUIN`, `DUCK` |

Các action mới cần đúng tổ hợp prop trong [bảng khả năng v2](CAPABILITIES_V2.md).

Có 2–4 câu hỏi mỗi truyện. Câu hỏi gồm `target` (SAU/ONG/BOTH), `prompt` kết thúc
bằng `?`, `acceptedConcepts` (1–4 cụm từ), `hint`, `feedback`, `silenceFallback`,
`maxWaitSeconds` (5–30). Với BOTH cần ít nhất một câu hỏi riêng cho Sâu, một câu
riêng cho Ong và một câu chung. Truyện SAU/ONG không có lời thoại của trẻ còn lại.

Lời mở đầu phải ghi rõ là truyện kể/tưởng tượng/ngày xưa. Hành động phù hợp lời:
“vẫy tay” dùng WAVE, “ngước nhìn” dùng LOOK_UP, “vuốt ve” dùng PET. COUNT chỉ hỗ
trợ prop NONE vì schema chưa biểu diễn số lượng hình. Không đặt URL, thông tin cá
nhân hay chỉ dẫn lái xe trong lời truyện. Validator hiện có áp dụng cho mọi nguồn.
Đây là kiểm tra quy tắc, không chứng nhận chất lượng nội dung.

Giới hạn một file: UTF-8 hợp lệ, 4 MiB, 100 truyện. Mỗi truyện chịu giới hạn codec
48000 ký tự và 65536 byte. Trường lạ, khóa trùng, ID trùng và schema mới bị từ chối.
File sai dù chỉ một truyện sẽ không thay bộ đang sử dụng.

Kiểm tra trên máy phát triển trước khi đăng:

```bash
./gradlew :core:checkStoryPack -PstoryPack=/duong/dan/stories.json
```

Tạo fixture tối thiểu: `./gradlew :core:writeStoryPackSample`. Lệnh ghi vào
`build/story-samples/stories.example.json`, không ghi đè bộ truyện đã biên soạn
trong `docs/story-source/stories.example.json`. Muốn thêm truyện, nối object vào
`stories`, giữ nguyên các truyện cũ rồi chạy kiểm tra pack ở trên.

## Cập nhật và offline

App kiểm tra khi trở lại foreground, tối đa một lần mỗi 5 phút. Cập nhật thủ công
bỏ qua khoảng chờ. Có ETag/Last-Modified; thời điểm thấy thay đổi còn phụ thuộc bộ
nhớ đệm của máy chủ nguồn. Mạng lỗi, timeout, HTTP lỗi hoặc JSON sai giữ bộ hợp lệ
đã lưu. Lần đầu chưa có bộ tải về thì dùng thư viện cục bộ hiện tại.

Nếu đang kể, tải về được lưu chờ; toàn bộ bộ truyện mới chỉ áp dụng khi lượt kể
kết thúc/dừng. Lượt mới dùng bản mới và bắt đầu lại nếu nội dung thay đổi; bản
không đổi giữ cursor/yêu thích. Truyện nguồn chỉ đọc trong app, có thể sao chép
thành bản nháp cục bộ; bản sao dùng quy trình duyệt truyện cục bộ hiện tại.

Đổi URL chọn bộ riêng của URL mới. Tắt nguồn loại truyện online khỏi lượt chọn
mới; lượt đang phát được tiếp tục. Giữ tối đa ba bộ nguồn gần nhất; bộ bị loại
khỏi cache cần tải lại khi chọn lại URL. Đổi quá nhiều nguồn khi đang phát có
thể cần thử lại sau lượt kể để giải phóng cache. Truyện cục bộ không bị đồng bộ xóa.
Không tải ảnh, mã lệnh hoặc động tác mới từ nguồn. Không gửi dữ liệu trẻ, vị trí,
âm thanh hay câu trả lời tới nguồn; yêu cầu tải chỉ là HTTPS GET và cache validators.
