package com.khuongnd.dexkids.story.adventure;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import com.khuongnd.dexkids.game.StoryGesture;
import com.khuongnd.dexkids.story.VisualCue;

class StoryPlaybackDirectorTest {
    @Test void completeOfflineUsingActualSpeechCallbacksAndFairQuestions() {
        var d=new StoryPlaybackDirector();d.start(StoryFixtures.educational(StoryEpisode.Audience.BOTH));
        List<StoryEpisode.Audience> targets=new ArrayList<>();int spoken=0;
        while(d.state()!=StoryPlaybackDirector.State.COMPLETE) {
            var event=d.next();assertNotNull(event);
            if(event instanceof StoryPlaybackDirector.Speech s) {
                spoken++;assertEquals(StoryPlaybackDirector.State.SPEAKING,d.state());
                assertNull(d.next());assertTrue(d.speechCompleted(s.token()));
                assertFalse(d.speechCompleted(s.token()));
            } else if(event instanceof StoryPlaybackDirector.Listen l) {
                targets.add(l.target());assertTrue(d.answer(true));assertFalse(d.answer(true));
            }
        }
        assertEquals(List.of(StoryEpisode.Audience.SAU,StoryEpisode.Audience.ONG,StoryEpisode.Audience.BOTH),targets);
        assertEquals(18,spoken);
    }
    @Test void interruptionRejectsStaleCompletionAndReplaysSameUnheardBeat() {
        var d=new StoryPlaybackDirector();d.start(StoryFixtures.educational(StoryEpisode.Audience.SAU));
        var first=(StoryPlaybackDirector.Speech)d.next();d.pause();
        assertFalse(d.speechCompleted(first.token()));assertNull(d.next());d.resume();
        var replay=(StoryPlaybackDirector.Speech)d.next();assertEquals(first.text(),replay.text());
        assertNotEquals(first.token(),replay.token());assertTrue(d.speechCompleted(replay.token()));
    }
    @Test void silenceAndMicrophoneRecreationNeverOverlapNarration() {
        var d=new StoryPlaybackDirector();d.start(StoryFixtures.educational(StoryEpisode.Audience.ONG));
        for(int i=0;i<3;i++){var s=(StoryPlaybackDirector.Speech)d.next();d.speechCompleted(s.token());}
        assertInstanceOf(StoryPlaybackDirector.Listen.class,d.next());
        d.pause();d.resume();var question=(StoryPlaybackDirector.Speech)d.next();
        assertEquals(StoryPlaybackDirector.Kind.QUESTION,question.kind());d.speechCompleted(question.token());d.next();
        assertFalse(d.tickListening(11));assertTrue(d.tickListening(1));
        assertEquals(StoryPlaybackDirector.Kind.FEEDBACK,((StoryPlaybackDirector.Speech)d.next()).kind());
    }
    @Test void editedQuestionsAndFeedbackCannotInheritNarrationPropsOrActions() {
        var d=new StoryPlaybackDirector();d.start(StoryFixtures.educational(StoryEpisode.Audience.ONG));
        for(int i=0;i<2;i++) {
            var narration=(StoryPlaybackDirector.Speech)d.next();
            assertEquals(VisualCue.Prop.TREE,narration.visual().cue().prop());
            d.speechCompleted(narration.token());
        }
        var question=(StoryPlaybackDirector.Speech)d.next();
        assertEquals(StoryPlaybackDirector.Kind.QUESTION,question.kind());
        assertEquals(question.text(),question.visual().caption());
        assertEquals("ONG",question.visual().focus());
        assertEquals(VisualCue.Prop.NONE,question.visual().cue().prop());
        assertEquals(VisualCue.Action.WAVE,question.visual().cue().action());
        d.speechCompleted(question.token());d.next();d.answer(true);
        var feedback=(StoryPlaybackDirector.Speech)d.next();
        assertEquals(StoryPlaybackDirector.Kind.FEEDBACK,feedback.kind());
        assertEquals(VisualCue.Prop.NONE,feedback.visual().cue().prop());
        assertEquals(feedback.text(),feedback.visual().caption());
    }
    @Test void explicitVoiceFailureAndCursorRestoreRejectOldCallbacks() {
        var d=new StoryPlaybackDirector();var episode=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
        d.start(episode);var s=(StoryPlaybackDirector.Speech)d.next();
        var cursor=d.cursor();d.restore(episode,cursor);assertFalse(d.speechCompleted(s.token()));
        var retry=(StoryPlaybackDirector.Speech)d.next();assertEquals(s.text(),retry.text());
        assertTrue(d.speechFailed(retry.token()));assertFalse(d.speechFailed(retry.token()));
        d.stop();assertNull(d.next());assertEquals(StoryPlaybackDirector.State.IDLE,d.state());
    }
    @Test void gesturesAreRealBoundedDistinctTransformsAndReplayCannotAuthorImages() {
        assertNotEquals(StoryGesture.sample(VisualCue.Action.WAVE,0),StoryGesture.sample(VisualCue.Action.WAVE,.4f));
        assertNotEquals(StoryGesture.sample(VisualCue.Action.PET,0),StoryGesture.sample(VisualCue.Action.COUNT,0));
        var policy=new StoryIllustrationPolicy();
        assertEquals(StoryIllustrationPolicy.Decision.LOCAL_ATLAS,policy.decide(StoryIllustrationPolicy.Phase.REPLAY,true,new StoryIllustrationPolicy.Capabilities(true,true),false,true));
        assertEquals(StoryIllustrationPolicy.Decision.LOCAL_ATLAS,policy.decide(StoryIllustrationPolicy.Phase.AUTHORING,false,new StoryIllustrationPolicy.Capabilities(true,true),false,true));
    }
}
