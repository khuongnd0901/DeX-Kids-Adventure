package com.khuongnd.dexkids.qa;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import com.khuongnd.dexkids.story.StoryIllustrationCache;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;

/** Optional infrastructure only; no actual image provider or replay renderer integration asserted. */
final class StoryIllustrationValidation {
    static void run(LifecycleValidationRunner runner, Bundle result) throws Exception {
        Context base=runner.getTargetContext();
        File isolatedDir=new File(base.getCacheDir(),"qa-t058-illustration");
        delete(isolatedDir);LifecycleValidationRunner.require(isolatedDir.mkdirs(),"Isolated cache unavailable");
        Context isolated=new ContextWrapper(base) {
            @Override public Context getApplicationContext(){return this;}
            @Override public File getFilesDir(){return isolatedDir;}
        };
        Bitmap bitmap=Bitmap.createBitmap(32,24,Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xff117744);ByteArrayOutputStream encoded=new ByteArrayOutputStream();
        LifecycleValidationRunner.require(bitmap.compress(Bitmap.CompressFormat.PNG,100,encoded),"PNG fixture encode failed");bitmap.recycle();
        byte[] png=encoded.toByteArray();final int[] calls={0,0};
        StoryIllustrationCache.Adapter adapter=new StoryIllustrationCache.Adapter() {
            @Override public StoryIllustrationCache.Capability capability(){calls[0]++;return new StoryIllustrationCache.Capability(true,true);}
            @Override public byte[] authorBackground(String generalScene){calls[1]++;return png;}
        };
        StoryIllustrationCache cache=new StoryIllustrationCache(isolated,adapter);
        try {
            LifecycleValidationRunner.require(cache.replayFile("missing-art")==null,"Cache miss must fall back");
            LifecycleValidationRunner.require(!cache.authorAndStage("garden-art","GARDEN",new StoryIllustrationCache.Consent(false,false,true)),"Default OFF bypassed");
            LifecycleValidationRunner.require(!cache.authorAndStage("garden-art","GARDEN",new StoryIllustrationCache.Consent(true,true,false)),"Postpublication image authoring permitted");
            LifecycleValidationRunner.require(calls[0]==0 && calls[1]==0,"Opt-out touched adapter");
            LifecycleValidationRunner.require(cache.authorAndStage("garden-art","GARDEN",new StoryIllustrationCache.Consent(true,true,true)),"Explicit authoring did not stage");
            LifecycleValidationRunner.require(cache.replayFile("garden-art")==null,"Unreviewed image entered replay");
            boolean denied=false;try{cache.approveStaged("garden-art",false);}catch(IllegalArgumentException expected){denied=true;}
            LifecycleValidationRunner.require(denied,"Parent visual review not enforced");
            LifecycleValidationRunner.require(cache.approveStaged("garden-art",true),"Parent-reviewed publish failed");
            File approved=cache.replayFile("garden-art");LifecycleValidationRunner.require(approved!=null,"Approved cache unreadable");
            for(int i=0;i<20;i++)LifecycleValidationRunner.require(cache.replayFile("garden-art")!=null,"Offline cache replay failed");
            LifecycleValidationRunner.require(calls[0]==1 && calls[1]==1,"Replay contacted image adapter");
            Files.write(approved.toPath(),new byte[]{1,2,3});LifecycleValidationRunner.require(cache.replayFile("garden-art")==null,"Corrupt cache did not fall back");
            cache.remove("garden-art");cache.stageForParentReview("hash-art",png);cache.approveStaged("hash-art",true);
            File valid=cache.replayFile("hash-art");byte[] changed=Files.readAllBytes(valid.toPath());changed[changed.length-1]^=1;Files.write(valid.toPath(),changed);
            LifecycleValidationRunner.require(cache.replayFile("hash-art")==null,"Checksum corruption accepted");cache.remove("hash-art");
            boolean oversized=false;try{cache.stageForParentReview("large-art",new byte[StoryIllustrationCache.MAX_IMAGE_BYTES+1]);}catch(IllegalArgumentException expected){oversized=true;}
            LifecycleValidationRunner.require(oversized,"Image byte budget not enforced");
            Bitmap wide=Bitmap.createBitmap(1025,1,Bitmap.Config.ARGB_8888);ByteArrayOutputStream wideEncoded=new ByteArrayOutputStream();wide.compress(Bitmap.CompressFormat.PNG,100,wideEncoded);wide.recycle();
            boolean dimensionDenied=false;try{cache.stageForParentReview("wide-art",wideEncoded.toByteArray());}catch(IllegalArgumentException expected){dimensionDenied=true;}
            LifecycleValidationRunner.require(dimensionDenied,"PNG dimension budget not enforced");
            boolean unsafeKey=false;try{cache.replayFile("../outside");}catch(IllegalArgumentException expected){unsafeKey=true;}
            LifecycleValidationRunner.require(unsafeKey,"Cache path traversal accepted");
            for(int i=0;i<StoryIllustrationCache.MAX_IMAGES;i++)cache.stageForParentReview("budget-"+i,png);
            boolean full=false;try{cache.stageForParentReview("budget-overflow",png);}catch(IllegalArgumentException expected){full=true;}
            LifecycleValidationRunner.require(full,"Image count budget not enforced");
            for(int i=0;i<StoryIllustrationCache.MAX_IMAGES;i++)cache.remove("budget-"+i);
            Bitmap noise=Bitmap.createBitmap(512,512,Bitmap.Config.ARGB_8888);int[] pixels=new int[512*512];java.util.Random random=new java.util.Random(58);
            for(int i=0;i<pixels.length;i++)pixels[i]=0xff000000|random.nextInt(0x1000000);
            noise.setPixels(pixels,0,512,0,0,512,512);ByteArrayOutputStream noisyEncoded=new ByteArrayOutputStream();noise.compress(Bitmap.CompressFormat.PNG,100,noisyEncoded);noise.recycle();
            boolean diskFull=false;int diskItems=0;
            for(;diskItems<StoryIllustrationCache.MAX_IMAGES;diskItems++){try{cache.stageForParentReview("disk-"+diskItems,noisyEncoded.toByteArray());}catch(IllegalArgumentException expected){diskFull=true;break;}}
            LifecycleValidationRunner.require(diskFull && diskItems<StoryIllustrationCache.MAX_IMAGES,"Total disk budget did not precede count limit");
            long stored=0;for(File item:new File(isolatedDir,"story-illustrations-v1").listFiles())stored+=item.length();
            LifecycleValidationRunner.require(stored<=StoryIllustrationCache.MAX_CACHE_BYTES,"Cache exceeded total disk budget");
            final boolean[] mainDenied={false};runner.runOnMainSync(()->{try{cache.replayFile("missing-art");}catch(IllegalStateException expected){mainDenied[0]=true;}});
            LifecycleValidationRunner.require(mainDenied[0],"Main-thread cache I/O permitted");
            StoryIllustrationCache unsupported=new StoryIllustrationCache(isolated);
            LifecycleValidationRunner.require(!unsupported.authorAndStage("unsupported-art","GARDEN",new StoryIllustrationCache.Consent(true,true,true)),"Unsupported default provider authored");
            result.putString("t058_illustration_cache","PASS: default OFF; prepublication cost/opt-in gates; parent-review staging; PNG bounds; atomic private files; SHA256 corruption; image size/count/total-disk limits; replay20 with zero adapter calls; main-thread denial; unsupported-provider fallback. Actual image provider and renderer integration NOT VERIFIED.");
        } finally {delete(isolatedDir);}
    }
    private static void delete(File file){if(file.isDirectory())for(File child:file.listFiles())delete(child);file.delete();}
}
