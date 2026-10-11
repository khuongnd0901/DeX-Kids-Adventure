package com.khuongnd.dexkids.story;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VisualCueTest {
    @Test void vietnameseButterflyMapsToAvailableArt() {
        var lesson=new CreativeLessonPack.Lesson(
            "Con bướm đang bay qua khu vườn nào?",
            "Bạn bướm đang bay trong vườn hoa.",
            "butterfly","con bướm","animals",CreativeLessonPack.Genre.QUIZ,
            EntertainmentDirector.Audience.SAU,5,false);
        var cue=VisualCue.fromCreative(lesson);
        assertEquals(VisualCue.Theme.ANIMALS,cue.theme());
        assertEquals(VisualCue.Prop.BUTTERFLY,cue.prop());
        assertEquals(VisualCue.Action.PET,cue.action());
    }

    @Test void countingAndCloudsHaveBoundedGestures() {
        var count=VisualCue.fromAuthored("creative-unknown",
            "Có bao nhiêu hình tròn trong tranh nhỉ?","");
        assertEquals(VisualCue.Theme.COUNTING,count.theme());
        assertEquals(VisualCue.Action.COUNT,count.action());
        var clouds=VisualCue.fromAuthored("sau-weather",
            "Mây trắng trôi trên bầu trời đó con ơi!","");
        assertEquals(VisualCue.Prop.CLOUD,clouds.prop());
        assertEquals(VisualCue.Action.LOOK_UP,clouds.action());
    }

    @Test void unknownPhrasesDoNotLoadInventedTextures() {
        var cue=VisualCue.fromAuthored("creative-123",
            "Ngày hôm nay chúng mình cùng khám phá điều mới nhé!","");
        assertEquals(VisualCue.Prop.NONE,cue.prop());
        assertEquals(VisualCue.Action.WAVE,cue.action());
    }

    @Test void legacyAndExplicitBeatConstructorsRemainCompatible() {
        var old=new EntertainmentDirector.Beat("sau-rabbit",
            EntertainmentDirector.Audience.SAU,
            "Một chú thỏ trong tranh đang nhảy lên nào!",
            "Thỏ nhảy bằng đôi chân sau.",EntertainmentDirector.Reaction.WAVE);
        assertEquals(VisualCue.Prop.RABBIT,old.visual().prop());
        var explicit=new EntertainmentDirector.Beat("creative-xyz",
            EntertainmentDirector.Audience.BOTH,
            "Cùng quan sát những bông hoa trong tranh nhé!",
            "Một bông hoa đang nở rất đẹp.",EntertainmentDirector.Reaction.WAVE,
            "flower",new VisualCue(VisualCue.Theme.NATURE,
                VisualCue.Action.COUNT,VisualCue.Prop.FLOWER));
        assertEquals(VisualCue.Action.COUNT,explicit.visual().action());
    }
}
