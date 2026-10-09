package com.khuongnd.dexkids.story;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * Small deterministic Vietnamese child reply matcher. No LLM, recording,
 * microphone or transcript persistence. Never repeat user speech to UI/logs.
 */
public final class ChildAnswerInterpreter {
    private ChildAnswerInterpreter() {}
    public enum Kind { UNKNOWN, UNSURE, YES, NO, RELEVANT, OTHER }
    public record Response(Kind kind, String textVi) {}

    static String norm(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replace("đ", "d").replaceAll("\\p{M}+", "")
                .replaceAll("[^a-z0-9 ]+", " ").replaceAll("\\s+", " ").trim();
        return normalized.length() > 160 ? normalized.substring(0,160) : normalized;
    }
    private static boolean words(String text, String query) {
        return (" " + text + " ").contains(" " + query + " ");
    }
    private static final Set<String> SIGNAL_WORDS = Set.of(
            "gach","nuoc","tren","duoi","xuong","cao","thap","xanh","do","vang","bien","gio",
            "cay","da","buoi","di","mua","song","co","khong","mat",
            "la","trai","dai","tron","cung","tho","thuyen");

    public static Response quiz(String spoken, String knownAnswer) {
        String said = norm(spoken), answer = norm(knownAnswer);
        if (said.isEmpty()) return new Response(Kind.UNKNOWN,
                "Mình chưa nghe rõ câu trả lời. Đáp án tham khảo là: " + knownAnswer);
        if (words(said,"khong biet") || words(said,"chua biet") || words(said,"con chiu"))
            return new Response(Kind.UNSURE,
                    "Không sao đâu, mình cùng khám phá nhé! Đáp án là: " + knownAnswer);
        if (words(said,"khong") && !words(said,"biet") && !words(answer,"khong") &&
                (words(answer,"co") || words(answer,"duoc")))
            return new Response(Kind.NO,
                    "Con đã chọn không. Mình cùng xem nhé! Đáp án tham khảo: " + knownAnswer);
        if ((words(said,"co") || words(said,"dung") || words(said,"phai")) &&
                (words(answer,"co") || words(answer,"dung")))
            return new Response(Kind.YES, "Đúng rồi, con quan sát rất tốt! " + knownAnswer);
        for (String word : SIGNAL_WORDS) {
            if (word.length() > 2 && words(said,word) && words(answer,word))
                return new Response(Kind.RELEVANT, "Con nói đúng ý rồi! " + knownAnswer);
        }
        return new Response(Kind.OTHER,
                "Cảm ơn con đã trả lời! Mình cùng tìm hiểu nhé: " + knownAnswer);
    }

    public static Response chat(String spoken) {
        String said = norm(spoken);
        if (said.isEmpty()) return new Response(Kind.UNKNOWN,
                "Mình chưa nghe rõ. Không sao, con cứ ngắm cảnh nhé!");
        if (words(said,"khong biet") || words(said,"chua biet"))
            return new Response(Kind.UNSURE,
                "Không sao, chúng mình còn nhiều điều để khám phá!");
        if (words(said,"bien"))
            return new Response(Kind.RELEVANT, "Con nhắc đến biển! Biển có những con sóng và rất nhiều sinh vật.");
        if (words(said,"cay") || words(said,"hoa") || words(said,"rung"))
            return new Response(Kind.RELEVANT, "Con thích thiên nhiên nhỉ! Mình cùng bảo vệ cây và hoa nhé.");
        if (words(said,"xe") || words(said,"buyt"))
            return new Response(Kind.RELEVANT, "Con nhắc đến xe! Bạn xe buýt của mình cũng thích khám phá.");
        if (words(said,"do") || words(said,"xanh") || words(said,"vang"))
            return new Response(Kind.RELEVANT, "Màu sắc thật thú vị! Con thử tìm các màu quanh mình nhé.");
        return new Response(Kind.OTHER, "Mình đã nghe con rồi! Cảm ơn con đã trò chuyện cùng Capybara.");
    }
}
