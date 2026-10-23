package com.khuongnd.dexkids.story.adventure;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import static com.khuongnd.dexkids.story.adventure.StoryEpisode.*;

/** Conservative deterministic gate. Does not replace independent review or adult approval. */
public final class StoryEpisodeValidator {
    private StoryEpisodeValidator() {}
    private static final Pattern PRIVATE = Pattern.compile("https?://|www\\.|@|[0-9]{7,}|[0-9]+\\.[0-9]+\\s*[,;]\\s*-?[0-9]+\\.[0-9]+");
    private static final List<String> BANNED = List.of("so dien thoai", "dia chi nha", "ten day du", "ho ten", "truong con hoc", "mat khau", "gui anh", "gui am thanh", "lien he nguoi la", "dang di qua", "vua di qua", "chung ta dang o", "da den noi", "re trai", "re phai", "xuong xe", "mo cua xe", "dung xe ngay", "nhin thang mat troi", "uong thuoc", "vu khi", "giet", "tu tu", "danh nhau", "khieu dam", "ngu ngoc", "vo dung");
    private static final Pattern BANNED_PATTERN=Pattern.compile("(?<![a-z])(?:" +
        BANNED.stream().map(Pattern::quote).collect(java.util.stream.Collectors.joining("|")) + ")(?![a-z])");
    public static void validate(StoryEpisode e) {
        require(e!=null,"EPISODE_REQUIRED");
        require((e.schemaVersion()==SCHEMA_VERSION || e.schemaVersion()==WORLD_SCHEMA_VERSION),"SCHEMA_UNSUPPORTED");
        require(e.revision()>0 && e.revision()<=100000,"REVISION_INVALID");
        id(e.id()); text(e.title(),3,100); text(e.educationalGoal(),4,180); text(e.synopsis(),8,320);
        require(e.audience()!=null && e.sauAge()>=4 && e.sauAge()<=7 && e.ongAge()>=4 && e.ongAge()<=7,"AUDIENCE_AGE_INVALID");
        require(e.scenes().size()>=4 && e.scenes().size()<=8,"SCENE_COUNT_INVALID");
        Set<String> ids=new HashSet<>(); EnumSet<Audience> turns=EnumSet.noneOf(Audience.class);
        int questions=0; boolean sau=false,ong=false;
        for(Scene s:e.scenes()) {
            require(s!=null,"SCENE_REQUIRED"); id(s.id()); require(ids.add(s.id()),"SCENE_DUPLICATE");
            require(s.backdrop()!=null,"BACKDROP_REQUIRED");
            require(e.schemaVersion()==WORLD_SCHEMA_VERSION || !StoryWorldCatalog.newTheme(s.backdrop()),"V2_REQUIRED");
            require(s.beats().size()>=1 && s.beats().size()<=6,"BEAT_COUNT_INVALID");
            for(Beat b:s.beats()) {
                require(b!=null && b.actor()!=null && b.emotion()!=null && b.action()!=null && b.prop()!=null,"BEAT_INVALID");
                StoryActionCatalog.validate(b.action(),b.prop());
                require(e.schemaVersion()==WORLD_SCHEMA_VERSION ||
                    (b.supportingProps().isEmpty() && !StoryWorldCatalog.newAction(b.action()) &&
                     !StoryWorldCatalog.isNew(b.prop()) && b.emotion().ordinal()<=Emotion.SURPRISED.ordinal()),"V2_REQUIRED");
                require(b.supportingProps().size()<=5,"SUPPORTING_PROP_LIMIT");
                Set<ObjectSlot> slots=EnumSet.noneOf(ObjectSlot.class);
                for(SupportingProp support:b.supportingProps()) {
                    require(support!=null && support.prop()!=null && support.prop()!=com.khuongnd.dexkids.story.VisualCue.Prop.NONE &&
                        support.slot()!=null && slots.add(support.slot()),"SUPPORTING_PROP_INVALID");
                }
                require(b.action()!=com.khuongnd.dexkids.story.VisualCue.Action.COUNT || b.supportingProps().isEmpty(),"COUNT_QUANTITY_UNSUPPORTED");
                // Quantity is not represented in this schema: one sprite cannot illustrate an arbitrary count.
                require(b.action()!=com.khuongnd.dexkids.story.VisualCue.Action.COUNT || b.prop()==com.khuongnd.dexkids.story.VisualCue.Prop.NONE,"COUNT_QUANTITY_UNSUPPORTED");
                text(b.utterance(),4,240);
                String spoken=fold(b.utterance());
                if(spoken.contains("vay tay"))require(b.action()==com.khuongnd.dexkids.story.VisualCue.Action.WAVE,"ACTION_TEXT_MISMATCH");
                if(spoken.contains("nguoc nhin"))require(b.action()==com.khuongnd.dexkids.story.VisualCue.Action.LOOK_UP,"ACTION_TEXT_MISMATCH");
                if(spoken.contains("vuot ve"))require(b.action()==com.khuongnd.dexkids.story.VisualCue.Action.PET,"ACTION_TEXT_MISMATCH");
                require(b.utterance().trim().split("\\s+").length<=48,"BEAT_TOO_WORDY");
                require(e.audience()!=Audience.SAU || b.actor()!=Actor.ONG,"ACTOR_AUDIENCE_MISMATCH");
                require(e.audience()!=Audience.ONG || b.actor()!=Actor.SAU,"ACTOR_AUDIENCE_MISMATCH");
                sau|=b.actor()==Actor.SAU; ong|=b.actor()==Actor.ONG;
            }
            Question q=s.question(); if(q==null)continue;
            questions++; require(q.target()!=null,"QUESTION_TARGET_REQUIRED");
            require(e.audience()==Audience.BOTH || q.target()==e.audience(),"QUESTION_AUDIENCE_MISMATCH");
            turns.add(q.target()); text(q.prompt(),5,180); text(q.hint(),4,180);
            text(q.feedback(),4,180); text(q.silenceFallback(),4,180);
            require(q.prompt().endsWith("?"),"QUESTION_MARK_REQUIRED");
            require(q.acceptedConcepts().size()>=1 && q.acceptedConcepts().size()<=4,"CONCEPT_COUNT_INVALID");
            for(String concept:q.acceptedConcepts())text(concept,1,80);
            require(q.maxWaitSeconds()>=5 && q.maxWaitSeconds()<=30,"QUESTION_WAIT_INVALID");
        }
        require(questions>=2 && questions<=4,"QUESTION_COUNT_INVALID");
        require(e.audience()!=Audience.BOTH || turns.containsAll(EnumSet.allOf(Audience.class)),"BOTH_TURNS_REQUIRED");
        require(e.audience()==Audience.ONG || sau,"SAU_ROLE_REQUIRED");
        require(e.audience()==Audience.SAU || ong,"ONG_ROLE_REQUIRED");
        String opening=fold(e.scenes().get(0).beats().get(0).utterance());
        require(opening.contains("tuong tuong") || opening.contains("truyen ke") || opening.contains("ngay xua"),"FICTION_LABEL_REQUIRED");
    }
    static void text(String value,int min,int max) {
        require(value!=null && value.length()>=min && value.length()<=max,"TEXT_LENGTH_INVALID");
        require(value.codePoints().noneMatch(c->Character.isISOControl(c)),"CONTROL_TEXT_REJECTED");
        String normalized=fold(value);
        require(!PRIVATE.matcher(normalized).find(),"PRIVATE_OR_URL_REJECTED");
        require(!BANNED_PATTERN.matcher(normalized).find(),"SAFETY_REJECTED");
    }
    static String fold(String value) { return Normalizer.normalize(value.toLowerCase(Locale.ROOT).replace('đ','d'),Normalizer.Form.NFD).replaceAll("\\p{M}+",""); }
    static void id(String value) { require(value!=null && value.matches("[a-z][a-z0-9-]{1,63}"),"ID_INVALID"); }
    static void require(boolean ok,String code) { if(!ok)throw new IllegalArgumentException(code); }
}
