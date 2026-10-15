package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import com.khuongnd.dexkids.game.KidsGame;
import com.khuongnd.dexkids.story.StoryRepository;
import com.khuongnd.dexkids.story.adventure.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import org.json.*;
import static com.khuongnd.dexkids.qa.ArtVisualValidation.*;

/** Instrumentation-only offline story fixtures. Production callbacks run unmodified.
 * No ASR recognition, GPS truth or physical DeX acceptance is inferred from these checks. */
final class StoryAdventureValidation {
    private static final String PKG="com.khuongnd.dexkids";
    static void run(LifecycleValidationRunner r,Bundle result,boolean soak) throws Exception { run(r,result,soak,false); }
    static void run(LifecycleValidationRunner r,Bundle result,boolean soak,boolean live) throws Exception {
        require(r.fullHdQaDisplay() && (!live || r.targetDisplay()==0),"Explicit FullHD target required; synthetic LIVE fixture is emulator-only");
        var context=r.getTargetContext();var prefs=context.getSharedPreferences("parent_settings",0);var old=prefs.getAll();
        var ai=context.getSharedPreferences("kids_ai_settings_v1",0);var oldAi=ai.getAll();
        File dir=new File(context.getExternalFilesDir(null),live?"qa-t059-live":soak?"qa-t059-soak":"qa-t059-smoke");
        require(dir.isDirectory()||dir.mkdirs(),"Evidence dir unavailable");
        Map<StoryEpisode.Audience,String> ids=new EnumMap<>(StoryEpisode.Audience.class);
        var records=new JSONArray();var samples=new JSONArray();
        Set<StoryEpisode.Audience> completed=EnumSet.noneOf(StoryEpisode.Audience.class);Activity[] activities=new Activity[2];
        var monitor=r.addMonitor(PKG+".KidsActivity",null,false);
        try {
            try(var repo=new StoryRepository(context)) {
                for(var audience:StoryEpisode.Audience.values()) {
                    var sample=StoryFixtures.educational(audience);
                    File generated=new File(context.getCacheDir(),"qa-t054-reviewed-draft.json");
                    if(audience==StoryEpisode.Audience.BOTH && generated.isFile()) {
                        var authored=StoryEpisodeCodec.parse(new String(Files.readAllBytes(generated.toPath()),StandardCharsets.UTF_8));
                        var reviewed=repo.get(authored.id());
                        require(reviewed!=null && reviewed.getReviewed(),"Generated source lost independent review");
                        sample=authored;result.putString("both_source","Exact content of real Gemini reviewed draft cloned into isolated QA fixture; test approval only");
                    }
                    String id="qa-t059-"+audience.name().toLowerCase(Locale.ROOT);ids.put(audience,id);
                    var episode=new StoryEpisode(id,1,1,sample.title(),audience,5,4,sample.educationalGoal(),sample.synopsis(),sample.scenes());
                    var entry=repo.save(episode,true,null);repo.approve(id,entry.getEpisode().revision());
                    repo.cursor(id,entry.getEpisode().revision(),0);
                }
                // Real SQLite edit -> invalidated approval -> undo -> delete -> undo, all off UI.
                String id=ids.get(StoryEpisode.Audience.BOTH);var original=repo.get(id);
                var draft=repo.save(original.getEpisode(),false,original.getEpisode().revision());
                require(!draft.getApproved(),"Edited revision retained approval");repo.undo(id);
                require(repo.get(id).getApproved(),"Undo lost original approval");repo.delete(id,true);
                require(repo.list(false).stream().noneMatch(e->e.getEpisode().id().equals(id)),"Deleted entry listed");
                repo.undo(id);require(!repo.get(id).getDeleted(),"Delete undo failed");
            }
            prefs.edit().putString("audience_mode","SAU").putBoolean("audio_only",false)
                .putBoolean("offline_tts",true).putBoolean("tts_enabled",true)
                .putBoolean("ambient_music",false).putBoolean("audio_effects",false)
                .putBoolean("child_mic_optin",false).commit();
            ai.edit().putBoolean("ai_quizzes",false).putBoolean("ai_trip_companion_optin",false)
                .putBoolean("creative_trip_packs",false).commit();
            activities[0]=r.startActivitySync(new Intent().setClassName(PKG,PKG+".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            if(live) {
                activities[1]=r.startActivitySync(new Intent().setClassName(PKG,PKG+".KidsActivity")
                    .putExtra("com.khuongnd.dexkids.extra.LIVE_GPS",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                r.runOnMainSync(()->require(preview(activities[0].getWindow().getDecorView()).performClick(),"DEMO missing"));
                activities[1]=r.waitForMonitorWithTimeout(monitor,10000);require(activities[1]!=null,"Child missing");
            }
            SystemClock.sleep(3500);
            var game=(KidsGame)field(activities[1],"runningGame");Object screen=game.getScreen();
            Object session=field(activities[1],"storySession");require(session!=null,"Production story controller missing");
            var director=(StoryPlaybackDirector)field(session,"director");
            var load=session.getClass().getDeclaredMethod("load",String.class,boolean.class);load.setAccessible(true);
            var pause=session.getClass().getDeclaredMethod("pause",long.class);pause.setAccessible(true);
            var resume=session.getClass().getDeclaredMethod("resume");resume.setAccessible(true);
            gl(()->set(screen,"runMetrics",new com.khuongnd.dexkids.game.RenderRunMetrics()));
            long started=SystemClock.elapsedRealtime(),duration=soak?1800000:180000;
            String last="";int captures=0,loads=0;int[] ttsSamples={0};boolean paused=false,poiInjected=false,poiResumed=false;double[] distance={0,0};
            com.khuongnd.dexkids.story.NarrationCue poiCue;
            try(var input=context.getAssets().open("narration/sample-hcm.tsv")) {
                poiCue=com.khuongnd.dexkids.story.OfflineNarrationCatalog.parse(input)
                    .findByPoiId("osm:way:500888743").orElseThrow();
            }
            long poiAt=0;
            gl(()->distance[0]=((com.khuongnd.dexkids.journey.JourneyFeed)field(screen,"journey")).distanceMeters());
            for(int tick=0;SystemClock.elapsedRealtime()-started<duration;tick++) {
                long elapsed=SystemClock.elapsedRealtime()-started;
                r.runOnMainSync(()->{try{if((boolean)field(activities[1],"speechStarted"))ttsSamples[0]++;}catch(Exception e){throw new AssertionError(e);}});
                var audience=StoryEpisode.Audience.values()[(int)((elapsed/(soak?240000:30000))%3)];
                if(director.state()==StoryPlaybackDirector.State.COMPLETE && director.episode()!=null) {
                    completed.add(director.episode().audience());
                    records.put(new JSONObject().put("event","episode_completed").put("audience",director.episode().audience().name()).put("elapsed_ms",elapsed));
                }
                if(tick==0 || director.state()!=StoryPlaybackDirector.State.PAUSED && (director.state()==StoryPlaybackDirector.State.COMPLETE || director.state()==StoryPlaybackDirector.State.IDLE
                        || director.episode()!=null && director.episode().audience()!=audience)) {
                    r.runOnMainSync(()->{try {
                        prefs.edit().putString("audience_mode",audience.name()).commit();game.setAudienceMode(audience.name());
                        field(activities[1],"adventureDashboard").getClass().getMethod("setMode",String.class,int.class,int.class)
                            .invoke(field(activities[1],"adventureDashboard"),audience.name(),5,4);
                        load.invoke(session,ids.get(audience),false);
                    }catch(Exception e){throw new AssertionError(e);}});loads++;
                }
                if(!paused && elapsed>15000) {
                    var cursor=director.cursor();
                    r.runOnMainSync(()->{try{pause.invoke(session,0L);}catch(Exception e){throw new AssertionError(e);}});
                    require(director.state()==StoryPlaybackDirector.State.PAUSED,"Pause missing");SystemClock.sleep(700);
                    require(director.cursor().equals(cursor),"Paused story advanced");
                    r.runOnMainSync(()->{try{resume.invoke(session);}catch(Exception e){throw new AssertionError(e);}});paused=true;
                }
                if(!poiInjected && elapsed>(soak?240000:60000) && director.state()!=StoryPlaybackDirector.State.PAUSED) {
                    // Inject a sourced POI only at the downstream queue boundary. No GPS arrival is fabricated.
                    gl(()->((java.util.concurrent.atomic.AtomicReference<com.khuongnd.dexkids.story.NarrationCue>)field(screen,"narrationQueue")).set(poiCue));
                    long until=SystemClock.elapsedRealtime()+5000;
                    while(director.state()!=StoryPlaybackDirector.State.PAUSED && SystemClock.elapsedRealtime()<until)SystemClock.sleep(100);
                    require(director.state()==StoryPlaybackDirector.State.PAUSED,"Sourced POI did not interrupt story");
                    var cursor=director.cursor();SystemClock.sleep(1000);require(director.cursor().equals(cursor),"POI interruption advanced story");
                    capture(r,dir,"sourced-poi-interruption");poiInjected=true;poiAt=SystemClock.elapsedRealtime();
                    records.put(new JSONObject().put("event","sourced_downstream_poi_interruption").put("simulated",true).put("elapsed_ms",poiAt-started));
                }
                if(poiInjected && !poiResumed && director.state()!=StoryPlaybackDirector.State.PAUSED) {
                    require(SystemClock.elapsedRealtime()-poiAt>=85000,"Story resumed before POI priority window");
                    poiResumed=true;records.put(new JSONObject().put("event","poi_priority_resume").put("elapsed_ms",SystemClock.elapsedRealtime()-started));
                    capture(r,dir,"after-poi-resume");
                }
                String state=audience+":"+director.state()+":"+director.cursor();
                if(!state.equals(last)) {
                    var row=new JSONObject();row.put("elapsed_ms",elapsed);row.put("state",state);row.put("tts_active_samples",ttsSamples[0]);records.put(row);last=state;
                    if(captures<24 && director.state()==StoryPlaybackDirector.State.SPEAKING) {
                        capture(r,dir,String.format(Locale.ROOT,"scene-%02d-%s",captures++,audience));
                    }
                }
                if(tick%10==0)gl(()->{
                    var metric=(com.khuongnd.dexkids.game.RenderRunMetrics)field(screen,"runMetrics");
                    var mem=new android.os.Debug.MemoryInfo();android.os.Debug.getMemoryInfo(mem);
                    var row=new JSONObject();row.put("elapsed_ms",SystemClock.elapsedRealtime()-started);
                    row.put("frames",metric.frames());row.put("render_seconds",metric.seconds());
                    row.put("average_fps",metric.averageFps());row.put("p95_upper_ms",metric.p95UpperMs());
                    row.put("draw_calls",field(screen,"previousFrameDrawCalls"));row.put("pss_kib",mem.getTotalPss());
                    row.put("managed_textures",com.badlogic.gdx.graphics.Texture.getNumManagedTextures());samples.put(row);
                });
                Files.write(new File(dir,"events.json").toPath(),records.toString(2).getBytes(StandardCharsets.UTF_8));
                Files.write(new File(dir,"performance.json").toPath(),samples.toString(2).getBytes(StandardCharsets.UTF_8));
                SystemClock.sleep(1000);
            }
            gl(()->distance[1]=((com.khuongnd.dexkids.journey.JourneyFeed)field(screen,"journey")).distanceMeters());
            require(distance[1]>distance[0],"Journey stopped during story");
            require(poiInjected && poiResumed,"POI pause/resume not completed");
            if(soak)require(completed.size()==3,"Not all three audience episodes completed: "+completed);
            result.putString("completed_audiences",completed.toString());
            result.putInt("tts_active_samples",ttsSamples[0]);
            result.putBoolean("live_gps",live); result.putDouble("journey_distance_delta_m",distance[1]-distance[0]);
            result.putLong("actual_duration_ms",SystemClock.elapsedRealtime()-started);result.putInt("story_loads",loads);
            result.putInt("captures",captures);result.putString("sqlite_crud","edit invalidates approval; undo; soft-delete; undo");
            result.putString("limits", "Local fixtures; optional pre-existing reviewed draft identified by both_source; sourced downstream POI simulated; " + (live?"synthetic Android GPS fixes injected; ":"DEMO journey; ") + "display="+r.targetDisplay()+"; no real-worldGPS or real ASR recognition");
        } finally {
            r.runOnMainSync(()->{for(var a:activities)if(a!=null)a.finish();});r.removeMonitor(monitor);
            r.waitForIdleSync();SystemClock.sleep(500);
            try(var cleanup=new StoryRepository(context)) {for(String id:ids.values()) {
                if(cleanup.get(id)!=null){cleanup.delete(id,true);cleanup.purgeDeleted(id);}
            }}
            restore(prefs,old);restore(ai,oldAi);
        }
    }
}
