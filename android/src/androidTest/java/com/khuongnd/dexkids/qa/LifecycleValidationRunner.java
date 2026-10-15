package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;

/** Framework-only real Activity recreation check; screenshots require visual review. */
public final class LifecycleValidationRunner extends Instrumentation {
    private static final String PACKAGE = "com.khuongnd.dexkids";
    private Activity parent, child;
    private ActivityMonitor monitor;
    private String mode;
    private String audioAudience="";
    private int targetDisplay = -1;
    private String captureDisplayId = "";
    private int performanceSeconds = 6;
    private int creativeSeconds = 1805;
    private boolean creativeNetwork;
    private boolean creativeMatrix;
    private boolean creativeSingle;
    private int creativeAttempts = 6;
    private int creativeSauAge = 5, creativeOngAge = 4;

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mode = arguments == null ? "recreation" : arguments.getString("mode", "recreation");
        audioAudience=arguments==null?"":arguments.getString("audio_audience","");
        require(audioAudience.isEmpty() || java.util.Set.of("SAU","ONG","BOTH").contains(audioAudience),
            "Invalid QA audio audience");
        if (arguments != null && arguments.containsKey("display_id"))
            targetDisplay = Integer.parseInt(arguments.getString("display_id"));
        captureDisplayId = arguments == null ? "" : arguments.getString("capture_display_id", "");
        require(captureDisplayId.isEmpty() || (targetDisplay > 0 && captureDisplayId.matches("[1-9][0-9]{0,19}")),
                "Physical screenshot requires an explicit positive SurfaceFlinger display ID and external logical display");
        if (arguments != null && arguments.containsKey("perf_seconds"))
            performanceSeconds = Integer.parseInt(arguments.getString("perf_seconds"));
        if (arguments != null && arguments.containsKey("creative_seconds"))
            creativeSeconds = Integer.parseInt(arguments.getString("creative_seconds"));
        creativeNetwork=arguments!=null && "true".equals(arguments.getString("creative_network"));
        creativeMatrix=arguments!=null && "true".equals(arguments.getString("creative_matrix"));
        creativeSingle=arguments!=null && "true".equals(arguments.getString("creative_single"));
        if (arguments != null && arguments.containsKey("creative_attempts"))
            creativeAttempts = Integer.parseInt(arguments.getString("creative_attempts"));
        require(creativeAttempts >= 1 && creativeAttempts <= 6, "Creative attempts must be 1–6");
        if (arguments != null && arguments.containsKey("creative_sau_age"))
            creativeSauAge = Integer.parseInt(arguments.getString("creative_sau_age"));
        if (arguments != null && arguments.containsKey("creative_ong_age"))
            creativeOngAge = Integer.parseInt(arguments.getString("creative_ong_age"));
        require(creativeSauAge>=4 && creativeSauAge<=7 && creativeOngAge>=4 && creativeOngAge<=7,
            "Creative QA ages must be 4–7");
        require(!creativeMatrix || creativeNetwork,"Age matrix requires authorized creative network");
        require(creativeSeconds>=30 && creativeSeconds<=3600,"Creative runtime must be 30–3600 seconds");
        require(performanceSeconds >= 6 && performanceSeconds <= 120,
                "Performance window must be 6–120 seconds");
        start();
    }

    public int performanceSeconds() { return performanceSeconds; }
    public int targetDisplay() { return targetDisplay; }

    /** Host resolves SurfaceFlinger ID from the active external DeX viewport; never use display0 fallback. */
    Bitmap captureQaDisplay() throws Exception {
        if (targetDisplay == 0) {
            require(fullHdQaDisplay(), "Display0 capture requires emulator");
            return getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).takeScreenshot();
        }
        require(targetDisplay > 0 && !captureDisplayId.isEmpty(), "External capture_display_id required");
        try (var descriptor = getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
                .executeShellCommand("screencap -p -d " + captureDisplayId);
             var stream = new android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor)) {
            return android.graphics.BitmapFactory.decodeStream(stream);
        }
    }

    /** Display zero is allowed only on the explicitly selected Android emulator. */
    public boolean fullHdQaDisplay() {
        return targetDisplay > 0 || (targetDisplay == 0 &&
            (android.os.Build.HARDWARE.equals("ranchu") || android.os.Build.HARDWARE.equals("goldfish")));
    }

    @Override public void runOnMainSync(Runnable action) {
        java.util.concurrent.atomic.AtomicReference<Throwable> failure = new java.util.concurrent.atomic.AtomicReference<>();
        super.runOnMainSync(() -> {
            try { action.run(); } catch (Throwable error) { failure.set(error); }
        });
        if (failure.get() != null) throw new AssertionError("Main-thread QA action failed: " + failure.get(), failure.get());
    }

    @Override public Activity startActivitySync(Intent intent) {
        if (targetDisplay < 0) return super.startActivitySync(intent);
        android.app.ActivityOptions options = android.app.ActivityOptions.makeBasic();
        options.setLaunchDisplayId(targetDisplay);
        Activity activity = super.startActivitySync(intent, options.toBundle());
        require(activity.getDisplay().getDisplayId() == targetDisplay,
                "Requested QA display unavailable; refusing phone fallback");
        return activity;
    }

    @Override public void onStart() {
        Bundle result = new Bundle();
        long started = SystemClock.elapsedRealtime();
        int code = 1;
        try {
            getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            if ("t053_age_prepare".equals(mode) || "t053_age_verify".equals(mode)) {
                ChildOutfitValidation.processAgeRestart(this,result,"t053_age_prepare".equals(mode));
                finish(0,result);return;
            }
            if ("t053_outfits".equals(mode) || "t053_fallback".equals(mode)) {
                ChildOutfitValidation.run(this,result,"t053_fallback".equals(mode));
                finish(0,result);return;
            }
            if ("t051_art".equals(mode) || "t051_soak".equals(mode)) {
                ArtVisualValidation.run(this,result,"t051_soak".equals(mode));
            } else if ("t049_traffic".equals(mode)) {
                validateT049Traffic(result);
            } else if ("t048_art".equals(mode)) {
                validateT048Art(result);
            } else if ("sound_cache".equals(mode)) {
                SoundCacheValidation.run(this,result);
            } else if ("scenery_residency".equals(mode)) {
                validateSceneryResidency(result);
            } else if ("creative_review".equals(mode)) {
                CreativeReviewValidation.run(result);
            } else if ("m10_atlas".equals(mode)) {
                validateM10Atlas(result);
            } else if ("ai_limiter".equals(mode)) {
                validateAiLimiter(result);
            } else if ("creative_cache".equals(mode)) {
                CreativeCacheValidation.run(this, result);
            } else if ("creative_coordinator".equals(mode)) {
                CreativeCoordinatorValidation.run(this,result);
            } else if ("creative_endurance".equals(mode)) {
                validateCreativeEndurance(result);
            } else if ("visual_hud".equals(mode)) {
                validateVisualHud(result);
            } else if ("route_30min".equals(mode) || "route_30_smoke".equals(mode)) {
                RouteThirtyValidation.run(this, result, "route_30_smoke".equals(mode));
            } else if ("opening_variety".equals(mode)) {
                validateOpeningVariety(result);
            } else if ("audience_picker".equals(mode)) {
                validateAudiencePicker(result);
            } else if ("audience_audio".equals(mode)) {
                java.nio.file.Files.deleteIfExists(new File(getTargetContext().getCacheDir(),"qa-audience-audio-result.json").toPath());
                validateAudienceAudio(result);
            } else if ("configure_parent_ai".equals(mode)) {
                configureParentAi(result);
            } else if (("gemini_live".equals(mode) || "gemini_context".equals(mode) || "creative_live".equals(mode))) {
                validateGeminiLive(result);
            } else if ("enable_ambient".equals(mode)) {
                require(getTargetContext().getSharedPreferences("parent_settings", 0).edit()
                    .putBoolean("ambient_music", true).commit(), "Cannot persist authorized music preference");
                result.putString("ambient_setting", "ON for DEMO and LIVE by explicit parent request");
            } else if ("music_smoke".equals(mode) || "audio_volume".equals(mode)) {
                var audio=(android.media.AudioManager)getTargetContext().getSystemService("audio");
                int before=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
                result.putInt("original_media_volume",before);
                validateMusicSmoke(result);
                int after=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
                result.putInt("final_media_volume",after);
                require(after==before,"Device volume changed during music smoke");
            } else if ("parent_controls".equals(mode)) {
                validateParentControls(result);
            } else if ("audio_playback".equals(mode)) {
                validateAudioPlayback(result);
            } else if ("physical_soak".equals(mode)) {
                validatePhysicalSoak(result);
            } else if ("gpx_parser".equals(mode)) {
                try (java.io.InputStream stream = getTargetContext().getAssets().open("gps/hcm-preview.gpx")) {
                    com.khuongnd.dexkids.journey.GpxReplayFeed feed =
                            com.khuongnd.dexkids.journey.GpxReplayFeed.fromGpx(stream);
                    require(feed.totalTimeSeconds() > 0, "Sample GPX duration missing");
                    result.putDouble("duration_seconds", feed.totalTimeSeconds());
                    String unsafe = "<?xml version='1.0'?><!DOCTYPE gpx [<!ENTITY x SYSTEM 'file:///not-readable'>]><gpx>&x;</gpx>";
                    for (java.nio.charset.Charset encoding : new java.nio.charset.Charset[] {
                            java.nio.charset.StandardCharsets.UTF_8, java.nio.charset.StandardCharsets.UTF_16,
                            java.nio.charset.StandardCharsets.UTF_16LE, java.nio.charset.StandardCharsets.UTF_16BE}) {
                        boolean rejected = false;
                        try {
                            com.khuongnd.dexkids.journey.GpxReplayFeed.fromGpx(new java.io.ByteArrayInputStream(unsafe.getBytes(encoding)));
                        } catch (IllegalArgumentException expected) {
                            rejected = expected.getCause().getMessage().contains("declarations are forbidden");
                        }
                        require(rejected, "Unsafe declaration accepted: " + encoding);
                    }
                    result.putString("security", "DTD/entity rejected in UTF8/UTF16/UTF16LE/UTF16BE");
                } catch (Throwable failure) {
                    throw new AssertionError(android.util.Log.getStackTraceString(failure));
                }
            } else if ("gpx_sample".equals(mode) || "gpx_picker".equals(mode)) {
                validateGpxJourney(result);
            } else if ("permission".equals(mode)) {
                validatePermissionRefusal(result);
            } else if ("p0_hcm".equals(mode) || "p0_child_mic_privacy".equals(mode) || "p0_ai_cache".equals(mode) || "p0_perf_ab".equals(mode) || "p0_live".equals(mode) || "p0_live_nearby".equals(mode) || "p0_route".equals(mode)) {
                P0Validation.run(this, mode, result);
            } else if ("single_display".equals(mode) || "single_display_mouse".equals(mode)) {
                validateSingleDisplay(result, "single_display_mouse".equals(mode));
            } else {
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            Intent intent = new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            parent = startActivitySync(intent);
            boolean[] click = new boolean[2];
            runOnMainSync(() -> {
                Button preview = findPreview(parent.getWindow().getDecorView());
                click[0] = preview != null;
                if (preview != null) click[1] = preview.performClick();
            });
            require(click[0], "Explicit parent preview button absent");
            require(click[1], "Parent preview click failed");
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null, "Child Activity did not start");
            SystemClock.sleep(3000);
            checkDemo(child);
            File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-recreation");
            require(directory.isDirectory() || directory.mkdirs(), "Screenshot directory unavailable");
            result.putString("before_png", screenshot(directory, "before.png"));
            Bundle before = savedState(child);
            require(!before.containsKey("kids.session.deadline.elapsed"),
                    "Legacy session timeout still saved");
            Object originalFeed = journey(child);
            double beforeDistance = distance(originalFeed);
            Activity old = child;
            removeMonitor(monitor);
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            runOnMainSync(old::recreate);
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null && child != old, "Recreation did not create a distinct instance");
            SystemClock.sleep(3000);
            checkDemo(child);
            result.putString("after_png", screenshot(directory, "after.png"));
            require(!savedState(child).containsKey("kids.session.deadline.elapsed"),
                    "Session timeout restored after recreation");
            Object afterFeed = journey(child);
            result.putDouble("distance_before", beforeDistance);
            result.putDouble("distance_after", distance(afterFeed));
            result.putBoolean("same_feed", afterFeed == originalFeed);
            require(afterFeed == originalFeed, "Journey feed reset on recreation");
            require(distance(afterFeed) >= beforeDistance, "Journey distance decreased on recreation");
            result.putString("assertions", "distinct_instance,no_session_deadline,journey_feed_and_distance_preserved,demo_gps_inactive");
            }
            result.putString("qa_status", "PASS");
            result.putString("visual_status", "NOT_VERIFIED: review screenshot artifacts");
            code = 0;
        } catch (Throwable failure) {
            result.putString("qa_status", "FAIL");
            result.putString("qa_error", android.util.Log.getStackTraceString(failure));
        } finally {
            try {
                runOnMainSync(() -> {
                    if (child != null && !child.isFinishing()) child.finish();
                    if (parent != null && !parent.isFinishing()) parent.finish();
                });
                if (monitor != null) removeMonitor(monitor);
            } catch (Throwable cleanup) {
                code = 1;
                result.putString("qa_status", "FAIL");
                result.putString("cleanup_error", cleanup.toString());
            }
            result.putLong("elapsed_ms", SystemClock.elapsedRealtime() - started);
            if ("audience_audio".equals(mode)) {
                try {
                    // Test-only numeric/status evidence survives a host ADB disconnect.
                    // This mode never records child audio, transcripts, keys or GPS.
                    var saved=new org.json.JSONObject();
                    for(String key:result.keySet()) saved.put(key,result.get(key));
                    java.nio.file.Files.write(new File(getTargetContext().getCacheDir(),
                        "qa-audience-audio-result.json").toPath(),
                        saved.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } catch(Exception failure) {
                    code=1;result.putString("qa_status","FAIL");
                    result.putString("qa_error","Cannot persist audio QA result: "+failure.getClass().getSimpleName());
                }
            }
            finish(code, result);
        }
    }

    private Object field(Object object, String name) throws Exception {
        Field f = object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(object);
    }

    private void validateSceneryResidency(Bundle result) throws Exception {
        require(fullHdQaDisplay(), "Explicit DeX/emulator display required");
        var prefs=getTargetContext().getSharedPreferences("parent_settings",0);
        var ai=getTargetContext().getSharedPreferences("kids_ai_settings_v1",0);
        var old=prefs.getAll();var oldAi=ai.getAll();
        try {
            require(prefs.edit().putBoolean("audio_only",true).putBoolean("offline_tts",false).commit(),"Fixture settings failed");
            require(ai.edit().putBoolean("ai_quizzes",false).commit(),"Disable QA provider failed");
            monitor=addMonitor(PACKAGE+".KidsActivity",null,false);
            parent=startActivitySync(new Intent().setClassName(PACKAGE,PACKAGE+".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runOnMainSync(()->require(findPreview(parent.getWindow().getDecorView()).performClick(),"Preview absent"));
            child=waitForMonitorWithTimeout(monitor,10000);require(child!=null,"Child absent");
            SystemClock.sleep(1500);
            int baseline=-1;
            long firstFullNative=0,lastFullNative=0;
            for(int i=0;i<=200;i++) {
                boolean audio=i%2==0;
                runOnMainSync(()->{
                    try { ((com.khuongnd.dexkids.game.KidsGame)field(child,"runningGame")).setAudioOnly(audio); }
                    catch(Exception e){throw new AssertionError(e);}
                });
                SystemClock.sleep(120);
                var done=new java.util.concurrent.CountDownLatch(1);
                var error=new java.util.concurrent.atomic.AtomicReference<Throwable>();
                int[] count={-1};
                com.badlogic.gdx.Gdx.app.postRunnable(()->{
                    try {
                        var game=(com.khuongnd.dexkids.game.KidsGame)field(child,"runningGame");
                        require((field(game.getScreen(),"cartoonSprites")==null)==audio,"Scene residency disagrees with mode");
                        count[0]=com.badlogic.gdx.graphics.Texture.getNumManagedTextures();
                    }catch(Throwable e){error.set(e);}finally{done.countDown();}
                });
                require(done.await(10,java.util.concurrent.TimeUnit.SECONDS),"GL transition stalled");
                if(error.get()!=null)throw new AssertionError("Residency transition failed",error.get());
                if(i==0)baseline=count[0];
                if(!audio) {
                    lastFullNative=android.os.Debug.getNativeHeapAllocatedSize();
                    if(i==1)firstFullNative=lastFullNative;
                }
                if(audio)require(count[0]==baseline,"Managed texture count grew after disposal");
                else require(count[0]>baseline,"Visual textures absent");
                require(child.getDisplay().getDisplayId()==targetDisplay,"Unexpected display fallback");
            }
            result.putLong("native_allocated_first_full",firstFullNative);
            result.putLong("native_allocated_last_full",lastFullNative);
            result.putLong("native_allocated_final_audio",android.os.Debug.getNativeHeapAllocatedSize());
            result.putInt("full_audio_cycles",100);result.putInt("audio_managed_textures",baseline);
            result.putString("scope","Actual GL creation/disposal across 100 Full/Audio-only cycles; managed count, not physical VRAM leak proof");
        } finally { restorePreferences(prefs,old);restorePreferences(ai,oldAi); }
    }

    /** Synthetic frozen DEMO composition; same production SpriteBatch and atlas path. */
    private void validateT049Traffic(Bundle result) throws Exception {
        require(fullHdQaDisplay(), "Explicit emulator display required");
        var prefs=getTargetContext().getSharedPreferences("parent_settings",0);
        var old=prefs.getAll();
        var ai=getTargetContext().getSharedPreferences("kids_ai_settings_v1",0);
        var oldAi=ai.getAll();
        File directory=new File(getTargetContext().getExternalFilesDir(null),"qa-t049");
        require(directory.isDirectory() || directory.mkdirs(),"Evidence directory unavailable");
        try {
            require(prefs.edit().putString("audience_mode","BOTH").putBoolean("audio_only",false)
                .putBoolean("offline_tts",false).putBoolean("child_mic_optin",false).commit(),"Offline preferences failed");
            require(ai.edit().putBoolean("ai_quizzes",false).putBoolean("ai_trip_companion_optin",false)
                .putBoolean("creative_trip_packs",false).commit(),"Disable fixture network failed");
            monitor=addMonitor(PACKAGE+".KidsActivity",null,false);
            parent=startActivitySync(new Intent().setClassName(PACKAGE,PACKAGE+".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runOnMainSync(()->require(findPreview(parent.getWindow().getDecorView()).performClick(),"Preview absent"));
            child=waitForMonitorWithTimeout(monitor,10000);require(child!=null,"Child absent");
            SystemClock.sleep(2500);checkDemo(child);
            var game=(com.khuongnd.dexkids.game.KidsGame)field(child,"runningGame");
            runOnMainSync(()->{
                try {
                    ((android.os.Handler)field(child,"handler")).removeCallbacks((Runnable)field(child,"updatePoiNativeStatus"));
                    var clear=child.getClass().getDeclaredMethod("clearTalkQueue");clear.setAccessible(true);clear.invoke(child);
                }catch(Exception e){throw new AssertionError(e);}
            });
            String[] names={"sedan-far","sedan-near","truck-far","truck-near","scooter-near","bicycle-near","two-lanes","poi-backdrop"};
            int[] kinds={0,0,2,2,4,5,0,0};
            boolean[] near={false,true,false,true,true,true,true,false};
            for(int i=0;i<names.length;i++) {
                final int index=i;
                var done=new java.util.concurrent.CountDownLatch(1);
                var error=new java.util.concurrent.atomic.AtomicReference<Throwable>();
                com.badlogic.gdx.Gdx.app.postRunnable(()->{
                    try {
                        Object screen=game.getScreen(),sprites=field(screen,"cartoonSprites");
                        setQaField(screen,"parentMenuOpen",(java.util.function.BooleanSupplier)()->true);
                        var world=(com.khuongnd.dexkids.world.WorldWindow)field(screen,"worldWindow");
                        // Locate an actual deterministic original traffic chunk for the baseline.
                        long selected=1;
                        for(long c=1;c<2000;c++) {
                            world.prepare(c);var chunk=world.get(c);
                            if(com.khuongnd.dexkids.game.SceneryCast.showTraffic(c,chunk.biome()) &&
                                com.khuongnd.dexkids.game.SceneryCast.trafficIndex(c,chunk.detailSeed())==kinds[index]) {selected=c;break;}
                        }
                        Object journey=field(screen,"journey");
                        require(journey instanceof com.khuongnd.dexkids.journey.DemoJourneyFeed,"DEMO feed required");
                        setQaField(journey,"distance",(selected*640-235)/7.5);
                        var director=field(screen,"sceneDirector");
                        var scene=new com.khuongnd.dexkids.game.PoiSceneDirector.Scene(
                            com.khuongnd.dexkids.world.Biome.URBAN,1f,selected-1,
                            index==7?"osm:way:39598493":"qa-fictional-traffic",true);
                        var requestClass=Class.forName("com.khuongnd.dexkids.game.PoiSceneDirector$Request");
                        var constructor=requestClass.getDeclaredConstructor(com.khuongnd.dexkids.world.Biome.class,String.class,boolean.class);
                        constructor.setAccessible(true);
                        setQaField(director,"current",constructor.newInstance(scene.biome(),scene.poiId(),true));
                        setQaField(director,"startChunk",selected-1);
                        setQaField(director,"opacity",1f);
                        setQaField(director,"lastDistance",((com.khuongnd.dexkids.journey.JourneyFeed)journey).distanceMeters());
                        setQaField(director,"cachedScene",scene);
                        try {
                            Object traffic=field(sprites,"traffic");
                            // Production model places the selected type at x500 using its normal trajectory.
                            var model=new com.khuongnd.dexkids.game.TrafficDepthModel(
                                kinds[index]-(near[index]?0:3),true);
                            var lane=near[index]?com.khuongnd.dexkids.game.TrafficDepthModel.Lane.NEAR:
                                com.khuongnd.dexkids.game.TrafficDepthModel.Lane.FAR;
                            double delay=near[index]?2:0;
                            double width=com.khuongnd.dexkids.game.TrafficLayout.height(kinds[index],lane)*360/185;
                            model.advance((float)(delay+((near[index]?500:800)+width+8)/com.khuongnd.dexkids.game.TrafficDepthModel.speed(kinds[index],lane)),
                                com.khuongnd.dexkids.world.Biome.URBAN);
                            setQaField(sprites,"traffic",model);
                            // Keep the clock frozen; next production advance(0) preserves model depth.
                            if(index!=6) {
                                setQaField(model,near[index]?"farEnabled":"nearEnabled",false);
                            }
                        }catch(NoSuchFieldException baseline){ result.putString("baseline","Original environment traffic; no near lane exists"); }
                    }catch(Throwable e){error.set(e);}finally{done.countDown();}
                });
                require(done.await(8,java.util.concurrent.TimeUnit.SECONDS),"Traffic GL fixture stalled");
                if(error.get()!=null)throw new AssertionError("Traffic fixture failed",error.get());
                SystemClock.sleep(500);
                result.putString(names[i],screenshot(directory,names[i]+".png"));
            }
            result.putString("scope","Actual API35 screenshots, synthetic frozen DEMO both-child fixtures; review images separately; no physical DeX claim");
        }finally{restorePreferences(prefs,old);restorePreferences(ai,oldAi);}
    }
    private static void setQaField(Object target,String name,Object value) throws Exception {
        var f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);
    }

    /** Fixed visual fixtures use production prompt/phase mapping; no network. */
    private void validateT048Art(Bundle result) throws Exception {
        require(fullHdQaDisplay(), "Select emulator display 0 or authorized external display");
        var prefs=getTargetContext().getSharedPreferences("parent_settings",0);
        var before=prefs.getAll();
        var ai=getTargetContext().getSharedPreferences("kids_ai_settings_v1",0);
        var aiBefore=ai.getAll();
        File directory=new File(getTargetContext().getExternalFilesDir(null),"qa-t048");
        require(directory.isDirectory() || directory.mkdirs(),"Evidence directory unavailable");
        try {
            require(prefs.edit().putString("audience_mode","BOTH").putBoolean("audio_only",false)
                .putBoolean("tts_enabled",false).putBoolean("child_mic_optin",false).commit(),"Offline fixture preferences failed");
            require(ai.edit().putBoolean("enabled",false).putBoolean("trip_companion",false)
                .putBoolean("creative_lessons",false).commit(),"Offline AI fixture failed");
            monitor=addMonitor(PACKAGE+".KidsActivity",null,false);
            parent=startActivitySync(new Intent().setClassName(PACKAGE,PACKAGE+".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runOnMainSync(()->require(findPreview(parent.getWindow().getDecorView()).performClick(),"Preview unavailable"));
            child=waitForMonitorWithTimeout(monitor,10000);require(child!=null,"Child missing");
            SystemClock.sleep(3000);checkDemo(child);
            var game=(com.khuongnd.dexkids.game.KidsGame)field(child,"runningGame");
            runOnMainSync(()->{
                try {
                    ((android.os.Handler)field(child,"handler")).removeCallbacks((Runnable)field(child,"updatePoiNativeStatus"));
                    var clear=child.getClass().getDeclaredMethod("clearTalkQueue");clear.setAccessible(true);clear.invoke(child);
                } catch(Exception failure){throw new AssertionError(failure);}
            });
            var textureBefore=visualTextures(game);
            require(textureBefore.containsKey("pngAtlas-0"),"New PNG atlas not resident");
            require(textureBefore.get("pngAtlas-0").contains("1024x1024"),"Unexpected T-048 page size");
            String[] names={"IDLE","ASK","LAUGH","WAVE"};
            for(String name:names) {
                String id=name.equals("WAVE")?"welcome":"qa-t048-"+name;
                game.showEntertainmentPrompt(id,"Chiếc xe buýt hoạt hình cùng khám phá khu vườn.");
                game.showEntertainmentPhase(id,name.equals("IDLE")?"INTERRUPTED":name.equals("LAUGH")?"ANSWER":"QUESTION");
                // Suppress automatic wave timing only in this controlled QA fixture.
                var done=new java.util.concurrent.CountDownLatch(1);
                var failure=new java.util.concurrent.atomic.AtomicReference<Throwable>();
                com.badlogic.gdx.Gdx.app.postRunnable(()->{
                    try {
                        Object sprites=field(game.getScreen(),"cartoonSprites");
                        require(field(sprites,"compositeBus")!=null,"Composite bus missing");
                        var waveCycle=sprites.getClass().getDeclaredField("lastWaveCycle");waveCycle.setAccessible(true);waveCycle.setLong(sprites,Long.MAX_VALUE);
                        Object actor=field(sprites,"actor");
                        var wave=actor.getClass().getDeclaredField("waveRemainingSeconds");wave.setAccessible(true);wave.setDouble(actor,0);
                        game.setNarrationActive(false);
                    }catch(Throwable error){failure.set(error);}finally{done.countDown();}
                });
                require(done.await(5,java.util.concurrent.TimeUnit.SECONDS),"GL fixture stalled");
                if(failure.get()!=null)throw new AssertionError("Fixture failed",failure.get());
                SystemClock.sleep(700);
                var poseDone=new java.util.concurrent.CountDownLatch(1);
                var actual=new java.util.concurrent.atomic.AtomicReference<String>();
                com.badlogic.gdx.Gdx.app.postRunnable(()->{
                    try {
                        Object transition=field(field(game.getScreen(),"cartoonSprites"),"pngTransition");
                        actual.set(transition.getClass().getMethod("current").invoke(transition).toString());
                    }catch(Exception error){actual.set("ERROR: "+error);}finally{poseDone.countDown();}
                });
                require(poseDone.await(5,java.util.concurrent.TimeUnit.SECONDS),"Pose sampling stalled");
                require(name.equals(actual.get()),"Pose mismatch "+name+" -> "+actual.get());
                result.putString(name,screenshot(directory,name+".png"));
            }
            require(textureBefore.equals(visualTextures(game)),"Textures changed during pose transitions");
            result.putString("texture_snapshot",textureBefore.toString());
            result.putString("scope","Emulator production PNG rendering and four controlled visual states; physical DeX/FPS/leak NOT VERIFIED");
        } finally {restorePreferences(prefs,before);restorePreferences(ai,aiBefore);}
    }

    private void validateM10Atlas(Bundle result) throws Exception {
        monitor=addMonitor(PACKAGE+".KidsActivity",null,false);
        parent=startActivitySync(new Intent().setClassName(PACKAGE,PACKAGE+".ParentActivity")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        runOnMainSync(()->require(findPreview(parent.getWindow().getDecorView()).performClick(),"Preview absent"));
        child=waitForMonitorWithTimeout(monitor,10000);require(child!=null,"Child absent");
        SystemClock.sleep(3000);checkDemo(child);
        var done=new java.util.concurrent.CountDownLatch(1);
        var error=new java.util.concurrent.atomic.AtomicReference<Throwable>();
        com.badlogic.gdx.Gdx.app.postRunnable(()->{
            try {
                var props=new com.badlogic.gdx.graphics.g2d.TextureAtlas(
                    com.badlogic.gdx.Gdx.files.internal("generated/m10/props.atlas"));
                try {
                    require(props.getRegions().size==32,"M10 prop count differs");
                    result.putInt("loaded_prop_regions",props.getRegions().size);
                    result.putInt("loaded_prop_pages",props.getTextures().size);
                } finally {props.dispose();}
            } catch(Throwable failure){error.set(failure);}finally{done.countDown();}
        });
        require(done.await(15,java.util.concurrent.TimeUnit.SECONDS),"Atlas GL load stalled");
        if(error.get()!=null)throw new AssertionError("Android LibGDX atlas load failed",error.get());
        result.putString("scope","Actual Android GL TextureAtlas load/dispose; loaded count is not gameplay visibility or human art approval");
    }

    /** Production persisted dispatch algorithm with a fake clock; zero HTTP or real sleeps. */
    private void validateAiLimiter(Bundle result) throws Exception {
        var base=getTargetContext();
        var context=new android.content.ContextWrapper(base) {
            @Override public android.content.Context getApplicationContext() { return this; }
            @Override public android.content.SharedPreferences getSharedPreferences(String name,int mode) {
                return base.getSharedPreferences("qa-limiter-"+name,mode);
            }
        };
        var prefs=context.getSharedPreferences("kids_ai_rate_v1",0);
        prefs.edit().clear().commit();
        var now=new java.util.concurrent.atomic.AtomicLong(1_000_000);
        var starts=new java.util.ArrayList<Long>();
        try {
            for(int i=0;i<600;i++) {
                com.khuongnd.dexkids.ai.KidsAiDispatchLimiter.INSTANCE.acquire(context,
                    now::get,ms->{now.addAndGet(ms);return kotlin.Unit.INSTANCE;});
                long at=now.get();
                require(starts.isEmpty() || at-starts.get(starts.size()-1)>=6100,"Dispatch spacing violated");
                require(starts.stream().filter(t->at-t<60000).count()<10,"Rolling ten RPM violated");
                starts.add(at);
            }
            require(prefs.getString("starts","").split(",").length<=10,"Rate history unbounded");
            // Rollback must not reset the persisted budget.
            long previous=starts.get(starts.size()-1);now.addAndGet(-30000);
            com.khuongnd.dexkids.ai.KidsAiDispatchLimiter.INSTANCE.acquire(context,
                now::get,ms->{now.addAndGet(ms);return kotlin.Unit.INSTANCE;});
            require(now.get()-previous>=6100,"Clock rollback bypassed budget");
            result.putInt("fake_dispatches",601);
            result.putString("limiter","PASS: production persisted rolling 10 RPM, 6100ms spacing, bounded history, rollback");
            result.putString("scope","Fake clock; zero provider requests");
        } finally {prefs.edit().clear().commit();}
    }

    /** Synthetic lesson only: no provider, child speech, GPS, or persistent pack edits. */
    private void validateVisualHud(Bundle result) throws Exception {
        require(fullHdQaDisplay(), "Explicit external display or emulator display required");
        var prefs = getTargetContext().getSharedPreferences("parent_settings", 0);
        var before = prefs.getAll();
        var ai = getTargetContext().getSharedPreferences("kids_ai_settings_v1", 0);
        var aiBefore = ai.getAll();
        try {
            require(ai.edit().putBoolean("ai_quizzes", false).putBoolean("automatic_trip_packs", false).commit(), "Disable QA network");
            require(prefs.edit().putBoolean("child_mic_optin", false).putBoolean("offline_tts", false).putBoolean("audio_only",false).commit(), "Disable QA speech");
            File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-t032");
            require(directory.isDirectory() || directory.mkdirs(), "Screenshot directory unavailable");
            int[][] ages = {{4,5},{5,4},{6,7},{7,7}};
            for (int[] pair : ages) for (String audience : new String[]{"SAU", "ONG", "BOTH"}) {
                require(prefs.edit().putString("audience_mode", audience).putInt("sau_age",pair[0]).putInt("ong_age",pair[1]).commit(), "Save QA ages");
                monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
                parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                runOnMainSync(() -> require(findPreview(parent.getWindow().getDecorView()).performClick(), "Start preview"));
                child = waitForMonitorWithTimeout(monitor, 10000);
                require(child != null, "Child did not start");
                SystemClock.sleep(2500);
                var dashboard = field(child, "adventureDashboard");
                var prompt = (android.widget.TextView) field(dashboard, "promptLabel");
                var label = (android.widget.TextView) field(dashboard, "modeLabel");
                var banner = (android.widget.TextView) field(child, "entertainmentBanner");
                var focus = com.khuongnd.dexkids.story.EntertainmentDirector.Audience.valueOf(audience);
                String question = "Trong câu chuyện tưởng tượng, bạn thỏ nhìn thấy một con bướm bay quanh bông hoa trong khu vườn xanh. Con hãy quan sát và thử nghĩ xem bạn thỏ có thể chào con bướm bằng cách nào nhé?";
                String answer = "Bạn thỏ có thể vẫy tay và nói lời chào thân thiện với bạn bướm. Hai bạn cùng ngắm bông hoa và giữ khu vườn sạch đẹp. Đây là câu chuyện tưởng tượng; chúng mình có thể nghĩ thêm một lời chào vui vẻ để kể cho Capybara nghe nhé.";
                var beat = new com.khuongnd.dexkids.story.EntertainmentDirector.Beat("creative-qa",focus,question,answer,
                    com.khuongnd.dexkids.story.EntertainmentDirector.Reaction.WAVE,"butterfly");
                var game = (com.khuongnd.dexkids.game.KidsGame) field(child,"runningGame");
                runOnMainSync(() -> {
                    try {
                        // Isolate fixed visual fixtures from natural DEMO opening/resolution timers.
                        ((android.os.Handler)field(child,"handler")).removeCallbacks(
                            (Runnable)field(child,"updatePoiNativeStatus"));
                        var clear=child.getClass().getDeclaredMethod("clearTalkQueue");
                        clear.setAccessible(true);clear.invoke(child);
                        dashboard.getClass().getMethod("showBeat",beat.getClass(),focus.getClass()).invoke(dashboard,beat,focus);
                        game.showEntertainmentPrompt(beat,focus);
                        banner.setText(answer); banner.setVisibility(View.VISIBLE);
                    } catch (Exception e) { throw new AssertionError(e); }
                });
                SystemClock.sleep(400);
                String name = audience + "-" + pair[0] + "-" + pair[1];
                runOnMainSync(() -> {
                    checkVisibleText(prompt,question); checkVisibleText(banner,answer);
                    try {
                        checkDisjoint(banner,(View) field(child,"poiNativeStatus"));
                        checkDisjoint(banner,(View) field(dashboard,"progressLabel"));
                    } catch (Exception e) { throw new AssertionError(e); }
                    String modeText = label.getText().toString();
                    require(modeText.contains(String.valueOf("ONG".equals(audience) ? pair[1] : pair[0])), "Configured age absent");
                    if ("BOTH".equals(audience)) require(modeText.contains("ONG " + pair[1]), "Ong age absent");
                    result.putString(name + "_layout", "prompt=" + prompt.getLineCount() + ",answer=" + banner.getLineCount() + ",label=" + modeText);
                });
                java.nio.file.Files.write(new File(getTargetContext().getCacheDir(),"qa-t032-frame").toPath(),name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                SystemClock.sleep(5000); // Allow Wi-Fi screencap to finish within this marker.
                if(targetDisplay==0) screenshot(directory,name+".png");
                if("BOTH".equals(audience) && pair[0]==7 && pair[1]==7) {
                    var textureBefore=visualTextures(game);
                    int themeTransitions=0;
                    com.khuongnd.dexkids.story.VisualCue.Theme previousTheme=null;
                    String[] subjects={"Con bướm bay trong vườn hoa.","Con thỏ nhảy trên bãi cỏ.","Bông hoa nở trong khu vườn.",
                        "Đám mây trắng trôi trên trời.","Chiếc xe buýt màu xanh.","Con chim bay trên cành cây.",
                        "Chú mèo ngồi bên cửa sổ.","Cây xanh mọc bên con đường.","Chiếc thuyền nổi trên mặt nước.",
                        "Cánh diều bay cao trên trời.","Mình cùng nghĩ một ý tưởng mới nhé.",
                        "Con bướm bay qua khu vườn.","Đám mây trôi trên trời xanh.","Bông hoa nở bên cửa sổ."};
                    for(int i=0;i<subjects.length;i++) {
                        var cueFocus=com.khuongnd.dexkids.story.EntertainmentDirector.Audience.values()[i%3];
                        var cueBeat=new com.khuongnd.dexkids.story.EntertainmentDirector.Beat("cue-"+i,cueFocus,subjects[i],
                            "Cùng quan sát hình minh họa và kể một chuyện tưởng tượng.",
                            i%2==0?com.khuongnd.dexkids.story.EntertainmentDirector.Reaction.SURPRISE:
                                com.khuongnd.dexkids.story.EntertainmentDirector.Reaction.WAVE);
                        runOnMainSync(()->{
                            try { dashboard.getClass().getMethod("showBeat",beat.getClass(),focus.getClass()).invoke(dashboard,cueBeat,cueFocus);
                                game.showEntertainmentPrompt(cueBeat,cueFocus);banner.setText(cueBeat.resolution());
                            } catch(Exception e){throw new AssertionError(e);}
                        });
                        SystemClock.sleep(400);
                        java.nio.file.Files.write(new File(getTargetContext().getCacheDir(),"qa-t032-frame").toPath(),
                            ("CUE-"+i).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        result.putString("cue_"+i,cueBeat.visual().toString()+",focus="+cueFocus);
                        if(previousTheme!=null && previousTheme!=cueBeat.visual().theme())themeTransitions++;
                        previousTheme=cueBeat.visual().theme();
                        SystemClock.sleep(5000);
                        if(targetDisplay==0) screenshot(directory,"CUE-"+i+".png");
                        runOnMainSync(()->require(prompt.getText().toString().equals(cueBeat.introduction()),
                            "Fixed visual fixture overwritten during capture"));
                    }
                    require(themeTransitions>=10,"Too few visual theme transitions");
                    require(textureBefore.equals(visualTextures(game)),"Texture set changed during visual transitions");
                    result.putInt("visual_theme_transitions",themeTransitions);
                    result.putString("preloaded_texture_snapshot",textureBefore.toString());
                }
                runOnMainSync(() -> { child.finish(); parent.finish(); });
                removeMonitor(monitor); monitor = null;
            }
            result.putString("scope", "Synthetic fixed text; real provider/semantic correspondence/endurance NOT VERIFIED");
        } finally {
            new File(getTargetContext().getCacheDir(),"qa-t032-frame").delete();
            restorePreferences(prefs,before); restorePreferences(ai,aiBefore);
        }
    }

    /** Actual GL texture handles/dimensions/formats; excludes driver overhead and compression. */
    private java.util.Map<String,String> visualTextures(com.khuongnd.dexkids.game.KidsGame game) throws Exception {
        var done=new java.util.concurrent.CountDownLatch(1);
        var snapshot=new java.util.TreeMap<String,String>();
        var failure=new java.util.concurrent.atomic.AtomicReference<Throwable>();
        com.badlogic.gdx.Gdx.app.postRunnable(()->{
            try {
                Object sprites=field(game.getScreen(),"cartoonSprites");
                for(var member:sprites.getClass().getDeclaredFields()) {
                    member.setAccessible(true);Object value=member.get(sprites);
                    if(value instanceof com.badlogic.gdx.graphics.Texture texture)
                        snapshot.put(member.getName(),textureStamp(texture));
                    else if(value instanceof com.badlogic.gdx.graphics.g2d.TextureAtlas atlas) {
                        int index=0;for(var texture:atlas.getTextures())
                            snapshot.put(member.getName()+"-"+index++,textureStamp(texture));
                    }
                }
                require(!snapshot.isEmpty(),"Preloaded visual textures missing");
            } catch(Throwable error){failure.set(error);} finally {done.countDown();}
        });
        require(done.await(5,java.util.concurrent.TimeUnit.SECONDS),"GL texture sampling stalled");
        if(failure.get()!=null)throw new AssertionError("Texture sampling failed",failure.get());
        return snapshot;
    }
    private String textureStamp(com.badlogic.gdx.graphics.Texture texture) {
        require(texture.getTextureObjectHandle()>0,"Texture not resident");
        return "handle="+texture.getTextureObjectHandle()+",size="+texture.getWidth()+"x"+texture.getHeight()+
            ",format="+texture.getTextureData().getFormat();
    }

    private static void checkVisibleText(android.widget.TextView view, String expected) {
        require(expected.contentEquals(view.getText()), "Text changed during QA");
        var layout = view.getLayout();
        require(layout != null && layout.getLineCount() > 0, "Missing native layout");
        int last = layout.getLineCount()-1;
        require(layout.getLineEnd(last) == expected.length(), "Text cut by line limit");
        require(layout.getLineBottom(last) <= view.getHeight()-view.getCompoundPaddingTop()-view.getCompoundPaddingBottom(), "Text clipped vertically");
        for (int i=0;i<=last;i++) require(layout.getEllipsisCount(i)==0, "Text ellipsized");
    }

    private static void checkDisjoint(View first, View second) {
        android.graphics.Rect a=new android.graphics.Rect(), b=new android.graphics.Rect();
        require(first.getGlobalVisibleRect(a) && second.getGlobalVisibleRect(b), "Overlay missing");
        require(!android.graphics.Rect.intersects(a,b), "Answer overlaps status/provenance panel");
    }

    /** Play real reviewed lessons on DeX; cloud/refill/matrix require explicit QA flags and fixture. */
    private void validateCreativeEndurance(Bundle result) throws Exception {
        var context=getTargetContext();
        var prefs=context.getSharedPreferences("parent_settings",0); var before=prefs.getAll();
        var ai=context.getSharedPreferences("kids_ai_settings_v1",0); var aiBefore=ai.getAll();
        var keys=context.getSharedPreferences("kids_ai_keys_v1",0); var keysBefore=keys.getAll();
        var credentials=new com.khuongnd.dexkids.ai.KidsAiKeys(context);
        boolean hadKey=credentials.configured(com.khuongnd.dexkids.ai.KidsAiProvider.GEMINI);
        File secret=new File(context.getFilesDir(),"qa-t031-runtime-secret.json");
        var state=context.getSharedPreferences("creative-pack-state-v1",0); var stateBefore=state.getAll();
        File cache=new File(context.getFilesDir(),"creative-packs-v1-both.json");
        File backup=new File(cache.getPath()+".bak");
        byte[] old=cache.isFile()?java.nio.file.Files.readAllBytes(cache.toPath()):null;
        byte[] oldBackup=backup.isFile()?java.nio.file.Files.readAllBytes(backup.toPath()):null;
        Thread[] matrixThread={null}; Bundle matrixResult=new Bundle();
        var matrixError=new java.util.concurrent.atomic.AtomicReference<Throwable>();
        try {
            var packs=new org.json.JSONArray(new String(java.nio.file.Files.readAllBytes(
                new File(context.getCacheDir(),"qa-t031-reviewed-packs.json").toPath()),java.nio.charset.StandardCharsets.UTF_8))
                ;
            var rows=packs.getJSONObject(0).getJSONArray("lessons");
            var lessons=new java.util.ArrayList<com.khuongnd.dexkids.story.CreativeLessonPack.Lesson>();
            for(int i=0;i<rows.length();i++) {
                var x=rows.getJSONObject(i);
                lessons.add(new com.khuongnd.dexkids.story.CreativeLessonPack.Lesson(x.getString("prompt"),x.getString("answer"),
                    x.getString("english"),x.getString("meaning"),x.getString("topic"),
                    com.khuongnd.dexkids.story.CreativeLessonPack.Genre.valueOf(x.getString("genre")),
                    com.khuongnd.dexkids.story.EntertainmentDirector.Audience.valueOf(x.getString("focus")),x.getInt("age"),x.getBoolean("fictional")));
            }
            state.edit().clear().commit();
            var store=new com.khuongnd.dexkids.ai.CreativePackStore(context,com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH,5,4);
            store.swap(com.khuongnd.dexkids.story.CreativeLessonPack.validate(lessons,
                com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH,5,4,java.util.List.of()));
            require(prefs.edit().putString("audience_mode","BOTH").putInt("sau_age",5).putInt("ong_age",4)
                .putInt("session_minutes",60).putBoolean("child_mic_optin",false).putBoolean("offline_tts",true)
                .putBoolean("audio_only",false).commit(),"Runtime settings failed");
            require(ai.edit().putBoolean("ai_quizzes",true).putBoolean("automatic_trip_packs",true)
                .putBoolean("creative_trip_packs",true).putBoolean("ai_trip_companion_optin",false)
                .putBoolean("child_text_cloud_explicit",false)
                .putBoolean("GEMINI_enabled",creativeNetwork).putBoolean("GROQ_enabled",false).commit(),"Endurance network settings failed");
            if(creativeNetwork) {
                require(secret.isFile(),"Missing authorized runtime fixture");
                var config=new org.json.JSONObject(new String(java.nio.file.Files.readAllBytes(secret.toPath()),java.nio.charset.StandardCharsets.UTF_8));
                require(secret.delete(),"Runtime secret removal failed");
                var settings=new com.khuongnd.dexkids.ai.KidsAiSettings(context);
                var provider=com.khuongnd.dexkids.ai.KidsAiProvider.GEMINI;
                credentials.save(provider,config.getString("key"));settings.setProvider(provider);
                settings.setModel(provider,config.getString("model"));settings.setFreeTierAcknowledged(provider,true);
            }
            monitor=addMonitor(PACKAGE+".KidsActivity",null,false);
            parent=startActivitySync(new Intent().setClassName(PACKAGE,PACKAGE+".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runOnMainSync(()->require(findPreview(parent.getWindow().getDecorView()).performClick(),"Preview failed"));
            child=waitForMonitorWithTimeout(monitor,10000); require(child!=null,"Child missing");
            SystemClock.sleep(3000);
            require(child.getWindow().getDecorView().getWidth()>=1700 && child.getWindow().getDecorView().getHeight()>=900,
                "Creative endurance requires maximized FullHD DeX window");
            if(creativeMatrix) {
                matrixThread[0]=new Thread(()->{
                    try { generateReviewedMatrix(context,new com.khuongnd.dexkids.ai.KidsAiGateway(context),matrixResult,
                        packs,new int[][]{{4,7},{6,7}}); }
                    catch(Throwable error){matrixError.set(error);}
                },"qa-creative-age-matrix");
                matrixThread[0].setDaemon(true);matrixThread[0].start();
            }
            long started=SystemClock.elapsedRealtime(), previous=-1; int checks=0;
            boolean pauseChecked=false,audioStarted=false,audioFinished=false;
            int resolutionsBeforeAudio=0;
            java.util.Set<String> heard=new java.util.HashSet<>();
            java.util.Set<String> fingerprints=new java.util.HashSet<>();
            while(SystemClock.elapsedRealtime()-started<creativeSeconds*1000L) {
                checkDemo(child); require(child.getDisplay().getDisplayId()==targetDisplay,"Moved off DeX");
                long elapsed=SystemClock.elapsedRealtime()-started;
                if(creativeSeconds==600 && !pauseChecked && elapsed>=30000) {
                    openParentMenu();
                    SystemClock.sleep(1000);
                    runOnMainSync(()->parentDialog().getButton(android.app.AlertDialog.BUTTON_NEGATIVE).performClick());
                    pauseChecked=true;result.putBoolean("cached_ai_f10_continue",true);
                }
                if(creativeSeconds==600 && !audioStarted && elapsed>=60000) {
                    int[] beforeAudio={0};
                    runOnMainSync(()->{try{
                        beforeAudio[0]=((com.khuongnd.dexkids.story.ChildEngagementMetrics)field(child,"engagementMetrics")).snapshot().resolutions();
                        new com.khuongnd.dexkids.ParentSettings(child).setAudioOnly(true);
                        ((com.khuongnd.dexkids.game.KidsGame)field(child,"runningGame")).setAudioOnly(true);
                        ((com.khuongnd.dexkids.AdventureDashboard)field(child,"adventureDashboard")).setVisualsEnabled(false);
                    }catch(Exception e){throw new AssertionError(e);}});
                    resolutionsBeforeAudio=beforeAudio[0];audioStarted=true;
                }
                if(creativeSeconds==600 && audioStarted && !audioFinished && elapsed>=180000) {
                    int[] afterAudio={0};
                    runOnMainSync(()->{try{
                        afterAudio[0]=((com.khuongnd.dexkids.story.ChildEngagementMetrics)field(child,"engagementMetrics")).snapshot().resolutions();
                        new com.khuongnd.dexkids.ParentSettings(child).setAudioOnly(false);
                        ((com.khuongnd.dexkids.game.KidsGame)field(child,"runningGame")).setAudioOnly(false);
                        ((com.khuongnd.dexkids.AdventureDashboard)field(child,"adventureDashboard")).setVisualsEnabled(true);
                    }catch(Exception e){throw new AssertionError(e);}});
                    require(afterAudio[0]>resolutionsBeforeAudio,"Cached AI stopped resolving in Audio-only");
                    audioFinished=true;result.putInt("audio_only_resolutions",afterAudio[0]-resolutionsBeforeAudio);
                }
                var sampled=new java.util.concurrent.CountDownLatch(1);
                long[] frames={-1}; double[] values={0,0};
                com.badlogic.gdx.Gdx.app.postRunnable(()->{
                    try {
                        var game=(com.khuongnd.dexkids.game.KidsGame)((com.khuongnd.dexkids.KidsActivity)child).getApplicationListener();
                        var metric=(com.khuongnd.dexkids.game.RenderRunMetrics)field(game.getScreen(),"runMetrics");
                        frames[0]=metric.frames(); values[0]=metric.averageFps();values[1]=metric.p95UpperMs();
                    } catch(Exception error) { frames[0]=-1; } finally { sampled.countDown(); }
                });
                require(sampled.await(5,java.util.concurrent.TimeUnit.SECONDS),"GL stalled");
                require(frames[0]>previous,"Frames stopped"); previous=frames[0]; checks++;
                runOnMainSync(()->{
                    try {
                        var coordinator=field(child,"tripPackCoordinator");
                        var snapshot=field(coordinator,"creativeActive");require(snapshot!=null,"Reviewed creative pack not loaded");
                        fingerprints.add(((com.khuongnd.dexkids.ai.CreativePackStore.Snapshot)snapshot).getFingerprint());
                        String id=(String)field(coordinator,"creativePendingId");if(id!=null)heard.add(id);
                        var banner=(android.widget.TextView)field(child,"entertainmentBanner");
                        if(banner.getVisibility()==View.VISIBLE) {
                            checkVisibleText(banner,banner.getText().toString());
                            checkDisjoint(banner,(View)field(child,"poiNativeStatus"));
                        }
                    } catch(Exception error) { throw new AssertionError(error); }
                });
                result.putDouble("avg_fps",values[0]);result.putDouble("p95_upper_ms",values[1]);
                android.util.Log.i("Fold3QA","creative_seconds="+(SystemClock.elapsedRealtime()-started)/1000+",checks="+checks+",fps="+values[0]);
                SystemClock.sleep(Math.min(10000,Math.max(1,creativeSeconds*1000L-(SystemClock.elapsedRealtime()-started))));
            }
            result.putLong("wall_seconds",(SystemClock.elapsedRealtime()-started)/1000);
            result.putInt("external_display_checks",checks);result.putInt("observed_creative_beats",heard.size());
            result.putInt("observed_reviewed_packs",fingerprints.size());
            if(matrixThread[0]!=null) {
                matrixThread[0].join(5000);require(!matrixThread[0].isAlive(),"Provider age matrix did not finish");
                result.putAll(matrixResult);
                if(matrixError.get()!=null)throw new AssertionError("Provider age matrix failed",matrixError.get());
            }
            if(creativeSeconds>=1800)require(heard.size()>=15,"Too few creative lessons played");
            if(creativeNetwork && creativeSeconds>=1800)require(fingerprints.size()>=3,"Initial refresh and exhaustion refill not both observed");
            com.khuongnd.dexkids.story.ChildEngagementMetrics.Snapshot[] counts={null};
            runOnMainSync(()->{try{counts[0]=((com.khuongnd.dexkids.story.ChildEngagementMetrics)field(child,"engagementMetrics")).snapshot();}
                catch(Exception e){throw new AssertionError(e);}});
            result.putString("engagement_metrics",counts[0].toString());
            if(creativeSeconds>=1800)require(counts[0].voiceUnavailable()==0,"Silent offline voice fallback");
            require(result.getDouble("avg_fps")>=58 && result.getDouble("p95_upper_ms")<=34,"FullHD performance gate failed");
            result.putString("scope","Real reviewed pack playback/pose/HUD on Android FullHD display="+targetDisplay+"; cloud refresh/refill="+creativeNetwork+"; no child mic/text/GPS, driving or navigation app QA");
        } finally {
            if(matrixThread[0]!=null && matrixThread[0].isAlive()) {matrixThread[0].interrupt();matrixThread[0].join(15000);}
            runOnMainSync(()->{if(child!=null)child.finish();if(parent!=null)parent.finish();});
            SystemClock.sleep(500);
            if(cache.exists())java.nio.file.Files.delete(cache.toPath());if(backup.exists())java.nio.file.Files.delete(backup.toPath());
            if(old!=null)java.nio.file.Files.write(cache.toPath(),old);if(oldBackup!=null)java.nio.file.Files.write(backup.toPath(),oldBackup);
            restorePreferences(state,stateBefore);restorePreferences(prefs,before);restorePreferences(ai,aiBefore);
            secret.delete();
            if(creativeNetwork && !hadKey)credentials.delete(com.khuongnd.dexkids.ai.KidsAiProvider.GEMINI);
            restorePreferences(keys,keysBefore);
        }
    }

    private void restorePreferences(android.content.SharedPreferences prefs,
            java.util.Map<String, ?> values) {
        android.content.SharedPreferences.Editor e = prefs.edit().clear();
        for (java.util.Map.Entry<String, ?> entry : values.entrySet()) {
            Object v = entry.getValue(); String k = entry.getKey();
            if (v instanceof Boolean) e.putBoolean(k, (Boolean)v);
            else if (v instanceof Integer) e.putInt(k, (Integer)v);
            else if (v instanceof Long) e.putLong(k, (Long)v);
            else if (v instanceof Float) e.putFloat(k, (Float)v);
            else if (v instanceof String) e.putString(k, (String)v);
            else throw new AssertionError("Unsupported QA preference type");
        }
        require(e.commit(), "Preference restore failed");
    }

    /** Explicit parent-requested local setup only, never packaged in production APK. */
    private void configureParentAi(Bundle result) throws Exception {
        var context = getTargetContext();
        File fixture = new File(context.getFilesDir(), "qa-parent-ai-secret.json");
        require(fixture.isFile(), "Missing explicitly authorized private setup fixture");
        var config = new org.json.JSONObject(new String(java.nio.file.Files.readAllBytes(fixture.toPath()),
            java.nio.charset.StandardCharsets.UTF_8));
        require(fixture.delete(), "Cannot remove private setup fixture");
        require(config.getBoolean("parent_authorized_child_text"), "Parent consent missing");
        var provider = com.khuongnd.dexkids.ai.KidsAiProvider.GEMINI;
        var settings = new com.khuongnd.dexkids.ai.KidsAiSettings(context);
        boolean acknowledged = config.getBoolean("free_tier_acknowledged");
        if (acknowledged) {
            new com.khuongnd.dexkids.ai.KidsAiKeys(context).save(provider, config.getString("key"));
            settings.setModel(provider, config.getString("model"));
            settings.setFreeTierAcknowledged(provider, true);
            settings.setProvider(provider);
            settings.setActive(provider, true);
        }
        settings.setEnabled(true);
        settings.setCloudChildReply(true);
        require(settings.getCloudChildReply(), "Cloud text consent did not persist");
        result.putBoolean("parent_authorized_cloud_text", true);
        result.putBoolean("gemini_ready", acknowledged && new com.khuongnd.dexkids.ai.KidsAiKeys(context).configured(provider));
        result.putString("scope", "Parent-requested local preferences; encrypted key when Free Tier confirmed; no network request/audio/GPS upload");
    }

    private void generateReviewedMatrix(android.content.Context context,
            com.khuongnd.dexkids.ai.KidsAiGateway gateway, Bundle result,
            org.json.JSONArray packs, int[][] ages) throws Exception {
        for (int[] pair : ages) {
            gateway.beginCreativeCycle();
            com.khuongnd.dexkids.story.CreativeLessonPack pack = null;
            int attempts = creativeSingle ? creativeAttempts : 6;
            for (int attempt=0;attempt<attempts && pack==null;attempt++) {
                try {
                    pack=gateway.generateCreativePack(
                        com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH,
                        pair[0],pair[1],java.util.List.of(),java.util.List.of());
                } catch (Exception failure) {
                    String detail=failure.getMessage();
                    result.putString("attempt_"+pair[0]+"_"+attempt,failure.getClass().getSimpleName()+
                        (detail!=null && detail.matches("[a-zA-Z0-9 _,.-]{1,120}")?": "+detail:""));
                        Bundle progress=new Bundle();progress.putString("qa_progress",result.getString("attempt_"+pair[0]+"_"+attempt));
                        sendStatus(0,progress);
                    // Real QA must not spend more quota after rate/billing/provider refusal.
                    if(failure instanceof com.khuongnd.dexkids.ai.KidsAiRetryAfterException ||
                        "AI_PROVIDER_REFUSED".equals(detail) || "NO_PROVIDER".equals(detail) ||
                        "AI_DISABLED".equals(detail)) throw failure;
                    if(attempt==attempts-1) throw failure;
                    SystemClock.sleep(com.khuongnd.dexkids.story.TripPackRetryPolicy.delayAfterFailure(attempt,0));
                }
            }
            var rows=new org.json.JSONArray();
            for(var x:pack.lessons()) rows.put(new org.json.JSONObject()
                .put("age",x.targetAge()).put("focus",x.focus().name()).put("genre",x.genre().name())
                .put("prompt",x.prompt()).put("answer",x.answer())
                .put("english",x.englishWord()).put("meaning",x.meaningVi())
                .put("topic",x.topic())
                .put("fictional",x.fictional()));
            packs.put(new org.json.JSONObject().put("sauAge",pair[0]).put("ongAge",pair[1]).put("lessons",rows));
            java.nio.file.Files.write(new File(context.getCacheDir(),"qa-t031-reviewed-packs.json").toPath(),
                packs.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            result.putInt("reviewed_pack_"+pair[0]+"_"+pair[1],pack.lessons().size());
        }
    }

    private void validateGeminiLive(Bundle result) throws Exception {
        android.content.Context context = getTargetContext();
        File fixture = new File(context.getFilesDir(), "qa-gemini-secret.json");
        android.content.SharedPreferences settings = context.getSharedPreferences("kids_ai_settings_v1", 0);
        android.content.SharedPreferences keys = context.getSharedPreferences("kids_ai_keys_v1", 0);
        java.util.Map<String, ?> oldSettings = settings.getAll(), oldKeys = keys.getAll();
        com.khuongnd.dexkids.ai.KidsAiKeys credentials = new com.khuongnd.dexkids.ai.KidsAiKeys(context);
        com.khuongnd.dexkids.ai.KidsAiProvider provider = com.khuongnd.dexkids.ai.KidsAiProvider.GEMINI;
        boolean hadKey = credentials.configured(provider);
        try {
            require(fixture.isFile(), "Missing app-private Gemini fixture");
            org.json.JSONObject config = new org.json.JSONObject(new String(
                    java.nio.file.Files.readAllBytes(fixture.toPath()), java.nio.charset.StandardCharsets.UTF_8));
            require(fixture.delete(), "Cannot remove secret fixture");
            credentials.save(provider, config.getString("key"));
            com.khuongnd.dexkids.ai.KidsAiSettings prefs = new com.khuongnd.dexkids.ai.KidsAiSettings(context);
            prefs.setEnabled(true); prefs.setCloudChildReply(false); prefs.setProvider(provider);
            prefs.setModel(provider, config.getString("model"));
            // Temporary test gate, not an assertion about account billing/free-tier eligibility.
            prefs.setFreeTierAcknowledged(provider, true); prefs.setActive(provider, true);
            prefs.setActive(com.khuongnd.dexkids.ai.KidsAiProvider.GROQ, false);
            if ("creative_live".equals(mode)) {
                prefs.setAutomaticPacks(true); prefs.setCreativePacks(true);
                var gateway = new com.khuongnd.dexkids.ai.KidsAiGateway(context);
                var packs = new org.json.JSONArray();
                int[][] ages = creativeSingle ? new int[][]{{creativeSauAge,creativeOngAge}} : new int[][]{{5,4},{4,7},{6,7}};
                generateReviewedMatrix(context,gateway,result,packs,ages);
                prefs.setCreativePacks(false);
                boolean revoked=false;
                try { gateway.generateCreativePack(com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH,
                    5,4,java.util.List.of(),java.util.List.of()); }
                catch(IllegalStateException expected) { revoked=true; }
                require(revoked,"Revoked creative consent accepted");
                result.putBoolean("creative_revocation",true);
                result.putString("scope","Real production generation + independent review; generated lessons only, no GPS/audio/child data; human QA pending");
                return;
            }
            if ("gemini_context".equals(mode)) {
                validateGeminiContext(context, prefs, result);
                result.putString("model", config.getString("model"));
                return;
            }
            java.util.List<com.khuongnd.dexkids.ai.KidsQuiz> quizzes =
                new com.khuongnd.dexkids.ai.KidsAiGateway(context).generate(
                    "osm:way:530247697",
                    "Công viên Xuân An là không gian xanh ở Long Khánh. Cây trong công viên cho chúng ta bóng mát.",
                    new com.khuongnd.dexkids.ai.KidsQuiz("Cây xanh cho chúng ta điều gì?",
                        "Cây trong công viên cho chúng ta bóng mát.", "Con thích nhìn cây xanh không?"), 4);
            require(quizzes.size() >= 10, "Gateway did not validate ten quizzes");
            result.putInt("validated_quizzes", quizzes.size());
            result.putString("provider", "GEMINI");
            result.putString("model", config.getString("model"));
            result.putString("payload", "Authored sourced fact and age only; no GPS/audio/child reply");
        } finally {
            fixture.delete();
            if (!hadKey) credentials.delete(provider);
            restorePreferences(keys, oldKeys);
            restorePreferences(settings, oldSettings);
        }
    }

    private void validateGeminiContext(android.content.Context context,
            com.khuongnd.dexkids.ai.KidsAiSettings prefs, Bundle result) throws Exception {
        var gateway = new com.khuongnd.dexkids.ai.KidsAiGateway(context);
        var cachePrefs = context.getSharedPreferences("kids_ai_quiz_cache_v1", 0);
        var before = cachePrefs.getAll();
        try {
            var cache = new com.khuongnd.dexkids.ai.KidsAiQuizCache(context);
            int total = 0;
            var periods = com.khuongnd.dexkids.story.DayPeriod.values();
            for (int i = 0; i < periods.length; i++) {
                var period = periods[i];
                int age = i % 2 == 0 ? 4 : 5;
                String poi = i % 2 == 0 ? "osm:way:530247697" : "osm:way:234800751";
                // Synthetic quality-gated proximity on existing coordinates, entirely local.
                String poiRow = null;
                try (var in = context.getAssets().open("poi/live-landmarks.tsv")) {
                    for (String line : new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).split("\\R"))
                        if (line.startsWith(poi + "\t")) poiRow = line;
                }
                require(poiRow != null, "POI coordinate fixture missing");
                String pack = "# dexkids-poi-v1\n" + com.khuongnd.dexkids.geo.OfflinePoiCatalog.HEADER + "\n" + poiRow + "\n";
                var one = com.khuongnd.dexkids.geo.OfflinePoiCatalog.parse(new java.io.ByteArrayInputStream(pack.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                var entry = one.entries().get(0).poi();
                var engine = new com.khuongnd.dexkids.geo.OfflinePoiEngine(one);
                long tick = 100000;
                require(engine.observe(new com.khuongnd.dexkids.geo.JourneyPosition(entry.latitude(), entry.longitude(), 80f, 0f, tick, true), tick).isEmpty(), "Poor GPS accuracy selected a POI");
                var notice = engine.observe(new com.khuongnd.dexkids.geo.JourneyPosition(entry.latitude(), entry.longitude(), 5f, 0f, tick + 1000, true), tick + 1000).orElseThrow();
                require(poi.equals(notice.entry().poi().id()) && notice.simulated(), "Incorrect synthetic nearby POI");
                String fact = null;
                try (var in = context.getAssets().open("narration/live-landmarks.tsv")) {
                    String text = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    for (String line : text.split("\\R")) {
                        String[] row = line.split("\\t", -1);
                        if (row.length == 7 && poi.equals(row[1])) fact = row[2];
                    }
                }
                require(fact != null, "Sourced POI fixture missing");
                String answer = i % 2 == 0 ? "Cây trong công viên cho chúng ta bóng mát." : "Ở đây có sóng biển và bãi cát.";
                String question = i % 2 == 0 ? "Cây xanh cho chúng ta điều gì?" : "Bãi biển có sóng biển và bãi cát không?";
                var base = new com.khuongnd.dexkids.ai.KidsQuiz(question, answer, "Con muốn tưởng tượng thêm điều gì?");
                var quizzes = gateway.generate(poi, fact, base, age, periods[i]);
                require(quizzes.size() >= 10, "Context quiz count too small");
                String otherAgeKey = poi.replace(":", "_") + "_age" + (age == 4 ? 5 : 4) + "_" + periods[i].name();
                cachePrefs.edit().remove(otherAgeKey).commit();
                cache.put(poi, age, fact, base, quizzes, periods[i]);
                require(cache.get(poi, age, fact, base, periods[i]).size() == quizzes.size(), "Context cache missing");
                var other = periods[(i + 1) % periods.length];
                // Clear only this other-period fixture key before checking isolation.
                String otherKey = poi.replace(":", "_") + "_age" + age + "_" + other.name();
                cachePrefs.edit().remove(otherKey).commit();
                require(cache.get(poi, age, fact, base, other).isEmpty(), "Day-period cache leaked");
                require(cache.get(poi, age == 4 ? 5 : 4, fact, base, periods[i]).isEmpty(), "Age cache leaked");
                require(cache.get(poi, age, fact + " Nguồn đã thay đổi.", base, periods[i]).isEmpty(), "Source cache leaked");
                total += quizzes.size();
                var sample = quizzes.stream().filter(q -> q.getQuestion().toLowerCase(java.util.Locale.ROOT).contains(period.label()) ||
                    q.getFollowUp().toLowerCase(java.util.Locale.ROOT).contains(period.label())).findFirst().orElseThrow();
                result.putString("context_" + periods[i].name(), "age=" + age + ";poi=" + poi +
                    ";quizzes=" + quizzes.size() + ";sample=" + sample.getQuestion() + " / " + sample.getFollowUp());
                if (i == 0 || i == periods.length - 1) {
                    prefs.setCloudChildReply(true); // Synthetic authored phrase only, not a child's recording.
                    var reply = gateway.childReply(question, fact, "Con thích nhìn cây và sóng biển", age, periods[i]);
                    require(!reply.getText().isBlank() && reply.getNextQuestion().endsWith("?"), "Reply invalid");
                    result.putString("synthetic_reply_" + periods[i].name(), reply.getText() + " / " + reply.getNextQuestion());
                    prefs.setCloudChildReply(false);
                }
            }
            result.putInt("validated_quizzes", total);
            result.putString("provider", "GEMINI");
            result.putString("payload", "Existing sourced POI facts, ages4/5, four coarse local day periods; two authored synthetic replies. No GPS/audio/real child text.");
            result.putString("scope", "Production gateway/cache on Android; synthetic sourced POIs, not real road GPS/ASR or child observation.");
        } finally { restorePreferences(cachePrefs, before); }
    }

    private void validateAudiencePicker(Bundle result) throws Exception {
        require(fullHdQaDisplay(), "Explicit external display or emulator display required");
        android.content.SharedPreferences prefs = getTargetContext().getSharedPreferences("parent_settings", 0);
        java.util.Map<String, ?> original = prefs.getAll();
        try {
            parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            android.accessibilityservice.AccessibilityServiceInfo service = getUiAutomation().getServiceInfo();
            service.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            getUiAutomation().setServiceInfo(service);
            String[] ids = {"SAU", "ONG", "BOTH"};
            var settings = new com.khuongnd.dexkids.ParentSettings(getTargetContext());
            String[] labels = {"Cho Sâu (" + settings.getSauAge() + " tuổi)",
                "Cho Ong (" + settings.getOngAge() + " tuổi)",
                "Cả Sâu và Ong cùng xem (" + settings.getSauAge() + " và " + settings.getOngAge() + " tuổi)"};
            for (int i = 0; i < ids.length; i++) {
                String buttonText = "Chọn người xem · " + new com.khuongnd.dexkids.ParentSettings(getTargetContext()).getAudienceLabel();
                runOnMainSync(() -> require(findButton(parent.getWindow().getDecorView(), buttonText).performClick(), "Audience picker click failed"));
                boolean clicked = false; long until = SystemClock.elapsedRealtime() + 5000;
                while (!clicked && SystemClock.elapsedRealtime() < until) {
                    java.util.List<android.view.accessibility.AccessibilityWindowInfo> windows = getUiAutomation().getWindowsOnAllDisplays().get(targetDisplay);
                    if (windows != null) for (android.view.accessibility.AccessibilityWindowInfo window : windows) {
                        android.view.accessibility.AccessibilityNodeInfo root = window.getRoot();
                        if (root == null) continue;
                        for (android.view.accessibility.AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(labels[i])) {
                            if (labels[i].contentEquals(node.getText()) && node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)) { clicked = true; break; }
                        }
                        if (clicked) break;
                    }
                    if (!clicked) SystemClock.sleep(100);
                }
                require(clicked, "Visible audience option not clickable on DeX");
                SystemClock.sleep(250);
                require(ids[i].equals(prefs.getString("audience_mode", "")), "UI choice did not persist");
                result.putString(ids[i], "PASS: visible picker choice persisted on same DeX display");
            }
        } finally { restorePreferences(prefs, original); }
    }

    private void validateOpeningVariety(Bundle result) throws Exception {
        android.content.SharedPreferences prefs = getTargetContext().getSharedPreferences("parent_settings", 0);
        java.util.Map<String, ?> before = prefs.getAll();
        java.util.Set<String> firstIds = new java.util.HashSet<>();
        try {
            require(prefs.edit().putString("audience_mode", "BOTH").putInt("entertainment_opening_BOTH", 1)
                .putBoolean("child_mic_optin", false).commit(), "Opening fixture settings");
            for (int session = 0; session < 3; session++) {
                monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
                parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                runOnMainSync(() -> require(findPreview(parent.getWindow().getDecorView()).performClick(), "Demo missing"));
                child = waitForMonitorWithTimeout(monitor, 10000);
                require(child != null && child.getDisplay().getDisplayId() == targetDisplay, "Child off DeX");
                var director = (com.khuongnd.dexkids.story.EntertainmentDirector) field(child, "entertainmentDirector");
                var expected = new com.khuongnd.dexkids.story.EntertainmentDirector();
                expected.setOpeningOffset(director.openingOffset());
                var beat = expected.next(com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH);
                require(firstIds.add(beat.id()), "Repeated opening across new journeys");
                require(!"sau-colors".equals(beat.id()), "Old fixed yellow-bus opener returned immediately");
                long until = SystemClock.elapsedRealtime() + 20000;
                boolean shown = false;
                while (SystemClock.elapsedRealtime() < until) {
                    String[] line = {null};
                    runOnMainSync(() -> { try { line[0] = (String) field(child, "subtitleText"); }
                        catch (Exception e) { throw new AssertionError(e); } });
                    if (line[0] != null && line[0].contains(beat.introduction())) { shown = true; break; }
                    SystemClock.sleep(100);
                }
                require(shown, "Expected varied opening not displayed");
                Bundle state = savedState(child);
                require(state.getInt("entertainment.opening.offset") == session + 1, "Opening offset not saved");
                require(prefs.getInt("entertainment_opening_BOTH", -1) == session + 2, "Next session offset not persisted");
                result.putString("session_" + session, beat.id());
                if (session == 0) {
                    int nextOffset = prefs.getInt("entertainment_opening_BOTH", -1);
                    Activity old = child;
                    removeMonitor(monitor); monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
                    runOnMainSync(old::recreate);
                    child = waitForMonitorWithTimeout(monitor, 10000);
                    require(child != null && child != old, "Recreation missing");
                    var restored = (com.khuongnd.dexkids.story.EntertainmentDirector) field(child, "entertainmentDirector");
                    require(restored.openingOffset() == 1 && restored.position() == state.getInt("entertainment.position"),
                        "Recreation reset entertainment selection");
                    require(prefs.getInt("entertainment_opening_BOTH", -1) == nextOffset, "Recreation consumed a new opening");
                    result.putBoolean("recreation_keeps_opening", true);
                }
                runOnMainSync(() -> child.finish());
                removeMonitor(monitor); monitor = null;
            }
        } finally { restorePreferences(prefs, before); }
    }

    private void validateAudienceAudio(Bundle result) throws Exception {
        require(fullHdQaDisplay(), "Explicit external display or emulator display required");
        android.content.SharedPreferences prefs = getTargetContext().getSharedPreferences("parent_settings", 0);
        java.util.Map<String, ?> original = prefs.getAll();
        android.media.AudioManager audio = (android.media.AudioManager)getTargetContext().getSystemService("audio");
        int volume = audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
        result.putInt("original_media_volume",volume);
        try {
            for (String audience : audioAudience.isEmpty()?new String[]{"SAU", "ONG", "BOTH"}:new String[]{audioAudience}) {
                require(prefs.edit().putString("audience_mode", audience).putBoolean("ambient_music", true)
                    .putBoolean("audio_effects", true).putBoolean("offline_tts", true)
                    .putBoolean("child_mic_optin", false).putBoolean("audio_only", false).commit(), "Fixture settings failed");
                monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
                parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                com.khuongnd.dexkids.ParentSettings selected = new com.khuongnd.dexkids.ParentSettings(getTargetContext());
                int configuredAge="SAU".equals(audience)?selected.getSauAge():
                    "ONG".equals(audience)?selected.getOngAge():Math.min(selected.getSauAge(),selected.getOngAge());
                require(selected.getActiveAge()==Math.min(6,configuredAge),"Audience age mismatch");
                runOnMainSync(() -> require(findPreview(parent.getWindow().getDecorView()).performClick(), "Demo click failed"));
                child = waitForMonitorWithTimeout(monitor, 10000);
                require(child != null && child.getDisplay().getDisplayId() == targetDisplay, "Child missing on DeX");
                SystemClock.sleep(3000);
                var game = (com.khuongnd.dexkids.game.KidsGame)
                    ((com.khuongnd.dexkids.KidsActivity)child).getApplicationListener();
                var sprites = field(game.getScreen(), "cartoonSprites");
                for (String textureName : new String[]{"sauStoryTexture", "ongStoryTexture"}) {
                    var texture = (com.badlogic.gdx.graphics.Texture)field(sprites, textureName);
                    require(texture != null && texture.getWidth() == 1024 && texture.getHeight() == 1536,
                        "High-resolution child story sheet not loaded: " + textureName);
                }
                Object sound = field(child, "soundscape");
                require(((java.util.Set<?>)field(sound, "loaded")).size() == 6, "Six SoundPool samples not loaded");
                require(((Number)field(sound, "musicStream")).intValue() > 0, "Ambient stream not started");
                // nextJourney inserts focused learning cards every third beat; BOTH first
                // reaches a shared learning turn on the ninth beat, not the third.
                int expectedBeats="BOTH".equals(audience)?9:3;
                long until = SystemClock.elapsedRealtime() + ("BOTH".equals(audience)?430000:160000);
                com.khuongnd.dexkids.story.ChildEngagementMetrics.Snapshot[] snapshot = {null};
                boolean[] speechDuck = {false};
                while (SystemClock.elapsedRealtime() < until) {
                    runOnMainSync(() -> {
                        try {
                            snapshot[0] = ((com.khuongnd.dexkids.story.ChildEngagementMetrics)field(child, "engagementMetrics")).snapshot();
                            if ((Boolean)field(sound, "speechActive")) {
                                require(((Number)field(sound, "musicStream")).intValue() == 0, "Ambient overlaps speech");
                                speechDuck[0] = true;
                            }
                            String line = (String)field(child, "subtitleText");
                            if (line != null && line.contains("CAPYBARA") && line.contains("DÀNH CHO")) {
                                if ("SAU".equals(audience)) require(!line.contains("Ong"), "Wrong child in Sau mode");
                                if ("ONG".equals(audience)) require(!line.contains("Sâu"), "Wrong child in Ong mode");
                            }
                        } catch (Exception e) { throw new AssertionError(e); }
                    });
                    if (snapshot[0].starts() >= expectedBeats && snapshot[0].resolutions() >= expectedBeats) break;
                    SystemClock.sleep(250);
                }
                result.putString(audience + "_metrics", snapshot[0].toString());
                require(snapshot[0].starts() >= expectedBeats && snapshot[0].resolutions() >= expectedBeats, "Actual audience beats did not complete");
                require(speechDuck[0], "No production speech duck observed");
                require(snapshot[0].voiceUnavailable() == 0, "A beat silently fell back without voice");
                if ("SAU".equals(audience)) require(snapshot[0].ongBeats() == 0 && snapshot[0].togetherBeats() == 0, "Solo metrics mismatch");
                if ("ONG".equals(audience)) require(snapshot[0].sauBeats() == 0 && snapshot[0].togetherBeats() == 0, "Solo metrics mismatch");
                if ("BOTH".equals(audience)) require(snapshot[0].sauBeats() > 0 && snapshot[0].ongBeats() > 0 && snapshot[0].togetherBeats() > 0, "Unbalanced both rotation");
                result.putString(audience + "_metrics", snapshot[0].toString());
                android.util.Log.i("Fold3QA","audience_audio="+audience+","+snapshot[0]);
                openParentMenu();
                require((Boolean)field(sound, "paused") && ((Number)field(sound, "musicStream")).intValue() == 0, "F10 did not pause ambient");
                runOnMainSync(() -> parentDialog().getButton(android.app.AlertDialog.BUTTON_NEGATIVE).performClick());
                SystemClock.sleep(700);
                require(!(Boolean)field(sound, "paused"), "Continue did not resume soundscape");
                runOnMainSync(() -> child.finish());
                long disposeUntil=SystemClock.elapsedRealtime()+10000;
                boolean[] disposed={false};
                while(!disposed[0] && SystemClock.elapsedRealtime()<disposeUntil) {
                    runOnMainSync(()->{try{disposed[0]=(Boolean)field(sound,"disposed");}
                        catch(Exception e){throw new AssertionError(e);}});
                    if(!disposed[0])SystemClock.sleep(100);
                }
                require(disposed[0], "SoundPool not disposed after bounded lifecycle wait");
                removeMonitor(monitor); monitor = null;
            }
            result.putInt("final_media_volume",audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC));
            require(audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) == volume, "Device volume changed");
            result.putString("audio_contract", "6 loaded samples, ambient starts, TTS ducks, F10 pauses, Continue resumes, shutdown disposes, volume preserved");
            result.putString("child_art", "Sâu/Ong 1024x1536 story sheets loaded in all three audience sessions");
        } finally { restorePreferences(prefs, original); }
    }

    private void validateMusicSmoke(Bundle result) throws Exception {
        var prefs = getTargetContext().getSharedPreferences("parent_settings", 0);
        var before = prefs.getAll();
        try {
            require(prefs.edit().putBoolean("ambient_music", true).putBoolean("offline_tts", true)
                .putBoolean("child_mic_optin", false).commit(), "Music fixture preferences failed");
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runOnMainSync(() -> require(findPreview(parent.getWindow().getDecorView()).performClick(), "Demo button missing"));
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null, "DEMO child missing");
            awaitChildReady(child);
            Object[] sound = {null};
            long until = SystemClock.elapsedRealtime() + 15000;
            boolean[] music = {false};
            while (!music[0] && SystemClock.elapsedRealtime() < until) {
                runOnMainSync(() -> { try {
                    sound[0] = field(child, "soundscape");
                    if (sound[0] != null) music[0] = ((Number)field(sound[0], "musicStream")).intValue() > 0;
                } catch (Exception e) { throw new AssertionError(e); } });
                SystemClock.sleep(100);
            }
            require(music[0], "Ambient stream failed to start");
            boolean[] duck = {false}, resumed = {false};
            until = SystemClock.elapsedRealtime() + 45000;
            while (!resumed[0] && SystemClock.elapsedRealtime() < until) {
                runOnMainSync(() -> { try {
                    boolean talking = (Boolean)field(sound[0], "speechActive");
                    int stream = ((Number)field(sound[0], "musicStream")).intValue();
                    if (talking) { require(stream == 0, "Ambient overlaps TTS"); duck[0] = true; }
                    if (duck[0] && !talking && stream > 0) resumed[0] = true;
                } catch (Exception e) { throw new AssertionError(e); } });
                SystemClock.sleep(100);
            }
            require(duck[0] && resumed[0], "Actual narration duck/resume was not observed");
            openParentMenu();
            require(((Number)field(sound[0], "musicStream")).intValue() == 0, "Ambient continues with parent menu open");
            result.putString("music", "PASS: actual ambient stream, production TTS duck/resume, F10 stops music; no volume changes or recording");
        } finally { restorePreferences(prefs, before); }
    }

    private void validateParentControls(Bundle result) throws Exception {
        android.content.SharedPreferences prefs = getTargetContext()
                .getSharedPreferences("parent_settings", android.content.Context.MODE_PRIVATE);
        boolean hadAudioOnly = prefs.contains("audio_only");
        boolean oldAudioOnly = prefs.getBoolean("audio_only", false);
        android.content.SharedPreferences ai = getTargetContext().getSharedPreferences(
                "kids_ai_settings_v1", android.content.Context.MODE_PRIVATE);
        var aiBefore = ai.getAll();
        boolean oldAi = ai.getBoolean("ai_quizzes", true);
        boolean oldCloud = ai.getBoolean("child_text_cloud_explicit", false);
        try {
            // Upgrade regression: obsolete PIN fields are removed; normal parent UI needs no login.
            require(prefs.edit().putString("pin_credential", "obsolete-test-only")
                    .putInt("pin_failures", 2).putLong("pin_retry_after", 12345L)
                    .putBoolean("audio_only", false).commit(), "Cannot prepare parent preferences");
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            require(!prefs.contains("pin_credential") && !prefs.contains("pin_failures") &&
                    !prefs.contains("pin_retry_after"), "Legacy PIN credentials were not purged");
            runOnMainSync(() -> {
                Button preview = findPreview(parent.getWindow().getDecorView());
                require(preview != null && preview.performClick(), "DEMO button unavailable without PIN");
            });
            result.putString("parent_access", "PASS: legacy PIN purged, dashboard immediately accessible");
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null, "DEMO child missing");
            awaitChildReady(child);
            glDistance(); // Confirm GL is running before opening a native dialog.
            require(!savedState(child).containsKey("kids.session.deadline.elapsed"),
                    "Legacy session deadline still present");
            openParentMenu();
            double paused = glDistance();
            SystemClock.sleep(700);
            require(Math.abs(glDistance() - paused) < 0.001, "Journey advances while Parent menu is open");
            runOnMainSync(() -> require(parentDialog().getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                    .performClick(), "Continue button failed"));
            SystemClock.sleep(700);
            require(glDistance() > paused, "Continue did not resume journey");
            require(!savedState(child).containsKey("kids.session.deadline.elapsed"),
                    "Parent menu added a timeout");
            result.putString("pause_resume", "PASS: frozen distance, Continue advances, no session timeout");
            openParentMenu();
            boolean[] selected = {false};
            runOnMainSync(() -> {
                android.widget.ListView list = parentDialog().getListView();
                require(list != null && list.isShown() && list.getChildCount() > 0,
                        "Parent action list is hidden; Audio-only/AI actions inaccessible");
                selected[0] = list.performItemClick(list.getChildAt(0), 0, list.getAdapter().getItemId(0));
            });
            require(selected[0] && prefs.getBoolean("audio_only", false), "Visible Audio-only action failed");
            result.putString("audio_only_toggle", "PASS: visible parent list toggled preference");
            SystemClock.sleep(350);
            if (oldAi && !oldCloud) {
                openParentMenu();
                runOnMainSync(() -> {
                    android.widget.ListView list = parentDialog().getListView();
                    require(list != null && list.isShown() && list.getChildCount() > 1,
                            "AI disable action not visible");
                    int index = -1;
                    for (int i = 0; i < list.getAdapter().getCount(); i++) {
                        if (list.getAdapter().getItem(i).toString().startsWith("Tắt AI Kids")) {
                            require(index < 0, "Ambiguous AI disable action");
                            index = i;
                        }
                    }
                    require(index >= 0, "AI disable action absent; refusing another selection");
                    View action = list.getChildAt(index - list.getFirstVisiblePosition());
                    require(action != null && action.isShown(), "AI disable action outside visible list");
                    require(list.performItemClick(action, index, list.getAdapter().getItemId(index)),
                            "AI disable action failed");
                });
                require(!ai.getBoolean("ai_quizzes", true)
                        && !ai.getBoolean("child_text_cloud_explicit", false), "AI disable not immediate");
                result.putString("ai_disable", "PASS: visible action immediately disables AI/cloud sharing");
            } else result.putString("ai_disable", "NOT_VERIFIED: existing AI/consent settings preserved");
        } finally {
            android.content.SharedPreferences.Editor edit = prefs.edit();
            if (hadAudioOnly) edit.putBoolean("audio_only", oldAudioOnly);
            else edit.remove("audio_only");
            require(edit.commit(), "Cannot restore Audio-only setting");
            restorePreferences(ai, aiBefore);
        }
    }

    private android.app.AlertDialog parentDialog() {
        try {
            Field dialog = child.getClass().getDeclaredField("parentDialog");
            dialog.setAccessible(true);
            return (android.app.AlertDialog) dialog.get(child);
        } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
    }

    private void openParentMenu() {
        runOnMainSync(() -> child.dispatchKeyEvent(new android.view.KeyEvent(
                android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_F10)));
        SystemClock.sleep(350);
        runOnMainSync(() -> require(parentDialog() != null && parentDialog().isShowing(), "Parent menu missing"));
    }

    private double glDistance() throws Exception {
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
        double[] value = {0};
        com.badlogic.gdx.Gdx.app.postRunnable(() -> {
            try { value[0] = distance(journey(child)); }
            catch (Throwable failure) { error.set(failure); }
            finally { done.countDown(); }
        });
        require(done.await(5, java.util.concurrent.TimeUnit.SECONDS), "GL distance read timed out");
        require(error.get() == null, "GL distance read failed: " + error.get());
        return value[0];
    }

    private void validateAudioPlayback(Bundle result) throws Exception {
        parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        android.media.AudioManager audio = (android.media.AudioManager) getTargetContext()
                .getSystemService(android.content.Context.AUDIO_SERVICE);
        int originalVolume = audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
        boolean originallyMuted = audio.isStreamMute(android.media.AudioManager.STREAM_MUSIC);
        result.putInt("original_media_volume", originalVolume);
        result.putInt("media_max_volume", audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC));
        java.util.ArrayList<String> outputs = new java.util.ArrayList<>();
        for (android.media.AudioDeviceInfo output : audio.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS))
            outputs.add(String.valueOf(output.getType()));
        result.putString("available_output_types", String.join(",", outputs));
        result.putBoolean("on_device_asr_service_available",
                android.speech.SpeechRecognizer.isOnDeviceRecognitionAvailable(getTargetContext()));
        com.khuongnd.dexkids.OfflineVietnameseNarrator[] narrator = {null};
        try {
            if (originalVolume == 0) {
                audio.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.ADJUST_UNMUTE, 0);
                audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        Math.max(1, audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC) / 4), 0);
            }
            result.putInt("playback_media_volume", audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC));
            android.media.ToneGenerator tone = new android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_MUSIC, 25);
            try {
                // PROP_BEEP has an intrinsic short duration, even with a longer
                // duration argument. Use a continuous tone for the active-media sample.
                require(tone.startTone(android.media.ToneGenerator.TONE_SUP_DIAL, 1000),
                        "Local test tone did not start");
                SystemClock.sleep(200);
                result.putBoolean("media_active_during_tone", audio.isMusicActive());
                require(audio.isMusicActive(), "Media playback not active during local tone");
                SystemClock.sleep(1000);
                result.putString("local_tone", "PASS: generated tone and active media playback");
            } finally { tone.release(); }
            runOnMainSync(() -> narrator[0] = new com.khuongnd.dexkids.OfflineVietnameseNarrator(getTargetContext()));
            Field ready = narrator[0].getClass().getDeclaredField("ready");
            ready.setAccessible(true);
            long until = SystemClock.elapsedRealtime() + 15000;
            while (!ready.getBoolean(narrator[0]) && SystemClock.elapsedRealtime() < until)
                SystemClock.sleep(100);
            require(ready.getBoolean(narrator[0]), "Local TTS engine did not initialize");
            Field engineField = narrator[0].getClass().getDeclaredField("engine");
            engineField.setAccessible(true);
            android.speech.tts.TextToSpeech engine = (android.speech.tts.TextToSpeech) engineField.get(narrator[0]);
            result.putString("tts_engine", engine.getDefaultEngine());
            int vietnameseOffline = 0;
            if (engine.getVoices() != null) for (android.speech.tts.Voice voice : engine.getVoices())
                if ("vi".equals(voice.getLocale().getLanguage()) && !voice.isNetworkConnectionRequired())
                    vietnameseOffline++;
            result.putInt("vietnamese_offline_voices", vietnameseOffline);
            require(vietnameseOffline > 0, "No offline Vietnamese TTS voice installed; no network fallback");
            java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.CountDownLatch finished = new java.util.concurrent.CountDownLatch(1);
            boolean[] queued = {false};
            runOnMainSync(() -> {
                narrator[0].setParentApproved(true);
                queued[0] = narrator[0].speakReviewed(
                        "Xin chào! Đây là kiểm tra âm thanh tiếng Việt của DeX Kids Adventure. Chúc bạn một ngày vui vẻ!",
                        () -> { started.countDown(); return kotlin.Unit.INSTANCE; },
                        () -> { finished.countDown(); return kotlin.Unit.INSTANCE; },
                        () -> kotlin.Unit.INSTANCE);
            });
            require(queued[0], "Production offline narrator rejected audio focus or voice");
            require(started.await(10, java.util.concurrent.TimeUnit.SECONDS), "TTS playback never started");
            require(finished.await(30, java.util.concurrent.TimeUnit.SECONDS), "TTS playback did not finish");
            result.putString("audio_playback", "Production offline Vietnamese narrator queued, started and finished");
            // Production onFinished also handles cancellation/errors. Separately require
            // a native onDone for the same selected offline voice, without recording audio.
            require(engine.getVoice() != null && "vi".equals(engine.getVoice().getLocale().getLanguage())
                    && !engine.getVoice().isNetworkConnectionRequired(), "Selected TTS voice is not offline Vietnamese");
            java.util.concurrent.CountDownLatch nativeDone = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicBoolean completed = new java.util.concurrent.atomic.AtomicBoolean(false);
            engine.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
                @Override public void onStart(String id) {}
                @Override public void onDone(String id) {
                    if ("fold3_qa_native_done".equals(id)) { completed.set(true); nativeDone.countDown(); }
                }
                @Override public void onError(String id) {
                    if ("fold3_qa_native_done".equals(id)) nativeDone.countDown();
                }
            });
            android.media.AudioFocusRequest focus = new android.media.AudioFocusRequest.Builder(
                    android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(new android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    .setOnAudioFocusChangeListener(change -> {}).build();
            try {
                require(audio.requestAudioFocus(focus) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED,
                        "Native TTS verification audio focus denied");
                require(engine.speak("Kiểm tra âm thanh đã hoàn tất. Cảm ơn bạn!",
                        android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "fold3_qa_native_done")
                        == android.speech.tts.TextToSpeech.SUCCESS, "Native TTS queue failed");
                require(nativeDone.await(20, java.util.concurrent.TimeUnit.SECONDS) && completed.get(),
                        "Offline Vietnamese native playback failed or timed out");
                result.putBoolean("native_tts_on_done", true);
            } finally { audio.abandonAudioFocusRequest(focus); }
            result.putString("audibility_quality", "NOT_VERIFIED: no human listening or recording");
        } finally {
            runOnMainSync(() -> { if (narrator[0] != null) narrator[0].shutdown(); });
            if (originalVolume == 0) {
                audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, originalVolume, 0);
                if (originallyMuted) audio.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.ADJUST_MUTE, 0);
            }
            result.putInt("restored_media_volume", audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC));
        }
    }

    private void validatePhysicalSoak(Bundle result) throws Exception {
        require(targetDisplay > 0, "Physical soak requires explicit external display");
        // A one-hour soak is a test duration, not a production viewing deadline.
        try {
            monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
            parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runOnMainSync(() -> {
                Button preview = findPreview(parent.getWindow().getDecorView());
                require(preview != null && preview.performClick(), "DEMO launch missing");
            });
            child = waitForMonitorWithTimeout(monitor, 10000);
            require(child != null && child.getDisplay().getDisplayId() == targetDisplay,
                    "Soak child must stay on external display");
            SystemClock.sleep(3000);
            checkDemo(child);
            result.putInt("gl_width", com.badlogic.gdx.Gdx.graphics.getWidth());
            result.putInt("gl_height", com.badlogic.gdx.Gdx.graphics.getHeight());
            require(com.badlogic.gdx.Gdx.graphics.getWidth() >= 1900
                    && com.badlogic.gdx.Gdx.graphics.getHeight() >= 900,
                    "Physical soak requires maximized FullHD DeX window");
            require(!savedState(child).containsKey("kids.session.deadline.elapsed"),
                    "Unexpected session deadline on unlimited journey");
            long soakEnd = SystemClock.elapsedRealtime() + 3_600_000L;
            int checks = 0;
            long previousFrames = -1;
            while (SystemClock.elapsedRealtime() < soakEnd) {
                checkDemo(child);
                require(child.getDisplay().getDisplayId() == targetDisplay, "Child moved off DeX");
                java.util.concurrent.CountDownLatch sampled = new java.util.concurrent.CountDownLatch(1);
                long[] frames = {-1};
                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                    try {
                        com.khuongnd.dexkids.game.KidsGame game = (com.khuongnd.dexkids.game.KidsGame)
                                ((com.khuongnd.dexkids.KidsActivity) child).getApplicationListener();
                        Field metricField = game.getScreen().getClass().getDeclaredField("runMetrics");
                        metricField.setAccessible(true);
                        frames[0] = ((com.khuongnd.dexkids.game.RenderRunMetrics)
                                metricField.get(game.getScreen())).frames();
                    } catch (ReflectiveOperationException failure) {
                        android.util.Log.e("Fold3QA", "Cannot sample GL frame counter");
                    } finally { sampled.countDown(); }
                });
                require(sampled.await(5, java.util.concurrent.TimeUnit.SECONDS), "GL thread stalled");
                require(frames[0] > previousFrames, "GL frames stopped advancing");
                previousFrames = frames[0];
                checks++;
                android.util.Log.i("Fold3QA", "soak_check=" + checks);
                SystemClock.sleep(Math.min(30000, Math.max(1,
                        soakEnd - SystemClock.elapsedRealtime())));
            }
            require(!child.isFinishing() && !child.isDestroyed(),
                    "Unlimited journey ended during one-hour soak");
            com.khuongnd.dexkids.story.ChildEngagementMetrics.Snapshot[] counts = {null};
            runOnMainSync(() -> {
                try { counts[0] = ((com.khuongnd.dexkids.story.ChildEngagementMetrics)field(child, "engagementMetrics")).snapshot(); }
                catch (Exception error) { throw new AssertionError(error); }
            });
            result.putString("engagement_metrics", counts[0].toString());
            require(counts[0].starts() >= 80, "Too few real-time entertainment beats in one hour");
            require(counts[0].voiceUnavailable() == 0, "Silent voice fallback during endurance");
            result.putInt("external_display_checks", checks);
            result.putLong("last_sampled_gl_frames", previousFrames);
            result.putString("assertions", "60_min_demo,external_display,no_live_gps,focused_window,advancing_gl_frames,no_auto_expiry");
        } finally {
            final Activity finishedChild = child;
            if (finishedChild != null && !finishedChild.isFinishing())
                runOnMainSync(finishedChild::finish); // Test cleanup only.
        }
    }

    private void validateGpxJourney(Bundle result) throws Exception {
        monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
        parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            try {
                java.lang.reflect.Method tools = parent.getClass().getDeclaredMethod("showAdvancedTools");
                tools.setAccessible(true);
                tools.invoke(parent);
            } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
        });
        SystemClock.sleep(500);
        android.view.accessibility.AccessibilityNodeInfo root = getUiAutomation(
                android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).getRootInActiveWindow();
        require(root != null && PACKAGE.contentEquals(root.getPackageName()), "Advanced tools dialog missing");
        String label = "gpx_sample".equals(mode) ? "Xem mẫu HCMC (mô phỏng)" : "GPX REPLAY · chọn file";
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> tools = root.findAccessibilityNodeInfosByText(label);
        require(tools.size() == 1, "Advanced GPX action absent or ambiguous");
        clicked[0] = tools.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
        require(clicked[0], "GPX parent action missing");
        // Only select the exact host-owned synthetic fixture, on the explicit DeX display.
        if ("gpx_picker".equals(mode)) selectOwnedGpxFixture();
        child = waitForMonitorWithTimeout(monitor, 60000);
        require(child != null, "GPX child did not launch");
        Object holder = journey(child);
        Object replay = null;
        long until = SystemClock.elapsedRealtime() + 10000;
        while (replay == null && SystemClock.elapsedRealtime() < until) {
            replay = holder.getClass().getMethod("replay").invoke(holder);
            SystemClock.sleep(100);
        }
        require(replay != null, "GPX load failed; no parsed replay installed");
        SystemClock.sleep(1200);
        double time = ((Number) replay.getClass().getMethod("replayTimeSeconds").invoke(replay)).doubleValue();
        require(time > 0, "GPX timeline not advancing");
        checkDemo(child);
        File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-" + mode);
        require(directory.isDirectory() || directory.mkdirs(), "GPX screenshot directory unavailable");
        result.putString("game_png", screenshot(directory, "loaded.png"));
        Object timeline = replay;
        clickChildButton("Pause");
        SystemClock.sleep(650);
        require((Boolean) timeline.getClass().getMethod("isPaused").invoke(timeline), "Pause failed");
        double frozen = ((Number) timeline.getClass().getMethod("replayTimeSeconds").invoke(timeline)).doubleValue();
        SystemClock.sleep(500);
        require(((Number) timeline.getClass().getMethod("replayTimeSeconds").invoke(timeline)).doubleValue() == frozen,
                "Paused timeline advanced");
        clickChildButton("Speed 1.0×");
        SystemClock.sleep(650);
        require(((Number) timeline.getClass().getMethod("playbackRate").invoke(timeline)).doubleValue() == 2,
                "Playback speed action failed");
        require(!savedState(child).containsKey("kids.session.deadline.elapsed"),
                "Legacy GPX session timeout still persisted");
        Activity old = child;
        removeMonitor(monitor);
        monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
        runOnMainSync(old::recreate);
        child = waitForMonitorWithTimeout(monitor, 10000);
        require(child != null && child != old, "GPX recreation failed");
        SystemClock.sleep(1000);
        require(journey(child) == holder, "GPX holder reset on recreation");
        require(holder.getClass().getMethod("replay").invoke(holder) == replay,
                "Parsed timeline replaced on recreation");
        require(!savedState(child).containsKey("kids.session.deadline.elapsed"),
                "GPX Activity recreation must not add a timeout");
        require(((Number) replay.getClass().getMethod("replayTimeSeconds").invoke(replay)).doubleValue() >= time,
                "GPX playback moved backwards on recreation");
        require((Boolean) timeline.getClass().getMethod("isPaused").invoke(timeline), "Paused state lost on recreation");
        require(((Number) timeline.getClass().getMethod("playbackRate").invoke(timeline)).doubleValue() == 2,
                "Playback speed lost on recreation");
        clickChildButton("Resume");
        SystemClock.sleep(900);
        require(((Number) timeline.getClass().getMethod("replayTimeSeconds").invoke(timeline)).doubleValue() > frozen,
                "Resume did not advance timeline");
        clickChildButton("Restart");
        SystemClock.sleep(200);
        require(((Number) timeline.getClass().getMethod("replayTimeSeconds").invoke(timeline)).doubleValue() < frozen,
                "Restart did not reset timeline");
        result.putBoolean("recreation_preserved", true);
        result.putBoolean("pause_resume_restart_rate", true);
        result.putDouble("replay_seconds", time);
        result.putString("assertions", "parent_action,parsed_replay_installed,timeline_advances,no_live_gps");
    }

    private void clickChildButton(String title) {
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            Button button = findButton(child.getWindow().getDecorView(), title);
            clicked[0] = button != null && button.performClick();
        });
        require(clicked[0], "Child control missing: " + title);
    }


    private void selectOwnedGpxFixture() {
        var automation = getUiAutomation();
        var info = automation.getServiceInfo();
        info.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        automation.setServiceInfo(info);
        long until = SystemClock.elapsedRealtime() + 20000;
        while (SystemClock.elapsedRealtime() < until) {
            var windows = automation.getWindowsOnAllDisplays().get(targetDisplay);
            if (windows != null) for (var window : windows) {
                var root = window.getRoot();
                if (root == null || root.getPackageName() == null ||
                    !root.getPackageName().toString().endsWith("documentsui")) continue;
                for (var node : root.findAccessibilityNodeInfosByText("dexkids-qa-T011-20261011.gpx")) {
                    if (!"dexkids-qa-T011-20261011.gpx".equals(String.valueOf(node.getText()))) continue;
                    var click = node;
                    for (int n = 0; n < 3 && click != null; n++, click = click.getParent())
                        if (click.isClickable() && click.performAction(
                            android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)) return;
                }
                for (String label : new String[]{"Downloads", "Tệp tải xuống", "Tải xuống"})
                    for (var node : root.findAccessibilityNodeInfosByText(label))
                        if (label.equals(String.valueOf(node.getText()))) {
                            var click = node;
                            for (int n = 0; n < 3 && click != null; n++, click = click.getParent())
                                if (click.isClickable() && click.performAction(
                                    android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)) break;
                        }
            }
            SystemClock.sleep(200);
        }
        throw new AssertionError("Owned GPX fixture not selectable on external DocumentsUI");
    }

    /** Runtime test: dashboard, child and parent menu share the only emulator display. */
    private void validateSingleDisplay(Bundle result, boolean viaMouse) throws Exception {
        monitor = addMonitor(PACKAGE + ".KidsActivity", null, false);
        parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        int parentDisplay = parent.getDisplay().getDisplayId();
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            Button start = findPreview(parent.getWindow().getDecorView());
            clicked[0] = start != null && start.performClick();
        });
        require(clicked[0], "Same-display start button not clickable");
        child = waitForMonitorWithTimeout(monitor, 10000);
        require(child != null, "KidsActivity missing after same-display start");
        require(child.getDisplay().getDisplayId() == parentDisplay,
                "Game launched on a different display than the parent dashboard");
        SystemClock.sleep(700);
        if (viaMouse) {
            boolean[] clickedMenu = new boolean[1];
            runOnMainSync(() -> {
                Button button = findButton(child.getWindow().getDecorView(), "Parents · menu");
                clickedMenu[0] = button != null && button.performClick();
            });
            require(clickedMenu[0], "One-click parent menu unavailable");
        } else {
            runOnMainSync(() -> child.dispatchKeyEvent(new android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_F10)));
        }
        SystemClock.sleep(450);
        Object listener = child.getClass().getMethod("getApplicationListener").invoke(child);
        java.lang.reflect.Field field = listener.getClass().getDeclaredField("parentMenuOpen");
        field.setAccessible(true);
        require(((java.util.concurrent.atomic.AtomicBoolean) field.get(listener)).get(),
                "F10 did not pause the journey or open parent menu");
        // Android 15 accessibility-root text discovery is asynchronous and
        // occasionally returns an older surface, even while the real dialog
        // is visible. Check the SAME Activity's actual visible native modal.
        boolean[] modalVisible = new boolean[1];
        for (int attempt = 0; attempt < 25 && !modalVisible[0]; attempt++) {
            runOnMainSync(() -> {
                try {
                    java.lang.reflect.Field dialogField = child.getClass().getDeclaredField("parentDialog");
                    dialogField.setAccessible(true);
                    android.app.AlertDialog dialog = (android.app.AlertDialog) dialogField.get(child);
                    modalVisible[0] = dialog != null && dialog.isShowing();
                } catch (ReflectiveOperationException failure) {
                    throw new AssertionError("Parent dialog reflection failed", failure);
                }
            });
            if (!modalVisible[0]) SystemClock.sleep(120);
        }
        require(modalVisible[0], "Parent modal not visible in same-display game");
        File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-single-display");
        require(directory.isDirectory() || directory.mkdirs(), "No screenshot directory");
        result.putString("menu_png", screenshot(directory, "parent-menu.png"));
        // Android 15 accessibility search can return zero/multiple text nodes for
        // AlertDialog material-styled buttons. Interact with the actual dialog's
        // positive Button instead of guessing an accessibility text node count.
        boolean[] pressedEnd = new boolean[1];
        runOnMainSync(() -> {
            try {
                java.lang.reflect.Field dialogField = child.getClass().getDeclaredField("parentDialog");
                dialogField.setAccessible(true);
                android.app.AlertDialog dialog = (android.app.AlertDialog) dialogField.get(child);
                if (dialog != null && dialog.isShowing()) {
                    android.widget.Button end = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                    pressedEnd[0] = end != null && "End adventure".contentEquals(end.getText())
                            && end.isEnabled() && end.performClick();
                }
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError("Parent dialog reflection failed", failure);
            }
        });
        require(pressedEnd[0], "Visible dialog positive End adventure button not clickable");
        SystemClock.sleep(450);
        require(child.isFinishing() || child.isDestroyed(), "Child did not close after parent action");
        result.putInt("display_id", parentDisplay);
        result.putString("assertions",
                "same_display_dashboard_game," + (viaMouse ? "one_click_mouse_menu" : "keyboard_f10_parent_menu") +
                ",journey_paused,stop_returns_to_dashboard");
    }

    private void validatePermissionRefusal(Bundle result) throws Exception {
        android.app.UiAutomation automation = getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        android.accessibilityservice.AccessibilityServiceInfo info = automation.getServiceInfo();
        info.flags |= android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        automation.setServiceInfo(info);
        require(getTargetContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED, "Existing fine grant; test must not revoke/regrant it");
        parent = startActivitySync(new Intent().setClassName(PACKAGE, PACKAGE + ".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        boolean[] clicked = new boolean[1];
        runOnMainSync(() -> {
            Button button = findButton(parent.getWindow().getDecorView(),
                    "BẮT ĐẦU · GPS thật và Capybara trò chuyện");
            clicked[0] = button != null && button.performClick();
        });
        require(clicked[0], "Actual parent permission button not clicked");
        SystemClock.sleep(2000);
        android.view.accessibility.AccessibilityNodeInfo root = automation.getRootInActiveWindow();
        require(root != null, "Permission test active window unavailable");
        result.putString("root_package", String.valueOf(root.getPackageName()));
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> deny = permissionDenyNodes(root);
        for (int attempt = 0; deny.isEmpty() && attempt < 4
                && String.valueOf(root.getPackageName()).contains("permissioncontroller"); attempt++) {
            if (!scrollPermissionDialog(root)) break;
            SystemClock.sleep(350);
            root = automation.getRootInActiveWindow();
            require(root != null && String.valueOf(root.getPackageName()).contains("permissioncontroller"),
                    "Permission window disappeared during scroll");
            deny = permissionDenyNodes(root);
        }
        String denialIdentity = "resource_id";
        if (deny.isEmpty()) {
            denialIdentity = "exact_refusal_text";
            deny = root.findAccessibilityNodeInfosByText("Don’t allow");
            if (deny.isEmpty()) deny = root.findAccessibilityNodeInfosByText("Don't allow");
            deny.removeIf(node -> node.getText() == null ||
                    !"don't allow".equals(node.getText().toString().replace('’', '\'').toLowerCase(java.util.Locale.ROOT)));
        }
        boolean permissionWindow = String.valueOf(root.getPackageName()).contains("permissioncontroller");
        require(!permissionWindow || !deny.isEmpty(), "Visible permission window but deny control inaccessible");
        boolean dialog = !deny.isEmpty();
        if (dialog) {
            require(permissionWindow, "Deny control outside permission controller");
            require(deny.size() == 1, "Ambiguous denial control");
            require(!root.findAccessibilityNodeInfosByText("DeX Kids Adventure").isEmpty(), "Unrelated permission dialog");
            require(deny.get(0).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK),
                    "Resource-ID denial ACTION_CLICK failed");
        }
        SystemClock.sleep(2000);
        require(getTargetContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED, "Unexpected fine grant");
        require(getTargetContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED, "Unexpected coarse grant");
        boolean[] explained = new boolean[1];
        runOnMainSync(() -> explained[0] = containsText(parent.getWindow().getDecorView(),
                "Cần quyền vị trí chính xác để nhận biết địa danh. Có thể xem DEMO."));
        File directory = new File(getTargetContext().getExternalFilesDir(null), "qa-permission");
        require(directory.isDirectory() || directory.mkdirs(), "Permission evidence directory unavailable");
        result.putString("refusal_png", screenshot(directory, "permission-refused.png"));
        result.putBoolean("dialog_denied_by_node_action", dialog);
        result.putString("denial_identity", dialog ? denialIdentity : "already_refused_no_dialog");
        result.putBoolean("fine_denied", true);
        result.putBoolean("coarse_denied", true);
        require(explained[0], "Denied permission explanation missing after actual callback/resume");
        result.putString("assertions", "actual_parent_button,deny_only_node_ACTION_CLICK,fine_and_coarse_denied,parent_explanation_preserved");
    }

    private static java.util.List<android.view.accessibility.AccessibilityNodeInfo> permissionDenyNodes(
            android.view.accessibility.AccessibilityNodeInfo root) {
        for (String prefix : new String[] {String.valueOf(root.getPackageName()), "com.android.permissioncontroller"}) {
            for (String id : new String[] {"permission_deny_button", "permission_deny_and_dont_ask_again_button"}) {
                java.util.List<android.view.accessibility.AccessibilityNodeInfo> nodes =
                        root.findAccessibilityNodeInfosByViewId(prefix + ":id/" + id);
                if (!nodes.isEmpty()) return nodes;
            }
        }
        return new java.util.ArrayList<>();
    }

    private static boolean scrollPermissionDialog(android.view.accessibility.AccessibilityNodeInfo node) {
        if (node.isScrollable() && node.performAction(
                android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) return true;
        for (int i = 0; i < node.getChildCount(); i++) {
            android.view.accessibility.AccessibilityNodeInfo child = node.getChild(i);
            if (child != null && scrollPermissionDialog(child)) return true;
        }
        return false;
    }

    private static boolean containsText(View view, String expected) {
        if (view instanceof android.widget.TextView && expected.contentEquals(((android.widget.TextView) view).getText())) return true;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) if (containsText(group.getChildAt(i), expected)) return true;
        }
        return false;
    }

    private Object journey(Activity activity) throws Exception {
        Object listener = activity.getClass().getMethod("getApplicationListener").invoke(activity);
        Field field = listener.getClass().getDeclaredField("journey");
        field.setAccessible(true);
        return field.get(listener);
    }
    private double distance(Object feed) throws Exception {
        return ((Number) feed.getClass().getMethod("distanceMeters").invoke(feed)).doubleValue();
    }

    private Bundle savedState(Activity activity) {
        Bundle state = new Bundle();
        runOnMainSync(() -> callActivityOnSaveInstanceState(activity, state));
        return state;
    }

    private void checkDemo(Activity activity) throws Exception {
        require(!activity.isFinishing(), "Child Activity unexpectedly finishing");
        require(!activity.getIntent().getBooleanExtra(PACKAGE + ".extra.LIVE_GPS", false), "Live GPS enabled");
        for (String name : new String[] {"gps", "liveFeed"}) {
            Field field = activity.getClass().getDeclaredField(name);
            field.setAccessible(true);
            require(field.get(activity) == null, "Unexpected active " + name);
        }
        awaitChildReady(activity);
    }

    private void awaitChildReady(Activity activity) {
        boolean[] focused = new boolean[1];
        long until = SystemClock.elapsedRealtime() + 5000;
        do {
            runOnMainSync(() -> focused[0] = !activity.isFinishing() &&
                activity.getWindow().getDecorView().isShown() &&
                activity.getWindow().getDecorView().hasWindowFocus());
            if (focused[0]) return;
            SystemClock.sleep(100);
        } while (SystemClock.elapsedRealtime() < until);
        throw new AssertionError("Child window hidden or unfocused after bounded readiness wait; check DeX focus/permission windows");
    }

    private String screenshot(File directory, String name) throws Exception {
        // UiAutomation.takeScreenshot() captures display 0, not the DeX target.
        // Host must capture the external SurfaceFlinger display explicitly.
        if (targetDisplay > 0) return "NOT_CAPTURED: external screencap required";
        Bitmap bitmap = getUiAutomation(android.app.UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).takeScreenshot();
        require(bitmap != null, "Actual device screenshot unavailable");
        File output = new File(directory, name);
        try (FileOutputStream stream = new FileOutputStream(output)) {
            require(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream), "Screenshot encoding failed");
        } finally { bitmap.recycle(); }
        require(output.length() > 0, "Empty screenshot");
        return output.getAbsolutePath();
    }

    private static Button findPreview(View view) {
        return findButton(view, "XEM THỬ · hoạt hình DEMO");
    }

    private static Button findButton(View view, String text) {
        if (view instanceof Button && text.contentEquals(((Button) view).getText())) return (Button) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                Button found = findButton(group.getChildAt(index), text);
                if (found != null) return found;
            }
        }
        return null;
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
