package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.khuongnd.dexkids.KidsActivity;
import com.khuongnd.dexkids.media.*;
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/** No YouTube video is loaded. Storage is isolated; temporary actual hold is restored. */
final class YouTubeHandoffValidation {
    static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static Object field(Object object,String name) throws Exception {
        var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);
    }
    private static File backupFile(Context base){return new File(base.getFilesDir(),"qa-t071-youtube-backup.json");}
    private static void restoreScheduled(Context base) throws Exception {
        File backup=backupFile(base);if(!backup.exists())return;
        var data=new org.json.JSONObject(new String(Files.readAllBytes(backup.toPath()),java.nio.charset.StandardCharsets.UTF_8));
        var prefs=base.getSharedPreferences("youtube_handoff_v1",0);var edit=prefs.edit().clear();
        var values=data.getJSONObject("prefs");var keys=values.keys();
        while(keys.hasNext()){
            String key=keys.next();var row=values.getJSONObject(key);String value=row.getString("value");
            switch(row.getString("type")){
                case "Boolean":edit.putBoolean(key,Boolean.parseBoolean(value));break;
                case "Integer":edit.putInt(key,Integer.parseInt(value));break;
                case "Long":edit.putLong(key,Long.parseLong(value));break;
                case "String":edit.putString(key,value);break;
                default:throw new AssertionError("Unsupported backup type");
            }
        }
        android.util.AtomicFile catalog=new android.util.AtomicFile(new File(base.getFilesDir(),"youtube-library-v1.json"));
        if(data.getBoolean("hadCatalog")){
            var stream=catalog.startWrite();try{stream.write(android.util.Base64.decode(data.getString("catalog"),android.util.Base64.NO_WRAP));catalog.finishWrite(stream);}
            catch(Exception error){catalog.failWrite(stream);throw error;}
        }else catalog.delete();
        require(edit.commit(),"Restore actual YouTube preferences");
        require(backup.delete(),"Remove completed test backup");
    }
    /** Temporary reviewed test fixture; calls the real scheduler/launcher at a synthetic five-card boundary. */
    static void scheduled(LifecycleValidationRunner runner,Bundle result,int displayId,String url) throws Exception {
        require(displayId>0,"External display required");
        Context base=runner.getTargetContext();YouTubeStore store=new YouTubeStore(base);
        require(store.getPendingId().isEmpty(),"Real pending handoff must be resolved first");
        File backup=backupFile(base);require(!backup.exists(),"Previous scheduled test must be restored first");
        var data=new org.json.JSONObject();var values=new org.json.JSONObject();
        for(var item:base.getSharedPreferences("youtube_handoff_v1",0).getAll().entrySet()){
            Object value=item.getValue();String type=value.getClass().getSimpleName();
            require(Set.of("Boolean","Integer","Long","String").contains(type),"Supported preference backup");
            values.put(item.getKey(),new org.json.JSONObject().put("type",type).put("value",value.toString()));
        }
        data.put("prefs",values);File catalog=new File(base.getFilesDir(),"youtube-library-v1.json");
        data.put("hadCatalog",catalog.exists());
        if(catalog.exists())data.put("catalog",android.util.Base64.encodeToString(Files.readAllBytes(catalog.toPath()),android.util.Base64.NO_WRAP));
        Files.write(backup.toPath(),data.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String id=YouTubeVideo.idFromInput(url);
        // This approval belongs to the explicit QA fixture and is removed on confirmed return.
        YouTubeVideo fixture=new YouTubeVideo(id,"OWNER TEST ONLY — restored after QA",Audience.BOTH,4,7,300,true,true,"Con nhớ nhân vật nào?");
        store.save(List.of(fixture));store.acceptNotice();store.setEnabled(true);store.setAutomatic(true);store.setEvery(2);store.advance(1);
        base.getSharedPreferences("youtube_handoff_v1",0).edit().putString("launches","[]").commit();
        store.begin(fixture);
        Activity child=runner.startActivitySync(new Intent(base,KidsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));runner.waitForIdleSync();
        runner.runOnMainSync(()->{try{
            store.clearPending();
            var hold=child.getClass().getDeclaredField("youtubeHold");hold.setAccessible(true);hold.set(child,false);
            var parent=child.getClass().getDeclaredField("parentMenuOpen");parent.setAccessible(true);parent.set(child,false);
            AlertDialog dialog=(AlertDialog)field(child,"youtubeReturnDialog");dialog.dismiss();
            var dialogField=child.getClass().getDeclaredField("youtubeReturnDialog");dialogField.setAccessible(true);dialogField.set(child,null);
            var count=child.getClass().getDeclaredField("questionsSinceStory");count.setAccessible(true);count.set(child,5);
            var method=child.getClass().getDeclaredMethod("tryYouTubeInterlude");method.setAccessible(true);
            var embedSettings=new YouTubeEmbedSettings(base);boolean priorEmbed=embedSettings.getSelected();
            try { embedSettings.setSelected(false);require((Boolean)method.invoke(child),"Real scheduler requested official YouTube"); }
            finally { embedSettings.setSelected(priorEmbed); }
            require(store.getPendingId().equals(id),"Real scheduler wrote durable hold");
            require(store.launches().size()==1 && store.getLastVideoId().equals(id),"Real scheduler persisted request and rotation");
            require(((Number)count.get(child)).intValue()==0,"Real boundary consumed exactly once");
        }catch(Exception error){throw new RuntimeException(error);}});
        result.putString("scheduled","LAUNCH_REQUESTED: production hook, temporary owner-URL fixture, synthetic 5-card boundary; backup restored only after paused/acknowledged return");
        result.putInt("source_display",displayId);result.putString("test_video_id",id);
    }
    /** Explicit owner-supplied URL only. Launch leaves a durable hold; return requires test operator to pause YouTube first. */
    static void live(LifecycleValidationRunner runner,Bundle result,int displayId,String url,boolean returning) throws Exception {
        require(displayId>0,"Explicit external display required");
        String id=YouTubeVideo.idFromInput(url);
        YouTubeStore store=new YouTubeStore(runner.getTargetContext());
        if(returning)require(store.getPendingId().equals(id),"Expected owner-approved test handoff");
        else {
            require(store.getPendingId().isEmpty(),"Unresolved parent handoff");
            store.begin(new YouTubeVideo(id,"Owner-provided test URL — not a library approval",Audience.BOTH,4,7,60,false,false,""));
        }
        Activity child=runner.startActivitySync(new Intent(runner.getTargetContext(),KidsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        runner.waitForIdleSync();android.os.SystemClock.sleep(1200);
        runner.runOnMainSync(()->{try{
            require((Boolean)field(child,"youtubeHold"),"Durable audio hold active");
            require(!(Boolean)field(child,"speechStarted"),"Local narration stopped");
            require((Boolean)field(field(child,"soundscape"),"paused"),"Local soundscape stopped");
            if(returning){
                AlertDialog dialog=(AlertDialog)field(child,"youtubeReturnDialog");
                require(dialog!=null && dialog.isShowing(),"Return acknowledgement visible");
                require(dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick(),"Test operator confirms official player stopped");

            }else{
                YouTubeVideo test=new YouTubeVideo(id,"Owner-provided test URL",Audience.BOTH,4,7,60,false,false,"");
                require(OfficialYouTube.INSTANCE.preflight(child,test)==null,"Official YouTube preflight failed");
                OfficialYouTube.INSTANCE.launch(child,test);
            }
        }catch(Exception e){throw new RuntimeException(e);}});
        if(returning){
            runner.waitForIdleSync();
            runner.runOnMainSync(()->{try{
                require(store.getPendingId().isEmpty(),"Return journal released");
                require(!(Boolean)field(child,"youtubeHold"),"Journey released exactly once");child.finish();
            }catch(Exception e){throw new RuntimeException(e);}});
        }
        if(returning){runner.waitForIdleSync();restoreScheduled(runner.getTargetContext());}
        result.putString("youtube_phase",returning?"RETURN_ACKNOWLEDGED":"LAUNCH_REQUESTED");
        result.putString("test_video_id",id);result.putInt("source_display",displayId);
        result.putString("scope","Explicit test only; library/enable/automatic settings unchanged; no watched/end/Premium assertion");
    }
    /** Native explicit-paste action with Vietnamese hardware IME unchanged; original clipboard is opaque/restored. */
    static void paste(LifecycleValidationRunner runner,Bundle result,int displayId,String url) throws Exception {
        require(displayId>0,"External display required");String id=YouTubeVideo.idFromInput(url);
        Activity activity=runner.startActivitySync(new Intent(runner.getTargetContext(),YouTubeLibraryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        android.content.ClipboardManager clipboard=activity.getSystemService(android.content.ClipboardManager.class);
        android.content.ClipData[] previous={null};boolean[] captured={false};
        try{
            runner.waitForIdleSync();
            runner.runOnMainSync(()->{
                previous[0]=clipboard.getPrimaryClip();captured[0]=true;
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("T071 explicit test URL",url));
                android.widget.Button add=findButton(activity.getWindow().getDecorView(),"Thêm link video");
                require(add!=null && add.performClick(),"Open native video editor");
            });
            runner.waitForIdleSync();
            var automation=runner.getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            var root=automation.getRootInActiveWindow();require(root!=null,"Editor accessibility root");
            boolean clicked=false;
            for(var node:root.findAccessibilityNodeInfosByText("Dán link/ID từ bộ nhớ tạm")){
                if(node.isClickable() && node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)){clicked=true;break;}
            }
            require(clicked,"Explicit parent paste button clicked");runner.waitForIdleSync();
            root=automation.getRootInActiveWindow();boolean exact=false;
            for(var node:root.findAccessibilityNodeInfosByText("https://www.youtube.com/watch?v="+id))
                if("android.widget.EditText".contentEquals(node.getClassName()) && ("https://www.youtube.com/watch?v="+id).contentEquals(node.getText()))exact=true;
            require(exact,"Native paste preserved exact case-sensitive canonical video URL");
            result.putString("url_paste","PASS: actual native paste action preserves exact owner-supplied URL; no IME/global keyboard change, no catalog save");
            result.putInt("external_display",displayId);result.putString("test_video_id",id);
        }finally{
            runner.runOnMainSync(()->{
                if(captured[0]){if(previous[0]==null)clipboard.clearPrimaryClip();else clipboard.setPrimaryClip(previous[0]);}
                activity.finish();
            });runner.waitForIdleSync();
        }
    }
    private static android.widget.Button findButton(android.view.View view,String text){
        if(view instanceof android.widget.Button && text.contentEquals(((android.widget.Button)view).getText()))return (android.widget.Button)view;
        if(view instanceof android.view.ViewGroup){var group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++){var found=findButton(group.getChildAt(i),text);if(found!=null)return found;}}
        return null;
    }
    /** Actual foreground GPS listener and bundled synthetic GPX; never records coordinates. */
    static void journeys(LifecycleValidationRunner runner,Bundle result,int displayId) throws Exception {
        require(displayId>0,"External display required");
        Context base=runner.getTargetContext();YouTubeStore store=new YouTubeStore(base);
        require(store.getPendingId().isEmpty(),"Unresolved real handoff");
        for(int scenario=0;scenario<3;scenario++){
            final boolean live=scenario==0;
            final boolean wasRunning=scenario==1;
            Activity child=null;
            try{
                store.begin(new YouTubeVideo("abcdefghijk","QA ONLY",Audience.BOTH,4,7,60,false,false,""),wasRunning);
                Intent launch=new Intent(base,KidsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launch.putExtra(live?KidsActivity.EXTRA_LIVE_GPS:KidsActivity.EXTRA_HCM_SAMPLE,true);
                child=runner.startActivitySync(launch);runner.waitForIdleSync();
                Activity current=child;
                com.khuongnd.dexkids.journey.GpxReplayFeed replay=null;
                if(!live){
                    long deadline=android.os.SystemClock.elapsedRealtime()+10000;
                    while(android.os.SystemClock.elapsedRealtime()<deadline){
                        var feed=(com.khuongnd.dexkids.journey.DeferredGpxJourneyFeed)field(current,"journeyFeed");
                        replay=feed.replay();if(replay!=null)break;android.os.SystemClock.sleep(100);
                    }
                    require(replay!=null,"Bundled synthetic GPX loaded");
                    require(replay.isPaused(),"GPX paused during durable hold");
                    double before=replay.replayTimeSeconds();android.os.SystemClock.sleep(500);
                    require(replay.replayTimeSeconds()==before,"GPX timeline frozen during hold");
                }else{
                    require(field(field(current,"gps"),"listener")==null,"App GPS listener stopped during hold");
                }
                runner.runOnMainSync(()->{try{
                    AlertDialog dialog=(AlertDialog)field(current,"youtubeReturnDialog");
                    require(dialog!=null && dialog.isShowing(),"Parent return acknowledgement visible");
                    require(dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick(),"Acknowledge stopped video");
                }catch(Exception e){throw new RuntimeException(e);}});
                runner.waitForIdleSync();android.os.SystemClock.sleep(700);
                require(!(Boolean)field(current,"youtubeHold"),"Hold released");
                if(live)require(field(field(current,"gps"),"listener")!=null,"Foreground app GPS listener registered again");
                else require(replay.isPaused()!=wasRunning,"GPX restores only the previously running state");
                result.putString(live?"live_gps":wasRunning?"gpx_running":"gpx_manual_pause", "PASS: real Android hold and explicit acknowledgement restore correct state; no road/accuracy claim");
            }finally{
                if(child!=null){Activity ending=child;runner.runOnMainSync(ending::finish);runner.waitForIdleSync();}
                store.clearPending();
            }
        }
        result.putInt("external_display",displayId);
    }
    static void run(LifecycleValidationRunner runner,Bundle result,int displayId) throws Exception {
        Context base=runner.getTargetContext();
        String prefix="t071-youtube-"+System.nanoTime();
        File root=new File(base.getCacheDir(),prefix);require(root.mkdirs(),"Isolated root");
        Context isolated=new ContextWrapper(base){
            @Override public String getPackageName(){return base.getPackageName()+".qa.isolated";}
            @Override public File getFilesDir(){return root;}
            @Override public SharedPreferences getSharedPreferences(String name,int mode){return base.getSharedPreferences(prefix+name,mode);}
        };
        YouTubeVideo fixture=new YouTubeVideo("abcdefghijk","QA ONLY — NO VIDEO REQUEST",Audience.BOTH,4,7,300,true,true,"");
        try {
            YouTubeStore store=new YouTubeStore(isolated);
            require(!store.getEnabled() && !store.getAutomatic() && !store.getConsented(),"Default OFF");
            require(!store.getManagedStop(),"Managed media control defaults OFF");
            store.setEnabled(true);require(!store.getEnabled(),"No consent bypass");
            store.acceptNotice();store.setEnabled(true);store.setAutomatic(true);store.setEvery(4);
            store.save(List.of(fixture));
            YouTubeStore reopened=new YouTubeStore(isolated);
            require(reopened.getEnabled() && reopened.getAutomatic() && reopened.getEvery()==4,"Settings persisted");
            require(reopened.videos().equals(List.of(fixture)),"Catalog disk reload");
            byte[] before=Files.readAllBytes(new File(root,"youtube-library-v1.json").toPath());
            try{store.save(List.of(fixture,fixture));throw new AssertionError("Duplicate saved");}catch(IllegalArgumentException expected){}
            require(Arrays.equals(before,Files.readAllBytes(new File(root,"youtube-library-v1.json").toPath())),"Invalid save retains exact old file");
            android.util.AtomicFile atomic=new android.util.AtomicFile(new File(root,"youtube-library-v1.json"));
            FileOutputStream interrupted=atomic.startWrite();interrupted.write("partial".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            atomic.failWrite(interrupted);
            require(reopened.videos().equals(List.of(fixture)),"Atomic failure retains approved catalog");
            var setup=new YouTubeEmbedSettings(isolated);
            require(setup.getSelected() && !setup.ready(),"Embedded defaults selected but fail-closed until configured");
            String fakeKey="TEST_ONLY_"+"x".repeat(30);
            setup.configure(fakeKey,"","", "VN",false);
            require(!setup.ready() && setup.key().equals(fakeKey),"Key encryption roundtrip without enabling network");
            require(!isolated.getSharedPreferences("youtube_embed_v1",0).getString("key","").contains(fakeKey),"No plaintext key in preferences");
            setup.configure("","","", "VN",true);
            require(!setup.ready(),"Missing contact cannot enable personal playback");
            setup.configure("","","qa@example.invalid","VN",false);
            require(!setup.ready(),"Privacy consent still required without Google reference");
            setup.configure("","","qa@example.invalid","VN",true);
            require(new YouTubeEmbedSettings(isolated).ready() && setup.getNotificationReference().isEmpty(),"Personal readiness reload without fabricated Google reference");
            setup.delete();require(!setup.ready() && setup.key().isEmpty() && new YouTubeEmbedSettings(isolated).key().isEmpty(),"Delete removes key/readiness without reimporting private debug seed");
            store.beginEmbedded(fixture,true);
            require(store.getPendingEmbedded() && store.getReplayWasRunning(),"Embedded ownership persisted");
            store.markExternalPending();require(!store.getPendingEmbedded(),"Explicit native link preserves external audio hold");
            store.clearPending();
            store.begin(fixture);store.requested(fixture,10_000_000L);
            require(new YouTubeStore(isolated).getPendingId().equals(fixture.videoId()),"Durable pending handoff");
            require(new YouTubeStore(isolated).launches().size()==1,"Durable request budget");
            try{store.begin(fixture);throw new AssertionError("Double owner acquired");}catch(IllegalStateException expected){}
            store.setManagedStop(true);
            long beforeArm=android.os.SystemClock.elapsedRealtime();
            store.armReturn(123,2,300);
            YouTubeStore armed=new YouTubeStore(isolated);
            require(armed.getManagedStop() && armed.getReturnTask()==123 && armed.getReturnDisplay()==2,"Scoped return journal reload");
            require(armed.getDeadline()>=beforeArm+300000 && armed.getDeadline()<=android.os.SystemClock.elapsedRealtime()+300000,"Monotonic deadline persisted");
            store.clearPending();require(new YouTubeStore(isolated).getPendingId().isEmpty(),"Release persisted");
            require(armed.getDeadline()==0 && armed.getReturnTask()==-1 && armed.getReturnDisplay()==-1,"Release cancels timed return");
            store.deleteLibrary();require(!store.getEnabled() && !store.getAutomatic() && store.videos().isEmpty(),"Delete disables feature");
            result.putString("storage","PASS: default OFF, notice, atomic retention, reload, journal, budget, single owner, delete");
        }finally{
            base.getSharedPreferences(prefix+"youtube_handoff_v1",0).edit().clear().commit();
            base.getSharedPreferences(prefix+"youtube_embed_v1",0).edit().clear().commit();
            var keys=java.security.KeyStore.getInstance("AndroidKeyStore");keys.load(null);
            keys.deleteEntry(isolated.getPackageName()+".youtube_embed_key_v1");
            for(File file:root.listFiles())file.delete();root.delete();
        }
        require(displayId>0,"Explicit external display required");
        SharedPreferences prefs=base.getSharedPreferences("youtube_handoff_v1",0);
        Map<String,?> saved=new HashMap<>(prefs.getAll());
        // Do not interfere with a parent's real unresolved playback session.
        require(prefs.getString("pending","").isEmpty(),"Existing YouTube handoff requires parent acknowledgement");
        Activity[] child={null};
        try{
            require(prefs.edit().putString("pending",fixture.videoId()).commit(),"Install temporary hold");
            runner.runOnMainSync(()->{
                var options=android.app.ActivityOptions.makeBasic();options.setLaunchDisplayId(displayId);
                base.startActivity(new Intent(base,KidsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),options.toBundle());
            });
            android.os.SystemClock.sleep(2500);
            runner.runOnMainSync(()->{try{
                var current=com.khuongnd.dexkids.KidsSessionControl.INSTANCE;
                var ref=field(current,"activity");child[0]=(Activity)((java.lang.ref.WeakReference<?>)ref).get();
            }catch(Exception e){throw new RuntimeException(e);}});
            require(child[0]!=null,"Child active");
            var monitor=runner.addMonitor(KidsActivity.class.getName(),null,false);
            runner.runOnMainSync(()->child[0].recreate());
            Activity recreated=runner.waitForMonitorWithTimeout(monitor,10000);
            runner.removeMonitor(monitor);
            require(recreated!=null,"Activity recreation completed");child[0]=recreated;
            runner.waitForIdleSync();android.os.SystemClock.sleep(500);
            require(child[0].getDisplay().getDisplayId()==displayId,"Same external display");
            runner.runOnMainSync(()->{try{
                require((Boolean)field(child[0],"youtubeHold"),"Hold restored before audio");
                require((Boolean)field(child[0],"parentMenuOpen"),"Journey paused");
                require(!(Boolean)field(child[0],"speechStarted"),"No narration");
                require((Boolean)field(field(child[0],"soundscape"),"paused"),"No soundscape");
                AlertDialog dialog=(AlertDialog)field(child[0],"youtubeReturnDialog");
                require(dialog!=null && dialog.isShowing(),"Parent acknowledgement visible");
                // IPC resume must not clear the durable hold.
                ((KidsActivity)child[0]).resumeFromTrustedController();
                require((Boolean)field(child[0],"youtubeHold"),"IPC cannot bypass parent acknowledgement");
                require(dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick(),"Confirm stopped");
            }catch(Exception e){throw new RuntimeException(e);}});
            runner.waitForIdleSync();
            runner.runOnMainSync(()->{try{
                require(!(Boolean)field(child[0],"youtubeHold"),"Explicit acknowledgement releases hold");
            }catch(Exception e){throw new RuntimeException(e);}});
            require(prefs.getString("pending","").isEmpty(),"Pending cleared durably");
            result.putInt("external_display",displayId);
            result.putString("return_hold","PASS: actual KidsActivity recreated with durable hold, stopped audio, refused IPC bypass, explicit parent release");
            result.putString("official_playback","NOT TESTED IN THIS MODE: no Google/YouTube video request; explicit URL playback tested separately");
        }finally{
            if(child[0]!=null)runner.runOnMainSync(()->child[0].finish());
            runner.waitForIdleSync();
            var edit=prefs.edit().clear();
            for(var item:saved.entrySet()){
                Object v=item.getValue();String k=item.getKey();
                if(v instanceof String)edit.putString(k,(String)v);
                else if(v instanceof Boolean)edit.putBoolean(k,(Boolean)v);
                else if(v instanceof Integer)edit.putInt(k,(Integer)v);
                else if(v instanceof Long)edit.putLong(k,(Long)v);
                else if(v instanceof Float)edit.putFloat(k,(Float)v);
                else throw new AssertionError("Unsupported saved preference type");
            }
            require(edit.commit(),"Restore original YouTube settings");
            require(prefs.getAll().equals(saved),"Exact YouTube settings restored");
        }
    }
}
