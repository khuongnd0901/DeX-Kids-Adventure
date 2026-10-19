package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.khuongnd.dexkids.media.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

/** Actual native parent-review action on the owner's Windows simulator. */
final class YouTubePreviewValidation {
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static void run(LifecycleValidationRunner runner,Bundle result) throws Exception {
        var base=runner.getTargetContext();
        require(android.os.Build.HARDWARE.equals("ranchu") && runner.targetDisplay()>0,"Explicit emulator external display required");
        var store=new YouTubeStore(base);var before=store.videos();
        var video=before.stream().filter(v->v.videoId().equals("TXN0gMj2GZs")).findFirst().orElseThrow();
        require(!video.approved() && !video.enabled() && store.getPendingId().isEmpty(),"Pending sample and no existing playback hold");
        var settings=new YouTubeEmbedSettings(base);require(!settings.key().isBlank(),"Existing private key required");
        var prefs=base.getSharedPreferences("youtube_embed_v1",0);
        var privacy=new HashMap<String,Object>(prefs.getAll());
        var switches=new HashMap<>(base.getSharedPreferences("youtube_handoff_v1",0).getAll());
        Activity[] library={null},preview={null};EmbeddedYouTubeHost[] host={null};
        try {
            settings.configure("",settings.getNotificationReference(),"Parent preview test",settings.getCountry(),true);settings.setSelected(true);
            try{YouTubeMetadata.INSTANCE.validate(base,video,settings.key(),settings.getCountry());throw new AssertionError("Scheduled validation accepted unapproved sample");}
            catch(IllegalArgumentException expected){result.putString("scheduled_gate","PASS: ordinary metadata validation still rejects pending/disabled row");}
            for(int trial=0;trial<3;trial++) {
                library[0]=runner.startActivitySync(new Intent(base,YouTubeLibraryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));runner.waitForIdleSync();
                var monitor=runner.addMonitor(YouTubePreviewActivity.class.getName(),null,false);
                runner.runOnMainSync(()->{Button row=find(library[0].getWindow().getDecorView(),video.title());require(row!=null && row.performClick(),"Native pending-video row");});
                runner.waitForIdleSync();
                require(clickMenu(runner,"Xem thử trong app"),"Actual native preview menu item");
                preview[0]=runner.waitForMonitorWithTimeout(monitor,15_000);runner.removeMonitor(monitor);
                require(preview[0]!=null && preview[0].getDisplay().getDisplayId()==runner.targetDisplay(),"Preview on requested external display");
                host[0]=(EmbeddedYouTubeHost)YouTubeHandoffValidation.field(preview[0],"host");require(host[0]!=null,"Production preview host created");
                var session=(EmbeddedYouTubeSession)YouTubeHandoffValidation.field(host[0],"session");
                long started=SystemClock.elapsedRealtime();boolean played=false;
                while(SystemClock.elapsedRealtime()-started<180_000){
                    runner.runOnMainSync(()->{});
                    if(session.finished())break;
                    if(session.state()==EmbeddedYouTubeSession.State.PLAYING && !played){
                        played=true;capture(runner,preview[0],"t071-preview-"+trial+".png");
                        if(trial==1)runner.runOnMainSync(()->preview[0].dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,android.view.KeyEvent.KEYCODE_F10)));
                        if(trial==2)runner.startActivitySync(new Intent(base,com.khuongnd.dexkids.ParentActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                    SystemClock.sleep(200);
                }
                result.putString("trial_"+trial+"_state",session.state().name());
                result.putLong("trial_"+trial+"_elapsed_ms",SystemClock.elapsedRealtime()-started);
                require(played,"Real preview never reported PLAYING");
                require(session.state()==(trial==0?EmbeddedYouTubeSession.State.ENDED:EmbeddedYouTubeSession.State.CANCELLED),"Expected end/F10/background teardown");
                runner.waitForIdleSync();
                require(preview[0].isFinishing()||preview[0].isDestroyed(),"Parent preview closes");
                require((Boolean)YouTubeHandoffValidation.field(host[0],"disposed"),"Production player disposed");
                require(store.videos().equals(before) && switches.equals(base.getSharedPreferences("youtube_handoff_v1",0).getAll()),"Review leaves approval, switches, budget and journal unchanged");
                if(trial==0){
                    for(int attempt=0;attempt<30 && !library[0].hasWindowFocus();attempt++)SystemClock.sleep(100);
                    require(library[0].hasWindowFocus(),"Natural end returns to original library");capture(runner,library[0],"t071-preview-return.png");
                }
                result.putString("trial_"+trial,"PASS: native menu, real pending-video playback, "+session.state()+", preview closed, catalog/switches unchanged");
                runner.runOnMainSync(()->library[0].finish());runner.waitForIdleSync();
            }
        } finally {
            runner.runOnMainSync(()->{if(host[0]!=null)host[0].dispose();if(preview[0]!=null)preview[0].finish();if(library[0]!=null)library[0].finish();});runner.waitForIdleSync();
            var edit=prefs.edit().clear();
            for(var entry:privacy.entrySet()){
                Object value=entry.getValue();String key=entry.getKey();
                if(value instanceof String v)edit.putString(key,v);else if(value instanceof Boolean v)edit.putBoolean(key,v);
                else if(value instanceof Integer v)edit.putInt(key,v);else if(value instanceof Long v)edit.putLong(key,v);
                else throw new AssertionError("Unexpected privacy preference type");
            }
            require(edit.commit() && prefs.getAll().equals(privacy),"Original privacy/key settings restored exactly");
        }
    }
    private static Button find(View view,String text){
        if(view instanceof Button button && button.getText().toString().contains(text))return button;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){Button found=find(group.getChildAt(i),text);if(found!=null)return found;}
        return null;
    }
    private static boolean clickMenu(LifecycleValidationRunner runner,String text){
        var automation=runner.getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        var service=automation.getServiceInfo();
        if((service.flags & android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS)==0){
            service.flags|=android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;automation.setServiceInfo(service);
        }
        // Window/accessibility publication may trail Activity idle during a return transition.
        for(int attempt=0;attempt<30;attempt++){
            automation.clearCache();
            var windows=automation.getWindowsOnAllDisplays().get(runner.targetDisplay());
            if(windows!=null)for(var window:windows){var root=window.getRoot();if(root==null)continue;
                for(var node:root.findAccessibilityNodeInfosByText(text))if(node.getText()!=null && text.contentEquals(node.getText())){
                    for(int i=0;i<3 && node!=null;i++,node=node.getParent())if(node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK))return true;
                }
            }
            SystemClock.sleep(200);
        }
        return false;
    }
    private static void capture(LifecycleValidationRunner runner,Activity activity,String name) throws Exception {
        var bitmap=runner.getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).takeScreenshot(activity.getWindow());require(bitmap!=null,"Actual preview screenshot");
        var directory=new File(activity.getFilesDir(),"qa");directory.mkdirs();
        try(var output=new FileOutputStream(new File(directory,name))){require(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,output),"Screenshot saved");}
        finally{bitmap.recycle();}
    }
}
