package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import com.khuongnd.dexkids.game.KidsGame;
import com.khuongnd.dexkids.story.VisualCue;
import com.khuongnd.dexkids.story.adventure.*;
import java.io.File;
import static com.khuongnd.dexkids.qa.ArtVisualValidation.*;

/** Test-only fixed local cues through the production atomic story bridge. No AI/voice claim. */
final class StoryExpandedValidation {
    static void run(LifecycleValidationRunner r,Bundle result) throws Exception {
        require(r.fullHdQaDisplay(),"Explicit FullHD emulator or external DeX required");
        var context=r.getTargetContext();var prefs=context.getSharedPreferences("parent_settings",0);var old=prefs.getAll();
        var ai=context.getSharedPreferences("kids_ai_settings_v1",0);var oldAi=ai.getAll();
        Activity[] activities=new Activity[2];var monitor=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);
        File dir=new File(context.getExternalFilesDir(null),"qa-t063-story-expanded");require(dir.isDirectory()||dir.mkdirs(),"Evidence directory");
        try {
            prefs.edit().putString("audience_mode","BOTH").putBoolean("audio_only",false)
                .putBoolean("offline_tts",false).putBoolean("tts_enabled",false)
                .putBoolean("ambient_music",false).putBoolean("child_mic_optin",false).commit();
            ai.edit().putBoolean("ai_quizzes",false).putBoolean("ai_trip_companion_optin",false).putBoolean("creative_trip_packs",false).commit();
            activities[0]=r.startActivitySync(new Intent().setClassName("com.khuongnd.dexkids","com.khuongnd.dexkids.ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            r.runOnMainSync(()->require(preview(activities[0].getWindow().getDecorView()).performClick(),"DEMO missing"));
            activities[1]=r.waitForMonitorWithTimeout(monitor,10000);require(activities[1]!=null,"Child missing");SystemClock.sleep(3000);
            r.runOnMainSync(()->{try {
                ((android.os.Handler)field(activities[1],"handler")).removeCallbacks((Runnable)field(activities[1],"updatePoiNativeStatus"));
                var clear=activities[1].getClass().getDeclaredMethod("clearTalkQueue");clear.setAccessible(true);clear.invoke(activities[1]);
                Object session=field(activities[1],"storySession");var stop=session.getClass().getDeclaredMethod("stop");stop.setAccessible(true);stop.invoke(session);
            }catch(Exception e){throw new AssertionError(e);}});
            var game=(KidsGame)field(activities[1],"runningGame");Object screen=game.getScreen();int count=0;
            var actions=new VisualCue.Action[]{VisualCue.Action.JUMP,VisualCue.Action.DANCE,VisualCue.Action.BOW,VisualCue.Action.READ,VisualCue.Action.WATER,VisualCue.Action.SHARE,VisualCue.Action.INSPECT};
            for(String target:new String[]{"SAU","ONG","BOTH","GUIDE"})for(int index=0;index<(target.equals("BOTH")?23:7);index++) {
                var action=index<7?actions[index]:VisualCue.Action.WAVE;
                String audience=target.equals("GUIDE")?"BOTH":target;
                var prop=index>=7?VisualCue.Prop.values()[VisualCue.Prop.BOOK.ordinal()+index-7]:switch(action){case READ->VisualCue.Prop.BOOK;case WATER->VisualCue.Prop.SEEDLING;case SHARE->VisualCue.Prop.GIFT;case INSPECT->VisualCue.Prop.LEAF;default->VisualCue.Prop.NONE;};
                String caption="Trong truyện tưởng tượng · "+audience+" · "+action+" · "+prop;
                var compiled=new StorySceneCompiler.CompiledBeat("qa-visual-"+target+"-"+action+"-"+prop,caption,target.equals("GUIDE")?audience:target.equals("ONG")?"ONG":"SAU",
                    new VisualCue(VisualCue.Theme.NATURE,action,prop),StoryEpisode.Emotion.CURIOUS, target.equals("GUIDE")?StoryEpisode.Actor.CAPYBARA:audience.equals("ONG")?StoryEpisode.Actor.ONG:StoryEpisode.Actor.SAU);
                r.runOnMainSync(()->{try {
                    game.setAudienceMode(audience);game.showStoryBeat(compiled);game.showEntertainmentPhase(compiled.id(),"QUESTION");
                    var dashboard=field(activities[1],"adventureDashboard");dashboard.getClass().getMethod("setMode",String.class,int.class,int.class).invoke(dashboard,audience,5,4);
                    dashboard.getClass().getMethod("showConversation",String.class,String.class).invoke(dashboard,"QA TRUYỆN TƯỞNG TƯỢNG · "+audience,caption);
                    var banner=activities[1].getClass().getDeclaredMethod("showEntertainmentBanner",String.class);banner.setAccessible(true);banner.invoke(activities[1],caption);
                }catch(Exception e){throw new AssertionError(e);}});
                gl(()->set(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->true));SystemClock.sleep(200);
                capture(r,dir,target.toLowerCase()+"-"+action.name().toLowerCase()+"-"+prop.name().toLowerCase()+"-a");
                // Same cue and same journey position; advance only the render-owned gesture clock.
                gl(()->{for(int i=0;i<4;i++)((com.khuongnd.dexkids.game.AnimationTimeline)field(screen,"animationTimeline")).advance(.1f);});
                SystemClock.sleep(400);capture(r,dir,target.toLowerCase()+"-"+action.name().toLowerCase()+"-"+prop.name().toLowerCase()+"-b");count+=2;
            }
            result.putInt("captures",count);result.putString("scope","Production local cue bridge; seven expanded actions for all audiences; fixed clock A/B; visual review required; no TTS claim");
        } finally {
            r.runOnMainSync(()->{for(Activity a:activities)if(a!=null)a.finish();});r.removeMonitor(monitor);restore(prefs,old);restore(ai,oldAi);
        }
    }
}
