package com.khuongnd.dexkids.story;

import java.util.Objects;

/**
 * T-022/T-023: deterministic, offline-only passenger entertainment.
 * All episodes explicitly refer to a fictional cartoon world; never assert real
 * GPS landmarks, nearby animals or vehicles. Parent selection is not voice ID.
 */
public final class EntertainmentDirector {
    public enum Audience {
        SAU, ONG, BOTH;
        public static Audience fromId(String id) {
            if (id == null) return BOTH;
            try { return valueOf(id); } catch (IllegalArgumentException e) { return BOTH; }
        }
        public int age() { return this == SAU ? 4 : 3; }
        public String label() {
            return switch (this) {
                case SAU -> "Cho Sâu · 4 tuổi";
                case ONG -> "Cho Ong · 3 tuổi";
                case BOTH -> "Cả Sâu và Ong · 3–4 tuổi";
            };
        }
    }
    public enum Reaction { WAVE, SURPRISE }
    public record Beat(String id, Audience focus, String introduction, String resolution, Reaction reaction) {
        public Beat {
            Objects.requireNonNull(id);
            Objects.requireNonNull(focus);
            Objects.requireNonNull(introduction);
            Objects.requireNonNull(resolution);
            Objects.requireNonNull(reaction);
            if (id.isBlank() || introduction.isBlank() || resolution.isBlank() ||
                introduction.length() > 240 || resolution.length() > 240)
                throw new IllegalArgumentException("Invalid preschool entertainment beat");
        }
    }
    public static final long FIRST_BEAT_DELAY_MS = 8_000L;
    public static final long BETWEEN_BEATS_MS = 42_000L;
    public static final long POI_PRIORITY_DELAY_MS = 90_000L;

    private static final Beat[] SAU = {
        new Beat("sau-colors", Audience.SAU, "Sâu ơi, chiếc xe buýt màu vàng. Con thử tìm thêm một thứ màu vàng trong tranh nhé!", "Đúng rồi! Màu vàng sáng như nắng. Capybara vẫy tay chào Sâu.", Reaction.WAVE),
        new Beat("sau-count", Audience.SAU, "Sâu ơi, mình cùng đếm: một, hai, ba! Con thử giơ ba ngón tay nhé!", "Giỏi lắm! Một, hai, ba. Chúng mình vừa đếm đến ba.", Reaction.WAVE),
        new Beat("sau-rabbit", Audience.SAU, "Sâu ơi, bạn thỏ thích nhảy. Con đoán thỏ nhảy cao hay bơi dưới nước?", "Thỏ nhảy bằng đôi chân sau rất khỏe. Thỏ đang chào chúng mình!", Reaction.SURPRISE),
        new Beat("sau-truck", Audience.SAU, "Sâu ơi, xe tải to hay chiếc xe đạp to hơn nhỉ?", "Xe tải to hơn xe đạp nhiều. Cùng nhìn các xe trong thế giới hoạt hình nhé!", Reaction.WAVE),
        new Beat("sau-green", Audience.SAU, "Sâu ơi, cây lá thường có màu gì nhỉ? Con chỉ thử một chiếc lá trong tranh!", "Nhiều chiếc lá có màu xanh. Cây giúp chúng mình có bóng mát.", Reaction.WAVE),
        new Beat("sau-fox", Audience.SAU, "Sâu ơi, nếu gặp bạn cáo hoạt hình, con muốn chào bạn như thế nào?", "Capybara chào bạn cáo bằng một cái vẫy tay thật to!", Reaction.WAVE),
        new Beat("sau-shape", Audience.SAU, "Sâu ơi, bánh xe có dạng hình tròn hay hình vuông?", "Bánh xe hình tròn nên có thể lăn. Xe buýt lại tiếp tục chuyến đi!", Reaction.SURPRISE),
        new Beat("sau-bird", Audience.SAU, "Sâu ơi, con chim có thể bay bằng gì nhỉ?", "Chim bay nhờ đôi cánh. Mình cùng dang hai tay như đôi cánh nhé!", Reaction.WAVE),
        new Beat("sau-stop", Audience.SAU, "Sâu ơi, khi đèn đỏ thì xe cần dừng hay đi tiếp?", "Đèn đỏ thì dừng lại. Chúng mình luôn thắt dây an toàn khi ngồi xe nhé!", Reaction.WAVE),
        new Beat("sau-river", Audience.SAU, "Sâu ơi, cá thích bơi trong nước hay chạy trên đường?", "Cá bơi trong nước. Mình thử làm động tác cá bơi nào!", Reaction.SURPRISE),
        new Beat("sau-flower", Audience.SAU, "Sâu ơi, có bông hoa trong tranh. Con đoán hoa có những màu gì?", "Hoa có thể có nhiều màu khác nhau. Capybara thích ngắm hoa!", Reaction.WAVE),
        new Beat("sau-weather", Audience.SAU, "Sâu ơi, trời nắng thì mình thường thấy mặt trời hay ngôi sao?", "Ban ngày trời nắng, chúng mình thường thấy mặt trời. Vẫy chào nắng nào!", Reaction.WAVE),
        new Beat("sau-boat", Audience.SAU, "Trong tranh có một chiếc thuyền. Thuyền đi trên sông hay trên đường nhỉ?", "Thuyền đi trên mặt nước. Chúng mình ngắm dòng sông hoạt hình nào!", Reaction.WAVE),
        new Beat("sau-bridge", Audience.SAU, "Đây là cây cầu trong tranh. Theo con cầu giúp xe đi qua chỗ nào?", "Cây cầu giúp xe đi qua sông hoặc con đường khác, thật tiện lợi!", Reaction.WAVE),
        new Beat("sau-fruit", Audience.SAU, "Sâu ơi, con thử kể một trái cây có màu đỏ nhé!", "Quả dâu tây thường màu đỏ. Con còn thích quả nào nữa?", Reaction.SURPRISE),
        new Beat("sau-school", Audience.SAU, "Nếu thấy một ngôi trường trong tranh, con đoán các bạn đến trường để làm gì?", "Các bạn đến trường để học và chơi an toàn cùng nhau!", Reaction.WAVE),
        new Beat("sau-safe", Audience.SAU, "Khi ngồi trong ô tô, Sâu nên cài dây an toàn hay đứng dậy chạy?", "Chúng mình ngồi đúng ghế và thắt dây an toàn suốt chuyến đi nhé!", Reaction.WAVE),
        new Beat("sau-share", Audience.SAU, "Nếu bạn nhỏ muốn chơi cùng đồ chơi, Sâu có thể làm gì nhỉ?", "Mình có thể chia sẻ và thay phiên nhau chơi. Cảm ơn Sâu nhé!", Reaction.WAVE)
    };

    private static final Beat[] ONG = {
        new Beat("ong-hello", Audience.ONG, "Ong ơi, Capybara đang vẫy tay kìa. Mình vẫy tay chào bạn nhé!", "Chào Ong! Capybara vui quá, bạn ấy vẫy tay thêm một lần nữa.", Reaction.WAVE),
        new Beat("ong-beep", Audience.ONG, "Ong ơi, xe buýt kêu bíp bíp trong trò chơi. Con nói bíp bíp được không?", "Bíp bíp! Đây là tiếng xe hoạt hình. Chúng mình cùng cười nào!", Reaction.SURPRISE),
        new Beat("ong-red", Audience.ONG, "Ong ơi, mình cùng tìm màu đỏ nhé! Màu đỏ, đỏ, đỏ!", "Màu đỏ thật nổi bật. Capybara vẫy tay khen Ong nào!", Reaction.WAVE),
        new Beat("ong-cat", Audience.ONG, "Ong ơi, bạn mèo kêu meo meo. Con thử kêu meo meo nhé!", "Meo meo! Bạn mèo hoạt hình đang chào chúng mình!", Reaction.WAVE),
        new Beat("ong-rabbit", Audience.ONG, "Ong ơi, thỏ nhảy lóc cóc! Con thử nhún người thật nhẹ nhé!", "Lóc cóc, lóc cóc. Bạn thỏ vui vẻ đang nhảy trong tranh!", Reaction.SURPRISE),
        new Beat("ong-wave", Audience.ONG, "Ong ơi, một bàn tay nhỏ xinh. Con vẫy tay cùng Capybara nhé!", "Xin chào! Capybara thấy Ong vẫy tay rồi!", Reaction.WAVE),
        new Beat("ong-tree", Audience.ONG, "Ong ơi, đây là cây xanh! Con nói cây xanh nào!", "Cây xanh! Mình nhìn những tán lá đang đung đưa nhé!", Reaction.WAVE),
        new Beat("ong-round", Audience.ONG, "Ong ơi, bánh xe tròn tròn! Con làm vòng tròn bằng hai tay được không?", "Tròn tròn! Bánh xe đang quay trong hoạt hình!", Reaction.SURPRISE),
        new Beat("ong-panda", Audience.ONG, "Ong ơi, bạn gấu trúc có màu trắng và đen. Con nói gấu trúc nhé!", "Bạn gấu trúc đang mỉm cười. Chúng mình chào bạn nào!", Reaction.WAVE),
        new Beat("ong-one", Audience.ONG, "Ong ơi, mình đếm một nhé! Một ngón tay!", "Một! Giỏi lắm Ong. Capybara tặng con một cái vẫy tay!", Reaction.WAVE),
        new Beat("ong-cloud", Audience.ONG, "Ong ơi, mây trắng trôi trên bầu trời. Con nhìn lên mây nào!", "Mây trắng mềm mại như bông. Mình nhìn mây bay nhé!", Reaction.WAVE),
        new Beat("ong-smile", Audience.ONG, "Ong ơi, Capybara đang cười! Con cười thật tươi với bạn nhé!", "Hì hì! Nụ cười của Ong làm chuyến đi thật vui.", Reaction.SURPRISE),
        new Beat("ong-two", Audience.ONG, "Ong ơi, một, hai! Con thử giơ hai ngón tay nhé!", "Một, hai. Hai ngón tay xinh, mình cùng vỗ tay nào!", Reaction.WAVE),
        new Beat("ong-fish", Audience.ONG, "Trong tranh, bạn cá bơi bơi. Ong làm tay bơi cùng bạn nhé!", "Bơi bơi! Bạn cá nhỏ đang quẫy đuôi chào Ong!", Reaction.WAVE),
        new Beat("ong-yellow", Audience.ONG, "Ong ơi, xe buýt màu vàng. Con thử nói màu vàng nhé!", "Vàng! Xe buýt vàng đang lăn bánh trong hoạt hình.", Reaction.WAVE),
        new Beat("ong-sun", Audience.ONG, "Mặt trời tròn tròn trên trời. Ong chỉ mặt trời trong tranh nào!", "Mặt trời sáng. Mình nhìn hình vẽ chứ không nhìn thẳng mặt trời thật nhé!", Reaction.SURPRISE),
        new Beat("ong-fruit", Audience.ONG, "Ong ơi, quả chuối màu gì nhỉ? Mình nói chuối vàng nào!", "Chuối vàng! Capybara thích nghe Ong nói tên trái cây!", Reaction.WAVE),
        new Beat("ong-safe", Audience.ONG, "Ong ơi, mình ngồi ngoan và cài dây an toàn trong xe nhé!", "Tốt lắm! Ngồi đúng ghế giúp chúng mình đi chơi an toàn!", Reaction.WAVE)
    };

    private static final Beat[] BOTH = {
        new Beat("both-together", Audience.BOTH, "Sâu và Ong ơi, hai anh em cùng vẫy tay chào Capybara nhé!", "Capybara vẫy tay lại! Anh Sâu và em Ong cùng đi chơi thật vui.", Reaction.WAVE),
        new Beat("both-count", Audience.BOTH, "Sâu đếm một, Ong đếm hai. Hai anh em cùng thử nào!", "Một, hai! Chúng mình cùng vỗ tay chúc mừng nhé!", Reaction.WAVE),
        new Beat("both-bus", Audience.BOTH, "Sâu và Ong ơi, xe buýt của Capybara đang lăn bánh. Hai anh em nói bíp bíp nào!", "Bíp bíp! Xe buýt hoạt hình đi chậm rãi và vui vẻ.", Reaction.SURPRISE),
        new Beat("both-rabbit", Audience.BOTH, "Có bạn thỏ trong thế giới hoạt hình! Sâu chào bạn, Ong vẫy tay nhé!", "Bạn thỏ cảm ơn Sâu và Ong. Capybara cũng vẫy tay nào!", Reaction.WAVE),
        new Beat("both-colors", Audience.BOTH, "Anh Sâu thử tìm màu vàng, em Ong thử tìm màu xanh trong tranh nhé!", "Vàng và xanh! Cùng ngắm hai màu thật vui nào!", Reaction.WAVE),
        new Beat("both-animals", Audience.BOTH, "Sâu thích bạn cáo hay bạn mèo? Ong thử kêu meo meo nhé!", "Meo meo! Các bạn thú hoạt hình cùng đến chào hai anh em.", Reaction.SURPRISE),
        new Beat("both-wheels", Audience.BOTH, "Sâu đếm bánh xe, Ong nói tròn tròn. Mình cùng nhìn bánh xe buýt nhé!", "Bánh xe tròn tròn đang quay. Capybara thích chuyến đi này!", Reaction.WAVE),
        new Beat("both-bird", Audience.BOTH, "Sâu và Ong ơi, chúng mình cùng dang tay làm cánh chim nhé!", "Bay bay! Đây là trò giả làm chim của hai anh em.", Reaction.WAVE),
        new Beat("both-flower", Audience.BOTH, "Sâu tìm bông hoa, Ong vẫy tay chào bông hoa nào!", "Bông hoa trong tranh thật đẹp. Cảm ơn hai anh em nhé!", Reaction.WAVE),
        new Beat("both-smile", Audience.BOTH, "Anh Sâu cười thật tươi, em Ong cũng cười nào!", "Capybara cũng cười rồi! Cả ba bạn cùng vui nhé!", Reaction.SURPRISE),
        new Beat("both-panda", Audience.BOTH, "Có bạn gấu trúc hoạt hình! Sâu nói gấu trúc, Ong chào bạn nhé!", "Xin chào gấu trúc! Bạn ấy cảm ơn Sâu và Ong.", Reaction.WAVE),
        new Beat("both-goodbye", Audience.BOTH, "Sâu và Ong ơi, mình chào các bạn thú trước khi xe đi tiếp nào!", "Tạm biệt các bạn thú! Lát nữa thế giới hoạt hình lại có điều bất ngờ.", Reaction.WAVE),
        new Beat("both-fruit", Audience.BOTH, "Sâu thử nói quả táo, Ong thử nói quả chuối nhé! Hai anh em cùng chơi nào!", "Táo và chuối! Mỗi trái cây có màu sắc và hương vị khác nhau.", Reaction.WAVE),
        new Beat("both-bridge", Audience.BOTH, "Sâu thử tìm cây cầu, Ong thử tìm dòng sông trong bức tranh nhé!", "Cầu và sông! Cả hai đã khám phá thêm cảnh đẹp trong tranh.", Reaction.WAVE),
        new Beat("both-safe", Audience.BOTH, "Sâu và Ong ơi, hai anh em cùng kiểm tra dây an toàn khi ngồi xe nhé!", "Thật tốt! Chúng mình luôn ngồi ghế phù hợp và cài dây an toàn.", Reaction.WAVE),
        new Beat("both-kind", Audience.BOTH, "Nếu em Ong buồn, anh Sâu có thể hỏi em điều gì nhỉ?", "Anh có thể hỏi em có sao không. Hai anh em biết quan tâm nhau thật vui!", Reaction.SURPRISE),
        new Beat("both-turns", Audience.BOTH, "Sâu trả lời trước, Ong trả lời sau nhé. Hai anh em mình cùng thay phiên nào!", "Mỗi bạn đều có một lượt. Biết chờ đến lượt là một điều đáng quý!", Reaction.WAVE),
        new Beat("both-weather", Audience.BOTH, "Anh Sâu tìm đám mây, em Ong tìm mặt trời trong bức tranh nhé!", "Mây và mặt trời ở trên trời. Chuyến khám phá thật nhiều màu sắc!", Reaction.WAVE)
    };

    private static final Beat[] COMMON = {
        new Beat("common-shapes", Audience.BOTH, "Mình cùng nhìn bánh xe tròn tròn của xe buýt hoạt hình nhé!", "Tròn tròn! Bánh xe quay nhẹ nhàng. Capybara lại mỉm cười.", Reaction.SURPRISE),
        new Beat("common-friend", Audience.BOTH, "Một người bạn động vật trong tranh đang vẫy tay. Mình chào bạn nào!", "Xin chào bạn! Thế giới hoạt hình có rất nhiều người bạn dễ thương.", Reaction.WAVE),
        new Beat("common-tree", Audience.BOTH, "Mình thấy cây xanh trong tranh kìa. Cây đang đung đưa nhẹ nhé!", "Lá cây đung đưa. Cùng hít thở thật nhẹ nào!", Reaction.WAVE),
        new Beat("common-cloud", Audience.BOTH, "Mình thử nhìn mây trắng trên bầu trời hoạt hình nhé!", "Đám mây trắng như bông. Nó đang trôi thật chậm.", Reaction.WAVE),
        new Beat("common-count", Audience.BOTH, "Một, hai! Mình cùng đếm hai lần vỗ tay nhẹ nào!", "Một, hai! Capybara cũng rất thích chơi đếm số.", Reaction.WAVE),
        new Beat("common-flower", Audience.BOTH, "Một bông hoa hoạt hình đang nở. Mình ngắm những cánh hoa nhé!", "Bông hoa thật xinh. Chúng mình chỉ ngắm, không bẻ hoa nhé!", Reaction.SURPRISE),
        new Beat("common-smile", Audience.BOTH, "Mình cùng làm khuôn mặt vui vẻ giống Capybara nào!", "Hì hì! Chuyến phiêu lưu hoạt hình vui quá!", Reaction.WAVE),
        new Beat("common-mouse", Audience.BOTH, "Bạn chuột nhỏ trong chuyện cổ tích chạy tí tách. Mình thử nói tí tách nhé!", "Tí tách, tí tách! Bạn chuột hoạt hình đã đi chơi tiếp rồi.", Reaction.WAVE),
        new Beat("common-rain", Audience.BOTH, "Trong câu chuyện tưởng tượng, mưa rơi tí tách. Mình cùng gõ nhịp nhẹ nhé!", "Tí tách. Mưa trong câu chuyện đã ngớt, mây trắng lại trôi.", Reaction.SURPRISE),
        new Beat("common-waves", Audience.BOTH, "Mình cùng giả làm con cá bơi: tay trái, tay phải, bơi nào!", "Bơi bơi! Đó là trò chơi giả làm cá của Capybara.", Reaction.WAVE),
        new Beat("common-sleep", Audience.BOTH, "Bạn gấu bông trong câu chuyện buồn ngủ. Mình cùng nói chúc ngủ ngon nhé!", "Chúc ngủ ngon bạn gấu! Capybara nói thật khẽ nào.", Reaction.WAVE),
        new Beat("common-goodbye", Audience.BOTH, "Mình chào một bạn nhỏ hoạt hình trước khi xe đi tiếp nhé!", "Tạm biệt! Một người bạn mới sẽ xuất hiện ở lần chơi sau.", Reaction.WAVE)
    };

    private int position;
    public int position() { return position; }
    /** Restore sequence after rotation. Clamped to bounded session length. */
    public void restore(int value) { position = Math.max(0, Math.min(value, 10_000)); }
    public static int uniqueBeatCount() { return SAU.length + ONG.length + BOTH.length + COMMON.length; }

    public Beat next(Audience audience) {
        Objects.requireNonNull(audience);
        int step = position++;
        Beat[] pool;
        int round;
        if (audience == Audience.BOTH) {
            pool = switch (step % 3) {
                case 0 -> SAU;
                case 1 -> ONG;
                default -> BOTH;
            };
            round = step / 3;
        } else {
            pool = step % 2 == 0 ? (audience == Audience.SAU ? SAU : ONG) : COMMON;
            round = step / 2;
        }
        // Coprime stride distributes all 12 beats before any repeat, rotates on wrap.
        int index = Math.floorMod((round % pool.length) * 5 + (round / pool.length) * 7, pool.length);
        return pool[index];
    }
}
