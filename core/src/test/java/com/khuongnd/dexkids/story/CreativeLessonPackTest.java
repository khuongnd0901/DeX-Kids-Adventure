package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CreativeLessonPackTest {
    @Test void rejectsObservedGenericAppleRiddleButKeepsVocabularyAndDistinctiveClues() {
        String prompt="Đố vui: Quả gì có vỏ màu đỏ tươi, vị ngọt và nhiều hạt nhỏ?";
        var failure=assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            prompt,"Đó chính là quả táo thơm ngon được bày trên đĩa.","apple","quả táo","food",
            CreativeLessonPack.Genre.RIDDLE,EntertainmentDirector.Audience.ONG,7,false));
        assertEquals("LESSON_KNOWN_AMBIGUOUS_RIDDLE",failure.getMessage());
        assertDoesNotThrow(()->new CreativeLessonPack.Lesson(
            "Từ tiếng Anh cho quả táo màu đỏ trên bàn là gì?","Đó là từ apple, nghĩa là quả táo.",
            "apple","quả táo","food",CreativeLessonPack.Genre.ENGLISH,EntertainmentDirector.Audience.ONG,7,false));
        assertDoesNotThrow(()->new CreativeLessonPack.Lesson(
            "Quả gì có vỏ màu đỏ, vị ngọt và hạt nằm trong lõi hình ngôi sao?","Đó là quả táo.",
            "apple","quả táo","food",CreativeLessonPack.Genre.RIDDLE,EntertainmentDirector.Audience.ONG,7,false));
    }
    @Test void rejectsTwoEyeglassLensesAnsweredAsMagnifier() {
        var failure=assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            "Bạn dùng đồ vật gì có hai tròng kính để nhìn rõ hơn các chữ cái nhỏ?",
            "Bạn sử dụng chiếc kính lúp để đọc chữ nhỏ rõ hơn.","magnifying glass","kính lúp","objects",
            CreativeLessonPack.Genre.QUIZ,EntertainmentDirector.Audience.SAU,5,false));
        assertEquals("LESSON_KNOWN_ANSWER_MISMATCH",failure.getMessage());
        assertDoesNotThrow(()->new CreativeLessonPack.Lesson(
            "Đồ vật nào có hai tròng kính và được đeo trước mắt?",
            "Đó là chiếc kính mắt giúp nhìn rõ hơn.","glasses","kính mắt","objects",
            CreativeLessonPack.Genre.QUIZ,EntertainmentDirector.Audience.SAU,5,false));
        assertDoesNotThrow(()->new CreativeLessonPack.Lesson(
            "Con dùng đồ vật gì để phóng to chữ nhỏ trên giấy?",
            "Con dùng chiếc kính lúp để nhìn chữ rõ hơn.","magnifying glass","kính lúp","objects",
            CreativeLessonPack.Genre.QUIZ,EntertainmentDirector.Audience.SAU,5,false));
    }
    private CreativeLessonPack.Lesson vocabulary(String word,String meaning) {
        return new CreativeLessonPack.Lesson("Từ tiếng Anh cho vật này là gì?",
            "Chúng mình cùng học từ mới này.",word,meaning,"language",
            CreativeLessonPack.Genre.ENGLISH,EntertainmentDirector.Audience.SAU,7,false);
    }
    @Test void rejectsWrongWordMeaningsObservedInApprovedProviderOutput() {
        for(var pair:List.of(new String[]{"acorn","hạt dẻ"},new String[]{"glasses","chiếc kính lúp"},
                new String[]{"cat","con mèo con"},new String[]{"whale","con cá"},
                new String[]{"fish","con cá vàng"},new String[]{"chestnut","hạt sồi"})) {
            var failure=assertThrows(IllegalArgumentException.class,()->vocabulary(pair[0],pair[1]));
            assertEquals("LESSON_KNOWN_TRANSLATION_ERROR",failure.getMessage());
        }
    }
    @Test void correctVocabularyAndUnrelatedAlternativeMeaningsRemainAllowed() {
        for(var pair:List.of(new String[]{"acorn","hạt sồi"},new String[]{"glasses","kính mắt"},
                new String[]{"glasses","những chiếc cốc"},new String[]{"cat","con mèo"},
                new String[]{"kitten","mèo con"},new String[]{"whale","cá voi"},
                new String[]{"fish","con cá"},new String[]{"goldfish","con cá vàng"}))
            assertDoesNotThrow(()->vocabulary(pair[0],pair[1]));
    }
    @Test void newWordsAreNotRestrictedToAFiniteDictionary() {
        assertDoesNotThrow(()->vocabulary("sundial","đồng hồ mặt trời"));
        assertDoesNotThrow(()->vocabulary("telescope","kính thiên văn"));
    }
    @Test void shortSafetyWordsDoNotConfuseAccentedVietnamese() {
        for(String answer:List.of("Bạn thỏ dùng bơm để bơm bong bóng.","Bạn thỏ sung sướng ngắm bông hoa."))
            assertDoesNotThrow(()->new CreativeLessonPack.Lesson(
                "Trong câu chuyện tưởng tượng, bạn thỏ đang làm gì?",answer,
                "rabbit","con thỏ","animals",CreativeLessonPack.Genre.STORY,
                EntertainmentDirector.Audience.SAU,5,true));
        for(String answer:List.of("Bạn thỏ tìm một khẩu súng.","Ban tho su dung sung.","Bạn thỏ tìm một quả bom."))
            assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
                "Trong câu chuyện tưởng tượng, bạn thỏ đang làm gì?",answer,
                "rabbit","con thỏ","animals",CreativeLessonPack.Genre.STORY,
                EntertainmentDirector.Audience.SAU,5,true));
    }
    @Test void validationReportsSafeCodesForSpecificFailedGate() {
        var failure=assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            "Con thấy hình gì trong bức tranh?","Một bông hoa rất đẹp.",
            "flower","bông hoa","INVALID_TOPIC",CreativeLessonPack.Genre.QUIZ,
            EntertainmentDirector.Audience.SAU,5,false));
        assertEquals("LESSON_TOPIC_INVALID",failure.getMessage());
        assertEquals("LESSON_FICTION_LABEL_MISSING",assertThrows(IllegalArgumentException.class,
            ()->new CreativeLessonPack.Lesson("Bạn thỏ bay lên một đám mây.","Bạn thỏ vui vẻ ngắm khu rừng.",
                "rabbit","con thỏ","animals",CreativeLessonPack.Genre.STORY,
                EntertainmentDirector.Audience.SAU,5,true)).getMessage());
    }
    @Test void vocabularyCannotBeSilentlyDroppedToFitTheAnswerBanner() {
        var shortLesson=sample(16,EntertainmentDirector.Audience.SAU,5,4).get(0);
        assertTrue(shortLesson.resolution().contains("green nghĩa là màu xanh lá"));
        assertTrue(shortLesson.resolution().length()<=240);
        assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            "Con thấy hình gì trong bức tranh này?","Một bông hoa rất đẹp. ".repeat(8),
            "flower","bông hoa với những cánh hoa rất đẹp trong khu vườn xanh mát","nature",
            CreativeLessonPack.Genre.QUIZ,EntertainmentDirector.Audience.SAU,7,false));
    }
    @Test void parentScheduleOwnsMixedAgeAndSoloRouting() {
        var modes=EntertainmentDirector.Audience.values();
        for(int s=4;s<=7;s++)for(int o=4;o<=7;o++)for(var mode:modes)for(int i=0;i<20;i++) {
            var focus=CreativeLessonPack.focusAt(mode,i);
            assertEquals(mode==EntertainmentDirector.Audience.BOTH?modes[i%3]:mode,focus);
            assertEquals(focus==EntertainmentDirector.Audience.SAU?s:focus==EntertainmentDirector.Audience.ONG?o:Math.min(s,o),
                CreativeLessonPack.ageForFocus(focus,s,o));
        }
        assertThrows(IllegalArgumentException.class,()->CreativeLessonPack.focusAt(EntertainmentDirector.Audience.BOTH,-1));
        assertThrows(IllegalArgumentException.class,()->CreativeLessonPack.ageForFocus(EntertainmentDirector.Audience.SAU,3,4));
    }
    @Test void rejectsObservedRomanizedAndAbstractLowAgeOutput() {
        var data=new ArrayList<CreativeLessonPack.Lesson>();
        for(int i=0;i<16;i++)data.add(new CreativeLessonPack.Lesson(
            (i%4==1 ? "Trong chuyen tuong tuong so " : "Con thay la cay so ")+i+" co mau gi?","La cay co mau xanh trong tranh.",
            "leaf","la cay","topic"+(i%4),CreativeLessonPack.Genre.values()[i%4],
            EntertainmentDirector.Audience.SAU,6,i%4==1));
        assertThrows(IllegalArgumentException.class,()->CreativeLessonPack.validate(
            data,EntertainmentDirector.Audience.SAU,6,6,List.of()));
        assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            "Vì sao lá cây lại có màu xanh lục?","Lá cây có chất diệp lục giúp quang hợp.",
            "leaf","lá cây","nature",CreativeLessonPack.Genre.QUIZ,
            EntertainmentDirector.Audience.ONG,4,false));
        assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            "Con hay gui so dien thoai cua bo nhe?","Minh se goi dien thoai.",
            "phone","điện thoại","safety",CreativeLessonPack.Genre.QUIZ,
            EntertainmentDirector.Audience.SAU,5,false));
        assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            "Bạn thỏ bỗng bay lên trời cùng một chiếc lá.","Bạn thỏ gặp một đám mây vui vẻ.",
            "cloud","đám mây","nature",CreativeLessonPack.Genre.STORY,
            EntertainmentDirector.Audience.SAU,5,true));
    }
    private List<CreativeLessonPack.Lesson> sample(int length,
                EntertainmentDirector.Audience audience, int sauAge, int ongAge) {
        var list=new ArrayList<CreativeLessonPack.Lesson>();
        String[] subjects={"animals","nature","numbers","weather","kindness","science"};
        for(int i=0;i<length;i++) {
            var focus=audience==EntertainmentDirector.Audience.BOTH ?
                switch(i%3) {
                    case 0 -> EntertainmentDirector.Audience.SAU;
                    case 1 -> EntertainmentDirector.Audience.ONG;
                    default -> EntertainmentDirector.Audience.BOTH;
                } : audience;
            int age=focus==EntertainmentDirector.Audience.SAU ? sauAge :
                focus==EntertainmentDirector.Audience.ONG ? ongAge : Math.min(sauAge,ongAge);
            var genre=CreativeLessonPack.Genre.values()[i%4];
            var prompt=genre==CreativeLessonPack.Genre.STORY ?
                "Trong câu chuyện tưởng tượng thứ "+i+", bạn thỏ tìm một chiếc lá phát sáng." :
                "Trong bức tranh số "+i+", con thấy cây có lá màu gì?";
            list.add(new CreativeLessonPack.Lesson(prompt,
                "Cây có lá màu xanh trong bức tranh này.",
                "green","màu xanh lá",subjects[i%subjects.length],
                genre,focus,age,genre==CreativeLessonPack.Genre.STORY));
        }
        return list;
    }
    @Test void acceptsCreativePacksForAgesFourThroughSevenAndAllModes() {
        for(var mode:EntertainmentDirector.Audience.values())
            for(int age=4;age<=7;age++) {
                var pack=CreativeLessonPack.validate(sample(16,mode,age,age),
                    mode,age,age,List.of());
                assertEquals(16,pack.lessons().size());
            }
        assertEquals(20,CreativeLessonPack.validate(
            sample(20,EntertainmentDirector.Audience.BOTH,7,4),
            EntertainmentDirector.Audience.BOTH,7,4,List.of()).lessons().size());
    }
    @Test void rejectsInvalidCountAgeAndFocus() {
        for(int invalidAge:List.of(3,8,9,10,11))assertThrows(IllegalArgumentException.class,
            ()->new CreativeLessonPack.Lesson("Con thấy màu gì trong bức tranh?","Con thấy màu xanh lá cây.",
                "green","màu xanh lá","colors",CreativeLessonPack.Genre.QUIZ,
                EntertainmentDirector.Audience.SAU,invalidAge,false));
        var mode=EntertainmentDirector.Audience.BOTH;
        assertThrows(IllegalArgumentException.class,()->CreativeLessonPack.validate(
            sample(14,mode,5,4),mode,5,4,List.of()));
        assertThrows(IllegalArgumentException.class,()->CreativeLessonPack.validate(
            sample(21,mode,5,4),mode,5,4,List.of()));
        var wrong=sample(16,mode,5,4);
        var x=wrong.get(1);
        wrong.set(1,new CreativeLessonPack.Lesson(x.prompt(),x.answer(),
            x.englishWord(),x.meaningVi(),x.topic(),x.genre(),
            EntertainmentDirector.Audience.SAU,5,x.fictional()));
        assertThrows(IllegalArgumentException.class,()->CreativeLessonPack.validate(
            wrong,mode,5,4,List.of()));
    }
    @Test void rejectsRepeatedOrPreviouslyHeardContent() {
        var mode=EntertainmentDirector.Audience.SAU;
        var data=sample(16,mode,5,4);
        assertThrows(IllegalArgumentException.class,()->CreativeLessonPack.validate(
            data,mode,5,4,List.of(CreativeLessonPack.fingerprint(data.get(0).prompt()))));
        data.set(7,data.get(3));
        assertThrows(IllegalArgumentException.class,()->CreativeLessonPack.validate(
            data,mode,5,4,List.of()));
        assertEquals(CreativeLessonPack.fingerprint("Con thấy gì ?"),
            CreativeLessonPack.fingerprint("  CON thấy  GÌ ?  "));
    }
    @Test void rejectsUnsafeLocationPrivateDataAndMislabelledFiction() {
        assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            "Chúng ta đang ở tại nhà con đúng không?", "Đó là ngôi nhà của bé.",
            "home","ngôi nhà","nature",CreativeLessonPack.Genre.QUIZ,
            EntertainmentDirector.Audience.SAU,5,false));
        assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            "Con hãy gửi số điện thoại của bố nhé?", "Mình sẽ gọi điện thoại.",
            "phone","điện thoại","safety",CreativeLessonPack.Genre.QUIZ,
            EntertainmentDirector.Audience.SAU,5,false));
        assertThrows(IllegalArgumentException.class,()->new CreativeLessonPack.Lesson(
            "Trong câu chuyện bạn mèo bay giữa biển sao.", "Mèo mỉm cười chào bạn.",
            "star","ngôi sao","arts",CreativeLessonPack.Genre.STORY,
            EntertainmentDirector.Audience.SAU,5,false));
    }
    @Test void rejectsInsufficientTopicsAndGenreVariety() {
        var data=sample(16,EntertainmentDirector.Audience.SAU,5,4);
        for(int i=0;i<data.size();i++){
            var x=data.get(i);
            data.set(i,new CreativeLessonPack.Lesson(x.prompt(),x.answer(),
                x.englishWord(),x.meaningVi(),"animals",x.genre(),x.focus(),
                x.targetAge(),x.fictional()));
        }
        assertThrows(IllegalArgumentException.class,()->CreativeLessonPack.validate(
            data,EntertainmentDirector.Audience.SAU,5,4,List.of()));
    }
}
