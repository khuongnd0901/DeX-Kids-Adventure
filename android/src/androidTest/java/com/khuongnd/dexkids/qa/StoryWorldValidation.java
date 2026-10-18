package com.khuongnd.dexkids.qa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.khuongnd.dexkids.game.*;
import com.khuongnd.dexkids.story.VisualCue;
import com.khuongnd.dexkids.story.adventure.*;
import java.io.File;
import java.util.*;
import static com.khuongnd.dexkids.qa.ArtVisualValidation.*;
import static com.khuongnd.dexkids.story.adventure.StoryEpisode.*;

/** Physical-display v2 rendering, bounded resource sample and reconstruction. No AI/TTS claims. */
final class StoryWorldValidation {
    static void run(LifecycleValidationRunner r,Bundle result)throws Exception {
        require(r.fullHdQaDisplay(),"Explicit FullHD QA display required");
        var context=r.getTargetContext();var prefs=context.getSharedPreferences("parent_settings",0);var old=new HashMap<>(prefs.getAll());
        var ai=context.getSharedPreferences("kids_ai_settings_v1",0);var oldAi=new HashMap<>(ai.getAll());
        Activity[] activities=new Activity[2];var monitor=r.addMonitor("com.khuongnd.dexkids.KidsActivity",null,false);
        File dir=new File(context.getExternalFilesDir(null),"qa-t070-world");require(dir.isDirectory()||dir.mkdirs(),"Evidence directory");
        try {
            prefs.edit().putString("audience_mode","BOTH").putBoolean("audio_only",false).putBoolean("offline_tts",false).putBoolean("tts_enabled",false).putBoolean("ambient_music",false).putBoolean("child_mic_optin",false).commit();
            ai.edit().putBoolean("ai_quizzes",false).putBoolean("ai_trip_companion_optin",false).putBoolean("creative_trip_packs",false).commit();
            activities[0]=r.startActivitySync(new Intent().setClassName(context,"com.khuongnd.dexkids.ParentActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            r.runOnMainSync(()->require(preview(activities[0].getWindow().getDecorView()).performClick(),"DEMO missing"));
            activities[1]=r.waitForMonitorWithTimeout(monitor,10000);require(activities[1]!=null,"Child missing");SystemClock.sleep(3000);
            r.runOnMainSync(()->{try {
                ((android.os.Handler)field(activities[1],"handler")).removeCallbacks((Runnable)field(activities[1],"updatePoiNativeStatus"));
                var clear=activities[1].getClass().getDeclaredMethod("clearTalkQueue");clear.setAccessible(true);clear.invoke(activities[1]);
                Object session=field(activities[1],"storySession");var stop=session.getClass().getDeclaredMethod("stop");stop.setAccessible(true);stop.invoke(session);
            }catch(Exception e){throw new AssertionError(e);}});
            KidsGame game=(KidsGame)field(activities[1],"runningGame");Object screen=game.getScreen();
            sample(screen,result,"journey");
            var themes=new VisualCue.Theme[]{VisualCue.Theme.HOME,VisualCue.Theme.CLASSROOM,VisualCue.Theme.PLAYGROUND,VisualCue.Theme.GARDEN,VisualCue.Theme.FOREST,VisualCue.Theme.BEACH,VisualCue.Theme.MARKET,VisualCue.Theme.LIBRARY};
            var props=new ArrayList<>(StoryWorldCatalog.objects());int count=0;
            for(int i=0;i<props.size();i++) {
                show(r,game,themes[i%8],Actor.values()[i%3],Emotion.values()[i%14],VisualCue.Action.IDLE,props.get(i).prop(),i);
                SystemClock.sleep(100);if(i<8)capture(r,dir,"background-"+i);count++;
            }
            for(Actor actor:Actor.values())for(Emotion emotion:Emotion.values()) {
                show(r,game,VisualCue.Theme.HOME,actor,emotion,VisualCue.Action.TALK,VisualCue.Prop.NONE,count++);SystemClock.sleep(100);
                if(emotion==Emotion.HAPPY||emotion==Emotion.SAD||emotion==Emotion.ANGRY)capture(r,dir,"emotion-"+actor+"-"+emotion);
            }
            for(Actor actor:Actor.values())for(VisualCue.Action action:VisualCue.Action.values()) {
                show(r,game,VisualCue.Theme.GARDEN,actor,Emotion.HAPPY,action,StoryWorldFixtures.target(action,count),count++);SystemClock.sleep(100);
                if(action==VisualCue.Action.SIT||action==VisualCue.Action.DRINK||action==VisualCue.Action.CLAP)capture(r,dir,"action-"+actor+"-"+action);
            }
            SystemClock.sleep(1500);sample(screen,result,"world");
            gl(()->{
                Object world=field(field(screen,"cartoonSprites"),"storyWorld");require(world!=null,"World renderer absent");
                long bytes=0;var atlases=new ArrayList<TextureAtlas>();atlases.add((TextureAtlas)field(world,"objectAtlas"));Collections.addAll(atlases,(TextureAtlas[])field(world,"characterAtlases"));
                for(var atlas:atlases)if(atlas!=null)for(Texture texture:atlas.getTextures())bytes+=(long)texture.getWidth()*texture.getHeight()*4;
                for(String name:new String[]{"background","previous"}){Texture texture=(Texture)field(world,name);if(texture!=null)bytes+=(long)texture.getWidth()*texture.getHeight()*4;}
                require(bytes<=StoryWorldRenderer.MAX_LIVE_TEXTURE_BYTES,"World texture backing exceeded bound");result.putLong("world_texture_bytes",bytes);
            });
            int[] managed=new int[10];
            for(int i=0;i<10;i++) {
                r.runOnMainSync(()->game.setAudioOnly(true));SystemClock.sleep(250);gl(()->require(field(screen,"cartoonSprites")==null,"Audio-only scenery retained"));
                r.runOnMainSync(()->game.setAudioOnly(false));SystemClock.sleep(400);final int index=i;
                gl(()->{require(field(field(screen,"cartoonSprites"),"storyWorld")!=null,"World reconstruction missing");managed[index]=Texture.getNumManagedTextures();});
            }
            require(managed[9]==managed[0],"Managed texture count grew across cycles");result.putString("managed_textures_cycles",Arrays.toString(managed));
            for(String audience:new String[]{"SAU","ONG","BOTH"}){r.runOnMainSync(()->game.setAudienceMode(audience));SystemClock.sleep(250);capture(r,dir,"audience-"+audience);}
            r.runOnMainSync(game::clearStoryBeat);SystemClock.sleep(300);capture(r,dir,"restored-journey");
            result.putInt("matrix_cues",count);result.putString("t070_world","PASS: 60 props, 42 emotions, 81 actions; eight backdrops; three audiences; ten Full/Audio reconstructions; texture bound; journey restored. Screenshots require review; short cadence samples exclude capture loops; no acoustic/endurance acceptance.");
        }finally{r.runOnMainSync(()->{for(Activity a:activities)if(a!=null)a.finish();});r.removeMonitor(monitor);restore(prefs,old);restore(ai,oldAi);}
    }
    private static void show(LifecycleValidationRunner r,KidsGame game,VisualCue.Theme theme,Actor actor,Emotion emotion,VisualCue.Action action,VisualCue.Prop prop,int id) {
        var supports=List.of(new SupportingProp(VisualCue.Prop.CHAIR,ObjectSlot.BACK_LEFT),new SupportingProp(VisualCue.Prop.TEDDY_BEAR,ObjectSlot.RIGHT));
        var beat=new StorySceneCompiler.CompiledBeat("qa-world-"+id,"Truyện minh họa · "+actor+" · "+emotion+" · "+action,"SAU",new VisualCue(theme,action,prop),emotion,actor,supports,true);
        r.runOnMainSync(()->{game.showStoryBeat(beat);game.showEntertainmentPhase(beat.id(),"QUESTION");});
    }
    private static void sample(Object screen,Bundle result,String name)throws Exception {
        gl(()->((FrameProfiler)field(screen,"frameProfiler")).reset());SystemClock.sleep(8000);
        gl(()->{var p=(FrameProfiler)field(screen,"frameProfiler");result.putDouble(name+"_fps",p.averageFps());result.putDouble(name+"_p95_ms",p.p95FrameMs());result.putInt(name+"_frames",p.size());});
        var memory=new android.os.Debug.MemoryInfo();android.os.Debug.getMemoryInfo(memory);result.putInt(name+"_pss_kib",memory.getTotalPss());
    }
}
