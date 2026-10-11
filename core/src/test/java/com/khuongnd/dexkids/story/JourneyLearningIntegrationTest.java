package com.khuongnd.dexkids.story;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JourneyLearningIntegrationTest {
    @Test void classicRotationIsPreservedAndNewLessonsRespectAudience() throws Exception {
        var director=new EntertainmentDirector();
        var catalog=OfflineLearningCatalog.parse(
            Files.newInputStream(Path.of("..","assets/learning/offline-learning.tsv")));
        director.setLearningCatalog(catalog);
        for(var audience:EntertainmentDirector.Audience.values()){
            director.restore(0);
            int found=0,english=0;
            for(int i=0;i<72;i++){
                var beat=director.nextJourney(audience,audience.age(),"sea");
                assertTrue(beat.introduction().length()<=240);
                assertTrue(beat.resolution().length()<=240);
                if(beat.id().startsWith("learn-")){
                    found++;
                    if(!beat.englishWord().isEmpty())english++;
                    if(audience!=EntertainmentDirector.Audience.BOTH)
                        assertEquals(audience,beat.focus());
                }
            }
            assertTrue(found>=20,"Learning cards should interleave after classic beats");
            assertTrue(english>=9,"Vocabulary not exposed in journey");
        }
    }
}
