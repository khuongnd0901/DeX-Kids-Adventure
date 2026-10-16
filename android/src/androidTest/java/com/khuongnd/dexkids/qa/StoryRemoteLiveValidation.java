package com.khuongnd.dexkids.qa;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.*;
import android.view.accessibility.AccessibilityNodeInfo;
import com.khuongnd.dexkids.story.*;
import com.khuongnd.dexkids.story.adventure.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/** Real Android HTTPS -> SQLite -> visible playback, followed by external force-stop/offline. */
final class StoryRemoteLiveValidation {
    static final String URL="https://raw.githubusercontent.com/khuongnd0901/DeX-Kids-Adventure/main/docs/story-source/stories.example.json?source=app";
    static final String[] PREFS={"story_source_v1","parent_settings","kids_ai_settings_v1","story_manager_v1"};
    final LifecycleValidationRunner runner;final Context context;final File stateFile,evidence;
    StoryRemoteLiveValidation(LifecycleValidationRunner r) {runner=r;context=r.getTargetContext();stateFile=new File(context.getFilesDir(),"t062-live-state.json");evidence=new File(context.getExternalFilesDir(null),"qa-t062-live");}
    static void run(LifecycleValidationRunner r,Bundle result,String mode)throws Exception {new StoryRemoteLiveValidation(r).run(result,mode);}
    void run(Bundle result,String mode)throws Exception {
        require(runner.fullHdQaDisplay(),"Select explicit FullHD emulator display0");require(evidence.isDirectory()||evidence.mkdirs(),"Evidence directory");
        if(mode.equals("t062_live_prepare"))prepare(result);
        else if(mode.equals("t062_live_offline"))offline(result);
        else if(mode.equals("t062_live_restore"))restore(result);
        else throw new IllegalArgumentException(mode);
    }
    void prepare(Bundle result)throws Exception {
        require(!stateFile.exists(),"Previous live test backup exists; restore it before retry");
        JSONObject state=new JSONObject(),backup=new JSONObject();
        for(String name:PREFS) {JSONObject values=new JSONObject();for(var entry:context.getSharedPreferences(name,0).getAll().entrySet()) {
            Object value=entry.getValue();values.put(entry.getKey(),new JSONObject().put("type",value.getClass().getSimpleName()).put("value",value instanceof Set?new JSONArray((Set<?>)value):value));
        }backup.put(name,values);}
        state.put("backup",backup).put("preparePid",android.os.Process.myPid());save(state);
        context.getSharedPreferences("kids_ai_settings_v1",0).edit().putBoolean("ai_quizzes",false).putBoolean("creative_trip_packs",false).putBoolean("automatic_trip_packs",false).commit();
        context.getSharedPreferences("parent_settings",0).edit().putBoolean("child_mic_optin",false).putBoolean("offline_tts",true).putBoolean("audio_only",false).putBoolean("ambient_music",false).commit();
        context.getSharedPreferences("story_manager_v1",0).edit().putBoolean("autoplay",false).commit();
        try(var repo=new StoryRepository(context)) {state.put("localDigest",localDigest(repo));save(state);}
        Activity manager=null;
        try {
            manager=runner.startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.StoryManagerActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            final Activity library=manager;await(()->Boolean.FALSE.equals(get(library,"busy")),15000,"Library worker busy");
            shot("00-library-before-source");click("Nguồn truyện online");setText(URL);
            var toggle=findText("Tự cập nhật khi mở app");require(toggle!=null,"Source enable checkbox missing");if(!toggle.isChecked())click("Tự cập nhật khi mở app");
            shot("01-source-url");click("Lưu");awaitIdle();
            require(StoryRemoteSync.INSTANCE.settings(context).getError().isEmpty(),"Real source sync failed: "+StoryRemoteSync.INSTANCE.settings(context).getError());
            try(var repo=new StoryRepository(context)) {
                var cache=repo.remotePack(URL);require(cache!=null && cache.getApplied(),"Live pack not activated");
                require(StoryPackCodec.parse(cache.getPayload().getBytes(StandardCharsets.UTF_8)).size()==3,"Live pack count");
                require(repo.list(false).stream().filter(StoryRepository.Entry::getRemote).count()==3,"Library must contain three remote stories");
                state.put("cacheSha",StoryPackCodec.digest(cache.getPayload())).put("cacheChecked",cache.getChecked());save(state);
                // The normal app client performs this real Android conditional request; no injected connection.
                var conditional=new StorySourceClient().fetch(URL,cache.getEtag(),cache.getModified());
                state.put("conditional304",conditional.unchanged());save(state);
            }
            SystemClock.sleep(500);shot("02-online-library");
        } finally {if(manager!=null){final Activity closing=manager;runner.runOnMainSync(closing::finish);runner.waitForIdleSync();}}
        JSONArray observations=new JSONArray();
        for(var audience:StoryEpisode.Audience.values())observations.put(play(audience,"online",true));
        try(var repo=new StoryRepository(context)) {
            String id=StoryPackCodec.remoteId(URL,"garden-both");int revision=repo.get(id).getEpisode().revision();
            state.put("cursor",repo.cursor(id,revision)).put("revision",revision).put("id",id);
            require(state.getString("localDigest").equals(localDigest(repo)),"Online sync changed local library");
        }
        state.put("online",observations).put("prepared",true);save(state);
        result.putString("live_https","PASS: actual source dialog URL+enable+save; real GitHub HTTPS; three remote stories; Android conditional304="+state.getBoolean("conditional304"));
        result.putString("live_playback",observations.toString());result.putString("next","External force-stop and network disable required before offline mode");
    }
    void offline(Bundle result)throws Exception {
        JSONObject state=load();require(state.optBoolean("prepared"),"Online preparation missing");
        require(state.getInt("preparePid")!=android.os.Process.myPid(),"External process restart did not occur");
        ConnectivityManager network=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);require(network.getActiveNetwork()==null,"Offline test requires no active Android network");
        try(var repo=new StoryRepository(context)) {var cache=repo.remotePack(URL);require(cache!=null && state.getString("cacheSha").equals(StoryPackCodec.digest(cache.getPayload())),"Pack lost across force-stop");require(repo.cursor(state.getString("id"),state.getInt("revision"))==state.getInt("cursor"),"Cursor lost across force-stop");}
        var observations=play(StoryEpisode.Audience.BOTH,"offline",false);awaitIdle();
        require(!StoryRemoteSync.INSTANCE.settings(context).getError().isEmpty(),"Offline startup did not record fetch error");
        try(var repo=new StoryRepository(context)) {
            require(state.getString("cacheSha").equals(StoryPackCodec.digest(repo.remotePack(URL).getPayload())),"Failed offline sync replaced cache");
            require(state.getString("localDigest").equals(localDigest(repo)),"Offline playback altered local library");
        }
        state.put("offline",observations).put("offlinePid",android.os.Process.myPid()).put("offlinePassed",true);save(state);
        result.putString("offline_restart","PASS: externally force-stopped process restarted with no active network; real failed fetch retained exact cache/cursor; visible BOTH story from cache");result.putString("offline_playback",observations.toString());
    }
    JSONObject play(StoryEpisode.Audience audience,String prefix,boolean question)throws Exception {
        context.getSharedPreferences("parent_settings",0).edit().putString("audience_mode",audience.name()).commit();
        String id=StoryPackCodec.remoteId(URL,"garden-"+audience.name().toLowerCase(Locale.ROOT));
        Activity activity=runner.startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.KidsActivity").putExtra("story_episode_id",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        try {
            await(()->{Object c=get(activity,"storySession");return c!=null && id.equals(get(c,"selectedId"));},20000,"Remote episode did not load: "+audience);
            Object controller=get(activity,"storySession");var director=(StoryPlaybackDirector)get(controller,"director");
            require(director.episode().audience()==audience,"Audience mismatch");
            await(()->String.valueOf(get(activity,"subtitleText")).contains("TRUYỆN TƯỞNG TƯỢNG"),10000,"No visible story caption");
            SystemClock.sleep(800);shot(prefix+"-"+audience+"-narration");
            int ttsSamples=0;long start=SystemClock.elapsedRealtime();boolean sawQuestion=false;
            do {
                if(Boolean.TRUE.equals(get(activity,"speechStarted")))ttsSamples++;
                Object speech=get(controller,"activeSpeech");
                if(speech instanceof StoryPlaybackDirector.Speech && ((StoryPlaybackDirector.Speech)speech).kind()==StoryPlaybackDirector.Kind.QUESTION){sawQuestion=true;break;}
                SystemClock.sleep(100);
            }while(question && SystemClock.elapsedRealtime()-start<45000);
            if(question)require(sawQuestion,"Narration did not reach question: "+audience);
            shot(prefix+"-"+audience+"-"+(sawQuestion?"question":"cached"));
            runner.runOnMainSync(()->StoryRemotePlaybackValidation.invoke(controller,"pause",new Class[]{long.class},0L));
            var cursor=director.cursor();int packed=cursor.scene()*1000+cursor.beat()*10+cursor.phase();
            try(var repo=new StoryRepository(context)) {await(()->repo.cursor(id,director.episode().revision())==packed,10000,"Playback cursor not committed");}
            return new JSONObject().put("audience",audience).put("questionObserved",sawQuestion).put("ttsActiveSamples",ttsSamples).put("scene",cursor.scene()).put("cursor",packed).put("caption",get(activity,"subtitleText"));
        } finally {runner.runOnMainSync(activity::finish);runner.waitForIdleSync();await(()->StoryRemotePlaybackValidation.leasesEmpty(),15000,"Playback lease leak");}
    }
    void restore(Bundle result)throws Exception {
        JSONObject state=load(),backup=state.getJSONObject("backup");
        for(String name:PREFS) {
            if(name.equals("story_source_v1"))continue; // Intentionally leave the requested test URL configured.
            var editor=context.getSharedPreferences(name,0).edit().clear();JSONObject values=backup.getJSONObject(name);
            for(var keys=values.keys();keys.hasNext();) {String key=keys.next();JSONObject item=values.getJSONObject(key);String type=item.getString("type");
                switch(type) {case "String"->editor.putString(key,item.getString("value"));case "Boolean"->editor.putBoolean(key,item.getBoolean("value"));case "Integer"->editor.putInt(key,item.getInt("value"));case "Long"->editor.putLong(key,item.getLong("value"));case "Float"->editor.putFloat(key,(float)item.getDouble("value"));default->{JSONArray array=item.getJSONArray("value");Set<String> set=new HashSet<>();for(int i=0;i<array.length();i++)set.add(array.getString(i));editor.putStringSet(key,set);}}
            }require(editor.commit(),"Preference restore failed: "+name);
        }
        JSONObject report=new JSONObject(state.toString());report.remove("backup");
        Files.write(new File(evidence,"results.json").toPath(),report.toString(2).getBytes(StandardCharsets.UTF_8));
        require(stateFile.delete(),"Backup state cleanup failed");
        Activity manager=runner.startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.StoryManagerActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        await(()->Boolean.FALSE.equals(get(manager,"busy")),15000,"Final library busy");shot("09-final-library");
        result.putString("restore","PASS: original parent/AI/autoplay preferences restored; source configuration left unchanged by cleanup; library opened for capture");
    }
    String localDigest(StoryRepository repo)throws Exception {JSONArray tables=new JSONArray();for(String table:List.of("episode","revision","playback")){JSONArray rows=new JSONArray();try(var c=repo.getReadableDatabase().rawQuery("SELECT * FROM "+table+" ORDER BY 1,2",null)){while(c.moveToNext()){JSONArray row=new JSONArray();for(int i=0;i<c.getColumnCount();i++)row.put(c.getString(i));rows.put(row);}}tables.put(rows);}return StoryPackCodec.digest(tables.toString());}
    void awaitIdle()throws Exception {var f=StoryRemoteSync.class.getDeclaredField("running");f.setAccessible(true);await(()->!(boolean)f.get(null),65000,"Real HTTPS sync did not finish");}
    interface Check{boolean get()throws Exception;}
    void await(Check c,long timeout,String reason)throws Exception {long until=SystemClock.elapsedRealtime()+timeout;while(SystemClock.elapsedRealtime()<until){if(c.get())return;SystemClock.sleep(80);}throw new AssertionError(reason);}
    Object get(Object o,String n)throws Exception{return StoryRemotePlaybackValidation.get(o,n);}
    void require(boolean value,String reason){StoryRemoteValidation.check(value,reason);}
    void save(JSONObject state)throws Exception {Files.write(stateFile.toPath(),state.toString(2).getBytes(StandardCharsets.UTF_8));}
    JSONObject load()throws Exception{return new JSONObject(new String(Files.readAllBytes(stateFile.toPath()),StandardCharsets.UTF_8));}
    void shot(String name)throws Exception {SystemClock.sleep(300);Bitmap bitmap=runner.captureQaDisplay();require(bitmap!=null && bitmap.getWidth()==1920 && bitmap.getHeight()==1080,"FullHD screenshot missing");try(var out=new FileOutputStream(new File(evidence,name+".png"))){require(bitmap.compress(Bitmap.CompressFormat.PNG,100,out),"Screenshot failed");}finally{bitmap.recycle();}}
    AccessibilityNodeInfo findText(String text){var root=runner.getUiAutomation().getRootInActiveWindow();if(root!=null)for(var n:root.findAccessibilityNodeInfosByText(text))if(n.isVisibleToUser() && n.getText()!=null && n.getText().toString().equalsIgnoreCase(text))return n;return null;}
    void click(String text)throws Exception {await(()->{var n=findText(text);if(n==null)return false;while(n!=null&&!n.isClickable())n=n.getParent();return n!=null && n.performAction(AccessibilityNodeInfo.ACTION_CLICK);},10000,"Visible button missing: "+text);SystemClock.sleep(400);}
    AccessibilityNodeInfo edit(AccessibilityNodeInfo n){if(n==null)return null;if(n.isVisibleToUser() && "android.widget.EditText".contentEquals(n.getClassName()))return n;for(int i=0;i<n.getChildCount();i++){var e=edit(n.getChild(i));if(e!=null)return e;}return null;}
    void setText(String text){var e=edit(runner.getUiAutomation().getRootInActiveWindow());require(e!=null,"URL editor missing");Bundle args=new Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text);require(e.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args),"URL entry failed");}
}
