package com.khuongnd.dexkids.media;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmbeddedYouTubeTest {
    private String valid() {
        return "{\"items\":[{\"id\":\"abcdefghijk\",\"status\":{\"madeForKids\":true,\"embeddable\":true,\"uploadStatus\":\"processed\",\"privacyStatus\":\"public\"},\"snippet\":{\"liveBroadcastContent\":\"none\"},\"contentDetails\":{\"duration\":\"PT4M12S\"}}]}";
    }
    private int validate(String json) { return YouTubeEligibility.validate(json,"abcdefghijk","VN",300); }
    @Test void acceptsExplicitCurrentKidsMetadataWithConservativeBudget() { assertEquals(252,validate(valid())); }
    @Test void refusesMissingOrWrongOrMultipleVideos() {
        for(String json:new String[]{"{}","{\"items\":[]}",valid().replace("abcdefghijk","lmnopqrstuv"),"{\"items\":[{},{}]}"})
            assertThrows(RuntimeException.class,()->validate(json));
    }
    @Test void requiresExplicitMfkEmbeddingAndAvailableProcessedVideo() {
        for(String json:new String[]{valid().replace("\"madeForKids\":true,",""),valid().replace("\"madeForKids\":true","\"madeForKids\":false"),
            valid().replace("\"embeddable\":true","\"embeddable\":false"),valid().replace("processed","uploaded"),
            valid().replace("public","private"),valid().replace("none","live")})
            assertThrows(RuntimeException.class,()->validate(json));
    }
    @Test void disallowsAgeRestrictionsAndWrongRegion() {
        String rated=valid().replace("\"duration\"","\"contentRating\":{\"ytRating\":\"ytAgeRestricted\"},\"duration\"");
        assertThrows(RuntimeException.class,()->validate(rated));
        for(String region:new String[]{"{\"allowed\":[\"US\"]}","{\"blocked\":[\"VN\"]}","{\"allowed\":[]}"})
            assertThrows(RuntimeException.class,()->validate(valid().replace("\"duration\"","\"regionRestriction\":"+region+",\"duration\"")));
        assertEquals(252,validate(valid().replace("\"duration\"","\"regionRestriction\":{\"allowed\":[\"VN\"]},\"duration\"")));
    }
    @Test void rejectsUnknownOrExcessiveDurationAndUnboundedResponses() {
        for(String duration:new String[]{"PT0S","PT31M","PT6M","","live"})
            assertThrows(RuntimeException.class,()->validate(valid().replace("PT4M12S",duration)));
        assertThrows(RuntimeException.class,()->validate(" ".repeat(131073)));
        assertThrows(RuntimeException.class,()->YouTubeEligibility.validate(valid(),"abcdefghijk","",300));
    }
    @Test void ignoresPreplayEndAndStaleUnknownCallbacks() {
        var session=new EmbeddedYouTubeSession(42);
        assertFalse(session.event(42,"ended"));assertFalse(session.finished());
        assertFalse(session.event(41,"error"));assertFalse(session.event(42,"nonsense"));
        assertEquals(EmbeddedYouTubeSession.State.LOADING,session.state());
    }
    @Test void decimalStringGenerationNeverUsesLossyJsonNumberCoercion() {
        var session=new EmbeddedYouTubeSession(9007199254740993L);
        assertTrue(session.matchesGeneration("9007199254740993"));
        assertFalse(session.matchesGeneration("9007199254740992"));
        assertFalse(session.matchesGeneration(null));
        assertFalse(session.matchesGeneration("9.007199254740992E15"));
    }
    @Test void actualEndRestoresExactlyOnceAndRejectsLaterCallbacks() {
        var session=new EmbeddedYouTubeSession(42);
        assertFalse(session.event(42,"playing"));assertFalse(session.event(42,"paused"));
        assertFalse(session.event(42,"buffering"));assertFalse(session.event(42,"playing"));
        assertTrue(session.event(42,"ended"));
        for(String event:new String[]{"ended","error","cancelled","playing"}) assertFalse(session.event(42,event));
        assertEquals(EmbeddedYouTubeSession.State.ENDED,session.state());
    }
    @Test void blockedErrorOrBackgroundCancelTerminateLoadingOnce() {
        for(String terminal:new String[]{"blocked","error","cancelled"}) {
            var session=new EmbeddedYouTubeSession(42);
            assertTrue(session.event(42,terminal));assertTrue(session.finished());
            assertFalse(session.event(42,"playing"));assertFalse(session.event(42,"ended"));
        }
    }
}
