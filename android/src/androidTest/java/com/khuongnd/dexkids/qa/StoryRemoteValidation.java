package com.khuongnd.dexkids.qa;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.khuongnd.dexkids.story.*;
import com.khuongnd.dexkids.story.adventure.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Isolated SQLite + process coordinator tests; fake HTTPS transport, no cloud or child data. */
final class StoryRemoteValidation {
    static final String URL="https://example.org/stories.json";
    static void check(boolean value,String message) {LifecycleValidationRunner.require(value,message);}
    static String pack(StoryEpisode... stories) {
        return "{\"schemaVersion\":1,\"stories\":["+String.join(",",Arrays.stream(stories).map(StoryEpisodeCodec::toJson).toList())+"]}";
    }
    static class Response extends HttpURLConnection {
        final int status; final String payload;
        Response(int status,String payload) throws Exception {super(new URL(URL));this.status=status;this.payload=payload;}
        public void disconnect(){} public boolean usingProxy(){return false;} public void connect(){}
        public int getResponseCode(){return status;}
        public InputStream getInputStream() throws IOException {return new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));}
        public String getHeaderField(String name){return name.equals("ETag")?"\"test-v1\"":null;}
        public long getContentLengthLong(){return -1;}
    }
    static void awaitIdle() throws Exception {
        var field=StoryRemoteSync.class.getDeclaredField("running");field.setAccessible(true);
        long until=android.os.SystemClock.elapsedRealtime()+15000;
        while((boolean)field.get(null) && android.os.SystemClock.elapsedRealtime()<until)Thread.sleep(20);
        check(!(boolean)field.get(null),"Sync did not finish");
    }
    static void run(LifecycleValidationRunner runner,Bundle result) throws Exception {
        Context base=runner.getTargetContext();File path=new File(base.getCacheDir(),"t062-isolated.db");SQLiteDatabase.deleteDatabase(path);
        Context context=new ContextWrapper(base) {
            public Context getApplicationContext(){return this;}
            public File getDatabasePath(String name){return path;}
            public SQLiteDatabase openOrCreateDatabase(String n,int m,SQLiteDatabase.CursorFactory f){return SQLiteDatabase.openOrCreateDatabase(path,f);}
            public SQLiteDatabase openOrCreateDatabase(String n,int m,SQLiteDatabase.CursorFactory f,android.database.DatabaseErrorHandler h){return SQLiteDatabase.openOrCreateDatabase(path.getPath(),f,h);}
            public SharedPreferences getSharedPreferences(String name,int mode){return base.getSharedPreferences("t062-isolated-"+name,mode);}
        };
        var prefs=context.getSharedPreferences("story_source_v1",0);prefs.edit().clear().commit();
        var local=StoryFixtures.educational(StoryEpisode.Audience.SAU);
        // Seed the exact previous database format before opening the v2 helper.
        try(var db=SQLiteDatabase.openOrCreateDatabase(path,null)) {
            db.execSQL("CREATE TABLE episode(id TEXT PRIMARY KEY,revision INTEGER NOT NULL,favourite INTEGER NOT NULL DEFAULT 0,deleted INTEGER NOT NULL DEFAULT 0,lastPlayed INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE TABLE revision(id TEXT NOT NULL,revision INTEGER NOT NULL,json TEXT NOT NULL,reviewed INTEGER NOT NULL,approved INTEGER NOT NULL,PRIMARY KEY(id,revision))");
            db.execSQL("CREATE TABLE playback(id TEXT PRIMARY KEY,revision INTEGER NOT NULL,beat INTEGER NOT NULL)");
            db.execSQL("INSERT INTO episode VALUES(?,1,1,0,123)",new Object[]{local.id()});
            db.execSQL("INSERT INTO revision VALUES(?,1,?,1,1)",new Object[]{local.id(),StoryEpisodeCodec.toJson(local)});
            db.execSQL("INSERT INTO playback VALUES(?,1,1002)",new Object[]{local.id()});db.setVersion(1);
        }
        var clientField=StoryRemoteSync.class.getDeclaredField("client");clientField.setAccessible(true);Object original=clientField.get(null);
        var count=new AtomicInteger();var queue=new java.util.concurrent.ConcurrentLinkedQueue<Response>();
        clientField.set(null,new StorySourceClient(url->{count.incrementAndGet();Response response=queue.poll();if(response==null)throw new IOException("Injected offline");return response;}));
        Object lease=new Object();
        try {
            try(var repo=new StoryRepository(context)) {
                check(repo.getWritableDatabase().getVersion()==2,"Migration version");
                check(repo.get(local.id()).getApproved() && repo.get(local.id()).getFavourite() && repo.cursor(local.id(),1)==1002,"Migration lost local content/metadata");
                result.putString("migration","PASS: actual v1 to v2 SQLite with approved local/favourite/cursor preserved");
            }
            var sau=StoryFixtures.educational(StoryEpisode.Audience.SAU);var ong=StoryFixtures.educational(StoryEpisode.Audience.ONG);var both=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
            queue.add(new Response(200,pack(sau,ong,both)));
            StoryRemoteSync.INSTANCE.configure(context,URL,true);awaitIdle();
            String id=StoryPackCodec.remoteId(URL,sau.id());
            try(var repo=new StoryRepository(context)) {
                check(repo.list(false).size()==4,"Initial remote snapshot missing");
                check(repo.get(id).getApproved() && !repo.get(id).getReviewed() && repo.get(id).getRemote(),"Remote provenance/approval wrong");
                for(var audience:StoryEpisode.Audience.values())check(repo.next(audience,0)!=null,"Missing audience "+audience);
                repo.cursor(id,1,1002);repo.favourite(id,true);
                boolean protectedRemote=false;try {repo.save(repo.get(id).getEpisode(),true,null);}catch(IllegalArgumentException expected){protectedRemote=true;}
                check(protectedRemote,"Remote write accepted");
                var copy=repo.copyRemote(id);check(!copy.getApproved() && !copy.getRemote(),"Copy must be local draft");
            }
            int before=count.get();StoryRemoteSync.INSTANCE.request(context,false);awaitIdle();check(count.get()==before,"Foreground throttle failed");
            queue.add(new Response(304,""));StoryRemoteSync.INSTANCE.request(context,true);awaitIdle();
            try(var repo=new StoryRepository(context)) {check(repo.get(id).getEpisode().revision()==1 && repo.cursor(id,1)==1002,"304 mutated content");}
            // Even changing only the publisher revision must preserve local cursor/revision.
            queue.add(new Response(200,pack(StoryPackCodec.identity(sau,sau.id(),99),ong,both)));
            StoryRemoteSync.INSTANCE.request(context,true);awaitIdle();
            try(var repo=new StoryRepository(context)) {check(repo.get(id).getEpisode().revision()==1 && repo.cursor(id,1)==1002,"Identical content created revision");}
            var changed=new StoryEpisode(sau.id(),1,2,"Khu vườn và tình bạn",sau.audience(),5,4,sau.educationalGoal(),sau.synopsis(),sau.scenes());
            StoryRemoteSync.INSTANCE.acquire(lease);queue.add(new Response(200,pack(changed,both)));
            StoryRemoteSync.INSTANCE.request(context,true);awaitIdle();
            try(var repo=new StoryRepository(context)) {check(repo.get(id).getEpisode().revision()==1,"Active playback mutated");check(!repo.remotePack(URL).getApplied(),"Snapshot should be pending");}
            StoryRemoteSync.INSTANCE.release(context,lease);
            try(var repo=new StoryRepository(context)) {
                check(repo.get(id).getEpisode().revision()==2 && repo.cursor(id,2)==0 && repo.get(id).getFavourite(),"Update lost cursor/favourite semantics");
                check(repo.get(StoryPackCodec.remoteId(URL,ong.id()))==null,"Removed story still selectable");
                check(repo.get(local.id()).getApproved() && repo.cursor(local.id(),1)==1002,"Sync altered local library");
            }
            for(int status:new int[]{200,404,500}) {
                queue.add(new Response(status,"{broken"));StoryRemoteSync.INSTANCE.request(context,true);awaitIdle();
                try(var repo=new StoryRepository(context)) {check(repo.get(id).getEpisode().revision()==2,"Failure changed last-good snapshot");}
                check(!StoryRemoteSync.INSTANCE.settings(context).getError().isEmpty(),"Missing sync error");
            }
            // A staged pack that collides with a local ID must not poison future corrections.
            String collisionId=StoryPackCodec.remoteId(URL,ong.id());
            try(var repo=new StoryRepository(context)) {
                repo.save(StoryPackCodec.identity(ong,collisionId,1),false,null);
                repo.stageRemote(URL,pack(ong),null,null,System.currentTimeMillis());
            }
            queue.add(new Response(200,pack(changed,both)));StoryRemoteSync.INSTANCE.request(context,true);awaitIdle();
            try(var repo=new StoryRepository(context)) {
                check(repo.remotePack(URL).getApplied() && repo.get(id).getEpisode().revision()==2,"Unactivatable pending pack blocked correction");
                repo.delete(collisionId,true);repo.purgeDeleted(collisionId);
            }
            // A second source with identical publisher IDs has independent identity/cache.
            String another="https://another.example.org/stories.json";
            queue.add(new Response(200,pack(ong)));StoryRemoteSync.INSTANCE.configure(context,another,true);awaitIdle();
            try(var repo=new StoryRepository(context)) {check(!repo.selectable(id),"Old URL source selected");check(repo.selectable(StoryPackCodec.remoteId(another,ong.id())),"New URL source missing");}
            // No response queued: source cache must remain usable after offline reconfiguration.
            StoryRemoteSync.INSTANCE.configure(context,URL,true);awaitIdle();
            try(var repo=new StoryRepository(context)) {check(repo.selectable(id) && repo.get(id).getEpisode().revision()==2,"Offline source cache lost");}
            StoryRemoteSync.INSTANCE.configure(context,URL,false);
            try(var repo=new StoryRepository(context)) {check(!repo.selectable(id) && repo.list(false).stream().noneMatch(StoryRepository.Entry::getRemote),"Disabled remote still selectable");check(repo.get(local.id()).getApproved(),"Disable changed local approval");}
            result.putString("remote_sync","PASS: mocked HTTPS + real SQLite add/all audiences,304,throttle,identical content,lease-deferred update/removal,cursor reset,favourite,copy/read-only,malformed/404/500/offline last-good,URL switch,isolation,disable,unactivatable-pending recovery");
        } finally {
            awaitIdle();StoryRemoteSync.INSTANCE.release(context,lease);clientField.set(null,original);prefs.edit().clear().commit();SQLiteDatabase.deleteDatabase(path);
        }
    }
}
