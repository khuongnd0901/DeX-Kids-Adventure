package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class EntertainmentDirectorTest {
    @Test void audienceDefaultsAndAges() {
        assertEquals(EntertainmentDirector.Audience.BOTH, EntertainmentDirector.Audience.fromId(null));
        assertEquals(EntertainmentDirector.Audience.BOTH, EntertainmentDirector.Audience.fromId("unknown"));
        assertEquals(3, EntertainmentDirector.Audience.SAU.age());
        assertEquals(2, EntertainmentDirector.Audience.ONG.age());
        assertEquals(2, EntertainmentDirector.Audience.BOTH.age());
    }
    @Test void bothRotatesWithoutFavoritismAndNoRepeatForThirtySixBeats() {
        var director = new EntertainmentDirector();
        Set<String> unique = new HashSet<>();
        for (int i = 0; i < 36; i++) {
            var beat = director.next(EntertainmentDirector.Audience.BOTH);
            assertEquals(i % 3 == 0 ? EntertainmentDirector.Audience.SAU :
                i % 3 == 1 ? EntertainmentDirector.Audience.ONG :
                EntertainmentDirector.Audience.BOTH, beat.focus());
            assertTrue(unique.add(beat.id()), "Repeated before all 36 episodes played");
            assertFalse(beat.introduction().isBlank());
            assertFalse(beat.resolution().isBlank());
        }
        assertEquals(48, EntertainmentDirector.uniqueBeatCount());
    }
    @Test void singleViewerNeverAddressesOtherChild() {
        for (var audience : new EntertainmentDirector.Audience[] {
                EntertainmentDirector.Audience.SAU, EntertainmentDirector.Audience.ONG}) {
            var director = new EntertainmentDirector();
            Set<String> unique = new HashSet<>();
            for (int i = 0; i < 24; i++) {
                var beat = director.next(audience);
                assertTrue(beat.focus() == audience || beat.focus() == EntertainmentDirector.Audience.BOTH);
                assertTrue(unique.add(beat.id()));
                String absentName = audience == EntertainmentDirector.Audience.SAU ? "Ong" : "Sâu";
                assertFalse(beat.introduction().contains(absentName), beat.id());
                assertFalse(beat.resolution().contains(absentName), beat.id());
            }
        }
    }
    @Test void restoresCursorAndRejectsNullAudience() {
        var d = new EntertainmentDirector();
        d.restore(6);
        assertEquals(6, d.position());
        assertEquals(EntertainmentDirector.Audience.SAU,
            d.next(EntertainmentDirector.Audience.BOTH).focus());
        d.restore(-3);
        assertEquals(0, d.position());
        assertThrows(NullPointerException.class, () -> d.next(null));
    }
}
