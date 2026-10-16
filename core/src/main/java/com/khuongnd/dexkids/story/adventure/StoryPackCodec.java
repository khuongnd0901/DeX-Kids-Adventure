package com.khuongnd.dexkids.story.adventure;

import com.badlogic.gdx.utils.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.security.MessageDigest;
import java.util.*;
import static com.khuongnd.dexkids.story.adventure.StoryEpisodeValidator.require;

/** Parent-authored pack; validation is structural, not an AI review. */
public final class StoryPackCodec {
    public static final int MAX_BYTES = 4 * 1024 * 1024;
    public static final int MAX_STORIES = 100;
    private StoryPackCodec() {}

    public static List<StoryEpisode> parse(byte[] bytes) {
        require(bytes != null && bytes.length <= MAX_BYTES, "PACK_SIZE_INVALID");
        final String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) { throw new IllegalArgumentException("PACK_UTF8_INVALID", e); }
        // Bound recursion before either the strict grammar or LibGDX parser runs.
        int depth=0; boolean quoted=false, escape=false;
        for (char c : text.toCharArray()) {
            if (escape) { escape=false; continue; }
            if (quoted && c=='\\') { escape=true; continue; }
            if (c=='"') quoted=!quoted;
            if (!quoted) {
                if (c=='{' || c=='[') require(++depth<=14, "PACK_DEPTH_INVALID");
                else if (c=='}' || c==']') require(--depth>=0, "PACK_JSON_INVALID");
            }
        }
        require(depth==0 && !quoted, "PACK_JSON_INVALID");
        new StoryEpisodeCodec.Grammar(text).check();
        JsonValue root = new JsonReader().parse(text);
        require(root.isObject(), "PACK_OBJECT_REQUIRED");
        Set<String> keys=new HashSet<>();
        for (JsonValue child : root)
            require(Set.of("schemaVersion", "stories").contains(child.name) && keys.add(child.name), "PACK_FIELD_INVALID");
        JsonValue version=root.get("schemaVersion"), stories=root.get("stories");
        require(version!=null && version.isLong() && version.asLong()==1, "PACK_SCHEMA_UNSUPPORTED");
        require(stories!=null && stories.isArray() && stories.size<=MAX_STORIES, "PACK_STORY_COUNT_INVALID");
        Set<String> ids=new HashSet<>(); List<StoryEpisode> result=new ArrayList<>();
        for (JsonValue value : stories) {
            StoryEpisode episode=StoryEpisodeCodec.parse(value.toJson(JsonWriter.OutputType.json));
            require(StoryEpisodeCodec.toJson(episode).getBytes(StandardCharsets.UTF_8).length<=65536, "STORY_SIZE_INVALID");
            require(ids.add(episode.id()), "PACK_DUPLICATE_ID"); result.add(episode);
        }
        return List.copyOf(result);
    }

    /** Source revisions are informational; only actual content changes create local revisions. */
    public static StoryEpisode identity(StoryEpisode e, String id, int revision) {
        return new StoryEpisode(id, e.schemaVersion(), revision, e.title(), e.audience(), e.sauAge(),
            e.ongAge(), e.educationalGoal(), e.synopsis(), e.scenes());
    }
    public static String digest(String text) {
        try {
            byte[] bytes=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder out=new StringBuilder(); for(byte b:bytes)out.append(String.format(Locale.ROOT,"%02x",b&255));
            return out.toString();
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static String remoteId(String source, String id) { return "remote-"+digest(source+"\n"+id).substring(0,56); }
}
