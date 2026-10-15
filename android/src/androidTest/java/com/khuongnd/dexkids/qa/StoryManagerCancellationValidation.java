package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.khuongnd.dexkids.story.StoryRepository;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/** Deterministic worker scheduling only; actual manager cancel Button is clicked on UI. No AI. */
final class StoryManagerCancellationValidation {
    static void run(LifecycleValidationRunner runner,Bundle result)throws Exception {
        Activity activity=runner.startActivitySync(new Intent().setClassName(runner.getTargetContext(),"com.khuongnd.dexkids.StoryManagerActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        CountDownLatch queueEntered=new CountDownLatch(1),queueRelease=new CountDownLatch(1);
        CountDownLatch runningEntered=new CountDownLatch(1),runningInterrupted=new CountDownLatch(1),runningRelease=new CountDownLatch(1);
        try {
            await(()->!((Boolean)field(activity,"busy")),"Initial library read did not complete");
            ExecutorService worker=(ExecutorService)field(activity,"worker");
            worker.execute(()->{queueEntered.countDown();awaitLatch(queueRelease);});
            require(queueEntered.await(5,TimeUnit.SECONDS),"Queue blocker did not start");
            AtomicBoolean cancelledActionRan=new AtomicBoolean(false);
            submit(runner,activity,()->{cancelledActionRan.set(true);return Unit.INSTANCE;});
            require((Boolean)field(activity,"busy"),"Queued manager task did not set busy");
            cancel(runner,activity);
            await(()->!((Boolean)field(activity,"busy")),"Cancelled queued manager task left busy stuck");
            require(!cancelledActionRan.get(),"Cancelled queued action ran");
            AtomicBoolean databaseRead=new AtomicBoolean(false);
            submit(runner,activity,()->{try(var repository=new StoryRepository(runner.getTargetContext())){repository.list(false);databaseRead.set(true);}return Unit.INSTANCE;});
            require((Boolean)field(activity,"busy"),"Follow-up task was rejected after queued cancellation");
            queueRelease.countDown();
            await(()->databaseRead.get()&&!((Boolean)field(activity,"busy")),"Next real DB work could not complete after cancellation");
            submit(runner,activity,()->{
                runningEntered.countDown();
                while(true) {try{runningRelease.await();break;}catch(InterruptedException expected){runningInterrupted.countDown();}}
                return Unit.INSTANCE;
            });
            require(runningEntered.await(5,TimeUnit.SECONDS),"Running manager action did not start");
            cancel(runner,activity);
            require(runningInterrupted.await(5,TimeUnit.SECONDS),"Running cancellation did not interrupt worker");
            require((Boolean)field(activity,"busy"),"Running cancellation cleared busy before worker completion");
            runningRelease.countDown();
            await(()->!((Boolean)field(activity,"busy")),"Running cancelled task did not clear busy on completion");
            AtomicBoolean finalRead=new AtomicBoolean(false);
            submit(runner,activity,()->{try(var repository=new StoryRepository(runner.getTargetContext())){repository.list(false);finalRead.set(true);}return Unit.INSTANCE;});
            await(()->finalRead.get()&&!((Boolean)field(activity,"busy")),"Manager remained stuck after running cancellation");
            result.putString("t057_cancellation","PASS: actual cancel Button; queued task skipped and busy released; next real DB operation works; running task stays busy until worker exits; subsequent DB operation works; no API");
        } finally {queueRelease.countDown();runningRelease.countDown();runner.runOnMainSync(activity::finish);}
    }
    private static void submit(LifecycleValidationRunner runner,Activity activity,Function0<Unit> action){runner.runOnMainSync(()->{try{var method=activity.getClass().getDeclaredMethod("runWork",Function0.class);method.setAccessible(true);method.invoke(activity,action);}catch(Exception e){throw new AssertionError(e);}});}
    private static void cancel(LifecycleValidationRunner runner,Activity activity){runner.runOnMainSync(()->{Button button=find(activity.getWindow().getDecorView());require(button!=null&&button.isShown()&&button.performClick(),"Actual visible cancel Button missing");});}
    private static Button find(View view){if(view instanceof Button&&"Hủy tác vụ đang chạy".equals(((Button)view).getText().toString()))return (Button)view;if(view instanceof ViewGroup){var group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){Button child=find(group.getChildAt(i));if(child!=null)return child;}}return null;}
    private static void awaitLatch(CountDownLatch latch){boolean done=false;while(!done)try{latch.await();done=true;}catch(InterruptedException ignored){}}
    private interface Check{boolean get()throws Exception;}
    private static void await(Check check,String message)throws Exception{long until=SystemClock.elapsedRealtime()+10000;while(SystemClock.elapsedRealtime()<until){if(check.get())return;SystemClock.sleep(25);}throw new AssertionError(message);}
    private static Object field(Object object,String name)throws Exception{var field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
