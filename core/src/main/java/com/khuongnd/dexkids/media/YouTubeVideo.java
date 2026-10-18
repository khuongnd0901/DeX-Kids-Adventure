package com.khuongnd.dexkids.media;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience;

/** Parent-authored local review, never YouTube metadata or an MFK certification. */
public record YouTubeVideo(String videoId, String title, Audience audience,
                           int minAge, int maxAge, int plannedSeconds,
                           boolean approved, boolean enabled, String recall) {
    public YouTubeVideo {
        if (videoId == null || !videoId.matches("[A-Za-z0-9_-]{11}") || title == null ||
            title.isBlank() || title.length() > 160 || audience == null || minAge < 4 ||
            maxAge > 7 || minAge > maxAge || plannedSeconds < 1 || plannedSeconds > 1800 ||
            recall == null || recall.length() > 400 || recall.split("\\R", -1).length > 2)
            throw new IllegalArgumentException("Invalid local video review");
    }

    public String url() { return "https://www.youtube.com/watch?v=" + videoId; }

    public boolean eligible(Audience viewers, int sauAge, int ongAge) {
        if (!approved || !enabled || viewers == null) return false;
        if (viewers == Audience.BOTH)
            return audience == Audience.BOTH && accepts(sauAge) && accepts(ongAge);
        return (audience == Audience.BOTH || audience == viewers) &&
            accepts(viewers == Audience.SAU ? sauAge : ongAge);
    }
    private boolean accepts(int age) { return age >= minAge && age <= maxAge; }

    /** Only exact supported hosts and a single video; playlists never auto-expand. */
    public static String idFromInput(String input) {
        if (input == null || input.length() > 2048) throw new IllegalArgumentException("Video URL required");
        String text = input.trim();
        if (text.matches("[A-Za-z0-9_-]{11}")) return text;
        try {
            URI uri = new URI(text);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null ||
                uri.getPort() != -1 || uri.getFragment() != null || uri.getHost() == null)
                throw new IllegalArgumentException("Canonical HTTPS YouTube URL required");
            String host = uri.getHost().toLowerCase(java.util.Locale.ROOT);
            String path = uri.getRawPath();
            String id = null;
            if (host.equals("youtu.be")) {
                if (path.matches("/[A-Za-z0-9_-]{11}")) id = path.substring(1);
            } else if (host.equals("youtube.com") || host.equals("www.youtube.com") || host.equals("m.youtube.com")) {
                if (path.matches("/(shorts|embed)/[A-Za-z0-9_-]{11}"))
                    id = path.substring(path.lastIndexOf('/') + 1);
                else if (path.equals("/watch") && uri.getRawQuery() != null) {
                    for (String pair : uri.getRawQuery().split("&")) {
                        String[] kv = pair.split("=", 2);
                        String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                        if (key.equals("v")) {
                            if (id != null || kv.length != 2) throw new IllegalArgumentException("Ambiguous video");
                            id = URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name());
                        }
                    }
                }
            }
            if (id == null || !id.matches("[A-Za-z0-9_-]{11}"))
                throw new IllegalArgumentException("Single YouTube video required");
            return id;
        } catch (java.net.URISyntaxException | java.io.UnsupportedEncodingException error) {
            throw new IllegalArgumentException("Invalid video URL", error);
        }
    }
}
