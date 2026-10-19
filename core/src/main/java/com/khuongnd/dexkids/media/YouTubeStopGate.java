package com.khuongnd.dexkids.media;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/** Confirmation is scoped to the exact sessions that received the pause command. */
public final class YouTubeStopGate {
    public static final String PACKAGE = "com.google.android.youtube";
    public record Session(Object token, String packageName, boolean pausedOrStopped) {}
    private YouTubeStopGate() {}
    public static boolean confirmed(Set<?> commandedTokens, List<Session> current) {
        if (commandedTokens == null || commandedTokens.isEmpty() || current == null) return false;
        Set<Object> actual = new HashSet<>();
        for (Session session : current) {
            if (!PACKAGE.equals(session.packageName())) continue;
            if (!session.pausedOrStopped()) return false;
            actual.add(session.token());
        }
        return actual.equals(commandedTokens);
    }
}
