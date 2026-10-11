package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualPlan;
import com.khuongnd.dexkids.story.VisualCue;
import java.util.EnumSet;
import java.util.Objects;

/** Visual-only capability registry, never permits external or invented actions. */
public final class AnimationRegistry {
    public enum Actor { CAPYBARA, SAU, ONG, ILLUSTRATION }
    private static final EnumSet<VisualPlan.Motion> CHARACTER =
            EnumSet.of(VisualPlan.Motion.IDLE,VisualPlan.Motion.WAVE,
                    VisualPlan.Motion.LOOK_UP,VisualPlan.Motion.COUNT,
                    VisualPlan.Motion.PET,VisualPlan.Motion.BOUNCE,
                    VisualPlan.Motion.CELEBRATE);
    public boolean supports(Actor actor, VisualPlan.Motion motion, VisualCue.Prop prop) {
        Objects.requireNonNull(actor);
        Objects.requireNonNull(motion);
        Objects.requireNonNull(prop);
        if(actor!=Actor.ILLUSTRATION)return CHARACTER.contains(motion);
        if(prop==VisualCue.Prop.NONE)return motion==VisualPlan.Motion.IDLE;
        return switch(motion) {
            case FLY -> prop==VisualCue.Prop.BIRD||prop==VisualCue.Prop.BUTTERFLY||prop==VisualCue.Prop.KITE;
            case FLOAT -> prop==VisualCue.Prop.CLOUD;
            case SAIL -> prop==VisualCue.Prop.BOAT;
            case DRIVE -> prop==VisualCue.Prop.VEHICLE;
            case GROW -> prop==VisualCue.Prop.TREE||prop==VisualCue.Prop.FLOWER;
            case BOUNCE -> prop==VisualCue.Prop.RABBIT||prop==VisualCue.Prop.CAT;
            case CELEBRATE, IDLE -> true;
            default -> false;
        };
    }
}
