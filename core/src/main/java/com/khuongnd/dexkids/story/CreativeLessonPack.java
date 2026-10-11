package com.khuongnd.dexkids.story;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.*;

/**
 * Open-ended AI lessons: no fixed question/word ID catalog and no lifetime content cap.
 * Strict shape, age, location/privacy, repeated-text and safety gates are deterministic.
 * These guards cannot prove a model-generated factual answer true: use age-appropriate
 * general knowledge or explicitly fictional stories, never live GPS claims.
 */
public final class CreativeLessonPack {
    public enum Genre { QUIZ, STORY, RIDDLE, ENGLISH }
    public record Lesson(String prompt, String answer, String englishWord,
                         String meaningVi, String topic, Genre genre,
                         EntertainmentDirector.Audience focus, int targetAge,
                         boolean fictional) {
        public Lesson {
            if (genre == null || focus == null || !topic.matches("[a-z][a-z0-9-]{2,31}") ||
                targetAge < 4 || targetAge > 10 ||
                !safe(prompt, 14, targetAge<=5 ? 135 : 190) ||
                !safe(answer, 8, targetAge<=5 ? 135 : 190) ||
                !safe(meaningVi, 2, 75) ||
                !englishWord.matches("[a-z][a-z -]{1,29}") ||
                (genre != Genre.STORY && !prompt.endsWith("?")) ||
                (genre == Genre.STORY && !fictional) ||
                // Low ages: short clear question, response and one simple English word.
                (targetAge <= 5 && (wordCount(prompt)>23 || wordCount(answer)>28)) ||
                unsafe(prompt+" "+answer+" "+meaningVi))
                throw new IllegalArgumentException("Unsafe or unsuitable creative lesson");
        }
    }
    private static final Set<String> BANNED = Set.of(
        "số điện thoại","địa chỉ nhà","tên đầy đủ","trường con học",
        "mật khẩu","họ tên","hãy gửi","gửi ảnh","liên hệ người lạ",
        "đang đi qua","vừa đi qua","chúng ta đang ở","đã đến nơi",
        "rẽ trái","rẽ phải","dừng xe ngay","xuống xe","mở cửa xe",
        "nhìn thẳng mặt trời","uống thuốc","tự ý dùng thuốc",
        "vũ khí","giết","tự tử","đánh nhau","bom","súng");
    private static boolean unsafe(String text) {
        String normalized=normalize(text);
        if (normalized.contains("http://") || normalized.contains("https://") ||
            normalized.contains("www.") || normalized.contains("@") ||
            normalized.matches(".*\\d{8,}.*")) return true;
        for (String term:BANNED) if(normalized.contains(term)) return true;
        return false;
    }
    private static int wordCount(String s) { return s.trim().split("\\s+").length; }
    private static boolean safe(String value, int min, int max) {
        return value!=null && value.length()>=min && value.length()<=max &&
            !value.contains("<") && !value.contains(">") &&
            value.codePoints().noneMatch(c->c<32 || c==127);
    }
    private final List<Lesson> lessons;
    private CreativeLessonPack(List<Lesson> value) { lessons=List.copyOf(value); }
    public List<Lesson> lessons() { return lessons; }
    public static String normalize(String value) {
        return Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT),Normalizer.Form.NFKC)
            .replaceAll("\\s+"," ");
    }
    public static String fingerprint(String question) {
        try {
            byte[] bytes=MessageDigest.getInstance("SHA-256")
                .digest(normalize(question).getBytes(StandardCharsets.UTF_8));
            StringBuilder out=new StringBuilder(bytes.length*2);
            for(byte v:bytes)out.append(String.format(Locale.ROOT,"%02x",v&255));
            return out.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    public static CreativeLessonPack validate(List<Lesson> items,
             EntertainmentDirector.Audience audience,int sauAge,int ongAge,
             Collection<String> recentFingerprints) {
        Objects.requireNonNull(items);Objects.requireNonNull(audience);
        Objects.requireNonNull(recentFingerprints);
        if(items.size()<15 || items.size()>20 || sauAge<4 || sauAge>10 || ongAge<4 || ongAge>10)
            throw new IllegalArgumentException("Pack size/age invalid");
        Set<String> exact=new HashSet<>();
        Set<String> topics=new HashSet<>();
        Set<Genre> genres=new HashSet<>();
        int fictionCount=0;
        for(int i=0;i<items.size();i++) {
            Lesson x=Objects.requireNonNull(items.get(i));
            var expected=audience==EntertainmentDirector.Audience.BOTH
                ? switch(i%3) {
                    case 0 -> EntertainmentDirector.Audience.SAU;
                    case 1 -> EntertainmentDirector.Audience.ONG;
                    default -> EntertainmentDirector.Audience.BOTH;
                  } : audience;
            int age=expected==EntertainmentDirector.Audience.SAU ? sauAge :
                expected==EntertainmentDirector.Audience.ONG ? ongAge : Math.min(sauAge,ongAge);
            String hash=fingerprint(x.prompt());
            if(x.focus()!=expected || x.targetAge()!=age || !exact.add(hash) ||
                recentFingerprints.contains(hash))
                throw new IllegalArgumentException("Age, focus or repeated question invalid");
            genres.add(x.genre());
            topics.add(x.topic());
            if(x.fictional())fictionCount++;
        }
        if (topics.size()<4 || genres.size()<3 || fictionCount<2)
            throw new IllegalArgumentException("Lesson creativity/diversity too low");
        return new CreativeLessonPack(items);
    }
}
