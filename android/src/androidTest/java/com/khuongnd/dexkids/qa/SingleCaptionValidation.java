package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.khuongnd.dexkids.AdventureDashboard;
import com.khuongnd.dexkids.game.KidsGame;
import java.io.*;
import java.util.*;
import static com.khuongnd.dexkids.qa.StoryRemotePlaybackValidation.*;

/** Actual view hierarchy: one spoken-content card in full and audio-only modes. */
final class SingleCaptionValidation {
    static void run(LifecycleValidationRunner r,Bundle result)throws Exception {
        require(r.fullHdQaDisplay() && r.targetDisplay()>0,"Physical external display required");
        Context c=r.getTargetContext();String[] names={"parent_settings","kids_ai_settings_v1","story_manager_v1"};
        Map<String,Map<String,?>> backup=new LinkedHashMap<>();
        for(String name:names)backup.put(name,new HashMap<>(c.getSharedPreferences(name,0).getAll()));
        Activity[] a={null};File dir=new File(c.getExternalFilesDir(null),"qa-t068");dir.mkdirs();
        try {
            a[0]=r.startActivitySync(new Intent().setClassName(c,"com.khuongnd.dexkids.LearningSourceActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            require(find(a[0].getWindow().getDecorView(),"Chọn file TSV")!=null,"Source import button absent");
            require(find(a[0].getWindow().getDecorView(),"Nguồn HTTPS · tự cập nhật khi mở ứng dụng")!=null,"HTTPS button absent");
            capture(r,dir,"source-controls.png");r.runOnMainSync(a[0]::finish);r.waitForIdleSync();a[0]=null;
            c.getSharedPreferences("parent_settings",0).edit().putBoolean("offline_tts",false).putBoolean("child_mic_optin",false).putBoolean("audio_only",false).commit();
            c.getSharedPreferences("kids_ai_settings_v1",0).edit().putBoolean("ai_quizzes",false).putBoolean("automatic_trip_packs",false).commit();
            c.getSharedPreferences("story_manager_v1",0).edit().putBoolean("autoplay",false).commit();
            a[0]=r.startActivitySync(new Intent().setClassName(c,"com.khuongnd.dexkids.KidsActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            Activity child=a[0];await(()->get(child,"adventureDashboard")!=null && get(child,"poiNativeStatus")!=null,"Native panels not ready");
            AdventureDashboard dashboard=(AdventureDashboard)get(child,"adventureDashboard");
            r.runOnMainSync(()->dashboard.showBeat(new com.khuongnd.dexkids.story.EntertainmentDirector.Beat(
                "learn-qa-caption",com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH,
                "Con thử nói con cá bằng tiếng Anh nhé?","Fish nghĩa là con cá.",
                com.khuongnd.dexkids.story.EntertainmentDirector.Reaction.WAVE,"fish"),
                com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH));
            String[] captions={"Con thử nói con cá bằng tiếng Anh nhé?","fish","Fish nghĩa là con cá.","Mình tiếp tục nha!","Trong truyện tưởng tượng, hạt mầm đã thành cây non."};
            for(int i=0;i<captions.length;i++) {
                String text=captions[i];
                r.runOnMainSync(()->{
                    invoke(child,"clearTalkQueue",new Class[]{});
                    invoke(child,"showConversationLine",new Class[]{String.class,String.class},"PHỤ ĐỀ",text);
                    try {
                        ((Runnable)get(child,"updatePoiNativeStatus")).run();
                        ((Handler)get(child,"handler")).removeCallbacks((Runnable)get(child,"updatePoiNativeStatus"));
                    }catch(Exception e){throw new AssertionError(e);}
                });r.waitForIdleSync();SystemClock.sleep(120);
                r.runOnMainSync(()->{
                    require(count(child.getWindow().getDecorView(),text)==1,"Duplicate/missing caption in actual full-mode hierarchy");
                    try {require(!((TextView)get(child,"poiNativeStatus")).getText().toString().contains(text),"Speech leaked into GPS status");}
                    catch(Exception e){throw new AssertionError(e);}
                });
                if(i==1)capture(r,dir,"single-english-caption.png");
            }
            r.runOnMainSync(()->{
                try {((KidsGame)get(child,"runningGame")).setAudioOnly(true);}catch(Exception e){throw new AssertionError(e);}
                dashboard.setVisualsEnabled(false);
            });r.waitForIdleSync();SystemClock.sleep(200);
            r.runOnMainSync(()->require(count(child.getWindow().getDecorView(),captions[4])==1,"Audio-only must keep exactly one readable caption"));
            capture(r,dir,"single-audio-caption.png");
            result.putString("t068_single_caption","PASS: actual source controls, five content phases each once, no GPS subtitle mirror, one audio-only caption; screenshots retained");
        } finally {
            if(a[0]!=null)r.runOnMainSync(a[0]::finish);r.waitForIdleSync();
            for(String name:names)restore(c.getSharedPreferences(name,0),backup.get(name));
            result.putBoolean("fixture_settings_restored",true);
        }
    }
    private static int count(View v,String text) {
        int n=v instanceof TextView t && v.isShown() && t.getText().toString().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))?1:0;
        if(v instanceof ViewGroup g)for(int i=0;i<g.getChildCount();i++)n+=count(g.getChildAt(i),text);
        return n;
    }
    private static View find(View v,String text) {
        if(v instanceof TextView t && t.getText().toString().equals(text))return v;
        if(v instanceof ViewGroup g)for(int i=0;i<g.getChildCount();i++){var found=find(g.getChildAt(i),text);if(found!=null)return found;}
        return null;
    }
    private static void capture(LifecycleValidationRunner r,File dir,String name)throws Exception {
        try(var out=new FileOutputStream(new File(dir,name))) {r.captureQaDisplay().compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}
    }
}
