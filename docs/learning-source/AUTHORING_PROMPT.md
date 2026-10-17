# Prompt tạo file câu hỏi / câu tiếng Anh

Thay phần INPUT rồi sao chép nguyên khối bên dưới vào công cụ AI bạn chọn. Đây là
prompt biên soạn nội dung, ứng dụng không tự gọi AI khi nhập file. Lưu đầu ra thành
`learning.tsv`, UTF-8; có thể nhập trên máy hoặc đưa lên URL HTTPS Raw.

```text
Bạn biên soạn học liệu hỏi đáp tiếng Việt và luyện nói từ/câu tiếng Anh ngắn cho
trẻ ngồi ghế an toàn trên ô tô. Hãy xuất một file TSV đúng schema dưới đây.

INPUT:
- Chủ đề mong muốn: động vật, thiên nhiên, đồ dùng, màu sắc, hình dạng, giao thông an toàn
- Độ tuổi: 5–7, bài dễ từ 5 tuổi, tăng dần độ khó tới 7 tuổi
- Số thẻ: 80, gồm 40 QUIZ + 40 WORD
- File cũ cần giữ (nếu có): [DÁN TOÀN BỘ TSV CŨ HOẶC ĐỂ TRỐNG]

YÊU CẦU NỘI DUNG:
1. Mỗi QUIZ có câu hỏi cụ thể, một đáp án rõ ràng, đúng kiến thức, diễn đạt ngắn gọn.
   Không câu đố mẹo, không câu có nhiều đáp án mơ hồ, không khen bé trả lời đúng khi
   chưa nghe bé trả lời. Đáp án nêu trực tiếp lời giải và tối đa một câu giải thích.
2. WORD cho bé thử nói một từ/cụm/câu tiếng Anh ngắn: câu hỏi tiếng Việt mời bé nói,
   answer_vi cho biết đúng từ/câu đó và nghĩa tiếng Việt, english chỉ chứa tiếng Anh.
   Ví dụ: prompt_vi="Con thử nói con cá bằng tiếng Anh được không?",
   answer_vi="Fish nghĩa là con cá.", english="fish".
   Có thể dùng câu đơn như "i see a cat"; không dấu câu, không viết tắt, tối đa 30 ký tự.
3. Không giả định bé thấy vật cụ thể trên màn hình hoặc ngoài cửa xe; hỏi bằng lời
   sao cho không cần tranh. Không khẳng định vị trí xe/địa danh/sự kiện chưa kiểm chứng.
4. Không yêu cầu đứng lên, tháo dây an toàn, đi lại, chạm tài xế hoặc rời ghế.
   Không hỏi tên thật đầy đủ, địa chỉ, trường học hay thông tin riêng tư; không URL.
5. Không lặp ý/câu hỏi chỉ bằng cách thay vài từ; đa dạng ít nhất 4 topic. Mọi dòng
   có prompt_vi khác nhau kể cả sau khi bỏ khác biệt viết hoa và khoảng trắng.
6. Thẻ dễ: min_age=5 max_age=7; thẻ khó hơn: min_age=6 max_age=7.
   Đảm bảo có đủ cả QUIZ và WORD dùng được cho tuổi 5. Dùng lời xưng "con"/"mình",
   không bắt buộc nhắc tên Sâu/Ong vì ứng dụng tự luân phiên người xem.
7. Nếu có file cũ: giữ nguyên tất cả dòng/ID cũ, bổ sung thẻ mới tới tổng số yêu cầu;
   không xóa/sửa dòng cũ, không trùng ID/nội dung; báo nếu đầu vào không hợp lệ hoặc
   số thẻ cũ đã vượt yêu cầu. Không tự bịa nguồn trích dẫn.

SCHEMA BẮT BUỘC:
- File UTF-8, không BOM. Dòng 1: # dexkids-offline-learning-v1
- Dòng 2 có 8 cột đúng tên, phân cách bằng TAB thật:
id<TAB>kind<TAB>topic<TAB>min_age<TAB>max_age<TAB>prompt_vi<TAB>answer_vi<TAB>english
- Sau đó mỗi thẻ là một dòng, đúng 8 cột, dùng TAB thật; không chuỗi ký tự "\\t",
  không ký hiệu <TAB>, không dấu | hoặc CSV. Không xuống dòng/TAB trong nội dung.
- id: 4–61 ký tự, bắt đầu chữ thường a–z, còn lại chữ thường/số/gạch ngang;
  dùng quiz-... cho QUIZ, word-... cho WORD; ID duy nhất.
- kind chỉ QUIZ hoặc WORD.
- topic chỉ sea, city, road, nature, weather, animal, garden, safety.
- 4 <= min_age <= max_age <= 7, số nguyên.
- prompt_vi dài 10–230 ký tự; answer_vi dài 6–230 ký tự, đầy đủ dấu tiếng Việt.
- QUIZ: english rỗng, giữ TAB cuối dòng để đủ 8 cột.
- WORD: english dài 2–30 ký tự, chỉ a–z chữ thường, dấu cách hoặc gạch ngang;
  bắt đầu bằng chữ cái; nghĩa tiếng Việt phải khớp chính xác.
- Tối đa 500 thẻ và 131072 byte toàn file.

Trước khi xuất, tự kiểm tra từng đáp án, bản dịch, độ tuổi, trùng lặp và đủ 8 cột.
Sửa mọi lỗi tìm được. Chỉ trả về nội dung file TSV, không code fence, lời dẫn hay
lời kết. Nếu yêu cầu đầu vào mâu thuẫn schema, báo ngắn gọn thay vì xuất file sai.
```

Mẫu để đối chiếu: [learning.example.tsv](learning.example.tsv). Sau khi tạo file,
đọc lại đáp án/bản dịch trước khi nhập. Bộ kiểm tra trong app xác nhận cấu trúc,
giới hạn và câu trùng; không thể bảo đảm mọi kiến thức AI viết đều đúng.
