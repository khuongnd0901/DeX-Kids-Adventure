package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.badlogic.gdx.Gdx;
import com.khuongnd.dexkids.game.*;
import com.khuongnd.dexkids.journey.DemoJourneyFeed;
import com.khuongnd.dexkids.world.Biome;
import com.khuongnd.dexkids.world.WorldWindow;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Android-only offline DEMO fixtures. Screenshots are evidence, not aesthetic assertions. */
final class ArtVisualValidation {
    static Object field(Object target,String name) throws Exception {
        var f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);
    }
    static void set(Object target,String name,Object value) throws Exception {
        var f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);
    }
    interface GlAction {void run() throws Exception;}
    static void gl(GlAction action) throws Exception {
        var done=new CountDownLatch(1);var error=new AtomicReference<Throwable>();
        Gdx.app.postRunnable(()->{try{action.run();}catch(Throwable e){error.set(e);}finally{done.countDown();}});
        require(done.await(10,TimeUnit.SECONDS),"GL fixture stalled");
        if(error.get()!=null)throw new AssertionError("GL fixture failed",error.get());
    }
    static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    static Button preview(View view){
        if(view instanceof Button b && "XEM THỬ · hoạt hình DEMO".contentEquals(b.getText()))return b;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++){
            Button b=preview(group.getChildAt(i));if(b!=null)return b;
        }
        return null;
    }
    static void restore(android.content.SharedPreferences prefs,java.util.Map<String,?> values){
        var edit=prefs.edit().clear();
        for(var e:values.entrySet()){
            Object v=e.getValue();String k=e.getKey();
            if(v instanceof Boolean b)edit.putBoolean(k,b);
            else if(v instanceof String s)edit.putString(k,s);
            else if(v instanceof Integer n)edit.putInt(k,n);
            else if(v instanceof Long n)edit.putLong(k,n);
            else if(v instanceof Float n)edit.putFloat(k,n);
            else if(v instanceof java.util.Set<?> s){
                @SuppressWarnings("unchecked") var strings=(java.util.Set<String>)s;edit.putStringSet(k,strings);
            }else throw new AssertionError("Unsupported preference type");
        }
        require(edit.commit(),"Preference restoration failed");
    }
    static void capture(LifecycleValidationRunner r,File dir,String name)throws Exception{
        Bitmap image=r.captureQaDisplay();
        require(image!=null,"Android screenshot unavailable");
        try{
            require(image.getWidth()==1920 && image.getHeight()==1080,"FullHD required");
            try(var out=new FileOutputStream(new File(dir,name+".png"))){require(image.compress(Bitmap.CompressFormat.PNG,100,out),"PNG encoding failed");}
        }finally{image.recycle();}
    }
    static void run(LifecycleValidationRunner r,Bundle result,boolean soak)throws Exception{
        require(r.fullHdQaDisplay(),"Explicit emulator or external DeX display required");
        var context=r.getTargetContext();
        var prefs=context.getSharedPreferences("parent_settings",0);var old=prefs.getAll();
        var ai=context.getSharedPreferences("kids_ai_settings_v1",0);var oldAi=ai.getAll();
        Activity[] activities=new Activity[2];
        android.app.Instrumentation.ActivityMonitor[] monitorHolder=new android.app.Instrumentation.ActivityMonitor[1];
        File dir=new File(context.getExternalFilesDir(null),soak?"qa-t051-soak":"qa-t051");
        require(dir.isDirectory() || dir.mkdirs(),"Evidence directory missing");
        try{
            require(prefs.edit().putString("audience_mode","BOTH").putBoolean("audio_only",false)
                .putBoolean("offline_tts",false).putBoolean("tts_enabled",false)
                .putBoolean("child_mic_optin",false).commit(),"Offline preferences failed");
            require(ai.edit().putBoolean("ai_quizzes",false).putBoolean("ai_trip_companion_optin",false)
                .putBoolean("creative_trip_packs",false).commit(),"Offline AI preferences failed");
            var monitor=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);monitorHolder[0]=monitor;
            activities[0]=r.startActivitySync(new Intent().setClassName("com.khuongnd.dexkids","com.khuongnd.dexkids.ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            r.runOnMainSync(()->{var button=preview(activities[0].getWindow().getDecorView());
                require(button!=null && button.performClick(),"DEMO launcher absent");});
            activities[1]=r.waitForMonitorWithTimeout(monitor,10000);require(activities[1]!=null,"DEMO child missing");
            SystemClock.sleep(3000);
            var game=(KidsGame)field(activities[1],"runningGame");Object screen=game.getScreen();
            require(field(screen,"journey") instanceof DemoJourneyFeed,"Only DEMO allowed");
            if(soak){
                capture(r,dir,"demo-start");
                long start=SystemClock.elapsedRealtime(),lastFrames=-1;
                var samples=new org.json.JSONArray();
                while(SystemClock.elapsedRealtime()-start<300000){
                    var sample=new org.json.JSONObject();
                    sample.put("elapsed_seconds",(SystemClock.elapsedRealtime()-start)/1000.0);
                    gl(()->{
                        var metric=(RenderRunMetrics)field(screen,"runMetrics");
                        sample.put("frames",metric.frames());sample.put("average_fps",metric.averageFps());
                        sample.put("p95_upper_ms",metric.p95UpperMs());
                        sample.put("previous_draw_calls",field(screen,"previousFrameDrawCalls"));
                        sample.put("managed_textures",com.badlogic.gdx.graphics.Texture.getNumManagedTextures());
                        sample.put("demo_distance_m",((DemoJourneyFeed)field(screen,"journey")).distanceMeters());
                    });
                    require(sample.getLong("frames")>lastFrames,"Render stopped");lastFrames=sample.getLong("frames");
                    var mem=new android.os.Debug.MemoryInfo();android.os.Debug.getMemoryInfo(mem);
                    sample.put("pss_kib",mem.getTotalPss());samples.put(sample);
                    SystemClock.sleep(15000);
                }
                capture(r,dir,"demo-end");
                java.nio.file.Files.write(new File(dir,"samples.json").toPath(),samples.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                result.putDouble("actual_seconds",(SystemClock.elapsedRealtime()-start)/1000.0);
                // Test pause/resume separately from the continuous measured session.
                double[] distance={0};
                gl(()->{set(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->true);
                    distance[0]=((DemoJourneyFeed)field(screen,"journey")).distanceMeters();});
                SystemClock.sleep(1000);
                gl(()->require(((DemoJourneyFeed)field(screen,"journey")).distanceMeters()==distance[0],"Pause drift"));
                gl(()->set(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->false));
                SystemClock.sleep(1000);
                gl(()->require(((DemoJourneyFeed)field(screen,"journey")).distanceMeters()>distance[0],"Resume stopped"));
                result.putString("pause_resume","PASS DEMO feed freeze/resume");
            }else{
                r.runOnMainSync(()->{try{
                    ((android.os.Handler)field(activities[1],"handler")).removeCallbacks((Runnable)field(activities[1],"updatePoiNativeStatus"));
                    var clear=activities[1].getClass().getDeclaredMethod("clearTalkQueue");clear.setAccessible(true);clear.invoke(activities[1]);
                }catch(Exception e){throw new AssertionError(e);}});
                String[] names={"TC01-bus-capybara","TC02-both-passengers","TC03-buffalo","TC04-bird","TC05-duck",
                    "TC06-urban","TC07-village","TC08-forest","TC09-farm","TC10-coast-backdrop","TC11-mountain-backdrop",
                    "TC12-two-traffic-planes","TC13-poi-backdrop","TC14-animals-hud"};
                Biome[] biomes={Biome.GENERAL,Biome.GENERAL,Biome.COUNTRYSIDE,Biome.PARK,Biome.RIVER,
                    Biome.URBAN,Biome.RESIDENTIAL,Biome.PARK,Biome.COUNTRYSIDE,Biome.RIVER,Biome.COUNTRYSIDE,
                    Biome.URBAN,Biome.URBAN,Biome.PARK};
                for(int i=0;i<names.length;i++){
                    final int index=i;gl(()->{
                        set(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->true);
                        var world=(WorldWindow)field(screen,"worldWindow");long selected=1;
                        // Choose a real generated chunk where original animal art is used.
                        if(index==2 || index==3 || index==4 || index==13){
                            for(long c=1;c<2000;c++){world.prepare(c);int seed=world.get(c+2).detailSeed()^(int)((c+2)*0x9E3779B9L);
                                if((seed&3)==0){selected=c;break;}}
                        }
                        set(field(screen,"journey"),"distance",selected*640/7.5);
                        var director=field(screen,"sceneDirector");String poi=switch(index){
                            case 9->"osm:way:234800751";case 10->"osm:node:5537927332";
                            case 12->"osm:way:39598493";default->"qa-fictional-art";};
                        var scene=new PoiSceneDirector.Scene(biomes[index],1f,selected,poi,true);
                        var type=Class.forName("com.khuongnd.dexkids.game.PoiSceneDirector$Request");
                        var ctor=type.getDeclaredConstructor(Biome.class,String.class,boolean.class);ctor.setAccessible(true);
                        set(director,"current",ctor.newInstance(scene.biome(),poi,true));set(director,"startChunk",selected);
                        set(director,"opacity",1f);set(director,"cachedScene",scene);
                        set(director,"lastDistance",((DemoJourneyFeed)field(screen,"journey")).distanceMeters());
                        Object sprites=field(screen,"cartoonSprites");
                        if(index==2 || index==3 || index==4 || index==13){
                            Object variety=field(sprites,"variety");var reset=variety.getClass().getDeclaredMethod("reset");reset.setAccessible(true);reset.invoke(variety);
                            long[] keys=(long[])field(variety,"cachedKey");int[] choices=(int[])field(variety,"cachedChoice");
                            int animal=index==2?16:index==4?13:14;
                            for(int j=0;j<4;j++){keys[j]=((selected+j)<<4)^biomes[index].ordinal();choices[j]=animal;}
                        }
                        // Fixed traffic overlaps use the actual production depth model.
                        var traffic=new TrafficDepthModel(0,true);
                        if(index==11)traffic.advance(5f,Biome.URBAN);
                        else traffic.advance(0f,Biome.PARK);
                        set(sprites,"traffic",traffic);
                    });
                    if(index==13)r.runOnMainSync(()->{try{
                        var focus=com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH;
                        var beat=new com.khuongnd.dexkids.story.EntertainmentDirector().next(focus);
                        Object dashboard=field(activities[1],"adventureDashboard");
                        dashboard.getClass().getMethod("showBeat",beat.getClass(),focus.getClass()).invoke(dashboard,beat,focus);
                        game.showEntertainmentPrompt(beat,focus);
                    }catch(Exception e){throw new AssertionError(e);}});
                    SystemClock.sleep(700);capture(r,dir,names[i]);
                }
                catalog(r,game,dir,"TC10-m10-coast-catalog",new String[]{"sand_dune","foam_wave","shell_white","shell_spiral","starfish_cartoon","crab_cartoon","beach_parasol","fishing_net_rack"});
                catalog(r,game,dir,"TC11-m10-mountain-catalog",new String[]{"mountain_ridge","pine_canopy","cascade_small","hill_cabin","scree_pile","rope_guardrail","highland_flower","viewpoint_platform"});
                result.putInt("captures",names.length+2);
                result.putString("coast_mountain","Existing named POI backdrops; M10 CST/MTN catalog not runtime");
            }
            result.putString("scope","Actual Windows Android API35 FullHD DEMO, offline; frozen synthetic compositions when not soak; visual review separate");
        }finally{
            r.runOnMainSync(()->{for(Activity a:activities)if(a!=null)a.finish();});
            if(monitorHolder[0]!=null)r.removeMonitor(monitorHolder[0]);
            restore(prefs,old);restore(ai,oldAi);
        }
    }
    /** Test-only Android atlas gallery. Never presented as a production journey biome. */
    private static void catalog(LifecycleValidationRunner r,KidsGame game,File dir,String name,String[] regions)throws Exception{
        var previous=game.getScreen();
        com.badlogic.gdx.Screen[] gallery={null};
        gl(()->{
            var atlas=new com.badlogic.gdx.graphics.g2d.TextureAtlas(Gdx.files.absolute(
                new File(dir.getParentFile(),"qa-t051-catalog/m10.atlas").getAbsolutePath()));
            var batch=new com.badlogic.gdx.graphics.g2d.SpriteBatch();
            var font=new com.badlogic.gdx.graphics.g2d.BitmapFont();font.getData().setScale(1.4f);
            for(String key:regions)require(atlas.findRegion(key)!=null,"Missing catalog region "+key);
            gallery[0]=new com.badlogic.gdx.ScreenAdapter(){
                @Override public void render(float delta){
                    Gdx.gl.glClearColor(.94f,.94f,.89f,1f);Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);
                    batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0,0,1920,1080));batch.begin();
                    font.setColor(.15f,.24f,.25f,1);font.draw(batch,"ANDROID ATLAS QA / CATALOG ONLY / "+name,70,890);
                    for(int i=0;i<regions.length;i++){
                        var sprite=atlas.findRegion(regions[i]);float x=90+(i%4)*455,y=150+(1-i/4)*320;
                        float width=ScenePropLayout.fitWidth(280,230,sprite.getRegionWidth(),sprite.getRegionHeight());
                        float height=width*sprite.getRegionHeight()/sprite.getRegionWidth();
                        batch.draw(sprite,x+(280-width)/2,y,width,height);font.draw(batch,regions[i],x,y-24);
                    }
                    batch.end();
                }
                @Override public void dispose(){atlas.dispose();batch.dispose();font.dispose();}
            };
            game.setScreen(gallery[0]);
        });
        try{SystemClock.sleep(800);capture(r,dir,name);}
        finally{gl(()->{game.setScreen(previous);gallery[0].dispose();});}
    }
}
