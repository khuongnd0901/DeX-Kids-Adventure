package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.khuongnd.dexkids.game.*;
import org.json.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import static com.khuongnd.dexkids.qa.ArtVisualValidation.*;

/** Identical bounded before/after probe; no forced GC, trim or network policy changes. */
final class ResourceOptimizationValidation {
    static void run(LifecycleValidationRunner r, Bundle result) throws Exception {
        require(r.targetDisplay()>0,"Physical external display required");
        var c=r.getTargetContext();
        String[] names={"parent_settings","kids_ai_settings_v1","story_manager_v1"};
        var backups=new HashMap<String,Map<String,?>>();
        for(String name:names)backups.put(name,c.getSharedPreferences(name,0).getAll());
        File dir=new File(c.getExternalFilesDir(null),"qa-t066-resources");
        require(dir.isDirectory()||dir.mkdirs(),"Evidence directory");
        Activity[] activities=new Activity[2];
        var monitor=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);
        JSONArray rows=new JSONArray();
        try {
            c.getSharedPreferences(names[0],0).edit().putBoolean("audio_only",false)
                .putString("audience_mode","BOTH").putBoolean("offline_tts",false)
                .putBoolean("child_mic_optin",false).putBoolean("ambient_music",false).commit();
            c.getSharedPreferences(names[1],0).edit().putBoolean("ai_quizzes",false)
                .putBoolean("creative_trip_packs",false).putBoolean("ai_trip_companion_optin",false).commit();
            c.getSharedPreferences(names[2],0).edit().putBoolean("autoplay",false).commit();
            long launched=SystemClock.elapsedRealtime();
            activities[0]=r.startActivitySync(new Intent().setClassName(c,"com.khuongnd.dexkids.ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            r.runOnMainSync(()->require(preview(activities[0].getWindow().getDecorView()).performClick(),"DEMO missing"));
            activities[1]=r.waitForMonitorWithTimeout(monitor,10000);require(activities[1]!=null,"Child missing");
            var game=(KidsGame)field(activities[1],"runningGame");
            long deadline=SystemClock.elapsedRealtime()+15000;
            boolean[] ready={false};
            do {gl(()->ready[0]=game.getScreen()!=null && field(game.getScreen(),"cartoonSprites")!=null);if(!ready[0])SystemClock.sleep(50);}
            while(!ready[0]&&SystemClock.elapsedRealtime()<deadline);
            require(ready[0],"Scenery startup timed out");
            Object screen=game.getScreen();
            result.putLong("launch_to_scenery_ms",SystemClock.elapsedRealtime()-launched);
            long started=SystemClock.elapsedRealtime(),cpu=android.os.Process.getElapsedCpuTime();
            for(int i=0;i<=9;i++) {
                long wait=started+i*10000-SystemClock.elapsedRealtime();if(wait>0)SystemClock.sleep(wait);
                sample(rows,screen,"full",started,cpu);
                Files.write(new File(dir,"samples.json").toPath(),rows.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            capture(r,dir,"full");
            r.runOnMainSync(()->game.setAudioOnly(true));SystemClock.sleep(15000);
            gl(()->require(field(screen,"cartoonSprites")==null,"Audio retained scene"));
            sample(rows,screen,"audio",started,cpu);
            r.runOnMainSync(()->game.setAudioOnly(false));SystemClock.sleep(15000);
            sample(rows,screen,"restored_full",started,cpu);
            Files.write(new File(dir,"samples.json").toPath(),rows.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            result.putString("resources","PASS: actual90sFull/15sAudio/15sFull; CPU100%=one core; PSS/GL bytes recorded, no forced GC");
            result.putString("samples",rows.toString());
        } finally {
            r.runOnMainSync(()->{for(Activity a:activities)if(a!=null)a.finish();});r.removeMonitor(monitor);
            for(String name:names)restore(c.getSharedPreferences(name,0),backups.get(name));
        }
    }
    private static void sample(JSONArray rows,Object screen,String mode,long start,long cpu) throws Exception {
        JSONObject row=new JSONObject();long elapsed=SystemClock.elapsedRealtime()-start;
        row.put("mode",mode).put("elapsed_ms",elapsed).put("cpu_ms",android.os.Process.getElapsedCpuTime()-cpu);
        Debug.MemoryInfo memory=new Debug.MemoryInfo();Debug.getMemoryInfo(memory);
        row.put("pss_kib",memory.getTotalPss()).put("native_pss_kib",memory.nativePss)
            .put("native_allocated_bytes",Debug.getNativeHeapAllocatedSize())
            .put("java_used_bytes",Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
        gl(()->{
            var metrics=(RenderRunMetrics)field(screen,"runMetrics");
            row.put("fps",metrics.averageFps()).put("p95_upper_ms",metrics.p95UpperMs());
            var textures=Collections.newSetFromMap(new IdentityHashMap<Texture,Boolean>());
            JSONObject inventory=new JSONObject();
            collect(screen,textures,inventory,"screen.");
            Object sprites=field(screen,"cartoonSprites");if(sprites!=null)collect(sprites,textures,inventory,"sprites.");
            long bytes=0;for(Texture t:textures)bytes+=(long)t.getWidth()*t.getHeight()*4;
            row.put("scene_rgba_upper_bytes",bytes).put("textures",inventory)
                .put("managed_textures",Texture.getNumManagedTextures());
        });
        rows.put(row);
    }
    private static void collect(Object owner,Set<Texture> textures,JSONObject inventory,String prefix) throws Exception {
        for(var member:owner.getClass().getDeclaredFields()) {
            member.setAccessible(true);Object value=member.get(owner);
            if(value instanceof Texture t)add(t,textures,inventory,prefix+member.getName());
            else if(value instanceof TextureAtlas atlas) {int i=0;for(Texture t:atlas.getTextures())add(t,textures,inventory,prefix+member.getName()+i++);}
            else if(value!=null && (member.getName().equals("m10Art")||member.getName().equals("storySupport")||member.getName().equals("painter")||member.getName().equals("backdropRenderer")))
                collect(value,textures,inventory,prefix+member.getName()+".");
        }
    }
    private static void add(Texture t,Set<Texture> textures,JSONObject inventory,String name) throws Exception {
        textures.add(t);inventory.put(name,t.getWidth()+"x"+t.getHeight());
    }
}
