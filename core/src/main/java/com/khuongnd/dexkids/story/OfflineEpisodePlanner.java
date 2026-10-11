package com.khuongnd.dexkids.story;

import java.util.Objects;
import java.util.Optional;

/**
 * Bounded, deterministic, offline companion episode assembled only from authored
 * questions and vocabulary. No model output is used as an answer or location fact.
 */
public final class OfflineEpisodePlanner {
    public record Episode(String id, EntertainmentDirector.Audience focus,
                          String opening, String question, String answer,
                          String wordPrompt, String wordAnswer, String englishWord) {
        public Episode {
            Objects.requireNonNull(id);
            Objects.requireNonNull(focus);
            Objects.requireNonNull(opening);
            Objects.requireNonNull(question);
            Objects.requireNonNull(answer);
            Objects.requireNonNull(wordPrompt);
            Objects.requireNonNull(wordAnswer);
            Objects.requireNonNull(englishWord);
        }

        /** Fits the existing TTS -> optional offline English voice -> caption pipeline. */
        public EntertainmentDirector.Beat asBeat() {
            String introduction = opening + " " + question;
            String resolution = answer;
            if (!wordAnswer.isBlank() && (resolution + " " + wordAnswer).length() <= 240)
                resolution += " " + wordAnswer;
            return new EntertainmentDirector.Beat("companion-" + id, focus,
                introduction, resolution, EntertainmentDirector.Reaction.WAVE, englishWord);
        }
    }

    public Optional<Episode> plan(TripContext context, OfflineLearningCatalog catalog,
                                  int turn, long nowElapsedMs) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(catalog);
        if (!context.active(nowElapsedMs) || turn < 0) return Optional.empty();
        var quiz = catalog.select(OfflineLearningCatalog.Kind.QUIZ, context.focusAge(),
            context.topic(), turn + 5);
        if (quiz == null) return Optional.empty();
        var word = catalog.select(OfflineLearningCatalog.Kind.WORD, context.focusAge(),
            context.topic(), turn + 11);
        // This is fictional educational play, even when context arose near a POI.
        String opening = "Capybara có một câu đố vui cho mình.";
        String question = quiz.promptVi();
        if (opening.length() + 1 + question.length() > 240)
            opening = "Câu đố:";
        String wordPrompt = word == null ? "" : word.promptVi();
        String wordAnswer = word == null ? "" : word.answerVi();
        String english = word == null ? "" : word.englishWord();
        return Optional.of(new Episode(quiz.id(), context.focus(), opening,
            question, quiz.answerVi(), wordPrompt, wordAnswer, english));
    }
}
