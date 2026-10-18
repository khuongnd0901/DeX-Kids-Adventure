package com.khuongnd.dexkids.media;

import java.util.List;
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience;

/** Pure selection. A launch is a request, never evidence a video played or ended. */
public final class YouTubeInterlude {
    public record Boundary(int completedQuestions, boolean busy, boolean journeyReady,
                           boolean online, boolean externalDisplay, boolean enabled,
                           int every, int step, String lastVideoId, Audience audience,
                           int sauAge, int ongAge, long nowMillis, List<Launch> launches) {}
    public record Launch(long atMillis, int plannedSeconds) {}
    public record Pick(boolean boundaryConsumed, int nextStep, YouTubeVideo video) {}
    private YouTubeInterlude() {}
    public static Pick choose(Boundary b, List<YouTubeVideo> catalog) {
        if (b.completedQuestions < 5 || b.busy) return new Pick(false, b.step, null);
        int every = b.every == 2 || b.every == 4 ? b.every : 0;
        int step = every == 0 ? 0 : Math.floorMod(b.step, every);
        int next = every == 0 ? 0 : (step + 1) % every;
        if (!b.enabled) return new Pick(true, 0, null);
        if (every == 0 || next != 0 || !b.journeyReady || !b.online || !b.externalDisplay)
            return new Pick(true, next, null);
        int count = 0; long seconds = 0;
        for (Launch launch : b.launches) {
            if (launch.atMillis > b.nowMillis || launch.plannedSeconds < 1)
                return new Pick(true, next, null); // Clock rollback / corrupt journal: fail closed.
            if (b.nowMillis - launch.atMillis < 3_600_000L) { count++; seconds += launch.plannedSeconds; }
        }
        if (count >= 2) return new Pick(true, next, null);
        List<YouTubeVideo> eligible = catalog.stream().filter(v ->
            v.eligible(b.audience, b.sauAge, b.ongAge)).collect(java.util.stream.Collectors.toList());
        int offset = 0;
        for (int i = 0; i < eligible.size(); i++)
            if (eligible.get(i).videoId().equals(b.lastVideoId)) offset = (i + 1) % eligible.size();
        for (int i = 0; i < eligible.size(); i++) {
            YouTubeVideo video = eligible.get((offset + i) % eligible.size());
            if (seconds + video.plannedSeconds() <= 900) return new Pick(true, next, video);
        }
        return new Pick(true, next, null);
    }
}
