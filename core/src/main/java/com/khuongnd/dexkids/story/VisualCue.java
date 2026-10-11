package com.khuongnd.dexkids.story;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * M2: finite, authored art vocabulary. AI may invent unlimited stories and questions,
 * but NEVER return arbitrary texture paths, coordinates, shader code, or GPS assertions.
 * Convert source content into vetted sprite/pose keys once per lesson, not per render frame.
 */
public record VisualCue(Theme theme, Action action, Prop prop) {
    public enum Theme {
        ANIMALS, SKY, NATURE, WATER, TRANSPORT, COUNTING, COLORS, LANGUAGE, STORY, GENERAL
    }
    public enum Action { WAVE, LOOK_UP, COUNT, PET }
    public enum Prop { NONE, BIRD, BUTTERFLY, CAT, RABBIT, CLOUD, FLOWER, TREE, BOAT, VEHICLE, KITE }

    public VisualCue {
        Objects.requireNonNull(theme);
        Objects.requireNonNull(action);
        Objects.requireNonNull(prop);
    }

    public static final VisualCue DEFAULT =
        new VisualCue(Theme.GENERAL, Action.WAVE, Prop.NONE);

    /** Human-facing label is local, never directly authored by the AI. */
    public String labelVi() {
        return switch (theme) {
            case ANIMALS -> "ĐỘNG VẬT";
            case SKY -> "BẦU TRỜI";
            case NATURE -> "THIÊN NHIÊN";
            case WATER -> "SÔNG BIỂN";
            case TRANSPORT -> "PHƯƠNG TIỆN";
            case COUNTING -> "ĐẾM SỐ";
            case COLORS -> "MÀU SẮC";
            case LANGUAGE -> "TIẾNG ANH";
            case STORY -> "KỂ CHUYỆN";
            default -> "KHÁM PHÁ";
        };
    }

    public static VisualCue fromCreative(CreativeLessonPack.Lesson lesson) {
        Objects.requireNonNull(lesson);
        return resolve(lesson.topic(), lesson.prompt(), lesson.englishWord(),
            lesson.genre().name());
    }

    public static VisualCue fromAuthored(String id, String prompt, String englishWord) {
        return resolve(id == null ? "" : id,
            prompt == null ? "" : prompt,
            englishWord == null ? "" : englishWord, "");
    }

    private static String fold(String value) {
        String s = Normalizer.normalize(value.toLowerCase(Locale.ROOT).replace('đ','d'),
            Normalizer.Form.NFD);
        return s.replaceAll("\\p{M}+", "");
    }

    /**
     * Prefer concrete items mentioned in a question, not vague AI topic labels.
     * Conservative result when there is no matching approved illustration.
     */
    private static VisualCue resolve(String topic, String prompt, String english, String genre) {
        String words = " " + fold(prompt) + " ";
        String tags = " " + fold(topic) + " " + fold(english) + " ";
        Prop prop;
        if (has(words,"buom","butterfly")) prop=Prop.BUTTERFLY;
        else if (has(words,"chim","bird")) prop=Prop.BIRD;
        else if (has(words,"meo","cat")) prop=Prop.CAT;
        else if (has(words,"tho","rabbit")) prop=Prop.RABBIT;
        else if (has(words,"may","cloud")) prop=Prop.CLOUD;
        else if (has(words,"hoa","flower")) prop=Prop.FLOWER;
        else if (has(words,"cay","tree")) prop=Prop.TREE;
        else if (has(words,"thuyen","boat","ship")) prop=Prop.BOAT;
        else if (has(words,"xe buyt","xe tai","xe hoi","car","bus")) prop=Prop.VEHICLE;
        else if (has(words,"dieu","kite")) prop=Prop.KITE;
        else if (has(tags,"butterfly","bird","cat","rabbit","cloud","flower",
                "tree","boat","car","bus","kite")) {
            if (has(tags,"butterfly")) prop=Prop.BUTTERFLY;
            else if (has(tags,"bird")) prop=Prop.BIRD;
            else if (has(tags,"cat")) prop=Prop.CAT;
            else if (has(tags,"rabbit")) prop=Prop.RABBIT;
            else if (has(tags,"cloud")) prop=Prop.CLOUD;
            else if (has(tags,"flower")) prop=Prop.FLOWER;
            else if (has(tags,"tree")) prop=Prop.TREE;
            else if (has(tags,"boat")) prop=Prop.BOAT;
            else if (has(tags,"kite")) prop=Prop.KITE;
            else prop=Prop.VEHICLE;
        } else prop=Prop.NONE;

        boolean counting = has(words,"dem","bao nhieu","may cai","mot hai","one two") ||
            has(tags,"count","numbers","math");
        boolean coloring = has(words,"mau","do","xanh","vang") && !counting ||
            has(tags,"color","colours");
        boolean englishLesson = "ENGLISH".equals(genre) ||
            has(tags,"english","language","vocabulary");
        boolean imaginary = "STORY".equals(genre);
        Theme theme;
        if (counting) theme=Theme.COUNTING;
        else if (coloring) theme=Theme.COLORS;
        else if (prop==Prop.CAT || prop==Prop.RABBIT ||
                prop==Prop.BIRD || prop==Prop.BUTTERFLY) theme=Theme.ANIMALS;
        else if (prop==Prop.CLOUD || prop==Prop.KITE ||
                has(tags,"weather","sky")) theme=Theme.SKY;
        else if (prop==Prop.BOAT || has(tags,"sea","water","river")) theme=Theme.WATER;
        else if (prop==Prop.VEHICLE || has(tags,"road","transport")) theme=Theme.TRANSPORT;
        else if (prop==Prop.FLOWER || prop==Prop.TREE ||
                has(tags,"nature","garden","plants")) theme=Theme.NATURE;
        else if (englishLesson) theme=Theme.LANGUAGE;
        else if (imaginary) theme=Theme.STORY;
        else theme=Theme.GENERAL;

        Action action = counting ? Action.COUNT :
            (prop==Prop.CLOUD || prop==Prop.BIRD || prop==Prop.KITE) ? Action.LOOK_UP :
            (prop==Prop.CAT || prop==Prop.RABBIT || prop==Prop.BUTTERFLY) ?
                Action.PET : Action.WAVE;
        return new VisualCue(theme,action,prop);
    }

    private static boolean has(String text, String... terms) {
        for (String term : terms) {
            // Match whole words/phrases to avoid 'do' matching e.g. 'dong'.
            if (text.contains(" " + term + " ") || text.contains(" " + term + ",") ||
                text.contains(" " + term + ".") || text.contains(" " + term + "?") ||
                text.contains(" " + term + "!")) return true;
        }
        return false;
    }
}
