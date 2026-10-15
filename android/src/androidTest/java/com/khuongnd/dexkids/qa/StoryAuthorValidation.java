package com.khuongnd.dexkids.qa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.khuongnd.dexkids.ai.*;
import com.khuongnd.dexkids.story.StoryAuthor;
import com.khuongnd.dexkids.story.StoryRepository;
import com.khuongnd.dexkids.story.adventure.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/** Explicit live author/review evidence. Temporary parent-authorized text-only setup; restores settings and never grants child-cloud consent. */
final class StoryAuthorValidation {
    static void run(LifecycleValidationRunner runner,Bundle result) throws Exception {
        Context context=runner.getTargetContext();
        SharedPreferences prefs=context.getSharedPreferences("kids_ai_settings_v1",0);
        SharedPreferences keys=context.getSharedPreferences("kids_ai_keys_v1",0);
        Map<String,?> oldPrefs=new HashMap<>(prefs.getAll()),oldKeys=new HashMap<>(keys.getAll());
        File secret=new File(context.getFilesDir(),"qa-story-author-secret.json");
        StoryRepository repository=null;
        try {
            KidsAiSettings settings=new KidsAiSettings(context);
            if(secret.isFile()) {
                JSONObject fixture=new JSONObject(new String(Files.readAllBytes(secret.toPath()),StandardCharsets.UTF_8));
                LifecycleValidationRunner.require(secret.delete(),"Cannot remove private story fixture");
                LifecycleValidationRunner.require(fixture.getBoolean("parent_authorized_story_text"),"Explicit story text authorization missing");
                settings.setModel(KidsAiProvider.GEMINI,fixture.getString("model"));
                settings.setFreeTierAcknowledged(KidsAiProvider.GEMINI,true);
                new KidsAiKeys(context).save(KidsAiProvider.GEMINI,fixture.getString("key"));
            }
            if(!settings.freeTierAcknowledged(KidsAiProvider.GEMINI) || settings.model(KidsAiProvider.GEMINI).isEmpty()) {
                result.putString("t054_story_ai","NOT VERIFIED: no parent-acknowledged Gemini model; zero requests");return;
            }
            if(!new KidsAiKeys(context).configured(KidsAiProvider.GEMINI)) {
                result.putString("t054_story_ai","NOT VERIFIED: acknowledged provider has no usable key; zero requests");return;
            }
            LifecycleValidationRunner.require(prefs.edit().putBoolean("ai_quizzes",true)
                .putBoolean("creative_trip_packs",true).putBoolean("automatic_trip_packs",false)
                .putBoolean("child_text_cloud_explicit",false).putBoolean("GEMINI_enabled",true)
                .putBoolean("GROQ_enabled",false).putString("provider","GEMINI").commit(),"Story author preferences failed");
            repository=new StoryRepository(context);
            // Exactly one candidate, BOTH first: at most one author + independent review, no retry loop.
            StoryEpisode episode;
            long started=android.os.SystemClock.elapsedRealtime();
            try {episode=new StoryAuthor(context).generate("Chăm sóc khu vườn tưởng tượng","Biết giúp bạn và chăm sóc cây",StoryEpisode.Audience.BOTH);}
            catch(Exception failure) {
                Throwable diagnostic=failure; while(diagnostic.getCause()!=null)diagnostic=diagnostic.getCause();
                String code=diagnostic.getMessage();
                result.putString("t054_story_ai","FAIL: "+failure.getClass().getSimpleName()+
                    (code!=null && code.matches("[A-Z0-9_]{1,80}")?" "+code:" (details withheld)"));
                result.putLong("elapsed_ms",android.os.SystemClock.elapsedRealtime()-started);
                result.putString("retry_policy","One candidate only; no retry; no parent approval or playback");return;
            }
            var saved=repository.save(episode,true,null);
            LifecycleValidationRunner.require(saved.getReviewed() && !saved.getApproved(),"Live candidate bypassed parent approval");
            Files.write(new File(context.getCacheDir(),"qa-t054-reviewed-draft.json").toPath(),StoryEpisodeCodec.toJson(saved.getEpisode()).getBytes(StandardCharsets.UTF_8));
            result.putString("t054_story_ai","PASS: real author + independent review + deterministic validation; saved reviewed DRAFT; parent approval still required");
            result.putString("episode_id",saved.getEpisode().id());result.putInt("revision",saved.getEpisode().revision());
            result.putInt("scenes",episode.scenes().size());result.putInt("questions",(int)episode.scenes().stream().filter(s->s.question()!=null).count());
            result.putLong("elapsed_ms",android.os.SystemClock.elapsedRealtime()-started);
            result.putString("privacy","Only general gardening topic/goal and authored schema; no audio, transcript, photo, GPS or child-cloud consent");
        } finally {
            if(secret.exists())LifecycleValidationRunner.require(secret.delete(),"Private story fixture cleanup failed");
            if(repository!=null)repository.close();
            restore(keys,oldKeys);restore(prefs,oldPrefs);
        }
    }
    private static void restore(SharedPreferences prefs,Map<String,?> original) {
        SharedPreferences.Editor editor=prefs.edit().clear();
        for(var row:original.entrySet()) {
            Object value=row.getValue();String key=row.getKey();
            if(value instanceof String)editor.putString(key,(String)value);
            else if(value instanceof Boolean)editor.putBoolean(key,(Boolean)value);
            else if(value instanceof Integer)editor.putInt(key,(Integer)value);
            else if(value instanceof Long)editor.putLong(key,(Long)value);
            else if(value instanceof Float)editor.putFloat(key,(Float)value);
            else throw new IllegalStateException("Unsupported preference type; preserve snapshot");
        }
        LifecycleValidationRunner.require(editor.commit(),"Story author preference restoration failed");
    }
}
