package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import com.khuongnd.dexkids.story.StoryRepository;
import com.khuongnd.dexkids.story.adventure.*;
import java.util.HashMap;

/** Actual Activity recreation plus production controller resume/revision checks; no AI. */
final class StoryLifecycleValidation {
    static void run(LifecycleValidationRunner r,Bundle result)throws Exception {
        var context=r.getTargetContext();var parent=context.getSharedPreferences("parent_settings",0);var ai=context.getSharedPreferences("kids_ai_settings_v1",0);
        var oldParent=new HashMap<>(parent.getAll());var oldAi=new HashMap<>(ai.getAll());
        Activity[] activity={null};var monitor=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);
        String id="qa-t059-lifecycle";
        try(var repository=new StoryRepository(context)) {
            parent.edit().putString("audience_mode","BOTH").putBoolean("child_mic_optin",false).commit();
            ai.edit().putBoolean("ai_quizzes",false).putBoolean("creative_trip_packs",false).putBoolean("child_text_cloud_explicit",false).commit();
            var sample=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
            var episode=new StoryEpisode(id,1,1,sample.title(),sample.audience(),5,4,sample.educationalGoal(),sample.synopsis(),sample.scenes());
            var saved=repository.save(episode,true,null);repository.approve(id,saved.getEpisode().revision());repository.cursor(id,saved.getEpisode().revision(),1000);
            activity[0]=r.startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.KidsActivity").putExtra("story_episode_id",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            Object controller=awaitController(activity[0],id);final Object original=controller;
            r.runOnMainSync(()->invoke(original,"pause",new Class[]{long.class},0L));
            var director=(StoryPlaybackDirector)field(controller,"director");var cursor=director.cursor();
            int packed=cursor.scene()*1000+cursor.beat()*10+cursor.phase();
            await(()->repository.cursor(id,saved.getEpisode().revision())==packed,"Pause cursor not persisted");
            // Observe only the new launch; the initial monitor retains its old Activity.
            r.removeMonitor(monitor);
            monitor=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);
            r.runOnMainSync(()->activity[0].recreate());
            Activity recreated=r.waitForMonitorWithTimeout(monitor,10000);require(recreated!=null&&recreated!=activity[0],"Actual Activity recreation missing");activity[0]=recreated;
            controller=awaitController(recreated,id);director=(StoryPlaybackDirector)field(controller,"director");
            require(id.equals(recreated.getIntent().getStringExtra("story_episode_id")),"Episode ID not restored");
            require(director.cursor().scene()==cursor.scene(),"Scene cursor lost during Activity recreation");
            final Object resumed=controller;
            r.runOnMainSync(()->invoke(resumed,"pause",new Class[]{long.class},0L));
            repository.delete(id,true);
            r.runOnMainSync(()->invoke(resumed,"resume",new Class[]{}));
            await(()->field(resumed,"selectedId")==null,"Deleted episode resumed from RAM");
            require(((StoryPlaybackDirector)field(resumed,"director")).state()==StoryPlaybackDirector.State.IDLE,"Deleted episode retained active director");
            repository.delete(id,false);
            r.runOnMainSync(()->invoke(resumed,"load",new Class[]{String.class,boolean.class},id,false));await(()->id.equals(field(resumed,"selectedId")),"Reload missing");
            r.runOnMainSync(()->invoke(resumed,"pause",new Class[]{long.class},0L));
            var current=repository.get(id);repository.save(current.getEpisode(),false,current.getEpisode().revision());
            // Repeated resume calls while revalidation is queued must coalesce. Stop invalidates every queued callback.
            r.runOnMainSync(()->{invoke(resumed,"resume",new Class[]{});invoke(resumed,"resume",new Class[]{});});
            await(()->field(resumed,"selectedId")==null,"Changed revision resumed old approved RAM content");
            repository.undo(id);
            r.runOnMainSync(()->{invoke(resumed,"load",new Class[]{String.class,boolean.class},id,false);invoke(resumed,"stop",new Class[]{});});
            SystemClock.sleep(1200);require(field(resumed,"selectedId")==null,"Stale load callback revived stopped story");
            require(((StoryPlaybackDirector)field(resumed,"director")).state()==StoryPlaybackDirector.State.IDLE,"Stale callback changed stopped director");
            result.putString("t059_lifecycle","PASS: actual Activity recreation episode ID/scene + persisted pause cursor; deleted/current-edited revisions rejected on resume; coalesced resume; stopped stale load callback rejected; AI disabled");
        } finally {
            r.runOnMainSync(()->{if(activity[0]!=null)activity[0].finish();});r.removeMonitor(monitor);
            try(var repository=new StoryRepository(context)){if(repository.get(id)!=null){repository.delete(id,true);repository.purgeDeleted(id);}}
            restore(parent,oldParent);restore(ai,oldAi);
        }
    }
    private static Object awaitController(Activity activity,String id)throws Exception {final Object[] found={null};await(()->{Object controller=field(activity,"storySession");if(controller!=null&&id.equals(field(controller,"selectedId"))){found[0]=controller;return true;}return false;},"Story load missing");return found[0];}
    private interface Check{boolean get()throws Exception;}
    private static void await(Check check,String reason)throws Exception{long until=SystemClock.elapsedRealtime()+12000;while(SystemClock.elapsedRealtime()<until){if(check.get())return;SystemClock.sleep(80);}throw new AssertionError(reason);}
    private static Object field(Object object,String name)throws Exception{var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    private static void invoke(Object object,String name,Class<?>[] types,Object...args){try{var method=object.getClass().getDeclaredMethod(name,types);method.setAccessible(true);method.invoke(object,args);}catch(Exception e){throw new AssertionError(e);}}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static void restore(android.content.SharedPreferences prefs,java.util.Map<String,?> rows){var edit=prefs.edit().clear();for(var row:rows.entrySet()){var v=row.getValue();if(v instanceof String)edit.putString(row.getKey(),(String)v);else if(v instanceof Boolean)edit.putBoolean(row.getKey(),(Boolean)v);else if(v instanceof Integer)edit.putInt(row.getKey(),(Integer)v);else if(v instanceof Long)edit.putLong(row.getKey(),(Long)v);else if(v instanceof Float)edit.putFloat(row.getKey(),(Float)v);}require(edit.commit(),"Preference restore failed");}
}
