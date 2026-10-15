package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import com.khuongnd.dexkids.ai.*;
import com.khuongnd.dexkids.story.*;
import com.khuongnd.dexkids.story.adventure.*;
import kotlin.jvm.functions.Function0;
import static com.khuongnd.dexkids.qa.ArtVisualValidation.*;

/** Real activity eligibility closure, automatic AI ON, ready fake provider; zero HTTP. */
final class StoryReplayValidation {
    static void run(LifecycleValidationRunner r,Bundle result)throws Exception {
        var context=r.getTargetContext();var ai=context.getSharedPreferences("kids_ai_settings_v1",0);var oldAi=ai.getAll();
        var p=context.getSharedPreferences("parent_settings",0);var oldP=p.getAll();var storyPrefs=context.getSharedPreferences("story_manager_v1",0);var oldStory=storyPrefs.getAll();Activity a=null;String id="qa-no-network";
        try(var repo=new StoryRepository(context)) {
            ai.edit().putBoolean("ai_quizzes",true).putBoolean("automatic_trip_packs",true).putBoolean("creative_trip_packs",true).commit();
            p.edit().putString("audience_mode","BOTH").putBoolean("child_mic_optin",false).putBoolean("offline_tts",false).commit();
            var s=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
            var saved=repo.save(new StoryEpisode(id,1,1,s.title(),s.audience(),5,4,s.educationalGoal(),s.synopsis(),s.scenes()),true,null);repo.approve(id,saved.getEpisode().revision());
            a=r.startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.KidsActivity").putExtra("story_episode_id",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            final Activity activity=a;long until=SystemClock.elapsedRealtime()+10000;
            while(SystemClock.elapsedRealtime()<until && field(field(a,"storySession"),"selectedId")==null)SystemClock.sleep(50);
            Object session=field(a,"storySession");require(id.equals(field(session,"selectedId")),"Approved replay did not load");
            Object coordinator=field(a,"tripPackCoordinator");
            @SuppressWarnings("unchecked") Function0<Boolean> gate=(Function0<Boolean>)field(coordinator,"isSessionActive");
            r.runOnMainSync(()->require(!gate.invoke(),"Story replay leaves automatic generation eligible"));
            require((boolean)field(coordinator,"firstStart") && field(coordinator,"thread")==null,"Replay started automatic pack worker");
            int[] calls={0};TripPackGateway ready=new TripPackGateway(){
                public boolean ready(){calls[0]++;return true;}
                public void beginCreativeCycle(){throw new AssertionError("Replay started generation");}
                public TripQuestionPack generateTripPack(OfflineLearningCatalog c,EntertainmentDirector.Audience audience,java.util.List<String> recent){throw new AssertionError("Replay requested pack");}
                public CreativeLessonPack generateCreativePack(EntertainmentDirector.Audience audience,int sau,int ong,java.util.List<String> recent,java.util.List<String> prompts){throw new AssertionError("Replay requested creative pack");}
            };
            TripPackCoordinator[] fake={null};r.runOnMainSync(()->{
                fake[0]=new TripPackCoordinator(context,OfflineLearningCatalog.empty(),EntertainmentDirector.Audience.BOTH,5,4,gate,ready,ms->kotlin.Unit.INSTANCE);
                fake[0].resume(true);
            });SystemClock.sleep(250);
            require(calls[0]==0 && (boolean)field(fake[0],"firstStart") && field(fake[0],"thread")==null,"Ready provider reached during replay");
            r.runOnMainSync(()->{try{var stop=session.getClass().getDeclaredMethod("stop");stop.setAccessible(true);stop.invoke(session);require(gate.invoke(),"Legacy eligibility not restored");fake[0].resume(false);}catch(Exception e){throw new AssertionError(e);}});
            CreativeCoordinatorValidation.idle(r,fake[0]);require(calls[0]==0,"Post-story cache-only resume queried provider");
            r.runOnMainSync(activity::finish);r.waitForIdleSync();
            storyPrefs.edit().putBoolean("autoplay",true).commit();
            a=r.startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.KidsActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            until=SystemClock.elapsedRealtime()+10000;
            while(SystemClock.elapsedRealtime()<until && field(field(a,"storySession"),"selectedId")==null)SystemClock.sleep(50);
            require(field(field(a,"storySession"),"selectedId")!=null,"Autoplay did not resolve approved local episode at startup");
            Object autoCoordinator=field(a,"tripPackCoordinator");
            @SuppressWarnings("unchecked") Function0<Boolean> autoGate=(Function0<Boolean>)field(autoCoordinator,"isSessionActive");
            r.runOnMainSync(()->require(!autoGate.invoke(),"Autoplay startup leaves automatic AI eligible"));
            require((boolean)field(autoCoordinator,"firstStart") && field(autoCoordinator,"thread")==null,"Autoplay started automatic pack worker before local replay");
            result.putString("t059_replay_no_network","PASS: actual activity closure blocks automatic packs during approved replay with AI+automatic+creative enabled and ready fake provider; zero gateway calls; cache-only legacy resume; automatic story startup resolves local first; no HTTP");
        } finally {
            final Activity closing=a;r.runOnMainSync(()->{if(closing!=null)closing.finish();});
            try(var repo=new StoryRepository(context)){if(repo.get(id)!=null){repo.delete(id,true);repo.purgeDeleted(id);}}
            restore(ai,oldAi);restore(p,oldP);restore(storyPrefs,oldStory);
        }
    }
}
