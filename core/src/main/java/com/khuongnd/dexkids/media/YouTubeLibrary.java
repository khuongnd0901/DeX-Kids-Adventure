package com.khuongnd.dexkids.media;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bounded whole-file local catalog. Decode rejects the whole revision on error. */
public final class YouTubeLibrary {
    public static final int MAX_BYTES = 65536;
    public static final int MAX_VIDEOS = 64;
    private YouTubeLibrary() {}
    public static List<YouTubeVideo> decode(String source) {
        if (source == null || source.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES)
            throw new IllegalArgumentException("Catalog too large");
        try {
            JsonValue root = new JsonReader().parse(source);
            if (!root.isObject() || integer(root,"schema") != 1 || !root.has("videos") ||
                !root.get("videos").isArray() || root.get("videos").size > MAX_VIDEOS)
                throw new IllegalArgumentException("Invalid catalog");
            List<YouTubeVideo> videos = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            for (JsonValue row : root.get("videos")) {
                // Review flags must be explicit; missing data cannot grant approval.
                if (!row.isObject() || !row.has("approved") || !row.get("approved").isBoolean() ||
                    !row.has("enabled") || !row.get("enabled").isBoolean())
                    throw new IllegalArgumentException("Missing review");
                YouTubeVideo video = new YouTubeVideo(string(row,"videoId"), string(row,"title"),
                    Audience.valueOf(string(row,"audience")), integer(row,"minAge"), integer(row,"maxAge"),
                    integer(row,"plannedSeconds"), row.getBoolean("approved"), row.getBoolean("enabled"),
                    row.has("recall") ? string(row,"recall") : "");
                if (!ids.add(video.videoId())) throw new IllegalArgumentException("Duplicate video");
                videos.add(video);
            }
            return List.copyOf(videos);
        } catch (RuntimeException error) { throw new IllegalArgumentException("Invalid catalog revision", error); }
    }
    private static int integer(JsonValue row, String key) {
        JsonValue value = row.get(key);
        if (value == null || !value.isLong() || value.asLong() < Integer.MIN_VALUE || value.asLong() > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid integer: " + key);
        return value.asInt();
    }
    private static String string(JsonValue row, String key) {
        JsonValue value = row.get(key);
        if (value == null || !value.isString()) throw new IllegalArgumentException("Invalid text: " + key);
        return value.asString();
    }
    public static String encode(List<YouTubeVideo> videos) {
        if (videos == null || videos.size() > MAX_VIDEOS) throw new IllegalArgumentException("Catalog full");
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("schema", new JsonValue(1L));
        JsonValue rows = new JsonValue(JsonValue.ValueType.array); root.addChild("videos", rows);
        for (YouTubeVideo v : videos) {
            JsonValue row = new JsonValue(JsonValue.ValueType.object);
            row.addChild("videoId", new JsonValue(v.videoId())); row.addChild("title", new JsonValue(v.title()));
            row.addChild("audience", new JsonValue(v.audience().name()));
            row.addChild("minAge", new JsonValue((long)v.minAge())); row.addChild("maxAge", new JsonValue((long)v.maxAge()));
            row.addChild("plannedSeconds", new JsonValue((long)v.plannedSeconds()));
            row.addChild("approved", new JsonValue(v.approved())); row.addChild("enabled", new JsonValue(v.enabled()));
            row.addChild("recall", new JsonValue(v.recall())); rows.addChild(row);
        }
        String json = root.toJson(JsonWriter.OutputType.json);
        decode(json); return json;
    }
}
