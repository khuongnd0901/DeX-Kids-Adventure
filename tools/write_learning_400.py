#!/usr/bin/env python3
"""T-069: reproducible original parent learning pack; no network or AI API calls."""
from pathlib import Path
import csv

QUIZZES = {
"sea": """
Nước biển thường có vị mặn hay vị ngọt?|Nước biển thường có vị mặn.|5
Con thuyền di chuyển trên mặt nước hay trong lòng đất?|Thuyền di chuyển trên mặt nước.|5
Khi đi chơi biển, con nên ở cùng người lớn hay đi một mình?|Con nên ở cùng người lớn.|5
Sóng biển là nước chuyển động hay một khối đá đứng yên?|Sóng biển là nước chuyển động.|5
Khi nước biển rút ra xa bờ, phần bãi cát lộ ra nhiều hơn hay ít hơn?|Phần bãi cát thường lộ ra nhiều hơn.|6
Áo phao giúp người ở dưới nước nổi dễ hơn hay làm nước biến mất?|Áo phao giúp người mặc nổi dễ hơn.|5
Vỏ sò nằm trên bãi biển thường cứng hay mềm như bông?|Vỏ sò thường cứng.|5
Cát khô có thể chảy qua kẽ ngón tay hay giữ nguyên như một khối gỗ?|Cát khô có thể chảy qua kẽ ngón tay.|5
Một chiếc xô đầy cát nặng hơn hay nhẹ hơn chính chiếc xô rỗng đó?|Chiếc xô đầy cát nặng hơn.|5
Để giữ bãi biển sạch, mình bỏ rác vào thùng hay để trên cát?|Mình bỏ rác vào thùng.|5
Cá heo cần ngoi lên để lấy không khí hay có thể ở mãi dưới nước?|Cá heo cần ngoi lên để lấy không khí.|6
Cá voi thở bằng phổi hay bằng mang như cá chép?|Cá voi thở bằng phổi.|7
Mai cua ở bên ngoài cơ thể hay là một chiếc lá trên đầu?|Mai cua ở bên ngoài cơ thể.|5
Hải đăng dùng ánh sáng để giúp tàu nhận biết vị trí hay để trồng cây?|Hải đăng dùng ánh sáng giúp tàu nhận biết vị trí.|6
Mỏ neo giúp giữ thuyền tại chỗ hay là chiếc ghế cho người lái?|Mỏ neo giúp giữ thuyền tại chỗ.|6
Cánh buồm có thể đón gió để giúp thuyền đi hay chỉ dùng làm bánh xe?|Cánh buồm có thể đón gió giúp thuyền đi.|6
Nếu một chiếc thuyền có hai mái chèo và thêm một mái nữa, có mấy mái chèo?|Có ba mái chèo.|5
Có bốn vỏ sò trong giỏ, lấy ra một vỏ thì còn mấy vỏ sò?|Còn ba vỏ sò.|5
Có ba con thuyền nhỏ và hai con thuyền lớn, tất cả có mấy con thuyền?|Tất cả có năm con thuyền.|6
Một hòn đảo được nước bao quanh hay luôn có đường nối với đất liền?|Một hòn đảo được nước bao quanh.|6
Trên bản vẽ, đường bờ biển ngăn giữa phần đất và phần gì?|Đường bờ biển ngăn giữa phần đất và phần biển.|6
Khi gió mạnh, con nên nghe hướng dẫn của người lớn hay tự chạy ra biển?|Con nên nghe hướng dẫn của người lớn.|5
Trong trò vẽ bản đồ, màu xanh thường được dùng cho biển hay cho ngọn lửa?|Màu xanh thường được dùng cho biển.|5
Nếu mỗi giỏ có ba vỏ sò, hai giỏ có tất cả bao nhiêu vỏ sò?|Hai giỏ có sáu vỏ sò.|7
Điều gì giúp bảo vệ biển: giảm rác nhựa hay ném thêm túi nhựa xuống nước?|Giảm rác nhựa giúp bảo vệ biển.|6
""",
"city": """
Mình đến thư viện để đọc sách hay để sửa bánh xe?|Mình đến thư viện để đọc sách.|5
Nơi các bác sĩ chăm sóc người bệnh thường được gọi là gì?|Nơi đó được gọi là bệnh viện.|5
Một ngôi nhà có mái để che mưa hay để làm bánh xe?|Mái nhà giúp che mưa.|5
Mình nhìn giờ bằng chiếc đồng hồ hay bằng cái bát?|Mình nhìn giờ bằng đồng hồ.|5
Cửa sổ thường giúp phòng nhận ánh sáng hay làm phòng biến thành hồ nước?|Cửa sổ giúp phòng nhận ánh sáng.|5
Muốn lên tầng trên trong nhà, mình có thể đi cầu thang hay đi vào tủ lạnh?|Mình có thể đi cầu thang.|5
Ở công viên, mình nên bỏ rác đúng chỗ hay rải khắp đường đi?|Mình nên bỏ rác đúng chỗ.|5
Trong lớp học, cây bút chì dùng để viết hay để uống nước?|Bút chì dùng để viết.|5
Cục tẩy giúp xóa nét bút chì hay làm giấy mọc thành cây?|Cục tẩy giúp xóa nét bút chì.|5
Một quyển sách thường gồm nhiều trang giấy hay nhiều bánh xe?|Một quyển sách thường có nhiều trang giấy.|5
Có hai quyển sách trên bàn, thêm ba quyển thì có tất cả mấy quyển?|Có tất cả năm quyển sách.|5
Bàn có bốn chân, nếu một chân bị gãy thì còn mấy chân nguyên vẹn?|Còn ba chân nguyên vẹn.|6
Trên tờ lịch, thứ Hai đến trước hay sau thứ Ba?|Thứ Hai đến trước thứ Ba.|5
Nếu hôm nay là thứ Ba thì ngày mai là thứ mấy?|Ngày mai là thứ Tư.|6
Một tuần thường có bao nhiêu ngày?|Một tuần có bảy ngày.|6
Kim ngắn của đồng hồ chỉ giờ hay là chiếc quạt làm mát?|Kim ngắn của đồng hồ chỉ giờ.|6
Trong cửa hàng, mình trả đồ về đúng chỗ hay giấu xuống gầm kệ?|Mình trả đồ về đúng chỗ.|5
Khi xếp hàng, mình chờ đến lượt hay chen lên trước mọi người?|Mình chờ đến lượt.|5
Một căn phòng hình vuông có mấy góc?|Một căn phòng hình vuông có bốn góc.|5
Để vẽ một đoạn thẳng dễ hơn, mình dùng thước kẻ hay chiếc khăn?|Mình dùng thước kẻ.|5
Có sáu bạn trong nhóm, hai bạn sang nhóm khác thì còn mấy bạn?|Còn bốn bạn.|6
Ba bàn, mỗi bàn có hai quyển vở, tất cả có bao nhiêu quyển vở?|Tất cả có sáu quyển vở.|7
Trong các số hai, năm và tám, số nào lớn nhất?|Số tám lớn nhất.|6
Nếu số nhà tăng theo thứ tự hai, bốn, sáu, số tiếp theo là mấy?|Số tiếp theo là tám.|7
Mượn sách của bạn xong, mình nên trả lại hay giữ luôn không hỏi?|Mình nên trả sách lại cho bạn.|5
""",
"road": """
Xe đạp thường có hai bánh hay tám bánh?|Xe đạp thường có hai bánh.|5
Bánh xe thường có dạng hình tròn hay hình tam giác?|Bánh xe thường có dạng hình tròn.|5
Người điều khiển xe buýt được gọi là tài xế hay thủ thư?|Người đó được gọi là tài xế.|5
Người đi trên xe nhưng không điều khiển xe được gọi là gì?|Người đó được gọi là hành khách.|6
Tàu hỏa thường chạy trên đường ray hay trên cành cây?|Tàu hỏa thường chạy trên đường ray.|5
Máy bay di chuyển chủ yếu trên không hay dưới lòng đất?|Máy bay di chuyển chủ yếu trên không.|5
Khi xe đang chạy, con nên ngồi đúng ghế hay đứng lên đi lại?|Con nên ngồi đúng ghế.|5
Kính trước xe giúp tài xế nhìn đường hay dùng để chứa hành lý?|Kính trước xe giúp tài xế nhìn đường.|5
Mũ bảo hiểm bảo vệ đầu hay là đồ đựng nước uống?|Mũ bảo hiểm giúp bảo vệ đầu.|5
Khi muốn sang đường, trẻ nhỏ nên đi cùng người lớn hay chạy một mình?|Trẻ nhỏ nên đi cùng người lớn.|5
Vỉa hè thường dành cho người đi bộ hay cho tàu ngầm?|Vỉa hè thường dành cho người đi bộ.|5
Một cây cầu có thể giúp đường đi vượt qua sông hay làm sông biến mất?|Cây cầu có thể giúp đường đi vượt qua sông.|5
Đường hầm là lối đi xuyên qua một phần núi hoặc dưới đất hay là đám mây?|Đường hầm là một lối đi xuyên qua núi hoặc dưới đất.|6
Xe cứu hỏa thường được dùng để hỗ trợ chữa cháy hay chở kem đi bán?|Xe cứu hỏa được dùng để hỗ trợ chữa cháy.|5
Xe chở hàng được dùng chủ yếu để chở đồ hay để làm tổ chim?|Xe chở hàng được dùng chủ yếu để chở đồ.|5
Cửa xe là bộ phận để ra vào khi an toàn hay là tay lái?|Cửa xe là bộ phận để ra vào khi an toàn.|5
Nếu có hai xe đạp, mỗi xe hai bánh, tất cả có mấy bánh?|Tất cả có bốn bánh.|5
Ba chiếc ô tô đồ chơi, mỗi chiếc bốn bánh, có tất cả bao nhiêu bánh?|Có tất cả mười hai bánh.|7
Xe đồ chơi đi từ ô số một đến ô số bốn, qua mấy khoảng giữa hai ô liền nhau?|Xe đi qua ba khoảng.|7
Có năm xe đồ chơi, cất đi hai xe thì còn mấy xe?|Còn ba xe đồ chơi.|5
Hai xe đỏ và ba xe xanh trong hộp, tất cả có mấy xe?|Tất cả có năm xe.|5
Trong dãy xe đỏ, xanh, đỏ, xanh, màu tiếp theo là màu gì?|Màu tiếp theo là màu đỏ.|6
Một chiếc xe đi chậm có cần ít thời gian hơn xe đi nhanh trên cùng quãng đường không?|Không, xe đi chậm cần nhiều thời gian hơn.|7
Trong dãy một, ba, năm chiếc xe đồ chơi, số tiếp theo theo quy luật là mấy?|Số tiếp theo là bảy.|6
Bến xe là nơi hành khách đón xe hay nơi trồng lúa?|Bến xe là nơi hành khách đón xe.|6
""",
"nature": """
Cây thường lấy nước từ đất qua rễ hay qua chiếc mũ?|Cây thường lấy nước từ đất qua rễ.|5
Lá cây thường mọc trên cành hay dưới đáy chiếc cốc?|Lá cây thường mọc trên cành.|5
Thân cây giúp nâng đỡ cành lá hay dùng để làm đôi cánh?|Thân cây giúp nâng đỡ cành lá.|5
Một hạt giống có thể nảy mầm thành cây hay thành chiếc ô tô thật?|Hạt giống có thể nảy mầm thành cây.|5
Cây cần ánh sáng và nước hay chỉ cần tiếng vỗ tay để sống?|Cây cần ánh sáng và nước.|5
Rừng thường có nhiều cây hay chỉ có những chiếc ghế?|Rừng thường có nhiều cây.|5
Dòng sông là nước chảy thành dòng hay một bức tường gạch?|Dòng sông là nước chảy thành dòng.|5
Hồ nước thường có nước được đất bao quanh hay có lửa bao quanh?|Hồ nước thường được đất bao quanh.|5
Núi thường cao hơn một mô đất nhỏ hay thấp như một hạt cát?|Núi thường cao hơn mô đất nhỏ.|5
Một hòn đá thường cứng hay mềm như khăn bông?|Một hòn đá thường cứng.|5
Đất ẩm có thêm nước so với đất khô hay có thêm bánh xe?|Đất ẩm có thêm nước so với đất khô.|5
Khi nước được làm lạnh đủ lâu trong ngăn đá, nó có thể thành gì?|Nước có thể đông thành nước đá.|6
Khi nước đá tan, nó trở thành nước lỏng hay cát khô?|Nước đá tan trở thành nước lỏng.|5
Nước sạch nhìn trong thường có hình dạng cố định như viên gạch không?|Không, nước lỏng đổi hình theo đồ đựng.|6
Đổ cùng một cốc nước sang bình khác có làm nước tự nhiều lên không?|Không, nếu không thêm hay làm đổ thì lượng nước không đổi.|7
Ánh nắng có thể làm vũng nước nhỏ khô dần hay biến nước thành đá ngay?|Ánh nắng có thể làm vũng nước khô dần.|6
Không khí có ở quanh mình dù khó nhìn thấy hay chỉ có trong tranh?|Không khí có ở quanh mình dù khó nhìn thấy.|6
Gió là không khí chuyển động hay một loại đồ chơi bằng gỗ?|Gió là không khí chuyển động.|6
Bóng của vật xuất hiện khi vật chắn ánh sáng hay khi vật phát ra tiếng hát?|Bóng xuất hiện khi vật chắn ánh sáng.|6
Bài đếm có ba cây cao và hai cây thấp, có tất cả mấy cây?|Có tất cả năm cây.|5
Có bảy chiếc lá, gió làm rơi hai chiếc thì còn mấy chiếc trên cành?|Còn năm chiếc lá trên cành.|6
Nếu một hàng có bốn cây, hai hàng như vậy có bao nhiêu cây?|Hai hàng có tám cây.|7
Muốn bảo vệ cây non, mình chăm sóc nhẹ nhàng hay kéo cây lên xem rễ?|Mình chăm sóc cây nhẹ nhàng.|5
Tiết kiệm giấy có thể giúp giảm lượng gỗ cần dùng hay làm tốn thêm giấy?|Tiết kiệm giấy có thể giúp giảm lượng gỗ cần dùng.|6
Lá khô rơi xuống đất có thể phân hủy dần hay luôn giữ nguyên mãi mãi?|Lá khô có thể phân hủy dần.|7
""",
"weather": """
Khi trời mưa, những giọt nước rơi từ mây hay từ quyển sách?|Những giọt mưa rơi từ mây.|5
Trời nắng thường sáng hơn hay tối như căn phòng tắt đèn?|Trời nắng thường sáng hơn.|5
Chiếc ô thường giúp che mưa hay giúp xe chạy nhanh hơn?|Chiếc ô thường giúp che mưa.|5
Gió có thể làm lá cây đung đưa hay làm cây biến thành đá ngay?|Gió có thể làm lá cây đung đưa.|5
Mây ở trên bầu trời hay nằm dưới đáy chiếc giày?|Mây ở trên bầu trời.|5
Cầu vồng thường có nhiều màu hay chỉ một màu đen?|Cầu vồng thường có nhiều màu.|5
Sương mù làm mình nhìn xa khó hơn hay rõ hơn?|Sương mù làm mình nhìn xa khó hơn.|6
Nhiệt kế dùng để đo nhiệt độ hay để đo độ dài cây bút?|Nhiệt kế dùng để đo nhiệt độ.|6
Nếu nhiệt độ tăng từ hai mươi lên hai mươi lăm độ thì trời ấm hơn hay lạnh hơn?|Nhiệt độ tăng nên ấm hơn.|6
Khi trời lạnh, áo ấm giúp giữ ấm hay làm tuyết rơi trong túi áo?|Áo ấm giúp giữ ấm.|5
Sau cơn mưa, mặt đường có thể ướt hay chắc chắn khô ngay lập tức?|Mặt đường có thể còn ướt.|5
Một vũng nước ngoài nắng thường khô nhanh hơn hay chậm hơn khi trời mát không gió?|Vũng nước thường khô nhanh hơn ngoài nắng.|7
Tiếng sấm và tia chớp có liên quan đến giông hay liên quan đến sách tô màu?|Sấm và chớp có liên quan đến giông.|6
Khi có giông, con nên nghe người lớn hướng dẫn hay tự chạy ra chỗ trống?|Con nên nghe hướng dẫn của người lớn.|5
Có nên nhìn thẳng vào mặt trời thật để quan sát màu của nó không?|Không, mình không nhìn thẳng vào mặt trời.|5
Bầu trời ban đêm thường dễ thấy sao hơn ban ngày hay ngược lại?|Ban đêm thường dễ thấy sao hơn.|5
Mặt trăng có thể xuất hiện cả ban ngày không?|Có, đôi khi mình có thể thấy mặt trăng ban ngày.|6
Trái Đất quay làm nhiều nơi lần lượt có ngày và đêm hay làm sách tự mở?|Trái Đất quay tạo nên sự luân phiên ngày và đêm.|7
Bài đếm có ba đám mây bên trái và hai đám mây bên phải, có tất cả mấy đám mây?|Bài đếm đó có năm đám mây.|5
Bài đếm có sáu giọt mưa, thêm hai giọt thì có mấy giọt?|Có tám giọt mưa.|6
Một tuần ghi ba ngày mưa và bốn ngày không mưa, có tất cả mấy ngày?|Có tất cả bảy ngày.|6
Trong dãy nắng, mưa, nắng, mưa, hình tiếp theo là gì?|Hình tiếp theo là nắng.|5
Đồ ướt được phơi thông thoáng có thể khô dần hay luôn ướt mãi?|Đồ ướt có thể khô dần.|6
Trên hai nhiệt kế cùng loại, mức mười độ thấp hay cao hơn mức hai mươi độ?|Mức mười độ thấp hơn mức hai mươi độ.|6
Một chiếc chong chóng có thể quay khi có gió hay chỉ quay khi có mưa?|Chong chóng có thể quay khi có gió.|5
""",
"animal": """
Con mèo thường kêu meo meo hay ò ó o?|Con mèo thường kêu meo meo.|5
Con chó thường sủa gâu gâu hay hót líu lo?|Con chó thường sủa gâu gâu.|5
Con gà trống thường gáy hay kéo sợi tơ như nhện?|Con gà trống thường gáy.|5
Con bò thường ăn cỏ hay ăn đá?|Con bò thường ăn cỏ.|5
Con cá dùng mang để lấy ôxy trong nước hay dùng bánh xe?|Con cá dùng mang để lấy ôxy trong nước.|6
Chim dùng cánh để bay hay dùng đuôi như mái chèo để bay?|Chim dùng cánh để bay.|5
Con thỏ có tai dài hay có vòi dài như voi?|Con thỏ có tai dài.|5
Con voi có vòi hay có mai như rùa?|Con voi có vòi.|5
Con rùa có mai bảo vệ cơ thể hay có cánh bằng giấy?|Con rùa có mai bảo vệ cơ thể.|5
Vịt có bàn chân có màng giúp bơi hay bàn chân có bánh xe?|Vịt có bàn chân có màng giúp bơi.|6
Nhện thường có tám chân hay hai chân?|Nhện thường có tám chân.|6
Côn trùng trưởng thành như kiến thường có mấy chân?|Côn trùng trưởng thành như kiến có sáu chân.|7
Bướm trưởng thành có bốn cánh hay một chiếc cánh duy nhất?|Bướm trưởng thành có bốn cánh.|6
Ong có thể giúp chuyển phấn hoa giữa các bông hoa hay làm hoa thành đá?|Ong có thể giúp chuyển phấn hoa.|6
Nòng nọc lớn lên có thể trở thành ếch hay trở thành mèo?|Nòng nọc có thể lớn lên thành ếch.|6
Gà con thường nở từ trứng hay từ hạt đậu?|Gà con thường nở từ trứng.|5
Hươu cao cổ nổi bật với cổ dài hay với một chiếc vòi?|Hươu cao cổ nổi bật với cổ dài.|5
Chim cánh cụt bơi giỏi nhưng có bay trên trời như chim sẻ không?|Không, chim cánh cụt không bay như chim sẻ.|6
Dơi là động vật có vú hay là một loại cây?|Dơi là động vật có vú.|7
Hai con mèo, mỗi con bốn chân, có tất cả mấy chân?|Có tất cả tám chân.|6
Bài đếm có năm con vịt, thêm hai con thì có mấy con?|Có bảy con vịt.|5
Bốn quả trứng trong mỗi giỏ, hai giỏ có bao nhiêu quả trứng?|Hai giỏ có tám quả trứng.|7
Với động vật chưa quen, con nên hỏi người lớn trước hay tự kéo đuôi nó?|Con nên hỏi người lớn trước.|5
Khi chăm thú nuôi, mình cần cho nước phù hợp hay bỏ mặc nó?|Mình cần chăm sóc và cho nước phù hợp.|5
Động vật cần môi trường sống phù hợp hay chỉ cần một tấm ảnh của nơi ở?|Động vật cần môi trường sống phù hợp.|6
""",
"garden": """
Rau trong vườn thường cần được tưới nước vừa đủ hay ngập nước liên tục?|Rau thường cần được tưới nước vừa đủ.|5
Quả chuối chín thường có vỏ màu vàng hay màu xanh dương?|Quả chuối chín thường có vỏ màu vàng.|5
Quả chanh thường có vị chua hay vị mặn như muối?|Quả chanh thường có vị chua.|5
Cà rốt thường có phần củ nằm trong đất hay treo trên ngọn cây?|Củ cà rốt thường nằm trong đất.|5
Bông hoa có cánh hoa hay có bánh xe bằng sắt?|Bông hoa có cánh hoa.|5
Bình tưới dùng để tưới cây hay để cắt giấy?|Bình tưới dùng để tưới cây.|5
Một chiếc giỏ thường giúp đựng trái cây hay giúp cây bay lên trời?|Giỏ giúp đựng trái cây.|5
Quả nho thường mọc thành chùm hay mọc thành củ dưới đất?|Quả nho thường mọc thành chùm.|5
Trước khi ăn trái cây, mình nên nhờ người lớn rửa sạch hay lấy ngay đồ rơi bẩn?|Mình nên nhờ người lớn rửa sạch trái cây.|5
Quả dưa hấu thường có nhiều nước hay khô như giấy?|Quả dưa hấu thường có nhiều nước.|5
Trong quả xoài thường có một hạt lớn hay hàng trăm bánh xe?|Quả xoài thường có một hạt lớn.|5
Cây non lớn dần có cần thời gian hay có thể kéo cao lên để lớn ngay?|Cây non cần thời gian để lớn.|5
Nếu cây thiếu nước, lá có thể héo hay mọc thành cánh buồm ngay?|Lá cây có thể héo khi thiếu nước.|6
Muốn biết cây cần bao nhiêu nước, mình hỏi người chăm vườn hay cứ đổ hết nước vào?|Mình hỏi người chăm vườn để tưới phù hợp.|5
Có ba quả táo trong đĩa, thêm hai quả thì có tất cả mấy quả?|Có tất cả năm quả táo.|5
Có tám quả cam, chia đều cho hai rổ thì mỗi rổ mấy quả?|Mỗi rổ có bốn quả cam.|7
Sáu bông hoa, tặng bạn hai bông thì còn mấy bông?|Còn bốn bông hoa.|5
Hai hàng cà rốt, mỗi hàng năm cây, có tất cả mấy cây?|Có tất cả mười cây cà rốt.|7
Một quả dưa được chia thành bốn phần bằng nhau, mỗi phần là một phần mấy?|Mỗi phần là một phần tư quả dưa.|7
Trong các quả: chuối, chanh, chuối, chanh, quả tiếp theo theo quy luật là gì?|Quả tiếp theo là chuối.|5
Một chậu cây hình vuông có mấy cạnh?|Chậu cây hình vuông có bốn cạnh.|5
Giỏ có bốn quả đỏ và ba quả vàng, số quả màu nào nhiều hơn?|Quả màu đỏ nhiều hơn.|6
Nếu cây cao thêm hai xăng-ti-mét mỗi tuần, sau hai tuần cây cao thêm bao nhiêu?|Cây cao thêm bốn xăng-ti-mét.|7
Lá và hoa có giống nhau hoàn toàn về hình dạng trên mọi cây không?|Không, lá và hoa có nhiều hình dạng khác nhau.|6
Giữ lối đi trong vườn gọn giúp đi lại dễ hơn hay làm khó đi hơn?|Giữ lối đi gọn giúp đi lại dễ hơn.|5
""",
"safety": """
Khi ngồi trên xe, con nên cài dây an toàn hay dùng dây làm đồ chơi?|Con nên cài dây an toàn và ngồi đúng ghế.|5
Nếu làm rơi đồ khi xe đang chạy, con nhờ người lớn lúc phù hợp hay tự bò xuống sàn?|Con nhờ người lớn lúc phù hợp.|5
Khi người lái đang tập trung, mình nói nhẹ nhàng hay bất ngờ kéo tay họ?|Mình nói nhẹ nhàng, không kéo tay người lái.|5
Nếu cần giúp đỡ, con nói với người lớn đáng tin hay tự giấu mọi việc?|Con nói với người lớn đáng tin.|5
Khi chưa biết đồ vật có nóng không, con hỏi người lớn hay chạm thử ngay?|Con hỏi người lớn trước.|5
Ổ điện là chỗ cắm thiết bị phù hợp hay nơi để chọc bút chì vào?|Ổ điện chỉ dùng cho thiết bị phù hợp, không chọc đồ vật vào.|5
Thuốc là đồ dùng theo hướng dẫn của người lớn hay kẹo để tự lấy ăn?|Không tự lấy thuốc ăn; cần người lớn quản lý.|5
Khi một người lạ rủ đi nơi khác, con báo người lớn đi cùng hay tự đi theo?|Con báo người lớn đi cùng, không tự đi theo.|5
Thông tin riêng như địa chỉ nhà nên kể cho người lạ hay giữ riêng và hỏi bố mẹ?|Con giữ riêng thông tin và hỏi bố mẹ.|6
Để lối đi không vướng, mình cất đồ chơi gọn hay bày kín lối đi?|Mình cất đồ chơi gọn.|5
Khi sàn đang ướt, mình đi cẩn thận hay chạy thật nhanh?|Mình đi cẩn thận và nghe người lớn hướng dẫn.|5
Trước khi mượn kéo, con cần hỏi người lớn hay tự dùng bất kỳ lúc nào?|Con cần hỏi người lớn và dùng có hướng dẫn.|5
Khi dùng kéo trong hoạt động có người lớn hướng dẫn, mình hướng mũi kéo vào bạn hay tránh hướng vào người?|Mình tránh hướng mũi kéo vào người.|6
Đồ chơi nhỏ có nên cho vào miệng không?|Không, mình không cho đồ chơi nhỏ vào miệng.|5
Nếu bị lạc khi đi cùng gia đình, con tìm sự giúp đỡ của nhân viên nơi đó hay đi theo bất kỳ ai?|Con tìm nhân viên nơi đó để nhờ giúp đỡ.|6
Khi không hiểu một hướng dẫn, con hỏi lại hay đoán rồi làm việc nguy hiểm?|Con hỏi lại người lớn.|5
Thấy vật sắc nhọn trên sàn, con báo người lớn hay cầm lên chơi?|Con báo người lớn, không cầm chơi.|5
Khi ai đó nói không muốn bị chạm vào, mình tôn trọng hay tiếp tục chọc bạn?|Mình tôn trọng lời nói của bạn.|5
Muốn tham gia trò chơi của bạn, mình hỏi lịch sự hay giật đồ chơi?|Mình hỏi lịch sự.|5
Khi buồn hay lo, con có thể kể với người lớn đáng tin không?|Có, con có thể kể và nhờ giúp đỡ.|5
Nếu đồ chơi bị hỏng lộ cạnh sắc, mình đưa người lớn kiểm tra hay tiếp tục chơi ngay?|Mình nhờ người lớn kiểm tra.|6
Có hai hàng, mỗi hàng ba bạn đang chờ lượt, tất cả có mấy bạn?|Có tất cả sáu bạn.|6
Trong một hàng năm bạn, con đứng thứ ba thì có mấy bạn đứng trước con?|Có hai bạn đứng trước con.|6
Trên sơ đồ trò chơi có lối đi trống và lối có đồ chắn, mình chọn lối nào dễ đi hơn?|Mình chọn lối đi trống.|5
Khi muốn dừng một trò chơi vì không thoải mái, con có thể nói dừng lại và tìm người lớn không?|Có, con có thể nói dừng lại và tìm người lớn giúp.|5
"""
}

VOCAB = {
"sea": """beach|bãi biển
sea|biển
ocean|đại dương
wave|sóng
shell|vỏ sò
boat|thuyền
ship|tàu thủy
sail|cánh buồm
anchor|mỏ neo
lighthouse|hải đăng
harbor|bến cảng
coast|bờ biển
tide|thủy triều
coral|san hô
shark|cá mập
whale|cá voi
dolphin|cá heo
crab|cua
shrimp|tôm
octopus|bạch tuộc
squid|mực
seahorse|cá ngựa
starfish|sao biển
jellyfish|sứa
sea turtle|rùa biển""",
"city": """house|ngôi nhà
school|trường học
library|thư viện
hospital|bệnh viện
market|chợ
shop|cửa hàng
park|công viên
bridge|cây cầu
building|tòa nhà
door|cửa ra vào
window|cửa sổ
roof|mái nhà
wall|bức tường
stairs|cầu thang
room|căn phòng
bed|giường
table|cái bàn
chair|ghế
book|quyển sách
pencil|bút chì
eraser|cục tẩy
ruler|thước kẻ
bag|cái túi
clock|đồng hồ
bottle|cái chai""",
"road": """road|con đường
street|đường phố
path|lối đi
sidewalk|vỉa hè
crosswalk|vạch sang đường
traffic light|đèn giao thông
bus|xe buýt
car|ô tô
bicycle|xe đạp
motorbike|xe máy
truck|xe tải
train|tàu hỏa
wheel|bánh xe
tire|lốp xe
engine|động cơ
driver|tài xế
passenger|hành khách
ticket|vé
station|nhà ga
airport|sân bay
airplane|máy bay
helicopter|trực thăng
traffic|giao thông
horn|còi xe
tunnel|đường hầm""",
"nature": """tree|cây
leaf|chiếc lá
root|rễ cây
branch|cành cây
forest|khu rừng
mountain|núi
hill|đồi
river|sông
lake|hồ
stream|suối
rock|tảng đá
stone|hòn đá
sand|cát
soil|đất trồng
water|nước
air|không khí
wood|gỗ
grass|cỏ
field|cánh đồng
island|hòn đảo
waterfall|thác nước
valley|thung lũng
seed|hạt giống
bark|vỏ cây
pond|ao""",
"weather": """sun|mặt trời
moon|mặt trăng
star|ngôi sao
sky|bầu trời
cloud|đám mây
rain|mưa
wind|gió
snow|tuyết
fog|sương mù
rainbow|cầu vồng
thunder|sấm
lightning|tia chớp
storm|cơn bão
sunny|có nắng
rainy|có mưa
windy|có gió
cloudy|nhiều mây
hot|nóng
cold|lạnh
warm|ấm
cool|mát mẻ
spring|mùa xuân
summer|mùa hè
autumn|mùa thu
winter|mùa đông""",
"animal": """cat|con mèo
dog|con chó
cow|con bò
pig|con lợn
horse|con ngựa
sheep|con cừu
goat|con dê
duck|con vịt
chicken|con gà
rooster|gà trống
hen|gà mái
bird|con chim
butterfly|con bướm
bee|con ong
ant|con kiến
spider|con nhện
elephant|con voi
lion|sư tử
tiger|con hổ
monkey|con khỉ
bear|con gấu
rabbit|con thỏ
mouse|con chuột
fox|con cáo
panda|gấu trúc""",
"garden": """garden|khu vườn
flower|bông hoa
petal|cánh hoa
stem|thân cây nhỏ
fruit|trái cây
apple|quả táo
banana|quả chuối
orange|quả cam
mango|quả xoài
grape|quả nho
lemon|quả chanh vàng
pear|quả lê
peach|quả đào
pineapple|quả dứa
watermelon|quả dưa hấu
carrot|cà rốt
potato|khoai tây
tomato|cà chua
cucumber|dưa chuột
pumpkin|bí ngô
basket|cái giỏ
watering can|bình tưới
shovel|cái xẻng
rake|cái cào
fence|hàng rào""",
"safety": """helmet|mũ bảo hiểm
seat belt|dây an toàn
life jacket|áo phao
gloves|đôi găng tay
face mask|khẩu trang
soap|xà phòng
towel|khăn lau
hand|bàn tay
foot|bàn chân
head|cái đầu
eye|mắt
ear|tai
nose|mũi
mouth|miệng
tooth|răng
knee|đầu gối
elbow|khuỷu tay
help|giúp đỡ
stop|dừng lại
wait|chờ
slow|chậm
careful|cẩn thận
safe|an toàn
adult|người lớn
rule|quy tắc"""
}

def main():
    rows=[]
    for topic,block in QUIZZES.items():
        items=[x.split('|') for x in block.strip().splitlines()]
        assert len(items)==25,(topic,len(items))
        for i,(q,a,age) in enumerate(items,1):
            rows.append([f'quiz-57-{topic}-{i:03}', 'QUIZ',topic,int(age),7,q,a,''])
    for topic,block in VOCAB.items():
        items=[x.split('|') for x in block.strip().splitlines()]
        assert len(items)==25,(topic,len(items))
        for i,(english,meaning) in enumerate(items,1):
            age=7 if english in {'tide','coral','harbor','valley','anchor'} else 6 if english in {'passenger','engine','crosswalk','thunder','lightning','bark','rake','elbow','rule','stem'} else 5
            rows.append([f'word-57-{topic}-{i:03}','WORD',topic,age,7,
                         f'Con thử nói “{meaning}” bằng tiếng Anh nhé?',f'{english.capitalize()} nghĩa là {meaning}.',english])
    assert len(rows)==400
    assert len({r[0] for r in rows})==400
    assert len({r[5].casefold() for r in rows})==400
    assert len({r[7] for r in rows if r[1]=='WORD'})==200
    output=Path(__file__).resolve().parents[1]/'docs/learning-source/learning.5-7.400.tsv'
    with output.open('w',encoding='utf-8',newline='') as f:
        f.write('# dexkids-offline-learning-v1\n')
        w=csv.writer(f,delimiter='\t',lineterminator='\n',quoting=csv.QUOTE_NONE,escapechar=None)
        w.writerow(['id','kind','topic','min_age','max_age','prompt_vi','answer_vi','english'])
        w.writerows(rows)
    assert output.stat().st_size<=131072
    print(f'Wrote {len(rows)} cards: {output.name}, {output.stat().st_size} bytes')
if __name__=='__main__':main()
