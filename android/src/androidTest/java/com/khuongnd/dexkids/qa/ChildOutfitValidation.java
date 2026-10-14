package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import com.khuongnd.dexkids.game.KidsGame;
import com.khuongnd.dexkids.game.StoryCompanionPose;
import com.khuongnd.dexkids.story.VisualCue;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import static com.khuongnd.dexkids.qa.ArtVisualValidation.*;

/** Test APK only: frozen offline fixture; no production timer or public debug API. */
final class ChildOutfitValidation {
    @SuppressWarnings("unchecked")
    static void run(LifecycleValidationRunner r, Bundle result) throws Exception {
        require(r.fullHdQaDisplay() && r.targetDisplay()==0,"Explicit emulator display 0 required");
        var context=r.getTargetContext();
        var prefs=context.getSharedPreferences("parent_settings",0); var old=prefs.getAll();
        var ai=context.getSharedPreferences("kids_ai_settings_v1",0); var oldAi=ai.getAll();
        Activity[] activities=new Activity[2];
        var monitor=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);
        File dir=new File(context.getExternalFilesDir(null),"qa-t053");
        require(dir.isDirectory() || dir.mkdirs(),"Evidence directory unavailable");
        try {
            prefs.edit().putString("audience_mode","BOTH").putBoolean("audio_only",false)
                .putBoolean("offline_tts",false).putBoolean("tts_enabled",false)
                .putBoolean("ambient_music",false).putBoolean("audio_effects",false)
                .putBoolean("child_mic_optin",false).commit();
            ai.edit().putBoolean("ai_quizzes",false).putBoolean("ai_trip_companion_optin",false)
                .putBoolean("creative_trip_packs",false).commit();
            activities[0]=r.startActivitySync(new Intent().setClassName("com.khuongnd.dexkids","com.khuongnd.dexkids.ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            r.runOnMainSync(()->require(preview(activities[0].getWindow().getDecorView()).performClick(),"DEMO click failed"));
            activities[1]=r.waitForMonitorWithTimeout(monitor,10000);require(activities[1]!=null,"DEMO absent");
            SystemClock.sleep(3000);
            r.runOnMainSync(()->{try {
                ((android.os.Handler)field(activities[1],"handler")).removeCallbacks((Runnable)field(activities[1],"updatePoiNativeStatus"));
                var clear=activities[1].getClass().getDeclaredMethod("clearTalkQueue");clear.setAccessible(true);clear.invoke(activities[1]);
            }catch(Exception e){throw new AssertionError(e);}});
            var game=(KidsGame)field(activities[1],"runningGame"); Object screen=game.getScreen();
            Class<?> rotation=Class.forName("com.khuongnd.dexkids.game.OutfitRotation");
            var ctor=rotation.getDeclaredConstructor(long.class);ctor.setAccessible(true);
            var sau=rotation.getDeclaredMethod("sau",float.class);sau.setAccessible(true);
            var ong=rotation.getDeclaredMethod("ong",float.class);ong.setAccessible(true);
            var prompt=Class.forName("com.khuongnd.dexkids.game.AdventureHud$Prompt").getDeclaredConstructors()[0];prompt.setAccessible(true);
            int captures=0;
            for(String audience:new String[]{"SAU","ONG","BOTH"})for(int index=0;index<6;index++) {
                Object selected=null;
                for(long seed=0;seed<10000;seed++) {
                    Object candidate=ctor.newInstance(seed);
                    if((int)sau.invoke(candidate,0f)==index && (int)ong.invoke(candidate,0f)==index){selected=candidate;break;}
                }
                require(selected!=null,"Cannot seed costume pair"); final Object fixed=selected;
                for(var pose:StoryCompanionPose.values()) {
                    Object cue=prompt.newInstance("TRANG PHUC","QA offline: "+audience+" / "+index+" / "+pose,
                        pose,"BOTH","qa-outfit",new VisualCue(VisualCue.Theme.GENERAL,VisualCue.Action.valueOf(pose.name()),VisualCue.Prop.NONE));
                    gl(()->{
                        set(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->true);set(screen,"clock",0f);
                        ((AtomicReference<String>)field(screen,"audienceMode")).set(audience);
                        ((AtomicReference<Object>)field(screen,"hudPrompt")).set(cue);
                        set(field(screen,"cartoonSprites"),"outfitRotation",fixed);
                    });
                    SystemClock.sleep(400);capture(r,dir,audience.toLowerCase()+"-"+index+"-"+pose.name().toLowerCase());captures++;
                }
            }
            result.putInt("captures",captures);
            result.putString("scope","72 actual Android FullHD frozen DEMO captures; visual review separate; no network");
        } finally {
            r.runOnMainSync(()->{for(Activity a:activities)if(a!=null)a.finish();});r.removeMonitor(monitor);
            restore(prefs,old);restore(ai,oldAi);
        }
    }
}
