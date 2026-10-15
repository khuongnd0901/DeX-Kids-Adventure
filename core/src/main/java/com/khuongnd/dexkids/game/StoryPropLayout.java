package com.khuongnd.dexkids.game;

/** FullHD activity objects, independent of the small scrolling background props. */
public final class StoryPropLayout {
    public static final float PET_WIDTH = 220f, PET_HEIGHT = 254f;
    public static final float FLOWER_WIDTH = 180f, FLOWER_HEIGHT = 201f;
    public static final float COUNT_WIDTH = 80f, COUNT_HEIGHT = 101f;
    /** Story-only spatial contract. Coordinates relative to the existing prop anchor. */
    public record Placement(boolean visible, float xOffset, float y, float width, float height,
                            boolean treeBehindFlower) {}
    private static final Placement HIDDEN = new Placement(false,0,0,0,0,false);
    public static Placement directed(com.khuongnd.dexkids.story.VisualCue.Action action,
                                     com.khuongnd.dexkids.story.VisualCue.Prop prop, boolean both) {
        // No numeric quantity exists in the episode schema. A concrete counted illustration
        // would invent an answer; parent text edits cannot turn it into a verified count.
        if(prop==com.khuongnd.dexkids.story.VisualCue.Prop.NONE ||
                action==com.khuongnd.dexkids.story.VisualCue.Action.COUNT)return HIDDEN;
        if(action==com.khuongnd.dexkids.story.VisualCue.Action.LOOK_UP) {
            return switch(prop) {
                case FLOWER -> both ? LOOK_FLOWER_BOTH : LOOK_FLOWER_SOLO;
                case TREE -> both ? LOOK_TREE_BOTH : LOOK_TREE_SOLO;
                case BIRD,BUTTERFLY,CLOUD,KITE -> both ? LOOK_SKY_BOTH : LOOK_SKY_SOLO;
                default -> HIDDEN; // Do not put a boat, car or pet in the sky.
            };
        }
        return switch(prop) {
            case CAT,RABBIT -> PET;
            case FLOWER -> FLOWER;
            case CLOUD -> CLOUD;
            case BIRD,BUTTERFLY,KITE -> SKY;
            default -> GROUND;
        };
    }
    private static final Placement LOOK_FLOWER_BOTH=new Placement(true,66,800,120,134,true);
    private static final Placement LOOK_FLOWER_SOLO=new Placement(true,220,800,120,134,true);
    private static final Placement LOOK_TREE_BOTH=new Placement(true,30,380,180,520,false);
    private static final Placement LOOK_TREE_SOLO=new Placement(true,185,380,180,520,false);
    private static final Placement LOOK_SKY_BOTH=new Placement(true,66,805,105,100,false);
    private static final Placement LOOK_SKY_SOLO=new Placement(true,220,805,105,100,false);
    private static final Placement PET=new Placement(true,8.5f,292,PET_WIDTH,PET_HEIGHT,false);
    private static final Placement FLOWER=new Placement(true,28.5f,292,FLOWER_WIDTH,FLOWER_HEIGHT,false);
    private static final Placement CLOUD=new Placement(true,47.5f,740,142,68,false);
    private static final Placement SKY=new Placement(true,66,740,105,112,false);
    private static final Placement GROUND=new Placement(true,66,292,105,112,false);
    private StoryPropLayout() {}
}
