package com.khuongnd.dexkids.story.adventure;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Acceptance contract for the authored public pack, exercised through real playback. */
class PublishedStoryPackTest {
    private List<StoryEpisode> stories() throws Exception {
        return StoryPackCodec.parse(Files.readAllBytes(Path.of("../docs/story-source/stories.example.json")));
    }

    @Test void replacementHasThreeTopicsForEachAudienceAndBoundedNarrationEstimate() throws Exception {
        var episodes = stories();
        assertEquals(9, episodes.size());
        Set<String> expected = new HashSet<>();
        for (var topic : List.of("tortoise-hare", "little-turtle-home", "wolf-seven-kids"))
            for (var audience : StoryEpisode.Audience.values())
                expected.add(topic + "-" + audience.name().toLowerCase(Locale.ROOT));
        assertEquals(expected, new HashSet<>(episodes.stream().map(StoryEpisode::id).toList()));
        for (var episode : episodes) {
            int syllables = 0;
            assertEquals(8, episode.scenes().size());
            for (int i = 0; i < 6; i++) {
                var scene = episode.scenes().get(i);
                for (var beat : scene.beats()) {
                    assertFalse(beat.utterance().contains("?"), episode.id());
                    syllables += beat.utterance().trim().split("\\s+").length;
                }
                if (i < 5) assertNull(scene.question(), episode.id());
            }
            // Vietnamese whitespace units approximate syllables, not lexical words.
            // These are editorial estimates at 220–320 syllables/min, not TTS measurements.
            assertTrue(syllables / 320.0 >= 4, episode.id() + ": too short");
            assertTrue(syllables / 220.0 <= 7, episode.id() + ": too long");
            var finalBeats = episode.scenes().get(5).beats();
            assertTrue(finalBeats.get(finalBeats.size()-1).utterance().contains("đã kết thúc ở đây"));
        }
    }

    @Test void actualPlaybackFinishesNarrativeBeforeQuestionsAndWaitsSixSecondsInSilence() throws Exception {
        for (var episode : stories()) {
            var director = new StoryPlaybackDirector();
            director.start(episode);
            boolean endingSpoken = false;
            int questions = 0, narrationBeforeFirstQuestion = 0;
            List<StoryEpisode.Audience> turns = new ArrayList<>();
            int events = 0;
            while (director.state() != StoryPlaybackDirector.State.COMPLETE) {
                assertTrue(++events < 100, "Playback did not finish: " + episode.id());
                var event = director.next();
                assertNotNull(event);
                if (event instanceof StoryPlaybackDirector.Speech speech) {
                    if (speech.kind() == StoryPlaybackDirector.Kind.QUESTION) {
                        assertTrue(endingSpoken, "Question interrupted story: " + episode.id());
                        assertEquals(36, narrationBeforeFirstQuestion);
                        questions++;
                        turns.add(speech.target());
                    } else if (speech.kind() == StoryPlaybackDirector.Kind.NARRATION) {
                        if (questions == 0) narrationBeforeFirstQuestion++;
                        endingSpoken |= speech.text().contains("đã kết thúc ở đây");
                    }
                    assertNull(director.next(), "Must wait for actual speech completion");
                    assertTrue(director.speechCompleted(speech.token()));
                } else if (event instanceof StoryPlaybackDirector.Listen listen) {
                    assertTrue(endingSpoken);
                    assertEquals(6, listen.maxWaitSeconds());
                    assertFalse(director.tickListening(5));
                    assertEquals(StoryPlaybackDirector.State.LISTENING, director.state());
                    assertNull(director.next(), "Do not speak over the child's answer window");
                    assertFalse(director.tickListening(0.99));
                    assertTrue(director.tickListening(0.01));
                }
            }
            assertEquals(3, questions);
            assertEquals(episode.audience() == StoryEpisode.Audience.BOTH
                ? List.of(StoryEpisode.Audience.SAU, StoryEpisode.Audience.ONG, StoryEpisode.Audience.BOTH)
                : Collections.nCopies(3, episode.audience()), turns);
        }
    }
}
