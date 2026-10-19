package com.khuongnd.dexkids.media;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.time.Duration;

/** Fresh API metadata never substitutes for the parent's full content review. */
public final class YouTubeEligibility {
    private YouTubeEligibility() {}
    public static int validate(String json, String expectedId, String country, int plannedSeconds) {
        if (json == null || json.length() > 131072 || !country.matches("[A-Z]{2}"))
            throw new IllegalArgumentException("Invalid metadata envelope");
        JsonValue items = new JsonReader().parse(json).get("items");
        require(items != null && items.isArray() && items.size == 1);
        JsonValue item = items.get(0);
        require(expectedId.equals(item.getString("id", "")));
        JsonValue status = item.get("status"), details = item.get("contentDetails"), snippet = item.get("snippet");
        require(status != null && details != null && snippet != null);
        require(status.getBoolean("madeForKids", false) && status.getBoolean("embeddable", false));
        require("processed".equals(status.getString("uploadStatus", "")));
        String privacy = status.getString("privacyStatus", "");
        require("public".equals(privacy) || "unlisted".equals(privacy));
        require("none".equals(snippet.getString("liveBroadcastContent", "")));
        JsonValue ratings = details.get("contentRating");
        require(ratings == null || !"ytAgeRestricted".equals(ratings.getString("ytRating", "")));
        JsonValue region = details.get("regionRestriction");
        if (region != null) {
            JsonValue allowed = region.get("allowed"), blocked = region.get("blocked");
            require(allowed == null || contains(allowed, country));
            require(blocked == null || !contains(blocked, country));
        }
        long seconds = Duration.parse(details.getString("duration", "")).getSeconds();
        require(seconds > 0 && seconds <= 1800 && seconds <= plannedSeconds);
        return (int) seconds;
    }
    private static boolean contains(JsonValue array, String value) {
        require(array.isArray());
        for (JsonValue row : array) if (value.equals(row.asString())) return true;
        return false;
    }
    private static void require(boolean condition) {
        if (!condition) throw new IllegalArgumentException("Video not eligible for embedded playback");
    }
}
