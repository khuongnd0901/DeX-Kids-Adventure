package com.khuongnd.dexkids.story.adventure;

import com.khuongnd.dexkids.story.VisualCue;
import java.util.*;
import static com.khuongnd.dexkids.story.adventure.StoryEpisode.*;

/** Original fictional fallback stories, shipped with the app; never remote/AI approval shortcuts. */
public final class BuiltinInterludes {
    private BuiltinInterludes() {}
    private static final String[] TITLES = {"Hạt mầm kiên nhẫn", "Chiếc ô biết chia sẻ", "Cuốn sách của bạn thỏ"};
    private static final String[][] LINES = {
        {"Trong câu chuyện tưởng tượng, Capybara tìm thấy một hạt mầm nhỏ và mang tới khu vườn của các bạn.",
         "Bạn đặt hạt xuống đất mềm, phủ đất nhẹ lên rồi tưới một chút nước. Bạn mong sáng mai sẽ có cây thật cao.",
         "Sáng hôm sau, mặt đất vẫn im lìm. Capybara hơi buồn. Bạn bướm ghé lại bảo: Hạt mầm cần thời gian, mình kiên nhẫn nhé.",
         "Các bạn thay phiên chăm đất, tưới vừa đủ nước và ngắm khu vườn. Không ai kéo hạt lên để xem nó đã lớn chưa.",
         "Một hôm, chiếc lá bé xíu nhú lên. Capybara vui vẻ gọi các bạn tới ngắm. Hạt mầm cuối cùng đã thành cây non.",
         "Các bạn nhận ra rằng chăm sóc đều đặn và chờ đợi cũng là một cách giúp đỡ. Khu vườn có thêm một người bạn xanh."},
        {"Trong câu chuyện tưởng tượng, bạn vịt đem theo chiếc ô tới thăm Capybara. Hai bạn định ngồi đọc sách dưới gốc cây.",
         "Bỗng mưa tí tách rơi. Vịt mở ô che cho mình rồi thấy cuốn sách của Capybara còn nằm ở chỗ có thể bị ướt.",
         "Vịt nói: Mình cùng che cuốn sách trước nhé. Capybara ôm sách, còn vịt nghiêng ô để cả hai cùng được che mưa.",
         "Bạn thỏ từ mái hiên gọi: Ở đây có chỗ khô ráo! Hai bạn cùng mang sách đến mái hiên và cảm ơn thỏ.",
         "Dưới mái hiên, mỗi bạn kể một điều mình thích trong sách. Tiếng mưa nghe giống một bản nhạc nhỏ ngoài vườn.",
         "Khi trời tạnh, vịt gấp chiếc ô lại. Các bạn vui vì đã cùng giữ sách khô và tìm được chỗ đọc sách thật ấm áp."},
        {"Trong câu chuyện tưởng tượng, thỏ có cuốn sách mới. Bạn muốn xem hết các bức tranh nhưng cũng muốn kể cho Capybara nghe.",
         "Capybara ngồi cạnh và hỏi: Mình cùng xem được không? Thỏ đồng ý, rồi hai bạn cùng nhẹ nhàng mở trang đầu tiên.",
         "Thỏ lật nhanh quá nên Capybara chưa kịp nhìn rõ. Capybara nói: Mình xem chậm một chút nhé, có nhiều chi tiết đáng yêu lắm.",
         "Thỏ dừng lại. Hai bạn cùng tìm chiếc lá nhỏ trong bức tranh rồi thay phiên kể xem mình tưởng tượng điều gì sẽ xảy ra tiếp.",
         "Thỏ nghĩ chiếc lá sẽ bay tới hồ. Capybara nghĩ nó sẽ nằm cạnh một bông hoa. Hai bạn đều thích nghe ý tưởng của nhau.",
         "Đọc xong, thỏ đặt sách lên kệ. Bạn vui vì cùng xem và biết chờ nhau đã làm cuốn sách trở nên thú vị hơn."}
    };
    private static final String[][] PROMPTS = {
        {"Capybara trồng thứ gì trong đất?", "Hạt mầm có lớn ngay thành cây cao không?", "Các bạn đã chăm sóc và chờ hạt mầm thế nào?"},
        {"Vịt dùng đồ vật gì để che mưa?", "Các bạn muốn giữ cuốn sách khô hay ướt?", "Bạn nào mời các bạn đến mái hiên?"},
        {"Thỏ và Capybara cùng xem đồ vật gì?", "Capybara muốn xem sách nhanh hơn hay chậm lại?", "Hai bạn có thay phiên kể ý tưởng không?"}
    };
    private static final String[][] ANSWERS = {
        {"Hạt mầm", "Không", "Kiên nhẫn"}, {"Chiếc ô", "Khô", "Thỏ"}, {"Cuốn sách", "Chậm lại", "Có"}
    };
    public static StoryEpisode at(Audience audience, int index) {
        int n = Math.floorMod(index, TITLES.length);
        List<Scene> scenes = new ArrayList<>();
        Actor child = audience == Audience.ONG ? Actor.ONG : Actor.SAU;
        String name = child == Actor.ONG ? "Ong" : "Sâu";
        for (int i=0; i<2; i++) {
            List<Beat> beats = new ArrayList<>();
            for (int j=0; j<3; j++) beats.add(new Beat(Actor.CAPYBARA, LINES[n][i*3+j], Emotion.CALM,
                VisualCue.Action.WAVE, VisualCue.Prop.NONE));
            scenes.add(new Scene("narration-"+i, VisualCue.Theme.STORY, beats, null));
        }
        for (int i=0; i<3; i++) {
            Actor actor = audience == Audience.BOTH && i == 1 ? Actor.ONG : child;
            String who = actor == Actor.ONG ? "Ong" : name;
            Audience target = audience == Audience.BOTH ? Audience.values()[i] : audience;
            String answer = "Trong truyện, câu trả lời là: " + ANSWERS[n][i] + ".";
            var q = new Question(target, PROMPTS[n][i], List.of(ANSWERS[n][i]),
                "Mình nhớ lại câu chuyện vừa nghe nhé.", answer, answer, 5);
            scenes.add(new Scene("recall-"+i, VisualCue.Theme.STORY,
                List.of(new Beat(actor, who+" mỉm cười nhớ lại câu chuyện vừa nghe.", Emotion.HAPPY,
                    VisualCue.Action.WAVE, VisualCue.Prop.NONE)), q));
        }
        var e = new StoryEpisode("builtin-interlude-"+n+"-"+audience.name().toLowerCase(Locale.ROOT), 1, 1,
            TITLES[n], audience, 5, 4, "Kiên nhẫn, chia sẻ và lắng nghe bạn",
            "Truyện tưởng tượng ngắn để cùng lắng nghe và nhớ lại điều các bạn đã làm.", scenes);
        StoryEpisodeValidator.validate(e);
        return e;
    }
    public static StoryEpisode find(String id, Audience audience) {
        if (id == null || !id.startsWith("builtin-interlude-")) return null;
        for (int i=0; i<TITLES.length; i++) {
            var e = at(audience, i);
            if (e.id().equals(id)) return e;
        }
        return null;
    }
}
