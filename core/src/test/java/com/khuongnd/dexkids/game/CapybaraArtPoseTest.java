package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CapybaraArtPoseTest {
    @Test void mapsExistingEventsWithoutChangingMotion() {
        var actor = new CharacterAnimationController();
        actor.drive(.1,12);
        assertEquals(CapybaraArtPose.IDLE,CapybaraArtPose.resolve(actor.frame(),"",AnimationTimeline.Phase.INTERRUPTED));
        assertEquals(CapybaraArtPose.ASK,CapybaraArtPose.resolve(actor.frame(),"quiz",AnimationTimeline.Phase.QUESTION));
        assertEquals(CapybaraArtPose.LAUGH,CapybaraArtPose.resolve(actor.frame(),"quiz",AnimationTimeline.Phase.ANSWER));
        assertEquals(CharacterAnimationController.State.ROLLING,actor.state());
        actor.requestWave();actor.drive(.1,12);
        assertEquals(CapybaraArtPose.WAVE,CapybaraArtPose.resolve(actor.frame(),"quiz",AnimationTimeline.Phase.QUESTION));
        assertEquals(CapybaraArtPose.WAVE,CapybaraArtPose.resolve(CharacterAnimationController.Frame.IDLE,"welcome",AnimationTimeline.Phase.QUESTION));
        assertEquals(CapybaraArtPose.IDLE,CapybaraArtPose.resolve(CharacterAnimationController.Frame.IDLE,"quiz",AnimationTimeline.Phase.INTERRUPTED));
    }
    @Test void fadeFreezesOnPauseAndSettlesDuringRapidChanges() {
        var transition = new CapybaraArtPose.Transition();
        transition.advance(CapybaraArtPose.ASK,.09f);
        assertEquals(.5f,transition.alpha(),.0001f);
        transition.advance(CapybaraArtPose.LAUGH,0);
        assertEquals(CapybaraArtPose.ASK,transition.current());
        assertEquals(.5f,transition.alpha(),.0001f);
        transition.advance(CapybaraArtPose.LAUGH,.09f);
        assertEquals(1,transition.alpha());
        transition.advance(CapybaraArtPose.LAUGH,.18f);
        assertEquals(CapybaraArtPose.LAUGH,transition.current());
        assertEquals(CapybaraArtPose.ASK,transition.from());
        assertThrows(IllegalArgumentException.class,()->transition.advance(CapybaraArtPose.IDLE,Float.NaN));
    }
    @Test void layoutPreservesBusAspectAndKeepsPassengersInCabin() {
        assertEquals(1432f/1010f,VehicleLayout.PNG_WIDTH/VehicleLayout.PNG_HEIGHT,.00001);
        assertTrue(VehicleLayout.PNG_CHILD_Y>VehicleLayout.Y);
        assertTrue(VehicleLayout.PNG_CHILD_Y+VehicleLayout.PNG_CHILD_HEIGHT<VehicleLayout.Y+VehicleLayout.PNG_HEIGHT);
        assertTrue(VehicleLayout.PNG_ONG_X+VehicleLayout.PNG_CHILD_WIDTH<VehicleLayout.X+VehicleLayout.PNG_WIDTH*.65f);
        assertTrue(VehicleLayout.PNG_CAPYBARA_X>=VehicleLayout.X+VehicleLayout.PNG_WIDTH);
    }
}
