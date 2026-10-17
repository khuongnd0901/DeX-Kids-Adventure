package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class Learning400PackTest {
    private OfflineLearningCatalog pack() throws Exception {
        try(var in=Files.newInputStream(Path.of("..","docs/learning-source/learning.5-7.400.tsv"))) {
            return OfflineLearningCatalog.parse(in);
        }
    }
    @Test void exactCountsUniqueTranslationsAndAgeBands() throws Exception {
        var p=pack();assertEquals(400,p.size());
        assertEquals(200,p.count(OfflineLearningCatalog.Kind.QUIZ));assertEquals(200,p.count(OfflineLearningCatalog.Kind.WORD));
        assertEquals(200,p.cards().stream().filter(c->c.kind()==OfflineLearningCatalog.Kind.WORD).map(OfflineLearningCatalog.Card::englishWord).distinct().count());
        assertEquals(8,p.cards().stream().map(OfflineLearningCatalog.Card::topic).distinct().count());
        for(var c:p.cards()) {assertTrue(c.minAge()>=5 && c.maxAge()==7);assertTrue(c.promptVi().endsWith("?"));}
        for(int age=5;age<=7;age++)for(var kind:OfflineLearningCatalog.Kind.values()) {
            int target=age;
            assertTrue(p.cards().stream().filter(c->c.forAge(target)&&c.kind()==kind).count()>=100);
        }
    }
    @Test void everyEligibleCardIsHeardBeforeRepeatEvenWithConstantTopic() throws Exception {
        var p=pack();
        for(int age=5;age<=7;age++)for(var kind:OfflineLearningCatalog.Kind.values()) {
            int target=age;
            int count=(int)p.cards().stream().filter(c->c.kind()==kind&&c.forAge(target)).count();
            var deck=new LearningDeck(p,new Random(69));Set<String> heard=new HashSet<>();String last="";
            for(int i=0;i<count;i++) {var c=deck.next(kind,age,"sea");assertTrue(c.forAge(age));assertTrue(heard.add(c.id()),c.id());last=c.id();}
            assertNotEquals(last,deck.next(kind,age,"sea").id());
        }
    }
    @Test void sevenYearOldsUseNewContentWhileLegacyCatalogRemainsUnchanged() throws Exception {
        var director=new EntertainmentDirector();director.setLearningCatalog(pack());
        for(int i=0;i<24;i++) {
            var beat=director.nextQuestion(EntertainmentDirector.Audience.BOTH,7,7,"");
            assertTrue(beat.id().startsWith("learn-quiz-57-")||beat.id().startsWith("learn-word-57-"),beat.id());
        }
        try(var in=Files.newInputStream(Path.of("..","assets/learning/offline-learning.tsv"))) {
            var legacy=OfflineLearningCatalog.parse(in);assertEquals(176,legacy.size());
            assertNull(legacy.select(OfflineLearningCatalog.Kind.QUIZ,7,"",0));
        }
    }
    @Test void boundIsFiveHundredNotUnboundedAndAgeEightIsRejected() {
        String header="# dexkids-offline-learning-v1\nid\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish\n";
        StringBuilder rows=new StringBuilder(header);
        for(int i=0;i<500;i++)rows.append("quiz-bound-").append(i).append("\tQUIZ\tnature\t5\t7\tCâu hỏi kiểm tra số ").append(i).append("?\tĐáp án đã có trong bộ mẫu.\t\n");
        assertEquals(500,OfflineLearningCatalog.parse(new ByteArrayInputStream(rows.toString().getBytes(StandardCharsets.UTF_8))).size());
        rows.append("quiz-extra\tQUIZ\tnature\t5\t7\tMột câu hỏi vượt giới hạn?\tĐáp án cho câu hỏi cuối.\t\n");
        assertThrows(IllegalArgumentException.class,()->OfflineLearningCatalog.parse(new ByteArrayInputStream(rows.toString().getBytes(StandardCharsets.UTF_8))));
        String invalid=header+"quiz-age-eight\tQUIZ\tnature\t5\t8\tCâu hỏi cho tuổi ngoài giới hạn?\tĐáp án nằm ngoài giới hạn.\t\n";
        assertThrows(IllegalArgumentException.class,()->OfflineLearningCatalog.parse(new ByteArrayInputStream(invalid.getBytes(StandardCharsets.UTF_8))));
    }
}
