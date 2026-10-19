package com.khuongnd.dexkids.media;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static com.khuongnd.dexkids.media.YouTubeStopGate.*;

class YouTubeStopGateTest {
    private Session yt(String token, boolean paused) { return new Session(token, PACKAGE, paused); }
    @Test void absentPermissionOrMissingSessionNeverConfirms() {
        assertFalse(confirmed(Set.of("a"), null));
        assertFalse(confirmed(Set.of("a"), List.of()));
        assertFalse(confirmed(Set.of(), List.of(yt("a", true))));
    }
    @Test void commandDoesNotMeanPauseWasApplied() {
        assertFalse(confirmed(Set.of("a"), List.of(yt("a", false))));
        assertTrue(confirmed(Set.of("a"), List.of(yt("a", true))));
    }
    @Test void allCommandedSessionsMustStillExistAndBePaused() {
        assertFalse(confirmed(Set.of("a", "b"), List.of(yt("a", true))));
        assertFalse(confirmed(Set.of("a", "b"), List.of(yt("a", true), yt("b", false))));
        assertTrue(confirmed(Set.of("a", "b"), List.of(yt("a", true), yt("b", true))));
    }
    @Test void replacementOrNewYouTubeSessionCannotReleaseHold() {
        assertFalse(confirmed(Set.of("a"), List.of(yt("b", true))));
        assertFalse(confirmed(Set.of("a"), List.of(yt("a", true), yt("b", true))));
    }
    @Test void otherAppsCannotConfirmYouTubePause() {
        assertFalse(confirmed(Set.of("a"), List.of(new Session("a", "com.android.chrome", true))));
        assertTrue(confirmed(Set.of("a"), List.of(yt("a", true), new Session("c", "com.maps", false))));
    }
}
