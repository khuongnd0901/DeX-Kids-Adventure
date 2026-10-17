package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.khuongnd.dexkids.story.VisualCue;
import com.khuongnd.dexkids.story.VisualCue.*;
import com.khuongnd.dexkids.story.adventure.StoryEpisode.*;
import com.khuongnd.dexkids.story.adventure.StoryWorldCatalog;
import java.util.*;
import java.util.function.Function;

/** Story-only cutout stage. GL-thread owned; one object page, three rig pages, <=2 backdrops. */
public final class StoryWorldRenderer implements Disposable {
    public static final long MAX_LIVE_TEXTURE_BYTES=5L*1024*1024*4+2L*1280*720*4;
    private static final String[][] COSTUMES={{"solar","tech","web","thunder","shield","shadow","classic_explorer","classic_firefighter","classic_pilot","classic_police"},
        {"rose","ocean","sky","forest","starlight","lavender","classic_explorer","classic_firefighter","classic_pilot","classic_police"},{"explorer"}};
    private static final String[] PARTS={"torso","upper_left","fore_left","upper_right","fore_right","leg_left","leg_right"};
    private final TextureAtlas objectAtlas;
    private final TextureRegion waterDrop,shadow;
    private final TextureAtlas[] rigAtlases=new TextureAtlas[3];
    private final TextureRegion[][][] bodies=new TextureRegion[3][][];
    private final TextureRegion[][][] knees=new TextureRegion[3][][];
    private final TextureRegion[][] heads=new TextureRegion[3][16];
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
        String[] names={"sau","ong","capybara"};
        for(int a=0;a<3;a++) {
            rigAtlases[a]=new TextureAtlas(asset(names[a]+".atlas"));
            bodies[a]=new TextureRegion[COSTUMES[a].length][PARTS.length];
            for(int c=0;c<COSTUMES[a].length;c++)for(int p=0;p<PARTS.length;p++)bodies[a][c][p]=required(rigAtlases[a],COSTUMES[a][c]+"_"+PARTS[p]);
            knees[a]=new TextureRegion[COSTUMES[a].length][4];
            for(int c=0;c<COSTUMES[a].length;c++)for(int leg=0;leg<2;leg++) {
                TextureRegion full=bodies[a][c][5+leg];int cut=Math.max(1,full.getRegionHeight()/2);
                knees[a][c][leg*2]=new TextureRegion(full,0,0,full.getRegionWidth(),cut+2);
                knees[a][c][leg*2+1]=new TextureRegion(full,0,cut,full.getRegionWidth(),full.getRegionHeight()-cut);
            }
            for(Emotion emotion:Emotion.values())heads[a][emotion.ordinal()]=required(rigAtlases[a],"head_"+emotion.name().toLowerCase(Locale.ROOT));
            heads[a][14]=required(rigAtlases[a],"head_closed");heads[a][15]=required(rigAtlases[a],"head_open");
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
                     float elapsed,boolean speaking,Audience audience,int sauOutfit,int ongOutfit) {
        if(background==null)throw new IllegalStateException("Prepare story background before drawing");
        if(previous!=null)batch.draw(previous,0,0,1920,1080);
        batch.setColor(1,1,1,fade);batch.draw(background,0,0,1920,1080);batch.setColor(Color.WHITE);
        for(SupportingProp support:supports)if(behindActors(support))
            drawSupport(batch,support,audience);
        float actorX=actor==Actor.CAPYBARA?400:actor==Actor.SAU?(audience==Audience.BOTH?850:1100):audience==Audience.BOTH?1300:1100;
        float actorH=StoryStageGeometry.height(actor);
        boolean worldAttached=attached(cue.action());
        if(!worldAttached && cue.prop()!=Prop.NONE) {
            var size=StoryStageGeometry.size(cue.prop(),cue.action()==Action.SIT?actorH:StoryStageGeometry.CHILD_HEIGHT);
            if(sky(cue.prop()))drawSized(batch,region(cue.prop()),1550,790,size.width()*.65f,size.height()*.65f);
            else if(cue.action()==Action.SIT)drawSized(batch,region(cue.prop()),actorX,StoryStageGeometry.FLOOR,size.width(),size.height());
            else if(size.width()>500||size.height()>500) {
                var box=StoryWorldLayout.support(ObjectSlot.BACK_RIGHT,cue.prop());drawSized(batch,region(cue.prop()),box.x()+box.width()/2,box.y(),box.width(),box.height());
            }else drawSized(batch,region(cue.prop()),1600,StoryStageGeometry.FLOOR,size.width(),size.height());
        }
        drawActor(batch,Actor.CAPYBARA,0,400,StoryStageGeometry.FLOOR,StoryStageGeometry.height(Actor.CAPYBARA),actor==Actor.CAPYBARA?cue.action():Action.IDLE,
            actor==Actor.CAPYBARA?emotion:Emotion.CALM,elapsed,speaking && actor==Actor.CAPYBARA);
        if(audience!=Audience.ONG)drawActor(batch,Actor.SAU,Math.floorMod(sauOutfit,10),audience==Audience.BOTH?850:1100,StoryStageGeometry.FLOOR,StoryStageGeometry.height(Actor.SAU),
            actor==Actor.SAU?cue.action():Action.IDLE,actor==Actor.SAU?emotion:Emotion.CALM,elapsed,speaking && actor==Actor.SAU);
        if(audience!=Audience.SAU)drawActor(batch,Actor.ONG,Math.floorMod(ongOutfit,10),audience==Audience.BOTH?1300:1100,StoryStageGeometry.FLOOR,StoryStageGeometry.height(Actor.ONG),
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
    private void drawInteraction(SpriteBatch b,VisualCue cue,Actor actor,float center,float baseline,float h,float t) {
        var pose=StoryRigMotion.sample(cue.action(),t);
        var hand=hand(actor,pose,cue.action(),t,false,h);
        float x=center+pose.x()+hand.x(),y=baseline+pose.y()+hand.y();
        var size=StoryStageGeometry.size(cue.prop(),h);
        if(cue.action()==Action.READ || cue.action()==Action.CARRY || cue.action()==Action.DRAW) {
            x=center+pose.x();y=baseline+pose.y()+h*.43f;
        } else {x-=size.width()*.18f;y-=size.height()*(cue.action()==Action.PICK_UP||cue.action()==Action.PUT_DOWN?.90f:.42f);}
        if(cue.action()==Action.DRAW) {
            var desk=StoryStageGeometry.size(Prop.TABLE,h);drawSized(b,region(Prop.TABLE),center,baseline,desk.width(),desk.height());y=baseline+h*.50f;size=new StoryStageGeometry.Size(size.width(),h*.08f);
        }
        if(cue.action()==Action.WATER){x=center+hand.x()+h*.15f;y=baseline;}
        if(cue.action()==Action.INSPECT){var left=hand(actor,pose,cue.action(),t,true,h);x=center+pose.x()+left.x();y=baseline+pose.y()+left.y()-size.height()*.4f;}
        if(cue.action()==Action.DRAW)b.draw(region(cue.prop()),x-size.width()/2,y,size.width(),size.height());
        else drawSized(b,region(cue.prop()),x,y,size.width(),size.height());
        if(cue.action()==Action.DRAW) {
            var pencil=StoryStageGeometry.size(Prop.PENCIL,h);
            drawSized(b,region(Prop.PENCIL),center+hand.x(),baseline+pose.y()+hand.y()-pencil.height()*.45f,pencil.width(),pencil.height());
        }
        if(cue.action()==Action.INSPECT) {
            var lens=StoryStageGeometry.size(Prop.MAGNIFYING_GLASS,h);
            drawSized(b,region(Prop.MAGNIFYING_GLASS),center+hand.x(),baseline+pose.y()+hand.y()-lens.height()*.2f,lens.width(),lens.height());
        }
        if(cue.action()==Action.WATER) {
            var can=StoryStageGeometry.size(Prop.WATERING_CAN,h);
            drawSized(b,region(Prop.WATERING_CAN),center+hand.x(),baseline+pose.y()+hand.y()-can.height()*.6f,can.width(),can.height());
            for(int i=0;i<3;i++){float fall=(t*.7f+i/3f)%1;b.setColor(.3f,.65f,.9f,.9f);b.draw(waterDrop,center+hand.x()+can.width()*.45f,baseline+pose.y()+hand.y()-can.height()*.6f-fall*h*.30f,4,8);}
            b.setColor(Color.WHITE);
        }
    }
    private static StoryStageGeometry.Point bodyPoint(StoryRigMotion.Pose pose,float x,float y,float h) {
        var rotated=StoryStageGeometry.rotate(x,y-h*.34f,pose.body());return new StoryStageGeometry.Point(rotated.x(),rotated.y()+h*.34f);
    }
    private static StoryStageGeometry.Arm armPose(Actor actor,StoryRigMotion.Pose pose,Action action,float t,boolean left,float h) {
        float sign=left?-1:1,shoulderX=sign*StoryStageGeometry.shoulderOffset(actor)*h,shoulderY=h*.64f;
        float tx=0,ty=0;boolean reach=false;float p=(1-MathUtils.cos(t*MathUtils.PI))*.5f;
        switch(action) {
            case READ,CARRY -> {tx=sign*h*.105f;ty=h*(action==Action.CARRY?.55f:.48f);reach=true;}
            case CLAP -> {tx=sign*h*(.025f+.05f*(1-p));ty=h*.55f;reach=true;}
            case DRAW -> {tx=(left?-.10f:.08f)*h;ty=h*(left?.52f:.59f+MathUtils.sin(t*5)*.012f);reach=true;}
            case EAT,DRINK -> {if(!left){tx=h*(.17f-p*.21f);ty=h*(.48f+p*.24f);reach=true;}}
            case THINK -> {if(!left){tx=h*.05f;ty=h*.73f;reach=true;}}
            case INSPECT -> {tx=h*(left?-.10f:.09f);ty=h*(left?.54f:.65f);reach=true;}
            case PICK_UP,PUT_DOWN -> {if(!left){tx=h*.17f;ty=h*(.36f+.15f*(action==Action.PICK_UP?p:1-p));reach=true;}}
            case WATER -> {if(!left){tx=h*.24f;ty=h*.55f;reach=true;}}
            default -> {}
        }
        return reach?StoryStageGeometry.reach(tx-shoulderX,ty-shoulderY,h*StoryStageGeometry.upperLength(actor),h*StoryStageGeometry.foreLength(actor),left):
            new StoryStageGeometry.Arm(left?pose.leftUpper():pose.rightUpper(),left?pose.leftFore():pose.rightFore());
    }
    private static StoryStageGeometry.Point hand(Actor actor,StoryRigMotion.Pose pose,Action action,float t,boolean left,float h) {
        var a=armPose(actor,pose,action,t,left,h);float sign=left?-1:1;
        var shoulder=bodyPoint(pose,sign*StoryStageGeometry.shoulderOffset(actor)*h,h*.64f,h);
        var elbow=StoryStageGeometry.endpoint(shoulder.x(),shoulder.y(),h*StoryStageGeometry.upperLength(actor),a.upper()+pose.body());
        return StoryStageGeometry.endpoint(elbow.x(),elbow.y(),h*StoryStageGeometry.foreLength(actor),a.upper()+a.fore()+pose.body());
    }
    private void drawActor(SpriteBatch b,Actor actor,int costume,float center,float baseline,float h,Action action,Emotion emotion,float t,boolean speaking) {
        var pose=StoryRigMotion.sample(action,t);TextureRegion[] parts=bodies[actor.ordinal()][costume];
        float x=center+pose.x(),y=baseline+pose.y(),torsoW=h*StoryStageGeometry.torsoWidth(actor);
        b.setColor(1,1,1,.22f);b.draw(shadow,center-h*.17f,baseline-7,h*.34f,18);b.setColor(Color.WHITE);
        for(int leg=0;leg<2;leg++) {
            var hip=bodyPoint(pose,(leg==0?-1:1)*h*.065f,h*.34f,h);float angle=leg==0?pose.leftLeg():pose.rightLeg();
            if(action==Action.SIT) {
                limb(b,knees[actor.ordinal()][costume][leg*2],x+hip.x(),y+hip.y(),h*.105f,h*.18f,angle);
                var knee=StoryStageGeometry.endpoint(x+hip.x(),y+hip.y(),h*.16f,angle);
                limb(b,knees[actor.ordinal()][costume][leg*2+1],knee.x(),knee.y(),h*.105f,h*.28f,0);
            }else limb(b,parts[5+leg],x+hip.x(),y+hip.y(),h*.115f,h*.36f,angle);
        }
        // Upper arms behind the torso: shoulders overlap under clothing, never expose sockets.
        for(int side=0;side<2;side++) {
            boolean left=side==0;var a=armPose(actor,pose,action,t,left,h);
            var shoulder=bodyPoint(pose,(left?-1:1)*StoryStageGeometry.shoulderOffset(actor)*h,h*.64f,h);
            armLimb(b,parts[left?1:3],x+shoulder.x(),y+shoulder.y(),h*(actor==Actor.CAPYBARA?.105f:.075f),h*StoryStageGeometry.upperLength(actor),a.upper()+pose.body());
        }
        b.draw(parts[0],x-torsoW/2,y+h*.29f,torsoW/2,h*.05f,torsoW,h*.42f,1,1,pose.body());
        for(int side=0;side<2;side++) {
            boolean left=side==0;var a=armPose(actor,pose,action,t,left,h);
            var shoulder=bodyPoint(pose,(left?-1:1)*StoryStageGeometry.shoulderOffset(actor)*h,h*.64f,h);
            var elbow=StoryStageGeometry.endpoint(x+shoulder.x(),y+shoulder.y(),h*StoryStageGeometry.upperLength(actor),a.upper()+pose.body());
            armLimb(b,parts[left?2:4],elbow.x(),elbow.y(),h*(actor==Actor.CAPYBARA?.105f:.075f),h*StoryStageGeometry.foreLength(actor),a.upper()+a.fore()+pose.body());
        }
        int expression=emotion.ordinal();
        if(speaking && (emotion==Emotion.HAPPY || emotion==Emotion.CALM))expression=((int)(Math.max(0,t)*7)%2==0)?14:15;
        TextureRegion head=heads[actor.ordinal()][expression];
        float headH=h*.32f,headW=headH*head.getRegionWidth()/head.getRegionHeight();
        var neck=bodyPoint(pose,0,h*.68f,h);
        b.draw(head,x+neck.x()-headW/2,y+neck.y()-headH*.08f,headW/2,headH*.08f,headW,headH,1,1,pose.head()+pose.body());
    }
    private static void armLimb(SpriteBatch b,TextureRegion r,float x,float y,float w,float length,float angle) {
        float height=length*1.22f;
        b.draw(r,x-w*.5f,y-height*.86f,w*.5f,height*.86f,w,height,1,1,angle);
    }
    private static void limb(SpriteBatch b,TextureRegion r,float x,float y,float w,float h,float angle) {
        b.draw(r,x-w*.5f,y-h*.92f,w*.5f,h*.92f,w,h,1,1,angle);
    }
    private static void drawSized(SpriteBatch b,TextureRegion r,float center,float baseline,float width,float height) {
        if(r==null)throw new IllegalStateException("Unmapped world object");
        float scale=Math.min(width/r.getRegionWidth(),height/r.getRegionHeight());float w=r.getRegionWidth()*scale,h=r.getRegionHeight()*scale;
        b.draw(r,center-w/2,baseline,w,h);
    }
    private static boolean sky(Prop prop) {
        var spec=StoryWorldCatalog.object(prop);return spec!=null&&spec.kind()==StoryWorldCatalog.Kind.SKY || prop==Prop.CLOUD||prop==Prop.BIRD||prop==Prop.BUTTERFLY||prop==Prop.KITE;
    }
    private static void drawFit(SpriteBatch b,TextureRegion r,float x,float y,float w,float h) {
        if(r==null)throw new IllegalStateException("Unmapped world object");
        float scale=Math.min(w/r.getRegionWidth(),h/r.getRegionHeight());
        float rw=r.getRegionWidth()*scale,rh=r.getRegionHeight()*scale;
        b.draw(r,x+(w-rw)/2,y+(h-rh)/2,rw,rh);
    }
    @Override public void dispose() {
        if(previous!=null)previous.dispose();if(background!=null)background.dispose();
        objectAtlas.dispose();for(TextureAtlas atlas:rigAtlases)atlas.dispose();
        previous=background=null;
    }
}
