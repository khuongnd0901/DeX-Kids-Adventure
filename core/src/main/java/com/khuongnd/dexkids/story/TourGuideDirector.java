package com.khuongnd.dexkids.story;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/** Stateless regarding location: never generates facts from untrusted coordinates. */
public final class TourGuideDirector {
    private final Map<String, Long> lastPlayed = new HashMap<>();
    private final long minimumGapMs;
    private long latestAny = Long.MIN_VALUE;
    public TourGuideDirector(long cooldownMillis) {
        if (cooldownMillis < 0) throw new IllegalArgumentException("cooldown");
        minimumGapMs = cooldownMillis;
    }
    public Optional<NarrationCue> select(NarrationCue cue, int age, boolean verifiedPoiPresent,
                                          long nowMillis) {
        if (age < cue.minimumAge() || age > cue.maximumAge()) return Optional.empty();
        if (!cue.isGeneric() && !verifiedPoiPresent) return Optional.empty();
        Long last = lastPlayed.get(cue.id());
        if (last != null && nowMillis >= last && nowMillis - last < minimumGapMs) return Optional.empty();
        if (latestAny != Long.MIN_VALUE && nowMillis >= latestAny &&
                nowMillis - latestAny < minimumGapMs) return Optional.empty();
        if (last != null && nowMillis < last || nowMillis < latestAny) return Optional.empty();
        lastPlayed.put(cue.id(), nowMillis);
        latestAny = nowMillis;
        return Optional.of(cue);
    }
    public void resetSession() { lastPlayed.clear(); latestAny = Long.MIN_VALUE; }
}
