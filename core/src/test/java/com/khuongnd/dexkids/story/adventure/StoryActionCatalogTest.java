package com.khuongnd.dexkids.story.adventure;

import com.khuongnd.dexkids.story.VisualCue;
import com.khuongnd.dexkids.story.VisualCue.*;
import com.khuongnd.dexkids.game.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StoryActionCatalogTest {
    @Test void semanticAttachmentCombinationsRejectInvisibleWrongObjects() {
        assertTrue(StoryActionCatalog.compatible(Action.READ,Prop.BOOK));
        assertFalse(StoryActionCatalog.compatible(Action.READ,Prop.TREE));
        assertTrue(StoryActionCatalog.compatible(Action.WATER,Prop.SEEDLING));
        assertTrue(StoryActionCatalog.compatible(Action.WATER,Prop.FLOWER));
        assertFalse(StoryActionCatalog.compatible(Action.WATER,Prop.BOAT));
        assertTrue(StoryActionCatalog.compatible(Action.SHARE,Prop.GIFT));
        assertFalse(StoryActionCatalog.compatible(Action.SHARE,Prop.TREE));
        assertTrue(StoryActionCatalog.compatible(Action.INSPECT,Prop.SHELL));
        assertFalse(StoryActionCatalog.compatible(Action.INSPECT,Prop.MAGNIFYING_GLASS));
        for(var prop:Prop.values())assertEquals(prop==Prop.NONE,StoryActionCatalog.compatible(Action.COUNT,prop));
    }
    @Test void newCuesRoundTripThroughActualStoryCodecAndCompiler() {
        var pairs=Map.of(Action.JUMP,Prop.NONE,Action.DANCE,Prop.BALL,Action.BOW,Prop.NONE,
            Action.READ,Prop.BOOK,Action.WATER,Prop.SEEDLING,Action.SHARE,Prop.GIFT,Action.INSPECT,Prop.LEAF);
        for(var entry:pairs.entrySet()) {
            var e=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
            var scenes=new ArrayList<>(e.scenes());var s=scenes.get(0);var beats=new ArrayList<>(s.beats());
            beats.set(0,new StoryEpisode.Beat(StoryEpisode.Actor.CAPYBARA,"Trong câu chuyện tưởng tượng, các bạn cùng khám phá.",StoryEpisode.Emotion.HAPPY,entry.getKey(),entry.getValue()));
            scenes.set(0,new StoryEpisode.Scene(s.id(),s.backdrop(),beats,s.question()));
            var expanded=new StoryEpisode(e.id(),1,1,e.title(),e.audience(),5,4,e.educationalGoal(),e.synopsis(),scenes);
            assertEquals(expanded,StoryEpisodeCodec.parse(StoryEpisodeCodec.toJson(expanded)));
            assertEquals(StoryEpisode.Actor.CAPYBARA,new StorySceneCompiler().compile(expanded,0,0).actor());
            if(entry.getKey()==Action.READ) {
                beats.set(0,new StoryEpisode.Beat(StoryEpisode.Actor.CAPYBARA,"Trong câu chuyện tưởng tượng, các bạn cùng khám phá.",StoryEpisode.Emotion.HAPPY,Action.READ,Prop.TREE));
                scenes.set(0,new StoryEpisode.Scene(s.id(),s.backdrop(),beats,s.question()));
                assertThrows(IllegalArgumentException.class,()->StoryEpisodeValidator.validate(new StoryEpisode(e.id(),1,1,e.title(),e.audience(),5,4,e.educationalGoal(),e.synopsis(),scenes)));
            }
        }
    }
    @Test void authoredTextSelectsObjectsWithoutVietnameseAccentCollisions() {
        assertEquals(Action.READ,VisualCue.fromAuthored("","Sâu đọc sách về khu vườn.","").action());
        assertEquals(Prop.BOOK,VisualCue.fromAuthored("","Sâu đọc sách về khu vườn.","").prop());
        assertEquals(Action.WATER,VisualCue.fromAuthored("","Ong tưới cây non nhẹ nhàng.","").action());
        assertEquals(Action.INSPECT,VisualCue.fromAuthored("","Sâu dùng kính lúp quan sát chiếc lá.","").action());
        assertEquals(Action.SHARE,VisualCue.fromAuthored("","Ong chia sẻ quả bóng với bạn.","").action());
        assertEquals(Action.JUMP,VisualCue.fromAuthored("","Sâu nhảy lên một nhịp.","").action());
        assertNotEquals(Prop.LEAF,VisualCue.fromAuthored("","Đây là bạn chim trên cành cao.","").prop());
        assertNotEquals(Prop.BASKET,VisualCue.fromAuthored("","Cơn gió nhẹ thổi qua khu vườn.","").prop());
        assertEquals(Prop.PENGUIN,VisualCue.fromAuthored("","Ong chào bạn chim cánh cụt.","").prop());
    }
    @Test void bundledPromptListsEveryActionAndPropAndOldFixturesRemainValid() {
        String prompt=StoryActionCatalog.promptCatalog();
        for(var action:Action.values())assertTrue(prompt.contains(action.name()));
        for(var prop:Prop.values())assertTrue(prompt.contains(prop.name()));
        for(var audience:StoryEpisode.Audience.values()) {
            StoryEpisodeValidator.validate(StoryFixtures.educational(audience));
            var expanded=StoryFixtures.expanded(audience);
            assertEquals(expanded,StoryEpisodeCodec.parse(StoryEpisodeCodec.toJson(expanded)));
            var turns=expanded.scenes().stream().filter(s->s.question()!=null).map(s->s.question().target()).toList();
            assertEquals(audience==StoryEpisode.Audience.BOTH?List.of(StoryEpisode.Audience.SAU,StoryEpisode.Audience.ONG,StoryEpisode.Audience.BOTH):Collections.nCopies(3,audience),turns);
        }
    }
}
