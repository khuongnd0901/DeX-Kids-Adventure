package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.*;
import android.os.*;
import com.khuongnd.dexkids.story.adventure.*;
import org.json.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import static com.khuongnd.dexkids.qa.StoryRemotePlaybackValidation.*;

/** Actual controller + native TTS + wall clock from a deliberate recall-scene fixture cursor. */
final class StoryWaitBoundsValidation {
    static void run(LifecycleValidationRunner r,Bundle result)throws Exception {
        require(r.fullHdQaDisplay() && r.targetDisplay()>0,"Physical DeX required");
        Context c=r.getTargetContext();
        String[] names={"parent_settings","kids_ai_settings_v1","story_manager_v1"};
        Map<String,Map<String,?>> original=new LinkedHashMap<>();
        for(String name:names)original.put(name,new HashMap<>(c.getSharedPreferences(name,0).getAll()));
        Activity[] a={null};JSONArray waits=new JSONArray();JSONObject report=new JSONObject();
        File dir=new File(c.getExternalFilesDir(null),"qa-t068");dir.mkdirs();
        try {
            c.getSharedPreferences("parent_settings",0).edit().putString("audience_mode","BOTH")
                .putBoolean("offline_tts",true).putBoolean("child_mic_optin",false).commit();
            c.getSharedPreferences("kids_ai_settings_v1",0).edit().putBoolean("ai_quizzes",false)
                .putBoolean("automatic_trip_packs",false).putBoolean("creative_trip_packs",false).commit();
            c.getSharedPreferences("story_manager_v1",0).edit().putBoolean("autoplay",false).commit();
            var episode=BuiltinInterludes.at(StoryEpisode.Audience.BOTH,0);
            a[0]=r.startActivitySync(new Intent().setClassName(c,"com.khuongnd.dexkids.KidsActivity")
                .putExtra("story_episode_id",episode.id()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            Activity current=a[0];
            await(()->{var controller=get(current,"storySession");return controller!=null && episode.id().equals(get(controller,"selectedId"));},"Builtin story not loaded");
            Object controller=get(current,"storySession");
            r.runOnMainSync(()->{
                invoke(controller,"pause",new Class[]{long.class},0L);
                try {((StoryPlaybackDirector)get(controller,"director")).restore(episode,new StoryPlaybackDirector.Cursor(2,0,0));}
                catch(Exception e){throw new AssertionError(e);}
                invoke(controller,"resume",new Class[]{});
            });
            long[] listeningAt={0};boolean[] complete={false};long until=SystemClock.elapsedRealtime()+100_000;
            while(SystemClock.elapsedRealtime()<until && !complete[0]) {
                r.runOnMainSync(()->{
                    try {
                        var player=(StoryPlaybackDirector)get(controller,"director");
                        if(player.state()==StoryPlaybackDirector.State.LISTENING && listeningAt[0]==0)
                            listeningAt[0]=((Number)get(controller,"waitStarted")).longValue();
                        var speech=(StoryPlaybackDirector.Speech)get(controller,"activeSpeech");
                        if(listeningAt[0]>0 && speech!=null && speech.kind()==StoryPlaybackDirector.Kind.FEEDBACK) {
                            long elapsed=SystemClock.elapsedRealtime()-listeningAt[0];waits.put(elapsed);listeningAt[0]=0;
                            require(elapsed>=2950 && elapsed<=5250,"Actual story response wait exceeded 3–5s: "+elapsed);
                        }
                        complete[0]=player.state()==StoryPlaybackDirector.State.COMPLETE;
                    }catch(Exception e){throw new AssertionError(e);}
                });
                SystemClock.sleep(35);
            }
            require(complete[0] && waits.length()==3,"Three fallback recall questions did not finish");
            report.put("status","PASS").put("wait_ms",waits).put("scope","Production builtin load/resume, recall scene2→complete; native TTS, mic off; preceding narration deliberately skipped by fixture cursor");
            result.putString("t068_story_wait",report.toString());
        } finally {
            if(!report.has("status"))report.put("status","FAILED_OR_INCOMPLETE").put("wait_ms",waits);
            Files.write(new File(dir,"story-wait.json").toPath(),report.toString(2).getBytes(StandardCharsets.UTF_8));
            if(a[0]!=null)r.runOnMainSync(a[0]::finish);r.waitForIdleSync();
            for(String name:names)restore(c.getSharedPreferences(name,0),original.get(name));
            result.putBoolean("fixture_settings_restored",true);
        }
    }
}
