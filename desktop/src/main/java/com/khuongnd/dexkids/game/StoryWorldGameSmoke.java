package com.khuongnd.dexkids.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.khuongnd.dexkids.story.adventure.*;
import com.khuongnd.dexkids.story.adventure.StoryEpisode.Audience;
import java.security.MessageDigest;
import java.util.Arrays;

/** Exercise the actual KidsGame/AdventureScreen HUD bridge, pause and Full/Audio resource ownership. */
public final class StoryWorldGameSmoke extends ApplicationAdapter {
    private KidsGame game;
    private int frame;
    private byte[] pausedPixels;
    private final String out="build/story-world-game-smoke";
    @Override public void create(){game=new KidsGame();game.create();}
    @Override public void resize(int width,int height){if(game!=null)game.resize(width,height);}
    private void story(Audience audience,int scene) {
        game.setAudienceMode(audience.name());var beat=new StorySceneCompiler().compile(StoryWorldFixtures.showcase(audience),scene,0);
        game.showStoryBeat(beat);game.showEntertainmentPhase(beat.id(),"QUESTION");
    }
    @Override public void render() {
        frame++;
        if(frame==1)story(Audience.BOTH,0);
        if(frame==10)game.setParentMenuOpen(true);
        if(frame==16)game.setParentMenuOpen(false);
        if(frame==25)game.clearStoryBeat();
        if(frame==30)game.setAudioOnly(true);
        if(frame==40)story(Audience.SAU,3);
        if(frame==45)game.setAudioOnly(false);
        if(frame==60)story(Audience.ONG,7);
        if(frame>=80 && frame<100 && frame%2==0)game.setAudioOnly(true);
        if(frame>=80 && frame<100 && frame%2==1)game.setAudioOnly(false);
        game.render();
        if(frame==26 && (game.currentVisual().world() || game.currentVisual().story()))throw new AssertionError("Journey cue was not restored");
        if(frame==55 && !game.currentVisual().world())throw new AssertionError("World beat lost through Audio-only");
        if(frame==12)pausedPixels=ScreenUtils.getFrameBufferPixels(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight(),true);
        if(frame==15 && !Arrays.equals(pausedPixels,ScreenUtils.getFrameBufferPixels(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight(),true)))throw new AssertionError("World animation moved during parent pause");
        if(frame==20 || frame==26 || frame==34 || frame==55 || frame==75) {
            Pixmap image=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
            try {PixmapIO.writePNG(Gdx.files.local(out+"/frame-"+frame+".png"),image,6,true);}finally{image.dispose();}
        }
        if(frame==105) {
            Gdx.files.local(out+"/results.json").writeString("{\"frames\":105,\"pausedPixelsIdentical\":true,\"fullAudioCycles\":10,\"audiences\":3,\"journeyRestored\":true}\n",false);
            Gdx.app.exit();
        }
    }
    @Override public void dispose(){if(game!=null)game.dispose();}
    public static void main(String[] args){var c=new Lwjgl3ApplicationConfiguration();c.setWindowedMode(1920,1080);c.setInitialVisible(false);c.disableAudio(true);c.setForegroundFPS(60);new Lwjgl3Application(new StoryWorldGameSmoke(),c);}
}
