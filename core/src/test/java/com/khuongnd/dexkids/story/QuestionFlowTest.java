package com.khuongnd.dexkids.story;

import com.khuongnd.dexkids.story.adventure.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuestionFlowTest {
    private OfflineLearningCatalog sample() throws Exception {
        try (var in = Files.newInputStream(Path.of("..", "docs/learning-source/learning.example.tsv"))) {
            return OfflineLearningCatalog.parse(in);
        }
    }
    @Test void waitsFromSpeechCompletionAndClaimsEachTransitionOnce() {
        var turn = new QuestionTurn(5_000);
        assertTrue(turn.isPromptPending());
        assertFalse(turn.beginAnswer(100_000));
        assertFalse(turn.answerFinished());
        assertTrue(turn.promptFinished(100_000));
        assertFalse(turn.promptFinished(101_000)); // late/duplicate watchdog
        assertFalse(turn.beginAnswer(104_999));
        assertTrue(turn.beginAnswer(105_000));
        assertFalse(turn.beginAnswer(105_001));
        assertTrue(turn.answerFinished());
        assertFalse(turn.answerFinished()); // answer callback vs watchdog race
        for (int i=0; i<100; i++) {
            long wait = new QuestionTurn().responseWaitMs();
            assertTrue(wait>=3_000 && wait<=5_000);
        }
    }
    @Test void shuffledDeckExhaustsAllTopicsAndDoesNotRepeatAtCycleBoundary() throws Exception {
        var catalog = sample();
        for (var kind : OfflineLearningCatalog.Kind.values()) {
            var deck = new LearningDeck(catalog, new Random(42));
            Set<String> heard = new HashSet<>(); String last = "";
            int size = (int)catalog.cards().stream().filter(c -> c.kind()==kind && c.forAge(5)).count();
            for (int i=0; i<size; i++) {
                var card = deck.next(kind, 5, "sea");
                assertTrue(heard.add(card.id())); last = card.id();
            }
            assertNotEquals(last, deck.next(kind, 5, "sea").id());
            var restored = new LearningDeck(catalog, new Random(3));
            restored.restoreHistory(deck.history());
            assertFalse(deck.history().contains(restored.next(kind, 5, "sea").id()));
        }
    }
    @Test void seedsChangeOrderAndHistorySurvivesContextAndAgeChanges() throws Exception {
        var a = new LearningDeck(sample(), new Random(1));
        var b = new LearningDeck(sample(), new Random(2));
        List<String> first = new ArrayList<>(), second = new ArrayList<>();
        for (int i=0; i<7; i++) {
            first.add(a.next(OfflineLearningCatalog.Kind.WORD,4,"").id());
            second.add(b.next(OfflineLearningCatalog.Kind.WORD,4,"").id());
        }
        assertNotEquals(first, second);
        var restored = new LearningDeck(sample(), new Random(1));
        restored.restoreHistory(a.history());
        assertFalse(first.contains(restored.next(OfflineLearningCatalog.Kind.WORD, 5, "sea").id()));
    }
    @Test void questionModeUsesActualChildAgesAndAlternatesVocabulary() throws Exception {
        var director = new EntertainmentDirector(); director.setLearningCatalog(sample());
        for (int i=0; i<12; i++) {
            var beat = director.nextQuestion(EntertainmentDirector.Audience.BOTH,5,4,"");
            assertTrue(beat.id().startsWith("learn-"));
            assertEquals(EntertainmentDirector.Audience.values()[i%3],beat.focus());
            assertEquals(i%2==1, !beat.englishWord().isEmpty());
            var card = sample().cards().stream().filter(c -> ("learn-"+c.id()).equals(beat.id())).findFirst().orElseThrow();
            assertTrue(card.forAge(beat.focus()==EntertainmentDirector.Audience.SAU?5:4));
        }
    }
    @Test void attentionCuesVaryWithoutRepeatsAndFitInsideTwoToFiveSecondGap() {
        var transitions = new QuestionTransitions(new Random(42));
        Set<String> cues = new HashSet<>();
        String last = "";
        for (int i=0;i<6;i++) {
            var cue=transitions.next();
            assertTrue(cues.add(cue.text()));
            assertTrue(cue.gapMs()>=2_000 && cue.gapMs()<=5_000);
            last=cue.text();
        }
        assertNotEquals(last, transitions.next().text());
        assertTrue(cues.contains("Mình tiếp tục nha!"));
        assertTrue(cues.contains("Câu tiếp theo muốn hỏi là…"));
        assertTrue(cues.contains("Vậy tiếp theo là…"));
    }
    @Test void companionDecorationPreservesShuffledCardAndBoundedText() throws Exception {
        var director = new EntertainmentDirector(); director.setLearningCatalog(sample());
        var selected = director.nextQuestion(EntertainmentDirector.Audience.ONG,5,4,"animal");
        var decorated = new OfflineEpisodePlanner().decorate(selected);
        assertEquals(selected.focus(), decorated.focus());
        assertEquals(selected.resolution(), decorated.resolution());
        assertEquals(selected.englishWord(), decorated.englishWord());
        assertTrue(decorated.introduction().endsWith(selected.introduction()));
        assertTrue(decorated.introduction().length() <= 240);
    }
    @Test void malformedEmptyDuplicateAndOversizedExternalPacksAreRejected() throws Exception {
        var file = Files.readString(Path.of("..", "docs/learning-source/learning.example.tsv"));
        String header = file.substring(0, file.indexOf('\n', file.indexOf('\n')+1)+1);
        String row = file.split("\n")[2];
        for (String text : List.of(header, file+row+"\n", file+row.replace("quiz-round-wheel","quiz-another-id")+"\n",
                file.replace("\t4\t6\t", "\t2\t6\t"), "x".repeat(OfflineLearningCatalog.MAX_BYTES+1))) {
            assertThrows(IllegalArgumentException.class, () -> OfflineLearningCatalog.parse(
                new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
        }
        assertEquals(16, sample().size());
    }
    @Test void fallbackStoriesUseProductionValidatorAndTellStoryBeforeRecallForEveryAudience() {
        for (var audience : StoryEpisode.Audience.values()) for (int i=0; i<3; i++) {
            var story = BuiltinInterludes.at(audience,i); StoryEpisodeValidator.validate(story);
            assertEquals(story, BuiltinInterludes.find(story.id(),audience));
            var player = new StoryPlaybackDirector(3,5); player.start(story);
            int narrations=0, questions=0;
            for (int step=0; step<40; step++) {
                var event = player.next();
                if (event instanceof StoryPlaybackDirector.Finished) break;
                if (event instanceof StoryPlaybackDirector.Speech speech) {
                    if (speech.kind()==StoryPlaybackDirector.Kind.NARRATION) narrations++;
                    if (speech.kind()==StoryPlaybackDirector.Kind.QUESTION) {
                        assertTrue(narrations>=6); questions++;
                    }
                    assertTrue(player.speechCompleted(speech.token()));
                } else if (event instanceof StoryPlaybackDirector.Listen listen) {
                    assertEquals(5, listen.maxWaitSeconds());
                    assertFalse(player.tickListening(4.999));
                    assertTrue(player.tickListening(0.002));
                } else fail("stalled story: "+player.state());
            }
            assertEquals(3, questions); assertEquals(StoryPlaybackDirector.State.COMPLETE,player.state());
        }
        assertNull(BuiltinInterludes.find("remote-not-builtin", StoryEpisode.Audience.BOTH));
    }
    @Test void hostWaitBoundsShortenExistingStoriesWithoutEditingContent() {
        var episode=StoryFixtures.educational(StoryEpisode.Audience.SAU);
        var player=new StoryPlaybackDirector(3,5); player.start(episode);
        while (player.state()!=StoryPlaybackDirector.State.LISTENING) {
            var event=player.next();
            if(event instanceof StoryPlaybackDirector.Speech speech) player.speechCompleted(speech.token());
        }
        assertFalse(player.tickListening(3)); assertFalse(player.tickListening(1.9));
        assertTrue(player.tickListening(0.1));
    }
}
