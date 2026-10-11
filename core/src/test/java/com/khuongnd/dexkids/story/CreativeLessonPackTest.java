package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CreativeLessonPackTest {
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
    @Test void acceptsCreativePacksForAllAgesAndModes() {
        for(var mode:EntertainmentDirector.Audience.values())
            for(int age=4;age<=10;age++) {
                var pack=CreativeLessonPack.validate(sample(16,mode,age,age),
                    mode,age,age,List.of());
                assertEquals(16,pack.lessons().size());
            }
        assertEquals(20,CreativeLessonPack.validate(
            sample(20,EntertainmentDirector.Audience.BOTH,10,4),
            EntertainmentDirector.Audience.BOTH,10,4,List.of()).lessons().size());
    }
    @Test void rejectsInvalidCountAgeAndFocus() {
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
