package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualCue.*;
import com.khuongnd.dexkids.story.adventure.StoryEpisode.Actor;

/** Art-direction units: every measurement is relative to standing character height. */
public final class StoryStageGeometry {
    public record Point(float x,float y) {}
    public record Size(float width,float height) {}
    public record Arm(float upper,float fore) {}
    public static final float FLOOR=245, CHILD_HEIGHT=500;
    public static float height(Actor actor){return actor==Actor.CAPYBARA?540:actor==Actor.SAU?500:465;}
    public static float torsoWidth(Actor actor){return actor==Actor.CAPYBARA?.34f:actor==Actor.ONG?.40f:.27f;}
    public static float shoulderOffset(Actor actor){return actor==Actor.ONG?.105f:torsoWidth(actor)*.42f;}
    public static float upperLength(Actor actor){return actor==Actor.CAPYBARA?.15f:.135f;}
    public static float foreLength(Actor actor){return actor==Actor.CAPYBARA?.16f:.15f;}
    public static Point rotate(float x,float y,float angle){double r=Math.toRadians(angle);return new Point((float)(x*Math.cos(r)-y*Math.sin(r)),(float)(x*Math.sin(r)+y*Math.cos(r)));}
    public static Point endpoint(float x,float y,float length,float angle){double r=Math.toRadians(angle);return new Point(x+(float)Math.sin(r)*length,y-(float)Math.cos(r)*length);}
    /** Two-bone reach, clamped inside the real limb envelope. Left/right elbows bend outward. */
    public static Arm reach(float dx,float dy,float upper,float fore,boolean left) {
        double distance=Math.max(Math.abs(upper-fore)+.001,Math.min(Math.hypot(dx,dy),upper+fore-.001));
        double base=Math.toDegrees(Math.atan2(dx,-dy));
        double shoulder=Math.toDegrees(Math.acos(clamp((distance*distance+upper*upper-fore*fore)/(2*distance*upper))));
        double elbow=180-Math.toDegrees(Math.acos(clamp((upper*upper+fore*fore-distance*distance)/(2*upper*fore))));
        int side=left?1:-1;return new Arm((float)(base-side*shoulder),(float)(side*elbow));
    }
    private static double clamp(double value){return Math.max(-1,Math.min(1,value));}
    /** Bounding envelopes, never an arbitrary shared icon size. Furniture and animals keep their roles. */
    public static Size size(Prop prop,float characterHeight) {
        Size ratio=switch(prop) {
            case NONE -> new Size(0,0);
            case CUP -> new Size(.105f,.115f);case WATER_BOTTLE,MILK -> new Size(.10f,.21f);
            case SPOON,TOOTHBRUSH,PENCIL -> new Size(.035f,.19f);case ERASER -> new Size(.065f,.035f);
            case APPLE,ORANGE -> new Size(.105f,.105f);case BANANA,CARROT -> new Size(.19f,.08f);
            case BREAD -> new Size(.20f,.12f);case RICE,BOWL -> new Size(.21f,.12f);
            case BOOK,NOTEBOOK -> new Size(.29f,.23f);case CRAYONS -> new Size(.16f,.17f);case RULER -> new Size(.035f,.30f);
            case TEDDY_BEAR,DOLL -> new Size(.24f,.33f);case BACKPACK -> new Size(.30f,.36f);
            case BALL -> new Size(.22f,.22f);case GIFT,BASKET -> new Size(.29f,.24f);
            case WATERING_CAN -> new Size(.32f,.25f);case SEEDLING,FLOWER -> new Size(.20f,.26f);
            case CHAIR -> new Size(.54f,.62f);case BENCH -> new Size(.95f,.58f);
            case TABLE -> new Size(.95f,.56f);case BED -> new Size(1.25f,.45f);
            case PILLOW -> new Size(.35f,.20f);case BLANKET -> new Size(.90f,.63f);case LAMP -> new Size(.22f,.39f);
            case BIN,RECYCLE_BIN -> new Size(.34f,.50f);case BICYCLE,SCOOTER -> new Size(.85f,.72f);
            case GOAL -> new Size(1.60f,1.15f);case HOOP -> new Size(.58f,.58f);case JUMP_ROPE -> new Size(.45f,.60f);
            case HELMET -> new Size(.29f,.21f);case TOY_TRAIN -> new Size(.42f,.21f);case BLOCKS,PUZZLE -> new Size(.34f,.20f);
            case DOG,FOX -> new Size(.55f,.45f);case CAT,RABBIT -> new Size(.33f,.32f);case TURTLE -> new Size(.26f,.13f);
            case ELEPHANT -> new Size(1.50f,1.45f);case MONKEY -> new Size(.43f,.55f);case PANDA -> new Size(.75f,.85f);
            case PENGUIN -> new Size(.30f,.42f);case DUCK -> new Size(.29f,.25f);case SNAIL -> new Size(.12f,.08f);case FISH -> new Size(.26f,.14f);
            case TREE -> new Size(1.1f,2.3f);case HOUSE -> new Size(3.2f,3.0f);case SCHOOL -> new Size(4.5f,3.0f);case BRIDGE -> new Size(4.0f,1.0f);
            case VEHICLE -> new Size(3.2f,1.5f);case BOAT -> new Size(2.7f,1.1f);case TELESCOPE -> new Size(.55f,.80f);
            case MAGNIFYING_GLASS -> new Size(.16f,.24f);case THERMOMETER -> new Size(.045f,.20f);
            case COMPASS,MAGNET -> new Size(.13f,.13f);case GLOBE -> new Size(.28f,.38f);case CLOCK -> new Size(.30f,.30f);
            case LEAF,SHELL,ROCK,MUSHROOM -> new Size(.14f,.12f);case UMBRELLA -> new Size(.80f,.85f);
            case BIRD -> new Size(.22f,.18f);case BUTTERFLY -> new Size(.13f,.11f);case KITE -> new Size(.38f,.45f);
            case CLOUD -> new Size(.90f,.40f);case SUN,MOON -> new Size(.34f,.34f);case RAINBOW -> new Size(1.0f,.48f);
            case RAIN -> new Size(.75f,.55f);case BUBBLES -> new Size(.35f,.45f);case STAR -> new Size(.12f,.12f);
            case TOWEL -> new Size(.30f,.45f);
        };
        return new Size(ratio.width*characterHeight,ratio.height*characterHeight);
    }
    private StoryStageGeometry(){}
}
