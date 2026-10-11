package com.khuongnd.dexkids.story;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** BOTH mode uses each child’s own age, with shared content at the youngest age. */
final class AudienceAgeGateTest {
    private static OfflineLearningCatalog syntheticPack() {
        String text = "# dexkids-offline-learning-v1\n"
          + "id\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish\n"
          + "quiz-five\tQUIZ\tsea\t5\t6\tCâu hỏi riêng cho bé năm tuổi?\tĐáp án cho bé năm tuổi.\t\n"
          + "word-four\tWORD\tsea\t4\t4\tCon nghe một từ tiếng Anh nhé!\tĐây là từ tiếng Anh.\tboat\n";
        return OfflineLearningCatalog.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }
    @Test void bothUsesFiveForSauAndFourForOngAndCommonTurns() {
        var director = new EntertainmentDirector();
        director.setLearningCatalog(syntheticPack());
        director.restore(2); // 1st lesson: Sâu age 5.
        var sau = director.nextJourney(EntertainmentDirector.Audience.BOTH, 4, "sea");
        assertEquals(EntertainmentDirector.Audience.SAU, sau.focus());
        assertEquals("learn-quiz-five", sau.id());
        director.restore(5); // 2nd lesson: Ong age 4.
        var ong = director.nextJourney(EntertainmentDirector.Audience.BOTH, 4, "sea");
        assertEquals(EntertainmentDirector.Audience.ONG, ong.focus());
        assertEquals("learn-word-four", ong.id());
        director.restore(8); // Shared lesson must reject age-5-only quiz.
        var together = director.nextJourney(EntertainmentDirector.Audience.BOTH, 4, "sea");
        assertEquals(EntertainmentDirector.Audience.BOTH, together.focus());
        assertFalse(together.id().startsWith("learn-"));
    }
    @Test void agesMatchNewParentOptions() {
        assertEquals(5, EntertainmentDirector.Audience.SAU.age());
        assertEquals(4, EntertainmentDirector.Audience.ONG.age());
        assertEquals(4, EntertainmentDirector.Audience.BOTH.age());
    }
}
