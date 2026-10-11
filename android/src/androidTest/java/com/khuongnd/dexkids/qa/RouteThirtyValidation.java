package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.FrameLayout;
import com.badlogic.gdx.Gdx;
import com.khuongnd.dexkids.game.*;
import com.khuongnd.dexkids.geo.*;
import com.khuongnd.dexkids.journey.JourneyFeed;
import com.khuongnd.dexkids.story.*;
import org.json.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Test APK only. Synthetic downstream POI fixtures; no Android GPS injection or production guard changes. */
public final class RouteThirtyValidation {
    private static final String PKG="com.khuongnd.dexkids", TAG="Route30QA";
    private static Object field(Object o,String n) throws Exception {
        Field f=o.getClass().getDeclaredField(n); f.setAccessible(true); return f.get(o);
    }
    private static void set(Object o,String n,Object v) throws Exception {
        Field f=o.getClass().getDeclaredField(n); f.setAccessible(true); f.set(o,v);
    }
    private interface Checked { void run() throws Exception; }
    private static void gl(Checked work) throws Exception {
        CountDownLatch done=new CountDownLatch(1); Throwable[] error={null};
        Gdx.app.postRunnable(()->{try{work.run();}catch(Throwable t){error[0]=t;}finally{done.countDown();}});
        require(done.await(5,TimeUnit.SECONDS),"GL stalled");
        if(error[0]!=null) throw new AssertionError(error[0]);
    }
    private static void require(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    private static String read(InputStream in) throws IOException {
        try(in;ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] b=new byte[4096]; for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);
            return out.toString("UTF-8");
        }
    }
    private static void restore(SharedPreferences p,Map<String,?> before) {
        SharedPreferences.Editor e=p.edit().clear();
        for(var x:before.entrySet()) {
            Object v=x.getValue();String k=x.getKey();
            if(v instanceof Boolean)e.putBoolean(k,(Boolean)v);
            else if(v instanceof Integer)e.putInt(k,(Integer)v);
            else if(v instanceof Long)e.putLong(k,(Long)v);
            else if(v instanceof Float)e.putFloat(k,(Float)v);
            else if(v instanceof String)e.putString(k,(String)v);
            else if(v instanceof Set)e.putStringSet(k,(Set<String>)v);
            else throw new AssertionError("Unsupported preference");
        }
        require(e.commit(),"Restore preferences failed");
    }
    private static final class FixtureFeed implements JourneyFeed {
        double lat,lon,distance,time; boolean active;
        public void update(float dt) { if(dt>0&&active){time+=Math.min(dt,.1f);distance+=Math.min(dt,.1f)*5;} }
        public double distanceMeters(){return distance;}
        public double speedMetersPerSecond(){return active?5:0;}
        public boolean isDemo(){return true;}
        public Optional<JourneyPosition> position(long now) {
            if(!active)return Optional.empty();
            // Approach 40m -> centre in 8s, then a small circle around the fixture.
            double north=time<8?40-time*5:12*Math.sin((time-8)*5/12);
            double east=time<8?0:12*Math.cos((time-8)*5/12);
            return Optional.of(new JourneyPosition(lat+north/111320,
                lon+east/(111320*Math.cos(Math.toRadians(lat))),5,5,now/1000*1000,true));
        }
        void select(JSONObject p) throws JSONException {lat=p.getDouble("lat");lon=p.getDouble("lon");time=0;active=true;}
    }
    public static void run(LifecycleValidationRunner runner,Bundle result,boolean smoke) throws Exception {
        require(runner.targetDisplay()>0,"Explicit DeX display required");
        SharedPreferences prefs=runner.getTargetContext().getSharedPreferences("parent_settings",0);
        SharedPreferences ai=runner.getTargetContext().getSharedPreferences("kids_ai_settings_v1",0);
        Map<String,?> old=prefs.getAll(),oldAi=ai.getAll();
        Activity parent=null,child=null;
        var monitor=runner.addMonitor(PKG+".KidsActivity",null,false);
        File directory=new File(runner.getTargetContext().getExternalFilesDir(null),"qa-route30");
        require(directory.isDirectory()||directory.mkdirs(),"Evidence directory unavailable");
        JSONArray records=new JSONArray();
        try {
            require(prefs.edit().putInt("session_minutes",30).putString("audience_mode","BOTH")
                .putBoolean("audio_only",false).putBoolean("offline_tts",true)
                .putBoolean("audio_effects",true).putBoolean("child_mic_optin",false).commit(),"Fixture prefs");
            require(ai.edit().putBoolean("ai_quizzes",false).putBoolean("child_text_cloud_explicit",false).commit(),"AI opt out");
            // Assert exact production preference key below before any POI can start a request.
            require(!new com.khuongnd.dexkids.ai.KidsAiSettings(runner.getTargetContext()).getEnabled(),"AI must be disabled");
            parent=runner.startActivitySync(new Intent().setClassName(PKG,PKG+".ParentActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            final Activity dashboard=parent;
            runner.runOnMainSync(()->dashboard.startActivity(new Intent(dashboard,com.khuongnd.dexkids.KidsActivity.class)));
            child=runner.waitForMonitorWithTimeout(monitor,10000);
            require(child!=null,"Child missing"); final Activity activity=child;
            long ready=SystemClock.elapsedRealtime()+10000;
            KidsGame game=(KidsGame)((com.khuongnd.dexkids.KidsActivity)child).getApplicationListener();
            while(game.getScreen()==null&&SystemClock.elapsedRealtime()<ready)SystemClock.sleep(100);
            require(game.getScreen() instanceof AdventureScreen,"Screen missing");
            AdventureScreen screen=(AdventureScreen)game.getScreen();
            FixtureFeed feed=new FixtureFeed();
            gl(()->{set(screen,"journey",feed);set(screen,"reviewedPoiData",true);set(screen,"hcmSamplePreview",true);});
            runner.runOnMainSync(()->{
                TextView label=new TextView(activity);label.setText("TEST 30 PHÚT · TỌA ĐỘ MÔ PHỎNG · KHÔNG PHẢI GPS THẬT");
                label.setTextColor(android.graphics.Color.WHITE);label.setBackgroundColor(0xCC713800);label.setTextSize(16);
                FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-2,-2,android.view.Gravity.RIGHT|android.view.Gravity.TOP);
                activity.addContentView(label,lp);
            });
            Bundle state=new Bundle();runner.runOnMainSync(()->runner.callActivityOnSaveInstanceState(activity,state));
            long deadline=state.getLong("kids.session.deadline.elapsed");long start=deadline-1800000;
            require(deadline>SystemClock.elapsedRealtime()+1780000,"30 minute deadline missing");
            require(Gdx.graphics.getWidth()>=1900&&Gdx.graphics.getHeight()>=900,"Maximized FullHD required");
            android.util.Log.i(TAG,"session_start elapsed="+start+" deadline="+deadline+" simulated=true");
            result.putInt("gl_width",Gdx.graphics.getWidth());result.putInt("gl_height",Gdx.graphics.getHeight());
            JSONArray points=new JSONObject(read(runner.getContext().getAssets().open("route-30min.json"))).getJSONArray("points");
            PoiDialogueCatalog dialogues;
            try(InputStream in=runner.getTargetContext().getAssets().open("poi/live-dialogue.tsv")){dialogues=PoiDialogueCatalog.parse(in);}
            int count=smoke?1:points.length(); long previousFrames=-1;
            for(int i=0;i<count;i++) {
                JSONObject point=points.getJSONObject(i);String id=point.getString("id"),name=point.getString("name");
                long slot=start+point.getLong("slot_start_s")*1000;
                while(SystemClock.elapsedRealtime()<slot) {check(runner,activity);SystemClock.sleep(Math.min(1000,slot-SystemClock.elapsedRealtime()));}
                String catalog=point.getString("catalog").equals("live")?"live-landmarks":"sample-hcm";
                String raw=read(runner.getTargetContext().getAssets().open("poi/"+catalog+".tsv"));
                String[] lines=raw.split("\\R");String row=Arrays.stream(lines).filter(s->s.startsWith(id+"\t")).findFirst().orElseThrow();
                OfflinePoiCatalog one=OfflinePoiCatalog.parse(new ByteArrayInputStream((lines[0]+"\n"+lines[1]+"\n"+row+"\n").getBytes(StandardCharsets.UTF_8)));
                OfflineNarrationCatalog narration;
                try(InputStream in=runner.getTargetContext().getAssets().open("narration/"+catalog+".tsv")){narration=OfflineNarrationCatalog.parse(in);}
                NarrationCue cue=narration.findByPoiId(id).orElseThrow();
                gl(()->{feed.select(point);set(screen,"poiEngine",new OfflinePoiEngine(one));set(screen,"storyCatalog",narration);
                    ((TourGuideDirector)field(screen,"storyDirector")).resetSession();set(screen,"lastPoiQueryAt",0L);});
                android.util.Log.i(TAG,"point_start index="+(i+1)+" id="+id+" simulated=true");
                JSONObject record=new JSONObject().put("id",id).put("name",name).put("simulated",true);
                boolean intro=false,quiz=false,answer=false,chat=false,audioSeen=false;long introAt=0;
                var card=dialogues.find(id); boolean queued=false;
                while(SystemClock.elapsedRealtime()<slot+60000) {
                    check(runner,activity);String[] subtitle={""};
                    boolean[] speech={false};
                    runner.runOnMainSync(()->{try{subtitle[0]=String.valueOf(field(activity,"subtitleText"));
                        speech[0]=(Boolean)field(activity,"speechStarted");
                    }catch(Exception e){throw new AssertionError(e);}});
                    audioSeen|=speech[0];
                    if(!intro&&subtitle[0].contains(cue.textVi())) {
                        intro=true;introAt=SystemClock.elapsedRealtime();record.put("intro_elapsed_s",(introAt-start)/1000.0);
                        require(screen.poiStatusText().contains(name),"Incorrect POI event: "+id);
                        android.util.Log.i(TAG,"point_intro id="+id+" downstream_event=true");
                    }
                    if(intro&&card.isPresent()&&!queued) {
                        // Reuse the production dialogue scheduler after its real synthetic POI intro.
                        // LIVE would normally enqueue it; simulated sample deliberately does not.
                        runner.runOnMainSync(()->{try {
                            Method m=activity.getClass().getDeclaredMethod("queueLiveConversation",PoiDialogueCatalog.Dialogue.class,String.class);
                            m.setAccessible(true);m.invoke(activity,card.get(),cue.textVi());
                        }catch(Exception e){throw new AssertionError(e);}});queued=true;
                    }
                    if(card.isPresent()) {var c=card.get();quiz|=subtitle[0].contains(c.quiz());answer|=subtitle[0].contains(c.answer());chat|=subtitle[0].contains(c.chat());}
                    SystemClock.sleep(250);
                }
                record.put("intro",intro).put("dialogue_available",card.isPresent()).put("quiz",quiz).put("answer",answer).put("chat",chat)
                    .put("app_speech_started_observed",audioSeen);
                final long[] frames={0};final double[] fps={0},p95={0};
                gl(()->{RenderRunMetrics metric=(RenderRunMetrics)field(screen,"runMetrics");frames[0]=metric.frames();fps[0]=metric.averageFps();p95[0]=metric.p95UpperMs();});
                require(frames[0]>previousFrames,"Rendering stopped");previousFrames=frames[0];
                record.put("frames",frames[0]).put("whole_run_fps",fps[0]).put("p95_upper_ms",p95[0]);records.put(record);
                write(directory,records);
                android.util.Log.i(TAG,"point_result "+record);
                // Collect all points even if a stage fails; acceptance fails after full coverage.
            }
            gl(()->feed.active=false);
            long pauseAt=smoke?SystemClock.elapsedRealtime():start+1620000;
            while(SystemClock.elapsedRealtime()<pauseAt){check(runner,activity);SystemClock.sleep(500);}
            gl(()->feed.active=true);
            runner.runOnMainSync(()->activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_F10)));
            require((Boolean)field(activity,"parentMenuOpen"),"F10 did not pause");
            double[] distance={0};gl(()->distance[0]=feed.distanceMeters());SystemClock.sleep(10000);
            gl(()->require(feed.distanceMeters()==distance[0],"Journey moved in parent menu"));
            runner.runOnMainSync(()->((com.khuongnd.dexkids.KidsActivity)activity).resumeFromTrustedController());
            // Dialog dismissal/onDismiss may be posted after the main-thread call returns.
            long resumeUntil=SystemClock.elapsedRealtime()+5000;
            boolean[] resumed={false};
            while(!resumed[0]&&SystemClock.elapsedRealtime()<resumeUntil) {
                runner.runOnMainSync(()->{try{resumed[0]=!(Boolean)field(activity,"parentMenuOpen");}
                    catch(Exception e){throw new AssertionError(e);}});
                if(!resumed[0])SystemClock.sleep(100);
            }
            require(resumed[0],"Resume failed");
            SystemClock.sleep(1000);
            gl(()->require(feed.distanceMeters()>distance[0],"Journey did not resume"));
            result.putBoolean("f10_pause_resume",true);
            if(!smoke) {
                while(SystemClock.elapsedRealtime()<deadline-1000){check(runner,activity);SystemClock.sleep(1000);}
                while(!activity.isFinishing()&&SystemClock.elapsedRealtime()<deadline+15000)SystemClock.sleep(250);
                require(activity.isFinishing(),"30 minute deadline did not expire");
                result.putBoolean("natural_30min_expiry",true);
                result.putLong("session_elapsed_ms",SystemClock.elapsedRealtime()-start);
                require(parent.getDisplay().getDisplayId()==runner.targetDisplay(),"Parent off DeX");
            }
            int introCount=0,dialogueCount=0;
            for(int i=0;i<records.length();i++) {JSONObject r=records.getJSONObject(i);
                if(r.getBoolean("intro"))introCount++;
                if(r.getBoolean("dialogue_available")&&r.getBoolean("quiz")&&r.getBoolean("answer")&&r.getBoolean("chat"))dialogueCount++;
            }
            result.putInt("poi_intros",introCount);result.putInt("complete_dialogues",dialogueCount);result.putInt("points",count);
            result.putString("scope","Physical DeX rendering/audio; synthetic downstream POI feed; AndroidGPS/LiveFixGate/road/ASR NOT_TESTED; HCMC intro-only");
            result.putString("evidence",new File(directory,"points.json").getAbsolutePath());
            require(introCount==count,"Some POI intros missing");require(dialogueCount==(smoke?1:22),"Some dialogue stages missing");
        } finally {
            try { write(directory,records); } finally {
                final Activity c=child,p=parent;
                try { runner.runOnMainSync(()->{if(c!=null&&!c.isFinishing())c.finish();if(p!=null&&!p.isFinishing())p.finish();}); }
                finally { runner.removeMonitor(monitor);try {restore(prefs,old);} finally {restore(ai,oldAi);} }
            }
        }
    }
    private static void check(LifecycleValidationRunner r,Activity a) {
        require(!a.isFinishing()&&!a.isDestroyed(),"Child closed early");
        require(a.getDisplay().getDisplayId()==r.targetDisplay(),"Display changed; phone fallback refused");
        boolean[] visible={false};r.runOnMainSync(()->visible[0]=a.getWindow().getDecorView().isShown()&&a.getWindow().getDecorView().hasWindowFocus());
        require(visible[0],"DeX child hidden/unfocused");
    }
    private static void write(File directory,JSONArray records) throws Exception {
        try(FileOutputStream out=new FileOutputStream(new File(directory,"points.json"))) {out.write(records.toString(2).getBytes(StandardCharsets.UTF_8));}
    }
}
