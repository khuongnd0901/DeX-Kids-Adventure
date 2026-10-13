package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class M10AnimationTest {
    @Test void allProfilesAreUniqueFiniteBoundedAndOneShotsReturnToRest() {
        var ids=new HashSet<String>();
        assertEquals(26,AnimationRegistry.Profile.values().length);
        for(var p:AnimationRegistry.Profile.values()) {
            assertTrue(ids.add(p.id));
            if(p.channel==AnimationRegistry.Channel.WHEEL)continue;
            for(long seed:new long[]{Long.MIN_VALUE,0,1,1000,Long.MAX_VALUE})
                for(double t:new double[]{0,.1,.5,1,10,10000,1e20}) {
                    float v=p.sample(t,seed);
                    assertTrue(Float.isFinite(v),p.id);
                    if(p.channel==AnimationRegistry.Channel.BREATH)assertTrue(v>=.985f && v<=1.015f,p.id);
                    else if(p.channel==AnimationRegistry.Channel.SURPRISE)assertTrue(v>=1 && v<=1.05f,p.id);
                    else assertTrue(Math.abs(v)<=Math.max(12,p.amplitude),p.id);
                }
            if(!p.loop)assertEquals(p.channel==AnimationRegistry.Channel.SURPRISE?1:0,p.sample(p.period+1,0));
            assertThrows(IllegalArgumentException.class,()->p.sample(Double.NaN,0));
        }
        var wheel=AnimationRegistry.Profile.VEH_WHEEL;
        assertThrows(IllegalStateException.class,()->wheel.sample(1,0));
        assertEquals(25,AnimationRegistry.Profile.ENV_DRIFT.maxPeriod);
        assertEquals(8,AnimationRegistry.Profile.SAU_BLINK.maxPeriod);
    }
    private void advance(AnimationTimeline t,int frames) { for(int i=0;i<frames;i++)t.advance(.1f); }
    @Test void oneShotClockFreezesOnPauseDeduplicatesAndLimitsConcurrentEffects() {
        var t=new AnimationTimeline();t.prompt("welcome",VisualCue.DEFAULT);
        assertEquals(AnimationRegistry.Effect.WELCOME_WAVE,t.effect());
        advance(t,5);float before=t.effectProgress();t.advance(0);
        assertEquals(before,t.effectProgress());
        assertFalse(t.trigger(AnimationRegistry.Effect.QUESTION_POP,"q"));
        advance(t,20);assertNull(t.effect());
        assertFalse(t.trigger(AnimationRegistry.Effect.WELCOME_WAVE,"welcome"));
        assertTrue(t.trigger(AnimationRegistry.Effect.QUESTION_POP,"q"));
        assertEquals(AnimationRegistry.Effect.QUESTION_POP,t.effect());
        t.signal(new AnimationTimeline.Event("",AnimationTimeline.Phase.INTERRUPTED));
        assertNull(t.effect());
        assertFalse(t.trigger(null,"q"));
        assertFalse(t.trigger(AnimationRegistry.Effect.MILESTONE_CONFETTI,"x".repeat(161)));
    }
    @Test void onlyCurrentLanguageAnswerTriggersRevealAndDoesNotInventChildGrade() {
        var t=new AnimationTimeline();
        var cue=new VisualCue(VisualCue.Theme.LANGUAGE,VisualCue.Action.WAVE,VisualCue.Prop.NONE);
        t.prompt("english",cue);advance(t,25);
        t.signal(new AnimationTimeline.Event("stale",AnimationTimeline.Phase.ANSWER));
        assertNull(t.effect());
        t.signal(new AnimationTimeline.Event("english",AnimationTimeline.Phase.ANSWER));
        assertEquals(AnimationRegistry.Effect.ENGLISH_WORD_REVEAL,t.effect());
        advance(t,25);t.prompt("quiz",VisualCue.DEFAULT);advance(t,25);
        t.signal(new AnimationTimeline.Event("quiz",AnimationTimeline.Phase.ANSWER));
        assertNull(t.effect()); // Answer revelation is not proof that a child answered correctly.
    }
}
