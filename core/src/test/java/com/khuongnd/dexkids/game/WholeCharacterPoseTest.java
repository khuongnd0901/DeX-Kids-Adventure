package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue.Action;
import com.khuongnd.dexkids.story.adventure.StoryEpisode.Emotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WholeCharacterPoseTest {
    @Test public void everyAuthoredActionAndExpressionHasFiniteWholeFigureFrames() {
        for (Action action : Action.values()) for (Emotion emotion : Emotion.values())
            for (float time : new float[]{-1, 0, .25f, .75f, 7.999f, 8, Float.MAX_VALUE, Float.NaN, Float.POSITIVE_INFINITY}) {
                var frame = WholeCharacterPose.sample(action, emotion, true, time);
                assertTrue(frame.index() >= 0 && frame.index() < WholeCharacterPose.COUNT);
                assertTrue(Float.isFinite(frame.x()) && Math.abs(frame.x()) <= 5);
                assertTrue(Float.isFinite(frame.y()) && frame.y() >= -1);
                assertTrue(Math.abs(frame.y()) <= 24);
                assertTrue(Float.isFinite(frame.rotation()) && Math.abs(frame.rotation()) <= 1.2f);
            }
    }
    @Test public void locomotionAndGesturePairsUseDistinctPaintingsWithoutPageChurn() {
        for (Action action : new Action[]{Action.WAVE, Action.JUMP, Action.DANCE, Action.WALK, Action.RUN, Action.CLAP}) {
            var first = WholeCharacterPose.sample(action, Emotion.HAPPY, false, .25f);
            var second = WholeCharacterPose.sample(action, Emotion.HAPPY, false, .75f);
            assertNotEquals(first.index(), second.index(), action.name());
            assertEquals(first.page(), second.page(), action.name());
        }
    }
    @Test public void frozenClockKeepsCompletePoseAndPropTransformIdentical() {
        for (Action action : Action.values())
            assertEquals(WholeCharacterPose.sample(action, Emotion.CALM, false, 2.14f),
                         WholeCharacterPose.sample(action, Emotion.CALM, false, 2.14f));
    }
    @Test public void expressionsAndSpeechUseWholeFiguresInTheirOwnRange() {
        for (Emotion emotion : Emotion.values())
            assertEquals(32 + emotion.ordinal(), WholeCharacterPose.sample(Action.IDLE, emotion, false, 0).index());
        assertEquals(46, WholeCharacterPose.sample(Action.TALK, Emotion.CALM, true, .25f).index());
        assertEquals(47, WholeCharacterPose.sample(Action.TALK, Emotion.CALM, true, .75f).index());
    }
}
