package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnimationTimelineTest {
    @Test void phaseEventsAreBeatBoundAndParentPauseFreezes() {
        var timeline = new AnimationTimeline();
        timeline.prompt("creative-1", new VisualCue(VisualCue.Theme.ANIMALS,
                VisualCue.Action.PET,VisualCue.Prop.BUTTERFLY));
        timeline.advance(.1f);
        assertTrue(timeline.elapsed() > 0f);
        timeline.signal(new AnimationTimeline.Event("creative-other",AnimationTimeline.Phase.ANSWER));
        assertEquals(AnimationTimeline.Phase.QUESTION,timeline.phase());
        timeline.signal(new AnimationTimeline.Event("creative-1",AnimationTimeline.Phase.ANSWER));
        assertEquals(AnimationTimeline.Phase.ANSWER,timeline.phase());
        assertEquals(0f,timeline.elapsed());
        timeline.advance(0f);
        assertEquals(0f,timeline.elapsed());
        timeline.signal(new AnimationTimeline.Event("",AnimationTimeline.Phase.INTERRUPTED));
        assertEquals(0f,timeline.x());
        assertEquals(0f,timeline.y());
        timeline.advance(2f);
        assertEquals(0f,timeline.elapsed());
        timeline.prompt("creative-2",VisualCue.DEFAULT);
        assertEquals(AnimationTimeline.Phase.QUESTION,timeline.phase());
    }
    @Test void boundsAndFiniteTransforms() {
        var t = new AnimationTimeline();
        t.prompt("q",VisualCue.DEFAULT);
        assertThrows(IllegalArgumentException.class,()->t.advance(Float.NaN));
        assertThrows(IllegalArgumentException.class,()->t.advance(-1f));
        for(int i=0;i<10000;i++)t.advance(1f);
        assertTrue(Float.isFinite(t.x()));
        assertTrue(Float.isFinite(t.y()));
        assertTrue(t.scale() >= 1f);
    }
}
