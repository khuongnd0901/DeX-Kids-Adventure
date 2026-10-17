package com.khuongnd.dexkids.story.adventure;

import com.khuongnd.dexkids.story.VisualCue.*;
import java.util.*;

/** Bundled capabilities v2. Existing schema-v1 enums and legacy combinations remain readable. */
public final class StoryActionCatalog {
    private StoryActionCatalog() {}
    private static final Set<Prop> SHARE = Set.of(Prop.BOOK,Prop.BALL,Prop.GIFT,Prop.APPLE,Prop.BASKET,Prop.FLOWER);
    private static final Set<Prop> INSPECT = Set.of(Prop.LEAF,Prop.SHELL,Prop.STAR,Prop.FLOWER);
    public static boolean attached(Action action) {
        return action==Action.READ || action==Action.WATER || action==Action.SHARE || action==Action.INSPECT;
    }
    public static boolean compatible(Action action,Prop prop) {
        if(action==null || prop==null)return false;
        return switch(action) {
            case PICK_UP, PUT_DOWN, CARRY -> StoryWorldCatalog.portable(prop);
            case DRAW -> prop==Prop.NOTEBOOK;
            case EAT -> Set.of(Prop.APPLE,Prop.BANANA,Prop.ORANGE,Prop.CARROT,Prop.BREAD,Prop.RICE).contains(prop);
            case DRINK -> Set.of(Prop.CUP,Prop.WATER_BOTTLE,Prop.MILK).contains(prop);
            case SIT -> prop==Prop.NONE || prop==Prop.CHAIR || prop==Prop.BENCH;
            case COUNT -> prop==Prop.NONE;
            case READ -> prop==Prop.BOOK;
            case WATER -> prop==Prop.SEEDLING || prop==Prop.FLOWER;
            case SHARE -> SHARE.contains(prop);
            case INSPECT -> INSPECT.contains(prop);
            // Preserve all previously accepted combinations. New authoring guidance is stricter.
            default -> true;
        };
    }
    public static Action fromWords(String text,Prop prop,Action fallback) {
        Action inferred=text.contains("nhay mua") || text.contains("nhun nhay")?Action.DANCE:
            text.contains("nhay len") || text.contains("nhay mot nhip")?Action.JUMP:
            text.contains("cui chao")?Action.BOW:text.contains("doc sach")?Action.READ:
            text.contains("tuoi cay") || text.contains("tuoi hoa")?Action.WATER:
            text.contains("kinh lup")?Action.INSPECT:
            text.contains("chia se") || text.contains("tang qua")?Action.SHARE:fallback;
        return compatible(inferred,prop)?inferred:fallback;
    }
    public static void validate(Action action,Prop prop) {
        if(!compatible(action,prop))throw new IllegalArgumentException("ACTION_PROP_INCOMPATIBLE");
    }
    public static String promptCatalog() {
        return "Bundled story capabilities v2. Actions="+Arrays.toString(Action.values())+
            "; props="+Arrays.toString(Prop.values())+"; themes="+Arrays.toString(Theme.values())+". "+
            "Choose cues that match the spoken story; do not promise arbitrary scenes, colors or object quantities. "+
            "JUMP is a small in-story character hop; DANCE is rhythmic body sway; BOW is a gentle body bow. "+
            "READ requires BOOK and shows an open book. WATER requires SEEDLING or FLOWER and shows a watering can/drops. "+
            "SHARE requires BOOK,BALL,GIFT,APPLE,BASKET or FLOWER and offers one item. "+
            "INSPECT requires LEAF,SHELL,STAR or FLOWER and shows a magnifying glass. "+
            "COUNT always requires NONE and is verbal counting only. "+
            "WAVE is greeting; LOOK_UP pairs with BIRD,BUTTERFLY,CLOUD,KITE,TREE (or flower on a branch); PET pairs with CAT/RABBIT. "+
            "FOX,PANDA,PENGUIN,DUCK are supporting illustrated companions, not new speaking actors. "+
            "Only SAU,ONG,CAPYBARA are actor values. Characters use existing poses plus bounded motion/objects; no invented limb animations. "+
            "Never ask the real child to stand, jump or move around a vehicle. Actions happen to characters in fictional stories. "+
            "Questions use neutral visuals: ask about narration, not an object assumed to remain visible." + StoryWorldCatalog.promptCatalog();
    }
}
