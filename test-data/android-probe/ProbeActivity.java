package com.khuongnd.dexkids.qaprobe;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.ComponentName;
import android.util.Log;
import android.widget.TextView;

/** Synthetic QA client: only project CONTROL requested; signer chosen by test script. */
public final class ProbeActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StringBuilder result = new StringBuilder("Isolated QA probe\nUID=" + android.os.Process.myUid() + "\n");
        boolean control = checkSelfPermission("com.khuongnd.dexkids.permission.CONTROL") == android.content.pm.PackageManager.PERMISSION_GRANTED;
        result.append(control ? "CONTROL_GRANTED\n" : "CONTROL_DENIED\n");
        Log.i("DexKidsQAProbe", control ? "CONTROL_GRANTED" : "CONTROL_DENIED");
        Intent stop = new Intent("com.khuongnd.dexkids.action.KIDS_STOP");
        stop.setComponent(new ComponentName("com.khuongnd.dexkids", "com.khuongnd.dexkids.KidsCommandReceiver"));
        try {
            sendBroadcast(stop);
            result.append("STOP submitted; delivery NOT proven.\nProtected receivers can silently reject it.\n");
        } catch (SecurityException denied) {
            result.append("STOP SecurityException: ").append(denied.getMessage()).append("\n");
        }
        try {
            startActivity(new Intent().setComponent(new ComponentName("com.khuongnd.dexkids", "com.khuongnd.dexkids.KidsActivity")));
            result.append("DIRECT_LAUNCH_UNEXPECTED_SUCCESS\n");
            Log.e("DexKidsQAProbe", "DIRECT_LAUNCH_UNEXPECTED_SUCCESS");
        } catch (SecurityException denied) {
            result.append("DIRECT_LAUNCH_DENIED_SECURITY\n");
            Log.i("DexKidsQAProbe", "DIRECT_LAUNCH_DENIED_SECURITY", denied);
        } catch (RuntimeException other) {
            result.append("DIRECT_LAUNCH_INCONCLUSIVE: ").append(other).append("\n");
            Log.e("DexKidsQAProbe", "DIRECT_LAUNCH_INCONCLUSIVE", other);
        }
        result.append(control ? "Press BACK; parent must be resumed.\n" : "Press BACK; child must still be resumed.\n");
        TextView view = new TextView(this);
        view.setTextSize(22); view.setPadding(32, 32, 32, 32); view.setText(result);
        setContentView(view);
    }
}
