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
final class StoryVisualValidation {
    static void run(LifecycleValidationRunner r,Bundle result) throws Exception {
        require(r.fullHdQaDisplay(),"Explicit FullHD emulator or external DeX required");
        var context=r.getTargetContext();var prefs=context.getSharedPreferences("parent_settings",0);var old=prefs.getAll();
        var ai=context.getSharedPreferences("kids_ai_settings_v1",0);var oldAi=ai.getAll();
        Activity[] activities=new Activity[2];var monitor=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);
        File dir=new File(context.getExternalFilesDir(null),"qa-t059-story-visual");require(dir.isDirectory()||dir.mkdirs(),"Evidence directory");
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
            for(String audience:new String[]{"SAU","ONG","BOTH"})for(var action:new VisualCue.Action[]{VisualCue.Action.LOOK_UP,VisualCue.Action.PET}) {
                var prop=action==VisualCue.Action.LOOK_UP?VisualCue.Prop.FLOWER:VisualCue.Prop.RABBIT;
                String caption=action==VisualCue.Action.LOOK_UP?"Trong truyện tưởng tượng, các bạn ngước nhìn đóa hoa trên cành cây.":"Trong truyện tưởng tượng, các bạn vuốt ve bạn thỏ thật nhẹ nhàng.";
                var compiled=new StorySceneCompiler.CompiledBeat("qa-visual-"+audience+"-"+action,caption,audience,
                    new VisualCue(VisualCue.Theme.NATURE,action,prop),StoryEpisode.Emotion.CURIOUS);
                r.runOnMainSync(()->{try {
                    game.setAudienceMode(audience);game.showStoryBeat(compiled);game.showEntertainmentPhase(compiled.id(),"QUESTION");
                    var dashboard=field(activities[1],"adventureDashboard");dashboard.getClass().getMethod("setMode",String.class,int.class,int.class).invoke(dashboard,audience,5,4);
                    dashboard.getClass().getMethod("showConversation",String.class,String.class).invoke(dashboard,"QA TRUYỆN TƯỞNG TƯỢNG · "+audience,caption);
                    var banner=activities[1].getClass().getDeclaredMethod("showEntertainmentBanner",String.class);banner.setAccessible(true);banner.invoke(activities[1],caption);
                }catch(Exception e){throw new AssertionError(e);}});
                gl(()->set(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->true));SystemClock.sleep(200);
                capture(r,dir,audience.toLowerCase()+"-"+action.name().toLowerCase()+"-a");
                // Same cue and same journey position; advance only the render-owned gesture clock.
                gl(()->{for(int i=0;i<4;i++)((com.khuongnd.dexkids.game.AnimationTimeline)field(screen,"animationTimeline")).advance(.1f);});
                SystemClock.sleep(400);capture(r,dir,audience.toLowerCase()+"-"+action.name().toLowerCase()+"-b");count+=2;
            }
            result.putInt("captures",count);result.putString("scope","Production local cue bridge; fixed FullHD LOOK_UP/FLOWER and PET/RABBIT for all audiences; visual review required; no TTS claim");
        } finally {
            r.runOnMainSync(()->{for(Activity a:activities)if(a!=null)a.finish();});r.removeMonitor(monitor);restore(prefs,old);restore(ai,oldAi);
        }
    }
}
