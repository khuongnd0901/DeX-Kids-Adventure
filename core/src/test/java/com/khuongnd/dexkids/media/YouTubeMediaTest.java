package com.khuongnd.dexkids.media;

import org.junit.jupiter.api.Test;
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class YouTubeMediaTest {
    private static final String A = "abcdefghijk", B = "lmnopqrstuv", C = "123456789_-";
    private static final long NOW = 10_000_000L;
    private YouTubeVideo video(String id) { return new YouTubeVideo(id,"Video phụ huynh duyệt",Audience.BOTH,4,7,300,true,true,"Con nhớ điều gì?"); }
    private YouTubeInterlude.Boundary boundary(int count, boolean busy, boolean demo, boolean online,
            boolean external, boolean enabled, int every, int step, String last, Audience audience,
            List<YouTubeInterlude.Launch> launches) {
        return new YouTubeInterlude.Boundary(count,busy,demo,online,external,enabled,every,step,last,audience,6,5,NOW,launches);
    }
    private YouTubeInterlude.Boundary ready(int every,int step,String last) {
        return boundary(5,false,true,true,true,true,every,step,last,Audience.BOTH,List.of());
    }
    @Test void exactHostsBecomeCanonicalSingleVideoWithoutTrackingParameters() {
        for (String input : List.of(A,"https://youtu.be/"+A+"?si=tracking",
            "https://www.youtube.com/watch?v="+A+"&list=not-imported&t=30",
            "https://m.youtube.com/shorts/"+A,"https://youtube.com/embed/"+A)) {
            assertEquals(A,YouTubeVideo.idFromInput(input));
            assertEquals("https://www.youtube.com/watch?v="+A,video(YouTubeVideo.idFromInput(input)).url());
        }
    }
    @Test void arbitraryHostsScriptsCredentialsPlaylistAndAmbiguousIdsAreRejected() {
        for (String input : List.of("http://youtu.be/"+A,"https://youtu.be.evil.test/"+A,
            "https://youtube.com@evil.test/watch?v="+A,"https://evil.test/?v="+A,
            "https://youtube.com/playlist?list=PL123", "https://youtube.com/watch?v="+A+"&v="+B,
            "https://youtube.com:443/watch?v="+A,"javascript:alert(1)","https://youtu.be/"+A+"/extra",
            "https://youtu.be/"+A+"#fragment","https://youtube.com/watch?v=%3Cscript%3E"))
            assertThrows(IllegalArgumentException.class,()->YouTubeVideo.idFromInput(input),input);
        assertThrows(IllegalArgumentException.class,()->YouTubeVideo.idFromInput("x".repeat(2049)));
    }
    @Test void catalogRoundTripPreservesVietnameseAndReviewAndReturnsImmutableSnapshot() {
        var original = List.of(video(A),new YouTubeVideo(B,"Chưa duyệt",Audience.ONG,4,6,120,false,true,""));
        var decoded = YouTubeLibrary.decode(YouTubeLibrary.encode(original));
        assertEquals(original,decoded);
        assertThrows(UnsupportedOperationException.class,()->decoded.add(video(C)));
        assertFalse(decoded.get(1).eligible(Audience.ONG,6,5));
    }
    @Test void corruptOrOversizedRevisionNeverPartiallyImportsOrGrantsApproval() {
        String valid = YouTubeLibrary.encode(List.of(video(A)));
        for (String source : List.of("{",valid.replace("\"schema\":1","\"schema\":2"),
            valid.replace("\"approved\":true,",""),valid.replace("\"enabled\":true","\"enabled\":\"true\""),
            valid.replace("\"minAge\":4","\"minAge\":3"),
            valid.replace("\"minAge\":4","\"minAge\":4.5"),
            valid.replace("\"plannedSeconds\":300","\"plannedSeconds\":4294967596")," ".repeat(65537)))
            assertThrows(IllegalArgumentException.class,()->YouTubeLibrary.decode(source),source.substring(0,Math.min(120,source.length())));
        assertThrows(IllegalArgumentException.class,()->YouTubeLibrary.encode(List.of(video(A),video(A))));
        assertThrows(IllegalArgumentException.class,()->YouTubeLibrary.encode(Collections.nCopies(65,video(A))));
        assertEquals(List.of(video(A)),YouTubeLibrary.decode(valid));
    }
    @Test void bothRequiresReviewAndAgeRangeForBothChildren() {
        var sauOnly = new YouTubeVideo(A,"Sâu",Audience.SAU,4,7,300,true,true,"");
        assertTrue(sauOnly.eligible(Audience.SAU,6,5));
        assertFalse(sauOnly.eligible(Audience.ONG,6,5));
        assertFalse(sauOnly.eligible(Audience.BOTH,6,5));
        var older = new YouTubeVideo(A,"Lớn",Audience.BOTH,6,7,300,true,true,"");
        assertTrue(older.eligible(Audience.SAU,6,5));
        assertFalse(older.eligible(Audience.BOTH,6,5));
        for (Audience mode : Audience.values()) assertTrue(video(A).eligible(mode,6,5));
        assertFalse(new YouTubeVideo(A,"Tắt",Audience.BOTH,4,7,300,true,false,"").eligible(Audience.BOTH,6,5));
    }
    @Test void defaultAlternatesStoryThenVideoAtFiveCompletedQuestionsOnly() {
        var story = YouTubeInterlude.choose(ready(2,0,""),List.of(video(A)));
        assertTrue(story.boundaryConsumed()); assertNull(story.video()); assertEquals(1,story.nextStep());
        var afterRecreation = YouTubeInterlude.choose(ready(2,story.nextStep(),""),List.of(video(A)));
        assertEquals(A,afterRecreation.video().videoId()); assertEquals(0,afterRecreation.nextStep());
        for (int count=0; count<5; count++) {
            var pick=YouTubeInterlude.choose(boundary(count,false,true,true,true,true,2,1,"",Audience.BOTH,List.of()),List.of(video(A)));
            assertFalse(pick.boundaryConsumed()); assertEquals(1,pick.nextStep()); assertNull(pick.video());
        }
        var busy=YouTubeInterlude.choose(boundary(5,true,true,true,true,true,2,1,"",Audience.BOTH,List.of()),List.of(video(A)));
        assertFalse(busy.boundaryConsumed()); assertEquals(1,busy.nextStep());
    }
    @Test void configuredThreeStoriesPerVideoAndOffAreDeterministic() {
        int step=0;
        for (int i=0; i<12; i++) {
            var pick=YouTubeInterlude.choose(ready(4,step,""),List.of(video(A)));
            assertEquals(i%4==3,pick.video()!=null); step=pick.nextStep();
        }
        assertNull(YouTubeInterlude.choose(ready(0,1,""),List.of(video(A))).video());
    }
    @Test void offlineUnreadyJourneyPhoneAndDisabledFallBackWithoutLeakingPlayback() {
        for (int gate=0;gate<4;gate++) {
            var pick=YouTubeInterlude.choose(boundary(5,false,gate!=0,gate!=1,gate!=2,gate!=3,2,1,"",Audience.BOTH,List.of()),List.of(video(A)));
            assertTrue(pick.boundaryConsumed()); assertNull(pick.video());
        }
        assertNull(YouTubeInterlude.choose(ready(2,1,""),List.of()).video());
    }
    @Test void rotationSkipsUnapprovedAndResumesAfterLastAcrossRecreation() {
        var unapproved=new YouTubeVideo(B,"Chưa duyệt",Audience.BOTH,4,7,300,false,true,"");
        List<YouTubeVideo> catalog=List.of(video(A),unapproved,video(C));
        assertEquals(C,YouTubeInterlude.choose(ready(2,1,A),catalog).video().videoId());
        assertEquals(A,YouTubeInterlude.choose(ready(2,1,C),catalog).video().videoId());
    }
    @Test void rollingBudgetExpiresAtHourAndRollbackCannotBypassIt() {
        var full=List.of(new YouTubeInterlude.Launch(NOW-1000,300),new YouTubeInterlude.Launch(NOW-2000,300));
        assertNull(YouTubeInterlude.choose(boundary(5,false,true,true,true,true,2,1,"",Audience.BOTH,full),List.of(video(A))).video());
        var expired=List.of(new YouTubeInterlude.Launch(NOW-3_600_000L,300));
        assertNotNull(YouTubeInterlude.choose(boundary(5,false,true,true,true,true,2,1,"",Audience.BOTH,expired),List.of(video(A))).video());
        var tooLong=List.of(new YouTubeInterlude.Launch(NOW-1000,601));
        assertNull(YouTubeInterlude.choose(boundary(5,false,true,true,true,true,2,1,"",Audience.BOTH,tooLong),List.of(video(A))).video());
        var future=List.of(new YouTubeInterlude.Launch(NOW+1,300));
        assertNull(YouTubeInterlude.choose(boundary(5,false,true,true,true,true,2,1,"",Audience.BOTH,future),List.of(video(A))).video());
    }
    @Test void recallAndManualDurationAreBounded() {
        assertThrows(IllegalArgumentException.class,()->new YouTubeVideo(A,"Title",Audience.BOTH,4,7,1801,true,true,""));
        assertThrows(IllegalArgumentException.class,()->new YouTubeVideo(A,"Title",Audience.BOTH,4,7,300,true,true,"1\n2\n3"));
        assertThrows(IllegalArgumentException.class,()->new YouTubeVideo(A,"Title",Audience.BOTH,4,7,300,true,true,"x".repeat(401)));
    }
}
