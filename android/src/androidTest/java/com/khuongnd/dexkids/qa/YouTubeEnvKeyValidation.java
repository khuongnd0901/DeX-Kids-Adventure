package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.khuongnd.dexkids.media.*;
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Adult simulator diagnostics: real key/API, but offline-only player callbacks. */
final class YouTubeEnvKeyValidation {
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static void run(LifecycleValidationRunner runner, Bundle result, String url) throws Exception {
        var base=runner.getTargetContext();
        require(android.os.Build.HARDWARE.equals("ranchu") || android.os.Build.HARDWARE.equals("goldfish"),"Emulator-only diagnostic");
        require((base.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)!=0,"Debug APK required");
        var prefs=base.getSharedPreferences("youtube_embed_v1",0);
        boolean priorConsent=prefs.getBoolean("consent_v1",false);
        String priorReference=prefs.getString("notification","");
        var store=new YouTubeStore(base);
        boolean enabled=store.getEnabled(),automatic=store.getAutomatic();
        String packaged;
        try(var input=base.getAssets().open("youtube-test-key.json")){
            packaged=new JSONObject(new String(input.readAllBytes(),StandardCharsets.UTF_8)).getString("key");
        }
        var settings=new YouTubeEmbedSettings(base);
        require(!settings.key().isEmpty() && settings.key().equals(packaged),"Private asset imports exact key");
        require(!prefs.getString("key","").contains(packaged),"Preferences store encrypted key only");
        require(priorConsent==settings.getConsented() && priorReference.equals(settings.getNotificationReference()),"Key injection must not invent consent or Google notification");
        require(enabled==store.getEnabled() && automatic==store.getAutomatic(),"Library enablement preserved");
        require(new YouTubeEmbedSettings(base).key().equals(packaged),"Key reload from Android Keystore storage");
        var namespace="youtube_seed_delete_"+System.nanoTime();
        var isolated=new android.content.ContextWrapper(base){
            @Override public String getPackageName(){return base.getPackageName()+".seedqa";}
            @Override public android.content.SharedPreferences getSharedPreferences(String name,int mode){return base.getSharedPreferences(namespace+name,mode);}
        };
        try{
            var seeded=new YouTubeEmbedSettings(isolated);
            require(seeded.key().equals(packaged) && !seeded.ready(),"Isolated seed imports without consent");
            seeded.delete();
            require(new YouTubeEmbedSettings(isolated).key().isEmpty(),"Delete survives reinitialization without seed reimport");
            result.putString("seed_delete","PASS: real Keystore isolated seed -> delete -> reinitialize stays empty");
        } finally {base.deleteSharedPreferences(namespace+"youtube_embed_v1");}
        result.putString("key_import","PASS: debug asset -> Keystore ciphertext -> exact reload; key not logged");
        result.putBoolean("child_setup_ready",settings.ready());
        result.putBoolean("consent_unchanged",priorConsent==settings.getConsented());
        String id=YouTubeVideo.idFromInput(url);
        // Explicit owner-provided test URL, adult API diagnostic only; never loads a child player.
        var video=new YouTubeVideo(id,"OWNER API TEST ONLY",Audience.BOTH,4,7,300,true,true,"");
        int seconds=YouTubeMetadata.INSTANCE.validate(base,video,settings.key(),settings.getCountry());
        result.putInt("metadata_validated_duration_seconds",seconds);
        result.putString("metadata","PASS: production Android HTTPS client/signature headers, MFK/embedding/region/duration checks; no player");
        Activity setup=runner.startActivitySync(new Intent(base,YouTubeEmbedSetupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        runner.waitForIdleSync();
        try {
            runner.runOnMainSync(()->require(contains(setup.getWindow().getDecorView(),"Đã lưu key"),"Native setup recognizes stored key without revealing it"));
            saveCapture(runner,base,"t071-env-key-setup.png");
            offlinePage(runner,setup,result);
            saveCapture(runner,base,"t071-env-key-offline-webview.png");
        } finally { runner.runOnMainSync(setup::finish); }
        result.putString("live_youtube_playback","NOT RUN: no Google child-directed setup fabricated; offline mock is not live playback");
    }
    private static boolean contains(View view,String text){
        if(view instanceof TextView label && label.getText().toString().contains(text))return true;
        if(view instanceof android.widget.EditText field && field.getHint()!=null && field.getHint().toString().contains(text))return true;
        if(view instanceof ViewGroup group)for(int i=0;i<group.getChildCount();i++)if(contains(group.getChildAt(i),text))return true;
        return false;
    }
    private static void saveCapture(LifecycleValidationRunner runner,android.content.Context base,String name) throws Exception {
        runner.waitForIdleSync();android.os.SystemClock.sleep(300);
        Bitmap bitmap=runner.captureQaDisplay();require(bitmap!=null,"Actual emulator screenshot available");
        File dir=new File(base.getFilesDir(),"qa");dir.mkdirs();
        try(var output=new FileOutputStream(new File(dir,name))){require(bitmap.compress(Bitmap.CompressFormat.PNG,100,output),"Screenshot saved");}
        finally {bitmap.recycle();}
    }
    private static void offlinePage(LifecycleValidationRunner runner,Activity activity,Bundle result) throws Exception {
        String original;
        try(var input=activity.getAssets().open("youtube/player.html")){original=new String(input.readAllBytes(),StandardCharsets.UTF_8);}
        int from=original.indexOf(" const script=document.createElement('script');");
        int to=original.indexOf("document.head.appendChild(script);")+"document.head.appendChild(script);".length();
        require(from>0 && to>from,"Actual production page loader located");
        String mock="""
            window.YT={Player:function(id,options){
              this.playVideo=()=>{options.events.onStateChange({data:1});setTimeout(()=>options.events.onStateChange({data:0}),200);};
              setTimeout(()=>options.events.onReady(),0);
            }};onYouTubeIframeAPIReady();
            """;
        String origin="https://"+activity.getPackageName();
        long generation=9007199254740993L;
        String html=(original.substring(0,from)+mock+original.substring(to))
            .replace("__GENERATION__",Long.toString(generation)).replace("__VIDEO_ID__","abcdefghijk")
            .replace("__ORIGIN__",origin).replace("__NONCE__","offline-test-port-only");
        var session=new EmbeddedYouTubeSession(generation);
        var finished=new CountDownLatch(1);var terminals=new AtomicInteger();
        var failure=new AtomicReference<Throwable>();
        WebView[] web={null};WebMessagePort[] channel={null};
        try {
            runner.runOnMainSync(()->{
                var column=new LinearLayout(activity);column.setOrientation(LinearLayout.VERTICAL);
                var label=new TextView(activity);label.setText("QA OFFLINE MOCK — no live YouTube / no network");column.addView(label);
                var view=new WebView(activity);web[0]=view;
                view.getSettings().setJavaScriptEnabled(true);view.getSettings().setBlockNetworkLoads(true);
                view.getSettings().setAllowFileAccess(false);view.getSettings().setAllowContentAccess(false);
                view.setWebChromeClient(new WebChromeClient(){
                    @Override public boolean onConsoleMessage(ConsoleMessage message){
                        if(message.messageLevel()==ConsoleMessage.MessageLevel.ERROR){failure.set(new AssertionError("Offline page JS error"));finished.countDown();}
                        return true;
                    }
                });
                view.setWebViewClient(new WebViewClient(){
                    @Override public void onPageFinished(WebView current,String url){
                        if(channel[0]!=null)return;
                        var ports=current.createWebMessageChannel();channel[0]=ports[0];
                        ports[0].setWebMessageCallback(new WebMessagePort.WebMessageCallback(){
                            @Override public void onMessage(WebMessagePort port,WebMessage message){
                                try{
                                    var row=new JSONObject(message.getData());
                                    require(session.matchesGeneration(row.getString("generation")),"Generation remains exact across real JS/native port");
                                    require("abcdefghijk".equals(row.getString("videoId")),"Expected mocked video ID");
                                    if(session.event(generation,row.getString("event"))){terminals.incrementAndGet();finished.countDown();}
                                }catch(Throwable error){failure.set(error);finished.countDown();}
                            }
                        },new Handler(Looper.getMainLooper()));
                        current.postWebMessage(new WebMessage("offline-test-port-only",new WebMessagePort[]{ports[1]}),Uri.parse(origin));
                    }
                });
                column.addView(view,new LinearLayout.LayoutParams(-1,0,1));activity.setContentView(column);
                view.loadDataWithBaseURL(origin+"/",html,"text/html","UTF-8",null);
            });
            require(finished.await(15,TimeUnit.SECONDS),"Offline real WebView callback timeout");
            if(failure.get()!=null)throw new AssertionError("Offline WebView failed",failure.get());
            require(terminals.get()==1 && session.state()==EmbeddedYouTubeSession.State.ENDED,"Playing -> ENDED completes once in actual Android WebView");
            result.putString("webview_mock","PASS: actual Android WebView + exact-origin message port + production HTML, >2^53 generation, PLAYING -> ENDED once; network blocked, API mocked");
        } finally {
            runner.runOnMainSync(()->{if(channel[0]!=null)channel[0].close();if(web[0]!=null){((ViewGroup)web[0].getParent()).removeView(web[0]);web[0].destroy();}});
        }
    }
}
