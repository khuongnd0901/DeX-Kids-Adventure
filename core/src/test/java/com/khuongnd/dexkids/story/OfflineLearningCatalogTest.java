package com.khuongnd.dexkids.story;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
class OfflineLearningCatalogTest {
    private OfflineLearningCatalog load() throws Exception {
        try(var in=Files.newInputStream(Path.of("..","assets/learning/offline-learning.tsv"))){
            return OfflineLearningCatalog.parse(in);
        }
    }
    @Test void cardsAreAgeSafeAndContextual() throws Exception{
        var a=load();assertEquals(176,a.size());
        assertEquals(96,a.count(OfflineLearningCatalog.Kind.QUIZ));
        assertEquals(80,a.count(OfflineLearningCatalog.Kind.WORD));
        for(int age=4;age<=6;age++)for(var k:OfflineLearningCatalog.Kind.values()){
            var c=a.select(k,age,"sea",age);
            assertNotNull(c);assertTrue(c.forAge(age));assertEquals("sea",c.topic());
        }
        for (int age : new int[]{2,3}) for (var kind : OfflineLearningCatalog.Kind.values())
            assertNull(a.select(kind,age,"",0));
        assertEquals("sea",OfflineLearningCatalog.topicForBackdrop("vungtau_beach"));
        assertEquals("nature",OfflineLearningCatalog.topicForBackdrop("waterfall"));
    }
    @Test void toddlerQuizzesCannotReturnThroughAReplacementPack() {
        String h="# dexkids-offline-learning-v1\n"+
            "id\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish\n";
        for (int age : new int[]{2,3}) {
            String row = "quiz-toddler\tQUIZ\tsea\t" + age + "\t6\tCon hãy chọn đáp án nhé?\tĐây là đáp án đã có.\t\n";
            assertThrows(IllegalArgumentException.class, () -> OfflineLearningCatalog.parse(
                new ByteArrayInputStream((h + row).getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        }
    }
    @Test void invalidContentCannotBePublished(){
        String h="# dexkids-offline-learning-v1\n"+
            "id\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish\n";
        assertThrows(IllegalArgumentException.class,()->OfflineLearningCatalog.parse(
            new ByteArrayInputStream((h+"a-1\tWORD\tsea\t4\t6\tCon hãy nói từ này\tĐáp án từ rất tốt\t../../x\n").getBytes())));
    }
}
