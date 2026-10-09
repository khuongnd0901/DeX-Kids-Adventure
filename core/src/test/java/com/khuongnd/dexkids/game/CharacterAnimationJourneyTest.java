package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharacterAnimationJourneyTest {
    @Test void drivingWavingStationarySleepingAndResuming() {
        var actor = new CharacterAnimationController();
        actor.drive(0.1, 12);
        assertEquals(CharacterAnimationController.State.ROLLING, actor.state());
        actor.requestWave();
        actor.drive(0.1, 12);
        assertEquals(CharacterAnimationController.Frame.WAVE, actor.frame());
        actor.drive(2.0, 12);
        actor.drive(0.1, 12);
        assertEquals(CharacterAnimationController.State.ROLLING, actor.state());
        for(int i=0;i<60;i++) actor.drive(1.0, 0);
        assertEquals(CharacterAnimationController.Frame.SLEEP, actor.frame());
        actor.drive(0.1, 3);
        assertEquals(CharacterAnimationController.State.ROLLING, actor.state());
    }
    @Test void speakingFramesRequireExplicitNarrationFlag() {
        var actor = new CharacterAnimationController();
        actor.drive(2, 7);
        assertNotEquals(CharacterAnimationController.Frame.TALK_OPEN, actor.frame());
        actor.setNarrationActive(true);
        actor.drive(0.1, 7);
        assertEquals(CharacterAnimationController.State.TALKING, actor.state());
        assertEquals(CharacterAnimationController.Frame.TALK_OPEN, actor.frame());
        actor.drive(0.2, 7);
        assertEquals(CharacterAnimationController.Frame.TALK_CLOSED, actor.frame());
        actor.setNarrationActive(false);
        actor.drive(0.1, 7);
        assertEquals(CharacterAnimationController.State.ROLLING, actor.state());
    }
    @Test void rejectsNonFiniteGpsAndNegativeTime() {
        var actor=new CharacterAnimationController();
        assertThrows(IllegalArgumentException.class, ()->actor.drive(Double.NaN, 5));
        assertThrows(IllegalArgumentException.class, ()->actor.drive(0.02, Double.POSITIVE_INFINITY));
    }
}
