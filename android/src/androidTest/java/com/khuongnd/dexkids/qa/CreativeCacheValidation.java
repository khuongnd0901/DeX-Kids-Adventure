package com.khuongnd.dexkids.qa;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import com.khuongnd.dexkids.ai.CreativePackStore;
import com.khuongnd.dexkids.story.CreativeLessonPack;
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Production AtomicFile implementation exercised in a disposable namespace. */
final class CreativeCacheValidation {
    static void run(LifecycleValidationRunner runner, Bundle result) throws Exception {
        Context base = runner.getTargetContext();
        File directory = new File(base.getCacheDir(), "qa-t031-cache");
        LifecycleValidationRunner.require(directory.isDirectory() || directory.mkdirs(), "QA directory missing");
        Context isolated = new ContextWrapper(base) {
            @Override public Context getApplicationContext() { return this; }
            @Override public File getFilesDir() { return directory; }
            @Override public android.content.SharedPreferences getSharedPreferences(String name, int mode) {
                return base.getSharedPreferences("qa-t031-" + name,mode);
            }
        };
        try {
            for (Audience audience : Audience.values()) {
                var store = new CreativePackStore(isolated,audience,10,4);
                store.clear();
                var lessons = new ArrayList<CreativeLessonPack.Lesson>();
                for (int i=0;i<16;i++) {
                    Audience focus = audience == Audience.BOTH ? Audience.values()[i%3] : audience;
                    int age = focus == Audience.SAU ? 10 : 4;
                    var genre = CreativeLessonPack.Genre.values()[i%4];
                    lessons.add(new CreativeLessonPack.Lesson((genre==CreativeLessonPack.Genre.STORY ? "Trong chuyện tưởng tượng số " : "Trong bức tranh số ") + i + ", con thấy chiếc lá màu gì?",
                        "Chiếc lá trong tranh có màu xanh.","green","màu xanh lá","topic"+(i%4),genre,focus,age,genre==CreativeLessonPack.Genre.STORY));
                }
                var pack = CreativeLessonPack.validate(lessons,audience,10,4,List.of());
                var saved = store.swap(pack);
                LifecycleValidationRunner.require(store.load().getPack().lessons().size()==16, "Roundtrip failed");
                store.markConsumed(saved.getFingerprint(),7);
                var reloaded = new CreativePackStore(isolated,audience,10,4);
                LifecycleValidationRunner.require(reloaded.load().getCursor()==7, "Cursor restart failed");
                store.remember(lessons.get(0).prompt());
                boolean duplicate = false;
                try { store.swap(pack); } catch (IllegalArgumentException expected) { duplicate=true; }
                LifecycleValidationRunner.require(duplicate && store.load().getCursor()==7, "Last good lost on rejected pack");
                LifecycleValidationRunner.require(new CreativePackStore(isolated,audience,9,4).load()==null, "Changed age cache accepted");
                File file = new File(directory,"creative-packs-v1-"+audience.name().toLowerCase(java.util.Locale.ROOT)+".json");
                var json = new org.json.JSONObject(new String(Files.readAllBytes(file.toPath()),java.nio.charset.StandardCharsets.UTF_8));
                json.put("sha256","invalid"); Files.write(file.toPath(),json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                LifecycleValidationRunner.require(store.load()==null, "Corrupt checksum accepted");
                store.clear();
                result.putString(audience.name(),"PASS: roundtrip/cursor/recent duplicate/last-good/age/checksum");
            }
        } finally {
            isolated.getSharedPreferences("creative-pack-state-v1",0).edit().clear().commit();
            File[] files=directory.listFiles(); if(files!=null) for(File file:files) Files.delete(file.toPath());
            Files.delete(directory.toPath());
        }
        result.putString("scope","Production Android AtomicFile; isolated synthetic pack, no cloud or user pack changes; power-loss NOT VERIFIED");
    }
}
