package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import com.khuongnd.dexkids.game.KidsGame;
import com.khuongnd.dexkids.journey.LiveJourneyFeed;
import static com.khuongnd.dexkids.qa.ArtVisualValidation.*;

/** Android GPS delivery preflight: console OK alone is not evidence of accepted motion. */
final class StoryGpsValidation {
    static void run(LifecycleValidationRunner r,Bundle result)throws Exception {
        require(r.fullHdQaDisplay() && r.targetDisplay()==0,"Explicit FullHD emulator required");
        var context=r.getTargetContext();var ai=context.getSharedPreferences("kids_ai_settings_v1",0);var old=ai.getAll();Activity activity=null;
        try {
            ai.edit().putBoolean("ai_quizzes",false).putBoolean("creative_trip_packs",false).putBoolean("automatic_trip_packs",false).commit();
            activity=r.startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.KidsActivity")
                .putExtra("com.khuongnd.dexkids.extra.LIVE_GPS",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            require(activity!=null && !activity.isFinishing(),"Direct test LIVE activity unavailable");
            SystemClock.sleep(2500);
            var game=(KidsGame)field(activity,"runningGame");var live=(LiveJourneyFeed)field(activity,"liveFeed");
            double[] distance={0};boolean[] accepted={false};long until=SystemClock.elapsedRealtime()+20000;
            while(SystemClock.elapsedRealtime()<until) {
                gl(()->{distance[0]=live.distanceMeters();accepted[0]=live.position(System.currentTimeMillis()).isPresent();});
                if(distance[0]>2 && accepted[0])break;SystemClock.sleep(250);
            }
            var manager=(LocationManager)context.getSystemService(android.content.Context.LOCATION_SERVICE);
            var fix=manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            result.putBoolean("gps_provider_enabled",manager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            result.putBoolean("android_has_fix",fix!=null);
            if(fix!=null){result.putFloat("accuracy_m",fix.getAccuracy());result.putLong("fix_age_ms",System.currentTimeMillis()-fix.getTime());}
            result.putDouble("accepted_distance_m",distance[0]);result.putBoolean("quality_accepted",accepted[0]);
            require(distance[0]>2 && accepted[0],"Android GPS fixes did not pass unchanged live quality gate");
            result.putString("t059_gps_preflight","PASS: actual Android GPS provider -> unchanged quality gate -> accepted moving LiveJourneyFeed; synthetic fixes, not real-road accuracy");
        } finally {
            final Activity closing=activity;r.runOnMainSync(()->{if(closing!=null)closing.finish();});restore(ai,old);
        }
    }
}
