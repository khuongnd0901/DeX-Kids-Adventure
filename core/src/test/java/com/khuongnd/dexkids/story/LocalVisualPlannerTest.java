package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalVisualPlannerTest {
    private final LocalVisualPlanner planner = new LocalVisualPlanner();
    @Test void mapsKnownPropsToSafeMotion() {
        var butterfly = planner.plan("q-butterfly",
                new VisualCue(VisualCue.Theme.ANIMALS, VisualCue.Action.PET, VisualCue.Prop.BUTTERFLY));
        assertEquals(VisualPlan.Motion.FLY, butterfly.questionMotion());
        assertEquals(VisualPlan.Motion.CELEBRATE, butterfly.answerMotion());
        assertEquals(butterfly, planner.plan("q-butterfly", butterfly.cue()));
        assertEquals(VisualPlan.Motion.GROW, planner.plan("flower",
                new VisualCue(VisualCue.Theme.NATURE, VisualCue.Action.WAVE, VisualCue.Prop.FLOWER)).questionMotion());
    }
    @Test void unknownAndCountQuestionsNeverInventObjects() {
        assertEquals(VisualPlan.Motion.WAVE, planner.plan("unknown", VisualCue.DEFAULT).questionMotion());
        assertEquals(VisualPlan.Motion.COUNT, planner.plan("numbers",
                new VisualCue(VisualCue.Theme.COUNTING,VisualCue.Action.COUNT,VisualCue.Prop.NONE)).questionMotion());
        assertThrows(IllegalArgumentException.class,()->planner.plan("",VisualCue.DEFAULT));
    }
}
