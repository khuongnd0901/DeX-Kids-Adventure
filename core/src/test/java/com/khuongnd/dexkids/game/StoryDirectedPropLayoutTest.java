package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StoryDirectedPropLayoutTest {
    @Test void flowerIsAboveBothOriginalHeadsAndInsideTreeCanopy() {
        for(boolean both:new boolean[]{false,true}) {
            var p=StoryPropLayout.directed(VisualCue.Action.LOOK_UP,VisualCue.Prop.FLOWER,both);
            assertTrue(p.visible());assertTrue(p.treeBehindFlower());
            assertTrue(p.y()>315+427,"Must clear tallest original solo companion");
            assertTrue(p.y()+p.height()<=1080);
            assertTrue(p.y()>=380+530*.7f && p.y()<380+530,"Flower overlaps existing tree canopy");
            assertTrue(1390+p.xOffset()+p.width()<1920,"Solo stage stays on-screen");
        }
    }
    @Test void upwardVocabularyNeverMakesPetsOrVehiclesFly() {
        for(var prop:new VisualCue.Prop[]{VisualCue.Prop.CAT,VisualCue.Prop.RABBIT,VisualCue.Prop.BOAT,VisualCue.Prop.VEHICLE})
            assertFalse(StoryPropLayout.directed(VisualCue.Action.LOOK_UP,prop,true).visible());
        for(var prop:new VisualCue.Prop[]{VisualCue.Prop.BIRD,VisualCue.Prop.BUTTERFLY,VisualCue.Prop.CLOUD,VisualCue.Prop.KITE})
            assertTrue(StoryPropLayout.directed(VisualCue.Action.LOOK_UP,prop,true).y()>742);
    }
    @Test void quantityMissingAndNoneAreHiddenWhilePetRemainsAtGround() {
        for(var prop:VisualCue.Prop.values())assertFalse(StoryPropLayout.directed(VisualCue.Action.COUNT,prop,true).visible());
        var rabbit=StoryPropLayout.directed(VisualCue.Action.PET,VisualCue.Prop.RABBIT,true);
        assertTrue(rabbit.visible());assertEquals(292,rabbit.y());assertFalse(rabbit.treeBehindFlower());
        assertFalse(StoryPropLayout.directed(VisualCue.Action.WAVE,VisualCue.Prop.NONE,true).visible());
    }
}
