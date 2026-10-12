# T-046 — Loại câu lỗi, sinh bổ sung và merge gói AI

Owner: Main. Dependencies: T-034, T-044. Ngày: 2026-10-12.

Trạng thái: **phương án thiết kế được người dùng chọn để ghi nhận; chưa triển khai hoặc nghiệm thu runtime**. Task hiện tại chỉ cập nhật Markdown, không thay đổi code, CI, credential hoặc gọi thêm API.

## Mục tiêu và khác biệt với code hiện tại

Thay vì sửa hoặc sinh lại cùng một câu lỗi, loại câu đó và tận dụng các câu tốt đã được kiểm duyệt. Gói hoàn chỉnh cần **16–20 câu** (đúng yêu cầu số câu >15), phù hợp trẻ 4–7 tuổi, đúng Sâu/Ong/cả hai, đa dạng và không trùng.

T-034 hiện review toàn bộ gói theo cơ chế fail-closed: một câu không đạt khiến cả gói bị từ chối. Phương án này đề xuất kết quả review theo từng câu và vùng gom nội dung tạm; **chưa thay đổi hành vi hiện tại**. Khi triển khai, cập nhật schema, validator, coordinator và regression tương ứng; các yêu cầu an toàn, consent, limiter và giữ last-good vẫn giữ nguyên.

## Luồng đề xuất

1. Sinh batch đầu gồm **20 câu**, mỗi câu có ID ổn định trong batch.
2. Kiểm tra cấu trúc và các quy tắc deterministic; loại câu lỗi đã nhận diện. Gửi **một request review toàn batch ứng viên** còn lại, không review từng câu bằng request riêng. Batch ứng viên có thể dưới15 câu sau lọc; schema review phải hỗ trợ trường hợp này khi triển khai.
3. Reviewer trả verdict cho từng ID: `ACCEPT` hoặc `REJECT`, kèm mã lý do. Chỉ giữ câu vừa đạt kiểm tra local vừa có verdict `ACCEPT` rõ ràng. Câu thiếu verdict, trùng ID hoặc verdict không hợp lệ không được mặc định đạt.
4. Gom các câu đạt vào vùng tạm cho chuyến đi và đúng audience/age pair. Chống trùng với các câu đã gom và lịch sử gần đây; giữ nguyên prompt, đáp án, từ vựng và metadata đã được review.
5. Nếu chọn được **16–20 câu** đạt đủ schema, topic/genre diversity và phân bổ audience hiện hành, tạo gói hoàn chỉnh rồi swap cache bằng cơ chế AtomicFile hiện có.
6. Nếu thiếu số lượng hoặc độ đa dạng, sinh batch mới15–20 câu, ưu tiên chủ đề/genre/audience còn thiếu. Prompt bổ sung dùng chủ đề và dấu vết chống trùng, không chứa GPS thật, audio, định danh hay dữ liệu cá nhân của trẻ. Không yêu cầu sửa lại câu đã bị loại.
7. Review toàn batch bổ sung bằng **một request**, chỉ merge các câu đạt. Nếu có dư, chọn tối đa20 câu theo các tiêu chí ở bước5; đủ số lượng nhưng không đủ đa dạng vẫn chưa được swap.
8. Hết ngân sách thử hoặc provider từ chối thì giữ gói cũ hoặc dùng fallback. Không chờ đủ nội dung mới để tiếp tục hoạt hình/audio.

## Quy tắc review và merge

- Mỗi batch được review có **một generation → một whole-batch review**. Generation hỏng JSON hoặc batch không còn ứng viên hợp lệ bị loại local; không cần gọi review cho dữ liệu không thể sử dụng. Không bỏ review để tiết kiệm quota.
- Mỗi câu được dùng phải có provenance về batch, model và verdict đã đạt; không lưu key/header. Nội dung đã review là bất biến khi merge. Nếu sửa nội dung thì phải review lại trong batch mới.
- Merge chỉ ghép các câu đã đạt; kiểm tra local toàn gói về schema, tuổi, audience, trùng lặp, topic/genre và giới hạn độ dài. Không cần thêm review riêng cho gói ghép nếu nội dung từng câu không thay đổi và các điều kiện toàn gói đều đạt.
- Response review malformed, thiếu liên kết ID, refusal hoặc lỗi toàn batch phải fail-closed; không suy diễn phần đạt. Chỉ salvage từng câu khi response có cấu trúc hợp lệ và verdict riêng rõ ràng.
- Các câu đạt riêng lẻ nằm trong vùng tạm, **không phải last-good pack và không vào gameplay** cho đến khi gói hoàn chỉnh đạt16–20 câu cùng tất cả acceptance gates.
- Các bộ lọc lỗi thực tế T-044 (kính mắt/kính lúp, câu đố táo mơ hồ) tiếp tục áp dụng. Salvage cải thiện tỷ lệ hoàn thành gói, không đảm bảo reviewer luôn nhận ra câu sai.

## Ngân sách, lifecycle và fallback

- Generation và review đều qua limiter chung hiện hành:10 requests/rolling60s, cách nhau ít nhất6100ms; không tăng quota, không đổi sang model/provider trả phí.
- Dùng ngân sách chung **tối đa6 lần generation kể cả lần đầu**, tính cả retry và batch bổ sung, không mở một vòng6 lần riêng cho mỗi batch. Tối đa6 review tương ứng; không gọi vô hạn trong chuyến dài hoặc render loop.
- Giữ backoff hiện hành5/15/35/75/150s, tôn trọng Retry-After. Provider refusal và các điều kiện terminal dừng theo policy hiện hành.
- Last-good chỉ thay sau khi gói hoàn chỉnh hợp lệ. Lỗi mạng,429,5xx,timeout hoặc thiếu câu không xóa gói cũ.
- Pause/resume và Activity recreation không tạo generation trùng; vùng tạm cần ownership/token theo chuyến để loại kết quả stale. Đây là acceptance gate cần triển khai và kiểm thử, chưa có bằng chứng cho luồng mới.

## Acceptance gates khi triển khai

| Tình huống | Kết quả cần đạt |
|---|---|
|20 câu,4 câu lỗi|Loại4, dùng16 câu đạt;1generation+1review|
|20 câu,7 câu lỗi|Giữ13 trong vùng tạm; sinh/review batch mới; merge thành16–20 câu đạt|
|Batch bổ sung lỗi hoặc review từ chối|Không mất câu đạt tạm; last-good không thay; retry có giới hạn/fallback|
|Câu trùng hoặc sai audience/tuổi|Không tính vào số câu hợp lệ; thiếu thì tiếp tục bổ sung trong ngân sách|
|Đủ16 câu nhưng thiếu genre/topic/focus|Chưa swap; bổ sung phần thiếu hoặc fallback|
|Review malformed/thiếu verdict/ID trùng|Fail-closed, không đưa câu chưa review vào gameplay|
|Hết6 lần generation|Dừng refill cho yêu cầu này, giữ last-good/fallback|
|Pause/resume/recreation/stale callback|Không request trùng; nội dung chuyến cũ không merge vào chuyến mới|
|Gemini thật, gọi có giới hạn|Chứng minh generation→review→salvage/refill→cache→gameplay; phân biệt fake và real API|

Verification của task tài liệu: rà soát nội dung và `git diff --check`; không chạy build/unit/instrumentation vì không đổi code. Các acceptance gates ở bảng trên **chưa thực thi**. M2/T-044 vẫn chưa đạt nghiệm thu chất lượng nội dung.
