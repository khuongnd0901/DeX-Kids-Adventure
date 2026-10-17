package com.khuongnd.dexkids.qa;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.khuongnd.dexkids.LearningSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

/** Isolated Android AtomicFile/preferences checks; does not touch the parent's real source. */
final class LearningSourceValidation {
    static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static void run(LifecycleValidationRunner runner, Bundle result) throws Exception { run(runner,result,false); }
    static void run(LifecycleValidationRunner runner, Bundle result, boolean liveHttps) throws Exception {
        Context base = runner.getTargetContext();
        String prefix = "t068-isolated-" + System.nanoTime();
        File root = new File(base.getCacheDir(), prefix); require(root.mkdirs(), "create isolated files");
        Context isolated = new ContextWrapper(base) {
            @Override public Context getApplicationContext() { return this; }
            @Override public File getFilesDir() { return root; }
            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return base.getSharedPreferences(prefix+name, mode);
            }
        };
        String data = "# dexkids-offline-learning-v1\n" +
            "id\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish\n" +
            "quiz-test-fish\tQUIZ\tanimal\t4\t6\tCá sống ở dưới nước hay trên cây?\tCá sống dưới nước.\t\n" +
            "word-test-fish\tWORD\tanimal\t4\t6\tCon nói con cá bằng tiếng Anh được không?\tFish nghĩa là con cá.\tfish\n";
        try {
            LearningSource source = LearningSource.INSTANCE;
            require(source.load(isolated).size() == 176, "first load falls back to bundled catalog");
            source.importFile(isolated, new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
            require(source.mode(isolated).equals("file"), "import selects local mode");
            require(source.load(isolated).size() == 2, "import replaces catalog snapshot");
            File cache = new File(root, "learning-source.json"); byte[] before = Files.readAllBytes(cache.toPath());
            for (byte[] invalid : new byte[][] { "bad".getBytes(StandardCharsets.UTF_8), new byte[131073] }) {
                boolean rejected = false;
                try { source.importFile(isolated, new ByteArrayInputStream(invalid)); }
                catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "malformed/oversized import rejected");
                require(Arrays.equals(before, Files.readAllBytes(cache.toPath())), "failed import retains exact last-good cache");
                require(source.load(isolated).size() == 2, "last-good cache still playable");
            }
            source.useBuiltin(isolated); require(source.load(isolated).size() == 176, "parent can restore builtin");
            boolean rejected = false;
            try { source.configure(isolated, "http://example.org/learning.tsv"); }
            catch (IllegalArgumentException expected) { rejected = true; }
            require(rejected && source.mode(isolated).equals("builtin"), "invalid URL cannot change configuration");
            source.configure(isolated, "https://example.org/learning.tsv");
            require(source.load(isolated).size() == 2, "new HTTPS URL retains last-good while awaiting download");
            // A fresh Context wrapper reads the same disk snapshot; no process-memory card cache.
            Context reopened = new ContextWrapper(isolated) {};
            require(source.load(reopened).size() == 2, "disk snapshot reload");
            if (liveHttps) {
                String publicSource="https://raw.githubusercontent.com/khuongnd0901/DeX-Kids-Adventure/main/assets/learning/offline-learning.tsv";
                source.configure(isolated, publicSource);
                var completed=new java.util.concurrent.CountDownLatch(1);
                source.request(isolated,true,()->{completed.countDown();return kotlin.Unit.INSTANCE;});
                require(completed.await(45,java.util.concurrent.TimeUnit.SECONDS),"HTTPS source callback timed out");
                require(source.status(isolated).startsWith("Đã lưu 176"),"Real public HTTPS sync failed: "+source.status(isolated));
                require(source.load(isolated).size()==176,"HTTPS catalog not activated");
                var record=new org.json.JSONObject(new String(Files.readAllBytes(cache.toPath()),StandardCharsets.UTF_8));
                byte[] downloaded=record.getString("data").getBytes(StandardCharsets.UTF_8);
                var digest=java.security.MessageDigest.getInstance("SHA-256").digest(downloaded);
                StringBuilder hash=new StringBuilder();for(byte value:digest)hash.append(String.format("%02x",value));
                result.putString("public_https_source",publicSource);
                result.putString("public_https_sha256",hash.toString());
                result.putInt("public_https_bytes",downloaded.length);
                require(source.load(new ContextWrapper(isolated) {}).size()==176,"Cached downloaded catalog cannot reload");
            }
            result.putString("t068_learning_source", "PASS: isolated import, bounds, atomic retention, source switching, disk reload");
        } finally {
            base.getSharedPreferences(prefix+"learning_source_v1",0).edit().clear().commit();
            for (File f : root.listFiles()) f.delete(); root.delete();
        }
    }
}
