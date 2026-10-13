package com.khuongnd.dexkids.qa;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.SystemClock;
import com.khuongnd.dexkids.KidSoundscape;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;

/** Existing local synthesis, isolated cache, paused SoundPools; no microphone/network. */
final class SoundCacheValidation {
    private static final String[] TYPES={"chime","bird","cat","rabbit","bus","ambient"};
    private static File sample(File dir,String type){return new File(dir,"dexkids-t024-v2-"+type+".wav");}
    private static void await(File dir) throws Exception {
        long until=SystemClock.elapsedRealtime()+10000;
        while(SystemClock.elapsedRealtime()<until) {
            boolean complete=true;
            for(String type:TYPES)complete&=sample(dir,type).length()==(type.equals("ambient")?128044:13484);
            if(complete){SystemClock.sleep(100);return;}
            SystemClock.sleep(20);
        }
        throw new AssertionError("Synthesized sample cache never completed");
    }
    static void run(LifecycleValidationRunner runner,Bundle result) throws Exception {
        Context base=runner.getTargetContext();
        File dir=new File(base.getCacheDir(),"qa-offline-sound-cache");
        LifecycleValidationRunner.require(dir.isDirectory()||dir.mkdirs(),"Fixture cache unavailable");
        Context isolated=new ContextWrapper(base) {
            @Override public File getCacheDir(){return dir;}
            @Override public Context getApplicationContext(){return this;}
        };
        KidSoundscape[] pools=new KidSoundscape[3];
        try {
            for(File f:dir.listFiles())Files.delete(f.toPath());
            runner.runOnMainSync(()->pools[0]=new KidSoundscape(isolated));await(dir);
            long[] timestamps=Arrays.stream(TYPES).mapToLong(t->sample(dir,t).lastModified()).toArray();
            byte[] original=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(sample(dir,"bird").toPath()));
            runner.runOnMainSync(()->pools[0].shutdown());
            SystemClock.sleep(1100);
            runner.runOnMainSync(()->pools[1]=new KidSoundscape(isolated));await(dir);
            LifecycleValidationRunner.require(Arrays.equals(timestamps,Arrays.stream(TYPES).mapToLong(t->sample(dir,t).lastModified()).toArray()),"Valid samples regenerated");
            runner.runOnMainSync(()->pools[1].shutdown());
            Files.write(sample(dir,"bird").toPath(),new byte[]{1,2,3});
            runner.runOnMainSync(()->pools[2]=new KidSoundscape(isolated));await(dir);
            LifecycleValidationRunner.require(Arrays.equals(original,MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(sample(dir,"bird").toPath()))),"Corrupt sample repair differs");
            result.putInt("bounded_cached_samples",6);
            result.putString("scope","Actual offline WAV reuse/corruption repair across SoundPool recreation; paused, isolated cache, no child audio/network");
        } finally {
            runner.runOnMainSync(()->{for(var pool:pools)if(pool!=null)pool.shutdown();});
            for(File f:dir.listFiles())Files.deleteIfExists(f.toPath());
            Files.deleteIfExists(dir.toPath());
        }
    }
}
