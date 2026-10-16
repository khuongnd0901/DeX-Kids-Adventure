package com.khuongnd.dexkids.game;
import com.khuongnd.dexkids.story.VisualCue.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StoryExpandedMotionTest {
    @Test void everyActionIsFiniteBoundedAndPeriodicIncludingHugeClockValues() {
        for(var action:Action.values())for(float t:new float[]{0,.2f,.7f,1.2f,3.99f,4,Float.MAX_VALUE,Float.NaN,Float.POSITIVE_INFINITY,-10}) {
            var p=StoryGesture.sample(action,t);
            assertTrue(Float.isFinite(p.x())&&Float.isFinite(p.y())&&Float.isFinite(p.rotation()));
            assertTrue(Math.abs(p.x())<=24 && Math.abs(p.y())<=40 && Math.abs(p.rotation())<=15);
            assertSame(p,StoryGesture.sample(action,t),"No per-frame transform allocation");
        }
        assertTrue(StoryGesture.sample(Action.JUMP,.5f).y()>25);
        assertTrue(StoryGesture.sample(Action.BOW,1).rotation()<-8);
        assertNotEquals(StoryGesture.sample(Action.DANCE,.3f),StoryGesture.sample(Action.WAVE,.3f));
        assertEquals(StoryGesture.sample(Action.JUMP,.5f),StoryGesture.sample(Action.JUMP,4.5f));
    }
    @Test void attachmentsRemainBelowFacesForEveryActorScale() {
        for(var action:new Action[]{Action.READ,Action.WATER,Action.SHARE,Action.INSPECT}) {
            var b=StoryInteractionLayout.primary(action);
            assertTrue(b.y()+b.height()<.64f,"Keep existing faces unobscured");
            assertTrue(b.x()+b.width()<=1.4f,"Bounded reach to supporting objects");
            for(float width:new float[]{148,224,248,285})assertTrue(b.width()*width<210);
        }
        assertEquals(0,StoryInteractionLayout.offerOffset(0),.0001);
        assertTrue(StoryInteractionLayout.offerOffset(1)>0);
    }
}
