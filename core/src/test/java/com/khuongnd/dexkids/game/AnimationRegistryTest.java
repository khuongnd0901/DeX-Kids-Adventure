package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue;
import com.khuongnd.dexkids.story.VisualPlan;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnimationRegistryTest {
    @Test void onlyPreapprovedMotionPerActorAndProp() {
        var registry=new AnimationRegistry();
        assertTrue(registry.supports(AnimationRegistry.Actor.ILLUSTRATION,
                VisualPlan.Motion.FLY,VisualCue.Prop.BUTTERFLY));
        assertFalse(registry.supports(AnimationRegistry.Actor.ILLUSTRATION,
                VisualPlan.Motion.FLY,VisualCue.Prop.CAT));
        assertFalse(registry.supports(AnimationRegistry.Actor.SAU,
                VisualPlan.Motion.DRIVE,VisualCue.Prop.VEHICLE));
        assertTrue(registry.supports(AnimationRegistry.Actor.ONG,
                VisualPlan.Motion.CELEBRATE,VisualCue.Prop.NONE));
    }
}
