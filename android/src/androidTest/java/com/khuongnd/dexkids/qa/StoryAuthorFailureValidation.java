package com.khuongnd.dexkids.qa;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.khuongnd.dexkids.ai.KidsAiRetryAfterException;
import com.khuongnd.dexkids.story.StoryAuthor;
import com.khuongnd.dexkids.story.StoryRepository;
import com.khuongnd.dexkids.story.adventure.*;
import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Injected transport failures only: no real HTTP, child speech or provider credentials. */
final class StoryAuthorFailureValidation {
    static void run(LifecycleValidationRunner runner, Bundle result) throws Exception {
        Context base=runner.getTargetContext();
        Context isolated=new ContextWrapper(base) {
            @Override public Context getApplicationContext(){return this;}
            @Override public File getDatabasePath(String name){return base.getDatabasePath("qa-t054-failures-"+name);}
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory factory){return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name),factory);}
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory factory,android.database.DatabaseErrorHandler handler){return SQLiteDatabase.openDatabase(getDatabasePath(name).getPath(),factory,SQLiteDatabase.CREATE_IF_NECESSARY,handler);}
        };
        File path=isolated.getDatabasePath("story-library-v1.db");SQLiteDatabase.deleteDatabase(path);
        StoryEpisode fixture=StoryFixtures.educational(StoryEpisode.Audience.BOTH);
        String json=StoryEpisodeCodec.toJson(fixture);
        try(StoryRepository store=new StoryRepository(isolated)) {
            var entry=store.save(fixture,true,null);store.approve(fixture.id(),entry.getEpisode().revision());
            // The gateway maps HTTP429 to this exception. Authoring must preserve its delay,
            // stop the candidate immediately, and never start review or silently retry.
            for(boolean failReview:new boolean[]{false,true}) {
                AtomicInteger authors=new AtomicInteger(),reviews=new AtomicInteger();
                KidsAiRetryAfterException limited=new KidsAiRetryAfterException(60000L);
                StoryAuthor author=new StoryAuthor(isolated,(system,input,review)->{
                    if(review)reviews.incrementAndGet();else authors.incrementAndGet();
                    if(review==failReview)return StoryAuthorFailureValidation.<RuntimeException,String>fail(limited);
                    return review?"{\"approved\":true}":json;
                });
                Throwable failure=null;
                try {store.save(author.generate("Giúp bạn","Biết chia sẻ",StoryEpisode.Audience.BOTH),true,null);}
                catch(Throwable expected){failure=expected;}
                LifecycleValidationRunner.require(failure==limited && limited.getRetryAfterMs()==60000L,"Retry-After exception or duration changed");
                LifecycleValidationRunner.require(authors.get()==1 && reviews.get()==(failReview?1:0),"429 caused unexpected retry/dispatch");
                retained(store,fixture.id());
            }
            cancellation(isolated,json,false,false);
            retained(store,fixture.id());
            cancellation(isolated,json,true,false);
            retained(store,fixture.id());
            cancellation(isolated,json,false,true);
            retained(store,fixture.id());
            result.putString("t054_author_failures","PASS: injected gateway-mapped429 at author/review preserves60000ms Retry-After with zero retries; pre-dispatch cancellation makes zero calls; Future.cancel(true) during late-returning author/review rejects output and persists no draft; existing approved offline story retained. HTTP429 response parsing and real ASR success are not asserted.");
        } finally {SQLiteDatabase.deleteDatabase(path);}
    }
    private static void retained(StoryRepository store,String id) {
        LifecycleValidationRunner.require(store.list(false).size()==1 && store.get(id).getApproved(),"Failed/cancelled authoring changed offline library");
    }
    private static void cancellation(Context context,String json,boolean cancelReview,boolean beforeDispatch) throws Exception {
        CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1),exited=new CountDownLatch(1);
        AtomicInteger authors=new AtomicInteger(),reviews=new AtomicInteger();AtomicReference<Throwable> outcome=new AtomicReference<>();
        AtomicBoolean saved=new AtomicBoolean(false);ExecutorService worker=Executors.newSingleThreadExecutor();
        StoryAuthor author=new StoryAuthor(context,(system,input,review)->{
            if(review)reviews.incrementAndGet();else authors.incrementAndGet();
            if(review==cancelReview) {
                entered.countDown();boolean interrupted=false;
                // Simulate an HTTP read that returns a response after cancellation.
                while(true){try{release.await();break;}catch(InterruptedException expected){interrupted=true;}}
                if(interrupted)Thread.currentThread().interrupt();
            }
            return review?"{\"approved\":true}":json;
        });
        Future<?> future=worker.submit(()->{
            try(StoryRepository store=new StoryRepository(context)) {
                if(beforeDispatch)Thread.currentThread().interrupt();
                StoryEpisode episode=author.generate("Giúp bạn","Biết chia sẻ",StoryEpisode.Audience.BOTH);
                store.save(episode,true,null);saved.set(true);
            } catch(Throwable failure){outcome.set(failure);}finally{exited.countDown();}
        });
        try {
            if(beforeDispatch) {
                LifecycleValidationRunner.require(exited.await(5,TimeUnit.SECONDS),"Pre-dispatch cancellation stalled");
                LifecycleValidationRunner.require(authors.get()==0 && reviews.get()==0,"Cancelled task dispatched transport");
            } else {
                LifecycleValidationRunner.require(entered.await(5,TimeUnit.SECONDS),"Fake transport was never reached");
                LifecycleValidationRunner.require(future.cancel(true),"Task could not be cancelled");
                release.countDown();
                LifecycleValidationRunner.require(exited.await(5,TimeUnit.SECONDS),"Cancelled transport failed to settle");
                LifecycleValidationRunner.require(authors.get()==1 && reviews.get()==(cancelReview?1:0),"Cancellation dispatched extra request");
            }
            Throwable failure=outcome.get();
            LifecycleValidationRunner.require(!saved.get() && failure instanceof IllegalStateException && "Story authoring cancelled".equals(failure.getMessage()),"Cancelled author/review output was accepted");
        } finally {release.countDown();worker.shutdownNow();LifecycleValidationRunner.require(worker.awaitTermination(5,TimeUnit.SECONDS),"Cancellation test worker leaked");}
    }
    @SuppressWarnings("unchecked")
    private static <E extends Throwable,T> T fail(Throwable failure) throws E {throw (E)failure;}
}
