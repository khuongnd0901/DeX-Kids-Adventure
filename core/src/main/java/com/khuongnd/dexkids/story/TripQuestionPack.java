package com.khuongnd.dexkids.story;

import java.text.Normalizer;
import java.util.*;

/** T-030: AI selects IDs only. Verified Vietnamese questions/answers and English words remain local. */
public final class TripQuestionPack {
    public record Item(String quizId, String wordId, EntertainmentDirector.Audience focus) {
        public Item {
            if (quizId == null || !quizId.matches("quiz-[a-z0-9-]{3,60}") ||
                wordId == null || !wordId.matches("word-[a-z0-9-]{3,60}") || focus == null)
                throw new IllegalArgumentException("Invalid curated lesson reference");
        }
    }

    private final List<Item> items;
    private TripQuestionPack(List<Item> items) { this.items = List.copyOf(items); }
    public List<Item> items() { return items; }

    public static TripQuestionPack validate(List<Item> proposed, OfflineLearningCatalog catalog,
                                           EntertainmentDirector.Audience audience) {
        Objects.requireNonNull(proposed);
        Objects.requireNonNull(catalog);
        Objects.requireNonNull(audience);
        if (proposed.size() < 15 || proposed.size() > 20) throw new IllegalArgumentException("Pack must have 15–20 cards");
        Map<String,OfflineLearningCatalog.Card> known = new HashMap<>();
        for (var card : catalog.cards()) known.put(card.id(), card);
        Set<String> ids = new HashSet<>();
        Set<String> questions = new HashSet<>();
        Set<String> topics = new HashSet<>();
        int pos = 0;
        for (Item item : proposed) {
            var question = known.get(item.quizId());
            var word = known.get(item.wordId());
            if (question == null || question.kind() != OfflineLearningCatalog.Kind.QUIZ ||
                word == null || word.kind() != OfflineLearningCatalog.Kind.WORD)
                throw new IllegalArgumentException("Unapproved quiz or word ID");
            var expectedFocus = audience == EntertainmentDirector.Audience.BOTH
                    ? switch (pos % 3) {
                        case 0 -> EntertainmentDirector.Audience.SAU;
                        case 1 -> EntertainmentDirector.Audience.ONG;
                        default -> EntertainmentDirector.Audience.BOTH;
                    } : audience;
            int age = expectedFocus.age();
            if (item.focus() != expectedFocus || !question.forAge(age) || !word.forAge(age) ||
                !question.topic().equals(word.topic()) ||
                !ids.add(question.id()) || !questions.add(normalize(question.promptVi())))
                throw new IllegalArgumentException("Duplicate/unsafe/age-incorrect card");
            topics.add(question.topic());
            pos++;
        }
        if (topics.size() < 4) throw new IllegalArgumentException("Insufficient topic diversity");
        return new TripQuestionPack(proposed);
    }

    public static String normalize(String text) {
        return Normalizer.normalize(text.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFKC)
                .replaceAll("\\s+", " ");
    }
}
