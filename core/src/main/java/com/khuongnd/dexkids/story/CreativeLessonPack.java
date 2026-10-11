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
            requireLesson(genre!=null && focus!=null,"LESSON_METADATA_INVALID");
            requireLesson(topic!=null && topic.matches("[a-z][a-z0-9-]{2,31}"),"LESSON_TOPIC_INVALID");
            requireLesson(targetAge>=4 && targetAge<=10,"LESSON_AGE_INVALID");
            requireLesson(safe(prompt,14,targetAge<=5?135:190),"LESSON_PROMPT_LENGTH_OR_TEXT_INVALID");
            requireLesson(safe(answer,8,targetAge<=5?135:190),"LESSON_ANSWER_LENGTH_OR_TEXT_INVALID");
            requireLesson(safe(meaningVi,2,75),"LESSON_MEANING_LENGTH_OR_TEXT_INVALID");
            requireLesson(englishWord!=null && englishWord.matches("[a-z][a-z -]{1,29}"),"LESSON_WORD_INVALID");
            requireLesson(!knownWrongTranslation(englishWord,meaningVi),"LESSON_KNOWN_TRANSLATION_ERROR");
            requireLesson((answer+" Từ tiếng Anh: "+englishWord+" nghĩa là "+meaningVi+".").length()<=240,
                "LESSON_RESOLUTION_TOO_LONG");
            requireLesson(genre==Genre.STORY || prompt.endsWith("?"),"LESSON_QUESTION_MARK_MISSING");
            requireLesson(genre!=Genre.STORY || (fictional && explicitFiction(prompt)),"LESSON_FICTION_LABEL_MISSING");
            requireLesson(targetAge>5 || (wordCount(prompt)<=23 && wordCount(answer)<=28),"LESSON_LOW_AGE_TOO_WORDY");
            requireLesson(targetAge>5 || !tooAbstract(prompt+" "+answer),"LESSON_LOW_AGE_TOO_ABSTRACT");
            requireLesson(!unsafe(prompt+" "+answer+" "+meaningVi),"LESSON_SAFETY_REJECTED");
        }
        public String resolution() {
            return answer+" Từ tiếng Anh: "+englishWord+" nghĩa là "+meaningVi+".";
        }
    }
    private static void requireLesson(boolean accepted,String code) {
        if(!accepted)throw new IllegalArgumentException(code);
    }
    // Observed incorrect pairs, not an allowed vocabulary catalog. New words,
    // synonyms and unrelated meanings still go through independent AI review.
    private static final Map<String,Set<String>> WRONG_MEANINGS=Map.of(
        "cat",Set.of("meo con"), "kitten",Set.of("meo"),
        "whale",Set.of("ca"), "fish",Set.of("ca voi","ca vang"),
        "acorn",Set.of("hat de"), "chestnut",Set.of("hat soi"),
        "glasses",Set.of("kinh lup"), "magnifying glass",Set.of("kinh mat"));
    private static boolean knownWrongTranslation(String word,String meaning) {
        String plain=unaccent(meaning).replaceFirst("^(?:con|cai|chiec|chu|bong) ","")
            .replaceAll("[.!?]+$","").trim();
        return WRONG_MEANINGS.getOrDefault(word,Set.of()).contains(plain);
    }
    private static final Set<String> BANNED = Set.of(
        "số điện thoại","địa chỉ nhà","tên đầy đủ","trường con học",
        "mật khẩu","họ tên","hãy gửi","gửi ảnh","liên hệ người lạ",
        "đang đi qua","vừa đi qua","chúng ta đang ở","đã đến nơi",
        "rẽ trái","rẽ phải","dừng xe ngay","xuống xe","mở cửa xe",
        "nhìn thẳng mặt trời","uống thuốc","tự ý dùng thuốc",
        "vũ khí","giết","tự tử","đánh nhau","bom","súng");
    private static boolean unsafe(String text) {
        String normalized=unaccent(text);
        if (normalized.contains("http://") || normalized.contains("https://") ||
            normalized.contains("www.") || normalized.contains("@") ||
            normalized.matches(".*\\d{8,}.*")) return true;
        // Preserve accents for short words: bơm is a pump, bom is a bomb.
        // Privacy/location phrases still accept unaccented spelling.
        String exact=normalize(text).replace("sung sướng","vui vẻ").replace("sung suong","vui ve");
        for(String term:BANNED) {
            if(term.contains(" ")) {
                if(wholePhrase(normalized,unaccent(term)))return true;
            } else if(wholePhrase(exact,term) || wholePhrase(exact,unaccent(term)))return true;
        }
        return false;
    }
    private static boolean wholePhrase(String text,String phrase) {
        return java.util.regex.Pattern.compile("(?<![\\p{L}\\p{N}])"+
            java.util.regex.Pattern.quote(phrase)+"(?![\\p{L}\\p{N}])").matcher(text).find();
    }
    private static String unaccent(String text) {
        return Normalizer.normalize(normalize(text),Normalizer.Form.NFD)
            .replaceAll("\\p{M}+","").replace('đ','d');
    }
    private static boolean tooAbstract(String text) {
        String plain=unaccent(text);
        for(String term:List.of("diệp lục","quang hợp","ngưng tụ","tốc độ ánh sáng","phân tử","nguyên tử"))
            if(plain.contains(unaccent(term)))return true;
        return false;
    }
    private static boolean explicitFiction(String text) {
        String plain=unaccent(text);
        return plain.contains("tuong tuong") || plain.contains("co tich") || plain.contains("hu cau");
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
    /** Parent-owned metadata; the provider authors prose, never child ages or turn routing. */
    public static EntertainmentDirector.Audience focusAt(EntertainmentDirector.Audience audience,int index) {
        Objects.requireNonNull(audience);
        if(index<0)throw new IllegalArgumentException("Negative lesson index");
        return audience==EntertainmentDirector.Audience.BOTH ? switch(index%3) {
            case 0 -> EntertainmentDirector.Audience.SAU;
            case 1 -> EntertainmentDirector.Audience.ONG;
            default -> EntertainmentDirector.Audience.BOTH;
        } : audience;
    }
    public static int ageForFocus(EntertainmentDirector.Audience focus,int sauAge,int ongAge) {
        Objects.requireNonNull(focus);
        if(sauAge<4 || sauAge>10 || ongAge<4 || ongAge>10)throw new IllegalArgumentException("Invalid parent age");
        return focus==EntertainmentDirector.Audience.SAU ? sauAge :
            focus==EntertainmentDirector.Audience.ONG ? ongAge : Math.min(sauAge,ongAge);
    }
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
        int accentedLessons=0;
        for(int i=0;i<items.size();i++) {
            Lesson x=Objects.requireNonNull(items.get(i));
            var expected=focusAt(audience,i);
            int age=ageForFocus(expected,sauAge,ongAge);
            String hash=fingerprint(x.prompt());
            if(x.focus()!=expected)throw new IllegalArgumentException("Pack focus invalid at item "+i);
            if(x.targetAge()!=age)throw new IllegalArgumentException("Pack age invalid at item "+i);
            if(!exact.add(hash) || recentFingerprints.contains(hash))
                throw new IllegalArgumentException("Repeated question at item "+i);
            genres.add(x.genre());
            topics.add(x.topic());
            if(x.genre()==Genre.STORY && x.fictional())fictionCount++;
            if(!normalize(x.prompt()+x.answer()).equals(unaccent(x.prompt()+x.answer())))accentedLessons++;
        }
        if (topics.size()<4 || genres.size()<3 || fictionCount<2)
            throw new IllegalArgumentException("Lesson creativity/diversity too low");
        if(accentedLessons*2<items.size())
            throw new IllegalArgumentException("Vietnamese diacritics missing from pack");
        return new CreativeLessonPack(items);
    }
}
