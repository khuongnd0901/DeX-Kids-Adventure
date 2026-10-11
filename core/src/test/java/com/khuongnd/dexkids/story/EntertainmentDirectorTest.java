package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class EntertainmentDirectorTest {
    @Test void eighteenOpeningsStayDistinctWithoutEarlyRepetitionAndRestoreCorrectly() {
        for (var audience : EntertainmentDirector.Audience.values()) {
            Set<String> openings = new HashSet<>();
            for (int offset = 0; offset < 18; offset++) {
                var director = new EntertainmentDirector();
                director.setOpeningOffset(offset);
                Set<String> cycle = new HashSet<>();
                int length = audience == EntertainmentDirector.Audience.BOTH ? 54 : 24;
                for (int i = 0; i < length; i++) {
                    var beat = director.next(audience);
                    if (i == 0) assertTrue(openings.add(beat.id()));
                    assertTrue(cycle.add(beat.id()));
                    if (audience != EntertainmentDirector.Audience.BOTH) {
                        String absent = audience == EntertainmentDirector.Audience.SAU ? "Ong" : "Sâu";
                        assertFalse((beat.introduction() + beat.resolution()).contains(absent));
                    }
                }
                var recreated = new EntertainmentDirector();
                recreated.setOpeningOffset(director.openingOffset());
                recreated.restore(director.position());
                assertEquals(director.next(audience), recreated.next(audience));
            }
            assertEquals(18, openings.size());
        }
    }
    @Test void audienceDefaultsAndAges() {
        assertEquals(EntertainmentDirector.Audience.BOTH, EntertainmentDirector.Audience.fromId(null));
        assertEquals(EntertainmentDirector.Audience.BOTH, EntertainmentDirector.Audience.fromId("unknown"));
        assertEquals(4, EntertainmentDirector.Audience.SAU.age());
        assertEquals(3, EntertainmentDirector.Audience.ONG.age());
        assertEquals(3, EntertainmentDirector.Audience.BOTH.age());
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
        assertEquals(66, EntertainmentDirector.uniqueBeatCount());
    }

    @Test void sixtySixEpisodesStayBoundedAndCoverNewSafetyTopics() {
        assertEquals(66, EntertainmentDirector.uniqueBeatCount());
        for (var mode : EntertainmentDirector.Audience.values()) {
            var director = new EntertainmentDirector();
            for (int i = 0; i < 66; i++) {
                var beat = director.next(mode);
                assertTrue(beat.introduction().length() <= 240);
                assertTrue(beat.resolution().length() <= 240);
                assertFalse(beat.id().isBlank());
            }
        }
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
