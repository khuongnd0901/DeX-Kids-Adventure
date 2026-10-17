package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.*;
import android.os.*;
import com.khuongnd.dexkids.LearningSource;
import com.khuongnd.dexkids.ai.KidsAiSettings;
import com.khuongnd.dexkids.story.*;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import static com.khuongnd.dexkids.qa.StoryRemotePlaybackValidation.*;

/** Explicit user-authorized persistent device configuration, not a temporary fixture. */
final class Learning400ConfigureValidation {
    static final String URL="https://raw.githubusercontent.com/khuongnd0901/DeX-Kids-Adventure/refs/heads/feat/T-069-learning-pack-400/docs/learning-source/learning.5-7.400.tsv";
    static final String SHA="2256c5c7d38316ac86c6072b245a1fc58a5b3b7b657439c03500c1b5d3b9acfb";
    static void run(LifecycleValidationRunner r,Bundle result)throws Exception {
        require(r.fullHdQaDisplay() && r.targetDisplay()>0,"Explicit external DeX required");
        Context c=r.getTargetContext();Activity[] a={null};
        KidsAiSettings ai=new KidsAiSettings(c);
        ai.setEnabled(false);ai.setAutomaticPacks(false);ai.setCreativePacks(false);
        LearningSource.INSTANCE.configure(c,URL);
        CountDownLatch done=new CountDownLatch(1);
        LearningSource.INSTANCE.request(c,true,()->{done.countDown();return kotlin.Unit.INSTANCE;});
        require(done.await(45,TimeUnit.SECONDS),"400-card HTTPS sync timed out");
        var catalog=LearningSource.INSTANCE.load(c);
        require(catalog.size()==400 && catalog.count(OfflineLearningCatalog.Kind.QUIZ)==200 &&
            catalog.count(OfflineLearningCatalog.Kind.WORD)==200,"Published pack not activated");
        JSONObject cache=new JSONObject(new String(Files.readAllBytes(new File(c.getFilesDir(),"learning-source.json").toPath()),StandardCharsets.UTF_8));
        byte[] bytes=cache.getString("data").getBytes(StandardCharsets.UTF_8);
        StringBuilder digest=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))digest.append(String.format("%02x",b));
        require(digest.toString().equals(SHA),"Downloaded pack differs from verified published pack");
        require(LearningSource.INSTANCE.load(new ContextWrapper(c) {}).size()==400,"Downloaded pack cannot reload from disk");
        JSONObject report=new JSONObject().put("url",URL).put("sha256",digest.toString()).put("cards",400)
            .put("quizzes",200).put("english",200).put("ai_enabled",ai.getEnabled())
            .put("automatic_packs",ai.getAutomaticPacks()).put("creative_packs",ai.getCreativePacks());
        File dir=new File(c.getExternalFilesDir(null),"qa-t069");dir.mkdirs();
        try {
            a[0]=r.startActivitySync(new Intent().setClassName(c,"com.khuongnd.dexkids.KidsActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            Activity child=a[0];await(()->get(child,"runningGame")!=null,"Child not ready");
            require(get(child,"tripPackCoordinator")==null,"External-source startup constructed AI update coordinator");
            require(get(child,"aiWarmThread")==null,"AI warm-up thread exists");
            require(((OfflineLearningCatalog)get(child,"learningCatalog")).size()==400,"Child did not capture400-card source snapshot");
            long until=SystemClock.elapsedRealtime()+90_000;int[] completed={0};
            while(SystemClock.elapsedRealtime()<until && completed[0]<2) {
                r.runOnMainSync(()->{
                    try {
                        completed[0]=((ChildEngagementMetrics)get(child,"engagementMetrics")).snapshot().resolutions();
                        require(get(child,"tripPackCoordinator")==null && get(child,"aiWarmThread")==null,"AI update worker appeared");
                    }catch(Exception e){throw new AssertionError(e);}
                });SystemClock.sleep(100);
            }
            require(completed[0]>=2,"External-source quiz and English turn did not complete");
            var history=((EntertainmentDirector)get(child,"entertainmentDirector")).questionHistory();
            require(history.stream().anyMatch(x->x.startsWith("quiz-57-")) && history.stream().anyMatch(x->x.startsWith("word-57-")),"Playback used old content instead of new file");
            report.put("completed_external_turns",completed[0]).put("played_card_ids",new org.json.JSONArray(history))
                .put("ai_coordinator_created",false).put("status","PASS");
            try(var out=new FileOutputStream(new File(dir,"external-learning.png"))) {r.captureQaDisplay().compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}
            result.putString("t069_configured",report.toString());
        } finally {
            if(a[0]!=null)r.runOnMainSync(a[0]::finish);r.waitForIdleSync();
            Files.write(new File(dir,"configuration.json").toPath(),report.toString(2).getBytes(StandardCharsets.UTF_8));
            // Keep URL/cache and AI generation OFF: this is the exact user-requested final configuration.
        }
    }
}
