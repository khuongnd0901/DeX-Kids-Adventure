package com.khuongnd.dexkids.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.khuongnd.dexkids.story.VisualCue;
import com.khuongnd.dexkids.story.VisualCue.*;
import com.khuongnd.dexkids.story.adventure.*;
import com.khuongnd.dexkids.story.adventure.StoryEpisode.*;
import java.util.*;

/** Actual OpenGL renderer gallery; no GPS, voice, network, parent settings or device installation. */
public final class StoryWorldGallery extends ApplicationAdapter {
    record Case(String name,Theme theme,Actor actor,Emotion emotion,Action action,Prop prop,
                List<SupportingProp> supports,Audience audience,int costume,float time) {}
    private final List<Case> cases=new ArrayList<>();
    private final String output;
    private CartoonSprites legacy;
    private StoryWorldRenderer world;
    private SpriteBatch batch;
    private BitmapFont font;
    private int index;
    private int lifecycleCycles;
    StoryWorldGallery(String output){this.output=output;}
    @Override public void create() {
        batch=new SpriteBatch();font=new BitmapFont();
        var camera=new OrthographicCamera(1920,1080);camera.position.set(960,540,0);camera.update();batch.setProjectionMatrix(camera.combined);
        legacy=new CartoonSprites(new M10SceneVariety(20261008L),new com.khuongnd.dexkids.world.WorldWindow(new com.khuongnd.dexkids.world.ProceduralWorldGenerator(20261008L),0,3));
        // Legacy mappings use the same production renderer ownership and atlases.
        world=new StoryWorldRenderer(legacy::worldGalleryRegion);
        for(Theme theme:Theme.values())if(StoryWorldCatalog.newTheme(theme))add("background-"+theme,theme,Actor.CAPYBARA,Emotion.CALM,Action.IDLE,Prop.NONE,List.of(),Audience.BOTH,0,0);
        for(var object:StoryWorldCatalog.objects())add("object-"+object.prop(),Theme.HOME,Actor.CAPYBARA,Emotion.HAPPY,Action.POINT,object.prop(),List.of(),Audience.BOTH,0,.75f);
        for(Actor actor:Actor.values()) {
            for(Emotion emotion:Emotion.values())add("emotion-"+actor+"-"+emotion,Theme.GARDEN,actor,emotion,Action.IDLE,Prop.NONE,List.of(),Audience.BOTH,0,0);
            for(Action action:Action.values())for(int phase=0;phase<2;phase++)add("action-"+actor+"-"+action+"-"+phase,Theme.CLASSROOM,actor,Emotion.CURIOUS,action,StoryWorldFixtures.target(action,0),List.of(),Audience.BOTH,0,phase==0?.25f:.75f);
            if(actor!=Actor.CAPYBARA)for(int c=0;c<10;c++)for(Action action:List.of(Action.WAVE,Action.SIT,Action.CARRY))
                add("outfit-"+actor+"-"+c+"-"+action,Theme.HOME,actor,Emotion.HAPPY,action,StoryWorldFixtures.target(action,0),List.of(),Audience.BOTH,c,1.25f);
        }
        for(Audience audience:Audience.values()) {
            var e=StoryWorldFixtures.showcase(audience);
            for(int s=0;s<8;s++){var b=e.scenes().get(s).beats().get(0);add("scene-"+audience+"-"+s,e.scenes().get(s).backdrop(),b.actor(),b.emotion(),b.action(),b.prop(),b.supportingProps(),audience,s%10,.8f);}
        }
        StringBuilder pack=new StringBuilder("{\"schemaVersion\":1,\"stories\":[");
        for(Audience audience:Audience.values()){if(audience.ordinal()>0)pack.append(',');pack.append(StoryEpisodeCodec.toJson(StoryWorldFixtures.showcase(audience)));}
        pack.append("]}\n");Gdx.files.local(output+"/stories.world.example.json").writeString(pack.toString(),false,"UTF-8");
        Gdx.files.local(output).mkdirs();
    }
    private void add(String name,Theme theme,Actor actor,Emotion emotion,Action action,Prop prop,List<SupportingProp> supports,Audience audience,int costume,float time) {
        cases.add(new Case(name,theme,actor,emotion,action,prop,supports,audience,costume,time));
    }
    @Override public void render() {
        if(index>=cases.size()) {
            if(lifecycleCycles++<5){world.dispose();world=new StoryWorldRenderer(legacy::worldGalleryRegion);world.prepare(Theme.HOME,.3f);return;}
            Gdx.files.local(output+"/results.json").writeString("{\"renderer\":\""+Gdx.gl.glGetString(GL20.GL_RENDERER)+"\",\"captures\":"+cases.size()+",\"lifecycleCycles\":5,\"liveTextureUpperBoundBytes\":"+StoryWorldRenderer.MAX_LIVE_TEXTURE_BYTES+"}\n",false);
            Gdx.app.exit();return;
        }
        var c=cases.get(index++);world.prepare(c.theme,.3f);
        Gdx.gl.glClearColor(.9f,.95f,1,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);batch.begin();
        world.draw(batch,new VisualCue(c.theme,c.action,c.prop),c.emotion,c.actor,c.supports,c.time,false,c.audience,c.costume,c.costume);
        font.setColor(.15f,.2f,.3f,1);font.draw(batch,c.name,25,1040);batch.end();
        Pixmap full=ScreenUtils.getFrameBufferPixmap(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
        // Capture is production GL; thumbnails reduce disk cost. Selected stage frames stay FullHD.
        boolean detailed=StoryWorldRenderer.attached(c.action) || c.name.startsWith("scene-") || c.name.startsWith("outfit-") || c.name.equals("action-CAPYBARA-READ-1");
        int w=detailed?1920:640,h=detailed?1080:360;Pixmap scaled=new Pixmap(w,h,Pixmap.Format.RGBA8888);
        scaled.drawPixmap(full,0,0,full.getWidth(),full.getHeight(),0,0,w,h);
        PixmapIO.writePNG(Gdx.files.local(output+"/"+c.name+".png"),scaled,1,true);
        scaled.dispose();full.dispose();
    }
    @Override public void dispose(){if(world!=null)world.dispose();if(legacy!=null)legacy.dispose();if(batch!=null)batch.dispose();if(font!=null)font.dispose();}
    public static void main(String[] args) {
        var config=new Lwjgl3ApplicationConfiguration();config.setTitle("T-070 story world QA");config.setWindowedMode(1920,1080);config.setForegroundFPS(60);config.setInitialVisible(false);config.disableAudio(true);
        new Lwjgl3Application(new StoryWorldGallery(args.length==0?"build/story-world-gallery":args[0]),config);
    }
}
