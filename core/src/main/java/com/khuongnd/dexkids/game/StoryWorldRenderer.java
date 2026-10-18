package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Disposable;
import com.khuongnd.dexkids.story.VisualCue;
import com.khuongnd.dexkids.story.VisualCue.*;
import com.khuongnd.dexkids.story.adventure.StoryEpisode.*;
import com.khuongnd.dexkids.story.adventure.StoryWorldCatalog;
import java.util.*;
import java.util.function.Function;

/** Story-only stage: one complete painted character per actor, never a head/limb rig.
 * GL-thread owned; one object page, one active four-pose page per actor, <=2 backdrops.
 */
public final class StoryWorldRenderer implements Disposable {
    public static final long MAX_LIVE_TEXTURE_BYTES=2048L*1024*4+3L*1024*1024*4+2L*1280*720*4;
    private final TextureAtlas objectAtlas;
    private final TextureRegion waterDrop,shadow;
    private final TextureAtlas[] characterAtlases=new TextureAtlas[3];
    private final int[] characterPages={-1,-1,-1};
    private final TextureRegion[][] characterFrames=new TextureRegion[3][4];
    private final TextureRegion[] objects=new TextureRegion[Prop.values().length];
    private final Function<Prop,TextureRegion> legacy;
    private Texture background,previous;
    private String backdropKey;
    private float fade=1;
    public static FileHandle asset(String path) {
        FileHandle file=Gdx.files.internal("generated/story-world/"+path);
        return file.exists()?file:Gdx.files.internal("assets/generated/story-world/"+path);
    }
    public StoryWorldRenderer(Function<Prop,TextureRegion> legacy) {
        this.legacy=legacy;
        objectAtlas=new TextureAtlas(asset("objects.atlas"));
        waterDrop=required(objectAtlas,"water_drop");shadow=required(objectAtlas,"shadow");
        for(var spec:StoryWorldCatalog.objects())objects[spec.prop().ordinal()]=required(objectAtlas,spec.region());
    }
    private static FileHandle characterAsset(String path) {
        FileHandle file=Gdx.files.internal("generated/story-characters-v3/"+path);
        return file.exists()?file:Gdx.files.internal("assets/generated/story-characters-v3/"+path);
    }
    /** Resource changes happen before SpriteBatch.begin, never while drawing a body. */
    public void prepareCharacters(VisualCue cue,Emotion emotion,Actor active,float elapsed,boolean speaking,Audience audience) {
        for(Actor actor:Actor.values()) {
            int a=actor.ordinal();
            boolean visible=actor==Actor.CAPYBARA || actor==Actor.SAU && audience!=Audience.ONG || actor==Actor.ONG && audience!=Audience.SAU;
            if(!visible) {
                if(characterAtlases[a]!=null){characterAtlases[a].dispose();characterAtlases[a]=null;characterPages[a]=-1;}
                continue;
            }
            var frame=WholeCharacterPose.sample(actor==active?cue.action():Action.IDLE,actor==active?emotion:Emotion.CALM,
                speaking && actor==active,elapsed);
            if(characterPages[a]==frame.page())continue;
            // Release the former page first so transient allocation obeys the live-art bound.
            if(characterAtlases[a]!=null){characterAtlases[a].dispose();characterAtlases[a]=null;characterPages[a]=-1;}
            String name=actor.name().toLowerCase(Locale.ROOT)+"-"+frame.page();
            TextureAtlas next=new TextureAtlas(characterAsset(name+".atlas"));
            try {
                for(int i=0;i<4;i++)characterFrames[a][i]=required(next,"pose_"+(frame.page()*4+i));
            } catch(RuntimeException missing) {next.dispose();throw missing;}
            characterAtlases[a]=next;characterPages[a]=frame.page();
        }
    }
    private static TextureRegion required(TextureAtlas atlas,String key) {
        TextureRegion region=atlas.findRegion(key);if(region==null)throw new IllegalStateException("Missing world sprite "+key);return region;
    }
    private TextureRegion region(Prop prop) {
        TextureRegion r=objects[prop.ordinal()];return r!=null?r:legacy.apply(prop);
    }
    private static String backdrop(Theme theme) {
        return switch(theme) {
            case HOME -> "home";case CLASSROOM -> "classroom";case PLAYGROUND -> "playground";
            case GARDEN,NATURE -> "garden";case FOREST,ANIMALS -> "forest";case BEACH,WATER -> "beach";
            case MARKET -> "market";case LIBRARY,LANGUAGE -> "library";default -> "home";
        };
    }
    /** Upload/dispose ONLY before SpriteBatch.begin. Delta is zero during pause. */
    public void prepare(Theme theme,float delta) {
        String key=backdrop(theme);
        if(!key.equals(backdropKey)) {
            if(previous!=null){previous.dispose();previous=null;}
            Texture next=new Texture(asset(key+".jpg"));next.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
            previous=background;background=next;backdropKey=key;fade=previous==null?1:0;
        }
        fade=Math.min(1,fade+(Float.isFinite(delta)?Math.max(0,delta):0)/.25f);
        if(fade==1 && previous!=null){previous.dispose();previous=null;}
    }
    public void draw(SpriteBatch batch,VisualCue cue,Emotion emotion,Actor actor,List<SupportingProp> supports,
                     float elapsed,boolean speaking,Audience audience) {
        if(background==null)throw new IllegalStateException("Prepare story background before drawing");
        if(previous!=null)batch.draw(previous,0,0,1920,1080);
        batch.setColor(1,1,1,fade);batch.draw(background,0,0,1920,1080);batch.setColor(Color.WHITE);
        for(SupportingProp support:supports)if(behindActors(support))
            drawSupport(batch,support,audience);
        float actorX=actor==Actor.CAPYBARA?400:actor==Actor.SAU?(audience==Audience.BOTH?850:1100):audience==Audience.BOTH?1300:1100;
        float actorH=StoryStageGeometry.height(actor);
        boolean worldAttached=attached(cue.action());
        if(!worldAttached && (cue.prop()!=Prop.NONE || cue.action()==Action.SIT)) {
            Prop stageProp=cue.action()==Action.SIT && cue.prop()==Prop.NONE?Prop.CHAIR:cue.prop();
            var size=StoryStageGeometry.size(stageProp,cue.action()==Action.SIT?actorH:StoryStageGeometry.CHILD_HEIGHT);
            if(sky(cue.prop()))drawSized(batch,region(cue.prop()),1550,790,size.width()*.65f,size.height()*.65f);
            else if(cue.action()==Action.SIT)drawSized(batch,region(stageProp),actorX,StoryStageGeometry.FLOOR,size.width(),size.height());
            else if(cue.action()==Action.PET)drawSized(batch,region(cue.prop()),actorX+actorH*(actor==Actor.SAU?-.28f:.28f),StoryStageGeometry.FLOOR,size.width(),size.height());
            else if(size.width()>500||size.height()>500) {
                var box=StoryWorldLayout.support(ObjectSlot.BACK_RIGHT,cue.prop());drawSized(batch,region(cue.prop()),box.x()+box.width()/2,box.y(),box.width(),box.height());
            }else drawSized(batch,region(cue.prop()),1600,StoryStageGeometry.FLOOR,size.width(),size.height());
        }
        drawActor(batch,Actor.CAPYBARA,400,StoryStageGeometry.FLOOR,StoryStageGeometry.height(Actor.CAPYBARA),actor==Actor.CAPYBARA?cue.action():Action.IDLE,
            actor==Actor.CAPYBARA?emotion:Emotion.CALM,elapsed,speaking && actor==Actor.CAPYBARA);
        if(audience!=Audience.ONG)drawActor(batch,Actor.SAU,audience==Audience.BOTH?850:1100,StoryStageGeometry.FLOOR,StoryStageGeometry.height(Actor.SAU),
            actor==Actor.SAU?cue.action():Action.IDLE,actor==Actor.SAU?emotion:Emotion.CALM,elapsed,speaking && actor==Actor.SAU);
        if(audience!=Audience.SAU)drawActor(batch,Actor.ONG,audience==Audience.BOTH?1300:1100,StoryStageGeometry.FLOOR,StoryStageGeometry.height(Actor.ONG),
            actor==Actor.ONG?cue.action():Action.IDLE,actor==Actor.ONG?emotion:Emotion.CALM,elapsed,speaking && actor==Actor.ONG);
        if(worldAttached)drawInteraction(batch,cue,actor,actorX,StoryStageGeometry.FLOOR,actorH,elapsed);
        for(SupportingProp support:supports)if(!behindActors(support))drawSupport(batch,support,audience);
        batch.setColor(Color.WHITE);
    }
    private static boolean behindActors(SupportingProp support) {
        var size=StoryStageGeometry.size(support.prop(),StoryStageGeometry.CHILD_HEIGHT);
        return support.slot()==ObjectSlot.BACK_LEFT||support.slot()==ObjectSlot.BACK_RIGHT||sky(support.prop())||size.width()>500||size.height()>500;
    }
    private void drawSupport(SpriteBatch batch,SupportingProp support,Audience audience) {
        var box=StoryWorldLayout.support(support.slot(),support.prop(),audience);
        if(support.prop()==Prop.BLANKET)batch.draw(region(support.prop()),box.x(),box.y(),box.width(),box.height());
        else drawSized(batch,region(support.prop()),box.x()+box.width()/2,box.y(),box.width(),box.height());
    }
    public static boolean attached(Action action) {
        return switch(action) {case READ,WATER,SHARE,INSPECT,PICK_UP,PUT_DOWN,CARRY,DRAW,EAT,DRINK -> true;default -> false;};
    }
    /** Props use authored anchors on the complete painting, never computed limb joints. */
    private void drawInteraction(SpriteBatch b,VisualCue cue,Actor actor,float center,float baseline,float h,float t) {
        var frame=WholeCharacterPose.sample(cue.action(),Emotion.CALM,false,t);
        float x=center+frame.x(), y=baseline+frame.y();
        var size=StoryStageGeometry.size(cue.prop(),h);
        switch(cue.action()) {
            case READ -> {y+=h*.32f;}
            case CARRY -> {y+=h*.26f;}
            case SHARE -> {x+=h*.31f;y+=h*.49f;}
            case PICK_UP -> {x+=h*.08f;y+=h*.03f;}
            case PUT_DOWN -> {x+=h*.08f;}
            case EAT,DRINK -> {
                x+=h*(actor==Actor.SAU || actor==Actor.CAPYBARA && cue.action()==Action.EAT?-.10f:.10f);
                y+=h*(actor==Actor.ONG?.39f:.59f);
            }
            case DRAW -> {y+=h*.19f;}
            case INSPECT -> {x+=h*.07f;y+=h*.46f;}
            case WATER -> {x+=h*.32f;}
            default -> {}
        }
        if(cue.action()==Action.DRAW) {
            b.draw(region(cue.prop()),x-size.width()/2,y,size.width(),h*.065f);
            var pencil=StoryStageGeometry.size(Prop.PENCIL,h);
            drawSized(b,region(Prop.PENCIL),x+h*.075f,y+h*.02f,pencil.width(),pencil.height());
        } else drawSized(b,region(cue.prop()),x,y,size.width(),size.height());
        if(cue.action()==Action.INSPECT) {
            var lens=StoryStageGeometry.size(Prop.MAGNIFYING_GLASS,h);
            drawSized(b,region(Prop.MAGNIFYING_GLASS),center-h*.085f,baseline+frame.y()+h*.59f,lens.width(),lens.height());
        }
        if(cue.action()==Action.WATER) {
            var can=StoryStageGeometry.size(Prop.WATERING_CAN,h);
            float canX=center-h*.13f,canY=baseline+frame.y()+h*.37f;
            drawSized(b,region(Prop.WATERING_CAN),canX,canY,can.width(),can.height());
            for(int i=0;i<3;i++) {
                float fall=((Float.isFinite(t)?Math.max(0,t):0)*.7f+i/3f)%1;
                b.setColor(.3f,.65f,.9f,.9f);b.draw(waterDrop,canX+can.width()*.45f,canY-fall*h*.28f,4,8);
            }
            b.setColor(Color.WHITE);
        }
    }
    private void drawActor(SpriteBatch b,Actor actor,float center,float baseline,float h,Action action,Emotion emotion,float t,boolean speaking) {
        var frame=WholeCharacterPose.sample(action,emotion,speaking,t);
        int a=actor.ordinal();
        if(characterPages[a]!=frame.page())throw new IllegalStateException("Prepare complete character art before drawing");
        b.setColor(1,1,1,.18f);b.draw(shadow,center-h*.17f,baseline-7,h*.34f,18);b.setColor(Color.WHITE);
        // The atlas stores complete, foot-aligned 512x512 paintings, including the face,
        // clothes, arms, hands and feet. One draw call; no stretching, masks or overlays.
        float scale=h/480f,w=512*scale,height=512*scale;
        b.draw(characterFrames[a][frame.index()%4],center+frame.x()-w/2,baseline+frame.y()-16*scale,
            w/2,16*scale,w,height,1,1,frame.rotation());
    }
    private static void drawSized(SpriteBatch b,TextureRegion r,float center,float baseline,float width,float height) {
        if(r==null)throw new IllegalStateException("Unmapped world object");
        float scale=Math.min(width/r.getRegionWidth(),height/r.getRegionHeight());float w=r.getRegionWidth()*scale,h=r.getRegionHeight()*scale;
        b.draw(r,center-w/2,baseline,w,h);
    }
    private static boolean sky(Prop prop) {
        var spec=StoryWorldCatalog.object(prop);return spec!=null&&spec.kind()==StoryWorldCatalog.Kind.SKY || prop==Prop.CLOUD||prop==Prop.BIRD||prop==Prop.BUTTERFLY||prop==Prop.KITE;
    }
    @Override public void dispose() {
        if(previous!=null)previous.dispose();if(background!=null)background.dispose();
        objectAtlas.dispose();for(TextureAtlas atlas:characterAtlases)if(atlas!=null)atlas.dispose();
        previous=background=null;
    }
}
