package com.khuongnd.dexkids.story.adventure;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StoryPackCodecTest {
    private String story() {return StoryEpisodeCodec.toJson(StoryFixtures.educational(StoryEpisode.Audience.BOTH));}
    private List<StoryEpisode> parse(String json) {return StoryPackCodec.parse(json.getBytes(StandardCharsets.UTF_8));}
    private String pack(String stories) {return "{\"schemaVersion\":1,\"stories\":["+stories+"]}";}
    @Test void allAudiencesAndEmptySnapshot() {
        String stories=String.join(",",Arrays.stream(StoryEpisode.Audience.values()).map(a->StoryEpisodeCodec.toJson(StoryFixtures.educational(a))).toList());
        assertEquals(3,parse(pack(stories)).size());assertTrue(parse(pack("")).isEmpty());
    }
    @Test void rejectsDuplicateIdsKeysAndUnsupportedSchema() {
        for(String bad:List.of(pack(story()+","+story()),pack(story()).replace("\"schemaVersion\":1","\"schemaVersion\":2"),
            pack(story()).replaceFirst("\\{","{\"schemaVersion\":1,"),pack(story()).replace("\"stories\":","\"extra\":0,\"stories\":"),
            pack(story()).replace("\"title\":","\"title\":\"Bản khác\",\"title\":"),pack(story()).replace("\"schemaVersion\":1","\"schemaVersion\":1.0")))
            assertThrows(IllegalArgumentException.class,()->parse(bad));
    }
    @Test void oneBadStoryRejectsEntirePack() {
        String bad=StoryEpisodeCodec.toJson(StoryFixtures.educational(StoryEpisode.Audience.SAU)).replace("\"actor\":\"SAU\"","\"actor\":\"NOT_SUPPORTED\"");
        assertThrows(IllegalArgumentException.class,()->parse(pack(story()+","+bad)));
    }
    @Test void rejectsMalformedUtf8SyntaxDepthAndSize() {
        assertThrows(IllegalArgumentException.class,()->StoryPackCodec.parse(new byte[]{(byte)0xc3,0x28}));
        assertThrows(IllegalArgumentException.class,()->StoryPackCodec.parse(new byte[StoryPackCodec.MAX_BYTES+1]));
        for(String bad:List.of(pack(story())+"null","[".repeat(5000),"{schemaVersion:1,stories:[]}","{\"schemaVersion\":1,\"stories\":[],}","{\"schemaVersion\":1,\"stories\":[],/*x*/}"))
            assertThrows(IllegalArgumentException.class,()->parse(bad));
        assertThrows(IllegalArgumentException.class,()->parse(pack(String.join(",",Collections.nCopies(101,story())))));
    }
    @Test void namespacedIdentityAndRevisionNormalization() {
        var e=StoryFixtures.educational(StoryEpisode.Audience.SAU);
        String id=StoryPackCodec.remoteId("https://example.org/stories.json",e.id());
        assertNotEquals(id,StoryPackCodec.remoteId("https://another.org/stories.json",e.id()));
        assertNotEquals(id,StoryPackCodec.remoteId("https://example.org/stories.json","other-story"));
        StoryEpisodeValidator.validate(StoryPackCodec.identity(e,id,2));
        assertEquals(1,StoryPackCodec.identity(e,e.id(),1).revision());
    }
}
