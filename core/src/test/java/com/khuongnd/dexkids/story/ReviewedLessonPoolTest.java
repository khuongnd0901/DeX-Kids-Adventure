package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ReviewedLessonPoolTest {
    private final EntertainmentDirector.Audience solo=EntertainmentDirector.Audience.SAU;
    private List<ReviewedLessonPool.Candidate> batch(int offset) {
        var out=new ArrayList<ReviewedLessonPool.Candidate>();
        for(int i=0;i<20;i++) {
            var genre=CreativeLessonPack.Genre.values()[i%4];
            String prompt=genre==CreativeLessonPack.Genre.STORY
                ? "Trong câu chuyện tưởng tượng thứ "+(offset+i)+", bạn thỏ tìm một chiếc lá phát sáng."
                : "Trong bức tranh số "+(offset+i)+", con thấy cây có lá màu gì?";
            out.add(new ReviewedLessonPool.Candidate("q"+(offset+i),new CreativeLessonPack.Lesson(
                prompt,"Cây có lá màu xanh trong bức tranh này.","green","màu xanh lá",
                "topic-"+(i%6),genre,solo,5,genre==CreativeLessonPack.Genre.STORY)));
        }
        return out;
    }
    private List<ReviewedLessonPool.Verdict> verdicts(List<ReviewedLessonPool.Candidate> batch,int count) {
        var out=new ArrayList<ReviewedLessonPool.Verdict>();
        for(int i=0;i<batch.size();i++)out.add(new ReviewedLessonPool.Verdict(batch.get(i).id(),i<count));
        return out;
    }
    @Test void fourRejectedLeavesSixteen() {
        var pool=new ReviewedLessonPool();var b=batch(0);
        pool.merge(b,verdicts(b,16),solo,5,4,List.of());
        assertEquals(16,pool.complete(solo,5,4,List.of()).lessons().size());
    }
    @Test void refillKeepsReviewedLessonsImmutable() {
        var pool=new ReviewedLessonPool();var b=batch(0);
        pool.merge(b,verdicts(b,13),solo,5,4,List.of());
        assertThrows(IllegalArgumentException.class,()->pool.complete(solo,5,4,List.of()));
        var next=batch(20);pool.merge(next,verdicts(next,7),solo,5,4,List.of());
        var pack=pool.complete(solo,5,4,List.of());
        assertEquals(20,pack.lessons().size());
        assertEquals(20,pack.reviewSources().size());
        assertEquals(20,pack.reviewSources().stream().map(CreativeLessonPack.ReviewSource::questionId).distinct().count());
        for(int i=0;i<13;i++)assertTrue(pack.lessons().contains(b.get(i).lesson()));
    }
    @Test void duplicateMissingAndUnknownVerdictsFailAtomically() {
        var pool=new ReviewedLessonPool();var b=batch(0);
        var duplicate=new ArrayList<>(verdicts(b,20));duplicate.set(19,duplicate.get(0));
        for(var v:List.of(duplicate,verdicts(b,20).subList(0,19),List.of(new ReviewedLessonPool.Verdict("unknown",true)))) {
            assertThrows(IllegalArgumentException.class,()->pool.merge(b,v,solo,5,4,List.of()));
            assertTrue(pool.accepted().isEmpty());
        }
        var repeated=new ArrayList<>(b);repeated.set(19,b.get(0));
        assertThrows(IllegalArgumentException.class,()->pool.merge(repeated,verdicts(b,20),solo,5,4,List.of()));
    }
    @Test void ageAudienceHistoryAndRepeatedQuestionsCannotEnterPool() {
        var pool=new ReviewedLessonPool();var b=batch(0);
        pool.merge(b,verdicts(b,20),solo,4,4,List.of());assertTrue(pool.accepted().isEmpty());
        pool.merge(b,verdicts(b,20),EntertainmentDirector.Audience.ONG,5,5,List.of());
        assertTrue(pool.accepted().isEmpty());
        pool.merge(b,verdicts(b,20),solo,5,4,List.of(CreativeLessonPack.fingerprint(b.get(0).lesson().prompt())));
        assertEquals(19,pool.accepted().size());
        pool.merge(b,verdicts(b,20),solo,5,4,List.of(CreativeLessonPack.fingerprint(b.get(0).lesson().prompt())));
        assertEquals(19,pool.accepted().size());
    }
}
