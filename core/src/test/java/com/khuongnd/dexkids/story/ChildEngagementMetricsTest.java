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
}
