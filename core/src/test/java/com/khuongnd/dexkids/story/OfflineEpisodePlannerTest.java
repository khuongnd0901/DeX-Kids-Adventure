package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class OfflineEpisodePlannerTest {
    private OfflineLearningCatalog sample() {
        String source = "# dexkids-offline-learning-v1\n" +
            "id\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish\n" +
            "sea-quiz1\tQUIZ\tsea\t3\t6\tBiển có sóng hay núi nhỉ?\tBiển có những con sóng vui.\t\n" +
            "sea-word1\tWORD\tsea\t3\t6\tMình học từ tiếng Anh về sóng nhé!\tWave nghĩa là con sóng.\twave\n";
        return OfflineLearningCatalog.parse(
            new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
    }

    @Test void yieldsOfflineAuthorApprovedQuestionAndEnglish() {
        var context = TripContextResolver.fictionalScene(
            EntertainmentDirector.Audience.ONG, "sea", 100, 0);
        var episode = new OfflineEpisodePlanner().plan(context, sample(), 0, 101).orElseThrow();
        assertEquals("Biển có sóng hay núi nhỉ?", episode.question());
        assertEquals("wave", episode.englishWord());
        assertEquals(EntertainmentDirector.Audience.ONG, episode.asBeat().focus());
        assertTrue(episode.asBeat().introduction().contains(episode.question()));
        assertTrue(episode.asBeat().resolution().contains("Biển có những con sóng vui."));
    }

    @Test void returnsNoneWhenExpiredOrNoOfflineCards() {
        var context = TripContextResolver.fictionalScene(
            EntertainmentDirector.Audience.SAU, "sea", 100, 0);
        var planner = new OfflineEpisodePlanner();
        assertTrue(planner.plan(context, sample(), 1, 60_100).isEmpty());
        assertTrue(planner.plan(context, OfflineLearningCatalog.empty(), 1, 102).isEmpty());
    }
}
