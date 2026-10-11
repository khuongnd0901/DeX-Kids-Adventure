package com.khuongnd.dexkids.qa;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.SystemClock;
import com.khuongnd.dexkids.ai.*;
import com.khuongnd.dexkids.story.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Real worker/cache/main-thread orchestration with deterministic provider faults; zero HTTP. */
final class CreativeCoordinatorValidation {
    static CreativeLessonPack pack(int version) {
        var rows=new ArrayList<CreativeLessonPack.Lesson>();
        for(int i=0;i<16;i++) {
            var genre=CreativeLessonPack.Genre.values()[i%4];
            var focus=EntertainmentDirector.Audience.values()[i%3];
            rows.add(new CreativeLessonPack.Lesson(
                (genre==CreativeLessonPack.Genre.STORY?"Trong chuyện tưởng tượng lần ":"Trong tranh lần ")+version+" số "+i+", hoa có màu gì?",
                "Bông hoa có màu đỏ trong bức tranh.","flower","bông hoa","topic"+(i%4),genre,focus,
                focus==EntertainmentDirector.Audience.SAU?5:4,genre==CreativeLessonPack.Genre.STORY));
        }
        return CreativeLessonPack.validate(rows,EntertainmentDirector.Audience.BOTH,5,4,List.of());
    }
    static final class Fake implements TripPackGateway {
        final AtomicInteger calls=new AtomicInteger(); int mode=0;
        final List<Long> delays=Collections.synchronizedList(new ArrayList<>());
        volatile CountDownLatch entered,release;
        public boolean ready(){return true;}
        public TripQuestionPack generateTripPack(OfflineLearningCatalog c,EntertainmentDirector.Audience a,List<String> r){throw new AssertionError("Unexpected curated call");}
        public CreativeLessonPack generateCreativePack(EntertainmentDirector.Audience a,int s,int o,List<String> r,List<String> q)throws IOException {
            int count=calls.incrementAndGet();
            if(mode==0) {
                switch(count) {
                    case 1: throw new IOException("offline");
                    case 2: throw new KidsAiRetryAfterException(30000);
                    case 3: throw new java.net.SocketTimeoutException("timeout");
                    case 4: throw new IllegalArgumentException("malformed pack");
                    case 5: throw new IOException("AI_CONTENT_REVIEW_REJECTED");
                    default: throw new IOException("offline");
                }
            }
            if(mode==2) {
                entered.countDown();
                try { release.await(); }catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException("cancelled");}
            }
            if(mode==3)throw new IOException("AI_PROVIDER_REFUSED");
            return pack(count);
        }
    }
    static Object field(Object value,String name)throws Exception {
        var f=value.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(value);
    }
    static void idle(LifecycleValidationRunner runner,TripPackCoordinator c)throws Exception {
        long end=SystemClock.elapsedRealtime()+10000;
        while(SystemClock.elapsedRealtime()<end) {
            Object[] thread={null};runner.runOnMainSync(()->{try{thread[0]=field(c,"thread");}catch(Exception e){throw new AssertionError(e);}});
            if(thread[0]==null)return;
            SystemClock.sleep(20);
        }
        throw new AssertionError("Worker never settled");
    }
    static TripPackCoordinator start(LifecycleValidationRunner runner,Context context,Fake fake)throws Exception {
        TripPackCoordinator[] value={null};
        runner.runOnMainSync(()->{
            value[0]=new TripPackCoordinator(context,OfflineLearningCatalog.empty(),EntertainmentDirector.Audience.BOTH,
                5,4,()->true,fake,ms->{fake.delays.add(ms);return kotlin.Unit.INSTANCE;});
            value[0].resume(true);
        });
        return value[0];
    }
    static void run(LifecycleValidationRunner runner,Bundle result)throws Exception {
        Context base=runner.getTargetContext();File directory=new File(base.getCacheDir(),"qa-t031-worker");directory.mkdirs();
        Context context=new ContextWrapper(base) {
            public Context getApplicationContext(){return this;}
            public File getFilesDir(){return directory;}
            public android.content.SharedPreferences getSharedPreferences(String name,int mode){return base.getSharedPreferences("qa-worker-"+name,mode);}
        };
        var settings=new KidsAiSettings(context);settings.setEnabled(true);settings.setAutomaticPacks(true);settings.setCreativePacks(true);
        var store=new CreativePackStore(context,EntertainmentDirector.Audience.BOTH,5,4);
        TripPackCoordinator[] current={null};
        try {
            store.clear();String last=store.swap(pack(0)).getFingerprint();
            var failed=new Fake();current[0]=start(runner,context,failed);idle(runner,current[0]);
            LifecycleValidationRunner.require(failed.calls.get()==6,"Initial + five retries not honored");
            LifecycleValidationRunner.require(failed.delays.equals(List.of(5000L,30000L,35000L,75000L,150000L)),"Backoff/Retry-After changed");
            LifecycleValidationRunner.require(store.load().getFingerprint().equals(last),"Fault replaced last-good");
            runner.runOnMainSync(()->current[0].pause());
            var success=new Fake();success.mode=1;current[0]=start(runner,context,success);idle(runner,current[0]);
            for(int i=0;i<16;i++) {
                int index=i;
                runner.runOnMainSync(()->{
                    var beat=current[0].nextBeat();LifecycleValidationRunner.require(beat!=null,"Missing creative beat");
                    LifecycleValidationRunner.require(beat.focus()==EntertainmentDirector.Audience.values()[index%3],"Focus rotation changed");
                    LifecycleValidationRunner.require(current[0].nextBeat()==null,"Duplicated pending beat");
                    current[0].complete("incorrect-id");LifecycleValidationRunner.require(current[0].nextBeat()==null,"Wrong completion advanced cursor");
                    current[0].complete(beat.id());
                });
            }
            LifecycleValidationRunner.require(store.load().getCursor()==16 && store.recent().size()==16,"Completion not persisted");
            runner.runOnMainSync(()->{LifecycleValidationRunner.require(current[0].nextBeat()!=null,"No last-good during refill");});
            idle(runner,current[0]);
            LifecycleValidationRunner.require(success.calls.get()==2 && store.load().getCursor()==0,"Refill not swapped once");
            runner.runOnMainSync(()->current[0].pause());
            for(boolean revoke:new boolean[]{false,true}) {
                String before=store.load().getFingerprint();var blocked=new Fake();blocked.mode=2;
                blocked.entered=new CountDownLatch(1);blocked.release=new CountDownLatch(1);
                current[0]=start(runner,context,blocked);
                LifecycleValidationRunner.require(blocked.entered.await(5,TimeUnit.SECONDS),"Worker not entered");
                if(revoke)settings.setCreativePacks(false);else runner.runOnMainSync(()->current[0].pause());
                blocked.release.countDown();SystemClock.sleep(300);idle(runner,current[0]);
                LifecycleValidationRunner.require(store.load().getFingerprint().equals(before),"Cancelled/revoked worker committed");
                runner.runOnMainSync(()->current[0].pause());settings.setCreativePacks(true);
            }
            var refused=new Fake();refused.mode=3;current[0]=start(runner,context,refused);idle(runner,current[0]);
            LifecycleValidationRunner.require(refused.calls.get()==1,"Terminal provider refusal retried");
            result.putString("coordinator","PASS: six attempts/backoff/429/timeout/malformed/review-reject/last-good/pending/focus/cursor/refill/pause/revoke/refusal");
            result.putString("scope","Production coordinator/cache; simulated provider outcomes, real network faults and physical power-loss NOT VERIFIED");
        } finally {
            if(current[0]!=null)runner.runOnMainSync(()->current[0].pause());
            store.clear();
            for(String name:new String[]{"creative-pack-state-v1","kids_ai_settings_v1","trip-pack-state-v1"})context.getSharedPreferences(name,0).edit().clear().commit();
            File[] files=directory.listFiles();if(files!=null)for(File file:files)file.delete();directory.delete();
        }
    }
}
