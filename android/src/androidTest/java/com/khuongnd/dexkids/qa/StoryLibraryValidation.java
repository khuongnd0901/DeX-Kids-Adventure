package com.khuongnd.dexkids.qa;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Bundle;
import com.khuongnd.dexkids.story.StoryRepository;
import com.khuongnd.dexkids.story.adventure.*;
import java.io.File;

/** Actual SQLite lifecycle/revision gates in an isolated private database. No gateway exists here. */
final class StoryLibraryValidation {
    static void processRestart(LifecycleValidationRunner runner,Bundle result,boolean prepare) throws Exception {
        StoryRepository store=new StoryRepository(runner.getTargetContext());
        try {
            if(prepare) {
                StoryEpisode sample=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
                StoryEpisode episode=new StoryEpisode("qa-t059-restart",1,1,sample.title(),sample.audience(),5,4,sample.educationalGoal(),sample.synopsis(),sample.scenes());
                var saved=store.save(episode,true,null);store.approve(episode.id(),saved.getEpisode().revision());store.cursor(episode.id(),saved.getEpisode().revision(),1002);
                result.putString("restart_prepare","PASS: approved revision and cursor1002 committed; external force-stop required");
            } else {
                var restored=store.get("qa-t059-restart");LifecycleValidationRunner.require(restored!=null && restored.getApproved(),"Story lost after process restart");
                LifecycleValidationRunner.require(store.cursor("qa-t059-restart",restored.getEpisode().revision())==1002,"Cursor lost after force-stop");
                result.putString("restart_verify","PASS: approved revision and cursor1002 survived external process restart");
                store.delete("qa-t059-restart",true);
            }
        } finally {store.close();}
    }
    static void run(LifecycleValidationRunner runner,Bundle result) throws Exception {
        Context base=runner.getTargetContext();
        Context isolated=new ContextWrapper(base) {
            @Override public Context getApplicationContext(){return this;}
            @Override public File getDatabasePath(String name){return base.getDatabasePath("qa-t056-"+name);}
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,CursorFactory factory){return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name),factory);}
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,CursorFactory factory,android.database.DatabaseErrorHandler handler){return SQLiteDatabase.openDatabase(getDatabasePath(name).getPath(),factory,SQLiteDatabase.CREATE_IF_NECESSARY,handler);}
        };
        File path=isolated.getDatabasePath("story-library-v1.db");SQLiteDatabase.deleteDatabase(path);
        StoryRepository store=new StoryRepository(isolated);
        try {
            final int[] requests={0};
            var rejectedAuthor=new com.khuongnd.dexkids.story.StoryAuthor(isolated,(system,input,review)->{requests[0]++;return "{\"approved\":false}";});
            boolean reviewRejected=false;try{rejectedAuthor.review(StoryFixtures.educational(StoryEpisode.Audience.BOTH));}catch(IllegalArgumentException expected){reviewRejected=true;}
            LifecycleValidationRunner.require(reviewRejected && requests[0]==1,"Independent review rejection failed");
            var timeoutAuthor=new com.khuongnd.dexkids.story.StoryAuthor(isolated,(system,input,review)->{throw new IllegalStateException("simulated timeout");});
            boolean timeout=false;try{timeoutAuthor.review(StoryFixtures.educational(StoryEpisode.Audience.BOTH));}catch(IllegalStateException expected){timeout=true;}
            LifecycleValidationRunner.require(timeout,"Review transport failure not propagated");
            var approvedAuthor=new com.khuongnd.dexkids.story.StoryAuthor(isolated,(system,input,review)->review?"{\"approved\":true}":StoryEpisodeCodec.toJson(StoryFixtures.educational(StoryEpisode.Audience.BOTH)));
            StoryEpisode authored=approvedAuthor.generate("Giúp bạn","Biết chia sẻ",StoryEpisode.Audience.BOTH);
            LifecycleValidationRunner.require(authored.id().startsWith("story-"),"Author did not assign private identity");
            boolean privateRejected=false;try{approvedAuthor.generate("địa chỉ nhà tôi","Biết chia sẻ",StoryEpisode.Audience.BOTH);}catch(IllegalArgumentException expected){privateRejected=true;}
            LifecycleValidationRunner.require(privateRejected,"Private topic accepted");
            var cancelAuthor=new com.khuongnd.dexkids.story.StoryAuthor(isolated,(system,input,review)->{Thread.currentThread().interrupt();return "{\"approved\":true}";});
            boolean cancelled=false;try{cancelAuthor.review(StoryFixtures.educational(StoryEpisode.Audience.BOTH));}catch(IllegalStateException expected){cancelled=true;}finally{Thread.interrupted();}
            LifecycleValidationRunner.require(cancelled,"Late network result ignored cancellation");
            StoryEpisode source=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
            var regenerated=approvedAuthor.regenerateScene(source,1);
            LifecycleValidationRunner.require(regenerated.scenes().get(0).equals(source.scenes().get(0)),"Regeneration changed unrelated scene");
            for(int i=0;i<55;i++) {
                StoryEpisode e=new StoryEpisode("qa-story-"+i,1,1,source.title(),source.audience(),5,4,source.educationalGoal(),source.synopsis(),source.scenes());
                var saved=store.save(e,true,null);store.approve(e.id(),saved.getEpisode().revision());
            }
            LifecycleValidationRunner.require(store.list(false).size()==55,"55 stories not persisted");
            java.util.ArrayDeque<String> recent=new java.util.ArrayDeque<>();
            java.util.HashSet<String> unheard=new java.util.HashSet<>();
            for(int i=0;i<100;i++) {
                var picked=store.next(StoryEpisode.Audience.BOTH,i);
                LifecycleValidationRunner.require(!recent.contains(picked.getEpisode().id()),"Recent story repeated");
                if(i<55)LifecycleValidationRunner.require(unheard.add(picked.getEpisode().id()),"Played episode chosen before unheard episode");
                store.markPlayed(picked.getEpisode().id());recent.addLast(picked.getEpisode().id());if(recent.size()>5)recent.removeFirst();
                Thread.sleep(2); // Distinct ordering timestamps in real SQLite LRU metadata.
            }
            var first=store.get("qa-story-0");int initial=first.getEpisode().revision();
            var second=store.save(first.getEpisode(),false,initial);
            boolean stale=false;try {store.approve(first.getEpisode().id(),initial);}catch(IllegalArgumentException expected){stale=true;}
            LifecycleValidationRunner.require(stale,"Stale revision approval accepted");
            store.undo(first.getEpisode().id());
            var third=store.save(first.getEpisode(),false,initial);
            LifecycleValidationRunner.require(third.getEpisode().revision()==second.getEpisode().revision()+1,"Undo edit collided with old revision");
            boolean unreviewed=false;try{store.approve(first.getEpisode().id(),third.getEpisode().revision());}catch(IllegalArgumentException expected){unreviewed=true;}
            LifecycleValidationRunner.require(unreviewed,"Unreviewed story approved");
            store.setReviewed(first.getEpisode().id(),third.getEpisode().revision());store.approve(first.getEpisode().id(),third.getEpisode().revision());
            store.favourite(first.getEpisode().id(),true);store.cursor(first.getEpisode().id(),third.getEpisode().revision(),2032);
            store.delete(first.getEpisode().id(),true);LifecycleValidationRunner.require(store.list(false).size()==54,"Delete filter failed");store.undo(first.getEpisode().id());
            int revision=third.getEpisode().revision();store.close();store=new StoryRepository(isolated);
            var restored=store.get(first.getEpisode().id());
            LifecycleValidationRunner.require(restored.getApproved() && restored.getFavourite() && !restored.getDeleted(),"Persistent state lost");
            LifecycleValidationRunner.require(store.cursor(first.getEpisode().id(),revision)==2032,"Cursor restart lost");
            final StoryRepository mainStore=store;final boolean[] denied={false};runner.runOnMainSync(()->{try{mainStore.list(false);}catch(IllegalStateException expected){denied[0]=true;}});
            LifecycleValidationRunner.require(denied[0],"Main thread DB access permitted");
            boolean purgeProtected=false;try{store.purgeDeleted("qa-story-54");}catch(IllegalArgumentException expected){purgeProtected=true;}
            LifecycleValidationRunner.require(purgeProtected,"Active story permanently deleted");
            store.delete("qa-story-54",true);store.purgeDeleted("qa-story-54");
            LifecycleValidationRunner.require(store.get("qa-story-54")==null && store.list(true).size()==54,"Permanent delete did not release library slot");
            result.putString("t056_library","PASS: 55 real SQLite stories; unheard-first and 100 recent-avoiding seeded picks; protected permanent deletion; draft/review/approval; stale approval; undo-edit revision; delete/restore; favourites; cursor; close/reopen; main-thread denial; zero gateway paths");
        } finally {store.close();SQLiteDatabase.deleteDatabase(path);}
    }
}
