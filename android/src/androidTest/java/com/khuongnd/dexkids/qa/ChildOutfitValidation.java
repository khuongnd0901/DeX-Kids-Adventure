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
    static void run(LifecycleValidationRunner r, Bundle result, boolean fallback) throws Exception {
        require(r.fullHdQaDisplay() && r.targetDisplay()==0,"Explicit emulator display 0 required");
        var context=r.getTargetContext();
        var prefs=context.getSharedPreferences("parent_settings",0); var old=prefs.getAll();
        var ai=context.getSharedPreferences("kids_ai_settings_v1",0); var oldAi=ai.getAll();
        Activity[] activities=new Activity[2];
        var monitor=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);
        File dir=new File(context.getExternalFilesDir(null),fallback?"qa-t053-fallback":"qa-t053");
        require(dir.isDirectory() || dir.mkdirs(),"Evidence directory unavailable");
        try {
            prefs.edit().putString("audience_mode","BOTH").putBoolean("audio_only",false)
                .putBoolean("offline_tts",false).putBoolean("tts_enabled",false)
                .putBoolean("ambient_music",false).putBoolean("audio_effects",false)
                .putBoolean("child_mic_optin",false).commit();
            ai.edit().putBoolean("ai_quizzes",false).putBoolean("ai_trip_companion_optin",false)
                .putBoolean("creative_trip_packs",false).commit();
            verifyAges(context, result);
            activities[0]=r.startActivitySync(new Intent().setClassName("com.khuongnd.dexkids","com.khuongnd.dexkids.ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            SystemClock.sleep(500);capture(r,dir,"parent-settings");
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
            var prompt=Class.forName("com.khuongnd.dexkids.game.AdventureHud$Prompt").getDeclaredConstructor(String.class,String.class,StoryCompanionPose.class,String.class,String.class,VisualCue.class);prompt.setAccessible(true);
            int captures=0;
            for(String audience:new String[]{"SAU","ONG","BOTH"})for(int index=0;index<(fallback?1:6);index++) {
                Object selected=null;
                for(long seed=0;seed<10000;seed++) {
                    Object candidate=ctor.newInstance(seed);
                    if((int)sau.invoke(candidate,0f)==index && (int)ong.invoke(candidate,0f)==index){selected=candidate;break;}
                }
                require(selected!=null,"Cannot seed costume pair"); final Object fixed=selected;
                r.runOnMainSync(()->{try {
                    prefs.edit().putString("audience_mode",audience).commit();game.setAudienceMode(audience);
                    Object dashboard=field(activities[1],"adventureDashboard");
                    dashboard.getClass().getMethod("setMode",String.class,int.class,int.class).invoke(dashboard,audience,5,4);
                }catch(Exception e){throw new AssertionError(e);}});
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
            Object[] atlas={null};
            var measurements=new org.json.JSONArray();
            var transitions=new org.json.JSONArray();
            java.util.Set<Integer> seenSau=new java.util.HashSet<>(),seenOng=new java.util.HashSet<>();
            for(int bucket=0;bucket<=6;bucket++) {
                float seconds=bucket*240f;
                int a=(int)sau.invoke(field(field(screen,"cartoonSprites"),"outfitRotation"),seconds);
                int b=(int)ong.invoke(field(field(screen,"cartoonSprites"),"outfitRotation"),seconds);
                if(bucket<6){require(seenSau.add(a) && seenOng.add(b),"Repeated before six");}
                gl(()->{
                    set(screen,"clock",seconds);
                    set(screen,"runMetrics",new com.khuongnd.dexkids.game.RenderRunMetrics());
                    Object current=field(field(screen,"cartoonSprites"),"childOutfitsAtlas");
                    if(atlas[0]==null)atlas[0]=current;else require(atlas[0]==current,"Atlas reloaded on rotation");
                });
                SystemClock.sleep(750);
                gl(()->{
                    var metric=(com.khuongnd.dexkids.game.RenderRunMetrics)field(screen,"runMetrics");
                    var sample=new org.json.JSONObject();sample.put("simulated_seconds",seconds);
                    sample.put("frames",metric.frames());sample.put("average_fps",metric.averageFps());sample.put("p95_upper_ms",metric.p95UpperMs());
                    sample.put("draw_calls",field(screen,"previousFrameDrawCalls"));transitions.put(sample);
                });
            }
            java.nio.file.Files.write(new File(dir,"transitions.json").toPath(),transitions.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            gl(()->{
                require((field(field(screen,"cartoonSprites"),"childOutfitsAtlas")==null)==fallback,"Wrong optional atlas fixture");
                set(screen,"runMetrics",new com.khuongnd.dexkids.game.RenderRunMetrics());
                set(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->false);
            });
            SystemClock.sleep(10000);
            gl(()->{
                Object metric=field(screen,"runMetrics");
                var sample=new org.json.JSONObject();
                for(String method:new String[]{"frames","seconds","averageFps","p95UpperMs"})
                    sample.put(method,metric.getClass().getMethod(method).invoke(metric));
                sample.put("draw_calls",field(screen,"previousFrameDrawCalls"));
                sample.put("managed_textures",com.badlogic.gdx.graphics.Texture.getNumManagedTextures());
                var mem=new android.os.Debug.MemoryInfo();android.os.Debug.getMemoryInfo(mem);sample.put("pss_kib",mem.getTotalPss());
                measurements.put(sample);
                set(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->true);
            });
            float[] paused={0};gl(()->paused[0]=(float)field(screen,"clock"));SystemClock.sleep(500);
            gl(()->require((float)field(screen,"clock")==paused[0],"Pause advances costume clock"));
            gl(()->set(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->false));SystemClock.sleep(500);
            gl(()->require((float)field(screen,"clock")>paused[0],"Resume clock stopped"));
            java.nio.file.Files.write(new File(dir,"performance.json").toPath(),measurements.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // Actual Android Activity/GL recreation; atlas must be disposed and rebuilt safely.
            var recreated=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);
            r.runOnMainSync(()->activities[1].recreate());
            Activity next=r.waitForMonitorWithTimeout(recreated,10000);r.removeMonitor(recreated);
            require(next!=null && next!=activities[1],"Activity recreation absent");activities[1]=next;
            SystemClock.sleep(3000);
            Object recreatedScreen=((KidsGame)field(next,"runningGame")).getScreen();
            gl(()->require((field(field(recreatedScreen,"cartoonSprites"),"childOutfitsAtlas")==null)==fallback,"Atlas recreation failed"));
            capture(r,dir,"after-recreation");
            result.putString("recreation","PASS actual Android Activity/GL recreation");
            result.putString("rotation","six unique costumes each; same atlas object; simulated 240-second boundaries; pause/resume verified");
            result.putInt("captures",captures);
            result.putString("scope",captures+" actual Android FullHD frozen DEMO captures; visual review separate; no network; optional_atlas="+!fallback);
        } finally {
            r.runOnMainSync(()->{for(Activity a:activities)if(a!=null)a.finish();});r.removeMonitor(monitor);
            restore(prefs,old);restore(ai,oldAi);
        }
    }
    /** Two instrument runs separated by adb force-stop verify actual process persistence. */
    static void processAgeRestart(LifecycleValidationRunner r,Bundle result,boolean prepare) throws Exception {
        var context=r.getTargetContext();var p=context.getSharedPreferences("parent_settings",0);
        File backup=new File(context.getFilesDir(),"qa-t053-age-restore.json");
        if(prepare){
            require(!backup.exists(),"Pending age restoration must be completed first");
            var saved=new org.json.JSONObject();
            for(String k:new String[]{"sau_age","ong_age","child_age_5_4_migrated"}) {
                saved.put(k+"_present",p.contains(k));
                saved.put(k,k.equals("child_age_5_4_migrated")?p.getBoolean(k,false):p.getInt(k,-1));
            }
            java.nio.file.Files.write(backup.toPath(),saved.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            require(p.edit().putInt("sau_age",4).putInt("ong_age",5).remove("child_age_5_4_migrated").commit(),"Prepare persistence failed");
            var settings=new com.khuongnd.dexkids.ParentSettings(context);
            require(settings.getSauAge()==5 && settings.getOngAge()==4,"Initial repair failed");
            settings.setSauAge(4);settings.setOngAge(5);
            require(p.edit().commit(),"Flush preferences failed");
            result.putString("ages_process","Prepared marked migration plus parent-selected4/5; force-stop then verify required");
        }else{
            require(backup.exists(),"Missing process-restart age fixture");
            try {
                var settings=new com.khuongnd.dexkids.ParentSettings(context);
                require(settings.getSauAge()==4 && settings.getOngAge()==5,"Migration overwrote parent after process restart");
                require(p.getBoolean("child_age_5_4_migrated",false),"Migration marker not persisted");
                result.putString("ages_process","PASS persisted parent choice after actual adb force-stop/new Android process");
            }finally{
                var saved=new org.json.JSONObject(new String(java.nio.file.Files.readAllBytes(backup.toPath()),java.nio.charset.StandardCharsets.UTF_8));
                var edit=p.edit();
                for(String k:new String[]{"sau_age","ong_age","child_age_5_4_migrated"}) {
                    if(!saved.getBoolean(k+"_present"))edit.remove(k);
                    else if(k.equals("child_age_5_4_migrated"))edit.putBoolean(k,saved.getBoolean(k));
                    else edit.putInt(k,saved.getInt(k));
                }
                require(edit.commit(),"Age restoration failed");require(backup.delete(),"Remove age fixture failed");
            }
        }
    }
    static void verifyAges(android.content.Context context,Bundle result) {
        var p=context.getSharedPreferences("parent_settings",0);var saved=p.getAll();
        try {
            int[][] cases={{-1,-1,5,4},{4,5,5,4},{5,4,5,4},{6,7,6,7}};
            for(int[] c:cases){
                p.edit().remove("child_age_5_4_migrated").remove("sau_age").remove("ong_age").commit();
                if(c[0]>=0)p.edit().putInt("sau_age",c[0]).putInt("ong_age",c[1]).commit();
                var settings=new com.khuongnd.dexkids.ParentSettings(context);
                require(settings.getSauAge()==c[2] && settings.getOngAge()==c[3],"Age migration mismatch");
                require(settings.ageForFocus(com.khuongnd.dexkids.story.EntertainmentDirector.Audience.SAU)==c[2],"SAU focus mismatch");
                require(settings.ageForFocus(com.khuongnd.dexkids.story.EntertainmentDirector.Audience.ONG)==c[3],"ONG focus mismatch");
                require(settings.ageForFocus(com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH)==Math.min(c[2],c[3]),"BOTH age mismatch");
            }
            // Parent deliberately selects reversed learning levels AFTER migration; restart retains them.
            p.edit().putInt("sau_age",4).putInt("ong_age",5).commit();
            var restarted=new com.khuongnd.dexkids.ParentSettings(context);
            require(restarted.getSauAge()==4 && restarted.getOngAge()==5,"Migration reran on restart");
            result.putString("ages","PASS fresh/swapped/correct/custom/restart and per-focus mapping");
        } finally {restore(p,saved);}
    }
}
