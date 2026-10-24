package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.khuongnd.dexkids.media.*;
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Explicit adult network test of the unmodified production host. */
final class YouTubeLiveEmbedValidation {
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static void journey(LifecycleValidationRunner runner,Bundle result,String url) throws Exception {
        var base=runner.getTargetContext();
        runner.requireYouTubeTestDisplay();
        var settings=new YouTubeEmbedSettings(base);var store=new YouTubeStore(base);
        require(!settings.key().isBlank() && store.getPendingId().isEmpty(),"Key required; no existing parent hold");
        var prefs=base.getSharedPreferences("youtube_handoff_v1",0);
        var saved=new java.util.HashMap<String,Object>(prefs.getAll());
        boolean consent=settings.getConsented(),selected=settings.getSelected();
        String reference=settings.getNotificationReference(),contact=settings.getContact(),country=settings.getCountry();
        Activity child=null;EmbeddedYouTubeHost[] active={null};
        try {
            settings.configure("","","Parent: personal family device","VN",true);settings.setSelected(true);
            child=runner.startActivitySync(new Intent(base,com.khuongnd.dexkids.KidsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            runner.waitForIdleSync();SystemClock.sleep(1000);
            final Activity target=child;
            var video=new YouTubeVideo(YouTubeVideo.idFromInput(url),"OWNER JOURNEY TEST ONLY",Audience.BOTH,4,7,300,true,true,"");
            for(int trial=0;trial<2;trial++) {
                runner.runOnMainSync(()->{try{
                    var start=target.getClass().getDeclaredMethod("startEmbeddedYouTube",YouTubeVideo.class);start.setAccessible(true);
                    require((Boolean)start.invoke(target,video),"Actual KidsActivity starts embedded turn");
                    active[0]=(EmbeddedYouTubeHost)YouTubeHandoffValidation.field(target,"embeddedYouTube");
                    require((Boolean)YouTubeHandoffValidation.field(target,"youtubeHold") && store.getPendingEmbedded(),"Actual child audio/journey hold acquired");
                }catch(Exception error){throw new RuntimeException(error);}});
                var session=(EmbeddedYouTubeSession)YouTubeHandoffValidation.field(active[0],"session");
                long started=SystemClock.elapsedRealtime();boolean playing=false;
                while(SystemClock.elapsedRealtime()-started<330_000) {
                    runner.runOnMainSync(()->{}); // Publish main-thread player transitions before observing.
                    if(session.finished())break;
                    if(session.state()==EmbeddedYouTubeSession.State.PLAYING && !playing){
                        playing=true;capture(runner,target,"t071-journey-"+trial+".png");
                        if(trial==1)runner.runOnMainSync(()->target.dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,android.view.KeyEvent.KEYCODE_F10)));
                    }
                    SystemClock.sleep(250);
                }
                result.putString("journey_"+trial+"_state",session.state().name());
                result.putLong("journey_"+trial+"_elapsed_ms",SystemClock.elapsedRealtime()-started);
                require(playing,"Real journey player never entered PLAYING");
                require(session.state()==(trial==0?EmbeddedYouTubeSession.State.ENDED:EmbeddedYouTubeSession.State.CANCELLED),"Expected natural end / F10 terminal");
                runner.waitForIdleSync();
                runner.runOnMainSync(()->{try{
                    require(YouTubeHandoffValidation.field(target,"embeddedYouTube")==null,"Child host released");
                    require(!(Boolean)YouTubeHandoffValidation.field(target,"youtubeHold") && store.getPendingId().isEmpty(),"Actual durable child hold released");
                    require(!hasWebView(target.getWindow().getDecorView()),"Actual child player removed");
                    require(target.getDisplay().getDisplayId()==runner.targetDisplay(),"Same external display");
                    if(session.state()==EmbeddedYouTubeSession.State.ENDED){
                        require(!(Boolean)YouTubeHandoffValidation.field(target,"parentMenuOpen"),"Learning flow restored after natural end");
                        require(!(Boolean)YouTubeHandoffValidation.field(YouTubeHandoffValidation.field(target,"soundscape"),"paused"),"App soundscape released after natural end");
                    }
                }catch(Exception error){throw new RuntimeException(error);}});
                result.putString("journey_"+trial,"PASS: actual KidsActivity hold, real playback, "+session.state()+", durable hold cleared, host removed");
                if(trial==0)capture(runner,target,"t071-journey-return.png");
            }
        } finally {
            Activity target=child;
            runner.runOnMainSync(()->{if(active[0]!=null)active[0].dispose();if(target!=null)target.finish();});
            settings.configure("",reference,contact,country,consent);settings.setSelected(selected);
            var edit=prefs.edit().clear();
            for(var row:saved.entrySet()){
                Object value=row.getValue();String key=row.getKey();
                if(value instanceof String v)edit.putString(key,v);else if(value instanceof Boolean v)edit.putBoolean(key,v);
                else if(value instanceof Integer v)edit.putInt(key,v);else if(value instanceof Long v)edit.putLong(key,v);
                else if(value instanceof Float v)edit.putFloat(key,v);else throw new AssertionError("Unsupported saved preference");
            }
            require(edit.commit() && prefs.getAll().equals(saved),"Exact original library switches/journal/budget restored");
        }
    }
    static void run(LifecycleValidationRunner runner,Bundle result,String url) throws Exception {
        var base=runner.getTargetContext();
        runner.requireYouTubeTestDisplay();
        require(runner.targetDisplay()>0,"Explicit external display required");
        var settings=new YouTubeEmbedSettings(base);
        require(!settings.key().isBlank(),"Private debug key must be provisioned");
        boolean consent=settings.getConsented(),selected=settings.getSelected();
        String reference=settings.getNotificationReference(),contact=settings.getContact(),country=settings.getCountry();
        Activity activity=null;
        EmbeddedYouTubeHost[] host={null};
        try {
            // Owner explicitly requested the adult personal-use test. No registration is invented.
            settings.configure("","","Parent: personal family device","VN",true);
            settings.setSelected(true);
            require(new YouTubeEmbedSettings(base).ready() && settings.getNotificationReference().isBlank(),"Personal readiness without Google reference");
            result.putString("configuration","PASS: personal privacy opt-in, empty notification reference; restored after test");
            activity=runner.startActivitySync(new Intent(base,YouTubeEmbedSetupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            runner.waitForIdleSync();
            require(OfficialYouTube.INSTANCE.externalDisplay(activity)!=null,"Production external display check");
            var video=new YouTubeVideo(YouTubeVideo.idFromInput(url),"OWNER LIVE TEST ONLY",Audience.BOTH,4,7,300,true,true,"");
            final Activity target=activity;
            for(int trial=0;trial<2;trial++) {
                var terminal=new AtomicReference<String>();var calls=new AtomicInteger();
                runner.runOnMainSync(()->{
                    host[0]=new EmbeddedYouTubeHost(target,video,reason->{calls.incrementAndGet();terminal.set(reason);return kotlin.Unit.INSTANCE;},uri->{terminal.set("external");return kotlin.Unit.INSTANCE;});
                    host[0].start();
                });
                long started=SystemClock.elapsedRealtime();boolean playing=false,captured=false;
                while(terminal.get()==null && SystemClock.elapsedRealtime()-started<330_000) {
                    boolean[] active={false};
                    runner.runOnMainSync(()->active[0]=contains(target.getWindow().getDecorView(),"YouTube · OWNER LIVE TEST ONLY"));
                    if(active[0] && !playing){playing=true;result.putString("trial_"+trial+"_playing","OBSERVED real production IFrame PLAYING");}
                    if(!captured && (playing || SystemClock.elapsedRealtime()-started>20_000)){
                        capture(runner,target,"t071-live-"+trial+".png");captured=true;
                    }
                    if(trial==0 && playing){runner.runOnMainSync(()->host[0].finish("cancelled"));}
                    SystemClock.sleep(250);
                }
                result.putString("trial_"+trial+"_terminal",String.valueOf(terminal.get()));
                result.putLong("trial_"+trial+"_elapsed_ms",SystemClock.elapsedRealtime()-started);
                require(playing,"Real player never reported PLAYING; terminal="+terminal.get());
                require((trial==0?"cancelled":"ended").equals(terminal.get()),"Unexpected production terminal="+terminal.get());
                runner.runOnMainSync(()->{host[0].finish("cancelled");require(!hasWebView(target.getWindow().getDecorView()),"Disposed player WebView removed");});
                require(calls.get()==1,"Exactly one terminal callback");
                result.putString("trial_"+trial,"PASS: real PLAYING -> "+terminal.get()+", once, WebView removed");
            }
            result.putString("live_playback","PASS: actual YouTube network player; cancelled and natural ENDED");
        } finally {
            Activity target=activity;
            runner.runOnMainSync(()->{if(host[0]!=null)host[0].dispose();if(target!=null)target.finish();});
            settings.configure("",reference,contact,country,consent);settings.setSelected(selected);
        }
    }
    private static boolean contains(View view,String text){
        if(view instanceof TextView label && label.getText().toString().contains(text))return true;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++)if(contains(group.getChildAt(i),text))return true;
        return false;
    }
    private static boolean hasWebView(View view){
        if(view instanceof android.webkit.WebView)return true;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++)if(hasWebView(group.getChildAt(i)))return true;
        return false;
    }
    private static void capture(LifecycleValidationRunner runner,Activity activity,String name) throws Exception {
        Bitmap bitmap=runner.getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).takeScreenshot(activity.getWindow());
        require(bitmap!=null,"Actual test window screenshot");
        File directory=new File(activity.getFilesDir(),"qa");directory.mkdirs();
        try(var output=new FileOutputStream(new File(directory,name))){require(bitmap.compress(Bitmap.CompressFormat.PNG,100,output),"Screenshot saved");}
        finally {bitmap.recycle();}
    }
}
