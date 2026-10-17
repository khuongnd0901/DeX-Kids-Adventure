package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.*;
import android.os.*;
import com.khuongnd.dexkids.story.*;
import com.khuongnd.dexkids.story.adventure.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import static com.khuongnd.dexkids.qa.StoryRemotePlaybackValidation.*;

/** Real elapsed-time observation of production callbacks; wraps and forwards onFinished unchanged. */
final class QuestionPacingValidation {
    static void run(LifecycleValidationRunner r, Bundle result) throws Exception {
        require(r.fullHdQaDisplay() && r.targetDisplay()>0, "Physical external DeX required");
        Context c=r.getTargetContext();
        String[] names={"parent_settings","kids_ai_settings_v1","story_manager_v1","learning_source_v1"};
        Map<String,Map<String,?>> backup=new LinkedHashMap<>();
        for(String name:names) backup.put(name,new HashMap<>(c.getSharedPreferences(name,0).getAll()));
        File dir=new File(c.getExternalFilesDir(null),"qa-t068");require(dir.isDirectory()||dir.mkdirs(),"evidence dir");
        JSONArray events=new JSONArray(), questions=new JSONArray();
        JSONObject report=new JSONObject();
        Activity[] current={null};
        long started=SystemClock.elapsedRealtime();
        try {
            c.getSharedPreferences("parent_settings",0).edit().putString("audience_mode","BOTH")
                .putBoolean("offline_tts",true).putBoolean("child_mic_optin",false).putBoolean("audio_only",false).commit();
            c.getSharedPreferences("kids_ai_settings_v1",0).edit().putBoolean("ai_quizzes",false)
                .putBoolean("creative_trip_packs",false).putBoolean("automatic_trip_packs",false).putBoolean("ai_trip_companion_optin",false).commit();
            c.getSharedPreferences("story_manager_v1",0).edit().putBoolean("autoplay",false).commit();
            c.getSharedPreferences("learning_source_v1",0).edit().putString("mode","builtin").putBoolean("interleave",true).commit();
            current[0]=r.startActivitySync(new Intent().setClassName(c,"com.khuongnd.dexkids.KidsActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            Activity a=current[0];
            await(()->get(a,"runningGame")!=null && get(a,"narrator")!=null,"Child not ready");
            r.runOnMainSync(()->{
                try {
                    Object packs=get(a,"tripPackCoordinator");
                    if(packs!=null)invoke(packs,"pause",new Class[]{});
                    set(a,"tripPackCoordinator",null); // isolate legacy cached AI; no generated content or calls
                }catch(Exception e){throw new AssertionError(e);}
            });
            Set<String> ids=new HashSet<>(), wrapped=new HashSet<>(), transitions=new HashSet<>();
            int[] previousStarts={0}, previousResolutions={0};
            JSONObject[] question={null};
            long[] questionDone={0}, answerDone={0}, previousAnswerDone={0};
            String[] storyId={null};
            boolean[] storySpoken={false};
            long until=SystemClock.elapsedRealtime()+240_000;
            while(SystemClock.elapsedRealtime()<until && !storySpoken[0]) {
                r.runOnMainSync(()->{
                    try {
                        long now=SystemClock.elapsedRealtime();
                        var metrics=((ChildEngagementMetrics)get(a,"engagementMetrics")).snapshot();
                        String caption=String.valueOf(get(a,"subtitleText"));
                        Object narrator=get(a,"narrator");
                        String utterance=(String)get(narrator,"currentUtteranceId");
                        if(utterance!=null && wrapped.add(utterance)) {
                            Function0<Unit> original=(Function0<Unit>)get(narrator,"currentFinish");
                            require(original!=null,"Missing native callback");
                            String phase=caption.contains("TRUYỆN TƯỞNG TƯỢNG")?"STORY":
                                caption.contains("ĐẾN LƯỢT MÌNH")?"ANSWER":caption.contains("NGHE MẪU TIẾNG ANH")?"ENGLISH_SAMPLE":metrics.unfinished()>0?"QUESTION":"TRANSITION";
                            var engine=(android.speech.tts.TextToSpeech)get(narrator,"engine");
                            String language=engine.getVoice().getLocale().getLanguage();
                            events.put(event("tts_queued_"+phase,now).put("utterance",utterance).put("language",language));
                            if(phase.equals("ENGLISH_SAMPLE")) {
                                require(language.equals("en"),"English sample selected wrong voice");
                                require(question[0]!=null && !question[0].has("response_wait_ms"),"English sample played after answer");
                                question[0].put("english_before_answer",true);
                            }
                            if(phase.equals("ANSWER")) {
                                require(language.equals("vi"),"English sample incorrectly queued in answer phase");
                                if(question[0]!=null && question[0].getString("card").startsWith("word-"))
                                    require(question[0].optBoolean("english_before_answer"),"Answer preceded native English sample");
                            }
                            set(narrator,"currentFinish",(Function0<Unit>)()->{
                                try {
                                    long finished=SystemClock.elapsedRealtime();
                                    events.put(event("tts_completed_"+phase,finished).put("utterance",utterance));
                                    if(phase.equals("QUESTION") || phase.equals("ENGLISH_SAMPLE"))questionDone[0]=finished;
                                    if(phase.equals("ANSWER"))answerDone[0]=finished;
                                    if(phase.equals("STORY"))storySpoken[0]=true;
                                } catch(Exception e){throw new AssertionError(e);}
                                return original.invoke();
                            });
                        }
                        if(metrics.starts()>previousStarts[0]) {
                            require(metrics.starts()==previousStarts[0]+1,"Skipped observation of question start");
                            var director=(EntertainmentDirector)get(a,"entertainmentDirector");
                            var history=director.questionHistory();String id=history.get(history.size()-1);
                            require(ids.add(id),"Repeated question within first five turns");
                            JSONObject row=event("question",now).put("card",id).put("caption",caption);
                            if(previousAnswerDone[0]>0) {
                                long gap=now-previousAnswerDone[0];
                                row.put("gap_after_answer_ms",gap);
                                require(gap>=1950 && gap<=5500,"Transition outside 2–5s tolerance: "+gap);
                            }
                            questions.put(row);question[0]=row;questionDone[0]=0;answerDone[0]=0;
                            previousStarts[0]=metrics.starts();
                        }
                        if(question[0]!=null && caption.contains("ĐẾN LƯỢT MÌNH") && !question[0].has("response_wait_ms")) {
                            require(questionDone[0]>0,"Answer appeared without observed question TTS completion");
                            long wait=now-questionDone[0];question[0].put("response_wait_ms",wait);
                            require(wait>=2950 && wait<=5250,"Response wait outside 3–5s tolerance: "+wait);
                        }
                        if(metrics.resolutions()>previousResolutions[0]) {
                            require(answerDone[0]>0,"Resolution advanced without native answer completion");
                            question[0].put("answer_completed_at",answerDone[0]);
                            previousAnswerDone[0]=answerDone[0];
                            previousResolutions[0]=metrics.resolutions();
                            transitions.add(caption);
                            events.put(event("transition",now).put("caption",caption)
                                .put("scheduled_next",((Number)get(a,"nextEntertainmentAtElapsed")).longValue()));
                        }
                        Object controller=get(a,"storySession");
                        var player=(StoryPlaybackDirector)get(controller,"director");
                        if(player.episode()!=null && player.state()!=StoryPlaybackDirector.State.IDLE) {
                            require(previousResolutions[0]==5,"Story did not follow exactly five completed questions");
                            require(previousStarts[0]==5,"Sixth question overlapped interlude");
                            storyId[0]=(String)get(controller,"selectedId");
                            report.put("interleaved_story",storyId[0]);
                        }
                        require(metrics.voiceUnavailable()==0,"Voice fallback observed during timing test");
                    } catch(Exception e){throw new AssertionError(e);}
                });
                SystemClock.sleep(35);
            }
            require(storySpoken[0] && storyId[0]!=null,"No interleaved story native completion within 240s");
            require(questions.length()==5 && transitions.size()>=3,"Not enough unique observed questions/transition cues");
            int englishSamples=0;
            for(int i=0;i<questions.length();i++)if(questions.getJSONObject(i).optBoolean("english_before_answer"))englishSamples++;
            require(englishSamples==2,"Both English turns must play native sample before response wait and Vietnamese answer");
            report.put("english_samples_before_answer",englishSamples);
            try(var repo=new com.khuongnd.dexkids.story.StoryRepository(c)) {
                var entry=repo.get(storyId[0]);
                require(entry!=null && entry.getApproved() && entry.getEpisode().audience()==StoryEpisode.Audience.BOTH,
                    "Interleaved story was not a permitted matching-audience library entry");
            }
            try(var out=new FileOutputStream(new File(dir,"interleaved-story.png"))) {
                r.captureQaDisplay().compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
            }
            report.put("questions",questions).put("events",events).put("distinct_transition_cues",transitions.size())
                .put("elapsed_ms",SystemClock.elapsedRealtime()-started).put("status","PASS")
                .put("scope","Real production local TTS callbacks, first five questions, library story first narration completion; no microphone or AI; no human listening assertion");
            result.putString("t068_pacing",report.toString());
        } finally {
            if(!report.has("status"))report.put("status","FAILED_OR_INCOMPLETE").put("questions",questions).put("events",events);
            Files.write(new File(dir,"pacing.json").toPath(),report.toString(2).getBytes(StandardCharsets.UTF_8));
            if(current[0]!=null)r.runOnMainSync(current[0]::finish);
            r.waitForIdleSync();
            for(String name:names)restore(c.getSharedPreferences(name,0),backup.get(name));
            result.putBoolean("fixture_settings_restored",true);
        }
    }
    private static JSONObject event(String kind,long time)throws JSONException {
        return new JSONObject().put("kind",kind).put("elapsed_realtime_ms",time);
    }
    private static void set(Object o,String name,Object value)throws Exception {
        var f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);
    }
}
