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
        var a=load();assertEquals(152,a.size());
        assertEquals(72,a.count(OfflineLearningCatalog.Kind.QUIZ));
        assertEquals(80,a.count(OfflineLearningCatalog.Kind.WORD));
        for(int age=3;age<=6;age++)for(var k:OfflineLearningCatalog.Kind.values()){
            var c=a.select(k,age,"sea",age);
            assertNotNull(c);assertTrue(c.forAge(age));assertEquals("sea",c.topic());
        }
        assertNull(a.select(OfflineLearningCatalog.Kind.WORD,2,"",0));
        assertEquals("sea",OfflineLearningCatalog.topicForBackdrop("vungtau_beach"));
        assertEquals("nature",OfflineLearningCatalog.topicForBackdrop("waterfall"));
    }
    @Test void invalidContentCannotBePublished(){
        String h="# dexkids-offline-learning-v1\n"+
            "id\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish\n";
        assertThrows(IllegalArgumentException.class,()->OfflineLearningCatalog.parse(
            new ByteArrayInputStream((h+"a-1\tWORD\tsea\t3\t6\tCon hãy nói từ này\tĐáp án từ rất tốt\t../../x\n").getBytes())));
    }
}
