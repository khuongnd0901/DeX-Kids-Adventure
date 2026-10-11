package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SauCareReminderTest {
    @Test void reminderIsOccasionalAndNeverAddressesOng() {
        for (var audience : EntertainmentDirector.Audience.values()) {
            var director = new EntertainmentDirector();
            int reminders = 0;
            for (int step = 0; step < 54; step++) {
                var beat = director.nextJourney(audience, audience.age(), "");
                if (!beat.id().equals("care-sau-lips")) continue;
                reminders++;
                assertEquals(16, step % 18);
                assertEquals(EntertainmentDirector.Audience.SAU, beat.focus());
                assertFalse(beat.introduction().contains("xấu"));
                assertFalse(beat.introduction().endsWith("?"));
                assertTrue(beat.introduction().contains("thả lỏng môi"));
            }
            assertEquals(audience == EntertainmentDirector.Audience.ONG ? 0 : 3, reminders);
        }
    }
    @Test void savedCursorDoesNotReplayTheReminderOnRecreation() {
        var director = new EntertainmentDirector();
        director.restore(16);
        assertEquals("care-sau-lips", director.nextJourney(EntertainmentDirector.Audience.SAU, 5, "").id());
        var recreated = new EntertainmentDirector();
        recreated.restore(director.position());
        assertNotEquals("care-sau-lips", recreated.nextJourney(EntertainmentDirector.Audience.SAU, 5, "").id());
    }
}
