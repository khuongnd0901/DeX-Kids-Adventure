package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue.Action;

/** Immutable joint keyframes sampled on the existing paused beat clock. Angles from down. */
public final class StoryRigMotion {
    public record Pose(float leftUpper,float leftFore,float rightUpper,float rightFore,
                       float leftLeg,float rightLeg,float head,float x,float y,float body) {}
    private static final Pose[][] TABLE=new Pose[Action.values().length][120];
    static {for(Action action:Action.values())for(int i=0;i<120;i++)TABLE[action.ordinal()][i]=create(action,i/30f);}
    private static Pose create(Action a,float t) {
        float s=(float)Math.sin(t*Math.PI*2),p=(1-(float)Math.cos(t*Math.PI))*0.5f;
        float l=-8,lf=0,r=8,rf=0,ll=0,rl=0,head=0,x=0,y=0,body=0;
        switch(a) {
            case WAVE -> {r=145;rf=s*22;}
            case LOOK_UP -> {head=-10;r=120;rf=-15;}
            case COUNT -> {r=100;rf=25+s*10;}
            case PET -> {r=38;rf=-15+s*12;body=6;}
            case JUMP -> {y=Math.max(0,s)*30;l=-30;r=30;ll=-8;rl=8;}
            case DANCE -> {l=-60+s*25;r=60-s*25;ll=s*12;rl=-s*12;body=s*6;}
            case BOW -> {body=-p*15;head=p*8;y=-p*12;}
            case READ -> {l=-15;lf=100;r=15;rf=-100;head=5;}
            case WATER -> {r=65;rf=25+s*6;body=4;}
            case SHARE -> {r=55+p*10;rf=30;}
            case INSPECT -> {r=120;rf=15;head=-5;}
            case WALK -> {ll=s*18;rl=-s*18;l=-s*15;r=s*15;y=Math.abs(s)*3;}
            case RUN -> {ll=s*34;rl=-s*34;l=-s*26;r=s*26;lf=-40;rf=40;y=Math.abs(s)*10;}
            case SIT -> {ll=75;rl=75;y=-25;lf=-20;rf=20;}
            case THINK -> {r=140;rf=25;head=-7;}
            case POINT -> {r=95;rf=0;head=-3;}
            case NOD -> head=s*9;
            case SHAKE_HEAD -> {head=s*7;x=s*3;}
            case CLAP -> {l=-15;lf=82+p*22;r=15;rf=-82-p*22;}
            case PICK_UP -> {body=(1-p)*15;y=-(1-p)*30;r=20;rf=p*50;head=(1-p)*10;}
            case PUT_DOWN -> {body=p*15;y=-p*30;r=12;rf=-(1-p)*80;}
            case CARRY -> {l=-15;lf=95;r=15;rf=-95;}
            case DRAW -> {r=45;rf=45+s*10;head=7;}
            case EAT, DRINK -> {r=15-p*50;rf=-90-p*45;head=p*3;}
            case TALK -> {r=25+s*12;rf=25;y=s;}
            case IDLE -> {}
        }
        return new Pose(l,lf,r,rf,ll,rl,head,x,y,body);
    }
    public static Pose sample(Action action,float elapsed) {
        int i=Float.isFinite(elapsed)?(int)((Math.max(0d,elapsed)%4d)*30)%120:0;
        return TABLE[action.ordinal()][i];
    }
    private StoryRigMotion(){}
}
