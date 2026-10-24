package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.khuongnd.dexkids.media.*;
import java.util.HashMap;

/** Explicit owner configuration, intentionally persistent; not an automatic test consent. */
final class YouTubeConfigurationValidation {
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static void run(LifecycleValidationRunner runner,Bundle result) throws Exception {
        runner.requireYouTubeTestDisplay();var base=runner.getTargetContext();
        var settings=new YouTubeEmbedSettings(base);String key=settings.key();require(!key.isBlank(),"Existing encrypted key required");
        var store=new YouTubeStore(base);require(store.getPendingId().isEmpty(),"No unresolved playback");
        var videos=store.videos();var switches=new HashMap<>(base.getSharedPreferences("youtube_handoff_v1",0).getAll());
        Activity setup=runner.startActivitySync(new Intent(base,YouTubeEmbedSetupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        try {
            runner.waitForIdleSync();
            runner.runOnMainSync(()->{
                View root=setup.getWindow().getDecorView();
                EditText contact=field(root,"Liên hệ người quản lý"),country=field(root,"Mã quốc gia");
                CheckBox consent=consent(root);Button save=button(root);
                require(contact!=null && country!=null && consent!=null && save!=null,"Native setup fields");
                contact.setText("");country.setText("VN");
                if(!consent.isChecked())consent.performClick();
                require(consent.isChecked(),"Native explicit owner-authorized consent selected");
                require(save.performClick(),"Native save action");
            });runner.waitForIdleSync();
            var reloaded=new YouTubeEmbedSettings(base);
            require(reloaded.ready() && reloaded.getConsented() && reloaded.getContact().isBlank() && reloaded.getCountry().equals("VN") && reloaded.getSelected(),"Personal native readiness without contact persists");
            require(reloaded.key().equals(key),"Existing key preserved without repasting or disclosure");
            require(store.videos().equals(videos) && switches.equals(base.getSharedPreferences("youtube_handoff_v1",0).getAll()),"Catalog, questions, review flags and interleaving settings unchanged");
            var bitmap=runner.captureQaDisplay();require(bitmap!=null,"Actual configured display capture");
            var dir=new java.io.File(base.getFilesDir(),"qa");dir.mkdirs();
            try(var out=new java.io.FileOutputStream(new java.io.File(dir,"t078-configured.png"))){require(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out),"Capture saved");}
            finally{bitmap.recycle();}
            result.putString("configuration","PASS: native persistent owner-authorized consent, optional contact blank, VN/embedded/key ready after reload; library and switches exact");
        } finally {runner.runOnMainSync(setup::finish);}
    }
    private static EditText field(View view,String prefix){
        if(view instanceof EditText e && e.getHint()!=null && e.getHint().toString().startsWith(prefix))return e;
        if(view instanceof ViewGroup g)for(int i=0;i<g.getChildCount();i++){EditText e=field(g.getChildAt(i),prefix);if(e!=null)return e;}return null;
    }
    private static CheckBox consent(View view){
        if(view instanceof CheckBox b && b.getText().toString().startsWith("Tôi dùng trong gia đình"))return b;
        if(view instanceof ViewGroup g)for(int i=0;i<g.getChildCount();i++){CheckBox b=consent(g.getChildAt(i));if(b!=null)return b;}return null;
    }
    private static Button button(View view){
        if(view instanceof Button b && b.getText().toString().startsWith("Lưu cấu hình"))return b;
        if(view instanceof ViewGroup g)for(int i=0;i<g.getChildCount();i++){Button b=button(g.getChildAt(i));if(b!=null)return b;}return null;
    }
}
