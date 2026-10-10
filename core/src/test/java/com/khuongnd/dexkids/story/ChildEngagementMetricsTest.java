package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChildEngagementMetricsTest {
    @Test void tracksThreeAudiencesAndPreemptionWithoutPrivateData() {
        var m = new ChildEngagementMetrics();
        assertTrue(m.recordBeatStart(EntertainmentDirector.Audience.SAU));
        assertFalse(m.recordBeatStart(EntertainmentDirector.Audience.ONG)); // no overlapping beats
        assertTrue(m.completeBeat());
        assertFalse(m.completeBeat());
        assertTrue(m.recordBeatStart(EntertainmentDirector.Audience.ONG));
        assertTrue(m.interruptForPoi());
        assertTrue(m.recordBeatStart(EntertainmentDirector.Audience.BOTH));
        assertTrue(m.completeBeat());
        m.recordVoiceUnavailable();
        m.recordParentPause();
        var snapshot = m.snapshot();
        assertEquals(3, snapshot.starts());
        assertEquals(1, snapshot.sauBeats());
        assertEquals(1, snapshot.ongBeats());
        assertEquals(1, snapshot.togetherBeats());
        assertEquals(2, snapshot.resolutions());
        assertEquals(1, snapshot.poiPreemptions());
        assertEquals(0, snapshot.otherCancellations());
        assertEquals(1, snapshot.voiceUnavailable());
        assertEquals(1, snapshot.parentPauses());
        assertEquals(0, snapshot.unfinished());
    }

    @Test void lifecycleAndPendingGuard() {
        var m = new ChildEngagementMetrics();
        assertThrows(IllegalArgumentException.class, () -> m.recordBeatStart(null));
        assertTrue(m.recordBeatStart(EntertainmentDirector.Audience.SAU));
        m.recordParentPause();
        assertFalse(m.hasPendingBeat());
        assertEquals(0, m.snapshot().poiPreemptions());
        assertEquals(1, m.snapshot().otherCancellations());
        assertEquals(1, m.snapshot().parentPauses());
        assertFalse(m.interruptForPoi());
        assertTrue(m.recordBeatStart(EntertainmentDirector.Audience.ONG));
    }
    @Test void simulatedBothModeQuarterHourAndHourCountersStayBalanced() {
        var director = new EntertainmentDirector();
        var metrics = new ChildEngagementMetrics();
        for (int i = 0; i < 86; i++) {
            var beat = director.next(EntertainmentDirector.Audience.BOTH);
            assertTrue(metrics.recordBeatStart(beat.focus()));
            assertTrue(metrics.completeBeat());
            if (i == 20) {
                var first15 = metrics.snapshot(); // 21 starts ≈ 15 minutes at 42s/event
                assertEquals(7, first15.sauBeats());
                assertEquals(7, first15.ongBeats());
                assertEquals(7, first15.togetherBeats());
            }
        }
        var hour = metrics.snapshot(); // 86 starts ≈ 60 minutes
        assertEquals(86, hour.starts());
        assertEquals(86, hour.resolutions());
        assertEquals(29, hour.sauBeats());
        assertEquals(29, hour.ongBeats());
        assertEquals(28, hour.togetherBeats());
        assertEquals(0, hour.unfinished());
    }
}
