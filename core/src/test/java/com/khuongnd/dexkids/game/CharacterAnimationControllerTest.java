package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharacterAnimationControllerTest {
    @Test void transitionResetsStateTimeAndKeepsGlobalTime() {
        var actor = new CharacterAnimationController();
        actor.advance(3);
        actor.setState(CharacterAnimationController.State.TALKING);
        actor.advance(0.21);
        assertEquals(1, actor.talkingFrame());
        assertEquals(0.21, actor.stateTime(), 1e-6);
        actor.setState(CharacterAnimationController.State.IDLE);
        assertEquals(0, actor.stateTime());
        assertEquals(3.21, actor.elapsedTotal(), 1e-6);
    }
    @Test void invalidDeltaRejected() {
        var actor = new CharacterAnimationController();
        assertThrows(IllegalArgumentException.class, () -> actor.advance(Double.NaN));
    }
}
