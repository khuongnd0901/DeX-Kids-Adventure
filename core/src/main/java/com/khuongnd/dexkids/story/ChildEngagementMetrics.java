package com.khuongnd.dexkids.story;

/**
 * T-025 objective in-memory session counters for parent-only QA.
 * These numbers measure app output, NOT a child's interest, attention or identity.
 * No transcript, location, timestamp history or network upload.
 */
public final class ChildEngagementMetrics {
    public record Snapshot(int sauBeats, int ongBeats, int togetherBeats,
                           int resolutions, int poiPreemptions, int otherCancellations,
                           int voiceUnavailable, int parentPauses) {
        public int starts() { return sauBeats + ongBeats + togetherBeats; }
        public int unfinished() { return Math.max(0, starts() - resolutions - poiPreemptions - otherCancellations); }
    }

    private int sau, ong, together, resolved, preempted, cancelled, voiceUnavailable, pauses;
    private boolean beatPending;

    /** Returns false only if the previous beat has not been resolved or interrupted. */
    public boolean recordBeatStart(EntertainmentDirector.Audience focus) {
        if (focus == null) throw new IllegalArgumentException("focus");
        if (beatPending) return false;
        switch (focus) {
            case SAU -> sau++;
            case ONG -> ong++;
            case BOTH -> together++;
        }
        beatPending = true;
        return true;
    }
    public boolean hasPendingBeat() { return beatPending; }
    public boolean completeBeat() {
        if (!beatPending) return false;
        beatPending = false;
        resolved++;
        return true;
    }
    public boolean interruptForPoi() {
        if (!beatPending) return false;
        beatPending = false;
        preempted++;
        return true;
    }
    /** Cancelled by a non-POI scripted card: not counted as a GPS interruption. */
    public boolean cancelBeat() {
        if (!beatPending) return false;
        beatPending = false;
        cancelled++;
        return true;
    }
    public void recordVoiceUnavailable() { voiceUnavailable++; }
    public void recordParentPause() {
        cancelBeat();
        pauses++;
    }
    public Snapshot snapshot() {
        return new Snapshot(sau, ong, together, resolved, preempted, cancelled, voiceUnavailable, pauses);
    }
}
