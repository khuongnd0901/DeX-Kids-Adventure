package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.story.VisualPlan;
import com.khuongnd.dexkids.story.VisualCue;
import java.util.EnumSet;
import java.util.Objects;

/** Visual-only capability registry, never permits external or invented actions. */
public final class AnimationRegistry {
    /** Authored channels; head/hand channels require the matching rig, never rotate a whole child. */
    public enum Channel { BREATH, BLINK, HAND, HEAD, POINT, CLAP, BOUNCE,
        SURPRISE, WHEEL, SUSPENSION, SWAY, WIND, RIPPLE, DRIFT }
    public enum Profile {
        CAP_IDLE("CAP-AN01",Channel.BREATH,3,.015f,true),
        CAP_BLINK("CAP-AN02",Channel.BLINK,3,8,true),
        CAP_WAVE("CAP-AN03",Channel.HAND,2,12,true),
        CAP_THINK("CAP-AN04",Channel.HEAD,3,6,true),
        CAP_POINT("CAP-AN05",Channel.POINT,2,1,false),
        CAP_CLAP("CAP-AN06",Channel.CLAP,2,1,false),
        CAP_LISTEN("CAP-AN07",Channel.HEAD,3,5,true),
        CAP_PRAISE("CAP-AN08",Channel.BOUNCE,2,10,false),
        SAU_IDLE("SAU-AN01",Channel.BREATH,3.3f,.015f,true),
        SAU_WAVE("SAU-AN02",Channel.HAND,2.4f,12,true),
        SAU_THINK("SAU-AN03",Channel.HEAD,2.8f,6,true),
        SAU_CLAP("SAU-AN04",Channel.CLAP,2,1,false),
        SAU_SURPRISE("SAU-AN05",Channel.SURPRISE,1.7f,.05f,false),
        SAU_BLINK("SAU-AN06",Channel.BLINK,4,8,true),
        ONG_IDLE("ONG-AN01",Channel.BREATH,3.4f,.015f,true),
        ONG_WAVE("ONG-AN02",Channel.HAND,2.2f,12,true),
        ONG_THINK("ONG-AN03",Channel.HEAD,2.9f,6,true),
        ONG_CLAP("ONG-AN04",Channel.CLAP,2,1,false),
        ONG_SURPRISE("ONG-AN05",Channel.SURPRISE,1.7f,.05f,false),
        ONG_BLINK("ONG-AN06",Channel.BLINK,3,8,true),
        VEH_WHEEL("VEH-AN01",Channel.WHEEL,1,1,true),
        VEH_SUSPENSION("VEH-AN02",Channel.SUSPENSION,1.8f,3,true),
        VEH_SWAY("VEH-AN03",Channel.SWAY,4,3,true),
        ENV_WIND("ENV-AN01",Channel.WIND,5,5.5f,true),
        ENV_RIPPLE("ENV-AN02",Channel.RIPPLE,6,2,true),
        ENV_DRIFT("ENV-AN03",Channel.DRIFT,10,25,true);

        public final String id;
        public final Channel channel;
        public final float period, maxPeriod, amplitude;
        public final boolean loop;
        Profile(String id, Channel channel, float period, float amplitude, boolean loop) {
            this.id=id;this.channel=channel;this.period=period;this.loop=loop;
            boolean interval=channel==Channel.BLINK || channel==Channel.DRIFT;
            this.maxPeriod=interval?amplitude:period;
            this.amplitude=interval?(channel==Channel.BLINK?1:12):amplitude;
        }
        /** Allocation-free channel sample; wheel angle uses absolute distance, not elapsed time. */
        public float sample(double seconds, long seed) {
            if (!Double.isFinite(seconds) || seconds<0) throw new IllegalArgumentException("seconds");
            if(channel==Channel.WHEEL) throw new IllegalStateException("Use existing VehicleMotionModel");
            double cycle=period;
            if(channel==Channel.BLINK || channel==Channel.DRIFT)
                cycle=period+(maxPeriod-period)*Math.floorMod(seed,1001)/1000.0;
            if(!loop && seconds>=cycle) return channel==Channel.SURPRISE?1:0;
            double t=(seconds%cycle)/cycle;
            return switch(channel) {
                case BLINK -> t>=1-.13/cycle?1:0;
                case BREATH -> 1+amplitude*(float)Math.sin(t*Math.PI*2);
                case SURPRISE -> 1+amplitude*(float)Math.sin(t*Math.PI);
                case POINT -> t<.2?(float)(t/.2):t>.8?(float)((1-t)/.2):1;
                case CLAP -> (float)Math.pow(Math.sin(t*Math.PI*2),2);
                case BOUNCE -> amplitude*(float)Math.sin(t*Math.PI);
                default -> amplitude*(float)Math.sin(t*Math.PI*2);
            };
        }
    }
    /** Render resources are finite and local. Correct/try-again require actual caller evidence. */
    public enum Effect {
        WELCOME_WAVE(1.4f), QUESTION_POP(1.1f), DISCOVERY_SPARKLE(1.6f),
        ANSWER_CORRECT_STAR(1.5f), ANSWER_TRY_AGAIN_SOFT(1.2f),
        ENGLISH_WORD_REVEAL(1.6f), MILESTONE_CONFETTI(1.8f), SCENE_TRANSITION_SOFT(2.5f);
        public final float duration;
        Effect(float duration) { this.duration=duration; }
    }
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
