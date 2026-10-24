package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.khuongnd.dexkids.media.*;
import java.util.HashMap;
import java.util.List;

/** Owner-authorized question fill through the native UI; no video approval or playback. */
final class YouTubeRecallValidation {
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static void run(LifecycleValidationRunner runner,Bundle result) throws Exception {
        runner.requireYouTubeTestDisplay();var base=runner.getTargetContext();var store=new YouTubeStore(base);
        require(store.getPendingId().isEmpty(),"No unresolved playback hold");
        var before=store.videos();var expected=YouTubeSampleVideos.INSTANCE.withSuggestedRecall(before);
        var switches=new HashMap<>(base.getSharedPreferences("youtube_handoff_v1",0).getAll());
        var privacy=new HashMap<>(base.getSharedPreferences("youtube_embed_v1",0).getAll());
        var seed=YouTubeSampleVideos.INSTANCE.pending().get(0);
        var custom=new YouTubeVideo(seed.videoId(),"Custom title",seed.audience(),5,6,600,true,true,"Câu hỏi riêng của gia đình?");
        var unknown=new YouTubeVideo("abcdefghijk","Unknown story",seed.audience(),4,7,120,false,false,"");
        require(YouTubeSampleVideos.INSTANCE.withSuggestedRecall(List.of(custom,unknown)).equals(List.of(custom,unknown)),"Preserve custom questions and unknown IDs exactly");
        Activity activity=runner.startActivitySync(new Intent(base,YouTubeLibraryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        try {
            runner.waitForIdleSync();click(runner,activity);
            require(store.videos().equals(expected),"Native fill persists only expected blank questions");
            require(new YouTubeStore(base).videos().equals(expected),"Disk reload exact");
            require(YouTubePreviewValidation.clickMenu(runner,"ĐÓNG"),"Close native result notice");
            click(runner,activity);require(YouTubePreviewValidation.clickMenu(runner,"ĐÓNG"),"Close repeated-fill notice");
            require(store.videos().equals(expected),"Repeat fill is exact-idempotent");
            int changed=0,questions=0;
            for(int i=0;i<expected.size();i++){
                var old=before.get(i);var now=expected.get(i);
                if(!old.equals(now)){
                    changed++;
                    require(old.recall().isBlank(),"Do not overwrite custom text");
                    require(new YouTubeVideo(now.videoId(),now.title(),now.audience(),now.minAge(),now.maxAge(),now.plannedSeconds(),now.approved(),now.enabled(),old.recall()).equals(old),"All non-question fields unchanged");
                }
                questions+=now.recall().isBlank()?0:now.recall().split("\\R").length;
            }
            require(switches.equals(base.getSharedPreferences("youtube_handoff_v1",0).getAll()) && privacy.equals(base.getSharedPreferences("youtube_embed_v1",0).getAll()),"Exact feature/privacy/journal/budget settings preserved");
            var story=expected.stream().filter(v->v.videoId().equals("Tfkf3NStsx4")).findFirst().orElseThrow();
            runner.runOnMainSync(()->{Button row=find(activity.getWindow().getDecorView(),story.title());require(row!=null && row.performClick(),"Native story row");});
            runner.waitForIdleSync();require(YouTubePreviewValidation.clickMenu(runner,"Sửa / duyệt lại"),"Open native editor");
            require(YouTubePreviewValidation.clickMenu(runner,story.recall()),"Exact authored questions visible in editor");
            runner.waitForIdleSync();SystemClock.sleep(300);
            var image=runner.captureQaDisplay();require(image!=null,"Physical editor capture");
            var dir=new java.io.File(base.getFilesDir(),"qa");dir.mkdirs();
            try(var out=new java.io.FileOutputStream(new java.io.File(dir,"t077-recall-editor.png"))){require(image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out),"Capture saved");}
            finally{image.recycle();}
            require(YouTubePreviewValidation.clickMenu(runner,"HỦY"),"Exit editor without changing approval");
            require(store.videos().equals(expected),"Editor inspection does not modify records");
            result.putInt("updated_stories",changed);result.putInt("total_stories",expected.size());result.putInt("questions",questions);
            result.putString("recall","PASS: native fill/reload/repeat/editor; custom text and unknown IDs preserved; all other fields and YouTube settings exact");
        }finally{runner.runOnMainSync(activity::finish);}
    }
    private static void click(LifecycleValidationRunner runner,Activity activity){
        runner.runOnMainSync(()->{Button action=find(activity.getWindow().getDecorView(),"Bổ sung câu hỏi gợi nhớ");require(action!=null && action.performClick(),"Native fill action");});runner.waitForIdleSync();
    }
    private static Button find(View view,String text){
        if(view instanceof Button b && b.getText().toString().contains(text))return b;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){Button b=find(group.getChildAt(i),text);if(b!=null)return b;}
        return null;
    }
}
