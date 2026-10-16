package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.*;
import android.os.*;
import com.khuongnd.dexkids.story.*;
import com.khuongnd.dexkids.story.adventure.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Real KidsActivity/controller integration for trusted remote content; mocked HTTPS only. */
final class StoryRemotePlaybackValidation {
    static void run(LifecycleValidationRunner runner,Bundle result) throws Exception {
        Context context=runner.getTargetContext();
        String source="https://example.org/t062-playback.json";
        var sourcePrefs=context.getSharedPreferences("story_source_v1",0);
        var parent=context.getSharedPreferences("parent_settings",0);
        var ai=context.getSharedPreferences("kids_ai_settings_v1",0);
        var story=context.getSharedPreferences("story_manager_v1",0);
        var oldSource=new HashMap<>(sourcePrefs.getAll());var oldParent=new HashMap<>(parent.getAll());
        var oldAi=new HashMap<>(ai.getAll());var oldStory=new HashMap<>(story.getAll());
        var field=StoryRemoteSync.class.getDeclaredField("client");field.setAccessible(true);Object oldClient=field.get(null);
        var queue=new ConcurrentLinkedQueue<StoryRemoteValidation.Response>();
        field.set(null,new StorySourceClient(url->{var response=queue.poll();if(response==null)throw new java.io.IOException("Injected offline");return response;}));
        Activity activity=null;
        try {
            ai.edit().putBoolean("ai_quizzes",false).putBoolean("creative_trip_packs",false).putBoolean("automatic_trip_packs",false).commit();
            parent.edit().putBoolean("child_mic_optin",false).commit();story.edit().putBoolean("autoplay",false).commit();
            var sau=StoryFixtures.educational(StoryEpisode.Audience.SAU);var ong=StoryFixtures.educational(StoryEpisode.Audience.ONG);var both=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
            queue.add(new StoryRemoteValidation.Response(200,StoryRemoteValidation.pack(sau,ong,both)));
            StoryRemoteSync.INSTANCE.configure(context,source,true);StoryRemoteValidation.awaitIdle();
            try(var repository=new StoryRepository(context)) {
                for(var audience:StoryEpisode.Audience.values()) {
                    parent.edit().putString("audience_mode",audience.name()).commit();
                    String id=StoryPackCodec.remoteId(source,StoryFixtures.educational(audience).id());
                    activity=runner.startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.KidsActivity").putExtra("story_episode_id",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    final Activity current=activity;
                    await(()->{var c=get(current,"storySession");return c!=null && id.equals(get(c,"selectedId"));},"Remote story not loaded: "+audience);
                    Object controller=get(activity,"storySession");
                    runner.runOnMainSync(()->invoke(controller,"pause",new Class[]{long.class},0L));
                    var director=(StoryPlaybackDirector)get(controller,"director");
                    require(director.episode().audience()==audience,"Remote audience mismatch");
                    // A received update must not invalidate paused playback or its resume check.
                    if(audience==StoryEpisode.Audience.BOTH) {
                        var changed=new StoryEpisode(both.id(),1,2,"Khu vườn của tình bạn",both.audience(),5,4,both.educationalGoal(),both.synopsis(),both.scenes());
                        queue.add(new StoryRemoteValidation.Response(200,StoryRemoteValidation.pack(sau,ong,changed)));
                        StoryRemoteSync.INSTANCE.request(context,true);StoryRemoteValidation.awaitIdle();
                        require(repository.get(id).getEpisode().revision()==1 && !repository.remotePack(source).getApplied(),"Active remote update was not deferred");
                        runner.runOnMainSync(()->invoke(controller,"resume",new Class[]{}));
                        await(()->id.equals(get(controller,"selectedId")),"Pending update rejected original playback");
                        runner.runOnMainSync(()->invoke(controller,"stop",new Class[]{}));
                        await(()->repository.get(id).getEpisode().revision()==2,"Stop did not activate pending pack");
                        runner.runOnMainSync(()->invoke(controller,"load",new Class[]{String.class,boolean.class},id,false));
                        await(()->id.equals(get(controller,"selectedId")) && ((StoryPlaybackDirector)get(controller,"director")).episode().revision()==2,"New playback did not use new revision");
                        runner.runOnMainSync(()->invoke(controller,"pause",new Class[]{long.class},0L));
                        queue.add(new StoryRemoteValidation.Response(200,StoryRemoteValidation.pack()));
                        StoryRemoteSync.INSTANCE.request(context,true);StoryRemoteValidation.awaitIdle();
                        require(repository.get(id)!=null,"Active story removed early");
                        runner.runOnMainSync(()->invoke(controller,"stop",new Class[]{}));
                        await(()->repository.get(id)==null,"Removed story remains after stop");
                    }
                    runner.runOnMainSync(current::finish);runner.waitForIdleSync();activity=null;
                    await(()->leasesEmpty(),"Playback lease leaked after Activity close");
                }
            }
            result.putString("remote_playback","PASS: actual KidsActivity loads SAU/ONG/BOTH remote episodes without parent/AI review; pause/resume retains current revision during update; stop atomically activates new revision; next load sees new content; removal waits for active playback; Activity cleanup releases all leases");
        } finally {
            if(activity!=null){final Activity closing=activity;runner.runOnMainSync(closing::finish);runner.waitForIdleSync();}
            await(()->leasesEmpty(),"Cleanup lease not released");StoryRemoteValidation.awaitIdle();
            sourcePrefs.edit().putBoolean("enabled",false).commit();
            try(var repository=new StoryRepository(context)) {var db=repository.getWritableDatabase();db.delete("remote_episode","source=?",new String[]{source});db.delete("remote_pack","source=?",new String[]{source});}
            field.set(null,oldClient);restore(sourcePrefs,oldSource);restore(parent,oldParent);restore(ai,oldAi);restore(story,oldStory);
        }
    }
    static boolean leasesEmpty() throws Exception {var f=StoryRemoteSync.class.getDeclaredField("leases");f.setAccessible(true);return ((Set<?>)f.get(null)).isEmpty();}
    interface Check {boolean get()throws Exception;}
    static void await(Check check,String message)throws Exception {long end=SystemClock.elapsedRealtime()+15000;while(SystemClock.elapsedRealtime()<end){if(check.get())return;SystemClock.sleep(60);}throw new AssertionError(message);}
    static Object get(Object o,String name)throws Exception {var f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    static void invoke(Object o,String name,Class<?>[] types,Object... args){try{var m=o.getClass().getDeclaredMethod(name,types);m.setAccessible(true);m.invoke(o,args);}catch(Exception e){throw new AssertionError(e);}}
    static void require(boolean value,String message){StoryRemoteValidation.check(value,message);}
    static void restore(SharedPreferences prefs,Map<String,?> values){var e=prefs.edit().clear();values.forEach((k,v)->{if(v instanceof String)e.putString(k,(String)v);else if(v instanceof Boolean)e.putBoolean(k,(Boolean)v);else if(v instanceof Long)e.putLong(k,(Long)v);else if(v instanceof Integer)e.putInt(k,(Integer)v);else if(v instanceof Float)e.putFloat(k,(Float)v);else if(v instanceof Set)e.putStringSet(k,(Set<String>)v);});require(e.commit(),"Restore preferences failed");}
}
