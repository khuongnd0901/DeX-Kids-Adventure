package com.khuongnd.dexkids.story.adventure;

import org.junit.jupiter.api.Test;
import java.util.*;
import static com.khuongnd.dexkids.story.adventure.StoryEpisode.*;
import static org.junit.jupiter.api.Assertions.*;

class StoryEpisodeTest {
    @Test void allAudiencesRoundtripWithActualAssetEnumsAndAges() {
        for(Audience audience:Audience.values()) {
            StoryEpisode e=StoryFixtures.educational(audience);
            assertEquals(e,StoryEpisodeCodec.parse(StoryEpisodeCodec.toJson(e)));
            assertEquals(audience==Audience.SAU?5:4,e.learningAge());
            assertEquals(4,e.scenes().size());
        }
    }
    @Test void rejectsSchemaUnknownFieldDuplicateKeyAndInvalidAsset() {
        String json=StoryEpisodeCodec.toJson(StoryFixtures.educational(Audience.BOTH));
        for(String bad:List.of(json.replace("\"schemaVersion\":1","\"schemaVersion\":0"),json.replace("\"schemaVersion\":1","\"schemaVersion\":2"),json.replace("\"schemaVersion\":1","\"schemaVersion\":\"1\""),json.replaceFirst("\\{","{\"shader\":\"evil\","),json.replaceFirst("\\{","{\"id\":\"duplicate\","),json.replace("\"WAVE\"","\"EXECUTE\""))) {
            assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(bad));
        }
    }
    @Test void rejectsContradictoryActionInstructions() {
        String json=StoryEpisodeCodec.toJson(StoryFixtures.educational(Audience.SAU));
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(json.replace("\"action\":\"LOOK_UP\"","\"action\":\"WAVE\"")));
    }
    @Test void rejectsPrivacyDangerGpsUrlsAndOverlongContent() {
        StoryEpisode e=StoryFixtures.educational(Audience.BOTH);
        String json=StoryEpisodeCodec.toJson(e);
        for(String bad:List.of("Chúng ta đang ở Hà Nội.","Địa chỉ nhà con ở đâu?","https://example.com","Hãy xuống xe ngay nhé!","10.12345, 106.12345","x".repeat(241))) {
            assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(json.replace(e.scenes().get(0).beats().get(0).utterance(),bad)));
        }
    }
    @Test void rejectsMissingQuestionsWrongTurnsAndActorMismatch() {
        StoryEpisode e=StoryFixtures.educational(Audience.BOTH);
        List<Scene> none=e.scenes().stream().map(s->new Scene(s.id(),s.backdrop(),s.beats(),null)).toList();
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeValidator.validate(copy(e,none)));
        String json=StoryEpisodeCodec.toJson(e);
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(json.replace("\"target\":\"ONG\"","\"target\":\"SAU\"")));
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(json.replace("\"audience\":\"BOTH\"","\"audience\":\"SAU\"")));
    }
    @Test void rejectsLenientJavascriptSyntaxAndMalformedJson() {
        String json=StoryEpisodeCodec.toJson(StoryFixtures.educational(Audience.SAU));
        for(String bad:List.of(json.replace("\"id\":","id:"),json.replaceFirst("\\{","{/* comment */"),json.substring(0,json.length()-1)+",}",json+" null"))
            assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(bad));
    }
    @Test void boundsInputDepthCountsAndCollections() {
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(" ".repeat(48001)));
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse("[".repeat(1000)));
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse("{}"));
        StoryEpisode e=StoryFixtures.educational(Audience.SAU);
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeValidator.validate(copy(e,e.scenes().subList(0,3))));
        assertThrows(UnsupportedOperationException.class,()->e.scenes().clear());
    }
    private StoryEpisode copy(StoryEpisode e,List<Scene> scenes) {return new StoryEpisode(e.id(),e.schemaVersion(),e.revision(),e.title(),e.audience(),e.sauAge(),e.ongAge(),e.educationalGoal(),e.synopsis(),scenes);}
}
