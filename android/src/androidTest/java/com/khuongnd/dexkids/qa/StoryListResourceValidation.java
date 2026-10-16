package com.khuongnd.dexkids.qa;

import android.content.*;
import android.database.sqlite.*;
import android.os.Bundle;
import com.khuongnd.dexkids.story.StoryRepository;
import com.khuongnd.dexkids.story.adventure.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static com.khuongnd.dexkids.qa.ArtVisualValidation.require;

/** Isolated mixed library: measure list latency and retain live metadata semantics. */
final class StoryListResourceValidation {
    static void run(LifecycleValidationRunner r,Bundle result) throws Exception {
        Context base=r.getTargetContext();File path=new File(base.getCacheDir(),"t066-list.db");
        SQLiteDatabase.deleteDatabase(path);AtomicInteger queries=new AtomicInteger();
        SQLiteDatabase.CursorFactory counted=(db,driver,table,query)->{queries.incrementAndGet();return new SQLiteCursor(driver,table,query);};
        Context c=new ContextWrapper(base) {
            public Context getApplicationContext(){return this;}
            public File getDatabasePath(String name){return path;}
            public SQLiteDatabase openOrCreateDatabase(String n,int m,SQLiteDatabase.CursorFactory f){return SQLiteDatabase.openOrCreateDatabase(path,counted);}
            public SQLiteDatabase openOrCreateDatabase(String n,int m,SQLiteDatabase.CursorFactory f,android.database.DatabaseErrorHandler h){return SQLiteDatabase.openOrCreateDatabase(path.getPath(),counted,h);}
            public SharedPreferences getSharedPreferences(String n,int m){return base.getSharedPreferences("t066-list-"+n,m);}
        };
        String source="https://example.org/library.json";
        try(var repo=new StoryRepository(c)) {
            var template=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
            for(int i=0;i<40;i++) {
                var e=StoryPackCodec.identity(template,String.format(Locale.ROOT,"local-%02d",i),1);
                repo.save(e,true,null);repo.approve(e.id(),1);
            }
            var remote=new ArrayList<StoryEpisode>();
            for(int i=0;i<30;i++)remote.add(StoryPackCodec.identity(template,"remote-"+i,1));
            repo.stageRemote(source,StoryRemoteValidation.pack(remote.toArray(new StoryEpisode[0])),null,null,1);
            repo.activateRemote(source);
            c.getSharedPreferences("story_source_v1",0).edit().putString("url",source).putBoolean("enabled",true).commit();
            repo.favourite("local-03",true);repo.markPlayed("local-02");repo.delete("local-01",true);
            var remoteId=StoryPackCodec.remoteId(source,"remote-0");repo.favourite(remoteId,true);
            var warm=repo.list(false); // Warm immutable content cache, retaining fresh SQL metadata reads.
            var identities=Collections.newSetFromMap(new IdentityHashMap<StoryEpisode,Boolean>());
            warm.forEach(e->identities.add(e.getEpisode()));
            int start=queries.get();long at=System.nanoTime();
            for(int i=0;i<20;i++) {
                var entries=repo.list(false);
                require(entries.size()==69,"Visible mixed count");
                require(entries.get(0).getEpisode().id().equals("local-03"),"Local favourite order");
                require(entries.get(39).getEpisode().id().equals(remoteId),"Remote favourite order/local-first partition");
                require(entries.stream().noneMatch(e->e.getDeleted()),"Deleted story visible");
            }
            result.putString("query_count",queries.get()==start ? "NOT_VERIFIED: platform bypasses Context cursor factory" : Integer.toString((queries.get()-start)/20));
            result.putDouble("warm_list_mean_ms",(System.nanoTime()-at)/20e6);
            long reused=repo.list(false).stream().filter(e->identities.contains(e.getEpisode())).count();
            require(reused>=64,"Oversized scan thrashed immutable content cache");
            require(((Map<?,?>)ArtVisualValidation.field(repo,"validatedEpisodes")).size()<=64,"Persistent cache exceeded64");
            result.putLong("cached_episode_identities_reused",reused);
            require(repo.list(true).size()==70,"Include deleted");
            c.getSharedPreferences("story_source_v1",0).edit().putBoolean("enabled",false).commit();
            require(repo.list(false).size()==39,"Disabled source leaked");
            repo.undo("local-01");require(repo.list(false).size()==40,"Undo not reflected");
            result.putString("library","PASS: isolated70stories; live favourite/deletion/undo/source flags, local-first stable ordering; latency measured; query count separately qualified");
        } finally {
            SQLiteDatabase.deleteDatabase(path);
            c.getSharedPreferences("story_source_v1",0).edit().clear().commit();
        }
    }
}
