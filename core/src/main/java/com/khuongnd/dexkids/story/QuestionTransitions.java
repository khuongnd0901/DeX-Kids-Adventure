package com.khuongnd.dexkids.story;

import java.util.*;

/** Short neutral transitions played inside the 2–5 second gap, never another question. */
public final class QuestionTransitions {
    public record Transition(String text, long gapMs) {}
    private static final List<String> LINES = List.of("Mình tiếp tục nha!", "Câu tiếp theo muốn hỏi là…", "Vậy tiếp theo là…",
        "Cùng khám phá câu tiếp theo nhé!", "Mình thử câu tiếp theo nào!", "Sẵn sàng cho câu tiếp theo nha!");
    private final Random random;
    private final List<String> deck = new ArrayList<>();
    private String last = "";
    public QuestionTransitions() { this(new Random()); }
    public QuestionTransitions(Random random) { this.random=Objects.requireNonNull(random); }
    public Transition next() {
        if (deck.isEmpty()) {
            deck.addAll(LINES); Collections.shuffle(deck,random);
            if(deck.get(0).equals(last))Collections.swap(deck,0,1);
        }
        last=deck.remove(0);
        return new Transition(last, (2L+random.nextInt(4))*1_000L);
    }
}
