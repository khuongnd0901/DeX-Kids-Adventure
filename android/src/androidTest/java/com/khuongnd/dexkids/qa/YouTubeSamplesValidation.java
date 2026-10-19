package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.khuongnd.dexkids.media.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

/** Owner-requested persistent sample links; no playback or parent approval. */
final class YouTubeSamplesValidation {
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static void run(LifecycleValidationRunner runner,Bundle result) throws Exception {
        var base=runner.getTargetContext();
        require(android.os.Build.HARDWARE.equals("ranchu") && runner.targetDisplay()>0,"Emulator external display only");
        var store=new YouTubeStore(base);List<YouTubeVideo> before=store.videos();
        require(store.getPendingId().isEmpty(),"Do not interfere with active playback");
        var switches=new HashMap<>(base.getSharedPreferences("youtube_handoff_v1",0).getAll());
        var privacy=new HashMap<>(base.getSharedPreferences("youtube_embed_v1",0).getAll());
        var missing=YouTubeSampleVideos.INSTANCE.pending().stream().filter(v->before.stream().noneMatch(old->old.videoId().equals(v.videoId()))).toList();
        require(before.size()+missing.size()<=YouTubeLibrary.MAX_VIDEOS,"Room for all samples");
        Activity activity=runner.startActivitySync(new Intent(base,YouTubeLibraryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        try {
            runner.waitForIdleSync();click(runner,activity);
            var first=new YouTubeStore(base).videos();
            require(first.size()==before.size()+missing.size() && first.containsAll(before),"Prior rows preserved exactly");
            require(first.containsAll(missing),"Pending disabled samples persisted");
            require(missing.stream().noneMatch(v->v.approved()||v.enabled()||!v.recall().isEmpty()),"No invented approval/questions");
            closeNotice(runner);click(runner,activity);closeNotice(runner);
            require(new YouTubeStore(base).videos().equals(first),"Repeat import preserves exact records");
            require(switches.equals(base.getSharedPreferences("youtube_handoff_v1",0).getAll()),"Feature/automatic/journal unchanged");
            require(privacy.equals(base.getSharedPreferences("youtube_embed_v1",0).getAll()),"API/privacy settings unchanged");
            runner.runOnMainSync(()->{
                View view=((ViewGroup)activity.getWindow().getDecorView().findViewById(android.R.id.content)).getChildAt(0);
                if(view instanceof android.widget.ScrollView scroll)scroll.fullScroll(View.FOCUS_DOWN);
            });runner.waitForIdleSync();android.os.SystemClock.sleep(350);
            var bitmap=runner.getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).takeScreenshot(activity.getWindow());
            require(bitmap!=null,"Actual library screenshot");
            var directory=new File(base.getFilesDir(),"qa");directory.mkdirs();
            try(var out=new FileOutputStream(new File(directory,"t071-story-samples.png"))){require(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out),"Screenshot saved");}
            finally {bitmap.recycle();}
            result.putInt("added",missing.size());result.putInt("library_total",first.size());
            result.putString("samples","PASS: native import -> persisted pending/disabled links; repeat idempotent; prior rows/privacy/switches unchanged");
        } finally {runner.runOnMainSync(activity::finish);}
    }
    private static void click(LifecycleValidationRunner runner,Activity activity){
        runner.runOnMainSync(()->{Button button=find(activity.getWindow().getDecorView());require(button!=null && button.performClick(),"Native sample-import button");});
        runner.waitForIdleSync();
    }
    private static Button find(View view){
        if(view instanceof Button button && button.getText().toString().startsWith("Thêm video kể chuyện mẫu"))return button;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){Button found=find(group.getChildAt(i));if(found!=null)return found;}
        return null;
    }
    private static void closeNotice(LifecycleValidationRunner runner) throws Exception {
        try(var descriptor=runner.getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).executeShellCommand("input -d "+runner.targetDisplay()+" keyevent 4");
            var input=new android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor)){input.readAllBytes();}
        runner.waitForIdleSync();
    }
}
