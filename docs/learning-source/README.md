# Nguồn câu hỏi và tiếng Anh

Vào **Cài đặt → Câu hỏi và tiếng Anh · file / HTTPS**. Chọn file `.tsv`
UTF-8 trên máy hoặc lưu URL HTTPS trực tiếp tới file TSV (GitHub: liên kết Raw).
Ứng dụng tải khi vào foreground, cách nhau tối thiểu 5 phút; nút tải lại bỏ qua
khoảng này. Chỉ GET nội dung công khai, không gửi âm thanh/câu trả lời/vị trí.
Không cần AI hoặc key. Nguồn câu hỏi độc lập với nguồn truyện JSON.

File mới **thay toàn bộ** bộ câu hỏi/tiếng Anh cho hành trình mới. Hành trình đang
chạy giữ snapshot cũ. Tải lỗi/file lỗi/mất mạng giữ nguyên bản cuối đã lưu; nếu chưa
có bản lưu dùng bộ có sẵn. Chọn “Dùng lại bộ có sẵn” để quay lại bộ gốc. Nhập file
chuyển sang nguồn file và dừng tự tải HTTPS; lưu URL bật lại tự tải. Xóa một dòng
khỏi file rồi tải/nhập lại sẽ loại dòng đó khỏi các hành trình mới.

Mặc định chờ bé **3–5 giây sau khi câu hỏi đọc xong**, sau đáp án chuyển lượt trong **2–5 giây**.
Trong khoảng chuyển lượt có lời dẫn luân phiên: “Mình tiếp tục nha!”, “Câu tiếp theo
muốn hỏi là…”, “Vậy tiếp theo là…”; không cộng thêm một khoảng chờ riêng cho lời dẫn.
Tắt giọng: giữ câu hỏi 9 giây để đọc rồi chờ thêm 3–5 giây. TTS không trả callback:
watchdog 30 giây, sau đó vẫn có cửa sổ chờ trả lời. Lượt tiếng Anh: đọc lời mời tiếng Việt → đọc mẫu bằng giọng tiếng Anh offline →
chờ bé nhắc lại 3–5 giây → đọc nghĩa/đáp án tiếng Việt. Khoảng chờ tính từ lúc mẫu
tiếng Anh kết thúc; không phát mẫu tiếng Anh sau đáp án. Câu hỏi và từ vựng được trộn riêng, ưu tiên chủ đề nhưng dùng hết các thẻ phù hợp
trước khi lặp; lượt cả hai luân phiên Sâu/Ong/cả hai theo tuổi cài đặt. Bộ TSV hiện
hỗ trợ tuổi 4–7; từng thẻ có giới hạn tuổi riêng, không tự nâng bài 4–6 thành bài tuổi 7.

**Kể chuyện sau mỗi 5 câu hỏi** mặc định bật, có checkbox ở cùng màn hình. Ưu tiên
truyện đã được phép phát và phù hợp người xem trong thư viện. Thư viện trống dùng
3 truyện tưởng tượng gốc đi kèm ứng dụng, luân phiên; không dùng bản nháp chưa được
phụ huynh cho phép. Truyện kể xong quay về hỏi đáp sau khoảng chuyển lượt 2–5 giây. Câu hỏi trong truyện
cũng giới hạn chờ trong 3–5 giây; nhận diện đúng có thể tiếp tục sớm, nhận diện rỗng/sai
không rút ngắn khoảng chờ. Tùy chọn tự phát truyện trong thư viện vẫn có thể kể truyện ở đầu hành trình;
khi bật xen kẽ, những truyện tiếp theo tuân theo mốc 5 câu để không kể liên tục.
Không đổi đường âm thanh/nhận diện/điều hướng hiện có.

File mẫu: [learning.example.tsv](learning.example.tsv), 8 câu hỏi + 8 từ tiếng Anh.
Prompt tạo bộ lớn: [AUTHORING_PROMPT.md](AUTHORING_PROMPT.md).

Định dạng: hai dòng đầu giữ nguyên như file mẫu, sau đó 8 cột phân cách bằng ký tự
TAB thật: `id kind topic min_age max_age prompt_vi answer_vi english`.

- `id`: chữ thường đầu tiên, tiếp theo chữ thường/số/gạch ngang, tổng 4–61 ký tự;
  khuyên dùng tiền tố `quiz-` hoặc `word-`; không trùng ID hoặc nội dung câu hỏi.
- `kind`: `QUIZ` hoặc `WORD`.
- `topic`: `sea city road nature weather animal garden safety`.
- Tuổi: số nguyên `4 <= min_age <= max_age <= 7`.
- `prompt_vi`: 10–230 ký tự; `answer_vi`: 6–230 ký tự; một dòng, không TAB bên trong.
- `english`: QUIZ để trống nhưng vẫn có TAB cuối; WORD dùng 2–30 ký tự
  chữ thường a–z, dấu cách/gạch ngang; ví dụ `fish` hoặc `good morning`.
- Tối đa 500 thẻ và 131072 byte; tối thiểu 1 thẻ. Ít thẻ sẽ hết nhanh và phải lặp;
  khuyên 60–100 thẻ đa dạng. Toàn file phải hợp lệ trước khi kích hoạt.

Phụ huynh kiểm tra tính đúng và độ phù hợp của nội dung trước khi nhập. Kiểm tra
schema không thay thế kiểm duyệt kiến thức. File mẫu là nội dung gốc của dự án.

Phụ đề dùng một khung chữ chính, kể cả chế độ chỉ âm thanh. Không lặp lời nói
trong bảng trạng thái GPS hay dải chữ phía dưới; nhãn tiếng Anh chỉ hướng dẫn nghe
và nói theo, không hiển thị lại cùng từ ở một vị trí thứ hai.

Bộ đầy đủ: [learning.5-7.400.tsv](learning.5-7.400.tsv) gồm **200 QUIZ +200 WORD**
cho tuổi5–7,8chủ đề, có thẻ nâng cao cho6/7tuổi. Giữ nguyên bộ có sẵn4–6 làm
phương án dự phòng. Nguồn file/HTTPS bỏ qua việc tạo/khởi động bộ cập nhật từ AI
API; chỉ tải file phụ huynh đã cấu hình. Không cần API key.

URL chính thức cho bộ400thẻ trên main:
https://raw.githubusercontent.com/khuongnd0901/DeX-Kids-Adventure/main/docs/learning-source/learning.5-7.400.tsv
